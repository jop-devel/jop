/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen

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

package com.jopdesign.build;

import java.util.*;
import java.io.PrintWriter;

import org.apache.bcel.classfile.*;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.exc.*;
import org.apache.bcel.verifier.structurals.*;

/**
 * It is a class that are invoked two times for every non-abstract method. The
 * <code>stackWalker</code> method simulates the instructions of a given
 * method. Then this simulated context is used to extract information about the
 * incoming (ie. how the locals and the operands) frame looked for this
 * particular value of the program counter (PC). Then the information must reach
 * JOP. It is done by saving the garbage collection (GC) information below the
 * code address. We save one word that is packed with information about max.
 * number of local, max. number of operands etc. Then, if the method has ANY
 * reference for ANY value of the PC, the GC information is saved below this
 * info word. That is the case in the majority of the cases. As we need the type
 * (primitive or reference) for every local and every operand for every value of
 * the PC it can take up a lot of space. Initial experiments demonstrated an
 * overhead of 133% but now the default is an indexed approach. In this way the
 * X local bits and the Y operand bits are appended to form a pattern. These
 * patterns form a small number of unique patterns. Each PC is then mapped
 * (using just enough bits) to point to the corresponding unique pattern. The
 * unique patterns are packed at the end of bit map. When reading the file, we
 * refer to the "raw" and "indexed" approach. The raw aproach was saving the
 * aforementioned patterns for every PC. On JOP, a class called GCStkWalk is
 * responsible for identifying the references that may be among the locals and
 * the operands for the threads. A method named <code>swk</code> is
 * responsible for this scan. It walks the frames from the top and uses the
 * packes GC bit maps to determine which (if any) of the locals and operands
 * that hold references. It then returns a reference to an integer array that is
 * used in <code>GC.java</code> to push only the references onto into the
 * scanned list. Little note: The bits are written out from left to right in the
 * comments. The sum of the locals+operands cannot exceed 32. TODO: Test a
 * Gosling violation. TODO: Implement bytecode rearrangement when Gosling
 * violation detected.
 * 
 * @author rup, ms
 */
public class GCRTMethodInfo {
	static int WORDLEN = 32;

	static int totalpcwords = 0;

	static int totalcntMgciWords = 0;

	static HashMap miMap = new HashMap();
  
  //  those methods with gc info 
  //  (provided by a call to CallGraph?) 
  static HashSet gcMethods = null;
  
  /**
   * Contains the set of methods which are included in the stack map generation.
   * @param gcMethods
   */
  public static void setGcMethods(HashSet gcMethods) {
    GCRTMethodInfo.gcMethods = gcMethods;
  }
  
	/**
	 * Called from JOPizer->SetGCRTMethodInfo to run the stack simulation for
	 * the method.
	 * 
	 * @param mi the method
	 */
	public static void stackWalker(OldMethodInfo mi) {
		((GCRTMethodInfo) miMap.get(mi)).stackWalker();
	}

	/**
	 * It runs the dump method without dumping.
	 * 
	 * @param methodbcel
	 * @return Returns the length in words that the GC bit maps will take.
	 */
	public static int gcLength(OldMethodInfo mi) {
		return ((GCRTMethodInfo) miMap.get(mi)).gcLength();
	}

	/**
	 * It writes out the gcpack and the bitmaps. Nice comments are dumped as
	 * well in the .jop file.
	 * 
	 * @param out
	 *            null if just the length is needed
	 * @param mi
	 *            the method
	 */
	public static void dumpMethodGcis(OldMethodInfo mi, PrintWriter out) {
		((GCRTMethodInfo) miMap.get(mi)).dumpMethodGcis(out);
	}
  
  public static void removePC(int pc,OldMethodInfo mi){
    ((GCRTMethodInfo) miMap.get(mi)).removePC(pc);
  }

	// instance
	OldMethodInfo mi;

	Method method;

	int length;

	// Operand map for a given PC
	int[] ogci;

	// Local variables map for a given PC
	int[] mgci;

	// Instruction count
	int instCnt;

	// instCnt
	String[] pcinfo;

	int mstack, margs, mreallocals, len;

  // word counters for indexed approach
  int pcwords = 0;
  
  String tostr;
  String signature;
  String name;  
  String cname;
  
	/**
	 * Instanciated from from <code>SetClassInfo</code>.
	 */
	public GCRTMethodInfo(OldMethodInfo mi, Method method) {
		this.mi = mi;
		this.method = method;

		mgci = new int[0];
		ogci = new int[0];
		instCnt = 0;
		length = 0;
		if (miMap.containsKey(mi)) {
			System.err.println("Alredy added mi.");
			System.exit(-1);
		} else {
			miMap.put(mi, this);
		}
	}

	/**
	 * It walks the stack frame at compile time. The locals and the operands are
	 * mapped to to bit maps. These bit maps are used at run time to identify an
	 * exact root set which are used in conjunction with a real-time garbage
	 * collector scheduler.
	 */
	private void stackWalker() {
		// System.out.println(".....................");
		// System.out.println("stackWalker");
		// System.out.println("Class
		// method:"+mi.cli.clazz.getClassName()+"."+mi.methodId);

		margs = mstack = mreallocals = len = instCnt = 0;

		Type at[] = method.getArgumentTypes();
		for (int i = 0; i < at.length; ++i) {
			margs += at[i].getSize();
		}

		if (!method.isStatic()) {
			margs++; //  this
		}

		if (!method.isAbstract()) {
			mstack = method.getCode().getMaxStack();
			mreallocals = method.getCode().getMaxLocals() - margs;
      
      if ((mreallocals+margs+mstack)>31) {
        // we interprete clinit on JOP - no size restriction
        if (!method.getName().equals("<clinit>")) {
          System.err.println("wrong size: "+method.getName()+" cannot have (mreallocals+margs+mstack)>31");
          System.exit(-1);          
        }
      }

			instCnt = (method.getCode().getCode()).length;
      
			operandWalker();
		}
		// System.out.println(" ***
		// "+mi.cli.clazz.getClassName()+"."+mi.methodId+" --> mreallocals
		// ="+mreallocals+" margs ="+margs+" mstack ="+mstack);

	}

