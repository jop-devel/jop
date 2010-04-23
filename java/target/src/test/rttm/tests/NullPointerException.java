package rttm.tests;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

import rttm.atomic;

public class NullPointerException {
	
	protected static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	public static void main(String[] args) {
		atomicMethod();
	}
	
	private int foo;
	
	@atomic static int atomicMethod() {
		NullPointerException o = null;
		
		return o.foo;
	}
}
