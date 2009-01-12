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

package ejip_old;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
*	a very SIMPLE HTML server class.
*/

public class Html {


	public static String prot;
	public static String end;

	public static int[] val;
	public static int[] tmp;
	private static int hits;
	private static int[] outVal;

	private static int[] valArr;

	public static int[] msg;

	private static final int MAX_MSG = 80;

	private static String text;

/**
*	allocate buffers.
*/
	public static void init() {

		val = new int[5];
		tmp = new int[1500];
		msg = new int[MAX_MSG];
		outVal = new int[1];

		for (int i=0; i<MAX_MSG; ++i) msg[i] = ' ';
		// hits = 17800;	// 10.3.2003 auch jetzt bei ACEX
		// hits = 18320;		// 14.3.2003
		// hits = 18750;		// 20.3.2003
		// hits = 19720;		// 31.3.2003
		// hits = 19990;		// 10.4.2003
		// hits = 22030;		// 15.5.2003
		// hits = 27472;		// 22.7.2003
		// hits = 30104;		// 23.9.2003
		// hits = 30390;		// 1.10.2003
		// hits = 30477;		// 6.10.2003
		// hits = 32914;		// 13.2.2004
		// hits = 37038;		// 27.4.2004
		// hits = 38175;		// 13.7.2004
		// hits = 38800;		// 17.8.2004
		hits = 39291;			// 9.9.2004
		hits = 0;
		outVal[0] = 0;
		Native.wr(outVal[0], Const.IO_OUT);

		text =
/*
			"<html><head></head><body>"+
			"<h2>Hej Rasmus and DDM class</h2>"+
			"<h3>Greetings from Vienna.</h3>"+
			"<h3>Have fun,</h3>"+
			"<h3>Martin</h3>"+
			"</body></html>";
*/
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

		prot = "HTTP/1.0 200 OK\r\n\r\n";
		end = "\r\n\r\n";
	}

/**
*	set value array
*/
	public static void setValArray(int[] vals) {

		valArr = vals;
	}
	public static void setOutValArray(int[] out) {
		
		outVal = out;
	}

	private static int append(int[] buf, int pos, String str) {

		int ret, len;

		len = str.length();
		for (ret=0; ret<len; ++ret) {
			buf[pos+ret] = str.charAt(ret);
		}
		return ret;
	}

	private static int append(int[] buf, int pos, int[] str) {

		int ret;
		for (ret=0; ret<str.length; ++ret) {
			buf[pos+ret] = str[ret];
		}
		return ret;
	}


	private static int[] getTemp() {

		int i = Native.rd(Const.IO_ADC1);
//		Dbg.intVal(i>>>16);
		i &= 0xffff;
//		Dbg.wr('T');
//		Dbg.intVal(i);
		i = (i-600)/17+27;
//		Dbg.intVal(i);

		if (i>99) i = 99;

		if (i<0) {
			i = -i;
			val[0] = '-';
		} else {
			val[0] = ' ';
		}
		val[1] = '0'+i/10;
		val[2] = '0'+i%10;
		val[3] = ' ';
		val[4] = ' ';

		return val;
	}

	private static void setInt(int val, int[] buf) {

		int i;

		for (i=buf.length-1; i>=0; --i) {
			buf[i] = val%10+'0';
			val /= 10;
			if (val==0) break;
		}
		--i;
		for (; i>=0; --i) {
			buf[i] = ' ';
		}
	}

	private static int[] getAnalog(int channel) {

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
		val[4] = ' ';
		val[3] = '0'+i%10;
		val[2] = '.';
		i /= 10;
		val[1] = '0'+i%10;
		val[0] = '0'+i/10;

		return val;
	}

	private static int[] getHit() {

		int j;

		for (j=0; j<5; ++j) {
			val[j] = ' ';
		}
		val[0] = '0'+hits/10000%10;
		val[1] = '0'+hits/1000%10;
		val[2] = '0'+hits/100%10;
		val[3] = '0'+hits/10%10;
		val[4] = '0'+hits%10;

		return val;
	}

	private static int[] getDigital() {

		setInt(Native.rd(Const.IO_IN), val);
		return val;
	}

	private static void setMsg(int[] buf, int pos) {

		int i, j;
		for (i=0; i<MAX_MSG; ++i) {
			msg[i] = ' ';
		}
// Dbg.wr('m');
// Dbg.wr(':');
		for (i=0; i<MAX_MSG; ++i) {
			j = buf[pos+i];
			if (j==' ') break;
			if (j=='+') j=' ';
			msg[i] = j;
//Dbg.wr(j);
		}
	}

	private static final int HTML_START = 0x90000;	// start at first address (should be changed!)

	private static int getChar(int pos) {

		// data from Flash
		// return Native.rdMem(HTML_START+pos);
		// data from String
		if (pos>=text.length()) return 0;
		return text.charAt(pos);
	}