	/**
	 * Collect type information on the operands for the stack frame. The code is
	 * and then the execution context of each instructions is used to map the
	 * type info of each entry on the stack. We map a reference entry with a 1
	 * and a non-reference entry with a 0.
	 * 
	 * The simulation part is based of the BCEL Pass3bVerifer.
	 */
	private void operandWalker() {
		// System.out.println("operandWalker");

		JavaClass jc = mi.getCli().clazz;

		ConstantPoolGen cpg = new ConstantPoolGen(jc.getConstantPool());

		// Some methods overridden (see bottom of this file)
		InstConstraintVisitor icv = new AnInstConstraintVisitor();

		icv.setConstantPoolGen(cpg);

		ExecutionVisitor ev = new ExecutionVisitor();
		ev.setConstantPoolGen(cpg);

		MethodGen mg = new MethodGen(method, jc.getClassName(), cpg);
		// To later get the PC positions of the instructions
		mg.getInstructionList().setPositions(true);
		tostr = mg.toString();
		signature = mg.getSignature();
		name = mg.getName();
		cname = mg.getClassName();
    if(method.getName().equalsIgnoreCase("trace")){
      boolean stop =true;
    }
		icv.setMethodGen(mg);

		if (!(mg.isAbstract() || mg.isNative())) { // IF mg HAS CODE (See pass
			// 2)

			ControlFlowGraph cfg = new ControlFlowGraph(mg);

			// InstructionContext as key for inFrame
			HashMap inFrames = new HashMap();
			HashMap outFrames = new HashMap();

//			// ArrayList array (size is length of bytecode)
//			// holds ArrayList objects with clones of OperandStack objects for
//			// each PC
//			ArrayList osa[] = new ArrayList[method.getCode().getCode().length]; // +1
//			// because
//			// incoming
//			// frame
//			// to
//			// method
//			// is
//			// included
//			// ArrayList array (size is length of bytecode)
//			// holds ArrayList objects with clones of LocalVariables objects for
//			// each PC
//			ArrayList lva[] = new ArrayList[method.getCode().getCode().length];
//			for (int i = 0; i < lva.length; i++) {
//				lva[i] = new ArrayList();
//				osa[i] = new ArrayList();
//			}

			// Build the initial frame situation for this method.
			FrameFrame fStart = new FrameFrame(mg.getMaxLocals(), mg
					.getMaxStack());
			if (!mg.isStatic()) {
				if (mg.getName().equals(Constants.CONSTRUCTOR_NAME)) {
					fStart.setThis(new UninitializedObjectType(new ObjectType(
							jc.getClassName())));
					fStart.getLocals().set(0, fStart.getThis());
				} else {
					fStart.setThis(null);
					fStart.getLocals()
							.set(0, new ObjectType(jc.getClassName()));
				}
			}
			Type[] argtypes = mg.getArgumentTypes();
			int twoslotoffset = 0;
			for (int j = 0; j < argtypes.length; j++) {
				if (argtypes[j] == Type.SHORT || argtypes[j] == Type.BYTE
						|| argtypes[j] == Type.CHAR
						|| argtypes[j] == Type.BOOLEAN) {
					argtypes[j] = Type.INT;
				}
				fStart.getLocals().set(
						twoslotoffset + j + (mg.isStatic() ? 0 : 1),
						argtypes[j]);
				if (argtypes[j].getSize() == 2) {
					twoslotoffset++;
					fStart.getLocals().set(
							twoslotoffset + j + (mg.isStatic() ? 0 : 1),
							Type.UNKNOWN);
				}
			}

			InstructionContext start = cfg.contextOf(mg.getInstructionList()
					.getStart());
			// don't need to compare for first frame
			inFrames.put(start, fStart);

			boolean fbool = start.execute(fStart, new ArrayList(), icv, ev);
			Frame fout = start.getOutFrame(new ArrayList());
			outFrames.put(start,fout);
			start.setTag(start.getTag() + 1);
			// int posnow = start.getInstruction().getPosition();

//			osa[start.getInstruction().getPosition()].add(fStart.getStack()
//					.getClone());
//			lva[start.getInstruction().getPosition()].add(fStart.getLocals()
//					.getClone());

			Vector ics = new Vector(); // Type: InstructionContext
			Vector ecs = new Vector(); // Type: ArrayList (of
			// InstructionContext)

			ics.add(start);
			ecs.add(new ArrayList());
			int loopcnt = 1;
			// LOOP!
			while (!ics.isEmpty()) {
				loopcnt++;
				InstructionContext u;
				ArrayList ec;
				u = (InstructionContext) ics.get(0);
				//System.out.println(u.toString());
				ec = (ArrayList) ecs.get(0);
				ics.remove(0);
				ecs.remove(0);
				ArrayList oldchain = (ArrayList) (ec.clone());
				ArrayList newchain = (ArrayList) (ec.clone());
				newchain.add(u);

				if ((u.getInstruction().getInstruction()) instanceof RET) {
					// We can only follow _one_ successor, the one after the
					// JSR that was recently executed.
					RET ret = (RET) (u.getInstruction().getInstruction());
					ReturnaddressType t = (ReturnaddressType) u.getOutFrame(
							oldchain).getLocals().get(ret.getIndex());
					InstructionContext theSuccessor = cfg.contextOf(t
							.getTarget());

					// Sanity check
					InstructionContext lastJSR = null;
					int skip_jsr = 0;
					for (int ss = oldchain.size() - 1; ss >= 0; ss--) {
						if (skip_jsr < 0) {
							throw new AssertionViolatedException(
									"More RET than JSR in execution chain?!");
						}
						if (((InstructionContext) oldchain.get(ss))
								.getInstruction().getInstruction() instanceof JsrInstruction) {
							if (skip_jsr == 0) {
								lastJSR = (InstructionContext) oldchain.get(ss);
								break;
							} else {
								skip_jsr--;
							}
						}
						if (((InstructionContext) oldchain.get(ss))
								.getInstruction().getInstruction() instanceof RET) {
							skip_jsr++;
						}
					}
					if (lastJSR == null) {
						throw new AssertionViolatedException(
								"RET without a JSR before in ExecutionChain?! EC: '"
										+ oldchain + "'.");
					}
					JsrInstruction jsr = (JsrInstruction) (lastJSR
							.getInstruction().getInstruction());
					if (theSuccessor != (cfg.contextOf(jsr.physicalSuccessor()))) {
						throw new AssertionViolatedException("RET '"
								+ u.getInstruction()
								+ "' info inconsistent: jump back to '"
								+ theSuccessor + "' or '"
								+ cfg.contextOf(jsr.physicalSuccessor()) + "'?");
					}

					if (theSuccessor.execute(u.getOutFrame(oldchain), newchain,
							icv, ev)) {
						ics.add(theSuccessor);
						ecs.add((ArrayList) newchain.clone());
					}
					//inFrames.put(theSuccessor,u.getOutFrame(oldchain));
					theSuccessor.setTag(theSuccessor.getTag() + 1);
//					osa[theSuccessor.getInstruction().getPosition()].add(fStart
//							.getStack().getClone());
//					lva[theSuccessor.getInstruction().getPosition()].add(fStart
//							.getLocals().getClone());
					Frame prevf = (Frame)inFrames.put(theSuccessor,u.getOutFrame(oldchain));
					Frame newf = theSuccessor.getOutFrame(newchain);
					Frame prevof = (Frame)outFrames.put(theSuccessor,newf);
					if (prevof != null && !frmComp(prevof,newf,theSuccessor)){
						System.out.println("A: Gosling violation:"
								+ prevf.toString()+newf.toString());
						System.exit(-1);
					}

				} else {// "not a ret"
					// Normal successors. Add them to the queue of successors.
					// TODO: Does u get executed?
					InstructionContext[] succs = u.getSuccessors();
					
//					System.out.println("suss#:" + succs.length);
					for (int s = 0; s < succs.length; s++) {
						InstructionContext v = succs[s];
						//System.out.println(v.toString());
                        if (v.execute(u.getOutFrame(oldchain), newchain, icv,
								ev)) {
							ics.add(v);
							ecs.add((ArrayList) newchain.clone());
						}
						v.setTag(v.getTag() + 1);
//						osa[v.getInstruction().getPosition()].add(fStart
//								.getStack().getClone());
//						lva[v.getInstruction().getPosition()].add(fStart
//								.getLocals().getClone());
						Frame prevf = (Frame)inFrames.put(v,u.getOutFrame(oldchain));
						Frame newf = v.getOutFrame(newchain);
						Frame prevof = (Frame)outFrames.put(v,newf);
						if (prevof != null && !frmComp(prevof,newf,v)){
							System.out.println("B: Gosling violation:"
									+ fStart.toString());
							System.exit(-1);
						}
					}
				}// end "not a ret"

				// Exception Handlers. Add them to the queue of successors.
				// [subroutines are never protected; mandated by JustIce]
				ExceptionHandler[] exc_hds = u.getExceptionHandlers();
				for (int s = 0; s < exc_hds.length; s++) {
					InstructionContext v = cfg.contextOf(exc_hds[s]
							.getHandlerStart());
					Frame f = new Frame(
							u.getOutFrame(oldchain).getLocals(),
							new OperandStack(
									u.getOutFrame(oldchain).getStack()
											.maxStack(),
									(exc_hds[s].getExceptionType() == null ? Type.THROWABLE
											: exc_hds[s].getExceptionType())));

					if (v.execute(f, new ArrayList(), icv, ev)) {
						ics.add(v);
						ecs.add(new ArrayList());
					}
					v.setTag(v.getTag() + 1);
//					osa[v.getInstruction().getPosition()].add(fStart.getStack()
//							.getClone());
//					lva[v.getInstruction().getPosition()].add(fStart
//							.getLocals().getClone());
					Frame prevf = (Frame)inFrames.put(v,f);
					Frame newf = v.getOutFrame(new ArrayList());
					Frame prevof = (Frame)outFrames.put(v,newf);
					if (prevof != null && !frmComp(prevof,newf,v)){							
						System.err.println("C: Gosling violation:"
								+ prevf.toString()+newf.toString());
						System.exit(-1);
					}
					
				}
			}// while (!ics.isEmpty()) END

			InstructionHandle ih = start.getInstruction();
            
			//Check that all instruction have been simulated
			do{
				InstructionContext ic = cfg.contextOf(ih);
				if(ic.getTag() == 0){
					System.err.println("Instruction "+ic.toString()+" not simulated.");
					System.exit(-1);
				}
			}while((ih = ih.getNext()) != null);
			
			// int inFramesSize = inFrames.size();
			// System.out.println("numInstructions:"+instCnt+"
			// inFramesSize:"+inFramesSize);

			// This array holds the operand type bits in the low bits
			ogci = new int[instCnt];
			// This array holds the local type bits in the low bits
			mgci = new int[instCnt];
			pcinfo = new String[instCnt];
			int oldPC = 0;
			int PC = 0;
			int icnt = 0;
			// for debug info
			StringBuffer ogciStr = new StringBuffer();

			ih = start.getInstruction();
			do {
				oldPC = PC;
				PC = ih.getPosition();
				
				// Pad up the operands with the same ref bits when the PC gets
				// ahead
				// it will either save time if a linked list is used or not be
				// used for anything
				// at run time as the PC will not point to the padded positions.
				for (int i = oldPC + 1; i < PC; i++) {
					// System.out.println("Padding up:
					// ogci["+i+"]=ogci["+oldPC+"]");
					ogci[i] = ogci[oldPC];
					mgci[i] = mgci[oldPC];
				}
				ogciStr.append("ih:" + ih.toString());
				pcinfo[PC] = ih.toString();
				InstructionContext ic = cfg.contextOf(ih);
				// System.out.println(ih.toString()+" tag:"+ic.getTag());
				// It is here the incoming frame is used to achieve the desired
				// PC->operand mapping
				Frame f1 = (Frame) inFrames.get(ic);
				LocalVariables lvs = f1.getLocals();
				// mapping the all the locals for this value of the PC
				for (int i = 0; i < lvs.maxLocals(); i++) {
					if (lvs.get(i) instanceof ReferenceType) {
						int ref = (1 << i);
						mgci[PC] |= ref;
					}
					if (lvs.get(i) instanceof UninitializedObjectType) {
						// this.addMessage("Warning: ReturnInstruction '"+ic+"'
						// may leave method with an uninitialized object in the
						// local variables array '"+lvs+"'.");
					}
				}
				OperandStack os = f1.getStack();
				// System.out.println("-->ih["+PC+"]: line"+ih.toString()+"
				// icnt:"+icnt);
				// System.out.println(" pre");
				// System.out.println(" os slots:"+os.slotsUsed());
				ogciStr.append(",os slots:" + os.slotsUsed());
				// Each slot is 32 bits
				int j = 0; // Used to offset for LONG and DOUBLE
				// mapping the operands for this value of the PC
				for (int i = 0; i < os.slotsUsed(); i++, j++) {
					// System.out.println("
					// op["+i+"]:"+(os.peek(j)).getSignature());
					ogciStr.append(",operand(" + i + "):"
							+ (os.peek(j)).getSignature());
					if (os.peek(j) instanceof UninitializedObjectType) {
						// System.out.println("Warning: ReturnInstruction
						// '"+ic+"' may leave method with an uninitialized
						// object on the operand stack '"+os+"'.");
					}
					if (os.peek(j) instanceof ReferenceType) {
						int ref = (1 << i);
						ogci[PC] |= ref;
					}
					// peek() is per type
					if (os.peek(j).getSignature().equals("J")
							|| os.peek(j).getSignature().equals("D")) {
						j--;
					}
				}
				// System.out.println(" ogci["+PC+"]:"+bitStr(ogci[PC]));

				// This frame is post (after) the instruction and thus not what
				// we need
				Frame f2 = ic.getOutFrame(new ArrayList());
				LocalVariables lvs2 = f2.getLocals();
				int ogci2 = 0;
				for (int i = 0; i < lvs2.maxLocals(); i++) {
					if (lvs2.get(i) instanceof UninitializedObjectType) {
						// System.out.println("Warning: ReturnInstruction
						// '"+ic+"' may leave method with an uninitialized
						// object in the local variables array '"+lvs+"'.");
					}
				}
				OperandStack os2 = f2.getStack();
				// System.out.println(" post");
				// System.out.println(" os slots:"+os2.slotsUsed());
				// Each slot is 32 bits
				j = 0; // Used to offset for LONG and DOUBLE

				for (int i = 0; i < os2.slotsUsed(); i++, j++) {
					// System.out.println("
					// op["+i+"]:"+(os2.peek(j)).getSignature());
					// ogciStr.append(",operand("+i+"):"+(os2.peek(j)).getSignature());
					if (os2.peek(j) instanceof UninitializedObjectType) {
						// System.out.println("Warning: ReturnInstruction
						// '"+ic+"' may leave method with an uninitialized
						// object on the operand stack '"+os+"'.");
					}
					if (os2.peek(j) instanceof ReferenceType) {
						int ref = (1 << i);
						ogci2 |= ref;
					}
					// peek() is per type
					if (os2.peek(j).getSignature().equals("J")
							|| os2.peek(j).getSignature().equals("D")) {
						j--;
					}
				}

				// System.out.println(" ogci["+PC+"]:"+bitStr(ogci2));

				icnt++;
				// System.out.println("");
			} while ((ih = ih.getNext()) != null);

			// pad up the end
			for (int i = (PC + 1); i < instCnt; i++) {
				// System.out.println("Post padding up:
				// ogci["+i+"]=ogci["+PC+"]");
				ogci[i] = ogci[PC];
				mgci[i] = mgci[PC];
			}

			// Good for debugging
			// System.out.println(" ***
			// "+mi.cli.clazz.getClassName()+"."+mi.methodId+" --> mreallocals
			// ="+mreallocals+" margs ="+margs+" mstack ="+mstack);
			for (int i = 0; i < instCnt; i++) {
				// System.out.println(pcinfo[i]+" ogci["+i+"]"+bitStr(ogci[i]));
				// System.out.println(pcinfo[i]+" mgci["+i+"]"+bitStr(mgci[i]));
			}
			// System.out.println("--");

			// calculate length for raw method
			int varcnt = mstack + mreallocals + margs;
			int pos = instCnt * varcnt;
			length = pos / WORDLEN; // whole words
			if ((pos % WORDLEN) > 0) {
				length++; // partly used word
			}
			length++; // gcpack
		} // if has code
	}

	
	
