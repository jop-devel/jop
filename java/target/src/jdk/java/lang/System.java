
package java.lang;

import com.jopdesign.sys.*;

public final class System {

	public static long currentTimeMillis() {
		return (long) (Native.rd(Native.IO_US_CNT)/1000);
	}

	// public static final PrintStream out = VMSystem.makeStandardOutputStream();
}
