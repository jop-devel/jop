
package com.jopdesign.sys;

/**
*	native functions in JOP JVM.
*	Mapping is defined in JopInstr.java
*/

public class Native {

	public static native int rd(int adr);
	public static native void wr(int val, int adr);
	public static native int rdMem(int adr);
	public static native void wrMem(int val, int adr);
	public static native int rdIntMem(int adr);
	public static native void wrIntMem(int val, int adr);
	public static native int getSP();
	public static native void setSP(int val);
	public static native int getVP();
	public static native void setVP(int val);
	public static native void int2extMem(int intAdr, int[] extAdr, int cnt);
	public static native void ext2intMem(int[] extAdr, int intAdr, int cnt);
	public static native long makeLong(int hWord, int lWord);
	public static native void invoke(int arg, int ptr);
	public static native void invoke(int ptr);
	public static native int toInt(Object o);
	public static native Object toObject(int i);
	public static native int toInt(float f);
	public static native float toFloat(int i);
	public static native long toLong(double d);
	public static native double toDouble(long l);
//	public static native int condMove(int a, int b, boolean cond);
	/**
	 * 
	 * @param src memory source address
	 * @param dest memory destination address
	 * @param cnt number of words (cnt must be >0!)
	 */
	public static native void memCopy(int src, int dest, int cnt);

}
