
package wcet.gc;

/**
*	Dummy for benchmark.
*/

public class Native {

	static private int dummy;

	public static int rd(int adr) {

		return dummy+adr;
	}
	public static void wr(int val, int adr) {

		dummy = val+adr;
	}

  	public static int rdMem(int adr){return 0;}
//	public static native void wrMem(int val, int adr);
  	public static int rdIntMem(int adr){return 0;}
//	public static native void wrIntMem(int val, int adr);
  	public static int getSP(){return 0;}
//	public static native void setSP(int val);
//	public static native int getVP();
//	public static native void setVP(int val);
//	public static native void int2extMem(int intAdr, int[] extAdr, int cnt);
//	public static native void ext2intMem(int[] extAdr, int intAdr, int cnt);
//	public static native long makeLong(int hWord, int lWord);
//	public static native void invoke(int arg, int ptr);


}
