package com.jopdesign.build;

import java.util.*;
import java.io.PrintWriter;

import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.Constants;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.exc.*;
import org.apache.bcel.verifier.structurals.*;
import org.apache.bcel.Repository;
import org.apache.bcel.verifier.structurals.Frame;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;
import org.apache.bcel.verifier.structurals.OperandStack;
import org.apache.bcel.verifier.structurals.LocalVariables;


/**
 * @author rup, ms
 */
public class GCRTMethodInfo {
// static
  static int WORDLEN = 32;
	
	static HashMap miMap = new HashMap();
	
	public static void stackWalker(MethodInfo mi){
		((GCRTMethodInfo)miMap.get(mi)).stackWalker();
	}
	
	public static int gcLength(MethodInfo mi){
		return ((GCRTMethodInfo)miMap.get(mi)).gcLength();
	}
	
	public static void dumpMethodGcis(MethodInfo mi, PrintWriter out) {
		((GCRTMethodInfo)miMap.get(mi)).dumpMethodGcis(out);
	}		

//instance
  MethodInfo mi;
  Method method;
  int length;

  // Operand map for a given PC
	int []ogci; 	
	// Local variables map for a given PC
	int []mgci; 	
	// Instruction count
	int instCnt;

	int mstack, margs, mreallocals, len;

	public GCRTMethodInfo(MethodInfo mi, Method method) {
		this.mi = mi;
		this.method = method;

		mgci = new int[0];
		ogci = new int[0];
		instCnt = 0;
		length = 0;
		if(miMap.containsKey(mi)){
		  System.err.println("Alredy added mi.");
		  System.exit(-1);
		} else{
		  miMap.put(mi, this);
		}
		
	}
	
	/**
	 * It walks the stack frame at compile time. The locals and the operands are mapped to 
	 * to bit maps. These bit maps are used at run time to identify an exact root
	 * set which are used in conjunction with a real-time garbage collector scheduler.
	 */
	private void stackWalker(){
//System.out.println(".....................");	  
//System.out.println("stackWalker");
	  margs = mstack = mreallocals = len = instCnt = 0;
//System.out.println("Class method:"+mi.cli.clazz.getClassName()+"."+mi.methodId);
    
    Type at[] = method.getArgumentTypes();
    for (int i=0; i<at.length; ++i) {
      margs += at[i].getSize();
    }
    
    if(!method.isStatic()) {
			margs++;
		}
    
    if (!method.isAbstract()){
		  mstack = method.getCode().getMaxStack();
		  mreallocals = method.getCode().getMaxLocals() - margs;
    	instCnt = (method.getCode().getCode()).length;
   	  operandWalker();
		} 
		//System.out.println(" *** "+mi.cli.clazz.getClassName()+"."+mi.methodId+" --> mreallocals ="+mreallocals+" margs ="+margs+" mstack ="+mstack);
		
	}
	
