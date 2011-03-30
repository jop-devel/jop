/*
 * Copyright (c) Martin Schoeberl, martin@jopdesign.com
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
 *	This product includes software developed by Martin Schoeberl
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
 * 
 */
package jembench.ejip;

/**
 * A simple logger for the ejip stack.
 * 
 * @author Martin Schoeberl
 *
 */
public class Logging {
	
	public static final boolean LOG = false;
	
	private static final int MAX_TMP = 32;
	private static int[] tmp = new int[MAX_TMP];			// a generic buffer

	
	static void wr(char c) {
		System.out.print(c);
	}

	static void wr(String string) {
		System.out.print(string);
	}
	
	static void wr(StringBuffer sb) {
		System.out.println(sb);
	}

	public static void intVal(int val) {

		int i;
		int sign = 1;
		if (val<0) {
			wr('-');
			sign = -1;
		}
		for (i=0; i<MAX_TMP-1; ++i) {
			tmp[i] = ((val%10)*sign)+'0';
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wr((char) tmp[val]);
		}
		wr(' ');
	}
	
	public static void lf() {
		System.out.println();
	}
	
	public static void hexVal(int val) {

		int i, j;
		if (val<16 && val>=0) wr('0');
		for (i=0; i<MAX_TMP-1; ++i) {
			j = val & 0x0f;
			if (j<10) {
				j += '0';
			} else {
				j += 'a'-10;
			}
			tmp[i] = j;
			val >>>= 4;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wr((char) tmp[val]);
		}
		wr(' ');
	}
	
	public static void byteVal(int val) {

		int j;
		j = (val>>4) & 0x0f;
		if (j<10) { j += '0'; } else { j += 'a'-10; }
		wr((char) j);
		j = val & 0x0f;
		if (j<10) { j += '0'; } else { j += 'a'-10; }
		wr((char) j);
		wr(' ');
	}

}
