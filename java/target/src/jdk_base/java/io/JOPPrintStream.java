package java.io;

import com.jopdesign.sys.JVMHelp;

// public class PrintStream extends FilterOutputStream
public class JOPPrintStream extends PrintStream {

	public JOPPrintStream() {
		if (tmp == null) {
			tmp = new int[MAX_TMP];
		}
	}

	private static final int MAX_TMP = 32;

	private static int[] tmp; // a generic buffer



	/**
	 * This method closes this stream and all underlying streams.
	 */
	public void close() {
		try {
			flush();

			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This method flushes any buffered bytes to the underlying stream and then
	 * flushes that stream as well.
	 */
	public void flush() {
		//do nothing
	}

	/**
	 * This methods prints a boolean value to the stream. <code>true</code>
	 * values are printed as "true" and <code>false</code> values are printed
	 * as "false".
	 * 
	 * @param bool
	 *            The <code>boolean</code> value to print
	 */
	public void print(boolean bool) {
		if (bool) {
			print("true");
		} else {
			print("false");
		}

	}

	/**
	 * This method prints a char to the stream. The actual value printed is
	 * determined by the character encoding in use.
	 * 
	 * @param ch
	 *            The <code>char</code> value to be printed
	 */

	public void print(char c) {
		JVMHelp.wr(c);
	}

	public void print(char[] charArray) {
		// MS: I don't like new ;-)
		// String s = new String(charArray);
		// print(s);
		for (int i=0; i<charArray.length; ++i) {
			wr(charArray[i]);
		}
	}

//	public void print(int inum) {
//		print(String.valueOf(inum));
//	}
	public void print(int i) {
		wr(i);
	}

	public void print(long lnum) {
		print(String.valueOf(lnum));
	}

	public void print(Object obj) {
		print(obj == null ? "null" : obj.toString());
	}

	private // synchronized
		void print(String str) {
		wr(str);
		// there is no flush on the serial line
		// flush();
	}

	/**
	 * This method prints a line separator sequence to the stream. The value
	 * printed is determined by the system property <xmp>line.separator</xmp>
	 * and is not necessarily the Unix '\n' newline character.
	 */
	public void println() {
		wr("\r\n");
	}

	/**
	 * This methods prints a boolean value to the stream. <code>true</code>
	 * values are printed as "true" and <code>false</code> values are printed
	 * as "false".
	 * <p>
	 * This method prints a line termination sequence after printing the value.
	 * 
	 * @param bool
	 *            The <code>boolean</code> value to print
	 */
	public void println(boolean bool) {
		print(bool);
		println();
	}

	/**
	 * This method prints a char to the stream. The actual value printed is
	 * determined by the character encoding in use.
	 * <p>
	 * This method prints a line termination sequence after printing the value.
	 * 
	 * @param ch
	 *            The <code>char</code> value to be printed
	 */
	public synchronized void println(char ch) {
		print(ch);
		println();
	}

	/**
	 * This method prints an array of characters to the stream. The actual value
	 * printed depends on the system default encoding.
	 * <p>
	 * This method prints a line termination sequence after printing the value.
	 * 
	 * @param charArray
	 *            The array of characters to print.
	 */
	public void println(char[] charArray) {
		print(charArray);
		println();
	}

	/**
	 * This method prints an integer to the stream. The value printed is
	 * determined using the <code>String.valueOf()</code> method.
	 * <p>
	 * This method prints a line termination sequence after printing the value.
	 * 
	 * @param inum
	 *            The <code>int</code> value to be printed
	 */
	public void println(int i) {
		print(i);
		println();
	}

	/**
	 * This method prints a long to the stream. The value printed is determined
	 * using the <code>String.valueOf()</code> method.
	 * <p>
	 * This method prints a line termination sequence after printing the value.
	 * 
	 * @param lnum
	 *            The <code>long</code> value to be printed
	 */
	public void println(long lnum) {
		print(lnum);
		println();
	}

	/**
	 * This method prints an <code>Object</code> to the stream. The actual
	 * value printed is determined by calling the <code>String.valueOf()</code>
	 * method.
	 * <p>
	 * This method prints a line termination sequence after printing the value.
	 * 
	 * @param obj
	 *            The <code>Object</code> to print.
	 */
	public void println(Object obj) {
		print(obj);
		println();
	}

	/**
	 * This method prints a <code>String</code> to the stream. The actual
	 * value printed depends on the system default encoding.
	 * <p>
	 * This method prints a line termination sequence after printing the value.
	 * 
	 * @param str
	 *            The <code>String</code> to print.
	 */
	public void println(String s) {
		print(s);
		println();
	}


	public void write(byte[] buffer, int offset, int len) throws IOException {
		if (buffer == null)
			throw new IOException("io.JOPPrintStream: buffer is null");
		for (int i = 0; i < len; i++) {
			JVMHelp.wr((char) (buffer[offset+i] & 0xff));
		}
	}

	/**
	 * This method writes a byte of data to the stream. If auto-flush is
	 * enabled, printing a newline character will cause the stream to be flushed
	 * after the character is written.
	 * 
	 * @param oneByte
	 *            The byte to be written
	 */
	public void write(int oneByte) {

		JVMHelp.wr((char) (oneByte & 0xff));
	}


	static void wr(char c) {

		JVMHelp.wr(c);
		// // no buffering => busy wait on serial line!
		// while ((Native.rd(Const.IO_STATUS)&1)==0)
		// 	;
		// Native.wr(c, Const.IO_UART);
	}

	static void wr(String s) {

		int i = s.length();
		for (int j = 0; j < i; ++j) {
			JVMHelp.wr(s.charAt(j));
		}
	}

	static void wr(int val) {

		int i;
		int sign = 1;
		if (val < 0) {
			wr('-');
			// val = -val;
			sign = -1;
		}
		for (i = 0; i < MAX_TMP - 1; ++i) {
			// tmp[i] = (val%10)+'0';
			tmp[i] = ((val % 10) * sign) + '0';
			val /= 10;
			if (val == 0)
				break;
		}
		for (val = i; val >= 0; --val) {
			wr((char) tmp[val]);
		}
		// wr(' ');
	}
}
