package com.jopdesign.build;

//NOTE: IT DOES NOT WORK YET AND IS WORK IN PROGRESS:-)
import java.util.*;
import java.io.PrintWriter;

import org.apache.bcel.classfile.*;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.exc.*;
import org.apache.bcel.verifier.structurals.*;

import sun.awt.windows.WVolatileImage;

/**
 * The class is a STARTING POINT for wcet analysis. It is pretty
 * straight-forward to add classes that hook into the
 * <code>controlFlowGraph</code> and is used as a departure for further
 * analysis. The <code>WCETMethodBlock</code> is an example of how the basic
 * blocks can be created.
 * 
 * The control flow graph is used as a basis for creating the basic blocks. It
 * builds up an array of basic blocks in the WCETMethodBlock.
 * 
 * The basic hierarchy is that WCETAnalyzer creates one WCETMethodBlock for each
 * method. The WCETMethodBlock assists WCETAnalyzer in creating the
 * WCETBasicBlock objects for each basic block. Then WCETBasicBlock can be used
 * together with WCETInstruction to calculate the WCET value for that particular
 * basic block.
 * 
 * @author rup, ms
 * @see Section 7.4 and Appendix D in MS thesis
 */
// History:
// 01-04-2006 rup: Initial version aimed at directed graph of basic blocks with
// wcet number
// TODO: Count cycles in BasicBlocks, how to get a grip on loops (unrolling?)
// TODO: How to analyze those bytecodes implemented in Java?
// TODO: How to handle the {209-221} opcodes for JOP?
public class WCETAnalyser {

  /**
   * The WCETAnalsis objects for each method.
   */
  static HashMap miMap = new HashMap();

  /**
   * Simulate and create the control flow graph.
   * 
   * @param mi
   *          the method
   */
  public static void controlFlowGraph(MethodInfo mi) {
    ((WCETAnalyser) miMap.get(mi)).controlFlowGraph();
  }

  /**
   * Return the control flow graph.
   * 
   * @param method
   * @return the control flow graph
   */
  public static ControlFlowGraph getControlFlowGraph(MethodInfo mi) {
    return ((WCETAnalyser) miMap.get(mi)).getControlFlowGraph();
  }

  /**
   * Return the method generator.
   * 
   * @param method
   * @return the method generator
   */
  public static MethodGen getMethodGen(MethodInfo mi) {
    return ((WCETAnalyser) miMap.get(mi)).getMethodGen();
  }

  /**
   * Return the method generator.
   * 
   * @param method
   * @return the method generator
   */
  public static HashMap getInFrames(MethodInfo mi) {
    return ((WCETAnalyser) miMap.get(mi)).getInFrames();
  }

  // instance vars
  MethodInfo mi;

  Method method;

  MethodGen mg;

  ControlFlowGraph cfg;

  // wcet stuff
  WCETMethodBlock wcmb;

  // Stack frame for the PC before execution (look at outframes to see after)
  HashMap inFrames;

  String tostr;

  String signature;

  String name;

  String cname;

  /**
   * Instanciated from from <code>SetClassInfo</code>.
   */
  public WCETAnalyser(MethodInfo mi, Method method) {
    this.mi = mi;
    this.method = method;

    if (miMap.containsKey(mi)) {
      System.err.println("Alredy added mi.");
      System.exit(-1);
    } else {
      miMap.put(mi, this);
    }
    // TODO: ms needs to check if the "3" needs to be added like
    // in MethodInfo?
    // set method length in 32 bit words
    WCETInstruction.setN((method.getCode().getCode().length + 3) / 4);
    // TODO: Needs to do something smarter here like a real calculation
    // set chance of cache miss
    WCETInstruction.setPmiss(0.0);
  }