	/**
	 * Compares the operands of two frames. It will detect the rare event that
	 * the Gosling property is violated from two jsr instructions reaching the
	 * same code but with different operand or local signatures. Operands are
	 * checked for fun even though they must obey the Gosling property. TODO:
	 * Implement code that can split local variables if a violation occurs.
	 * 
	 * @param prevf
	 *            the previous frame if it has been visited before
	 * @param newf
	 *            the next frame
	 * @return true if the frames equal or if prevf == null
	 */
	boolean frmComp(Frame prevf, Frame newf, InstructionContext ic){
		boolean res = true;

		LocalVariables lvprev = prevf.getLocals();
		LocalVariables lvnew = newf.getLocals();
		OperandStack opprev = prevf.getStack();
		OperandStack opnew = newf.getStack();

		if(opprev.slotsUsed()!=opnew.slotsUsed()){
			res = false;
			System.out.println("OperandStack size does not equal new OperandStack."+
					prevf.toString()+newf.toString()+ic.toString());
			
		}		
		
		int j=0;
		for(int i=0;i<opprev.slotsUsed();i++, j++){
			if(opprev.peek(j).getType()!=opnew.peek(j).getType()){
				System.err.println("Operands differ "+opprev.peek(j).toString()+" "+ opnew.peek(j).toString());
				res = false;
			}
			// peek() is per type
			if (opprev.peek(j).getSignature().equals("J")
					|| opprev.peek(j).getSignature().equals("D")) {
				j--;
			}
		}
        
	    if(lvprev.maxLocals()!=lvnew.maxLocals()){
			res = false;
			System.out.println("MaxLocals LocalVariables does not equal new LocalVariables."+
					prevf.toString()+newf.toString()+ic.toString());
			
		}		
		for(int i=0;i<lvprev.maxLocals();i++){
		  if(lvprev.get(i).getType()!=lvnew.get(i).getType()){
			  res = false;
			  System.err.println("Local types differ "+lvprev.get(i).toString()+" "+ lvnew.get(i).toString());
		  }
		}
		
		
//		TODO: Why does this not work for RTThread?
		if(!opprev.equals(opnew)||!lvprev.equals(lvnew)){
//			res = false;
			int stopit = 0;
//			System.out.println("Previous OperandStack or Localvars does not equal new OperandStack."+
//					prevf.toString()+newf.toString()+ic.toString());
		}		
		return res;
	}
	
