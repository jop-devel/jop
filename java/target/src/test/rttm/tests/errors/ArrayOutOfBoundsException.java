package rttm.tests.errors;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

import rttm.Atomic;

public class ArrayOutOfBoundsException {
	
	protected static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	public static void main(String[] args) {
		atomicMethod();
	}
	
	private static int[] foo = new int[1];
	
	@Atomic static int atomicMethod() {
		return foo[1];
	}
}
