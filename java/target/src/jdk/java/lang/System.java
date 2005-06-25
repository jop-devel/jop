
package java.lang;

import java.io.InputStream;
import java.io.PrintStream;

import com.jopdesign.sys.*;

public final class System {

	// should be final, but we don't call the class initializer
	// up to now.
	// public static final PrintStream out;
	public static PrintStream out;
	public static InputStream in;

	  
	public static long currentTimeMillis() {
		return (long) (Native.rd(Const.IO_US_CNT)/1000);
	}


	// should only be accessed by startup code!
	// but there are no friends in Java :-(
	public static void init() {

		out = new PrintStream();
		in = new InputStream();
	}
	
	public static void exit(int i) {
		Startup.exit();
	}
}