	/**
	 * Collect type information on the operands for the stack frame. The code is
	 * and then the execution context of each instructions is used to map the 
	 * type info of each entry on the stack. We map a reference entry with a 1 
	 * and a non-reference entry with a 0. 
	 
	 * The simulation part is based of the JustIce Pass3bVerifer.
	 */
	private void operandWalker(){
//System.out.println("operandWalker");	

		JavaClass jc = mi.cli.clazz;

		ConstantPoolGen cpg = new ConstantPoolGen(jc.getConstantPool());
		
		// Some methods overridden (see bottom of this file)
		InstConstraintVisitor icv = new AnInstConstraintVisitor();
		
		icv.setConstantPoolGen(cpg);
		
		ExecutionVisitor ev = new ExecutionVisitor();
		ev.setConstantPoolGen(cpg);
		
		MethodGen mg = new MethodGen(method, jc.getClassName(), cpg);
		
		icv.setMethodGen(mg);
		
		if (! (mg.isAbstract() || mg.isNative()) ){ // IF mg HAS CODE (See pass 2)
				
  		ControlFlowGraph cfg = new ControlFlowGraph(mg);
  		
  		// InstructionContext as key for inFrame
  		HashMap inFrames = new HashMap();
  
  		// Build the initial frame situation for this method.
  		FrameFrame fStart = new FrameFrame(mg.getMaxLocals(),mg.getMaxStack());
  		if ( !mg.isStatic() ){
  			if (mg.getName().equals(Constants.CONSTRUCTOR_NAME)){
  				fStart.setThis(new UninitializedObjectType(new ObjectType(jc.getClassName())));
  				fStart.getLocals().set(0, fStart.getThis());
  			}
  			else{
  				fStart.setThis(null);
  				fStart.getLocals().set(0, new ObjectType(jc.getClassName()));
  			}
  		}
  		Type[] argtypes = mg.getArgumentTypes();
  		int twoslotoffset = 0;
  		for (int j=0; j<argtypes.length; j++){
  			if (argtypes[j] == Type.SHORT || argtypes[j] == Type.BYTE || argtypes[j] == Type.CHAR || argtypes[j] == Type.BOOLEAN){
  				argtypes[j] = Type.INT;
  			}
  			fStart.getLocals().set(twoslotoffset + j + (mg.isStatic()?0:1), argtypes[j]);
  			if (argtypes[j].getSize() == 2){
  				twoslotoffset++;
  				fStart.getLocals().set(twoslotoffset + j + (mg.isStatic()?0:1), Type.UNKNOWN);
  			}
  		}
  						
      InstructionContext start = cfg.contextOf(mg.getInstructionList().getStart());
      inFrames.put(start,fStart);
      start.execute(fStart, new ArrayList(), icv, ev);
  		
  		Vector ics = new Vector(); // Type: InstructionContext
      Vector ecs = new Vector(); // Type: ArrayList (of InstructionContext)
  		
  		ics.add(start);
  		ecs.add(new ArrayList());
  				
  		// LOOP!
  		while (!ics.isEmpty()){
  			InstructionContext u;
  			ArrayList ec;
  		  u  = (InstructionContext) ics.get(0);
  			ec = (ArrayList) ecs.get(0);
  			ics.remove(0);
  			ecs.remove(0);
  			ArrayList oldchain = (ArrayList) (ec.clone());
  			ArrayList newchain = (ArrayList) (ec.clone());
  			newchain.add(u);
  
  			if ((u.getInstruction().getInstruction()) instanceof RET){
   				// We can only follow _one_ successor, the one after the
  				// JSR that was recently executed.
  				RET ret = (RET) (u.getInstruction().getInstruction());
  				ReturnaddressType t = (ReturnaddressType) u.getOutFrame(oldchain).getLocals().get(ret.getIndex());
  				InstructionContext theSuccessor = cfg.contextOf(t.getTarget());
  
  				// Sanity check
  				InstructionContext lastJSR = null;
  				int skip_jsr = 0;
  				for (int ss=oldchain.size()-1; ss >= 0; ss--){
  					if (skip_jsr < 0){
  						throw new AssertionViolatedException("More RET than JSR in execution chain?!");
  					}
    					if (((InstructionContext) oldchain.get(ss)).getInstruction().getInstruction() instanceof JsrInstruction){
  						if (skip_jsr == 0){
  							lastJSR = (InstructionContext) oldchain.get(ss);
  							break;
  						}
  						else{
  							skip_jsr--;
  						}
  					}
  					if (((InstructionContext) oldchain.get(ss)).getInstruction().getInstruction() instanceof RET){
  						skip_jsr++;
  					}
  				}
  				if (lastJSR == null){
  					throw new AssertionViolatedException("RET without a JSR before in ExecutionChain?! EC: '"+oldchain+"'.");
  				}
  				JsrInstruction jsr = (JsrInstruction) (lastJSR.getInstruction().getInstruction());
  				if ( theSuccessor != (cfg.contextOf(jsr.physicalSuccessor())) ){
  					throw new AssertionViolatedException("RET '"+u.getInstruction()+"' info inconsistent: jump back to '"+theSuccessor+"' or '"+cfg.contextOf(jsr.physicalSuccessor())+"'?");
  				}
  
  				inFrames.put(theSuccessor,u.getOutFrame(oldchain));
  				if (theSuccessor.execute(u.getOutFrame(oldchain), newchain, icv, ev)){
  					//icq.add(theSuccessor, (ArrayList) newchain.clone());
  					ics.add(theSuccessor);
  					ecs.add((ArrayList) newchain.clone());
  				}
  
  			}
  			else{// "not a ret"
  				// Normal successors. Add them to the queue of successors.
  				InstructionContext[] succs = u.getSuccessors();
  				for (int s=0; s<succs.length; s++){
  					InstructionContext v = succs[s];
  					inFrames.put(v,u.getOutFrame(oldchain));
  					if (v.execute(u.getOutFrame(oldchain), newchain, icv, ev)){
  						ics.add(v);
  						ecs.add((ArrayList) newchain.clone());
  					}
  				}
  			}// end "not a ret"
  
  			// Exception Handlers. Add them to the queue of successors.
  			// [subroutines are never protected; mandated by JustIce]
  			ExceptionHandler[] exc_hds = u.getExceptionHandlers();
  			for (int s=0; s<exc_hds.length; s++){
  				InstructionContext v = cfg.contextOf(exc_hds[s].getHandlerStart());
          Frame f = new Frame(u.getOutFrame(oldchain).getLocals(), new OperandStack (u.getOutFrame(oldchain).getStack().maxStack(), (exc_hds[s].getExceptionType()==null? Type.THROWABLE : exc_hds[s].getExceptionType())) );
          inFrames.put(v,f);
  				if (v.execute(f, new ArrayList(), icv, ev)){
  					ics.add(v);
  					ecs.add(new ArrayList());
  				}
  			}
  		}// while (!ics.isEmpty()) END
  		
  		// To later get the PC positions of the instructions
  		mg.getInstructionList().setPositions(true);
  		InstructionHandle ih = start.getInstruction();
  		
//int inFramesSize = inFrames.size();
//System.out.println("numInstructions:"+instCnt+" inFramesSize:"+inFramesSize);

  		ogci = new int[instCnt];
  		mgci = new int[instCnt];
  		int oldPC = 0;
  		int PC = 0;
  		int icnt = 0;
  		// for debug info
  		StringBuffer ogciStr = new StringBuffer();
  		
  		ih = start.getInstruction();
  		do{
  			oldPC = PC;
  			PC = ih.getPosition();
  			// Pad up the operands with the same ref bits when the PC gets ahead
  			// it will either save time if a linked list is used or not be used for anything
  			// at run time as the PC will not point to the padded positions.
  			for(int i=oldPC+1;i<PC;i++){
//System.out.println("Padding up: ogci["+i+"]=ogci["+oldPC+"]");
  			  ogci[i] = ogci[oldPC];
  			  mgci[i] = mgci[oldPC];
  			}
        ogciStr.append("ih:"+ih.toString());
  			InstructionContext ic = cfg.contextOf(ih);
  			// It is here the incoming frame is used to achieve the desired PC->operand mapping
  			Frame f1 = (Frame) inFrames.get(ic);
  			LocalVariables lvs = f1.getLocals();
  			for (int i=0; i<lvs.maxLocals(); i++){
  				if (lvs.get(i) instanceof	ReferenceType){
					  int	ref=(1<<i);
					  mgci[PC] |=	ref;					
				  }
  				if (lvs.get(i) instanceof UninitializedObjectType){
  					//this.addMessage("Warning: ReturnInstruction '"+ic+"' may leave method with an uninitialized object in the local variables array '"+lvs+"'.");
  				}
  			}
  			OperandStack os = f1.getStack();
//System.out.println("-->ih["+PC+"]: line"+ih.toString()+" icnt:"+icnt);
//System.out.println("  pre");
//System.out.println("    os slots:"+os.slotsUsed());
        ogciStr.append(",os slots:"+os.slotsUsed());
  			// Each slot is 32 bits				
  			int j = 0; //Used to offset for LONG and DOUBLE

				for	(int i=0;	i<os.slotsUsed();	i++,j++){
//System.out.println("    op["+i+"]:"+(os.peek(j)).getSignature());
					ogciStr.append(",operand("+i+"):"+(os.peek(j)).getSignature());
					if (os.peek(j) instanceof	UninitializedObjectType){
						//System.out.println("Warning: ReturnInstruction '"+ic+"'	may	leave	method with	an uninitialized object	on the operand stack '"+os+"'.");
					}
					if (os.peek(j) instanceof	ReferenceType){
						int	ref=(1<<i);
						ogci[PC] |=	ref;					
					}
					// peek()	is per type
					if (os.peek(j).getSignature().equals("J")	|| os.peek(j).getSignature().equals("D")){
						j--;
					}
				}
//System.out.println("    ogci["+PC+"]:"+bitStr(ogci[PC]));
				
				// This frame is post (after) the instruction and thus not what we need
				Frame f2 = ic.getOutFrame(new ArrayList());
  			LocalVariables lvs2 = f2.getLocals();
  			int ogci2 = 0;
  			for (int i=0; i<lvs2.maxLocals(); i++){
  				if (lvs2.get(i) instanceof UninitializedObjectType){
  					//System.out.println("Warning: ReturnInstruction '"+ic+"' may leave method with an uninitialized object in the local variables array '"+lvs+"'.");
  				}
  			}
  			OperandStack os2 = f2.getStack();
  			//System.out.println("  post");
  			//System.out.println("    os slots:"+os2.slotsUsed());
  			// Each slot is 32 bits				
  			j = 0; //Used to offset for LONG and DOUBLE

				for	(int i=0;	i<os2.slotsUsed();	i++,j++){
//System.out.println("    op["+i+"]:"+(os2.peek(j)).getSignature());
					//ogciStr.append(",operand("+i+"):"+(os2.peek(j)).getSignature());
					if (os2.peek(j) instanceof	UninitializedObjectType){
						//System.out.println("Warning: ReturnInstruction '"+ic+"'	may	leave	method with	an uninitialized object	on the operand stack '"+os+"'.");
					}
					if (os2.peek(j) instanceof	ReferenceType){
						int	ref=(1<<i);
						ogci2 |=	ref;					
					}
					// peek()	is per type
					if (os2.peek(j).getSignature().equals("J")	|| os2.peek(j).getSignature().equals("D")){
						j--;
					}
				}

//System.out.println("    ogci["+PC+"]:"+bitStr(ogci2));
        
        icnt++;
		    //System.out.println("");  
  		}while ((ih = ih.getNext()) != null);
 		
 		  // pad up the end
 		  for(int i=(PC+1);i<instCnt;i++){
//System.out.println("Post padding up: ogci["+i+"]=ogci["+PC+"]");
 		    ogci[i]=ogci[PC];
 		    mgci[i]=mgci[PC];
 		  }
 		  
 		  // Good for debugging
 		  for(int i=0;i<instCnt;i++){
 		  	//System.out.println("ogci["+i+"]"+bitStr(ogci[i]));
 		  	//System.out.println("mgci["+i+"]"+bitStr(mgci[i]));
 		  }
 		  //System.out.println("--");
 		  

   	  int varcnt = mstack+mreallocals+margs;
  	  int pos = instCnt*varcnt;
  	  length = pos / WORDLEN; // whole words
  	  if((pos % WORDLEN)>0){
  	    length++;  // partly used word
  	  }
  	  
  	  length++; // gcpack
  	  
		} // if has code
	} 
	
