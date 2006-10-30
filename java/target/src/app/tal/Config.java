/*
 * Created on 20.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

import util.Amd;
import util.Dbg;

import com.jopdesign.sys.Native;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Config {

	private static final int START_CONFIG = 0x30000+0x80000;

	private static Config single;
	private int[] buf;
	private int len;
	
	private Config() {
		// we could use a buffer with config (len+3)/4 !!!
		buf = new int[16384];
		len = 0;
	}
	
	public static Config getInstance() {
		if (single==null) {
			single = new Config();
		}
		return single;
	}
	
	public int getInt(int pos) {
		
		pos <<= 2;			// pos counts in words
		int val = 0;
		for (int i=0; i<4; ++i) {
			val <<= 8;
			val += Native.rdMem(START_CONFIG+pos+i);
		}

		return val;
	}
	
	public void setInt(int pos, int val) {
		if (len==0) read();
		if (pos<0 || pos>16383) return;
		buf[pos] = val;
	}

	public void getString(int pos, StringBuffer s) {
		
		s.setLength(0);
		pos <<= 2;			// pos counts in words
		int val = 0;
		for (int i=0; i<300; ++i) {
			val = Native.rdMem(START_CONFIG+pos+i);
			if (val==0 || val==0xff) return;
			s.append((char) val);
		}
	}

	public void clearBuffer(int pos) {

		for (int i=pos; i<buf.length; ++i) {
			setInt(i, 0);
		}
	}

	public void setString(int pos, StringBuffer s) {

		if (len==0) read();
		if (pos<0 || pos>16383) return;
		int slen = s.length();		
		// copy buffer
		int k = 0;
		for (int i=0; i<slen+4; i+=4) {
			for (int j=0; j<4; ++j) {
				k <<= 8;
				if (i+j < slen) {
					k += s.charAt(i+j);
				}
			}
			setInt(pos+(i>>>2), k);
		}	
	}
	
	public void write() {
		if (len==0) return;		// nothing has changed!
		// Amd adds the 0x80000 offset!
		Amd.erase(START_CONFIG-0x80000);
		for (int i=0; i<len; ++i) {
			int val = buf[i];
			for (int j=0; j<4; ++j) {
				// Amd adds the 0x80000 offset!
				Amd.program(START_CONFIG-0x80000+(i<<2)+j, val>>(8*(3-j)));
			}
		}
	}

	private void read() {
		
		len = getInt(FlashConst.CONFIG_LEN);
		if (len<=0 || len>1000) len = 512; // some ? default
		
		for (int i=0; i<len; ++i) {
			buf[i] = getInt(i);
		}
	}
}