	/*
	boolean frmComp(Frame prevf, Frame newf, InstructionContext ic) {
		boolean res = true;
		if (prevf != null) {
			// check the operand stacks
			OperandStack osp = prevf.getStack();
			OperandStack osn = newf.getStack();

			if (osp.slotsUsed() != osn.slotsUsed()) {
				System.out
						.println("Frame OperandStack slotsUsed does not equal");
				res = false;
			}
			int j = 0; // Used to offset for LONG and DOUBLE

			for (int i = 0; i < osp.slotsUsed(); i++, j++) {

				if (osp.peek(j).getSignature() != osn.peek(j).getSignature()
						&& !(osp.peek(j).equals(BasicType.UNKNOWN) || osn.peek(
								j).equals(BasicType.UNKNOWN))) {
					System.out.println("Error: Signatures does not match");
					res = false;
				}
				if ((osp.peek(j) instanceof UninitializedObjectType) != (osn
						.peek(j) instanceof UninitializedObjectType)
						&& !(osp.peek(j).equals(BasicType.UNKNOWN) || osn.peek(
								j).equals(BasicType.UNKNOWN))) {
					System.out
							.println("Error: Operand stacks not equal for UninitializedObjectType");
					res = false;
				}

				if ((osp.peek(j) instanceof ReferenceType) != (osn.peek(j) instanceof ReferenceType)
						&& !(osp.peek(j).equals(BasicType.UNKNOWN) || osn.peek(
								j).equals(BasicType.UNKNOWN))) {
					System.out
							.println("Error: Operand stacks not equal for ReferenceType");
					res = false;
				}
				// peek() is per type
				if (osp.peek(j).getSignature().equals("J")
						|| osn.peek(j).getSignature().equals("D")) {
					j--;
				}
			}

			// check the locals
			LocalVariables lvsp = prevf.getLocals();
			LocalVariables lvsn = newf.getLocals();

			if (lvsp.maxLocals() != lvsn.maxLocals()) {
				System.out
						.println("Frame LocalVariables maxLocals does not equal");
				res = false;
			}

			// mapping the all the locals for this value of the PC

			for (int i = 0; i < lvsp.maxLocals(); i++) {

				if (!lvsp.get(i).getSignature().equals(
						lvsn.get(i).getSignature())
						&& !(lvsp.get(i).equals(BasicType.UNKNOWN) || lvsn.get(
								i).equals(BasicType.UNKNOWN))) {
					System.out.println("Error: Local signatures for index " + i
							+ " from prev frame does not match next frame");
					System.out.println(lvsp.get(i).getSignature());
					System.out.println(lvsn.get(i).getSignature());
					res = false;
				}

				if (lvsp.get(i) instanceof ReferenceType != lvsn.get(i) instanceof ReferenceType
						&& !(lvsp.get(i).equals(BasicType.UNKNOWN) || lvsn.get(
								i).equals(BasicType.UNKNOWN))) {
					System.out
							.println("Error: Local ref from prev frame does not match next frame");
					res = false;
				}
				if (lvsp.get(i) instanceof UninitializedObjectType != lvsn
						.get(i) instanceof UninitializedObjectType
						&& !(lvsp.get(i).equals(BasicType.UNKNOWN) || lvsn.get(
								i).equals(BasicType.UNKNOWN))) {
					System.out
							.println("Error: Local unit ref from prev frame does not match next frame");
					res = false;
				}
			}
		} // prev != null

		if (res == false) { // print debug
			System.out.println(" *** " + mi.cli.clazz.getClassName() + "."
					+ mi.methodId + " --> mreallocals =" + mreallocals
					+ " margs =" + margs + " mstack =" + mstack);
			System.out.println("ic:" + ic.toString());
			System.out.println("ih:" + ic.getInstruction().toString());
			System.out.println("pc:" + ic.getInstruction().getPosition());
			System.out.println("prev Frame:");
			System.out.println(prevf);
			System.out.println("new Frame:");
			System.out.println(newf);

		}
		return true;
		// return res;
	}
*/
	/**
	 * It dumps the local variable and stack operands GC info structures. Can be
	 * called with out==null to get the word length. If the method is <code>
   * clinit</code> then the stackmap is not written out.
	 */
	public int dumpMethodGcis(PrintWriter out) {
		// System.out.println("dumpMethodGcis(PrintWriter out):"+out+",
		// mi.methodId:"+mi.methodId);

		int GCIHEADERLENGTH = 1; // key, gcpack

		// gcpack
		int UNICNTLEN = 10; // worst case if every PC has a unique pattern
		int INSTRLEN = 10; // 1024 instructions
		int MAXSTACKLEN = 5; // max 31 operands
		int MAXLOCALSLEN = 5; // max 31 args+locals
		int LOCALMARKLEN = 1; // 1 if local references
		int STACKMARKLEN = 1; // 1 if stack references

		int cntMgciWords = 0;
		int localmark = 0;
		int stackmark = 0;
		int ogcimark = 0;
		int mgcimark = 0;
    
    if(gcMethods != null && gcMethods.contains(method)){
      if(out != null){
        out.println("\n\t// no stackwalker info for "
            + mi.getCli().clazz.getClassName() + "." + mi.methodId +" because cfgReduce == true and gcMethods.contains(method)\n");
      }

      return 0;
    }
      
    
    
		// if(mi.code != null){ // not abstract
		if (!method.isAbstract()) {
			if (instCnt != mgci.length || instCnt != ogci.length) {
				System.err
						.println("exit: instCnt!=mgci.length || instCnt!=ogci.length");
				System.exit(-1);
			}

			if (out != null) {
				out.println("\n\t//stackwalker info for "
						+ mi.getCli().clazz.getClassName() + "." + mi.methodId);
			}

			StringBuffer sb = new StringBuffer();
			int mask = 0x01;
			int WORDLEN = 32;
			// raw packing
			int[] bitMap = new int[2 * instCnt];
			StringBuffer[] bitInfo = new StringBuffer[2 * instCnt];
			for (int i = 0; i < instCnt; i++) {
				bitInfo[i] = new StringBuffer();
				bitInfo[i].append("[bit,pos,index,offset,mgci or ogci]:");
			}

			int pos = 0;
			int index = 0;

			// raw patterns later used to determine unique patterns and the
			// index
			int pattern[] = new int[instCnt];

			// pack bitMap
			for (int i = 0; i < instCnt; i++) {
				// used for Ref. reduction in paper
				if (mgci[i] > 0) {
					mgcimark = 1;
				}
				if (ogci[i] > 0) {
					ogcimark = 1;
				}
				// pack locals in high bits
				// pattern[i] is also done here
				for (int j = 0; j < (mreallocals + margs); j++) {
					int bit = (mgci[i] >>> j) & mask;

					pattern[i] |= bit << (j + mstack); // (pattern[i]<<1)|

					index = pos / WORDLEN;
					int offset = pos % WORDLEN;
					bit = bit << offset;
					bitMap[index] |= bit;
					bitInfo[index].append("[" + bit + "," + pos + "," + index
							+ "," + offset + "," + "mgci[" + i + "][" + j
							+ "]] ");
					pos++;
				}
				// pack stack in low bits
				for (int j = 0; j < mstack; j++) {
					int bit = (ogci[i] >>> j) & mask;

					pattern[i] |= bit << j;

					index = pos / WORDLEN;
					int offset = pos % WORDLEN;
					bit = bit << offset;
					bitMap[index] |= bit;
					bitInfo[index].append("[" + bit + "," + pos + "," + index
							+ "," + offset + "," + "ogci[" + i + "][" + j
							+ "]] ");
					pos++;
				}
			}// for

			// packing bitMap2 according to the reduced indexed method.
			// identify the unique patterns
			int unique = 0;
			int uniquepattern[] = new int[instCnt];
			boolean match;
			for (int i = 0; i < instCnt; i++) {
				match = false;
				for (int j = 0; j < unique; j++) {
					if (pattern[i] == uniquepattern[j]) {
						match = true;
						break;
					}
				}
				if (!match) {
					uniquepattern[unique] = pattern[i];
					// System.out.println("uniquepattern["+unique+"]="+bitStr(uniquepattern[unique]));
					unique++;
				}
			}

			// bits needed for unique patterns
			int patbits = unique * (mreallocals + margs + mstack);

			// used in a paper
			if (false) { // index2 reduction
				if (mgcimark == 0) {
					patbits -= unique * (mreallocals + margs);
				}
				if (ogcimark == 0) {
					patbits -= unique * (mstack);
				}
			}

			// count index bits
			int num = 1;
			int indexbits = 0;
			for (int i = 1; i < 32; i++) {
				if (num < unique) {
					num = (num << 1) | 1;
				} else {
					indexbits = i;
					break;
				}
			}
			// count bits used on indexing
			int pcbits = indexbits * instCnt;
			int totalbits = pcbits + patbits;

			// total words for bitmap
			int wdc = totalbits / 32;
			if (totalbits % 32 > 0) {
				wdc++;
			}

			// now make the indexed bitmaps
			int bitMap2[] = new int[wdc];
			StringBuffer[] bitInfo2 = new StringBuffer[wdc];
			for (int i = 0; i < wdc; i++) {
				bitInfo2[i] = new StringBuffer();
			}
			int bitpos = 0;

			// bit map the indexes
			for (int i = 0; i < instCnt; i++) {
				match = false;
				for (int j = 0; j < unique; j++) {
					if (pattern[i] == uniquepattern[j]) {
						int control = 0;
						bitInfo2[bitpos / 32].append(" p" + i + "=up" + j
								+ ":[" + bitpos + ":");
						for (int k = 0; k < indexbits; k++) {
							int index2 = bitpos / 32;
							int offset2 = bitpos % 32;
							int bit = (j >>> k) & 0x01;
							control |= (bit << k);
							bitMap2[index2] |= (bit << offset2);
							bitInfo2[index2].append(bit);
							if (k == indexbits - 1) {
								bitInfo2[index2].append("]");
							}
							bitpos++;
						}

						// System.out.println("pattern["+i+"]==uniquepattern["+j+"]");
						// System.out.println("control:"+control);
						match = true;
						break;
					}
				} // for unique
				if (!match) {
					System.err.println("Problem with uniquematch for PC:" + i);
					System.exit(-1);
				}
			}

			// bit map the unique patterns
			for (int i = 0; i < unique; i++) {
				bitInfo2[bitpos / 32].append(" up" + i + ":[" + bitpos + ":");
				for (int j = 0; j < (mreallocals + margs + mstack); j++) {
					int index2 = bitpos / 32;
					int offset2 = bitpos % 32;
					int bit = (uniquepattern[i] >>> j) & 0x01;
					bitMap2[index2] |= (bit << offset2);
					bitInfo2[index2].append(bit);
					if (j == (mreallocals + margs + mstack) - 1) {
						bitInfo2[index2].append("]");
					}
					bitpos++;
				}
			}

			// pack gcpack
			int gcpack = unique; // unique patterns
			gcpack = (gcpack << INSTRLEN) | instCnt;
			gcpack = (gcpack << MAXSTACKLEN) | mstack;
			gcpack = (gcpack << MAXLOCALSLEN) | (mreallocals + margs);

			if ((mreallocals + margs) > 0) {
				localmark = 1;
			}
			// gcpack = (gcpack<<LOCALMARKLEN)|(localmark); // dump bit maps
			// even if no refs
			gcpack = (gcpack << LOCALMARKLEN) | (mgcimark); // only dump map
			// when a ref is
			// among the
			// operands "Ref.
			// only" in paper

			if (mstack > 0) {
				stackmark = 1;
			}
			// gcpack = (gcpack<<STACKMARKLEN)|(stackmark); // see above
			gcpack = (gcpack << STACKMARKLEN) | (ogcimark);

			// System.out.println("dumpMethodGcis Class
			// method:"+mi.cli.clazz.getClassName()+"."+mi.methodId);
			// System.out.println("patbits:"+patbits);
			// System.out.println("pcbits:"+pcbits);
			// System.out.println("unique:"+unique);
			// System.out.println("instCnt:"+instCnt);
			// System.out.println("indexbits:"+indexbits);
			// System.out.println("locals:"+(mreallocals+margs));
			// System.out.println("localmark:"+localmark);
			// System.out.println("stack:"+mstack);
			// System.out.println("stackmark:"+stackmark);
			// System.out.println("index="+index+"
			// bitMap.length:"+bitMap.length+" instCnt:"+instCnt+" pos:"+pos);
			pcwords = 1; // set this to 0 or 1 depending if gcpack is written
			// out

      //Don't write stackmap for <clinit> methods
      if(method.getName().equals("<clinit>")){
        gcpack = 0;
        localmark = 0;
        stackmark = 0;
        mgcimark = 0;
        ogcimark = 0;
      } // just check that the bitMap can be reconstructed from bitMap2 
      else if (!compareBitMap(bitMap, bitMap2, gcpack, mgci, ogci)) {
        System.err.println("Error in the bitmaps.");
        System.exit(-1);
      }
      
			// can also use localmark and stackmark here if bit maps are needed
			// regardless
			// of the presense of references
			if (instCnt > 0 && (mgcimark == 1 || ogcimark == 1)) {
				pcwords += (patbits + pcbits) / 32;
				if (((patbits + pcbits) % 32) > 0) {
					pcwords++;
				}
				// change flag below and also on in setMarkWords() in GCStkWalk
				if (true) { // write out indexed version
					for (int i = 0; i < wdc; i++) {
						if (out != null) {
							out.println("\t" + bitMap2[i] + ",//\tbitMap2["
									+ i + "]: " + bitInfo2[i].toString() + ", "
									+ bitStr(bitMap2[i]));
						}
						cntMgciWords++;
					}
          //debug
          for (int i = 0; i < instCnt; i++) {
            if (out != null) {
              out.println("\t// mgci["+i+"]:"+bitStr(mgci[i])+" ogci["+i+"]:"+bitStr(ogci[i]));
            }
          }
				} else // write out raw version
				{
					for (int i = 0; i <= index; i++) {
						if (out != null) {
							// out.println("\t\t//\tbitMap["+i+"]:"+bitStr(bitMap[i])+"
							// "+bitInfo[i].toString());
							out.println("\t\t" + bitMap[i] + ",//\tbitMap[" + i
									+ "]:" + bitStr(bitMap[i]));
						}
						cntMgciWords++;
					}
				}
			}

			cntMgciWords += GCIHEADERLENGTH;
			if (out != null) {
				out.println("\t" + gcpack + ",//\tgcpack[0:stackmark="
						+ stackmark + ",1:localmark=" + localmark
						+ ",2-6:maxlocals=" + (mreallocals + margs)
						+ ",7-11:maxstack=" + mstack + ",12-21:instr="
						+ instCnt + ",22-31:unicnt=" + unique
						+ "] indexbits(to index unique):" + indexbits
						+ " gcpack:" + bitStr(gcpack));
			}

			// System.out.println("cntMgciWords:"+cntMgciWords);
			// System.out.println("pcwords:"+pcwords);
			if (out != null) {
				totalpcwords += pcwords;
				totalcntMgciWords += cntMgciWords;
			}
			// System.out.println("totalpcwords:"+totalpcwords);
			// System.out.println("totalcntMgciWords:"+totalcntMgciWords);
			// System.out.println("--");

		} // if not abstract
		// System.err.println("dumpMethodGcis
		// method:"+cli.clazz.getClassName()+"."+methodId);
		// System.err.println("out MethodInfo.cntMgci:"+MethodInfo.cntMgci+"
		// MethodInfo.cntMgciWords:"+MethodInfo.cntMgciWords);
		// System.err.println("index="+index+" bitMap.length:"+bitMap.length+"
		// instCnt:"+instCnt+" mreallocals:"+mreallocals+" margs:"+margs +"
		// mstack:"+mstack+" pos:"+pos);
		// System.err.println("method.isAbstract():"+method.isAbstract());
		// System.err.println("...");
		// System.out.println("cntMgciWords:"+cntMgciWords);
		return cntMgciWords;
	}
  