	/*
	 * It dumps the local variable and stack operands GC info structures.
   */
	public void dumpMethodGcis(PrintWriter out) {		
    int GCIHEADERLENGTH = 1; //key, gcpack
    
    // gcpack
    int INSTRLEN = 10; //1024 instructions
    int MAXSTACKLEN = 5; // max 31 operands
    int MAXLOCALSLEN = 5; // max 31 args+locals
    int LOCALMARKLEN = 1; // 1 if local references
    int STACKMARKLEN = 1; // 1 if stack references
    
    int cntMgciWords = 0;
    
    if(mi.code != null){ // not abstract
  //out.println("\t//\tStackwalker garbage info word for method "+cli.clazz.getClassName()+"."+methodId);
  //out.println("\t//\targs size:"+margs+" locals:"+mreallocals+" mstack:"+mstack);
  //out.println("\t//\tpre, cntMgciWords:"+cntMgciWords+" cntMgci:"+cntMgci);
      if(instCnt!=mgci.length || instCnt!=ogci.length)
      {
  	    System.err.println("exit: instCnt!=mgci.length || instCnt!=ogci.length");
  	    System.exit(-1);
  	  }
    	
      out.println("\t\t//\t"+mi.codeAddress+": stackwalker info for "+mi.cli.clazz.getClassName()+"."+mi.methodId);
  
  	  int gcpack = instCnt;
  	  gcpack = (gcpack<<MAXSTACKLEN)|mstack;
  	  gcpack = (gcpack<<MAXLOCALSLEN)|(mreallocals+margs);
  	  
  	  int localmark = 0;
  	  if((mreallocals+margs)>0){
  	    localmark = 1;
  	  }
  	  gcpack = (gcpack<<LOCALMARKLEN)|(localmark);
  	  
  	  int stackmark = 0;
  	  if(mstack>0){
  	    stackmark = 1;
  	  }
  	  gcpack = (gcpack<<STACKMARKLEN)|(stackmark);
  
      StringBuffer sb = new StringBuffer();
  
      int mask = 0x01;
      int WORDLEN = 32;
      int[] bitMap = new int[2*instCnt];
      StringBuffer[] bitInfo = new StringBuffer[2*instCnt];
      for(int i=0;i<instCnt;i++){
      	bitInfo[i] = new StringBuffer();
      	bitInfo[i].append("[bit,pos,index,offset,mgci or ogci]:");
      }
      
      int pos = 0;
      int bit = 0;
      int index = 0;
      int offset = 0;
      
      for(int i=0;i<instCnt;i++){
        for(int j=0;j<(mreallocals+margs);j++){
          bit = (mgci[i]>>>j)&mask;
          index = pos/WORDLEN;
          offset = pos % WORDLEN;
          bit = bit<<offset;
          bitMap[index] |= bit;
          bitInfo[index].append("["+bit+","+pos+","+index+","+offset+","+"mgci["+i+"]["+j+"]] ");
          pos++;
        }
        for(int j=0;j<mstack;j++){
          bit = (ogci[i]>>>j)&mask;
          index = pos/WORDLEN;
          offset = pos % WORDLEN;
          bit = bit<<offset;
          bitMap[index] |= bit;
          bitInfo[index].append("["+bit+","+pos+","+index+","+offset+","+"ogci["+i+"]["+j+"]] ");
          pos++;
        }
      }
//  System.out.println("dumpMethodGcis Class method:"+mi.cli.clazz.getClassName()+"."+mi.methodId);
//  System.out.println("index="+index+" bitMap.length:"+bitMap.length+" instCnt:"+instCnt+" pos:"+pos);
      if(instCnt>0 && (localmark==1 || stackmark==1)){
        for(int i=0;i<=index;i++){
          //out.println("\t\t//\tbitMap["+i+"]:"+bitStr(bitMap[i])+" "+bitInfo[i].toString());
     		  out.println("\t\t"+bitMap[i]+",//\tbitMap["+i+"]:"+bitStr(bitMap[i]));
          cntMgciWords++;
        }
      }
      
      cntMgciWords += GCIHEADERLENGTH;
      out.println("\t\t"+gcpack+",//\tgcpack. instrCnt:"+instCnt+" mstack:"+mstack+" (mreallocals+margs):"+(mreallocals+margs)+" localmark:"+localmark+" stackmark:"+stackmark+" gcpack:"+bitStr(gcpack));
      
      //Check lengths
      if(cntMgciWords!=gcLength()){
      	System.err.println("Length mismatch. cntMgciWords:"+cntMgciWords+" gcLength():"+gcLength());
      	System.exit(-1);
      }
    } // if not abstract
//System.err.println("dumpMethodGcis method:"+cli.clazz.getClassName()+"."+methodId);
//System.err.println("out MethodInfo.cntMgci:"+MethodInfo.cntMgci+" MethodInfo.cntMgciWords:"+MethodInfo.cntMgciWords); 				
//System.err.println("index="+index+" bitMap.length:"+bitMap.length+" instCnt:"+instCnt+" mreallocals:"+mreallocals+" margs:"+margs +" mstack:"+mstack+" pos:"+pos);
//System.err.println("method.isAbstract():"+method.isAbstract());
//System.err.println("...");
	}
	
