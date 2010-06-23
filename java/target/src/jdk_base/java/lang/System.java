
package java.lang;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.JOPInputStream;
import java.io.JOPPrintStream;

import com.jopdesign.sys.*;

public final class System {

	// should be final, but we don't call the class initializer
	// up to now.
	// but we do it now, right?
	// public static final PrintStream out;
	public static PrintStream out = new JOPPrintStream();
	public static InputStream in = new JOPInputStream();
	public static PrintStream err = out;

	  
	public static long currentTimeMillis() {
		return (long) (Native.rd(Const.IO_US_CNT)/1000);
	}

	public static long nanoTime() {
		return (long)Native.rd(Const.IO_US_CNT)*1000;
	}


	// should only be accessed by startup code!
	// but there are no friends in Java :-(
//	public static void init() {
//
//		out = new JOPPrintStream();
//		in = new JOPInputStream();
//		err = out;
//	}
	
	public static void exit(int i) {
		Startup.exit();
	}
	
	public static void arraycopy(Object src, int srcOffset, Object dst,
			int dstOffset, int length) {

		long srcEnd, dstEnd;

		if ((src == null) || (dst == null)) {
			throw new NullPointerException();
		}

		srcEnd = length + srcOffset;
		dstEnd = length + dstOffset;

		int srcHandle = Native.toInt(src);
		int dstHandle = Native.toInt(dst);

		// the type field from the handle - see GC.java
		int src_type = Native.rdMem(srcHandle+GC.OFF_TYPE);
		int dst_type = Native.rdMem(dstHandle+GC.OFF_TYPE);
		
		// 0 means it's a plain object
		if (src_type==0 || dst_type==0) {
			throw new ArrayStoreException();
		}
		// should be the same, right?
		if (src_type!=dst_type) {
			throw new ArrayStoreException();			
		}
		// TODO: should we check the object types?

		// TODO: synchronized with GC
		synchronized (GC.getMutex()) {
			int srcPtr = Native.rdMem(srcHandle);
			int dstPtr = Native.rdMem(dstHandle);

			int srcLen = Native.rdMem(srcHandle+1);
			int dstLen = Native.rdMem(dstHandle+1);
			if ((srcOffset < 0) || (dstOffset < 0) || (length < 0)
					|| (srcEnd > srcLen) || (dstEnd > dstLen))
				throw new IndexOutOfBoundsException();

			if (src==dst && srcOffset<dstOffset) {
				for (int i=length-1; i>=0; --i) {
					Native.wrMem(Native.rdMem(srcPtr + srcOffset + i), dstPtr
							+ dstOffset + i);								
				}
			} else {
				for (int i = 0; i < length; i++) {
					Native.wrMem(Native.rdMem(srcPtr + srcOffset + i), dstPtr
							+ dstOffset + i);				
				}
			}
		}
	}


	public static String getProperty(String nm) {
		// no property in our embedded system
		return null;

	}
	public static void gc() {
		GC.gc();
	}


	// TODO: this is not correct...
	// MS: this is still not completely correct.
	// Perhaps we don't care about it at the
	// moment.
	// see Sun Javadoc for the correct version.
	public int identityHashCode(Object x) {

		return x.hashCode();
	}
	
}