  /**
   * Control flow analysis for WCET time.
   */
  public void controlFlowGraph() {
    JavaClass jc = mi.cli.clazz;

    ConstantPoolGen cpg = new ConstantPoolGen(jc.getConstantPool());

    // Some methods overridden (see bottom of this file)
    InstConstraintVisitor icv = new AnInstConstraintVisitor2();

    icv.setConstantPoolGen(cpg);

    ExecutionVisitor ev = new ExecutionVisitor();
    ev.setConstantPoolGen(cpg);

    mg = new MethodGen(method, jc.getClassName(), cpg);
    // To later get the PC positions of the instructions
    mg.getInstructionList().setPositions(true);
    tostr = mg.toString();
    signature = mg.getSignature();
    name = mg.getName();
    cname = mg.getClassName();

    // if(method.getName().equalsIgnoreCase("trace")){
    // boolean stop =true;
    // }
    icv.setMethodGen(mg);

    if (!(mg.isAbstract() || mg.isNative())) { // IF mg HAS CODE

      // pass 0: Create basic blocks
      InstructionHandle ih = mg.getInstructionList().getStart();
      // wcet startup: create the first full covering bb
      wcmb.init(ih, mg.getInstructionList().getEnd());

      do {
        // create new bb (a)for branch target and (b) for sucessor
        // TODO: What about return?
        if (ih.getInstruction() instanceof BranchInstruction) {
          InstructionHandle ihtar = ((BranchInstruction) ih.getInstruction())
              .getTarget();
          InstructionHandle ihnext = ih.getNext();
          wcmb.createBasicBlock(ihtar);
          if (ihnext != null) {
            //TODO: Check that last instruction works and that a basic block can be of length one
            wcmb.createBasicBlock(ihnext);
          }
        }
      } while ((ih = ih.getNext()) != null);

      // Pass 1: Set the id of each block
      int id = 0;
      // it is sorted on the (final) start pos of each block
      for (Iterator iter = wcmb.getBbs().keySet().iterator(); iter.hasNext();) {
        WCETBasicBlock wbb = (WCETBasicBlock) wcmb.getBbs().get(
            (Integer) iter.next());
        wbb.setId(id);
        id++;
      }

      // Pass 2: linking the blocks
      ih = mg.getInstructionList().getStart();

      do {
        if (ih.getInstruction() instanceof BranchInstruction) {
          // target
          InstructionHandle ihtar = ((BranchInstruction) ih.getInstruction())
              .getTarget();
          // next
          InstructionHandle ihnext = ih.getNext();
          WCETBasicBlock wbbthis = wcmb.getCoveringBB(ih);
          WCETBasicBlock wbbtar = wcmb.getCoveringBB(ihtar);
          // target wbb
          wbbthis.setTarbb(wbbtar);
          // targeter in target
          wbbtar.addTargeter(wbbthis);

          if (ihnext != null) {
            WCETBasicBlock wbbnxt = wcmb.getCoveringBB(ihnext);
            // nextwbb
            wbbthis.setSucbb(wbbnxt);
          }
        }
      } while ((ih = ih.getNext()) != null);

      // Pass 2:
      // TODO: Do we even need the controlflowgraph for this?
      cfg = new ControlFlowGraph(mg);
      // InstructionContext as key for inFrame
      inFrames = new HashMap();
      HashMap outFrames = new HashMap();

      // Build the initial frame situation for this method.
      FrameFrame2 fStart = new FrameFrame2(mg.getMaxLocals(), mg.getMaxStack());
      if (!mg.isStatic()) {
        if (mg.getName().equals(Constants.CONSTRUCTOR_NAME)) {
          fStart.setThis(new UninitializedObjectType(new ObjectType(jc
              .getClassName())));
          fStart.getLocals().set(0, fStart.getThis());
        } else {
          fStart.setThis(null);
          fStart.getLocals().set(0, new ObjectType(jc.getClassName()));
        }
      }
      Type[] argtypes = mg.getArgumentTypes();
      int twoslotoffset = 0;
      for (int j = 0; j < argtypes.length; j++) {
        if (argtypes[j] == Type.SHORT || argtypes[j] == Type.BYTE
            || argtypes[j] == Type.CHAR || argtypes[j] == Type.BOOLEAN) {
          argtypes[j] = Type.INT;
        }
        fStart.getLocals().set(twoslotoffset + j + (mg.isStatic() ? 0 : 1),
            argtypes[j]);
        if (argtypes[j].getSize() == 2) {
          twoslotoffset++;
          fStart.getLocals().set(twoslotoffset + j + (mg.isStatic() ? 0 : 1),
              Type.UNKNOWN);
        }
      }

      // if (method.getName().equalsIgnoreCase("sort")) {
      // System.out.println(method.getCode().toString());
      //
      // }

      InstructionContext start = cfg.contextOf(mg.getInstructionList()
          .getStart());
      // don't need to compare for first frame
      inFrames.put(start, fStart);

      boolean fbool = start.execute(fStart, new ArrayList(), icv, ev);
      Frame fout = start.getOutFrame(new ArrayList());
      outFrames.put(start, fout);
      start.setTag(start.getTag() + 1);
      // int posnow = start.getInstruction().getPosition();

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
        // TODO: Would it be better to call wcet here instead of in the TAG
        // count
        // loop?
        // System.out.println(u.toString());
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
          ReturnaddressType t = (ReturnaddressType) u.getOutFrame(oldchain)
              .getLocals().get(ret.getIndex());
          InstructionContext theSuccessor = cfg.contextOf(t.getTarget());

          // Sanity check
          InstructionContext lastJSR = null;
          int skip_jsr = 0;
          for (int ss = oldchain.size() - 1; ss >= 0; ss--) {
            if (skip_jsr < 0) {
              throw new AssertionViolatedException(
                  "More RET than JSR in execution chain?!");
            }
            if (((InstructionContext) oldchain.get(ss)).getInstruction()
                .getInstruction() instanceof JsrInstruction) {
              if (skip_jsr == 0) {
                lastJSR = (InstructionContext) oldchain.get(ss);
                break;
              } else {
                skip_jsr--;
              }
            }
            if (((InstructionContext) oldchain.get(ss)).getInstruction()
                .getInstruction() instanceof RET) {
              skip_jsr++;
            }
          }
          if (lastJSR == null) {
            throw new AssertionViolatedException(
                "RET without a JSR before in ExecutionChain?! EC: '" + oldchain
                    + "'.");
          }
          JsrInstruction jsr = (JsrInstruction) (lastJSR.getInstruction()
              .getInstruction());
          if (theSuccessor != (cfg.contextOf(jsr.physicalSuccessor()))) {
            throw new AssertionViolatedException("RET '" + u.getInstruction()
                + "' info inconsistent: jump back to '" + theSuccessor
                + "' or '" + cfg.contextOf(jsr.physicalSuccessor()) + "'?");
          }

          if (theSuccessor.execute(u.getOutFrame(oldchain), newchain, icv, ev)) {
            ics.add(theSuccessor);
            ecs.add((ArrayList) newchain.clone());
          }
          // inFrames.put(theSuccessor,u.getOutFrame(oldchain));
          theSuccessor.setTag(theSuccessor.getTag() + 1);
          // osa[theSuccessor.getInstruction().getPosition()].add(fStart
          // .getStack().getClone());
          // lva[theSuccessor.getInstruction().getPosition()].add(fStart
          // .getLocals().getClone());
          Frame prevf = (Frame) inFrames.put(theSuccessor, u
              .getOutFrame(oldchain));
          Frame newf = theSuccessor.getOutFrame(newchain);
          Frame prevof = (Frame) outFrames.put(theSuccessor, newf);
          if (prevof != null && !frmComp(prevof, newf, theSuccessor)) {
            System.out.println("A: Gosling violation:" + prevf.toString()
                + newf.toString());
            System.exit(-1);
          }

        } else {// "not a ret"
          // Normal successors. Add them to the queue of successors.
          // TODO: Does u get executed?
          InstructionContext[] succs = u.getSuccessors();

          // System.out.println("suss#:" + succs.length);
          for (int s = 0; s < succs.length; s++) {
            InstructionContext v = succs[s];
            // System.out.println(v.toString());
            if (v.execute(u.getOutFrame(oldchain), newchain, icv, ev)) {
              ics.add(v);
              ecs.add((ArrayList) newchain.clone());
            }
            v.setTag(v.getTag() + 1);
            Frame prevf = (Frame) inFrames.put(v, u.getOutFrame(oldchain));
            Frame newf = v.getOutFrame(newchain);
            Frame prevof = (Frame) outFrames.put(v, newf);
            if (prevof != null && !frmComp(prevof, newf, v)) {
              System.out.println("B: Gosling violation:" + fStart.toString());
              System.exit(-1);
            }
          }
        }// end "not a ret"

        // Exception Handlers. Add them to the queue of successors.
        // [subroutines are never protected; mandated by JustIce]
        ExceptionHandler[] exc_hds = u.getExceptionHandlers();
        for (int s = 0; s < exc_hds.length; s++) {
          InstructionContext v = cfg.contextOf(exc_hds[s].getHandlerStart());
          Frame f = new Frame(u.getOutFrame(oldchain).getLocals(),
              new OperandStack(u.getOutFrame(oldchain).getStack().maxStack(),
                  (exc_hds[s].getExceptionType() == null ? Type.THROWABLE
                      : exc_hds[s].getExceptionType())));

          if (v.execute(f, new ArrayList(), icv, ev)) {
            ics.add(v);
            ecs.add(new ArrayList());
          }
          v.setTag(v.getTag() + 1);
          Frame prevf = (Frame) inFrames.put(v, f);
          Frame newf = v.getOutFrame(new ArrayList());
          Frame prevof = (Frame) outFrames.put(v, newf);
          if (prevof != null && !frmComp(prevof, newf, v)) {
            System.err.println("C: Gosling violation:" + prevf.toString()
                + newf.toString());
            System.exit(-1);
          }

        }
      }// while (!ics.isEmpty()) END

