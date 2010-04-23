package rttm.tests;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

import rttm.atomic;

public class ArrayOutOfBoundsException {
	
	protected static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	public static void main(String[] args) {
		atomicMethod();
	}
	
	private static int[] foo = new int[1];
	
	@atomic static int atomicMethod() {
		return foo[1];
	}
}