	public static int setText(int[] buf, int req_pos, int req_len, int ret_pos) {

		int i, j, k;


//Dbg.wr('\n');
//Dbg.wr('h');
//Dbg.wr('t');
//Dbg.wr('m');
//Dbg.wr('l');
//Dbg.wr(':');
//Dbg.intVal(hits);



		// copy request to 'byte' buffer
		for (i=0; i<req_len; i+=4) {
			j = buf[req_pos+(i>>2)];
			tmp[i] = j>>>24;
			tmp[i+1] = (j>>>16)&0xff;
			tmp[i+2] = (j>>>8)&0xff;
			tmp[i+3] = j&0xff;
		}
Dbg.wr('\n');
for (i=0; i<req_len; ++i) Dbg.wr(tmp[i]);

		int ret = 0;
		if (tmp[0]!='G' || tmp[1]!='E') return 0;

		k = tmp[5];
		if (k=='T') {								// request for 'Tal.class'
			ret = setClassFile(tmp);
		} else {
			
			processRequest(tmp, 5);
			ret += append(tmp, ret, prot);

			if (k=='d') {							// request for 'data.txt'
				ret += setData(tmp, ret);
			} else {
				++hits;
				for (i=0; i<1000; ++i) {
					j = getChar(i);
					if (j==0) break;				// EOF reached

					if (j=='!') {
						++i;
						j = getChar(i);
						++i;
						k = getChar(i);
						ret += setSpecial(tmp, ret, j, k);
					} else if (j=='#') {
						++i;
						j = getChar(i)-'0';
						if (valArr!=null && j>=0 && j<valArr.length) {
							ret += setVal(tmp, ret, valArr[j]);
						}
					} else {
						tmp[ret] = j;
						++ret;
					}
				}
			}

			ret += append(tmp, ret, end);

		}
		// copy replay to word buffer
/*
Dbg.wr('\n');
for (i=0; i<ret; ++i) Dbg.wr(tmp[i]);
Dbg.wr('\n');
*/
		tmp[ret] = 0;					// make shure last bytes are 0 for checksum
		tmp[ret+1] = 0;
		tmp[ret+2] = 0;
		k = 0;
		for (i=0; i<(ret+3)>>2; ++i) {
			j = (tmp[k]<<24) + (tmp[k+1]<<16) +
				(tmp[k+2]<<8) + tmp[k+3];
			buf[ret_pos+i] = j;
			k += 4;
		}
		return ret;
	}

/**
*	Handle one HTTP request.
*/
	private static void processRequest(int[] buf, int pos) {

		if (buf[pos+0]!='?') return;		// nothing to do!

		int i = pos+1;

		if (buf[i]=='m') {
			setMsg(buf, i+2);
		} else {							// set/reset outValuts
			outVal[0] = 0;
			for(; i<100; ++i) {
				if (buf[i]=='o') {
					++i;
					int j = buf[i]-'1';
					if (j>=0 && j<=3) {
						outVal[0] |= 1<<j;
					}
				} else if (buf[i]=='\r') {
					break;
				}
			}
	
			Native.wr(outVal[0], Const.IO_OUT);
		}
	}

	private static int setVal(int[] buf, int pos, int val) {

		int j;

		for (j=0; j<5; ++j) {
			buf[pos+j] = ' ';
		}
		buf[pos+0] = '0'+val/1000000%10;
		buf[pos+1] = '0'+val/100000%10;
		buf[pos+2] = '0'+val/10000%10;
		buf[pos+3] = '0'+val/1000%10;
		buf[pos+4] = '0'+val/100%10;
		buf[pos+5] = '0'+val/10%10;
		buf[pos+6] = '0'+val%10;

		return 7;
	}

	private static int setSpecial(int[] buf, int pos, int ch1, int ch2) {

		int i, j;

		if (ch1=='m') {
			return append(buf, pos, msg);
		} else if (ch1=='a') {
			return append(buf, pos, getAnalog(ch2-'0'));
		} else if (ch1=='t') {
			return append(buf, pos, getTemp());
		} else if (ch1=='h') {
			return append(buf, pos, getHit());
		} else if (ch1=='i') {
			i = Native.rd(Const.IO_IN);
			j = ch2-'1';
			if ((i&(1<<j))!=0) {
				return append(buf, pos, "on");
			} else {
				return append(buf, pos, "off");
			}
		} else if (ch1=='o') {
			i = outVal[0];
			j = ch2-'1';
			if ((i&(1<<j))!=0) {
				return append(buf, pos, "on");
			} else {
				return append(buf, pos, "off");
			}
		}

		return 0;
	}

	private static int setData(int[] buf, int pos) {

		int ret = 0;
		ret += append(buf, pos+ret, getAnalog(1));
		ret += append(buf, pos+ret, getAnalog(2));
		ret += append(buf, pos+ret, getDigital());
		buf[pos+ret] = ' '; ++ret;
		ret += append(buf, pos+ret, getHit());

		return ret;
	}

	private static final int APPL_START = 0x80000 + 0x30000;	// start of class file

	// load Applet Tal.class
	private static int setClassFile(int[] buf) {

		int ret = Native.rdMem(APPL_START)<<8;
		ret += Native.rdMem(APPL_START+1);
		for (int i=0; i<ret; ++i) {
			buf[i] = Native.rdMem(APPL_START+2+i);
		}

		return ret;
	}
}
