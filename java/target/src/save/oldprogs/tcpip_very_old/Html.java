package tcpip_old;

import com.jopdesign.sys.*;
import util.*;

public class Html {

	private static final int IO_INOUT = 0;

	public static int[] prot;
	public static int[] end;

	public static int[] on;
	public static int[] off;

	public static int[] val;
	public static int[] tmp;
	private static int hits;
//	private static int outVal;		use this without test.SmsTest.outVal!!!

	public static int[] msg;

	private static final int MAX_MSG = 80;

	public static void init() {

		init1();

		val = new int[5];
		tmp = new int[6];
		msg = new int[MAX_MSG];
		for (int i=0; i<MAX_MSG; ++i) msg[i] = ' ';
		hits = 1923;
		test.SmsTest.outVal = 1;
		Native.wr(test.SmsTest.outVal, IO_INOUT);
	}

	private static void init1() {
		int[] s1 = {'H', 'T', 'T', 'P', '/', '1', '.', '0', ' ', '2', '0', '0', ' ', 'O', 'K', '\r', '\n', '\r', '\n'};
		int[] s2 = {'\r', '\n', '\r', '\n'};
		int[] s3 = {'o', 'n'};
		int[] s4 = {'o', 'f', 'f'};
		prot = s1;
		end = s2;
		on = s3;
		off = s4;
	}

	private static int append(int[] buf, int pos, int[] str) {

		int ret;
		for (ret=0; ret<str.length; ++ret) {
			buf[pos+ret] = str[ret];
		}
		return ret;
	}

	public static final int IO_ADC = 4;

	private static int[] getTemp() {

		int i = Native.rd(IO_ADC);
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

		int i = Native.rd(IO_ADC);
		if (channel==1) {
			i >>>= 16;
		}
		i &= 0xffff;

		// setInt(i, val);
		i += 100;
		i /= 201;
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

		for (j=0; j<6; ++j) {
			val[j] = ' ';
		}
		val[0] = '0'+hits/1000%10;
		val[1] = '0'+hits/100%10;
		val[2] = '0'+hits/10%10;
		val[3] = '0'+hits%10;
		val[4] = ' ';

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

	private static final int HTML_START = 0x80000;	// start at first address (should be changed!)

	public static int setText(int[] buf, int req_pos, int req_len, int pos) {

		int i, j, k;

		++hits;

//Dbg.wr('\n');
//Dbg.wr('h');
//Dbg.wr('t');
//Dbg.wr('m');
//Dbg.wr('l');
//Dbg.wr(':');
//Dbg.intVal(hits);

//Dbg.wr('\n');
//for (i=0; i<req_len; ++i) Dbg.wr(buf[req_pos+i]);


		processRequest(buf, req_pos+5);

		int ret = 0;

		ret += append(buf, pos+ret, prot);


		for (i=0; i<1000; ++i) {
			j = Native.rdMem(HTML_START+i);
			if (j==0) break;				// EOF reached

			if (j=='!') {
				++i;
				j = Native.rdMem(HTML_START+i);
				++i;
				k = Native.rdMem(HTML_START+i);
				ret += setSpecial(buf, pos+ret, j, k);
			} else {
				buf[pos+ret] = j;
				++ret;
			}
		}

		ret += append(buf, pos+ret, end);
		return ret;
	}

	private static void processRequest(int[] buf, int pos) {

		if (buf[pos+0]!='?') return;		// nothing to do!

		int i = pos+1;

		if (buf[i]=='m') {
			setMsg(buf, i+2);
		} else {							// set/reset test.SmsTest.outValuts
			test.SmsTest.outVal = 0;
			for(; i<100; ++i) {
				if (buf[i]=='o') {
					++i;
					int j = buf[i]-'1';
					if (j>=0 && j<=3) {
						test.SmsTest.outVal |= 1<<j;
					}
				} else if (buf[i]=='\r') {
					break;
				}
			}
	
			Native.wr(test.SmsTest.outVal, IO_INOUT);
		}
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
			i = Native.rd(IO_INOUT);
			j = ch2-'1';
			if ((i&(1<<j))!=0) {
				return append(buf, pos, off);
			} else {
				return append(buf, pos, on);
			}
		} else if (ch1=='o') {
			i = test.SmsTest.outVal;
			j = ch2-'1';
			if ((i&(1<<j))!=0) {
				return append(buf, pos, on);
			} else {
				return append(buf, pos, off);
			}
		}

		return 0;
	}

}
