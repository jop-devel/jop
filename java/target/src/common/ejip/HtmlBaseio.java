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

package ejip;


import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
*	A very SIMPLE HTML server class. Generates dynamic content
*	from the BaseIO I/O ports.
*/

public class HtmlBaseio extends SimpleHttp {


	public StringBuffer val;
	private int hits;
	private int[] outVal;

	private int[] valArr;

	public StringBuffer msg;

	private static final int MAX_MSG = 80;

	private String text;

	public HtmlBaseio() {
		
		val = new StringBuffer(5);
		msg = new StringBuffer(MAX_MSG);
		outVal = new int[1];

		hits = 0;
		outVal[0] = 0;
		Native.wr(outVal[0], Const.IO_OUT);

		text =
/*
				"<html><head></head><body><h2>BG 263</h2>"+
				"Some Communication Statistics<p>"+
				"sent packets #0<br>"+
				"sent bytes #1<br>"+
				"rcvd packets #2<br>"+
				"rcvd bytes #3<br>"+
				"wrong packets #4<br>"+
				"</body></html>";
*/


			"<html><head></head><body>"+
//			"<h2>TAL TeleAlarm</h2>"+
			"<h2><a href=\"http://www.jopdesign.com/\">JOP</a> Web Server on Altera Cyclone EP1C6</h2>"+
			"Analog 1: !a1 mA<br>"+
			"Analog 2: !a2 mA<br>"+
			"Vbat: !a3 V<p>"+
			"input 1: !i1<br>"+
			"input 2: !i2<br>"+
			"input 3: !i3<br>"+
			"input 4: !i4<br>"+
			"input 5: !i5<br>"+
			"input 6: !i6<br>"+
			"input 7: !i7<br>"+
			"input 8: !i8<br>"+
			"input 9: !i9<br>"+
			"input 10: !i:<br>"+
			"<form method=\"get\">"+
			"output 1: !o1 <input type=\"checkbox\" name=\"o1\">set 1<br>"+
			"output 2: !o2 <input type=\"checkbox\" name=\"o2\">set 2<br>"+
			"output 3: !o3 <input type=\"checkbox\" name=\"o3\">set 3<br>"+
			"output 4: !o4 <input type=\"checkbox\" name=\"o4\">set 4"+
			"<p><input type=\"submit\" value=\"Set outputs\">"+
			"</form>"+
			"<p><sub>!ht</sub>"+
			"</body></html>";
	}

	void setContent(StringBuffer sb, StringBuffer cmd) {
		sb.setLength(0);
//		sb.append("<html><head></head><body><h2>Hello xxx World!</h2>");
//		sb.append(cmd);
//		sb.append("</body></html>\r\n\r\n");
		
		char c = cmd.charAt(5);
		
		if (c=='T') {								// request for 'Tal.class'
//			ret = setClassFile(tmp);
		} else {
			
			// do the I/O stuff
			processRequest(cmd, 5);

			if (c=='d') {							// request for 'data.txt'
				setData(sb);
			} else {
				++hits;
				for (int i=0; i<1000; ++i) {
					char ch1 = getChar(i);
					if (ch1==0) break;				// EOF reached

					if (ch1=='!') {
						++i;
						ch1 = getChar(i);
						++i;
						char ch2 = getChar(i);
						setSpecial(sb, ch1, ch2);
					} else if (ch1=='#') {
						++i;
						ch1 = (char) (getChar(i)-'0');
						if (valArr!=null && ch1>=0 && ch1<valArr.length) {
							sb.append(valArr[ch1]);
						}
					} else {
						sb.append(ch1);
					}
				}
			}
			sb.append("\r\n\r\n");
		}
	}

/**
*	set value array
*/
	public void setValArray(int[] vals) {

		valArr = vals;
	}
	public void setOutValArray(int[] out) {
		
		outVal = out;
	}

	private StringBuffer getTemp() {

		int i = Native.rd(Const.IO_ADC1);
		i &= 0xffff;
		i = (i-600)/17+27;

		if (i>99) i = 99;

		val.setLength(5);
		if (i<0) {
			i = -i;
			val.setCharAt(0, '-');
		} else {
			val.setCharAt(0, ' ');
		}
		val.setCharAt(1, (char) ('0'+i/10));
		val.setCharAt(2, (char) ('0'+i%10));
		val.setCharAt(3, ' ');
		val.setCharAt(4, ' ');

		return val;
	}

