/*
 * Created on 24.05.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.jopdesign.sys;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Startup {
	
	// use static vars, don't waste stack
	static int var;
	
	/**
	 * called from jvm.asm as first method.
	 * Do all initialization here and call main method.
	 */
	static void boot() {
		
		// place for some initialization:
		System.init();
		msg();
		
		// call main()
		var = Native.rdMem(0);		// pointer to 'special' pointers
		var = Native.rdMem(var+3);	// pointer to mein method struct
		Native.invoke(0, var);		// call main (with null pointer on TOS
		JVMHelp.wr("\r\nJVM exit!\r\n");
		for (;;) ;
	}

	static void msg() {

		int version = Native.rdIntMem(64);
		JVMHelp.wr("JOP start V ");
		// take care with future GC - JVMHelp.intVal allocates
		// a buffer!
		if (version==0x12345678) {
			JVMHelp.wr("pre2005");
		} else {
			JVMHelp.intVal(version);
		}
		JVMHelp.wr("\r\n");
	}
}