	int gcLength(){
		return length;
  }

	/**
	 * Make a word into a 0/1 bit string.
	 */
	String bitStr(int word){
	  //make ogci[PC] to bit string for debugging
	  StringBuffer sb = new StringBuffer();
	  int mask = 0x01;
	  for(int i = 31;i>=0;i--){
		  int res = (word>>>i) & mask;
		  if((i+1)%8==0 && i<31){
			  sb.append("_");
			}
		  sb.append(res);
	  }
	  return sb.toString();
  }
}

/**
 * Extends org.apache.bcel.verifier.structurals.Frame just to get access
 * to the _this field, which the the operandWalker method in MethodInfo 
 * uses.
 *
 * @author rup, ms
 */
class FrameFrame extends Frame{
	
  public FrameFrame(int maxLocals, int maxStack){
    super(maxLocals, maxStack);
	}

	public FrameFrame(LocalVariables locals, OperandStack stack){
		super(locals, stack);
	}
	
	void setThis(UninitializedObjectType uot){
    _this = uot;
  }
   
  UninitializedObjectType getThis(){
    return _this;
  }
}

/**
 * BCEL throws an exception for the util.Dbg class because it overloads a field. 
 * The choice (as described in the BCEL method comment for around line 2551 in
 * org.apache.bcel.verifier.structurals.InstConstraintVisitor) is to comment out
 * this check and recompile BCEL jar. 
 * Insted of recompiling BCEL the choice is to override the methods, which is ok?
 * as we are not using BCEL for bytecode verification purposes (using Sun Javac).
 */
