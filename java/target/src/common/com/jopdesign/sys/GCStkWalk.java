package com.jopdesign.sys; 

import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.JVMHelp;

// History: 
// 2005-12-15: ms idea: Copy and paste from JVMHelp
// 2005-12-15: rup: Only inspect local vars + operands
// 2006-01-15: Operand walker

//TODO: ask ms about how much mem can be used

// The stack tracer does not inspect the top frame for 
// reference positions because the swk method is the top
// frame
// It is intended for use only from the GC class. No app class should call
//   directly.
public class GCStkWalk {
	static final int MAXSTACK = 128;
	static int stack[];
	
  // gcpack, see also MethodInfo.dumpMethodGcis
  static final int INSTRLEN = 10; //1024 instructions
  static final int MAXSTACKLEN = 5; // max 31 operands
  static final int MAXLOCALSLEN = 5; // max 31 args+locals
  static final int STACKMARKLEN = 1; // 1 if stack references
  static final int LOCALMARKLEN = 1; // 1 if local references

  static final int WORDLEN = 32;

  //indexes of the root items for the stack that is scanned using swk(int)
  private final static int[] roots = new int[MAXSTACK];
	
	// used by swk method and the other utility methods
	static int sp, cp, fp, mp, vp, pc, addr, loc, args, gcpack, val, active, num, infoaddr;

	//Walk one stack and then you should access refPos
	//All non-active stacks should have 
	//  the waitForNextPeriod stack frame on top of its
	//  stack. It should have no operands and thus we know
	//  the frame pointer and can decuct vp of the previous 
	//  threads.
	//If it is the active thread that invokes this method then 
	//  the swk(int) method is the top frame, which we do not inspect (should we?)
	public static int[] swk(int num, boolean active, boolean info) {
		// mark local refs and operand refs with 1, primitives with 0, 
		//  and the rest with -1. This array has plenty room for packing 
		//  more information if needed. For example using 1 for local refs and 
		//  2 for operand refs.
		
		for(int i=0;i<MAXSTACK;i++){
	    roots[i]=-1; 
	  }	
		
    if(active){
      sp = Native.getSP();
    } else //walk one of the saved stacks
    {
		  sp = RtThreadImpl.getSP(num);
		  stack = RtThreadImpl.getStack(num);
    }
        
		fp = sp - 4;  // last sp points to the end of the frame
		
		while (fp > 128 + 5) { // stop befor 'first' method
			// saved vars of curent frame that points to 
			//   previous frame. See Fig. 6.2 in ms thesis
			if(active){
				mp = Native.rdIntMem(fp + 4);
				cp = Native.rdIntMem(fp + 3);
				vp = Native.rdIntMem(fp + 2);
				pc = Native.rdIntMem(fp + 1);
				sp = Native.rdIntMem(fp);
			} else
			{
			  mp = stack[fp + 4 - 128];
			  cp = stack[fp + 3 - 128];//Native.rdIntMem(fp + 3);			  
			  vp = stack[fp + 2 - 128];
			  pc = stack[fp + 1 - 128];
			  sp = stack[fp - 128];
			}

			val = Native.rdMem(mp);
			addr = val >>> 10; // address of callee
			
			//TODO: This is not good for WCET. Include some pointer in the method stuct.
			//      Perhaps there is room for the mgcisKey index in the method struct.
			//      Methodinfo:464
			val = Native.rdMem(mp + 1); // cp, locals, args
			args = val & 0x1f;
			loc = (val >>> 5) & 0x1f;
      
      gcpack = Native.rdMem(addr-1);

			fp = vp + args + loc; // the fp can be calc. with vp and count of args + locals
			      
      //Just check to see if JOP args count equals the gcpack info
      if((args+loc) != gcMaxLocals()){
        System.out.println("Method "+addr+":"+(args+loc)+"!="+gcMaxLocals());
        System.exit(-1);
      }
      
      infoaddr = addr - 1 - gcLength(); // now point to first gc info word
      
      //Now do that root marking
      for(int i=0;i<gcMaxLocals();i++){
        // don't overwrite the ref belonging to the frame on top of this
        if(roots[vp+i-128]==-1){
          roots[vp+i-128]=gcLocalMarkBit(i);    	
        }
      }
      for(int i=0;i<gcMaxStack();i++){
        // don't overwrite the ref belonging to the frame on top of this
        if(roots[fp+5+i-128]==-1){
          roots[fp+5+i-128]=gcStackMarkBit(i);    	
        }
      }
      
      // debugging
      if(info){
        wrFrame();
      }
wr('Z');

		} // while loop
		
		// debugging
		if(info){
		  wrroots(num);
		}
		
		return roots;
	}
	
