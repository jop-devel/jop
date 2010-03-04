package rttm.tests.errors;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

import rttm.Atomic;

public class NullPointerException {
	
	protected static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	public static void main(String[] args) {
		atomicMethod();
	}
	
	private int foo;
	
	@Atomic static int atomicMethod() {
		NullPointerException o = null;
		
		return o.foo;
	}
}
