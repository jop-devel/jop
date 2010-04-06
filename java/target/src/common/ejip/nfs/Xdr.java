/*
 * Copyright (c) Daniel Reichhard, daniel.reichhard@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by Daniel Reichhard
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

/**
 * This is the data-marshalling class for primitive xdr-types
 */
package ejip.nfs;

/**
 * @author Daniel Reichhard
 * 
 */
public class Xdr {

	/**
	 * read an integer at position n (non-consuming)
	 * 
	 * @param dataBuffer
	 *            the buffer to read from
	 * @param n
	 *            the position in the buffer
	 * @return the integer
	 */
	public static int getIntAt(StringBuffer dataBuffer, int n) {
		int retval;
		if (((dataBuffer.length() - (4 * n)) / 4) > 0) {
			retval = dataBuffer.charAt(4 * n + 3)
					| dataBuffer.charAt(4 * n + 2) << 8
					| dataBuffer.charAt(4 * n + 1) << 16
					| dataBuffer.charAt(4 * n) << 24;
			return retval;
		}
		return 0; // error return value
	}

	/**
	 * get next integer out of the <code>dataBuffer</code> (and consume it)
	 * 
	 * @param dataBuffer
	 *            the buffer to read from
	 * @return the integer 
	 */
	public static int getNextInt(StringBuffer dataBuffer) {
		int retval;
		retval = getIntAt(dataBuffer, 0);
		dataBuffer.delete(0, 4);
		return retval;
	}

	public static long getNextLong(StringBuffer dataBuffer) {
		long retval;
		retval = getIntAt(dataBuffer, 0) << 32 | getIntAt(dataBuffer, 1);
		dataBuffer.delete(0, 8);
		return retval;
	}

	public static void getBytes(StringBuffer dataBuffer,
			StringBuffer targetBuffer, int size) {
		int fillBytes;
		if ((size > 0) & (dataBuffer.length() >= size)) {
			for (int i = 0; i < size; i++) {
				targetBuffer.append(dataBuffer.charAt(i));
			}

			fillBytes = 4 - (size % 4);

			if (fillBytes != 4) {
				size += fillBytes;
			}

			dataBuffer.delete(0, size);
		}
	}

	/**
	 * get next string out of the <code>dataBuffer</code> (and consume it)
	 * 
	 * @param dataBuffer
	 *            the buffer to read from
	 * @return the string
	 */
	public static void getNextStringBuffer(StringBuffer dataBuffer,
			StringBuffer targetBuffer) {
		int length;

		length = Xdr.getNextInt(dataBuffer);
		if (targetBuffer == null) {
			dataBuffer.delete(0, length);
		} else {
			targetBuffer.setLength(0);
			getBytes(dataBuffer, targetBuffer, length);
		}
	}

	/**
	 * append an integer to the <code>dataBuffer</code>
	 * 
	 * @param dataBuffer
	 *            the buffer to appended to
	 * @param i
	 *            the integer to append
	 */
	public static void append(StringBuffer dataBuffer, int i) {
		// if (i <= 0x7FFFFFFF) {
		dataBuffer.append((char) ((i & 0xFF000000) >>> 24));
		dataBuffer.append((char) ((i & 0xFF0000) >> 16));
		dataBuffer.append((char) ((i & 0xFF00) >> 8));
		dataBuffer.append((char) ((i & 0xFF)));
		// return true;
		// } else {
		// return false;
		// }
	}

	/**
	 * append a long integer to the <code>dataBuffer</code>
	 * 
	 * @param dataBuffer
	 *            the buffer to appended to
	 * @param i
	 *            the long integer to append
	 */
	public static void append(StringBuffer dataBuffer, long i) {
		// if (i <= 0x7FFFFFFF) {
		dataBuffer.append((char) ((i >>> 56) & 0xFF));
		dataBuffer.append((char) ((i >> 48) & 0xFF));
		dataBuffer.append((char) ((i >> 40) & 0xFF));
		dataBuffer.append((char) ((i >> 32) & 0xFF));
		dataBuffer.append((char) ((i >> 24) & 0xFF));
		dataBuffer.append((char) ((i >> 16) & 0xFF));
		dataBuffer.append((char) ((i >> 8) & 0xFF));
		dataBuffer.append((char) (i & 0xFF));
		// return true;
		// } else {
		// return false;
		// }
	}

	/**
	 * append an integer array to the <code>dataBuffer</code>
	 * 
	 * @param dataBuffer
	 *            the buffer to appended to
	 * @param i
	 *            the integer array to append
	 */
	public static void append(StringBuffer dataBuffer, int[] i) {
		for (int j = 0; j < i.length; j++) {
			if (i[j] == 0) {
				Xdr.append(dataBuffer, j);
				j = i.length;
			}
		}

		for (int j = 0; j < i.length; j++) {
			if (i[j] != 0) { // TODO: is 0 a valid group?
				Xdr.append(dataBuffer, i[j]);
			}
		}
	}

	/**
	 * append a StringBuffer s to the <code>dataBuffer</code>
	 * 
	 * @param dataBuffer
	 *            the buffer to appended to
	 * @param s
	 *            the String to append
	 */
	public static void append(StringBuffer dataBuffer, StringBuffer s) {
		Xdr.append(dataBuffer, s.length());
		Xdr.appendRaw(dataBuffer, s);
	}

	/**
	 * append a StringBuffer as Opaque with fixed size
	 * 
	 * @param dataBuffer
	 * @param s
	 */
	public static void appendRaw(StringBuffer dataBuffer, StringBuffer s) {
		int rest;
		for (int i = 0; i < s.length(); i++) { //@WCA loop=9
			dataBuffer.append(s.charAt(i));
		}

		rest = 4 - (s.length() % 4);

		while ((rest != 4) & (rest > 0)) { //@WCA loop<=3
			dataBuffer.append((char) 0);
			rest--;
		}
	}

}
