/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen
  Copyright (C) 2006, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.wcet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.verifier.structurals.ControlFlowGraph;
import org.apache.bcel.verifier.structurals.ExecutionVisitor;
import org.apache.bcel.verifier.structurals.InstConstraintVisitor;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.Pair;
import com.jopdesign.dfa.analyses.LoopBounds.ValueMapping;
import com.jopdesign.dfa.framework.Context;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.HashedString;

/**
 * It has a HashMap of WCETBasicBlocks. The class have methods that are called
 * from the WCETAnalyzers controlFlowGraph method. It creates the the directed
 * graph of wcbbs.
 *
 * @author rup,ms
 */

// History:
// 2006-06-22 rup: Extracted as from WCETAnalyser on wish from ms
class WCETMethodBlock {
	public int lpresult;
	public boolean analyzed = false; // set true when controlflow and directedgraph has been run
	public StringBuffer codeString; // is set in toString();
	StringBuffer lsobj;

	// list of BBs that are loopcontrollers
	ArrayList loopcontrollers = new ArrayList();

	int wcet = -1; // wcet count

	final int mid; // a unique id across the app
	//  event id that is incremented each time the method is invoked
	//  in a global context
	int E = 0;

	// Basic Blocks
	TreeMap bbs;

	JavaClass jc;

	// TODO WCETMethodBlock should probably extend MethodInfo
	MethodInfo mi;
//	Method methodbcel;
//
//	MethodGen mg;

	ControlFlowGraph cfg;

//	ConstantPoolGen cpg;

	String tostr;

	String signature;

	String name;

	String cname;

	WCETAnalyser wca;

	String lpf = null;

	String dotf = null;

	String[] codeLines = null;

	// directed graph of the basic blocks
	int dg[][];

	// method size in 32 bit words
	public int n = 0;

	public int wcetlp;

	static HashMap wcetvars;

	public WCETBasicBlock S;

	public WCETBasicBlock T;

	public boolean leaf = true; // if no invokes out from anly bb

	// create a bb covering the whole method
	// from here on we split it when necessary
	public void init(InstructionHandle stih, InstructionHandle endih) {
		WCETBasicBlock wcbb = null;
		if (stih.getInstruction() instanceof InvokeInstruction
				&& (((InvokeInstruction) stih.getInstruction())
						.getClassName(getCpg())).indexOf("Native") == -1) {
			wcbb = new WCETBasicBlock(stih, endih, this, WCETBasicBlock.INODE);
		} else {
			wcbb = new WCETBasicBlock(stih, endih, this, WCETBasicBlock.BNODE);
		}
		S.sucbb = wcbb;
		bbs.put(new Integer(wcbb.getStart()), wcbb);
	}

	/**
	 * Instanciated from from <code>SetClassInfo</code>.
	 */
	public WCETMethodBlock(MethodInfo mi, WCETAnalyser wca) {
		//System.out.println("WCMB CONSTR putting: "+jc.getClassName()+"."+method.getName());
		//if(method.getName().equals("printLn"))
		//  System.out.println("HELLO");
		this.mi = mi;
		wca.mtowcmb.put(mi, this);
		this.wca = wca;
		mid = wca.idtmp++;

		bbs = new TreeMap();
		name = mi.getMethod().getName();
		this.jc = mi.getCli().clazz;
		cname = jc.getClassName();
		//System.out.println("sourcefilename: "+ jc.getSourceFileName());

	}

	public void check() {
		if (!analyzed) {
			//System.out.println("about to check:"+name);
			//System.out.println("abstract:"+methodbcel.isAbstract());

			controlFlowGraph();

			Method methodbcel = mi.getMethod();
			//method length in words
			if (!methodbcel.isAbstract()) {
				n = (methodbcel.getCode().getCode().length + 3) / 4;
				//LImethod.getLineNumberTable()
				String methodId = methodbcel.getName()
						+ methodbcel.getSignature();
				String classId = jc.getClassName();
				String srcFile = jc.getSourceFileName();
				String filePath = (String) wca.javaFilePathMap.get(classId);
				if (filePath == null) {

					System.out.println("Did not find file:" + srcFile
							+ " class:" + classId + " package:"
							+ jc.getPackageName());
					System.exit(-1);
				}
				//codeLines = (String[])wca.filePathcodeLines.get(filePath);
				//  if(codeLines!=null)
				//    System.out.println("FILEPATH HIT");
				// if(codeLines == null){
				try {
					codeLines = new String[0];
					BufferedReader in = new BufferedReader(new FileReader(
							filePath));
					String str;
					ArrayList al = new ArrayList();
					int line = 0;

					LineNumberTable lnt = methodbcel.getLineNumberTable();
					int startLine = lnt.getSourceLine(getBbs(1).stih
							.getPosition());
					int endLine = lnt
							.getSourceLine(getBbs(bbs.size() - 2).endih
									.getPosition());

					while ((str = in.readLine()) != null) {
						line++;
						if (line >= startLine && line <= endLine) {
							if (str.trim().startsWith("for (")
									|| str.trim().startsWith("while (")
									|| str.trim().startsWith("for(")
									|| str.trim().startsWith("while(")) {
								if (str.indexOf("@WCA") == -1) {
									System.out
											.println("Error: no WCA annotation on line "
													+ line
													+ " in "
													+ filePath
													+ ":" + str);
									// System.exit(-1); // can be commented out to force continuation
									System.out
											.println("Default annotation inserted: \"//@WCA loop=9999\"");
									str += "// @WCA loop=9999";
								}
							}

							// Trevor pointed out that do while constructs does not give correct WCET
							if (str.trim().startsWith("do{")
									|| str.trim().startsWith("do {")) {
								System.out
										.println("Error: WCA does not support do{}while() at this point.");
								System.out
										.println("Please rewrite the code around line "
												+ line
												+ " in "
												+ filePath
												+ ":" + str);
								System.exit(-1); // can be commented out to force continuation
							}

						}
						al.add(str);
					}
					codeLines = (String[]) al.toArray(new String[0]);
					wca.filePathcodeLines.put(filePath, codeLines);
					in.close();
				} catch (IOException e) {
				}
				//}

			} else {
				n = 0;
			}
			//System.out.println("dg for:"+name);
			directedGraph();
			//System.out.println("link for:"+name);
			link();
			//System.out.println("about to string:"+name);
			wca.wcasb.append(toString());

			if (!wca.cfgwcmbs.contains(this))
				wca.cfgwcmbs.add(this);
			wca.dotout.print("\tdot -Tps " + dotf + " > "
					+ dotf.substring(0, dotf.length() - 4) + ".eps\n");
			analyzed = true;

		}
	}

