/* InputStreamReader.java -- Reader than transforms bytes to chars
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

public class InputStreamReader extends Reader {
	/**
	 * The input stream.
	 */
	private InputStream in;

	/**
	 * java.io canonical name of the encoding.
	 */
	private String encoding;

	/**
	 * This method initializes a new instance of <code>InputStreamReader</code>
	 * to read from the specified stream using the default encoding.
	 * 
	 * @param in
	 *            The <code>InputStream</code> to read from
	 */
	public InputStreamReader(InputStream in) {

		this.in = in;

	}

	/**
	 * This method initializes a new instance of <code>InputStreamReader</code>
	 * to read from the specified stream using a caller supplied character
	 * encoding scheme. Note that due to a deficiency in the Java language
	 * design, there is no way to determine which encodings are supported.
	 * 
	 * @param in
	 *            The <code>InputStream</code> to read from
	 * @param encoding_name
	 *            The name of the encoding scheme to use
	 * 
	 * @exception UnsupportedEncodingException
	 *                If the encoding scheme requested is not available.
	 */
	public InputStreamReader(InputStream in, String encoding_name)
			throws UnsupportedEncodingException {
		if (!(encoding_name.equals("ASCII") || encoding_name.equals("ascii") || encoding_name
				.equals("Ascii"))) {
			throw new UnsupportedEncodingException(
					"io.InputStreamReader: only ASCII encoding supported");
		}

		this.in = in;

	}

	/**
	 * This method closes this stream, as well as the underlying
	 * <code>InputStream</code>.
	 * 
	 * @exception IOException
	 *                If an error occurs
	 */
	public void close() throws IOException {
		synchronized (lock) {
			// Makes sure all intermediate data is released by the decoder.

			if (in != null)
				in.close();
			in = null;

		}
	}

	/**
	 * This method returns the name of the encoding that is currently in use by
	 * this object. If the stream has been closed, this method is allowed to
	 * return <code>null</code>.
	 * 
	 * @return The current encoding name
	 */
	public String getEncoding() {
		return "ASCII";
	}

	/**
	 * This method checks to see if the stream is ready to be read. It will
	 * return <code>true</code> if is, or <code>false</code> if it is not.
	 * If the stream is not ready to be read, it could (although is not required
	 * to) block on the next read attempt.
	 * 
	 * @return <code>true</code> if the stream is ready to be read,
	 *         <code>false</code> otherwise
	 * 
	 * @exception IOException
	 *                If an error occurs
	 */
	public boolean ready() throws IOException {
		if (in == null)
			throw new IOException("Reader has been closed");

		return in.available() != 0;
	}

	/**
	 * This method reads up to <code>length</code> characters from the stream
	 * into the specified array starting at index <code>offset</code> into the
	 * array.
	 * 
	 * @param buf
	 *            The character array to recieve the data read
	 * @param offset
	 *            The offset into the array to start storing characters
	 * @param length
	 *            The requested number of characters to read.
	 * 
	 * @return The actual number of characters read, or -1 if end of stream.
	 * 
	 * @exception IOException
	 *                If an error occurs
	 */
	public int read(char[] buf, int offset, int length) throws IOException {
		byte[] bytes = new byte[length];
		int read = in.read(bytes);
		for (int i = 0; i < read; i++)
			buf[offset + i] = (char) (bytes[i] & 0xFF);
		return read;

	}

	/**
	 * Reads an char from the input stream and returns it as an int in the range
	 * of 0-65535. This method also will return -1 if the end of the stream has
	 * been reached.
	 * <p>
	 * This method will block until the char can be read.
	 * 
	 * @return The char read or -1 if end of stream
	 * 
	 * @exception IOException
	 *                If an error occurs
	 */
	public int read() throws IOException {
		char[] buf = new char[1];
		int count = read(buf, 0, 1);
		return count > 0 ? buf[0] : -1;
	}

	/**
	 * Skips the specified number of chars in the stream. It returns the actual
	 * number of chars skipped, which may be less than the requested amount.
	 * 
	 * @param count
	 *            The requested number of chars to skip
	 * 
	 * @return The actual number of chars skipped.
	 * 
	 * @exception IOException
	 *                If an error occurs
	 */
	public long skip(long count) throws IOException {
		if (in == null)
			throw new IOException("Reader has been closed");

		return super.skip(count);
	}
}
