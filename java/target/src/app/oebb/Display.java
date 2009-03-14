package oebb;

/**
*	Display.java: Display and Keyboard handling
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import joprt.*;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Display extends RtThread {

	static int[] fb;
	static int posRef;

	static boolean gpsOk;
	static boolean dgpsOk;
	static boolean inetOk;
	static boolean hbOk;

	static final int ESC = 0x1b;
	// Language for 7 bit encoding
	static final int FON = 0x52;
	static final int GER = 2;

	static final int CCT = 0x74;	// character code type
	static final int LAT = 16;		// WPC 1252
	static final int HOM = 0x0b;

	static final int FB_OFF = 4;

	Display(int prio, int period) {
		super(prio, period);
		init();
	}

	private static final int MAX_TMP = 32;
	private static int[] tmp;			// a generic buffer

	private static void init() {

		int i;

		tmp = new int[MAX_TMP];

/*
		Native.wr(1, Native.IO_DISP);									// display reset
		sleepMs(5);
		Native.wr(0, Native.IO_DISP);									// display operate
		sleepMs(100);
*/

		fb = new int[20*4+FB_OFF];
		posRef = 0;

		initMsg();

	}

	public static void initMsg() {

		clear();
		String s = "ZLB-01 System   EEG";
		write(0, s);
		s = "Programm Ver: ";
		int i = s.length();
		write(20, s);
		write(20+i, Main.VER_MAJ+'0');
		write(20+i+1, '.');
		write(20+i+2, (Main.VER_MIN/10)+'0');
		write(20+i+3, (Main.VER_MIN%10)+'0');
		s = "Strecken Ver: ";
		i = s.length();
		write(40, s);
		intVal(40+i, Flash.getVer());
	}

	public static void setGpsOk(boolean val) {
		gpsOk = val;
		setStatus();
	}
	public static void setDgpsOk(boolean val) {
		dgpsOk = val;
		setStatus();
	}
	public static void setInetOk(boolean val) {
		inetOk = val;
		setStatus();
	}
	
	public static void setHbOk(boolean val) {
		hbOk = val;
		setStatus();
	}

	private static void setStatus() {

		fb[19+FB_OFF] = gpsOk ? '*' : ' ';
		fb[39+FB_OFF] = dgpsOk ? '+' : ' ';
		fb[59+FB_OFF] = inetOk ? '#' : ' ';
		if (Main.state!=null && Main.state.isDownloading()) {
			fb[59+FB_OFF] = 'D';			
		} else if (hbOk) {
			fb[59+FB_OFF] = 'H';
		}
	}
			

	/**
	*	Clear display, set status info.
	*/
	public static void clear() {

		fb[0] = HOM;
		fb[1] = ESC;
		fb[2] = CCT;
		fb[3] = LAT;
		for (int i=0; i<20*4; ++i) {
			fb[i+FB_OFF] = ' ';
		}
		setStatus();
	}

	public static void write(int pos, int[] buf) {

		int i = buf.length;
		int j;
		int end;

		if (pos < 20) {
			end = 19;
		} else if (pos < 40) {
			end = 39;
		} else {
			end = 59;
		}

		for (j=0; j<i; ++j) {
			fb[pos+j+FB_OFF] = buf[j];					// in [0] is cursor home
		}
		for (; pos+j < end; ++j) {
			fb[pos+j+FB_OFF] = ' ';
		}
	}

	/**
	*	write a String at position x and clear till character position 19
	*/
	public static void write(int pos, String s) {

		int i = s.length();
		int j;
		int end;

		if (pos < 20) {
			end = 19;
		} else if (pos < 40) {
			end = 39;
		} else {
			end = 59;
		}

		for (j=0; j<i; ++j) {
			fb[pos+j+FB_OFF] = s.charAt(j);					// in [0] is cursor home
		}
		for (; pos+j < end; ++j) {
			fb[pos+j+FB_OFF] = ' ';
		}
	}

	/**
	*	write a String and an integer at position x and clear till character position 19
	*/
	public static void write(int pos, String s, int val) {

		int i = s.length();
		int j;
		int end;

		if (pos < 20) {
			end = 19;
		} else if (pos < 40) {
			end = 39;
		} else {
			end = 59;
		}

		for (j=0; j<i; ++j) {
			fb[pos+j+FB_OFF] = s.charAt(j);					// in [0] is cursor home
		}

		pos += j;

		if (val<0) {
			write(pos++, '-');
			val = -val;
		}
		for (i=0; i<MAX_TMP-1; ++i) {
			tmp[i] = (val%10)+'0';
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			write(pos++, tmp[val]);
		}
		for (j=0; pos+j < end; ++j) {
			fb[pos+j+FB_OFF] = ' ';
		}
	}

	/**
	*	write a StringBuffer at position x and clear till character position 19
	*/
	public static void write(int pos, StringBuffer s) {

		int i = s.length();
		int j;
		int end;

		if (pos < 20) {
			end = 19;
		} else if (pos < 40) {
			end = 39;
		} else {
			end = 59;
		}

		for (j=0; j<i; ++j) {
			fb[pos+j+FB_OFF] = s.charAt(j);					// in [0] is cursor home
		}
		for (; pos+j < end; ++j) {
			fb[pos+j+FB_OFF] = ' ';
		}
	}

	/**
	*	write a StringBuffer and an integer at position x and clear till character position 19
	*/
	public static void write(int pos, StringBuffer s, int val) {

		int i = s.length();
		int j;
		int end;

		if (pos < 20) {
			end = 19;
		} else if (pos < 40) {
			end = 39;
		} else {
			end = 59;
		}

		for (j=0; j<i; ++j) {
			fb[pos+j+FB_OFF] = s.charAt(j);					// in [0] is cursor home
		}

		pos += j;

		if (val<0) {
			write(pos++, '-');
			val = -val;
		}
		for (i=0; i<MAX_TMP-1; ++i) {
			tmp[i] = (val%10)+'0';
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			write(pos++, tmp[val]);
		}
		for (j=0; pos+j < end; ++j) {
			fb[pos+j+FB_OFF] = ' ';
		}
	}

	public static void write(int pos, int ch) {

		fb[pos+FB_OFF] = ch;
	}

	public static void intVal(int pos, int val) {
		
		int i;
		if (val<0) {
			write(pos++, '-');
			val = -val;
		}
		for (i=0; i<MAX_TMP-1; ++i) {
			tmp[i] = (val%10)+'0';
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			write(pos++, tmp[val]);
		}
	}

	public static void ipVal(int pos, int val) {

		for (int i=0; i<4; ++i) {
			int b = (val>>((3-i)*8)) & 0xff;
			write(pos++, b/100+'0');
			write(pos++, b/10%10+'0');
			write(pos++, b%10+'0');
			if (i<3) write(pos++, '.');
		}
	}

	public static void hexVal(int val) {

		int i, j;
	}

	/**
	*	Write three lines.
	*/
	public static void write(String l1, String l2, String l3) {

		write(0, l1);
		write(20, l2);
		write(40, l3);
	}

	public static void write(String l1, int v1, String l2, String l3) {

		write(0, l1, v1);
		write(20, l2);
		write(40, l3);
	}

	public static void write(String l1, String l2, int v2, String l3) {

		write(0, l1);
		write(20, l2, v2);
		write(40, l3);
	}

	public static void write(String l1, int v1, String l2, int v2, String l3) {

		write(0, l1, v1);
		write(20, l2, v2);
		write(40, l3);
	}



	/**
	*	Write three lines.
	*/
	public static void write(StringBuffer l1, StringBuffer l2, StringBuffer l3) {

		write(0, l1);
		write(20, l2);
		write(40, l3);
	}

	/**
	*	Write three lines.
	*/
	public static void write(String l1, StringBuffer l2, StringBuffer l3) {

		write(0, l1);
		write(20, l2);
		write(40, l3);
	}

	/**
	*	Write three lines.
	*/
	public static void write(String l1, StringBuffer l2, String l3) {

		write(0, l1);
		write(20, l2);
		write(40, l3);
	}

	public void run() {

		int i, j;

		for (;;) {

			// try to write 3 characters to the disp-uart
			// buffer (usualla two will fit)
			// busy goes high after each character for two ms
			for (j=0; j<3; ++j) {
				if ((Native.rd(Const.IO_DISP) & 0x01) == 1) {
					i = posRef;
					Native.wr(fb[i], Const.IO_DISP+1);
					++i;
					// display only three lines
					if (i==(60+FB_OFF)) i = 0;
					posRef = i;
				}
			}

			Keyboard.loop();

			waitForNextPeriod();
		}
	}

}
