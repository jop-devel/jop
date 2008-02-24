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

package wcet.gc; 

//import com.jopdesign.sys.Const;

// History: 
// 2005-12-15: ms idea: Copy and paste from JVMHelp
// 2005-12-15: rup: Only inspect local vars + operands
// 2006-01-15: Operand walker
// 2006-01-27: Reduced word count by indexed version

//TODO: top frame inspection, the initialization issue from Sitescape

/**
 * It inspects the stack of a given thread and returns a reference to an array of stack
 * with the references for a given PC value marked with 1.
 * The <code>swk</code> method is called from the GC's getRoots method. 
 * @author rup, ms
 */
public class GCStkWalk {
	static final int MAXSTACK = 128;
	static int stack[];
	
  // gcpack, see also MethodInfo.dumpMethodGcis
  static final int UNICNTLEN = 10; // count of unique patterns
  static final int INSTRLEN = 10; //1024 instructions
  static final int MAXSTACKLEN = 5; // max 31 operands
  static final int MAXLOCALSLEN = 5; // max 31 args+locals
  static final int STACKMARKLEN = 1; // 1 if stack references
  static final int LOCALMARKLEN = 1; // 1 if local references

  //indexes of the root items for the stack that is scanned using swk(int)
  private final static int[] roots = new int[MAXSTACK];
	
	// used by swk method and the other utility methods
	static int sp, cp, fp, mp, vp, pc, addr, loc, args, mstk, mloc; 
	static int gcpack, val, active, num, infoaddr, instr;
	static int localmarkword, stackmarkword, indexbits, unicnt, length;
  
	//Walk one stack and then you should access refPos
	//All non-active stacks should have 
	//  the waitForNextPeriod stack frame on top of its
	//  stack. It should have no operands and thus we know
	//  the frame pointer and can decuct vp of the previous 
	//  threads.
	//If it is the active thread that invokes this method then 
	//  the swk(int) method is the top frame, which we do not inspect (should we?)
	/**
	 * It walks the stack for a given thread. Its behavior depends if it is walking the
	 * the active stack or not so that is flagged in the arguments. 
	 * @param num index of the thread to be "walked"
	 * @param active if the thread is the active one
	 * @param info if true then a lot of debug info is printed (which can cause 
	 *        the GC to miss the deadlines...)
	 * @return the root set marked with 1 for references, 0 for primitives and -1 for other
	 *          stack things from the frames.
	 */
	public static int[] swk(int num, boolean active, boolean info) {


//    int ts=Timer.us();
//    int ts1=0;

//    if(info){
//		  System.out.print("GCStkWalk.swk called(num=");
//		  System.out.print(num);
//		  System.out.print(",");
//		  if(active)
//		    System.out.print("active=true");
//		  else
//		    System.out.print("active=false");
//		  if(info)
//		    System.out.println("info=true)");
//		  else
//		    System.out.println("info=false)");
//		}
		// mark local refs and operand refs with 1, primitives with 0, 
		//  and the rest with -1. This array has plenty room for packing 
		//  more information if needed. For example using 1 for local refs and 
		//  2 for operand refs.
		
//enable
//		for(int i=0;i<MAXSTACK;i++){
//	    roots[i]=-1; 
//	  }	
		
//    if(active){
      sp = Native.getSP();
//    } else //walk one of the saved stacks
//    {
//		  sp = RtThreadImpl.getSP(num);
//		  stack = RtThreadImpl.getStack(num);
//    }
        
		fp = sp - 4;  // last sp points to the end of the frame
    //stop befor 'first' method, loop = 255-4-128-5/5 =  
		while (fp > 128 + 5) { // @WCA loop=5
      // saved vars of curent frame that points to 
			//   previous frame. See Fig. 6.2 in ms thesis
//enable
//			if(active){
				mp = Native.rdIntMem(fp + 4);
				cp = Native.rdIntMem(fp + 3);
				vp = Native.rdIntMem(fp + 2);
				pc = Native.rdIntMem(fp + 1);
				sp = Native.rdIntMem(fp);
//			} else
//			{
//			  mp = stack[fp + 4 - 128];
//			  cp = stack[fp + 3 - 128];//Native.rdIntMem(fp + 3);			  
//			  vp = stack[fp + 2 - 128];
//			  pc = stack[fp + 1 - 128];
//			  sp = stack[fp - 128];
//			}

			val = Native.rdMem(mp);
			addr = val >>> 10; // address of callee
			
			//TODO: Include some pointer in the method stuct.
			//      Perhaps there is room for the mgcisKey index in the method struct.
			//      Methodinfo:464
			val = Native.rdMem(mp + 1); // cp, locals, args
			args = val & 0x1f;
			loc = (val >>> 5) & 0x1f;
      
      gcpack = Native.rdMem(addr-1);
      mloc = (gcpack>>>(STACKMARKLEN+LOCALMARKLEN))&0x1F;//MAXLOCALSLEN. See gcMaxLocals();
      mstk = gcpack&0x01;//STACKMARKLEN gcMaxStack();
      unicnt = (gcpack>>>(INSTRLEN+MAXSTACKLEN+MAXLOCALSLEN+STACKMARKLEN+LOCALMARKLEN))&0x03FF; //UNICNTLEN. See gcUniCnt();
      instr = (gcpack>>>(MAXSTACKLEN+MAXLOCALSLEN+STACKMARKLEN+LOCALMARKLEN))&0x03FF;//INSTRLEN gcInstr();
//      length = gcLength(); // also sets indexbits
      
			fp = vp + args + loc; // the fp can be calc. with vp and count of args + locals
			      
      //Just check to see if JOP args count equals the gcpack info
//      if((args+loc) != mloc){
//        System.out.println("Method "+addr+":"+(args+loc)+"!="+gcMaxLocals());
//        System.exit(-1);
//      }
      length = gcLength();
      infoaddr = addr - 1 - length; // now point to first gc info word
      // it makes mgci and ogci words for the the PC

      setMarkWords();
         
      //Now do that root marking
      for(int i=0;i<mloc;i++){ //@WCA loop=32
        // don't overwrite the ref belonging to the frame on top of this
        if(roots[vp+i-128]==-1){
          roots[vp+i-128]=gcLocalMarkBit(i);
        }
      }
      for(int i=0;i<mstk;i++){ //@WCA loop=32
        // don't overwrite the ref belonging to the frame on top of this
        if(roots[fp+5+i-128]==-1){
          roots[fp+5+i-128]=gcStackMarkBit(i);
        }
      }
      // debugging
//      if(info){
//        wrFrame(num);
//      }

		} // while loop
    
		// debugging
//		if(info){
//		  wrroots(num);
//		}
    
		return roots;
	}
	