	/**
	 * Control flow analysis for one nonabstract-method.
	 */
	public void controlFlowGraph() {

		// Some methods overridden (see bottom of this file)
		InstConstraintVisitor icv = new AnInstConstraintVisitor();

		icv.setConstantPoolGen(mi.getConstantPoolGen());

		ExecutionVisitor ev = new ExecutionVisitor();
		ev.setConstantPoolGen(mi.getConstantPoolGen());

		MethodGen mg = mi.getMethodGen();

		icv.setMethodGen(mg);
		if (!(mg.isAbstract() || mg.isNative())) { // IF mg HAS CODE
			mg.getInstructionList().setPositions(true);
			S = new WCETBasicBlock(this, WCETBasicBlock.SNODE);
			bbs.put(new Integer(Integer.MIN_VALUE), S);
			T = new WCETBasicBlock(this, WCETBasicBlock.TNODE);
			// pass 0: Create basic blocks
			InstructionHandle ih = mg.getInstructionList().getStart();
			// wcet startup: create the first full covering bb
			InstructionHandle ihend = mg.getInstructionList().getEnd();
			if (ihend.getInstruction() instanceof NOP) {
				System.out.println("Info: "+mi.methodId+" ends with a nop! remove it");
				try {
					mg.getInstructionList().delete(ihend);
				} catch (TargetLostException e) {
					e.printStackTrace();
					System.exit(-1);
				}
				mg.getInstructionList().setPositions(true);
				ihend = mg.getInstructionList().getEnd();
			}
			init(ih, ihend);

			do {
				// create new bb (a)for branch target and (b) for sucessor
				Instruction ins = ih.getInstruction();

				if (ih.getInstruction() instanceof InvokeInstruction
						&& (((InvokeInstruction) ih.getInstruction())
								.getClassName(getCpg())).indexOf("Native") == -1) {
					//System.out.println("classname:"+((InvokeInstruction)ih.getInstruction()).getClassName(getCpg()));
					//System.out.println("wca.nativeClass:"+wca.nativeClass);
					//System.out.println("INVOKEINSTRUCTION");
					createBasicBlock(ih);
					createBasicBlock(ih.getNext());
				} else if (ih.getInstruction() instanceof BranchInstruction) {
					InstructionHandle ihtar = ((BranchInstruction) ih
							.getInstruction()).getTarget();
					InstructionHandle ihnext = ih.getNext();
					createBasicBlock(ihtar);
					if (ihnext != null) {
						createBasicBlock(ihnext);
					}
				}
			} while ((ih = ih.getNext()) != null);

			// Pass 1: Set the id of each block
			int bid = 0;
			// it is sorted on the (final) start pos of each block
			for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
				WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get(
						(Integer) iter.next());
				wbb.calculateWcet();
				wbb.setBid(bid);

				bid++;

				if (wbb.nodetype != WCETBasicBlock.SNODE
						&& wbb.nodetype != WCETBasicBlock.TNODE) {
					ih = wbb.getEndih();
					WCETBasicBlock wbbthis = getCoveringBB(ih);

					if (ih.getInstruction() instanceof BranchInstruction) {
						// target
						InstructionHandle ihtar = ((BranchInstruction) ih
								.getInstruction()).getTarget();
						WCETBasicBlock wbbtar = getCoveringBB(ihtar);
						// target wbb
						wbbthis.setTarbb(wbbtar);
						// targeter in target
						wbbtar.addTargeter(wbbthis);

						// next when the instruction is an if
						// TODO: What about TABLESWITCH and LOOKUPSWITCH
						if (ih.getInstruction() instanceof IfInstruction) {
							InstructionHandle ihnext = ih.getNext();
							if (ihnext != null) {
								WCETBasicBlock wbbnxt = getCoveringBB(ihnext);
								// nextwbb
								wbbthis.setSucbb(wbbnxt);
							}
						}
					} else if (ih.getInstruction() instanceof ReturnInstruction) {
						// TODO: set T node here
						if (T == null)
							System.out.println("T=null");
						if (wbbthis == null)
							System.out.println("wbbthis=null");

						wbbthis.sucbb = T;
						T.addTargeter(wbbthis);
					} else { // set the successor
						InstructionHandle ihnext = ih.getNext();

						if (ihnext != null) {
							WCETBasicBlock wbbnxt = getCoveringBB(ihnext);
							// nextwbb
							wbbthis.setSucbb(wbbnxt);
						}
					}
				}
			}

			bbs.put(new Integer(Integer.MAX_VALUE), T);

			T.bid = bid;

			TreeMap newbbs = new TreeMap();
			//System.out.println("cfg for:"+name);
			for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
				WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get(
						(Integer) iter.next());
				if (wbb.nodetype != WCETBasicBlock.SNODE
						&& wbb.nodetype != WCETBasicBlock.TNODE) {
					if (wbb.stih.getInstruction() instanceof InvokeInstruction) {
						wbb.nodetype = WCETBasicBlock.INODE;
						String methodid = ((InvokeInstruction) wbb.stih
								.getInstruction()).getClassName(getCpg())
								+ "."
								+ ((InvokeInstruction) wbb.stih
										.getInstruction())
										.getMethodName(getCpg())
								+ ((InvokeInstruction) wbb.stih
										.getInstruction())
										.getSignature(getCpg());
						String retsig = ((InvokeInstruction) wbb.stih
								.getInstruction()).getReturnType(getCpg())
								.getSignature();

						//signature Java Type, Z boolean, B byte, C char, S short, I int
						//J long, F float, D double, L fully-qualified-class, [ type type[]
						//System.out.println("found inode and wbb.bbinvo="+methodid);
						wbb.bbinvo = methodid;
						//System.out.println("-bbinvo:"+wbb.bbinvo);
						//System.out.println("-name:"+name);
						//System.out.println("-cname:"+cname);
						//System.out.println("-IDS:"+wbb.getIDS());

					} else {
						wbb.nodetype = WCETBasicBlock.BNODE;
						wbb.bbinvo = null;
					}
					//TODO: research how this should be done (athrow discussion with ms)
					if (wbb.getInbbs().size() == 0) {
						System.out.println("Warning : no flow to "
								+ wbb.getIDS() + " in " + wbb.wcmb.cname + "."
								+ wbb.wcmb.name);
						//wbb.addTargeter(S);
						wbb.addTargeter(wbb);
					}
				}
				newbbs.put(new Integer(wbb.bid), wbb);
				//System.out.println("CFG putting "+wbb.bid+" in newbbs. Nodetype:"+wbb.nodetype);
			}
			bbs = newbbs;
			//bbs.put(new Integer(T.bid),T);

