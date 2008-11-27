/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen
  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)

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

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Visitor;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.structurals.*;

import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.analyses.ReceiverTypes;
import com.jopdesign.dfa.framework.*;
import com.jopdesign.build.AppVisitor;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.tools.JopInstr;

/**
 * The class is for WCET analysis. The class hierarchy is such that WCETAnalyzer
 * creates one WCETMethodBlock for each method. The WCETMethodBlock assists
 * WCETAnalyzer in creating the WCETBasicBlock objects for each basic block.
 * Then WCETBasicBlock can be used together with WCETInstruction to calculate
 * the WCET/BCET value for that particular basic block.
 * 
 * Options in the Makefile for the wcet target: You can set "latex" to true, and
 * WCA will generate "&" characters between columns and "\\" as row terminator.
 * In Latex do this post-processing: replace ">" with "$>$ and "_" with "\_". A
 * directed graph of the basic blocks can be generated in dot format by setting
 * the "dot" property to true.
 * 
 * It can generate LPSolve compliant code which can be used to calculate WCET of
 * each method. Enable the "ls" switch in the Makefile.
 * 
 * @author rup, Martin
 * @see Section 7.4 and Appendix D in MS thesis
 * @see http://www.graphviz.org
 * @see http://lpsolve.sourceforge.net/5.5/
 */

// History:
// 2006-04-01 rup: Initial version aimed at directed graph of basic blocks
// 2006-04-07 rup: Moved to become a non-Jopizer dependent piece of code
// 2006-04-20 rup: Show both cachehit and cachemiss entries
// 2006-04-27 rup: Show latex tables and load/store info for locals
// 2006-05-04 ms:  Split cache miss column
// 2006-05-07 rup: Output dot graphs
// 2006-05-25 rup: "Annotations" and lp_solvable wcet output
// 2006-05-30 rup: Exact call graph permutation to allow cache simulation
// 2008-11-01 ms:  Use AppInfo
// TODO: abstract method resolution (try all implementations and select worst).
/**
 * The thing that controls the WCETClassBlock etc.
 */
public class WCETAnalyser extends com.jopdesign.dfa.framework.AppInfo {

	public HashMap filePathcodeLines = new HashMap();

	public ArrayList cfgwcmbs = new ArrayList();

	WCETMethodBlock wcmbapp = null;
	boolean global = true; // controls names of blocks B1 or B1_M1 if true

	String dotf = null;
	// dot property: it will generate dot graphs if true
	public boolean jline;
	public boolean instr; // true if you want instriction cycles
	// printed
	
	public boolean useDfa; // use DFA for the loop bounds, default is yes

	// The app method or main if not provided
	public String appmethod;

	public int idtmp = 0; // counter to make unique ids

	public final static String nativeClass = "com.jopdesign.sys.Native";

	PrintWriter out;
	PrintWriter dotout;

	public StringBuffer wcasb = new StringBuffer();

	// signaure -> methodbcel
	HashMap mmap;

	// MethodInfo -> WCETMethodBlock
	HashMap mtowcmb = new HashMap();

	// method name to id
	HashMap midmap;

	// id to wcmb
	HashMap idmmap;

	// methodsignature -> wcmb
	HashMap msigtowcmb;

	HashMap javaFilePathMap;

	ArrayList javaFiles;

	public ArrayList wcmbs; // all the wcmbs

	public boolean init = true;
	
	LoopBounds lb;

	public WCETAnalyser() {
		super(ClassInfo.getTemplate());

		wcmbs = new ArrayList();
		msigtowcmb = new HashMap();
		classpath = new org.apache.bcel.util.ClassPath(".");
		mmap = new HashMap();
		midmap = new HashMap();
		javaFiles = new ArrayList();
		javaFilePathMap = new HashMap();
	}
	