      // Check that all instruction have been simulated
      do {
        InstructionContext ic = cfg.contextOf(ih);
        if (ic.getTag() == 0) {
          System.err
              .println("Instruction " + ic.toString() + " not simulated.");
          System.exit(-1);
        }
        // TODO: Can it handle direct loops back
        if (ih.getInstruction() instanceof BranchInstruction) {
          int target = ((BranchInstruction) ih.getInstruction()).getTarget()
              .getPosition();
          InstructionHandle btarget = ((BranchInstruction) ih.getInstruction())
              .getTarget();
          // check and possibly create a new bb starting at the target
          wcmb.createBasicBlock(btarget);
          InstructionHandle ihnext = ih.getNext();
          // check if the next instruction was the target and if not see if a
          // new bb is to be created
          // TODO: Could it be true?
          if (!ihnext.equals(btarget)) {
            wcmb.createBasicBlock(ihnext);
          }

        }

      } while ((ih = ih.getNext()) != null);

    }

  }

  /**
   * Compares the operands of two frames. It will detect the rare event that the
   * Gosling property is violated from two jsr instructions reaching the same
   * code but with different operand or local signatures. Operands are checked
   * for fun even though they must obey the Gosling property. TODO: Implement
   * code that can split local variables if a violation occurs.
   * 
   * @param prevf
   *          the previous frame if it has been visited before
   * @param newf
   *          the next frame
   * @return true if the frames equal or if prevf == null
   */
  boolean frmComp(Frame prevf, Frame newf, InstructionContext ic) {
    boolean res = true;

    LocalVariables lvprev = prevf.getLocals();
    LocalVariables lvnew = newf.getLocals();
    OperandStack opprev = prevf.getStack();
    OperandStack opnew = newf.getStack();

    if (opprev.slotsUsed() != opnew.slotsUsed()) {
      res = false;
      System.out.println("OperandStack size does not equal new OperandStack."
          + prevf.toString() + newf.toString() + ic.toString());

    }

    int j = 0;
    for (int i = 0; i < opprev.slotsUsed(); i++, j++) {
      if (opprev.peek(j).getType() != opnew.peek(j).getType()) {
        System.err.println("Operands differ " + opprev.peek(j).toString() + " "
            + opnew.peek(j).toString());
        res = false;
      }
      // peek() is per type
      if (opprev.peek(j).getSignature().equals("J")
          || opprev.peek(j).getSignature().equals("D")) {
        j--;
      }
    }

    if (lvprev.maxLocals() != lvnew.maxLocals()) {
      res = false;
      System.out
          .println("MaxLocals LocalVariables does not equal new LocalVariables."
              + prevf.toString() + newf.toString() + ic.toString());

    }
    for (int i = 0; i < lvprev.maxLocals(); i++) {
      if (lvprev.get(i).getType() != lvnew.get(i).getType()) {
        res = false;
        System.err.println("Local types differ " + lvprev.get(i).toString()
            + " " + lvnew.get(i).toString());
      }
    }

    // TODO: Why does this not work for RTThread?
    if (!opprev.equals(opnew) || !lvprev.equals(lvnew)) {
      // res = false;
      int stopit = 0;
      // System.out.println("Previous OperandStack or Localvars does not equal
      // new OperandStack."+
      // prevf.toString()+newf.toString()+ic.toString());
    }
    return res;
  }

  public ControlFlowGraph getControlFlowGraph() {
    return cfg;
  }

  public MethodGen getMethodGen() {
    return mg;
  }

  public HashMap getInFrames() {
    return inFrames;
  }

}

/**
 * It has a HashMap of WCETBasicBlocks. The class have methods that are called
 * from the WCETAnalyzers controlFlowGraph method. It creates the the directed
 * graph of wcbbs.
 */
class WCETMethodBlock {
  // Basic Blocks
  TreeMap bbs = new TreeMap();

  // directed graph of the basic blocks
  int dg[][];

