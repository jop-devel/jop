
package java.lang;

import com.jopdesign.sys.*;
import java.io.PrintStream;

public final class System {

	// should be final, but we don't call the class initializer
	// up to now.
	// public static final PrintStream out;
	public static PrintStream out;

	public static long currentTimeMillis() {
		return (long) (Native.rd(Const.IO_US_CNT)/1000);
	}


	// should only be accessed by startup code!
	// but there are no friends in Java :-(
	public static void init() {

		out = new PrintStream();
	}
}
