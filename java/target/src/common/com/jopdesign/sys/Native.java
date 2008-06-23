/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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
	public static native int[] toIntArray(int i);
	public static native int toInt(float f);
	public static native float toFloat(int i);
	public static native long toLong(double d);
	public static native double toDouble(long l);
	public static native void monitorExit(int ref);
//	public static native int condMove(int a, int b, boolean cond);
	/**
	 * 
	 * @param dest memory destination address
	 * @param src memory source address
	 * @param pos position to copy
	 */
	public static native void memCopy(int dest, int src, int pos);

}