class AnInstConstraintVisitor extends InstConstraintVisitor{
  public void visitAALOAD(AALOAD o) {}
  public void visitAASTORE(AASTORE o) {}
  public void visitACONST_NULL(ACONST_NULL o) {}
  public void visitALOAD(ALOAD o) {}
  public void visitANEWARRAY(ANEWARRAY o) {}
  public void visitARETURN(ARETURN o) {}
  public void visitARRAYLENGTH(ARRAYLENGTH o) {}
  public void visitASTORE(ASTORE o) {}
  public void visitATHROW(ATHROW o) {}
  public void visitBALOAD(BALOAD o) {}
  public void visitBASTORE(BASTORE o) {}
  public void visitBIPUSH(BIPUSH o) {}
  public void visitBREAKPOINT(BREAKPOINT o) {}
  public void visitCALOAD(CALOAD o) {}
  public void visitCASTORE(CASTORE o) {}
  public void visitCHECKCAST(CHECKCAST o) {}
  public void visitCPInstruction(CPInstruction o) {}
  public void visitD2F(D2F o) {}
  public void visitD2I(D2I o) {}
  public void visitD2L(D2L o) {}
  public void visitDADD(DADD o) {}
  public void visitDALOAD(DALOAD o) {}
  public void visitDASTORE(DASTORE o) {}
  public void visitDCMPG(DCMPG o) {}
  public void visitDCMPL(DCMPL o) {}
  public void visitDCONST(DCONST o) {}
  public void visitDDIV(DDIV o) {}
  public void visitDLOAD(DLOAD o) {}
  public void visitDMUL(DMUL o) {}
  public void visitDNEG(DNEG o) {}
  public void visitDREM(DREM o) {}
  public void visitDRETURN(DRETURN o) {}
  public void visitDSTORE(DSTORE o) {}
  public void visitDSUB(DSUB o) {}
  public void visitDUP_X1(DUP_X1 o) {}
  public void visitDUP_X2(DUP_X2 o) {}
  public void visitDUP(DUP o) {}
  public void visitDUP2_X1(DUP2_X1 o) {}
  public void visitDUP2_X2(DUP2_X2 o) {}
  public void visitDUP2(DUP2 o) {}
  public void visitF2D(F2D o) {}
  public void visitF2I(F2I o) {}
  public void visitF2L(F2L o) {}
  public void visitFADD(FADD o) {}
  public void visitFALOAD(FALOAD o) {}
  public void visitFASTORE(FASTORE o) {}
  public void visitFCMPG(FCMPG o) {}
  public void visitFCMPL(FCMPL o) {}
  public void visitFCONST(FCONST o) {}
  public void visitFDIV(FDIV o) {}
  public void visitFieldInstruction(FieldInstruction o) {}
  public void visitFLOAD(FLOAD o) {}
  public void visitFMUL(FMUL o) {}
  public void visitFNEG(FNEG o) {}
  public void visitFREM(FREM o) {}
  public void visitFRETURN(FRETURN o) {}
  public void visitFSTORE(FSTORE o) {}
  public void visitFSUB(FSUB o) {}
  public void visitGETFIELD(GETFIELD o) {}
  public void visitGETSTATIC(GETSTATIC o) {}
  public void visitGOTO_W(GOTO_W o) {}
  public void visitGOTO(GOTO o) {}
  public void visitI2B(I2B o) {}
  public void visitI2C(I2C o) {}
  public void visitI2D(I2D o) {}
  public void visitI2F(I2F o) {}
  public void visitI2L(I2L o) {}
  public void visitI2S(I2S o) {}
  public void visitIADD(IADD o) {}
  public void visitIALOAD(IALOAD o) {}
  public void visitIAND(IAND o) {}
  public void visitIASTORE(IASTORE o) {}
  public void visitICONST(ICONST o) {}
  public void visitIDIV(IDIV o) {}
  public void visitIF_ACMPEQ(IF_ACMPEQ o) {}
  public void visitIF_ACMPNE(IF_ACMPNE o) {}
  public void visitIF_ICMPEQ(IF_ICMPEQ o) {}
  public void visitIF_ICMPGE(IF_ICMPGE o) {}
  public void visitIF_ICMPGT(IF_ICMPGT o) {}
  public void visitIF_ICMPLE(IF_ICMPLE o) {}
  public void visitIF_ICMPLT(IF_ICMPLT o) {}
  public void visitIF_ICMPNE(IF_ICMPNE o) {}
  public void visitIFEQ(IFEQ o) {}
  public void visitIFGE(IFGE o) {}
  public void visitIFGT(IFGT o) {}
  public void visitIFLE(IFLE o) {}
  public void visitIFLT(IFLT o) {}
  public void visitIFNE(IFNE o) {}
  public void visitIFNONNULL(IFNONNULL o) {}
  public void visitIFNULL(IFNULL o) {}
  public void visitIINC(IINC o) {}
  public void visitILOAD(ILOAD o) {}
  public void visitIMPDEP1(IMPDEP1 o) {}
  public void visitIMPDEP2(IMPDEP2 o) {}
  public void visitIMUL(IMUL o) {}
  public void visitINEG(INEG o) {}
  public void visitINSTANCEOF(INSTANCEOF o) {}
  public void visitInvokeInstruction(InvokeInstruction o) {}
  public void visitINVOKEINTERFACE(INVOKEINTERFACE o) {}
  public void visitINVOKESPECIAL(INVOKESPECIAL o) {}
  public void visitINVOKESTATIC(INVOKESTATIC o) {}
  public void visitINVOKEVIRTUAL(INVOKEVIRTUAL o) {}
  public void visitIOR(IOR o) {}
  public void visitIREM(IREM o) {}
  public void visitIRETURN(IRETURN o) {}
  public void visitISHL(ISHL o) {}
  public void visitISHR(ISHR o) {}
  public void visitISTORE(ISTORE o) {}
  public void visitISUB(ISUB o) {}
  public void visitIUSHR(IUSHR o) {}
  public void visitIXOR(IXOR o) {}
  public void visitJSR_W(JSR_W o) {}
  public void visitJSR(JSR o) {}
  public void visitL2D(L2D o) {}
  public void visitL2F(L2F o) {}
  public void visitL2I(L2I o) {}
  public void visitLADD(LADD o) {}
  public void visitLALOAD(LALOAD o) {}
  public void visitLAND(LAND o) {}
  public void visitLASTORE(LASTORE o) {}
  public void visitLCMP(LCMP o) {}
  public void visitLCONST(LCONST o) {}
  public void visitLDC_W(LDC_W o) {}
  public void visitLDC(LDC o) {}
  public void visitLDC2_W(LDC2_W o) {}
  public void visitLDIV(LDIV o) {}
  public void visitLLOAD(LLOAD o) {}
  public void visitLMUL(LMUL o) {}
  public void visitLNEG(LNEG o) {}
  public void visitLoadClass(LoadClass o) {}
  public void visitLoadInstruction(LoadInstruction o) {}
  public void visitLocalVariableInstruction(LocalVariableInstruction o) {}
  public void visitLOOKUPSWITCH(LOOKUPSWITCH o) {}
  public void visitLOR(LOR o) {}
  public void visitLREM(LREM o) {}
  public void visitLRETURN(LRETURN o) {}
  public void visitLSHL(LSHL o) {}
  public void visitLSHR(LSHR o) {}
  public void visitLSTORE(LSTORE o) {}
  public void visitLSUB(LSUB o) {}
  public void visitLUSHR(LUSHR o) {}
  public void visitLXOR(LXOR o) {}
  public void visitMONITORENTER(MONITORENTER o) {}
  public void visitMONITOREXIT(MONITOREXIT o) {}
  public void visitMULTIANEWARRAY(MULTIANEWARRAY o) {}
  public void visitNEW(NEW o) {}
  public void visitNEWARRAY(NEWARRAY o) {}
  public void visitNOP(NOP o) {}
  public void visitPOP(POP o) {}
  public void visitPOP2(POP2 o) {}
  public void visitPUTFIELD(PUTFIELD o) {}
  public void visitPUTSTATIC(PUTSTATIC o) {}
  public void visitRET(RET o) {}
  public void visitRETURN(RETURN o) {}
  public void visitReturnInstruction(ReturnInstruction o) {}
  public void visitSALOAD(SALOAD o) {}
  public void visitSASTORE(SASTORE o) {}
  public void visitSIPUSH(SIPUSH o) {}
  public void visitStackConsumer(StackConsumer o) {}
  public void visitStackInstruction(StackInstruction o) {}
  public void visitStackProducer(StackProducer o) {}
  public void visitStoreInstruction(StoreInstruction o) {}
  public void visitSWAP(SWAP o) {}
  public void visitTABLESWITCH(TABLESWITCH o) {}
}
