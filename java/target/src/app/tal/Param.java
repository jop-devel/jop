/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * Created on 14.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

import joprt.RtThread;
import util.Dbg;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Param {
	
	private StringBuffer string;
	private StringBuffer tmp;

	public StringBuffer usnr;	
	public int mask;
	public int[] time;
	public int disconn;
	public StringBuffer modem;
	public int cntTel;
	public StringBuffer[] telnr;
	public boolean ok;
	
	public Param() {
		
		int i;
		string = new StringBuffer(100);
		tmp = new StringBuffer();

		usnr = new StringBuffer();
// usnr.append("4101");
		mask = 0;
		time = new int[8];
		for (i=0; i<8; ++i) time[i] = 0;
		modem = new StringBuffer();
		disconn = 0;
		cntTel = 0;
		telnr = new StringBuffer[3];
		for (i = 0; i < 3; i++) {
			telnr[i] = new StringBuffer();
		}
		cntTel = 0;
		// TODO .....
// telnr[0].append("t022833900924");
// ok = true;

		Config conf = Config.getInstance();
		conf.getString(FlashConst.CONFIG_TAL_PARAM, string);
		if (string.length()==0) {
			string.append("no parameter");
/*
string.setLength(0);
Dbg.wr("set test parameter\n");
string.append("US_NR:123:TEL_NR:1:4711:IO_PAR:00:00:00:00:00:00:01:02:03:");
Dbg.wr(string);
Dbg.lf();
*/
			conf.setString(FlashConst.CONFIG_TAL_PARAM, string);
			conf.write();
		}
		extract();
		ok = cntTel != 0;
	}
	public void resetString() {
		string.setLength(0);
	}
	public void append(char ch) {
		string.append(ch);
	}
	public void append(StringBuffer s) {
		string.append(s);
	}
	public void append(String s) {
		string.append(s);
	}
	
	/**
	 * Take the parameter string and extract parameter.
	 */
	public void extract() {
		
		int pos = 0;
		int len = string.length();
		while (pos<len) {
			pos = extractString(pos, tmp);
			if (is(tmp,"US_NR")) {
				pos = extractString(pos, usnr);
			} else if (is(tmp,"IO_PAR")) {
				pos = extractString(pos, tmp);
				mask = readHexByte(tmp, 0);
				for (int i=0; i<8; ++i) {
					pos = extractString(pos, tmp);
					time[i] = readInt(tmp);					
				}
			} else if (is(tmp,"TEL_NR")) {
				pos = extractString(pos, tmp);
				cntTel = readInt(tmp);
				if (cntTel > 3) cntTel = 0;
				for (int i = 0; i<cntTel; i++) {
					pos = extractString(pos, telnr[i]);					
				} 
			} else if (is(tmp,"PAGERNR")) {
				// we don't care
			} else if (is(tmp,"MODEM")) {
				pos = extractString(pos, tmp);
				disconn = readInt(tmp);
				pos = extractString(pos, modem);				
			} else if (is(tmp,"TEXTE")) {
				// we don't care
			}
		}
	
		dump();
	}
	/**
	 * 
	 */
	public void dump() {
		
		Dbg.wr("Parameter are: \n");
		Dbg.wr(usnr);
		Dbg.lf();
		Dbg.intVal(mask);
		for (int i=0; i<8; ++i) Dbg.intVal(time[i]);
		Dbg.lf();
		Dbg.intVal(disconn);
		Dbg.wr(modem);
		Dbg.lf();
		for (int i = 0; i < cntTel; i++) {
			Dbg.wr(telnr[i]);
			Dbg.lf();
		}

	}

	/**
	 * @param c
	 * @return
	 */
	public static int hex2int(char c) {
		
		if (c>='0' && c<='9') {
			return c-'0';
		} else if (c>='a' && c<='f') {
			return c-'a'+10;
		} else if (c>='A' && c<='F') {
			return c-'A'+10;
		}
		return 0;
	}
	public static int readHexByte(StringBuffer s, int i) {
		
		int sum = (hex2int(s.charAt(i))<<4)+
				hex2int(s.charAt(i+1));
		return sum;
	}
	/**
	 * @param tmp
	 * @param string
	 * @return
	 */
	static boolean is(StringBuffer tmp, String string) {
		// TODO equals in StringBuffer
		int max = string.length()-1;
		for (int i = 0; i < tmp.length(); i++) {
			if (i>max || tmp.charAt(i)!=string.charAt(i)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * @param tmp
	 * @return
	 */
	private int readInt(StringBuffer tmp) {
		
		int val = 0;
		
		for (int i = 0; i < tmp.length(); i++) {
			val *= 10;
			val += tmp.charAt(i)-'0';
		}
		return val;
	}
	/**
	 * @param pos
	 * @param tmp
	 * @return
	 */
	private int extractString(int pos, StringBuffer tmp) {
		
		tmp.setLength(0);
		int len = string.length();
		for (; pos<len; ++pos) {
			char c = string.charAt(pos);
			if (c==':') {
				++pos;
				break;
			}
			tmp.append(c);
		}
		// trim the string
		for (len=tmp.length(); len>0; --len) {
			if (tmp.charAt(len-1)!=' ') break;
		}
		tmp.setLength(len);
		return pos;
	}
	/**
	 * 
	 */
	public void save() {

Dbg.wr("parameter string=");
Dbg.wr(string);
Dbg.lf();
		Config conf = Config.getInstance();
		conf.clearBuffer(FlashConst.CONFIG_TAL_PARAM);
		conf.setString(FlashConst.CONFIG_TAL_PARAM, string);
Dbg.wr("write parameter and stop the WD\n");
Dbg.lf();
		conf.write();
		Tal.stop();
	}
	
	public void erase() {
		string.setLength(0);
		save();
	}
	

}