	// This is a good method for debugging. You start by finding 
	//  the matching method (mpi) id in the .jop file.
	static void wrFrame(){
	  wr("---Frame info---\n");

		wr("gcpack:");
		bitStr(gcpack);
		wr('\n');

		wr("maxStack:");
		wrSmall(gcMaxStack());
		
		wr(" maxLocals:");
		wrSmall(gcMaxLocals());
		
		wr(" args:");
		wrSmall(args);
		
		wr(" loc:");
		wrSmall(loc);
		wr('\n');
		
	  wr("sp:");
		wrSmall(sp);
		
		wr(" fp:");
		wrSmall(fp);

		wr(" mp:");
		wrSmall(mp);
		wr('\n');
		
		wr("vp:");
		wrSmall(vp);
		
		wr(" pc:");
		wrSmall(pc);
		
		wr(" addr:");
		wrSmall(addr);

		wr(" infoaddr:");
		wrSmall(infoaddr);
		wr('\n');
		
		wr("ogis[pc=");
		wrSmall(pc);
		wr("]:");
  	int mask = 0x01;
		for(int i = 31;i>=0;i--){
		  if((i+1)%8==0 && i<31){
			  wr('_');
			}
			wrDigit(gcStackMarkBit(i));
		}
		wr('\n');			
			
		wr("mgis[pc=");
		wrSmall(pc);
		wr("]:");
		for(int i = 31;i>=0;i--){
		  if((i+1)%8==0 && i<31){
			  wr('_');
			}
			wrDigit(gcLocalMarkBit(i));
		}
		wr('\n');

    wr("local ref bits:\n");
    for(int i=0;i<gcMaxLocals();i++){
    	 wr("stack[");
    	 wrSmall(vp+i);
    	 wr("]:");
       wrDigit(roots[vp+i-128]);
       wr("\n"); 	
    }

    wr("operand ref bits:\n");
    for(int i=0;i<gcMaxStack();i++){
    	 wr("stack[");
    	 wrSmall(fp+5+i);
    	 wr("]:");
       wrDigit(roots[fp+5+i-128]);
       wr("\n"); 	
    }
 		wr('\n');			
	}
	
	static void wrroots(int num){
		wr("--Complete stack for RtThread no. ");
		wrSmall(num);
		wr("--\n");
		for(int i=0;i<MAXSTACK;i++){
    	wr("stack[");
    	wrSmall(i+128);
    	wr("]:");
      wrDigit(roots[i]);
      wr("\n"); 	
		}
	}
	
	static void wrByte(int i) {
		wr('0' + i / 100);
		wr('0' + i / 10 % 10);
		wr('0' + i % 10);
		wr(' ');
	}

	static void wrSmall(int i) {
		wr('0' + i / 10000 % 10);
		wr('0' + i / 1000 % 10);
		wr('0' + i / 100 % 10);
		wr('0' + i / 10 % 10);
		wr('0' + i % 10);
	}

	static void wrDigit(int i) {
		wr('0' + i % 10);
	}

	static void wr(int c) {
		if (true) {
			while ((Native.rd(Const.IO_STATUS) & 1) == 0)
				;
			Native.wr(c, Const.IO_UART);
		}
	}

	static void wr(String s) {
		int i = s.length();
		for (int j = 0; j < i; ++j) {
			wr(s.charAt(j));
		}
	}

	//gc unpacking
	static int gcStackMark(){
	  return (gcpack&0x01);//STACKMARKLEN
	}
	 
	static int gcLocalMark(){
	  return ((gcpack>>>STACKMARKLEN)&0x01);//LOCALMARKLEN
	}
	
  static int gcMaxLocals(){
    return ((gcpack>>>(STACKMARKLEN+LOCALMARKLEN))&0x1F);//MAXLOCALSLEN
  }	

  static int gcMaxStack(){
    return ((gcpack>>>(MAXLOCALSLEN+STACKMARKLEN+LOCALMARKLEN))&0x1F);//MAXSTACKLEN
  }	
  
  static int gcInstr(){
    return ((gcpack>>>(MAXSTACKLEN+MAXLOCALSLEN+STACKMARKLEN+LOCALMARKLEN))&0x03FF);//INSTRLEN
  }
  
  // in words
  static int gcLength(){
   	int instCnt = gcInstr();
//System.out.print("gcInstr():");
//System.out.println(gcInstr());   	
   	int varcnt = gcMaxStack()+gcMaxLocals();
//System.out.print("gcMaxStack()+gcMaxLocals():");
//System.out.println(gcMaxStack()+gcMaxLocals());
//System.out.print("gcMaxStack():");
//System.out.println(gcMaxStack());
//System.out.print("gcMaxLocals():");
//System.out.println(gcMaxLocals());
  	int pos = instCnt*varcnt;
//System.out.print("pos:");
//System.out.println(pos);  	  	
  	int wordcnt = pos / WORDLEN;
  	if((pos % WORDLEN)>0){
  	  wordcnt++;
  	}
//System.out.print("wordcnt:");
//System.out.println(wordcnt);  	  	  	
  	return wordcnt;
  }
  
  /**
   * 1 if the stack has a reference on the <code>index</code> position.
   */
  static int gcStackMarkBit(int index){
    int bit = 0;
    if(index<gcMaxStack()){
      int varcnt = gcMaxStack()+gcMaxLocals();
      int pos = pc * varcnt+gcMaxLocals()+index;
      int wordindex = pos / WORDLEN;
//wrSmall(infoaddr);
//wrSmall(pc);
//wrSmall(index);
//wrSmall(pos);
//wrSmall(WORDLEN);
      int offset = pos % WORDLEN;
      int val = Native.rdMem(infoaddr+wordindex);
      bit = (val>>>offset) & 0x01;
    }
    return bit;
  }
  
  /**
   * 1 if the locals has a reference on the <code>index</code> position.
   */
  static int gcLocalMarkBit(int index){
    
    int bit = 0;
    
    if(index<gcMaxLocals()){
      int varcnt = gcMaxStack()+gcMaxLocals();
    
      int pos = pc * varcnt + index;
    
      int wordindex = pos / WORDLEN;
      int offset = pos % WORDLEN;
      int val = Native.rdMem(infoaddr+wordindex);
    
      bit = (val>>>offset) & 0x01;
    }
    return bit;
  }
  
  static void bitStr(int word){
	  int mask = 0x01;
	  for(int i = 31;i>=0;i--){
		  int res = (word>>>i) & mask;
		  if((i+1)%8==0 && i<31){
			  wr('_');
			}
		  if(res==1){
		    wr('1');
		  }
		  else{
		    wr('0');
		  }
	  }
  }
}