	public void prepare() {
		WCETAnalyser wca = this;
		
		// the tables can be easier to use in latex using this property
		wca.jline = System.getProperty("jline", "false").equals("true");
		wca.instr = System.getProperty("instr", "true").equals("true");
		wca.useDfa = System.getProperty("dfa", "true").equals("true");

		wca.appmethod = wca.mainClass + "." + wca.mainMethodName;
		StringTokenizer st = new StringTokenizer(wca.srcPath,
				File.pathSeparator);
		while (st.hasMoreTokens()) {
			String srcDir = st.nextToken();// "java/target/src/common";
			File sDir = new File(srcDir);
			if (sDir.isDirectory()) {
				// System.out.println("srcDir="+srcDir);
				wca.visitAllFiles(sDir);
			}
		}
		// Iterator ito = wca.javaFilePathMap.values().iterator();
		// while(ito.hasNext()){
		// System.out.println(ito.next());
		// }

		// System.out.println("CLASSPATH=" + wca.classpath + "\tmain
		// class="
		// + mainClass);

		try {
			wca.out = new PrintWriter(new FileOutputStream(wca.outFile));
			String ds = new File(wca.outFile).getParentFile().getAbsolutePath()
				+ File.separator + "Makefile";
			wca.dotout = new PrintWriter(new FileOutputStream(ds));
			wca.dotout.print("doteps:\n");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void analyze() {
		WCETAnalyser wca = this;		

		try {

			wca.iterate(new SrcMethodVisitor(wca));

			wca.global = false;
			wca.iterate(new SetWCETAnalysis(wca));
			wca.init = false;
			// wca.iterate(new SetWCETAnalysis(wca));

			// wca.out.println("*************APPLICATION
			// WCET="+wca.wcmbapp.wcet+"********************");
			StringBuffer wcasbtemp = new StringBuffer();
			// was dependent on a flag just set before
			if (true) {
				wca.global = true;
				wca.wcmbapp.check();
				wcasbtemp.append(wca.wcmbapp.toLS(true, true, null));
				// wca.out.println(wca.wcmbapp.toLS(true,true, null));
				wcasbtemp.append(wca.toDot());
				System.out.println("Application WCET=" + wca.wcmbapp.wcetlp);
				if (wca.wcmbapp.wcetlp >= 0)
					wcasbtemp.insert(0, "*************APPLICATION WCET="
							+ wca.wcmbapp.wcetlp + "********************\n");
				else
					wcasbtemp
							.insert(
									0,
									"*************APPLICATION WCET=UNBOUNDED (CHECK LOOP BOUNDS I.E.: @WCA loop=XYZ)********************\n");
				// for (int i=0;i<wca.cfgwcmbs.size();i++){
				// WCETMethodBlock wcmb =
				// (WCETMethodBlock)wca.cfgwcmbs.get(i);
				// wca.wcasb.append(wcmb.codeString.toString());
				// wca.dotout.print("\tdot -Tps "+wcmb.dotf+" >
				// "+wcmb.dotf.substring(0,wcmb.dotf.length()-4)+".eps\n");
				// }
				wca.out.println(wcasbtemp.toString());
				wca.dotout.print("\tdot -Tps " + wca.dotf + " > "
						+ wca.dotf.substring(0, wca.dotf.length() - 4)
						+ ".eps\n");
			}
			wca.out
					.println("*************END APPLICATION WCET*******************");
			wca.out.println(wca.wcasb.toString());

			// instruction info
			wca.out
					.println("*****************************************************");
			if (wca.instr)
				wca.out.println(WCETInstruction.toWCAString());
			wca.out.println("Note: Remember to keep WCETAnalyzer updated");
			wca.out.println("each time a bytecode implementation is changed.");
			wca.out.close();
			wca.dotout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public static void main(String[] args) {
				
		long time;
		
		WCETAnalyser wca = new WCETAnalyser();
		if (args.length == 0) {
			System.err
					.println("WCETAnalyser arguments: [-cp classpath] [-o file] class [class]*");
			System.exit(1);
		}
		wca.parseOptions(args);
		wca.prepare();
//		wca.excludeClass(nativeClass);
		
		try {
			wca.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		time = System.currentTimeMillis();
		if (wca.useDfa) {
			// get receivers for this program
			ReceiverTypes rt = new ReceiverTypes();
			wca.setReceivers(wca.runAnalysis(rt));
//			rt.printResult(wca);
			// run loop bounds analysis
			wca.lb = new LoopBounds();
			wca.runAnalysis(wca.lb);
//			wca.lb.printResult(wca);
			time = System.currentTimeMillis()-time;
			System.out.println("DFA finished after "+time+" ms");
		}
		
		time = System.currentTimeMillis();
		wca.analyze();
		time = System.currentTimeMillis()-time;
		System.out.println("WCA finished after "+time+" ms");
	}

	// Java Dev. Almanac
	public void visitAllFiles(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllFiles(new File(dir, children[i]));
			}
		} else {
			String filePath = dir.getAbsolutePath();
			String fileName = dir.getName();
			if (fileName.endsWith(".java")) {
				// System.out.println(fileName);
				// System.out.println(filePath);
				// String prevPath = (String)javaFilePathMap.get(fileName);
				// if(prevPath != null && !prevPath.equals(filePath)){
				// System.out.println(fileName +" is referring to "+prevPath+"
				// and to "+filePath+". Exiting.");
				// System.exit(1);
				// }
				// else{
				// javaFilePathMap.put(fileName,filePath);
				// }
				javaFiles.add(filePath);
			}
		}
	}

	public WCETMethodBlock getWCMB(MethodInfo mi) {
		WCETMethodBlock wcmb = (WCETMethodBlock) mtowcmb.get(mi);
		// System.out.println("getWCMB: "+mi.methodId+" "+wcmb.mid);
		return wcmb;
	}

	/**
	 * Return the opcode for the methodId (applicable to Native methods).
	 * 
	 * @param methodid
	 * @return opcode which can be used to call WCETIstruction.getcycles
	 */
	public int getNativeOpcode(String methodid) {
		int opcode = JopInstr.getNative(methodid);
		if (opcode == -1) {
			System.out.println("Did not find native");
			System.exit(-1);
		}
		return opcode;
	}
	
	public String getOutFile() {
		return outFile;
	}
	
	/**
	 * Needed by WCETMethodBlock, should use ClassInfo instead.
	 * @return
	 */
	JavaClass[] toClassArray() {
		Object cli[] = cliMap.values().toArray();
		JavaClass jca[] = new JavaClass[cli.length];
		for (int i = 0; i < jca.length; i++) {
			jca[i] = ((ClassInfo) cli[i]).clazz;
		}
		return jca;
	}

	public String toDot() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n/* App Dot Graph */\n");
		sb.append("digraph G {\n"); // 8.27 x 11.69 inches
		sb.append("size = \"7.27,9.69\"\n");
		// boolean mem = global;
		// global = false;
		ArrayList appWCMB = new ArrayList();
		appWCMB.add(wcmbapp);
		while (appWCMB.size() > 0) {
			WCETMethodBlock wcmb = (WCETMethodBlock) appWCMB.get(0);
			wcmb.link();
			sb.append("subgraph cluster" + wcmb.mid + " {\n");
			sb.append("color = black;\n");
			sb.append(wcmb.toDot(true) + "\n");
			sb.append("label = \"" + wcmb.cname + "." + wcmb.name + " : M"
					+ wcmb.mid + "\";\n");

			sb.append("}\n");
			WCETBasicBlock[] wcbba = wcmb.getBBSArray();
			for (int j = 0; j < wcbba.length; j++) {
				if (wcbba[j].invowcmb != null) {
					sb.append(wcbba[j].toDotFlowEdge(wcbba[j].invowcmb.S));
					sb.append(" [label=\""
							+ wcbba[j].toDotFlowLabel(wcbba[j].invowcmb.S)
							+ "\"];\n");
					sb.append(wcbba[j].invowcmb.T.toDotFlowEdge(wcbba[j]));
					sb.append(" [label=\""
							+ wcbba[j].invowcmb.T.toDotFlowLabel(wcbba[j])
							+ "\"];\n");
					appWCMB.add(wcbba[j].invowcmb);
				}
			}
			appWCMB.remove(0);
		}
		sb.append("}\n");

		try {
			dotf = new File(outFile).getParentFile()
					.getAbsolutePath()
					+ File.separator + "App.dot";
			dotf = dotf.replace('<', '_');
			dotf = dotf.replace('>', '_');
			dotf = dotf.replace('\\', '/');
			PrintWriter dotout = new PrintWriter(new FileOutputStream(dotf));
			dotout.write(sb.toString());
			dotout.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		// global = mem;
		return "";
	}
}

// BCEL overrides

/**
 * Extends org.apache.bcel.verifier.structurals. Frame just to get access to the
 * _this field, which the the operandWalker method in MethodInfo uses.
 */
class FrameFrame extends Frame {

	public FrameFrame(int maxLocals, int maxStack) {
		super(maxLocals, maxStack);
	}

	public FrameFrame(LocalVariables locals, OperandStack stack) {
		super(locals, stack);
	}

	void setThis(UninitializedObjectType uot) {
		_this = uot;
	}

	UninitializedObjectType getThis() {
		return _this;
	}
}

/**
 * Get additional info for WCA.
 * @author Martin Schoeberl
 *
 */
class SrcMethodVisitor extends AppVisitor {

	/**
	 * A counter for a unique method id;
	 */
    int mid = 0;

	public SrcMethodVisitor(AppInfo ai) {
		super(ai);
	}
	
	@Override
	public void visitJavaClass(JavaClass clazz) {
		super.visitJavaClass(clazz);
		
		WCETAnalyser wca = (WCETAnalyser) ai;
		// package name and associated sourcefile
		String pacSrc = clazz.getPackageName() + "."
				+ clazz.getSourceFileName();
		boolean fileMatch = false;
		for (int k = 0; k < wca.javaFiles.size(); k++) {
			String orig = (String) wca.javaFiles.get(k);
			String pn = orig;
			pn = pn.replace('/', '.');
			pn = pn.replace('\\', '.');
			// System.out.println("Trying to match:"+pn+ " with: "+pacSrc);
			int match = pn.lastIndexOf(pacSrc);
			if (match != -1) {
				String key = clazz.getClassName();
				// System.out.println("Match! Key :"+key);
				wca.javaFilePathMap.put(key, orig);
				fileMatch = true;
				break;
			}
		}
		if (!fileMatch) {
			System.out.println("No filematch for " + clazz.getClassName()
					+ " and pacSrc=" + pacSrc);
			System.exit(-1);
		}
		Method[] m = clazz.getMethods();
		for (int ii = 0; ii < m.length; ii++) {
			String msig = clazz.getClassName() + "." + m[ii].getName()
					+ m[ii].getSignature();
			// System.out.println("m to be put:"+msig);//TODO mig everywhere
			// System.out.println("r:
			// "+m[ii].getReturnType().getSignature());//TODO mig everywhere
			wca.mmap.put(msig, m[ii]);
			wca.midmap.put(new Integer(mid), msig);
			mid++;
		}
	}
}

/**
 * BCEL throws an exception for the util.Dbg class because it overloads a field.
 * The choice (as described in the BCEL method comment for around line 2551 in
 * org.apache.bcel.verifier.structurals.InstConstraintVisitor) is to comment out
 * this check and recompile BCEL jar. Insted of recompiling BCEL the choice is
 * to override the methods, which is ok? as we are not using BCEL for bytecode
 * verification purposes (using Sun Javac).
 */
// TODO: Can this extension be avoided (ie. why did BCEL not like the
// overloaded Dbg field).
class AnInstConstraintVisitor extends InstConstraintVisitor {
	public void visitAALOAD(AALOAD o) {
	}

	public void visitAASTORE(AASTORE o) {
	}

	public void visitACONST_NULL(ACONST_NULL o) {
	}

	public void visitALOAD(ALOAD o) {
	}

	public void visitANEWARRAY(ANEWARRAY o) {
	}

	public void visitARETURN(ARETURN o) {
	}

	public void visitARRAYLENGTH(ARRAYLENGTH o) {
	}

	public void visitASTORE(ASTORE o) {
	}

	public void visitATHROW(ATHROW o) {
	}

	public void visitBALOAD(BALOAD o) {
	}

	public void visitBASTORE(BASTORE o) {
	}

	public void visitBIPUSH(BIPUSH o) {
	}

	public void visitBREAKPOINT(BREAKPOINT o) {
	}

	public void visitCALOAD(CALOAD o) {
	}

	public void visitCASTORE(CASTORE o) {
	}

	public void visitCHECKCAST(CHECKCAST o) {
	}

	public void visitCPInstruction(CPInstruction o) {
	}

	public void visitD2F(D2F o) {
	}

	public void visitD2I(D2I o) {
	}

	public void visitD2L(D2L o) {
	}

	public void visitDADD(DADD o) {
	}

	public void visitDALOAD(DALOAD o) {
	}

	public void visitDASTORE(DASTORE o) {
	}

	public void visitDCMPG(DCMPG o) {
	}

	public void visitDCMPL(DCMPL o) {
	}

	public void visitDCONST(DCONST o) {
	}

	public void visitDDIV(DDIV o) {
	}

	public void visitDLOAD(DLOAD o) {
	}

	public void visitDMUL(DMUL o) {
	}

	public void visitDNEG(DNEG o) {
	}

	public void visitDREM(DREM o) {
	}

	public void visitDRETURN(DRETURN o) {
	}

	public void visitDSTORE(DSTORE o) {
	}

	public void visitDSUB(DSUB o) {
	}

	public void visitDUP_X1(DUP_X1 o) {
	}

	public void visitDUP_X2(DUP_X2 o) {
	}

	public void visitDUP(DUP o) {
	}

	public void visitDUP2_X1(DUP2_X1 o) {
	}

	public void visitDUP2_X2(DUP2_X2 o) {
	}

	public void visitDUP2(DUP2 o) {
	}

	public void visitF2D(F2D o) {
	}

	public void visitF2I(F2I o) {
	}

	public void visitF2L(F2L o) {
	}

	public void visitFADD(FADD o) {
	}

	public void visitFALOAD(FALOAD o) {
	}

	public void visitFASTORE(FASTORE o) {
	}

	public void visitFCMPG(FCMPG o) {
	}

	public void visitFCMPL(FCMPL o) {
	}

	public void visitFCONST(FCONST o) {
	}

	public void visitFDIV(FDIV o) {
	}

	public void visitFieldInstruction(FieldInstruction o) {
	}

	public void visitFLOAD(FLOAD o) {
	}

	public void visitFMUL(FMUL o) {
	}

	public void visitFNEG(FNEG o) {
	}

	public void visitFREM(FREM o) {
	}

	public void visitFRETURN(FRETURN o) {
	}

	public void visitFSTORE(FSTORE o) {
	}

	public void visitFSUB(FSUB o) {
	}

	public void visitGETFIELD(GETFIELD o) {
	}

	public void visitGETSTATIC(GETSTATIC o) {
	}

	public void visitGOTO_W(GOTO_W o) {
	}

	public void visitGOTO(GOTO o) {
	}

	public void visitI2B(I2B o) {
	}

	public void visitI2C(I2C o) {
	}

	public void visitI2D(I2D o) {
	}

	public void visitI2F(I2F o) {
	}

	public void visitI2L(I2L o) {
	}

	public void visitI2S(I2S o) {
	}

	public void visitIADD(IADD o) {
	}

	public void visitIALOAD(IALOAD o) {
	}

	public void visitIAND(IAND o) {
	}

	public void visitIASTORE(IASTORE o) {
	}

	public void visitICONST(ICONST o) {
	}

	public void visitIDIV(IDIV o) {
	}

	public void visitIF_ACMPEQ(IF_ACMPEQ o) {
	}

	public void visitIF_ACMPNE(IF_ACMPNE o) {
	}

	public void visitIF_ICMPEQ(IF_ICMPEQ o) {
	}

	public void visitIF_ICMPGE(IF_ICMPGE o) {
	}

	public void visitIF_ICMPGT(IF_ICMPGT o) {
	}

	public void visitIF_ICMPLE(IF_ICMPLE o) {
	}

	public void visitIF_ICMPLT(IF_ICMPLT o) {
	}

	public void visitIF_ICMPNE(IF_ICMPNE o) {
	}

	public void visitIFEQ(IFEQ o) {
	}

	public void visitIFGE(IFGE o) {
	}

	public void visitIFGT(IFGT o) {
	}

	public void visitIFLE(IFLE o) {
	}

	public void visitIFLT(IFLT o) {
	}

	public void visitIFNE(IFNE o) {
	}

	public void visitIFNONNULL(IFNONNULL o) {
	}

	public void visitIFNULL(IFNULL o) {
	}

	public void visitIINC(IINC o) {
	}

	public void visitILOAD(ILOAD o) {
	}

	public void visitIMPDEP1(IMPDEP1 o) {
	}

	public void visitIMPDEP2(IMPDEP2 o) {
	}

	public void visitIMUL(IMUL o) {
	}

	public void visitINEG(INEG o) {
	}

	public void visitINSTANCEOF(INSTANCEOF o) {
	}

	public void visitInvokeInstruction(InvokeInstruction o) {
	}

	public void visitINVOKEINTERFACE(INVOKEINTERFACE o) {
	}

	public void visitINVOKESPECIAL(INVOKESPECIAL o) {
	}

	public void visitINVOKESTATIC(INVOKESTATIC o) {
	}

	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL o) {
	}

	public void visitIOR(IOR o) {
	}

	public void visitIREM(IREM o) {
	}

	public void visitIRETURN(IRETURN o) {
	}

	public void visitISHL(ISHL o) {
	}

	public void visitISHR(ISHR o) {
	}

	public void visitISTORE(ISTORE o) {
	}

	public void visitISUB(ISUB o) {
	}

	public void visitIUSHR(IUSHR o) {
	}

	public void visitIXOR(IXOR o) {
	}

	public void visitJSR_W(JSR_W o) {
	}

	public void visitJSR(JSR o) {
	}

	public void visitL2D(L2D o) {
	}

	public void visitL2F(L2F o) {
	}

	public void visitL2I(L2I o) {
	}

	public void visitLADD(LADD o) {
	}

	public void visitLALOAD(LALOAD o) {
	}

	public void visitLAND(LAND o) {
	}

	public void visitLASTORE(LASTORE o) {
	}

	public void visitLCMP(LCMP o) {
	}

	public void visitLCONST(LCONST o) {
	}

	public void visitLDC_W(LDC_W o) {
	}

	public void visitLDC(LDC o) {
	}

	public void visitLDC2_W(LDC2_W o) {
	}

	public void visitLDIV(LDIV o) {
	}

	public void visitLLOAD(LLOAD o) {
	}

	public void visitLMUL(LMUL o) {
	}

	public void visitLNEG(LNEG o) {
	}

	public void visitLoadClass(LoadClass o) {
	}

	public void visitLoadInstruction(LoadInstruction o) {
	}

	public void visitLocalVariableInstruction(LocalVariableInstruction o) {
	}

	public void visitLOOKUPSWITCH(LOOKUPSWITCH o) {
	}

	public void visitLOR(LOR o) {
	}

	public void visitLREM(LREM o) {
	}

	public void visitLRETURN(LRETURN o) {
	}

	public void visitLSHL(LSHL o) {
	}

	public void visitLSHR(LSHR o) {
	}

	public void visitLSTORE(LSTORE o) {
	}

	public void visitLSUB(LSUB o) {
	}

	public void visitLUSHR(LUSHR o) {
	}

	public void visitLXOR(LXOR o) {
	}

	public void visitMONITORENTER(MONITORENTER o) {
	}

	public void visitMONITOREXIT(MONITOREXIT o) {
	}

	public void visitMULTIANEWARRAY(MULTIANEWARRAY o) {
	}

	public void visitNEW(NEW o) {
	}

	public void visitNEWARRAY(NEWARRAY o) {
	}

	public void visitNOP(NOP o) {
	}

	public void visitPOP(POP o) {
	}

	public void visitPOP2(POP2 o) {
	}

	public void visitPUTFIELD(PUTFIELD o) {
	}

	public void visitPUTSTATIC(PUTSTATIC o) {
	}

	public void visitRET(RET o) {
	}

	public void visitRETURN(RETURN o) {
	}

	public void visitReturnInstruction(ReturnInstruction o) {
	}

	public void visitSALOAD(SALOAD o) {
	}

	public void visitSASTORE(SASTORE o) {
	}

	public void visitSIPUSH(SIPUSH o) {
	}

	public void visitStackConsumer(StackConsumer o) {
	}

	public void visitStackInstruction(StackInstruction o) {
	}

	public void visitStackProducer(StackProducer o) {
	}

	public void visitStoreInstruction(StoreInstruction o) {
	}

	public void visitSWAP(SWAP o) {
	}

	public void visitTABLESWITCH(TABLESWITCH o) {
	}
}

// We may need this later in controlFlowMethod to simulate the basic blocks
// // TODO: Do we even need the controlflowgraph for this?
// cfg = new ControlFlowGraph(mg);
// // InstructionContext as key for inFrame
// HashMap inFrames = new HashMap();
// HashMap outFrames = new HashMap();
//
// // Build the initial frame situation for this method.
// FrameFrame fStart = new FrameFrame(mg.getMaxLocals(), mg.getMaxStack());
// if (!mg.isStatic()) {
// if (mg.getName().equals(Constants.CONSTRUCTOR_NAME)) {
// fStart.setThis(new UninitializedObjectType(new ObjectType(jc
// .getClassName())));
// fStart.getLocals().set(0, fStart.getThis());
// } else {
// fStart.setThis(null);
// fStart.getLocals().set(0, new ObjectType(jc.getClassName()));
// }
// }
// Type[] argtypes = mg.getArgumentTypes();
// int twoslotoffset = 0;
// for (int j = 0; j < argtypes.length; j++) {
// if (argtypes[j] == Type.SHORT || argtypes[j] == Type.BYTE
// || argtypes[j] == Type.CHAR || argtypes[j] == Type.BOOLEAN) {
// argtypes[j] = Type.INT;
// }
// fStart.getLocals().set(twoslotoffset + j + (mg.isStatic() ? 0 : 1),
// argtypes[j]);
// if (argtypes[j].getSize() == 2) {
// twoslotoffset++;
// fStart.getLocals().set(twoslotoffset + j + (mg.isStatic() ? 0 : 1),
// Type.UNKNOWN);
// }
// }
//
// // if (method.getName().equalsIgnoreCase("sort")) {
// // System.out.println(method.getCode().toString());
// //
// // }
//
// InstructionContext start = cfg.contextOf(mg.getInstructionList()
// .getStart());
// // don't need to compare for first frame
// inFrames.put(start, fStart);
//
// boolean fbool = start.execute(fStart, new ArrayList(), icv, ev);
// Frame fout = start.getOutFrame(new ArrayList());
// outFrames.put(start, fout);
// start.setTag(start.getTag() + 1);
// // int posnow = start.getInstruction().getPosition();
//
// Vector ics = new Vector(); // Type: InstructionContext
// Vector ecs = new Vector(); // Type: ArrayList (of
// // InstructionContext)
//
// ics.add(start);
// ecs.add(new ArrayList());
// int loopcnt = 1;
// // LOOP!
// while (!ics.isEmpty()) {
// loopcnt++;
// InstructionContext u;
// ArrayList ec;
// u = (InstructionContext) ics.get(0);
// // TODO: Would it be better to call wcet here instead of in the TAG
// // count
// // loop?
// // System.out.println(u.toString());
// ec = (ArrayList) ecs.get(0);
// ics.remove(0);
// ecs.remove(0);
// ArrayList oldchain = (ArrayList) (ec.clone());
// ArrayList newchain = (ArrayList) (ec.clone());
// newchain.add(u);
//
// if ((u.getInstruction().getInstruction()) instanceof RET) {
// // We can only follow _one_ successor, the one after the
// // JSR that was recently executed.
// RET ret = (RET) (u.getInstruction().getInstruction());
// ReturnaddressType t = (ReturnaddressType) u.getOutFrame(oldchain)
// .getLocals().get(ret.getIndex());
// InstructionContext theSuccessor = cfg.contextOf(t.getTarget());
//
// // Sanity check
// InstructionContext lastJSR = null;
// int skip_jsr = 0;
// for (int ss = oldchain.size() - 1; ss >= 0; ss--) {
// if (skip_jsr < 0) {
// throw new AssertionViolatedException(
// "More RET than JSR in execution chain?!");
// }
// if (((InstructionContext) oldchain.get(ss)).getInstruction()
// .getInstruction() instanceof JsrInstruction) {
// if (skip_jsr == 0) {
// lastJSR = (InstructionContext) oldchain.get(ss);
// break;
// } else {
// skip_jsr--;
// }
// }
// if (((InstructionContext) oldchain.get(ss)).getInstruction()
// .getInstruction() instanceof RET) {
// skip_jsr++;
// }
// }
// if (lastJSR == null) {
// throw new AssertionViolatedException(
// "RET without a JSR before in ExecutionChain?! EC: '" + oldchain
// + "'.");
// }
// JsrInstruction jsr = (JsrInstruction) (lastJSR.getInstruction()
// .getInstruction());
// if (theSuccessor != (cfg.contextOf(jsr.physicalSuccessor()))) {
// throw new AssertionViolatedException("RET '" + u.getInstruction()
// + "' info inconsistent: jump back to '" + theSuccessor
// + "' or '" + cfg.contextOf(jsr.physicalSuccessor()) + "'?");
// }
//
// if (theSuccessor.execute(u.getOutFrame(oldchain), newchain, icv, ev)) {
// ics.add(theSuccessor);
// ecs.add((ArrayList) newchain.clone());
// }
// // inFrames.put(theSuccessor,u.getOutFrame(oldchain));
// theSuccessor.setTag(theSuccessor.getTag() + 1);
// // osa[theSuccessor.getInstruction().getPosition()].add(fStart
// // .getStack().getClone());
// // lva[theSuccessor.getInstruction().getPosition()].add(fStart
// // .getLocals().getClone());
// Frame prevf = (Frame) inFrames.put(theSuccessor, u
// .getOutFrame(oldchain));
// Frame newf = theSuccessor.getOutFrame(newchain);
// Frame prevof = (Frame) outFrames.put(theSuccessor, newf);
//
// } else {// "not a ret"
// // Normal successors. Add them to the queue of successors.
// // TODO: Does u get executed?
// InstructionContext[] succs = u.getSuccessors();
//
// // System.out.println("suss#:" + succs.length);
// for (int s = 0; s < succs.length; s++) {
// InstructionContext v = succs[s];
// // System.out.println(v.toString());
// if (v.execute(u.getOutFrame(oldchain), newchain, icv, ev)) {
// ics.add(v);
// ecs.add((ArrayList) newchain.clone());
// }
// v.setTag(v.getTag() + 1);
// Frame prevf = (Frame) inFrames.put(v, u.getOutFrame(oldchain));
// Frame newf = v.getOutFrame(newchain);
// Frame prevof = (Frame) outFrames.put(v, newf);
// }
// }// end "not a ret"
//
// // Exception Handlers. Add them to the queue of successors.
// // [subroutines are never protected; mandated by JustIce]
// ExceptionHandler[] exc_hds = u.getExceptionHandlers();
// for (int s = 0; s < exc_hds.length; s++) {
// InstructionContext v = cfg.contextOf(exc_hds[s].getHandlerStart());
// Frame f = new Frame(u.getOutFrame(oldchain).getLocals(),
// new OperandStack(u.getOutFrame(oldchain).getStack().maxStack(),
// (exc_hds[s].getExceptionType() == null ? Type.THROWABLE
// : exc_hds[s].getExceptionType())));
//
// if (v.execute(f, new ArrayList(), icv, ev)) {
// ics.add(v);
// ecs.add(new ArrayList());
// }
// v.setTag(v.getTag() + 1);
// Frame prevf = (Frame) inFrames.put(v, f);
// Frame newf = v.getOutFrame(new ArrayList());
// Frame prevof = (Frame) outFrames.put(v, newf);
//
// }
// }// while (!ics.isEmpty()) END
//
// // Check that all instruction have been simulated
// do {
// InstructionContext ic = cfg.contextOf(ih);
// if (ic.getTag() == 0) {
// System.err
// .println("Instruction " + ic.toString() + " not simulated.");
// System.exit(-1);
// }
// // TODO: Can it handle direct loops back
// if (ih.getInstruction() instanceof BranchInstruction) {
// int target = ((BranchInstruction) ih.getInstruction()).getTarget()
// .getPosition();
// InstructionHandle btarget = ((BranchInstruction) ih.getInstruction())
// .getTarget();
// // check and possibly create a new bb starting at the target
// createBasicBlock(btarget);
// InstructionHandle ihnext = ih.getNext();
// // check if the next instruction was the target and if not see if a
// // new bb is to be created
// // TODO: Could it be true?
// if (!ihnext.equals(btarget)) {
// createBasicBlock(ihnext);
// }
//
// }
//
// } while ((ih = ih.getNext()) != null);

// Some utilitilities