  // create a bb covering the whole method
  // from here on we split it when necessary
  public void init(InstructionHandle stih, InstructionHandle endih) {
    WCETBasicBlock wcbb = new WCETBasicBlock(stih, endih);
    // wcbb.setEnd(endih.getPosition()); //we will do it lastly
    bbs.put(new Integer(wcbb.getStart()), wcbb);
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
      // save the new bb in the hash map
      if (bbs.put(new Integer(stih.getPosition()), wcbb) != null) {
        System.err.println("The starting pos should be unique.");
        System.exit(-1);
      }
      res = true;
    }
    return res;
  }

  /**
   * It sorts the basic blocks and creates the directed graph.
   */
  public void directedGraph() {
    // now create the directed graph
    dg = new int[bbs.size()][bbs.size()];
    for (Iterator iter = bbs.keySet().iterator(); iter.hasNext();) {
      Integer keyInt = (Integer) iter.next();
      // HERE
      WCETBasicBlock wcbb = (WCETBasicBlock) bbs.get(keyInt);
      WCETBasicBlock tarwcbb = wcbb.getTarbb();
      // TODO: Check for bugs
      int id = wcbb.getId();
      if (tarwcbb != null) {
        int tarbbid = tarwcbb.getId();
        dg[id][tarbbid]++;
      }
      WCETBasicBlock sucbb = wcbb.getSucbb();
      if (sucbb != null) {
        int sucid = sucbb.getId();
        dg[id][sucid]++;
      }
    }

  }

  public TreeMap getBbs() {
    return bbs;
  }
}

/**
 * Basic block of byte codes
 */
class WCETBasicBlock {
  // id of the bb
  int id;

  // start pos
  final int start;

  final Integer key;

  final InstructionHandle stih;

  // end pos which will change as splitting happens
  int end;

  // end instruction handle
  InstructionHandle endih;

  // target branch block
  WCETBasicBlock tarbb;

  // sucessor block
  WCETBasicBlock sucbb;

  // invard links from other BBs called targeters
  HashMap inbbs = new HashMap();

  WCETBasicBlock(InstructionHandle stih, InstructionHandle endih) {
    start = stih.getPosition();
    key = new Integer(start);
    end = endih.getPosition();
    this.stih = stih;
    this.endih = endih;
  }

