/*
 * Created on 28.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test;

import com.jopdesign.sys.*;

/**
 * @author martin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HelloWorld {

	public static void main(String[] args) {

		int val;

		System.out.println("Hello World from JOP!");
		val = Native.rd(Const.IO_CNT);
		System.out.println(val);
		val = Native.rd(Const.IO_CNT);
		System.out.println(val);
	}
}
