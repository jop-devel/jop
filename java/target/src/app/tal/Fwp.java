/*
 * Created on 14.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

import util.Dbg;
import ejip.Packet;
import ejip.Udp;
import ejip.UdpHandler;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Fwp {

	private static final int FWP_PORT = 6261;
	
	private StringBuffer in, out;
	private StringBuffer usnr;
	private String version;
	private boolean lf;
	private int expBlock;
	
	
	
	//
	//	Vv ... Version request, reply
	//	Bpccnn ... Parameter download, cc block count, nn block number starting with 1
	//	
	public Fwp() {
		
		in = new StringBuffer();
		out = new StringBuffer();
		usnr = new StringBuffer();
		version = "Version: TAL261 1.01 vom 30.10.2006";
		lf = true;		// default for serial/modem
		usnr.append(Tal.par.usnr);
		
		UdpHandler uh;
		uh = new UdpHandler() {
			public void request(Packet p) {

				Udp.getData(p, in);
				handle();
				if (out.length()!=0) {
					Udp.setData(p, out);
					Udp.reply(p);
				} else{
					p.setStatus(Packet.FREE);
				}
			}
		};
		Udp.addHandler(FWP_PORT, uh);
	}

	void handle() {
		
		Dbg.lf();
		Dbg.wr('>');
		Dbg.wr(in);
		Dbg.lf();
		out.setLength(0);
		
		if (!checkIn(in)) {
			Dbg.wr("wrong checksum");
			out.append("ErrOR");
			return;
		}
		int off = getUsnr();
		char cmd = in.charAt(off);
		if (cmd	== 'B') {
			doParam(off+2);
		} else if (cmd=='A' || cmd=='T') {
			if (!Tal.par.ok) {
				out.append("*U");
				out.append(usnr);
				out.append("Vp");
				int sum = checkSum(out, out.length());
				out.append(int2hex(sum>>4));
				out.append(int2hex(sum));
				out.append('\r');
				if (lf) out.append('\n');
			} else {
				doTelegram();
			}
		} else if (cmd=='V') {
			doVersion();
		} else {
			Dbg.wr("wrong cmd");
			out.append("ERROR");
		}
		Dbg.lf();
		Dbg.wr('<');
		Dbg.wr(out);
		Dbg.lf();
	}
	
	public StringBuffer getXXX() {
		
		out.setLength(0);
		out.append("*U");
		out.append(usnr);
		out.append("Vp");

		int sum = checkSum(out, out.length());
		out.append(int2hex(sum>>4));
		out.append(int2hex(sum));
		out.append('\r');
		if (lf) out.append('\n');
		
		return out;
	}
	public StringBuffer getFwpString(int val) {

		int mel = val>>Alarm.MB_OFF;
		int stat1 = val>>Alarm.SB1_OFF;
		int stat2 = val>>Alarm.SB2_OFF;
		out.setLength(0);
		out.append("*U");
		out.append(usnr);
		out.append("Vm");
		out.append(int2hex(mel>>4));
		out.append(int2hex(mel));
		out.append('@');
		out.append(int2hex(stat1>>4));
		out.append(int2hex(stat1));
		out.append(int2hex(stat2>>4));
		out.append(int2hex(stat2));
		int sum = checkSum(out, out.length());
		out.append(int2hex(sum>>4));
		out.append(int2hex(sum));
		out.append('\r');
		if (lf) out.append('\n');
		
		return out;
	}

	private void doVersion() {
		out.append("*U");
		out.append(usnr);
		out.append("Vv");
		out.append(version);			
		int sum = checkSum(out, out.length());
		out.append(int2hex(sum>>4));
		out.append(int2hex(sum));
		out.append('\r');
		if (lf) out.append('\n');
	}

	private void doTelegram() {
		getFwpString(Alarm.getNewMsg());
	}

	private void doParam(int off) {
		
		int blocks = Param.readHexByte(in, off);
		int nr = Param.readHexByte(in, off+2);
		if (nr==1) {
			Tal.par.resetString();
			expBlock = 1;
		}
		if (nr==expBlock) {
			int len = in.length();
			for (int i = off+4; i < len; i++) {
				Tal.par.append(in.charAt(i));
			}
			++expBlock;
		}
		if (nr==blocks) {
			expBlock = 0;
			Tal.par.usnr.setLength(0);
			Tal.par.append("US_NR:");
			Tal.par.append(usnr);
			Tal.par.append(':');
			Tal.par.extract();
			Tal.par.save();
		}
		out.append("*\r");
		if (lf) out.append('\n');
	}
	
	/**
	 * @return
	 */
	private int getUsnr() {
		
		usnr.setLength(0);
		char c = in.charAt(5);
		usnr.append(in.charAt(2));
		usnr.append(in.charAt(3));
		usnr.append(in.charAt(4));
		if (c>='0' && c<='9') {
			usnr.append(in.charAt(5));
			return 6;
		} else {
			return 5;
		}
	}

	private int checkSum(StringBuffer s, int len) {
		
		int sum = 0;
		for (int i=0; i<len; ++i) {
			sum ^= s.charAt(i);
		}
		return sum;
	}
	
	private boolean checkIn(StringBuffer s) {
		
		int len = s.length();
		int i;
		for (i=0; i<len; ++i) {
			if (s.charAt(i)=='\r') {
				break;
			}
		}
		// no CR found
		if (i==len) return false;
		// too short
		if (len<7) return false;
		// FWP223 over serial/TAL is with "\r\n"
		// FWP223 over UDP is with "\r"!
		if ((i+1)<len && s.charAt(i+1)=='\n') {
			lf = true;
		} else {
			lf = false;
		}
		int sum = Param.readHexByte(s, i-2);
		int chk = checkSum(s, i-2);
		s.setLength(i-2);		// strip off checksum
		return sum == chk;
	}

	/**
	 * @param i
	 * @return
	 */
	private char int2hex(int i) {
		
		i &= 0x0f;
		if (i<10) {
			return (char) ('0'+i);
		} else {
			return (char) ('A'+i-10);
		}
	}
	/**
	 * @return
	 */
	public StringBuffer getIn() {
		return in;
	}

	/**
	 * @return
	 */
	public StringBuffer getOut() {
		return out;
	}

}