  /**
   * Called with a pc (program counter) value which will be removed from the 
   * mgci and ogci structures. The <oode>instCnt</code> is also decremented by
   * one.
   * @param pc
   * @return true if pc<instCnt
   */
  public void removePC(int pc){
    
    if(gcMethods != null && gcMethods.contains(method))
      return;
    
    int oldogci[] = ogci;
    int oldmgci[] = mgci;
    ogci = new int[instCnt-1];
    mgci = new int[instCnt-1];
      
    if(pc==0){
      System.arraycopy(oldogci,1,ogci,0,instCnt-1);        
      System.arraycopy(oldmgci,1,mgci,0,instCnt-1);
    } else if(pc==instCnt-1){
      System.arraycopy(oldogci,0,ogci,0,instCnt-1);
      System.arraycopy(oldmgci,0,mgci,0,instCnt-1);
    }
    else{
      System.arraycopy(oldogci,0,ogci,0,pc);
      System.arraycopy(oldogci,pc+1,ogci,pc,instCnt-1-pc);
      System.arraycopy(oldmgci,0,mgci,0,pc);
      System.arraycopy(oldmgci,pc+1,mgci,pc,instCnt-1-pc);
    }
    
    //reduce instcnt accordingly
    instCnt--;
  }

	// Sanity checking the bitmaps by unpacking both and comparing
	boolean compareBitMap(int bitMap[], int bitMap2[], int gcpack, int mcgi[],
			int ocgi[]) {
		boolean compareresult = true;

		int UNICNTLEN = 10; // worst case if every PC has a unique pattern
		int INSTRLEN = 10; // 1024 instructions
		int MAXSTACKLEN = 5; // max 31 operands
		int MAXLOCALSLEN = 5; // max 31 args+locals
		int LOCALMARKLEN = 1; // 1 if local references
		int STACKMARKLEN = 1; // 1 if stack references

		int unicnt = (gcpack >>> (INSTRLEN + MAXSTACKLEN + MAXLOCALSLEN
				+ LOCALMARKLEN + STACKMARKLEN)) & 0x03ff;
		int instr = (gcpack >>> (MAXSTACKLEN + MAXLOCALSLEN + LOCALMARKLEN + STACKMARKLEN)) & 0x03ff;
		int maxstack = (gcpack >>> (MAXLOCALSLEN + LOCALMARKLEN + STACKMARKLEN)) & 0x1f;
		int maxlocals = (gcpack >>> (LOCALMARKLEN + STACKMARKLEN)) & 0x1f;
		int localmark = (gcpack >>> (STACKMARKLEN)) & 0x01;
		;
		int stackmark = gcpack & 0x01;
		// System.out.println("unicnt:"+unicnt);
		// System.out.println("instr:"+instr);
		// System.out.println("maxstack:"+maxstack);
		// System.out.println("maxlocals:"+maxlocals);
		// System.out.println("localmark:"+localmark);
		// System.out.println("stackmark:"+stackmark);
		int num = 1;
		int indexbits = 0;
		for (int i = 1; i < 32; i++) {
			if (num < unicnt) {
				num = (num << 1) | 1;
			} else {
				indexbits = i;
				break;
			}
		}

		// reconstruct patterns from bitMap2
		int patternindex2[] = new int[instr];
		int unipattern[] = new int[unicnt];
		int pos = 0;
		for (int i = 0; i < instr; i++) {
			patternindex2[i] = getBitsFromArray(bitMap2, pos, indexbits);
			pos += indexbits;
		}
		for (int i = 0; i < unicnt; i++) {
			unipattern[i] = getBitsFromArray(bitMap2, pos,
					(maxstack + maxlocals));
			pos += (maxstack + maxlocals);
		}

		// bitmap1 patterns
		int patterns1[] = new int[instr];
		pos = 0;
		for (int i = 0; i < instr; i++) {
			patterns1[i] = getBitsFromArray(bitMap, pos, (maxstack + maxlocals));
			pos += (maxstack + maxlocals);
			if (patterns1[i] != unipattern[patternindex2[i]]) {
				// System.out.println("mgci["+i+"]:"+bitStr(mcgi[i]));
				// System.out.println("ocgi["+i+"]:"+bitStr(ocgi[i]));
				// System.out.println("patterns1["+i+"]:"+bitStr(patterns1[i]));
				// System.out.println("patternindex2["+i+"]:"+patternindex2[i]);
				// System.out.println("unipattern[patternindex2[["+i+"]]:"+bitStr(unipattern[patternindex2[i]]));
				compareresult = false;
				break;
			}
		}

		return compareresult;
	}