  /**
   * Add wbb that points to this wbb.
   * 
   * @param wbbtargeter
   *          a wbb that points to this wbb.
   * @return true if it was already added
   */
  boolean addTargeter(WCETBasicBlock wbbtargeter) {
    WCETBasicBlock wbbold = (WCETBasicBlock) inbbs.put(wbbtargeter.getKey(),
        wbbtargeter);
    if (wbbold == null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Will create a new BB by splitting the old.
   * 
   * @param stih
   *          the first instruction of the new block
   * @return the new BB
   */
  // TODO: Do they inherit the targeters (a "targeter" is something that points
  // to a target)
  WCETBasicBlock split(InstructionHandle newstih) {
    WCETBasicBlock spbb = new WCETBasicBlock(newstih, endih);
    // spbb.end = stih.getPrev().getPosition();
    // end the last block with the previous pos
    end = stih.getPrev().getPosition();
    endih = newstih.getPrev();
    // start the new block with stih handle
    // spbb.inbbs.put(new Integer(spbb.start), spbb);
    return spbb;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public InstructionHandle getStih() {
    return stih;
  }

  public InstructionHandle getEndih() {
    return endih;
  }

  public void setEndih(InstructionHandle endih) {
    this.endih = endih;
  }

  public WCETBasicBlock getTarbb() {
    return tarbb;
  }

  public WCETBasicBlock getSucbb() {
    return sucbb;
  }

  public void setTarbb(WCETBasicBlock tarbb) {
    this.tarbb = tarbb;
  }

  public void setSucbb(WCETBasicBlock sucbb) {
    this.sucbb = sucbb;
  }

  public Integer getKey() {
    return key;
  }
}

/**
 * It has wcet info on byte code instruction granlularity. Should we consider
 * making a class that wraps the microcodes into objects?
 */
class WCETInstruction {
  // indicate that wcet is not available for this byte code
  public static final int WCETNOTAVAILABLE = -1;

  // bytecode load
  public static final int a = 3;

  // mem read: 3 for mem and 3 for pipe
  public static final int r = 6;

  // mem write
  public static final int w = 6;

  // load time (it is set externally)
  public static int b = -1;

  // cache miss probability see ms thesis p. 232
  public static double pmiss = 1.0;

  // method size in 32 bit words (is it set externally)
  public static int n = -1;

  /**
   * Returns the wcet count for the instruction.
   * 
   * @see table D.1 in ms thesis
   * @param opcode
   * @return wcet cycle count or -1 if wcet not available
   */
  int getWCET(int opcode) {
    int wcet = 0;

    switch (opcode) {
    // NOP = 0
    case org.apache.bcel.Constants.NOP:
      wcet = 1;
      break;
    // ACONST_NULL = 1
    case org.apache.bcel.Constants.ACONST_NULL:
      wcet = 1;
      break;
    // ICONST_M1 = 2
    case org.apache.bcel.Constants.ICONST_M1:
      wcet = 1;
      break;
    // ICONST_0 = 3
    case org.apache.bcel.Constants.ICONST_0:
      wcet = 1;
      break;
    // ICONST_1 = 4
    case org.apache.bcel.Constants.ICONST_1:
      wcet = 1;
      break;
    // ICONST_2 = 5
    case org.apache.bcel.Constants.ICONST_2:
      wcet = 1;
      break;
    // ICONST_3 = 6
    case org.apache.bcel.Constants.ICONST_3:
      wcet = 1;
      break;
    // ICONST_4 = 7
    case org.apache.bcel.Constants.ICONST_4:
      wcet = 1;
      break;
    // ICONST_5 = 8
    case org.apache.bcel.Constants.ICONST_5:
      wcet = 1;
      break;
    // LCONST_0 = 9
    case org.apache.bcel.Constants.LCONST_0:
      wcet = 2;
      break;
    // LCONST_1 = 10
    case org.apache.bcel.Constants.LCONST_1:
      wcet = 2;
      break;
    // FCONST_0 = 11
    case org.apache.bcel.Constants.FCONST_0:
      wcet = -1;
      break;
    // FCONST_1 = 12
    case org.apache.bcel.Constants.FCONST_1:
      wcet = -1;
      break;
    // FCONST_2 = 13
    case org.apache.bcel.Constants.FCONST_2:
      wcet = -1;
      break;
    // DCONST_0 = 14
    case org.apache.bcel.Constants.DCONST_0:
      wcet = -1;
      break;
    // DCONST_1 = 15
    case org.apache.bcel.Constants.DCONST_1:
      wcet = -1;
      break;
    // BIPUSH = 16
    case org.apache.bcel.Constants.BIPUSH:
      wcet = 2;
      break;
    // SIPUSH = 17
    case org.apache.bcel.Constants.SIPUSH:
      wcet = 3;
      break;
    // LDC = 18
    case org.apache.bcel.Constants.LDC:
      wcet = 3 + r;
      break;
    // LDC_W = 19
    case org.apache.bcel.Constants.LDC_W:
      wcet = 4 + r;
      break;
    // LDC2_W = 20
    case org.apache.bcel.Constants.LDC2_W:
      wcet = 8 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // ILOAD = 21
    case org.apache.bcel.Constants.ILOAD:
      wcet = 2;
      break;
    // LLOAD = 22
    case org.apache.bcel.Constants.LLOAD:
      wcet = 11;
      break;
    // FLOAD = 23
    case org.apache.bcel.Constants.FLOAD:
      wcet = 2;
      break;
    // DLOAD = 24
    case org.apache.bcel.Constants.DLOAD:
      wcet = 11;
      break;
    // ALOAD = 25
    case org.apache.bcel.Constants.ALOAD:
      wcet = 2;
      break;
    // ILOAD_0 = 26
    case org.apache.bcel.Constants.ILOAD_0:
      wcet = 1;
      break;
    // ILOAD_1 = 27
    case org.apache.bcel.Constants.ILOAD_1:
      wcet = 1;
      break;
    // ILOAD_2 = 28
    case org.apache.bcel.Constants.ILOAD_2:
      wcet = 1;
      break;
    // ILOAD_3 = 29
    case org.apache.bcel.Constants.ILOAD_3:
      wcet = 1;
      break;
    // LLOAD_0 = 30
    case org.apache.bcel.Constants.LLOAD_0:
      wcet = 2;
      break;
    // LLOAD_1 = 31
    case org.apache.bcel.Constants.LLOAD_1:
      wcet = 2;
      break;
    // LLOAD_2 = 32
    case org.apache.bcel.Constants.LLOAD_2:
      wcet = 2;
      break;
    // LLOAD_3 = 33
    case org.apache.bcel.Constants.LLOAD_3:
      wcet = 11;
      break;
    // FLOAD_0 = 34
    case org.apache.bcel.Constants.FLOAD_0:
      wcet = 1;
      break;
    // FLOAD_1 = 35
    case org.apache.bcel.Constants.FLOAD_1:
      wcet = 1;
      break;
    // FLOAD_2 = 36
    case org.apache.bcel.Constants.FLOAD_2:
      wcet = 1;
      break;
    // FLOAD_3 = 37
    case org.apache.bcel.Constants.FLOAD_3:
      wcet = 1;
      break;
    // DLOAD_0 = 38
    case org.apache.bcel.Constants.DLOAD_0:
      wcet = 2;
      break;
    // DLOAD_1 = 39
    case org.apache.bcel.Constants.DLOAD_1:
      wcet = 2;
      break;
    // DLOAD_2 = 40
    case org.apache.bcel.Constants.DLOAD_2:
      wcet = 2;
      break;
    // DLOAD_3 = 41
    case org.apache.bcel.Constants.DLOAD_3:
      wcet = 11;
      break;
    // ALOAD_0 = 42
    case org.apache.bcel.Constants.ALOAD_0:
      wcet = 1;
      break;
    // ALOAD_1 = 43
    case org.apache.bcel.Constants.ALOAD_1:
      wcet = 1;
      break;
    // ALOAD_2 = 44
    case org.apache.bcel.Constants.ALOAD_2:
      wcet = 1;
      break;
    // ALOAD_3 = 45
    case org.apache.bcel.Constants.ALOAD_3:
      wcet = 1;
      break;
    // IALOAD = 46
    case org.apache.bcel.Constants.IALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // LALOAD = 47
    case org.apache.bcel.Constants.LALOAD:
      wcet = -1;
      break;
    // FALOAD = 48
    case org.apache.bcel.Constants.FALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // DALOAD = 49
    case org.apache.bcel.Constants.DALOAD:
      wcet = -1;
      break;
    // AALOAD = 50
    case org.apache.bcel.Constants.AALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // BALOAD = 51
    case org.apache.bcel.Constants.BALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // CALOAD = 52
    case org.apache.bcel.Constants.CALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // SALOAD = 53
    case org.apache.bcel.Constants.SALOAD:
      wcet = 19 + r;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      break;
    // ISTORE = 54
    case org.apache.bcel.Constants.ISTORE:
      wcet = 2;
      break;
    // LSTORE = 55
    case org.apache.bcel.Constants.LSTORE:
      wcet = 11;
      break;
    // FSTORE = 56
    case org.apache.bcel.Constants.FSTORE:
      wcet = 2;
      break;
    // DSTORE = 57
    case org.apache.bcel.Constants.DSTORE:
      wcet = 11;
      break;
    // ASTORE = 58
    case org.apache.bcel.Constants.ASTORE:
      wcet = 2;
      break;
    // ISTORE_0 = 59
    case org.apache.bcel.Constants.ISTORE_0:
      wcet = 1;
      break;
    // ISTORE_1 = 60
    case org.apache.bcel.Constants.ISTORE_1:
      wcet = 1;
      break;
    // ISTORE_2 = 61
    case org.apache.bcel.Constants.ISTORE_2:
      wcet = 1;
      break;
    // ISTORE_3 = 62
    case org.apache.bcel.Constants.ISTORE_3:
      wcet = 1;
      break;
    // LSTORE_0 = 63
    case org.apache.bcel.Constants.LSTORE_0:
      wcet = 2;
      break;
    // LSTORE_1 = 64
    case org.apache.bcel.Constants.LSTORE_1:
      wcet = 2;
      break;
    // LSTORE_2 = 65
    case org.apache.bcel.Constants.LSTORE_2:
      wcet = 2;
      break;
    // LSTORE_3 = 66
    case org.apache.bcel.Constants.LSTORE_3:
      wcet = 11;
      break;
    // FSTORE_0 = 67
    case org.apache.bcel.Constants.FSTORE_0:
      wcet = 1;
      break;
    // FSTORE_1 = 68
    case org.apache.bcel.Constants.FSTORE_1:
      wcet = 1;
      break;
    // FSTORE_2 = 69
    case org.apache.bcel.Constants.FSTORE_2:
      wcet = 1;
      break;
    // FSTORE_3 = 70
    case org.apache.bcel.Constants.FSTORE_3:
      wcet = 1;
      break;
    // DSTORE_0 = 71
    case org.apache.bcel.Constants.DSTORE_0:
      wcet = 2;
      break;
    // DSTORE_1 = 72
    case org.apache.bcel.Constants.DSTORE_1:
      wcet = 2;
      break;
    // DSTORE_2 = 73
    case org.apache.bcel.Constants.DSTORE_2:
      wcet = 2;
      break;
    // DSTORE_3 = 74
    case org.apache.bcel.Constants.DSTORE_3:
      wcet = 11;
      break;
    // ASTORE_0 = 75
    case org.apache.bcel.Constants.ASTORE_0:
      wcet = 1;
      break;
    // ASTORE_1 = 76
    case org.apache.bcel.Constants.ASTORE_1:
      wcet = 1;
      break;
    // ASTORE_2 = 77
    case org.apache.bcel.Constants.ASTORE_2:
      wcet = 1;
      break;
    // ASTORE_3 = 78
    case org.apache.bcel.Constants.ASTORE_3:
      wcet = 1;
      break;
    // IASTORE = 79
    case org.apache.bcel.Constants.IASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // LASTORE = 80
    case org.apache.bcel.Constants.LASTORE:
      wcet = -1;
      break;
    // FASTORE = 81
    case org.apache.bcel.Constants.FASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // DASTORE = 82
    case org.apache.bcel.Constants.DASTORE:
      wcet = -1;
      break;
    // AASTORE = 83
    case org.apache.bcel.Constants.AASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // BASTORE = 84
    case org.apache.bcel.Constants.BASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // CASTORE = 85
    case org.apache.bcel.Constants.CASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // SASTORE = 86
    case org.apache.bcel.Constants.SASTORE:
      wcet = 22;
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      wcet += w;
      break;
    // POP = 87
    case org.apache.bcel.Constants.POP:
      wcet = 1;
      break;
    // POP2 = 88
    case org.apache.bcel.Constants.POP2:
      wcet = 2;
      break;
    // DUP = 89
    case org.apache.bcel.Constants.DUP:
      wcet = 1;
      break;
    // DUP_X1 = 90
    case org.apache.bcel.Constants.DUP_X1:
      wcet = 5;
      break;
    // DUP_X2 = 91
    case org.apache.bcel.Constants.DUP_X2:
      wcet = -1;
      break;
    // DUP2 = 92
    case org.apache.bcel.Constants.DUP2:
      wcet = 6;
      break;
    // DUP2_X1 = 93
    case org.apache.bcel.Constants.DUP2_X1:
      wcet = -1;
      break;
    // DUP2_X2 = 94
    case org.apache.bcel.Constants.DUP2_X2:
      wcet = -1;
      break;
    // SWAP = 95
    case org.apache.bcel.Constants.SWAP:
      wcet = -1;
      break;
    // IADD = 96
    case org.apache.bcel.Constants.IADD:
      wcet = 1;
      break;
    // LADD = 97
    case org.apache.bcel.Constants.LADD:
      wcet = -1;
      break;
    // FADD = 98
    case org.apache.bcel.Constants.FADD:
      wcet = -1;
      break;
    // DADD = 99
    case org.apache.bcel.Constants.DADD:
      wcet = -1;
      break;
    // ISUB = 100
    case org.apache.bcel.Constants.ISUB:
      wcet = 1;
      break;
    // LSUB = 101
    case org.apache.bcel.Constants.LSUB:
      wcet = -1;
      break;
    // FSUB = 102
    case org.apache.bcel.Constants.FSUB:
      wcet = -1;
      break;
    // DSUB = 103
    case org.apache.bcel.Constants.DSUB:
      wcet = -1;
      break;
    // IMUL = 104
    case org.apache.bcel.Constants.IMUL:
      wcet = 35;
      break;
    // LMUL = 105
    case org.apache.bcel.Constants.LMUL:
      wcet = -1;
      break;
    // FMUL = 106
    case org.apache.bcel.Constants.FMUL:
      wcet = -1;
      break;
    // DMUL = 107
    case org.apache.bcel.Constants.DMUL:
      wcet = -1;
      break;
    // IDIV = 108
    case org.apache.bcel.Constants.IDIV:
      wcet = -1;
      break;
    // LDIV = 109
    case org.apache.bcel.Constants.LDIV:
      wcet = -1;
      break;
    // FDIV = 110
    case org.apache.bcel.Constants.FDIV:
      wcet = -1;
      break;
    // DDIV = 111
    case org.apache.bcel.Constants.DDIV:
      wcet = -1;
      break;
    // IREM = 112
    case org.apache.bcel.Constants.IREM:
      wcet = -1;
      break;
    // LREM = 113
    case org.apache.bcel.Constants.LREM:
      wcet = -1;
      break;
    // FREM = 114
    case org.apache.bcel.Constants.FREM:
      wcet = -1;
      break;
    // DREM = 115
    case org.apache.bcel.Constants.DREM:
      wcet = -1;
      break;
    // INEG = 116
    case org.apache.bcel.Constants.INEG:
      wcet = 4;
      break;
    // LNEG = 117
    case org.apache.bcel.Constants.LNEG:
      wcet = -1;
      break;
    // FNEG = 118
    case org.apache.bcel.Constants.FNEG:
      wcet = -1;
      break;
    // DNEG = 119
    case org.apache.bcel.Constants.DNEG:
      wcet = -1;
      break;
    // ISHL = 120
    case org.apache.bcel.Constants.ISHL:
      wcet = 1;
      break;
    // LSHL = 121
    case org.apache.bcel.Constants.LSHL:
      wcet = -1;
      break;
    // ISHR = 122
    case org.apache.bcel.Constants.ISHR:
      wcet = 1;
      break;
    // LSHR = 123
    case org.apache.bcel.Constants.LSHR:
      wcet = -1;
      break;
    // IUSHR = 124
    case org.apache.bcel.Constants.IUSHR:
      wcet = 1;
      break;
    // LUSHR = 125
    case org.apache.bcel.Constants.LUSHR:
      wcet = -1;
      break;
    // IAND = 126
    case org.apache.bcel.Constants.IAND:
      wcet = 1;
      break;
    // LAND = 127
    case org.apache.bcel.Constants.LAND:
      wcet = -1;
      break;
    // IOR = 128
    case org.apache.bcel.Constants.IOR:
      wcet = 1;
      break;
    // LOR = 129
    case org.apache.bcel.Constants.LOR:
      wcet = -1;
      break;
    // IXOR = 130
    case org.apache.bcel.Constants.IXOR:
      wcet = 1;
      break;
    // LXOR = 131
    case org.apache.bcel.Constants.LXOR:
      wcet = -1;
      break;
    // IINC = 132
    case org.apache.bcel.Constants.IINC:
      wcet = 11;
      break;
    // I2L = 133
    case org.apache.bcel.Constants.I2L:
      wcet = -1;
      break;
    // I2F = 134
    case org.apache.bcel.Constants.I2F:
      wcet = -1;
      break;
    // I2D = 135
    case org.apache.bcel.Constants.I2D:
      wcet = -1;
      break;
    // L2I = 136
    case org.apache.bcel.Constants.L2I:
      wcet = 3;
      break;
    // L2F = 137
    case org.apache.bcel.Constants.L2F:
      wcet = -1;
      break;
    // L2D = 138
    case org.apache.bcel.Constants.L2D:
      wcet = -1;
      break;
    // F2I = 139
    case org.apache.bcel.Constants.F2I:
      wcet = -1;
      break;
    // F2L = 140
    case org.apache.bcel.Constants.F2L:
      wcet = -1;
      break;
    // F2D = 141
    case org.apache.bcel.Constants.F2D:
      wcet = -1;
      break;
    // D2I = 142
    case org.apache.bcel.Constants.D2I:
      wcet = -1;
      break;
    // D2L = 143
    case org.apache.bcel.Constants.D2L:
      wcet = -1;
      break;
    // D2F = 144
    case org.apache.bcel.Constants.D2F:
      wcet = -1;
      break;
    // I2B = 145
    case org.apache.bcel.Constants.I2B:
      wcet = -1;
      break;
    // INT2BYTE = 145 // Old notion
    // case org.apache.bcel.Constants.INT2BYTE : wcet = -1; break;
    // I2C = 146
    case org.apache.bcel.Constants.I2C:
      wcet = 2;
      break;
    // INT2CHAR = 146 // Old notion
    // case org.apache.bcel.Constants.INT2CHAR : wcet = -1; break;
    // I2S = 147
    case org.apache.bcel.Constants.I2S:
      wcet = -1;
      break;
    // INT2SHORT = 147 // Old notion
    // case org.apache.bcel.Constants.INT2SHORT : wcet = -1; break;
    // LCMP = 148
    case org.apache.bcel.Constants.LCMP:
      wcet = -1;
      break;
    // FCMPL = 149
    case org.apache.bcel.Constants.FCMPL:
      wcet = -1;
      break;
    // FCMPG = 150
    case org.apache.bcel.Constants.FCMPG:
      wcet = -1;
      break;
    // DCMPL = 151
    case org.apache.bcel.Constants.DCMPL:
      wcet = -1;
      break;
    // DCMPG = 152
    case org.apache.bcel.Constants.DCMPG:
      wcet = -1;
      break;
    // IFEQ = 153
    case org.apache.bcel.Constants.IFEQ:
      wcet = 4;
      break;
    // IFNE = 154
    case org.apache.bcel.Constants.IFNE:
      wcet = 4;
      break;
    // IFLT = 155
    case org.apache.bcel.Constants.IFLT:
      wcet = 4;
      break;
    // IFGE = 156
    case org.apache.bcel.Constants.IFGE:
      wcet = 4;
      break;
    // IFGT = 157
    case org.apache.bcel.Constants.IFGT:
      wcet = 4;
      break;
    // IFLE = 158
    case org.apache.bcel.Constants.IFLE:
      wcet = 4;
      break;
    // IF_ICMPEQ = 159
    case org.apache.bcel.Constants.IF_ICMPEQ:
      wcet = 4;
      break;
    // IF_ICMPNE = 160
    case org.apache.bcel.Constants.IF_ICMPNE:
      wcet = 4;
      break;
    // IF_ICMPLT = 161
    case org.apache.bcel.Constants.IF_ICMPLT:
      wcet = 4;
      break;
    // IF_ICMPGE = 162
    case org.apache.bcel.Constants.IF_ICMPGE:
      wcet = 4;
      break;
    // IF_ICMPGT = 163
    case org.apache.bcel.Constants.IF_ICMPGT:
      wcet = 4;
      break;
    // IF_ICMPLE = 164
    case org.apache.bcel.Constants.IF_ICMPLE:
      wcet = 4;
      break;
    // IF_ACMPEQ = 165
    case org.apache.bcel.Constants.IF_ACMPEQ:
      wcet = 4;
      break;
    // IF_ACMPNE = 166
    case org.apache.bcel.Constants.IF_ACMPNE:
      wcet = 4;
      break;
    // GOTO = 167
    case org.apache.bcel.Constants.GOTO:
      wcet = 4;
      break;
    // JSR = 168
    case org.apache.bcel.Constants.JSR:
      wcet = -1;
      break;
    // RET = 169
    case org.apache.bcel.Constants.RET:
      wcet = -1; // TODO: Should this be 1?
      break;
    // TABLESWITCH = 170
    case org.apache.bcel.Constants.TABLESWITCH:
      wcet = -1;
      break;
    // LOOKUPSWITCH = 171
    case org.apache.bcel.Constants.LOOKUPSWITCH:
      wcet = -1;
      break;
    // IRETURN = 172
    case org.apache.bcel.Constants.IRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 8) {
        wcet += b - 8;
      } else {
        wcet += 0;
      }
      break;
    // LRETURN = 173
    case org.apache.bcel.Constants.LRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 9) {
        wcet += b - 9;
      } else {
        wcet += 0;
      }
      break;
    // FRETURN = 174
    case org.apache.bcel.Constants.FRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 8) {
        wcet += b - 8;
      } else {
        wcet += 0;
      }
      break;
    // DRETURN = 175
    case org.apache.bcel.Constants.DRETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 9) {
        wcet += b - 9;
      } else {
        wcet += 0;
      }
      break;
    // ARETURN = 176
    case org.apache.bcel.Constants.ARETURN:
      wcet = 15;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 8) {
        wcet += b - 8;
      } else {
        wcet += 0;
      }
      break;
    // RETURN = 177
      //TODO: Check with ms why it is 1 in the DATE paper
    case org.apache.bcel.Constants.RETURN:
      wcet = 13;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (b >= 7) {
        wcet += b - 7;
      } else {
        wcet += 0;
      }
      break;
    // GETSTATIC = 178
    case org.apache.bcel.Constants.GETSTATIC:
      wcet = 4 + 2 * r;
      break;
    // PUTSTATIC = 179
    case org.apache.bcel.Constants.PUTSTATIC:
      wcet = 5 + r + w;
      break;
    // GETFIELD = 180
    case org.apache.bcel.Constants.GETFIELD:
      wcet = 10 + 2 * r;
      break;
    // PUTFIELD = 181
    case org.apache.bcel.Constants.PUTFIELD:
      wcet = 13 + r + w;
      break;
    // INVOKEVIRTUAL = 182
    case org.apache.bcel.Constants.INVOKEVIRTUAL:
      wcet = 78 + 2 * r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // INVOKESPECIAL = 183
    case org.apache.bcel.Constants.INVOKESPECIAL:
      wcet = 58 + r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // INVOKENONVIRTUAL = 183
    // case org.apache.bcel.Constants.INVOKENONVIRTUAL : wcet = -1; break;
    // INVOKESTATIC = 184
    case org.apache.bcel.Constants.INVOKESTATIC:
      wcet = 58 + r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // INVOKEINTERFACE = 185
    case org.apache.bcel.Constants.INVOKEINTERFACE:
      wcet = 84 + 4 * r;
      if (r >= 7) {
        wcet += r - 3;
      } else {
        wcet += 4;
      }
      if (r >= 6) {
        wcet += r - 2;
      } else {
        wcet += 4;
      }
      if (b >= 39) {
        wcet += b - 39;
      } else {
        wcet += 0;
      }
      break;
    // NEW = 187
    case org.apache.bcel.Constants.NEW:
      wcet = -1;
      break;
    // NEWARRAY = 188
    case org.apache.bcel.Constants.NEWARRAY:
      wcet = 12 + w; // TODO: Time to clear array not included
      break;
    // ANEWARRAY = 189
    case org.apache.bcel.Constants.ANEWARRAY:
      wcet = -1;
      break;
    // ARRAYLENGTH = 190
    case org.apache.bcel.Constants.ARRAYLENGTH:
      wcet = 2 + r;
      break;
    // ATHROW = 191
    case org.apache.bcel.Constants.ATHROW:
      wcet = -1;
      break;
    // CHECKCAST = 192
    case org.apache.bcel.Constants.CHECKCAST:
      wcet = -1;
      break;
    // INSTANCEOF = 193
    case org.apache.bcel.Constants.INSTANCEOF:
      wcet = -1;
      break;
    // MONITORENTER = 194
    case org.apache.bcel.Constants.MONITORENTER:
      wcet = 9;
      break;
    // MONITOREXIT = 195
    case org.apache.bcel.Constants.MONITOREXIT:
      wcet = 10;
      wcet = 11; // TODO: Which one to keep?
      break;
    // WIDE = 196
    case org.apache.bcel.Constants.WIDE:
      wcet = -1;
      break;
    // MULTIANEWARRAY = 197
    case org.apache.bcel.Constants.MULTIANEWARRAY:
      wcet = -1;
      break;
    // IFNULL = 198
    case org.apache.bcel.Constants.IFNULL:
      wcet = 4;
      break;
    // IFNONNULL = 199
    case org.apache.bcel.Constants.IFNONNULL:
      wcet = 4;
      break;
    // GOTO_W = 200
    case org.apache.bcel.Constants.GOTO_W:
      wcet = -1;
      break;
    // JSR_W = 201
    case org.apache.bcel.Constants.JSR_W:
      wcet = -1;
      break;
    }
    // TODO: Add the JOP speciffic codes?
    return wcet;
  }

  /**
   * Check to see if there is a valid WCET count for the instruction.
   * 
   * @param opcode
   * @return true if there is a valid wcet value
   */
  boolean wcetAvailable(int opcode) {
    if (getWCET(opcode) == WCETNOTAVAILABLE)
      return false;
    else
      return true;
  }

  /**
   * Method load time on invoke or return if there is a cache miss (see pMiss).
   * 
   * @see ms thesis p 232
   */
  public static void calculateB() {
    if (n == -1) {
      System.err.println("n not set!");
      System.exit(-1);
    } else {
      b = 2 + (n + 1) * a;
    }
  }

  public static int getB() {
    return b;
  }

  public static void setB(int b) {
    WCETInstruction.b = b;
  }

  public static double getPmiss() {
    return pmiss;
  }

  public static void setPmiss(double pmiss) {
    WCETInstruction.pmiss = pmiss;
  }

  public static int getN() {
    return n;
  }

  public static void setN(int n) {
    WCETInstruction.n = n;
  }

}

// BCEL overrides

/**
 * Extends org.apache.bcel.verifier.structurals. Frame just to get access to the
 * _this field, which the the operandWalker method in MethodInfo uses.
 */
class FrameFrame2 extends Frame {

  public FrameFrame2(int maxLocals, int maxStack) {
    super(maxLocals, maxStack);
  }

  public FrameFrame2(LocalVariables locals, OperandStack stack) {
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
// TODO: Can this extension be avoided (ie. why did BCEL not like the
// overloaded Dbg field).
class AnInstConstraintVisitor2 extends InstConstraintVisitor {
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