	// This is a good method for debugging. You start by finding 
	//  the matching method (mpi) id in the .jop file.
/*	static void wrFrame(int num){
	  wr("---Frame info-Thread-no ");
    wrSmall(num);
    wr("--\n");

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
		
		wr(" vp:");
		wrSmall(vp);
				
		wr(" pc:");
		wrSmall(pc);
		wr('\n');
		
    wr("instrs:");
    wrSmall(gcInstr());

		wr(" indexbits:");
		wrSmall(indexbits);
    
    wr(" length:");
    wrSmall(gcLength());
    wr('\n');
    
		wr("uc:");
		wrSmall(gcUniCnt());
		
		wr(" addr:");
		wrSmall(addr);

		wr(" infoaddr:");
		wrSmall(infoaddr);
		wr('\n');
	
	  wr("localmark:");
	  wrDigit(gcLocalMark());
	  
	  wr(" stackmark:");
	  wrDigit(gcStackMark());
	  wr('\n');
		
		wr("ogis[pc=");
		wrSmall(pc);
		wr("]:");
  	int mask = 0x01;
		for(int i = 31;i>=0;i--){ // @WCA loop=32
		  if((i+1)%8==0 && i<31){ 
			  wr('_');
			}
			if(i<gcMaxStack()){
			  wrDigit(gcStackMarkBit(i));
			} else{
			  wrDigit(0);
			}
		}
		wr('\n');			
			
		wr("mgis[pc=");
		wrSmall(pc);
		wr("]:");
		for(int i = 31;i>=0;i--){ // @WCA loop=32
		  if((i+1)%8==0 && i<31){
			  wr('_');
			}
			if(i<gcMaxLocals()){
			  wrDigit(gcLocalMarkBit(i));
			} else {
			  wrDigit(0);
			}
		}
		wr('\n');

    wr("local ref bits:\n");
    for(int i=0;i<gcMaxLocals();i++){ // @WCA loop=32
    	 wr("stack[");
    	 wrSmall(vp+i);
    	 wr("]:");
       wrDigit(roots[vp+i-128]);
       wr("\n"); 	
    }

    wr("operand ref bits:\n");
    for(int i=0;i<gcMaxStack();i++){ // @WCA loop=32
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
		for(int i=0;i<MAXSTACK;i++){ // @WCA loop=128
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
//			while ((Native.rd(Const.IO_STATUS) & 1) == 0)
//				;
			Native.wr(c, 0);
		}
	}

	static void wr(String s) { 
		int i = s.length();
//    if(i>80)
//      System.out.println("String too long...");
		for (int j = 0; j < i; ++j) { // @WCA loop=80
			wr(s.charAt(j));
		}
	}
*/
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