			// error if no blocks point to T
			// example: wcet.kflapp.Mast.doService(): for(;;)...
			if (T.getInbbs().size() == 0) {
				System.out.println("Error: No basic blocks point to T in "
						+ cname + "." + name);
				System.exit(-1);
			}
		}
	}

	public void link() {
		// set up the  loop controllers
		//   WCMB: Arraylist of <WCBB> loopcontrollers
		//     WCBB: ArrayList of <ArrayList> of loopchains
		//       loopchain: ArrayList <WCBB> of WCBB in chain
		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
			if (wcbb.loopcontroller) {
				wcbb.createLoopChains();
				loopcontrollers.add(wcbb);
			}
		}

		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);

			// work-around - should use ClassInfo
			JavaClass jca[] = wca.toClassArray();

			// hook the called method to the outgoing node
			if (wcbb.nodetype == WCETBasicBlock.INODE) {
//				System.out.println("+linking inode in:"+name);
//				System.out.println("+class:"+cname);
//				System.out.println("+and linking bbinvo:"+wcbb.bbinvo);
//				if(wca.getMethod(wcbb.bbinvo)==null){
//				  System.out.println("wca.getMethod(wcbb.bbinvo) == null");
//				}
//				else
//				  System.out.println(wca.getMethod(wcbb.bbinvo).getMethod().getName());
//				if(wca.getWCMB(wca.getMethod(wcbb.bbinvo))==null){
//				  System.out.println("wca.getWCMB(wca.getMethod(wcbb.bbinvo)) == null");
//				}
				wcbb.invowcmb = wca.getWCMB(wca.getMethod(wcbb.bbinvo));
				if (wcbb.invowcmb == null)
					System.out.println("Info: " + wcbb.bbinvo
							+ " link target == null (probably inherited)");
				//else
				//  System.out.println("link target:"+wcbb.invowcmb.name);
				if (wcbb.invowcmb == null) { //check super class(es)
				//System.out.println("jc:"+jc.getClassName());
				//System.out.println("method:"+methodbcel.getName());
				//System.out.println("sig:"+methodbcel.getSignature());
				//System.out.println("wcbb.invowcmb0==null");
					String bbinvotmp = wcbb.bbinvo;
					//System.out.println("bbinvotmp0:"+bbinvotmp);
					String jcinvostr = wcbb.bbinvo.substring(0, wcbb.bbinvo
							.lastIndexOf('.'));
					String minvo = wcbb.bbinvo.substring(wcbb.bbinvo
							.lastIndexOf('.'));
					//System.out.println("minvo:"+minvo);
					JavaClass jcinvo = null;
					//System.out.println("jcinvo:"+jcinvo);
					for (int i = 0; i < jca.length; i++) {
						if (jca[i].getClassName().equals(jcinvostr)) {
							jcinvo = jca[i];
							//System.out.println("found jcinvo");
							break;
						}
					}

					bbinvotmp = jcinvo.getSuperclassName() + minvo;
					System.out.println("Info: found match in " + bbinvotmp);
					//System.out.println("jcinvo:"+jcinvo.getClassName());
					wcbb.invowcmb = wca.getWCMB(wca.getMethod(bbinvotmp));
					//System.out.println("wcbb.invowcmb.name:"+wcbb.invowcmb.name);
					//System.out.println("wcbb.invowcmb.cname:"+wcbb.invowcmb.cname);
					wcbb.invowcmb.check();

					if (jcinvo == null || wcbb.invowcmb == null) {
						System.out
								.println("Could not resolve inheritance for: "
										+ jcinvostr);
						System.exit(-1);
					}

					//System.out.println("bbinvotmp1:"+bbinvotmp);

					if (wcbb.invowcmb != null)
						wcbb.bbinvo = bbinvotmp;
					else {
						System.out.println("Could not resolve " + wcbb.bbinvo
								+ " for linking in " + jc.getClassName() + "."
								+ name + ":" + wcbb.getIDS());
						System.out.println("jc abstract:" + jc.isAbstract());
					}
				} else if (wcbb.invowcmb.mi.getMethod().isAbstract()) { // such as jbe.ejip.LinkLayer.loop()
					System.out.println("* This method is abstract: "
							+ wcbb.invowcmb.cname + "." + wcbb.invowcmb.name);
					JavaClass jcabs = wcbb.invowcmb.jc;
					int classhits = 0;
					String mname = wcbb.bbinvo.substring(wcbb.bbinvo
							.lastIndexOf('.') + 1);
					//System.out.println("* mname:"+mname);
					wcbb.invowcmb = null;
					wcbb.bbinvo = null;
					for (int i = 0; i < jca.length; i++) {
						String newbbinvo = jca[i].getClassName() + "."
								+ mname;
						//System.out.println("* Checking for :"+newbbinvo);
						WCETMethodBlock invocandi = wca.getWCMB(wca.getMethod(newbbinvo));
//								.getMethodB(newbbinvo));
						if (invocandi != null
								&& !invocandi.mi.getMethod().isAbstract()) {
							//System.out.println("** "+invocandi.jc.getSuperclassName());
							//System.out.println("** "+jcabs.getClassName());

							if (invocandi.jc.getSuperclassName().equals(
									jcabs.getClassName())) {
								wcbb.invowcmb = invocandi;
								wcbb.bbinvo = newbbinvo;
								System.out
										.println("* Implementatin of abstract method "
												+ wcbb.bbinvo
												+ " found in "
												+ newbbinvo);
								classhits++;
							}
						}
					}
					if (classhits > 1) {
						System.out
								.println("Error (sorry...): multiple implementations of "
										+ wcbb.bbinvo
										+ " : "
										+ jc.getClassName()
										+ "."
										+ name
										+ ":"
										+ wcbb.getIDS()
										+ " is not supported yet");
						//System.exit(-1);
						System.out
								.println("Keeping last match...(until correctly implemented...)");
					}

					wcbb.invowcmb.check();
				} else
					wcbb.invowcmb.check();

				leaf = false;

				// backtrack
				/*        ArrayList wcbbs = new ArrayList();
				 wcbbs.add(wcbb);
				 WCETBasicBlock lcwcbb = null;
				 while(wcbbs.size()>0){
				 WCETBasicBlock curwcbb = (WCETBasicBlock)wcbbs.get(0);
				 WCETBasicBlock[] tarbb = curwcbb.getInBBSArray();
				 for (int i=0;i<tarbb.length;i++){
				 if(tarbb[i].loopcontroller){
				 lcwcbb = tarbb[i];
				 wcbbs.clear();
				 break;
				 }
				 if(tarbb[i].nodetype != WCETBasicBlock.SNODE || tarbb[i].nodetype != WCETBasicBlock.TNODE){
				 wcbbs.add(tarbb[i]);
				 }
				 }
				 }*/

			}
		}
		/*//TODO: discuss with ms
		 // if there are any path that leads back to the INODE that has only one loopcontroller
		 for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
		 Integer keyInt = (Integer) iter.next();
		 WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
		 if(wcbb.nodetype == WCETBasicBlock.INODE && wca.global){
		 if(wcbb.invowcmb.leaf){

		 System.out.println("invowcmb from "+cname+"."+name+"("+wcbb.getIDS()+")"+":"+wcbb.invowcmb.cname+"."+wcbb.invowcmb.name+" is a leaf");
		 //  we have a candidate

		 //  dismiss if there is an invoblock in an inner loop
		 ArrayList okl = new ArrayList();  //  ok
		 ArrayList pl = new ArrayList(); //  pending
		 ArrayList link = new ArrayList();
		 link.add(wcbb);
		 pl.add(link);
		 while(pl.size()>0){
		 link = (ArrayList)pl.get(0);
		 System.out.println("\na:"+ WU.printChain(link));
		 WCETBasicBlock wcbbinvo = (WCETBasicBlock)link.get(0);
		 WCETBasicBlock wcbblast = (WCETBasicBlock)link.get(link.size()-1);

		 //first check the chain
		 if(wcbbnext == wcbbinvo){
		 System.out.println("removing  pl(0). pll.size:"+pl.size());
		 okl.add(pl.remove(0)); // the max 1 candidate chain saved
		 System.out.println("removed  pl(0). pll.size:"+pl.size());
		 if(pl.size()>0)
		 System.out.println("pl(0):"+WU.printChain((ArrayList)pl.get(0)));
		 }
		 else if(link.contains(wcbbnext)){
		 pl.remove(0);
		 }
		 else
		 link.add(wcbbnext);
		 }}}

		 //then advance it

		 if(wcbblast.sucbb != null && wcbblast.tarbb != null){
		 ArrayList linkclone = (ArrayList)link.clone();
		 pl.add(linkclone);
		 }
		 WCETBasicBlock wcbbnext = null;

		 if(wcbblast.sucbb != null){
		 wcbbnext = wcbblast.sucbb;
		 }else if(wcbblast.tarbb != null){
		 wcbbnext = wcbblast.tarbb;
		 }else{ // T node
		 System.out.println("T:"+ WU.printChain(link));
		 pl.remove(0);
		 }
		 System.out.println("b:"+ WU.printChain(link));
		 System.out.println("wcbbnext:"+wcbbnext.getIDS());
		 System.out.println("wcbbinvo:"+wcbbinvo.getIDS());
		 if(wcbbnext != null){
		 if(wcbbnext == wcbbinvo){
		 System.out.println("removing  pl(0). pll.size:"+pl.size());
		 okl.add(pl.remove(0)); // the max 1 candidate chain saved
		 System.out.println("removed  pl(0). pll.size:"+pl.size());
		 if(pl.size()>0)
		 System.out.println("pl(0):"+WU.printChain((ArrayList)pl.get(0)));
		 }
		 else if(link.contains(wcbbnext)){
		 pl.remove(0);
		 }
		 else
		 link.add(wcbbnext);

		 //              else if(wcbbnext.loopcontroller){ // check that it is the first loopcontroller
		 //                  boolean onlylc = true;
		 //                  for (int i=1;i<link.size();i++){
		 //                    if(((WCETBasicBlock)link.get(i)).loopcontroller){
		 //                      onlylc = false;
		 //                      break;
		 //                    }
		 //                  }
		 //                  if(onlylc){
		 //                    link.add(wcbbnext);
		 //                    wcbbinvo.innerlc = wcbbnext.bid;
		 //                  }
		 //                  else
		 //                    pl.remove(0);
		 //              } else if(wcbbnext.nodetype == WCETBasicBlock.INODE){
		 //                if(wcbbnext.invowcmb == wcbbinvo.invowcmb)
		 //                  link.add(wcbbnext); // ok to invoke the same method (one cache miss is false, however)
		 //                else
		 //                  pl.remove(0);
		 //              }
		 //              else{
		 //                link.add(wcbbnext);
		 //              }
		 }
		 }

		 System.out.println("#ok chains:"+okl.size());
		 for (int i=0;i<okl.size();i++){
		 link = (ArrayList)okl.get(i);
		 System.out.println(WU.printChain(link));
		 }

		 while(okl.size()>0){
		 // conservative: require that it is the only invo block for all loops
		 // ok to have it multiple times (just a more conservative estimate)
		 link = (ArrayList)okl.get(0);
		 WCETBasicBlock wcbbinvo = (WCETBasicBlock)link.get(0);
		 wcbbinvo.innerinode = true;
		 for (int i=1;i<link.size()-1;i++){
		 WCETBasicBlock wcbbtest = (WCETBasicBlock)link.get(i);
		 if(wcbbtest.nodetype == WCETBasicBlock.INODE && wcbbtest != wcbbinvo)
		 wcbbinvo.innerinode = false;
		 }
		 }
		 } else{
		 wcbb.innerinode = false;
		 }
		 }
		 }
		 */
	}

	public ControlFlowGraph getControlFlowGraph() {
		return cfg;
	}

	public MethodGen getMethodGen() {
		return mi.getMethodGen();
	}

	/**
	 * Find a local variable based on an entry in the LocalVariableTable attribute.
	 * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#5956
	 * @param index
	 * @param pc
	 * @return local variable type and name or "NA"
	 */
	public String getLocalVarName(int index, int pc) {
		//System.out.println("getLocalVarName: index:"+index+" pc:"+pc+" info:"+mg.getClassName()+"."+mg.getName());
		LocalVariableTable lvt = mi.getMethod().getLocalVariableTable();
		String lvName = "";
		boolean match = false;
		if (lvt != null) {
			LocalVariable[] lva = lvt.getLocalVariableTable();

			//System.out.println("lva.length:"+lva.length);
			for (int i = 0; i < lva.length; i++) {
				LocalVariable lv = lva[i];
				//System.out.println("lv["+i+"]: index:"+lv.getIndex()+" name:"+lv.getName()+" pcstart:"+pc+" length:"+lv.getLength());
				if (lv.getIndex() == index) {
					//System.out.println("index match");
					if (pc >= lv.getStartPC()) {
						//System.out.println("startpc match");
						if (pc <= lv.getStartPC() + lv.getLength()) {
							//System.out.println("endpc match");
							if (match && !lvName.equals(lv.getSignature() + ":" + lv.getName())) {
								System.out
										.println("Only one match pr. local variable table is possible");
								System.exit(-1);
							}
							lvName = lv.getSignature() + ":" + lv.getName();
							match = true;
							//break; //safety check when commented out, but slower
						}
					}
				}
			}
		}
		return lvName;
	}

	/**
	 * Get the bb that currently covers the bytecode at the position. The design
	 * is such that some bb will always cover a bytecode. It may the the same bb
	 * that is returned if the branch points back (direct loop).
	 *
	 * @param pos
	 * @return covering bb
	 */
	public WCETBasicBlock getCoveringBB(InstructionHandle ih) {
		// if cov bb starts on pos then done otherwise run throug all keys
		WCETBasicBlock covbb = (WCETBasicBlock) bbs.get(new Integer(ih
				.getPosition()));
		if (covbb == null) {
			Iterator it = bbs.keySet().iterator();
			// find the cov. bb
			int bbpos = 0;
			while (it.hasNext()) {
				int apos = ((Integer) it.next()).intValue();
				if (apos < ih.getPosition() && apos > bbpos) {
					bbpos = apos;
				}
			}
			covbb = (WCETBasicBlock) bbs.get(new Integer(bbpos));
		}
		return covbb;
	}

	/**
	 * Create a new wcbb if none of the existing wcbbs are starting on that
	 * position. Call this method twice when you encounter a branch type byte
	 * code.
	 *
	 * @param start
	 *          the position to check for
	 * @return true if a wcbb was created
	 */
	public boolean createBasicBlock(InstructionHandle stih) {
		boolean res;
		// get the covering bb
		WCETBasicBlock covwcbb = getCoveringBB(stih);
		if (covwcbb.getStart() == stih.getPosition()) {
			// already a bb on start pos
			res = false;
		} else // create one by splitting the covering bb
		{
			WCETBasicBlock wcbb = covwcbb.split(stih);
			if ((stih.getInstruction() instanceof InvokeInstruction)
					&& !(((InvokeInstruction) stih.getInstruction())
							.getClassName(getCpg())).equals(wca.nativeClass)) {
				//System.out.println("inode:"+((InvokeInstruction)stih.getInstruction()).getClassName(getCpg()));
				wcbb.nodetype = WCETBasicBlock.INODE;
				leaf = false;
			}
			// save the new bb in the hash map
			if (bbs.put(new Integer(stih.getPosition()), wcbb) != null) {
				System.err.println("The starting pos should be unique.");
				System.exit(-1);
			}
			res = true;
		}
		return res;
	}

	public void createBasicBlock(int type) {
		if (type == WCETBasicBlock.SNODE) {
			S = new WCETBasicBlock(this, WCETBasicBlock.SNODE);
			bbs.put(new Integer(Integer.MIN_VALUE), S);
		}
		if (type == WCETBasicBlock.TNODE) {
			T = new WCETBasicBlock(this, WCETBasicBlock.TNODE);
			bbs.put(new Integer(Integer.MAX_VALUE), T);
		}
	}

	/**
	 * It sorts the basic blocks and creates the directed graph.
	 */
	public void directedGraph() {
		// now create the directed graph
		dg = new int[bbs.size()][bbs.size()];
		WCETBasicBlock.bba = new WCETBasicBlock[bbs.size() + 2];//TODO
		LineNumberTable lnt = mi.getMethod().getLineNumberTable();
		WCETBasicBlock pbb = null;
		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
			if (pbb != null)
				wcbb.prevbb = pbb;
			pbb = wcbb;
			if (wcbb.nodetype != WCETBasicBlock.SNODE
					&& wcbb.nodetype != WCETBasicBlock.TNODE)
				wcbb.line = lnt.getSourceLine(wcbb.endih.getPosition());
			else
				wcbb.line = -1;
			WCETBasicBlock tarwcbb = wcbb.getTarbb();
			int bid = wcbb.getBid();
			//System.out.println(bid + ":"+WCETBasicBlock.bba.length);
			WCETBasicBlock.bba[bid] = wcbb;
			if (tarwcbb != null) {
				int tarbbid = tarwcbb.getBid();
				tarwcbb.addTargeter(wcbb);
				dg[bid][tarbbid]++;
			}
			WCETBasicBlock sucbb = wcbb.getSucbb();
			if (sucbb != null) {// && sucbb.nodetype != WCETBasicBlock.TNODE) {
				int sucid = sucbb.getBid();
				sucbb.addTargeter(wcbb);
				dg[bid][sucid]++;
			}
		}

		HashSet lines = new HashSet();
		// find loopdrivers/loopcontrollers
		//System.out.println("\nmethod:"+method.getClass().getName()+"."+method.getName());
		//    System.out.println("METHOD:"+name);
		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {

			Integer keyInt = (Integer) iter.next();
			WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);

			//System.out.println("wcbb:"+wcbb.getIDS());
			//if(wcbb.line >=0)
			//  System.out.println("codeline:"+codeLines[wcbb.line-1]);
			//System.out.println("outer loop wcbb.id:"+wcbb.id);
			// identify loop controller candidate
			HashMap wcaA = null;
			//System.out.println("wcbb.wcmb.name:"+wcbb.wcmb.name);
			//System.out.println("wcbb.wcmb.cname:"+wcbb.wcmb.cname);
			//if(codeLines == null)
			//  System.out.println("codelines == null");
			if (wcbb.line >= 0)
				wcaA = WU.wcaA(codeLines[wcbb.line - 1]);
			if (wcaA != null) {
				if (wcaA.get("loop") != null) { // wcbb is now loopdriver

					if (((wcbb.sucbb != null || wcbb.tarbb != null) && (!wcbb.loopdriver || !wcbb.loopcontroller))
							&& !lines.contains(new Integer(wcbb.line))) {

						//System.out.println("loopdriver id:"+wcbb.id);
						//System.out.println("LOOPDRIVER:"+wcbb.getIDS());
						// find loopcontroller
						boolean set = false;
						WCETBasicBlock wcbbhit = wcbb;
						for (Iterator lciter = bbs.keySet().iterator(); lciter
								.hasNext();) {
							Integer lckeyInt = (Integer) lciter.next();
							WCETBasicBlock lcwcbb = (WCETBasicBlock) bbs
									.get(lckeyInt);
							if (set) {
								if (lcwcbb.sucbb != null
										&& lcwcbb.tarbb != null) {
									if (lcwcbb.line == wcbbhit.line) {
										//System.out.println("hit candidate:"+lcwcbb.id);
										wcbbhit = lcwcbb;
									}
								}
							}

							if (lcwcbb == wcbb) {
								//System.out.println("set on id:"+lcwcbb.id);
								set = true;
							}
						}
						// MS: I really don't undertstand the following code :-(
						//System.out.println("loop controller hit:"+wcbbhit.id);
						wcbb.loopdriver = true;
						wcbb.loopcontroller = false;
						wcbb.loop = Integer.parseInt((String) wcaA.get("loop"));
						wcbbhit.loopcontroller = true;
						String wcastr = (codeLines[wcbb.line - 1]
								.substring(codeLines[wcbb.line - 1]
										.indexOf("@WCA")));
						if (wcastr.indexOf("<=") != -1)
							wcbbhit.leq = true;
						wcbbhit.loopdriver = false;
						wcbbhit.loopid = wcbb.bid;
						wcbbhit.loop = Integer.parseInt((String) wcaA.get("loop"));
						wcbbhit.loopdriverwcbb = wcbb;
						lines.add(new Integer(wcbbhit.line));
						
						if (wca.useDfa) {
							System.out.println("Loop bounds from annotation: wcbb.loop="+wcbb.loop+" wcbbhit.loop="+wcbbhit.loop);
							// MS: it's the WRONG BB for the loop bound!
							InstructionHandle instr = wcbb.endih;
							// a very quick and dirty hack:
							instr = wcbb.sucbb.endih;
							int bound = wca.lb.getBound(wca, instr);
							if (bound==-1) {
								System.out.println("No DFA based bound available");
							} else {
								System.out.println("Loop bounds from DFA: "+bound);
								// now use our DFA bounds:
								wcbb.loop = bound;
								// MS: I don't understand that stuff:
								wcbbhit.loop = bound;
								wcbbhit.leq = true;	// what's the difference for WCET?							
							}							
						}
						
						//            if(wcaA.get("innerloop") != null){
						//              if(((String)wcaA.get("innerloop")).equals("true")){
						//System.out.println(wcbb.getIDS() +" is an inner loop controller");
						//                wcbb.innerloop = true;
						//              }
						//            }
					}
				}
			}

			//      if(wcbb.loopcontroller){
			//        HashMap tinbbs = wcbb.getInbbs();
			//        if(wcbb.bid > 0 && tinbbs.size()!=2){
			////          System.out.println("error in loopcontrol:"+wcbb.id);
			////          System.out.println("tinbbs.size:"+tinbbs.size());
			////          System.exit(-1);
			//        }
			//      }
		}
	}

	/**
	 * Converts the WCETMethodBasicBlock to a String.
	 *
	 * @return string representation of the MehtodBasicBlock
	 */
	public String toString() {
		codeString = new StringBuffer();

		codeString
				.append("******************************************************************************\n");
		codeString.append("WCET info for:" + jc.getClassName() + "."
				+ mi.getMethod().getName() + mi.getMethod().getSignature() + "\n\n");

		// directed graph
		codeString.append("Directed graph of basic blocks(row->column):\n");
		StringBuffer top = new StringBuffer();
		top.append(WU.prepad("", 4));

		for (int i = 0; i < dg.length; i++) {
			if (i < dg.length - 1)
				top.append(WU.postpad(getBbs(i).getIDS(), 4));
			else
				top.append(WU.postpad(getBbs(i).getIDS(), 4));
		}
		top.append("\n");

		for (int i = 0; i < top.length() - 3; i++) {
			codeString.append("=");
		}
		codeString.append("\n" + top.toString());

		for (int i = 0; i < dg.length; i++) {
			codeString.append(WU.postpad(getBbs(i).getIDS(), 3));

			for (int j = 0; j < dg.length; j++) {
				if (dg[i][j] == 0)
					codeString.append(" ."); // a space does not clutter it as much as a zero
				else
					codeString.append(" " + dg[i][j]);

				if (j < dg.length - 1)
					codeString.append(WU.postpad("", 2));
				else
					codeString.append(WU.postpad("", 2));
			}
			codeString.append("\n");
		}
		codeString.append(WU.repeat("=", top.length() - 3));
		codeString.append("\n");

		// bytecode listing
		codeString.append("\nTable of basic blocks' and instructions\n");
		codeString
				.append("=========================================================================\n");
		codeString
				.append("Block Addr.  Bytecode                Cycles    Cache miss     Misc. info\n");
		codeString
				.append("             [opcode]                        invoke  return\n");
		codeString
				.append("-------------------------------------------------------------------------\n");
		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
			//System.out.println("about to codestring this type of node:"+wcbb.nodetype);
			//System.out.println("ids:"+wcbb.getIDS());
			codeString.append(wcbb.toCodeString());
		}
		codeString
				.append("=========================================================================\n");
		codeString.append("Info: n=" + n + " r=" + WCETInstruction.r + " w=" + WCETInstruction.w + "\n");
		codeString.append("\n");
		//    if(wca.ls){
		//      sb.append(toLS(true, true, ""));
		//      WCETBasicBlock.linkbb(WCETBasicBlock.bba[0]);
		//      WCETBasicBlock.bbe();
		//      sb.append("\n"+toLinkBBS());
		//    }
		codeString.append(toLS(false, true, null));

		codeString.append(toDot(false));

		return codeString.toString();
	}

	public String toDot(boolean global) {
		// global if true then dot is appwide
		StringBuffer sb = new StringBuffer();
		// dot graph
		// use: dot -Tps graph.dot -o graph.ps
		boolean labels = true;

		sb.append("\n/*" + jc.getClassName() + "." + mi.getMethod().getName()
				+ mi.getMethod().getSignature() + "*/\n");
		if (!global) {
			sb.append("digraph G {\n");
			sb.append("size = \"10,7.5\"\n");
		}

		for (int i = 0; i < dg.length; i++) {
			for (int j = 0; j < dg.length; j++) {
				if (dg[i][j] > 0) {

					sb.append("\t" + getBbs(i).toDotFlowEdge(getBbs(j)));
					if (labels) {
						//sb.append(" [label=\""+dg[i][j]+"\"");
						String edge = getBbs(i).toDotFlowLabel(getBbs(j));

						if (wcetvars.get(edge) != null) {
							int edgeval = 0;
							try {
								edgeval = Integer.parseInt((String) wcetvars
										.get(edge));							
							} catch (Exception e) {
								System.out.println((String) wcetvars
								.get(edge)+" not an integer, assuming 0");
							}
							if (edgeval > 0)
								sb.append(" [label=\""
										+ getBbs(i).toDotFlowLabel(getBbs(j))
										+ "=" + edgeval + "\"");
							else
								sb.append(" [style=dashed,label=\""
										+ getBbs(i).toDotFlowLabel(getBbs(j))
										+ "=" + edgeval + "\"");
						} else
							sb.append(" [label=\""
									+ getBbs(i).toDotFlowLabel(getBbs(j))
									+ "=?\"");

						//sb.append(",labelfloat=true");
						sb.append("]");
					}
					sb.append(";\n");
				}
			}
		}

		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
			int id = wcbb.getBid();
			if (wcbb.nodetype != WCETBasicBlock.SNODE
					&& wcbb.nodetype != WCETBasicBlock.TNODE)
				sb.append("\t" + wcbb.getIDS() + " [label=\"" + wcbb.getIDS()
						+ "\\n" + wcbb.wcetHit + "\"];\n");
			else
				sb.append("\t" + wcbb.getIDS() + ";\n");
		}
		if (!global) {
			sb.append("}\n");
		}

		if (!global) {
			try {
				dotf = new File(wca.getOutFile()).getParentFile()
						.getAbsolutePath()
						+ File.separator
						+ jc.getClassName()
						+ "."
						+ mi.getMethod().getName() + ".dot";
				dotf = dotf.replace('<', '_');
				dotf = dotf.replace('>', '_');
				dotf = dotf.replace('\\', '/');
				PrintWriter dotout = new PrintWriter(new FileOutputStream(dotf));
				dotout.write(sb.toString());
				dotout.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}

		return sb.toString();

	}

	//TODO: loop follows loop controller?
	/**
	 * @param global follow the invokes
	 * @param term terminate with s=1, t=1
	 * @param invowcbb the invoking wcbb or null
	 */
	public String toLS(boolean global, boolean term, WCETBasicBlock invowcbb) {

		if (global)
			E++;
		StringBuffer ls = new StringBuffer();
		StringBuffer lsinvo = new StringBuffer();
		lsobj = new StringBuffer();

		ls.append("/* WCA flow constraints: " + name + " : M" + mid + " */\n");

		lsobj.append(toLSO());

		WCETBasicBlock wcbb = null;
		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			wcbb = (WCETBasicBlock) bbs.get(keyInt);
			//S
			if (wcbb.nodetype == WCETBasicBlock.SNODE)
				if (invowcbb != null)
					ls.append(wcbb.toLSS(invowcbb));
				else
					ls.append(wcbb.toLSS(null));
			else if (wcbb.nodetype == WCETBasicBlock.BNODE
					|| wcbb.nodetype == WCETBasicBlock.INODE) {
				ls.append(wcbb.toLSFlow());
				if (wcbb.loopcontroller)
					ls.append(wcbb.toLSLoop());
			} else if (wcbb.nodetype == WCETBasicBlock.TNODE) {
				if (invowcbb != null)
					ls.append(wcbb.toLST(invowcbb));
				else
					ls.append(wcbb.toLST(null));
			}

			if (wcbb.nodetype == WCETBasicBlock.INODE && global) {
				wcbb.invowcmb.check();
				lsinvo.append(wcbb.invowcmb.toLS(global, false, wcbb));
				ls.append(wcbb.toLSInvo());
				lsobj.append(" " + wcbb.invowcmb.getLSO());
			}
		}

		ls.append("/* WCA flow to cycle count */\n");
		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			wcbb = (WCETBasicBlock) bbs.get(keyInt);
			if (wcbb.nodetype == WCETBasicBlock.BNODE
					|| wcbb.nodetype == WCETBasicBlock.INODE) {
				ls.append(wcbb.toLSCycles());
			}
		}
		if (wcbb.invowcmb != null) {
			ls.append("/* Invocation(s) from " + name + ":[M" + mid + "] -> "
					+ wcbb.invowcmb.name + ":[M" + mid + "]*/\n");
			ls.append(lsinvo.toString());
		} else {
			ls.append("/* Invocation(s) from " + name + ":[M" + mid + "] */\n");
			ls.append(lsinvo.toString());
		}

		if (term) { // once
			//String lso = obs.toString();
			StringBuffer lso = new StringBuffer();
			lso.append("/***WCET calculation source***/\n");
			lso.append("/* WCA WCET objective: " + jc.getClassName() + "."
					+ mi.getMethod().getName() + " */\n");
			lso.append("max: " + lsobj.toString() + ";\n");
			ls.insert(0, lso.toString());

			try {
				lpf = new File(wca.getOutFile()).getParentFile()
						.getAbsolutePath()
						+ File.separator
						+ jc.getClassName()
						+ "."
						+ mi.getMethod().getName() + ".lp";
				lpf = lpf.replace('<', '_');
				lpf = lpf.replace('>', '_');
				//System.out.println("about to write:"+lpf);
				PrintWriter lsout = new PrintWriter(new FileOutputStream(lpf));
				lsout.write(ls.toString());
				//System.out.println("LS to be solved:"+ls.toString());
				lsout.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			try {
				wcetvars = new HashMap();
				LpSolve problem = LpSolve.readLp(lpf, LpSolve.NORMAL, jc
						.getClassName()
						+ "." + mi.getMethod().getName());
				problem.setOutputfile(lpf + ".output.txt");
				problem.setVerbose(LpSolve.CRITICAL);
				lpresult = problem.solve();
				if (lpresult == LpSolve.UNBOUNDED)
					ls.append(" ILP problem is unbounded!\n");
				problem.setOutputfile(lpf + ".solved.txt");
				problem.printObjective();
				problem.printSolution(1);
				wcetlp = (int) (problem.getObjective() + 0.5);
				try {
					BufferedReader in = new BufferedReader(new FileReader(lpf
							+ ".solved.txt"));
					String str;
					while ((str = in.readLine()) != null) {
						ls.append(str + "\n");
						StringTokenizer st = new StringTokenizer(str);
						if (st.countTokens() == 2) {
							String st1 = st.nextToken();
							String st2 = st.nextToken();
							wcetvars.put(st1, st2);
							//System.out.println("putting:"+st1+","+st2);
						}
					}
					in.close();
				} catch (IOException e) {
				}
			} catch (LpSolveException e) {
				System.out.println("LP not solvable for: " + jc.getClassName()
						+ "." + mi.getMethod().getName());
				//e.printStackTrace();
			}
		}

		return ls.toString();
	}

	// tS tB1 etc.
	private String toLSO() {
		StringBuffer lso = new StringBuffer();
		for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
			Integer keyInt = (Integer) iter.next();
			WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
			lso.append(wcbb.toLSObj());

			if (iter.hasNext())
				lso.append(" ");
		}
		return lso.toString();
	}

	public String toLinkBBS() {
		StringBuffer lsb = new StringBuffer();
		int l[] = (int[]) WCETBasicBlock.bbl.get(WCETBasicBlock.bcetid);
		lsb.append("BBs bcet link:");
		for (int i = 0; i < l.length; i++) {
			if (l[i] != -1) {
				lsb.append(l[i] + "->");
			} else
				break;
		}
		lsb.append("T\n");
		lsb.append("BBs bcet:" + WCETBasicBlock.bbe[WCETBasicBlock.bcetid]
				+ "\n");

		l = (int[]) WCETBasicBlock.bbl.get(WCETBasicBlock.wcetid);
		lsb.append("BBs wcet link:");
		for (int i = 0; i < l.length; i++) {
			if (l[i] != -1) {
				lsb.append(l[i] + "->");
			} else
				break;
		}
		lsb.append("T\n");
		lsb.append("BBs wcet:" + WCETBasicBlock.bbe[WCETBasicBlock.wcetid]
				+ "\n");
		//lsb.append("BBs bcet:"+WCETBasicBlock.bbe[WCETBasicBlock.bcetid]+"\n");
		return lsb.toString();
	}

	public TreeMap getBbs() {
		return bbs;
	}

	public WCETBasicBlock[] getBBSArray() {
		WCETBasicBlock[] awcbb = new WCETBasicBlock[bbs.size()];
		int i = 0;
		for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
			WCETBasicBlock wbb = (WCETBasicBlock) getBbs().get(
					(Integer) iter.next());
			awcbb[i] = wbb;
			i++;
		}
		return awcbb;
	}

	public WCETBasicBlock getBbs(int bid) {
		WCETBasicBlock wbb = null;
		for (Iterator iter = getBbs().keySet().iterator(); iter.hasNext();) {
			wbb = (WCETBasicBlock) getBbs().get((Integer) iter.next());
			if (wbb.bid == bid) {
				break;
			} else
				wbb = null;
		}
		return wbb;
	}

	public int getN() {
		return n;
	}

	public ConstantPoolGen getCpg() {
		return mi.getConstantPoolGen();
	}

	// valid after call to toLS
	public StringBuffer getLSO() {
		return lsobj;
	}

}