	// can return max 32 bits.
	int getBitsFromArray(int array[], int pos, int len) {
		if (len > 32) {
			System.out.println("len>32");
			System.exit(-1);
		}
		if ((pos + len) > array.length * 32) {
			System.out.println("pos:" + pos + " len:" + len
					+ " too big for array[].length:" + array.length);

			System.exit(-1);
		}
		int res = 0;
		for (int i = pos; i < pos + len; i++) {
			int index = i / 32;
			int offset = i % 32;
			int word = array[index];
			res |= (word >> offset) & 0x01;
		}
		return res;
	}

	/**
	 * Returns the length in words that the GC info wil consume. Called from
	 * SetMethodInfo's visitJavaClass method.
	 */
	public int gcLength() {
    int len = -1;
    if(gcMethods != null && gcMethods.contains(method)){
      len = 0;
    } 
    else {
      len = dumpMethodGcis(null);
    }
   
    return len;
	}

	/**
	 * Make a word into a 0/1 bit string. Note that the bit are written with the
	 * low bits first.
	 */
	String bitStr(int word) {
		// make ogci[PC] to bit string for debugging
		StringBuffer sb = new StringBuffer();
		int mask = 0x01;
		// for(int i = 31;i>=0;i--){
		for (int i = 0; i < 32; i++) {
			int res = (word >>> i) & mask;
			if ((i + 1) % 8 == 0 && i < 31) {
				sb.append("_");
			}
			sb.append(res);
		}
		return sb.toString();
	}

}

/**
 * Extends org.apache.bcel.verifier.structurals.Frame just to get access to the
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
 * BCEL throws an exception for the util.Dbg class because it overloads a field.
 * The choice (as described in the BCEL method comment for around line 2551 in
 * org.apache.bcel.verifier.structurals.InstConstraintVisitor) is to comment out
 * this check and recompile BCEL jar. Insted of recompiling BCEL the choice is
 * to override the methods, which is ok? as we are not using BCEL for bytecode
 * verification purposes (using Sun Javac).
 */
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