	private StringBuffer getAnalog(int channel) {

		int i = 0;
		if (channel==1) {
			i = Native.rd(Const.IO_ADC1);	// I = ADCout * 3.3 / (100 * (2^16-1))
			i *= 100;
			i /= 19859;
		} else if (channel==2) {
			i = Native.rd(Const.IO_ADC2);
			i *= 100;
			i /= 19859;
		} else if (channel==3) {
			i = Native.rd(Const.IO_ADC3);	// U = 11 * ADCout * 3.3 / (2^16-1)
			i *= 100;
			i /= 18054;
		}
		// value is now in 1/10 mA or 1/10 V

		// setInt(i, val);
/*
		i += 100;
		i /= 201;
*/
		val.setLength(5);
		val.setCharAt(4, ' ');
		val.setCharAt(3, (char) ('0'+i%10));
		val.setCharAt(2, '.');
		i /= 10;
		val.setCharAt(1, (char) ('0'+i%10));
		val.setCharAt(0, (char) ('0'+i/10));

		return val;
	}

	private StringBuffer getHit() {

		val.setLength(5);
		val.setCharAt(0, (char) ('0'+hits/10000%10));
		val.setCharAt(1, (char) ('0'+hits/1000%10));
		val.setCharAt(2, (char) ('0'+hits/100%10));
		val.setCharAt(3, (char) ('0'+hits/10%10));
		val.setCharAt(4, (char) ('0'+hits%10));

		return val;
	}

	private StringBuffer getDigital() {

		val.setLength(0);
		val.append(Native.rd(Const.IO_IN));
		return val;
	}

	private void setMsg(StringBuffer sb, int pos) {

		msg.setLength(0);
		for (int i=pos; i<sb.length(); ++i) {
			char ch = sb.charAt(pos+i);
			if (ch==' ') break;
			if (ch=='+') ch=' ';
			msg.append(ch);
		}
	}

	private static final int HTML_START = 0x90000;	// start at first address (should be changed!)

	private char getChar(int pos) {

		// data from Flash
		// return Native.rdMem(HTML_START+pos);
		// data from String
		if (pos>=text.length()) return 0;
		return text.charAt(pos);
	}


	/**
	 * Set the output values (or the message ?)
	 */
	private void processRequest(StringBuffer cmd, int pos) {

		// param was int[] buf
		if (cmd.charAt(pos+0)!='?') return;		// nothing to do!

		int i = pos+1;

		if (cmd.charAt(i)=='m') {
			setMsg(cmd, i+2);
		} else {							// set/reset outValuts
			outVal[0] = 0;
			for(; i<100; ++i) {
				if (cmd.charAt(i)=='o') {
					++i;
					int j = cmd.charAt(i)-'1';
					if (j>=0 && j<=3) {
						outVal[0] |= 1<<j;
					}
				} else if (cmd.charAt(i)=='\r') {
					break;
				}
			}
	
			Native.wr(outVal[0], Const.IO_OUT);
		}
	}

	private void setSpecial(StringBuffer buf, char ch1, char ch2) {

		int i, j;

		if (ch1=='m') {
			buf.append(msg);
		} else if (ch1=='a') {
			buf.append(getAnalog(ch2-'0'));
		} else if (ch1=='t') {
			buf.append(getTemp());
		} else if (ch1=='h') {
			buf.append(getHit());
		} else if (ch1=='i') {
			i = Native.rd(Const.IO_IN);
			j = ch2-'1';
			if ((i&(1<<j))!=0) {
				buf.append("on");
			} else {
				buf.append("off");
			}
		} else if (ch1=='o') {
			i = outVal[0];
			j = ch2-'1';
			if ((i&(1<<j))!=0) {
				buf.append("on");
			} else {
				buf.append("off");
			}
		}
	}

	private void setData(StringBuffer buf) {

		buf.append(getAnalog(1));
		buf.append(getAnalog(2));
		buf.append(getDigital());
		buf.append(' ');
		buf.append(getHit());
	}

	private static final int APPL_START = 0x80000 + 0x30000;	// start of class file

	// load Applet Tal.class
	private int setClassFile(int[] buf) {

		int ret = Native.rdMem(APPL_START)<<8;
		ret += Native.rdMem(APPL_START+1);
		for (int i=0; i<ret; ++i) {
			buf[i] = Native.rdMem(APPL_START+2+i);
		}

		return ret;
	}
}
