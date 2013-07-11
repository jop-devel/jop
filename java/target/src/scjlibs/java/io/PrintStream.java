/* PrintStream.java -- OutputStream for printing output
 Copyright (C) 1998, 1999, 2001, 2003, 2004, 2005, 2006
 Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.
 
 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package java.io;

public class PrintStream extends OutputStream {

	OutputStream out;

	/*
	 * Notice the implementation is quite similar to OutputStreamWriter. This
	 * leads to some minor duplication, because neither inherits from the other,
	 * and we want to maximize performance.
	 */

	/**
	 * This boolean indicates whether or not an error has ever occurred on this
	 * stream.
	 */
	private boolean error_occurred = false;

	/**
	 * This method intializes a new <code>PrintStream</code> object to write
	 * to the specified output sink.
	 * 
	 * @param out
	 *            The <code>OutputStream</code> to write to.
	 */
	public PrintStream(OutputStream out) {
		this.out = out;
	}
	
	// FIXME: this constructor is only to make System.out work
	public PrintStream(){
		this.out = null;
	}

	/**
	 * This method checks to see if an error has occurred on this stream. Note
	 * that once an error has occurred, this method will continue to report
	 * <code>true</code> forever for this stream. Before checking for an error
	 * condition, this method flushes the stream.
	 * 
	 * @return <code>true</code> if an error has occurred, <code>false</code>
	 *         otherwise
	 */
	public boolean checkError() {
		flush();
		return error_occurred;
	}

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
		try {
			out.flush();
		} catch (IOException iioe) {
		}
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
	public synchronized void print(char c) {
		Character ch = new Character(c);
		print(ch.toString());
	}

	public void print(char[] charArray) {
		String s = new String(charArray);
		print(s);
	}

	public void print(int inum) {
		print(String.valueOf(inum));
	}

	public void print(long lnum) {
		print(String.valueOf(lnum));
	}

	public void print(Object obj) {
		print(obj == null ? "null" : obj.toString());
	}

	private synchronized void print(String str) {
		try {
			writeChars(str);
			flush();
		} catch (IOException e) {
			setError();
		}
	}

	/**
	 * This method prints a line separator sequence to the stream. The value
	 * printed is determined by the system property <xmp>line.separator</xmp>
	 * and is not necessarily the Unix '\n' newline character.
	 */
	public void println() {
		String str = "\r\n";
		try {
			writeChars(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	/**
	 * This method can be called by subclasses to indicate that an error has
	 * occurred and should be reported by <code>checkError</code>.
	 */
	protected void setError() {
		error_occurred = true;
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

		try {
			out.write(oneByte & 0xff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(byte[] buffer, int offset, int len) throws IOException {
		out.write(buffer, offset, len);

	}

	public void writeChars(String str)
			throws IOException {
		byte[] bytes = str.substring(0,str.length()).getBytes();
		out.write(bytes, 0, bytes.length);
	}
} // class PrintStream