  static int gcUniCnt(){
    return ((gcpack>>>(INSTRLEN+MAXSTACKLEN+MAXLOCALSLEN+STACKMARKLEN+LOCALMARKLEN))&0x03FF); //UNICNTLEN
  }
  
  // in words
  static int gcLength(){
  	int wordcnt;

		// how wide is index
		int num = 1;
    indexbits = 0;
    for(int i=1;i<32;i++){ // @WCA loop=31
      if(num<unicnt){
        num = (num<<1)|1;
      } else
      {
	      indexbits = i;
	      break;
      }
    }

  	if(gcStackMark()==1 || gcLocalMark()==1){
   	  int pos = instr*indexbits + unicnt*(mstk+mloc);
  	  wordcnt = pos >> 5;
  	  if((pos & 0x1F)>0){
  	    wordcnt++;
  	  }
  	} else{
  	  wordcnt = 0;
  	}
  	
  	return wordcnt;
  }

  // prepare the stack and local mark words 
  // called once for each frame
  static void setMarkWords(){
    stackmarkword = localmarkword = 0;  	
//    wr("inside setMarkWords\n");
    if(gcLocalMark()==1 || gcStackMark() ==1){ // is there info
      //read the index
      int index = 0;
      int indexpos = pc*indexbits;
      for(int j=0;j<indexbits;j++){ // @WCA loop=31
        int wordindex = (indexpos+j) >> 5; // div 32
        int offset = (indexpos+j) & 0x1F; // mod 32
        int val = Native.rdMem(infoaddr+wordindex);
        int bit = (val>>>offset) & 0x01;  
//System.out.print("val ");
//bitStr(val);
//System.out.println("");
//System.out.print("infoaddr ");
//System.out.println(infoaddr);
//System.out.print("wordindex ");
//System.out.println(wordindex);
//System.out.print("offset ");
//System.out.println(offset);
//System.out.print("indexpos ");
//System.out.println(indexpos);
//System.out.print("indexbits ");
//System.out.println(indexbits);
//System.out.print("bit ");
//System.out.print(j);
//System.out.print(" ");
//System.out.println(bit);
        index |= (bit<<j);	
      }


//System.out.print("p:");
//System.out.println(index);        
        //read the corresponding unique pattern
      indexpos = instr*indexbits+index*(mloc+mstk);
      int oldwordindex = -1;
      int val = -1;
      for(int j=0;j<(mloc+mstk);j++){ // @WCA loop=32

        //          indexpos = inst*indexbits+index*(gcMaxLocals()+gcMaxStack())+j;
        int wordindex = (indexpos+j)>>5; // /32
        int offset = (indexpos+j) & 0x1F;
        
        if(oldwordindex != wordindex){
          val = Native.rdMem(infoaddr+wordindex);
        } 
        oldwordindex = wordindex;
        int bit = (val>>>offset) & 0x01;  
        if(j<mstk){ // stack marks
          stackmarkword |= bit<<j;
        } 
        else { // local marks
          localmarkword |= bit<<(j-mstk);
        }

      }

    } 
  }
  
  /**
   * 1 if the stack has a reference on the <code>index</code> position.
   */
  static int gcStackMarkBit(int index){
    if(index>mstk){
	    //System.out.println("index too big");
	    //System.exit(-1);
    } 	
    return (stackmarkword>>>index)&0x01;
  }
  
  /**
   * 1 if the locals has a reference on the <code>index</code> position.
   */
  public static int gcLocalMarkBit(int index){
    if(index>mloc){
	    //System.out.println("index too big");
	    //System.exit(-1);
    } 	
  	return (localmarkword>>>index)&0x01;
  }
  
  // Note that we write low order bits first.
/*  static void bitStr(int word){
	  int mask = 0x01;
	  //for(int i = 31;i>=0;i--){
	  for(int i = 0;i<32;i++){ // @WCA loop=32
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
  }*/
}
