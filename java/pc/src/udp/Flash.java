/*
  This file is part of JOP, the Java Optimized Processor (http://www.jopdesign.com/)

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

/**
*	Flash programming via TFTP.
*
*	For new jopcore board with 512 KB flash.
*
*	three file types:
*
*		.ttf	ACEX config start at 0x60000
*				Cyclone config start at 0x40000
*		.bin	Java files start at 0x10000		(history of BB project)
*		.html	start at 0x00000
*		.class	start at 0x30000 (for Applet Tal.class)
*
*	Flash overview (in 64 KB sectors):
*
*		0x00000 :	Java program
*		0x10000 :	HTML or longer Java program
*		0x20000 :	user data, Streckendaten in oebb BG263
*		0x30000 :	Applet for TAL, bgid in oebb
*		0x40000 :	CYC config
*		0x50000 :	CYC config
*		0x60000 :	ACX, CYC config
*		0x70000 :	ACX, CYC config
*
*	Changelog:
*		2008-05-22	ACEX configuration disabled as we use a compressed bitstream for
*					the Cyclone and size detection does not work.
*/
package udp;
import java.io.*;
import java.util.*;

public class Flash extends FlashConst {

	protected static final int FLASH_SIZE = 0x80000;
	protected static final int SECTOR_SIZE = 0x10000;
	protected static final int SECTOR_MASKx = 0xf0000;
	protected static final int SECTOR_SHIFT = 16;

	protected static final int MAX_ACEX = 32768*3;	// 96kB (acex 1k50)
	protected static final int MAX_MEM = 65536*4;		// 256kB (Cyclone)
	protected static final int MAX_JAVA = 16384*4*2;	// max. 128KB
	protected static final int MAX_HTML = 1024;		// max. 1KB

	protected static final int START_JAVA = 0x00000;
	protected static final int START_HTML = 0x10000;
	protected static final int START_DATA = 0x20000;
	protected static final int START_CONFIG = 0x30000;
	protected static final int START_ACX_TTF = 0x60000;
	protected static final int START_CYC_TTF = 0x40000;
	
	protected int start;

	protected String fname;

	protected byte[] mem;
	protected int len;
	private Tftp tftp;

	public Flash(Tftp t, String fname) {

		tftp = t;
		this.fname = fname;
		mem = new byte[MAX_MEM];
		for (int i=0; i<MAX_MEM; ++i) mem[i] = (byte) 0xff;
	}

	public void read() {

		String line;
		StringTokenizer st;
		int pos;
		boolean isTtf = false;
		boolean isJava = false;
		boolean isHtml = false;
		boolean isAppl = false;
		int byteCnt = 0;

		len = 0;
		try {
			File f = new File(fname);
			String s = f.getName();
			pos = s.indexOf('.');
			if (pos==-1) {
				System.out.println("wrong filename");
				System.exit(-1);
			}
			s = s.substring(pos);
			if (s.equals(".jop") || s.equals(".bin")) {
				isJava = true;
				start= START_JAVA;
			} else if (s.equals(".html")) {
				isHtml = true;
				start= START_HTML;
			} else if (s.equals(".class")) {
				isAppl = true;
				start= START_CONFIG;
			} else if (s.equals(".ttf")) {
				isTtf = true;
				// TODO: should be changed to an option
//				start= START_ACX_TTF;		// assume a file for Jopcore (ACEX)
				start = START_CYC_TTF;
			} else {
				System.out.println("wrong file type: '"+s+"'");
				System.exit(-1);
			}

			if (isJava || isTtf) {

				BufferedReader in = new BufferedReader(new FileReader(f));

				while ((line = in.readLine()) != null) {

					if ((pos = line.indexOf('/'))!=-1) {
						st = new StringTokenizer(line.substring(0, pos), " \t\n\r\f,");
					} else {
						st = new StringTokenizer(line, " \t\n\r\f,");
					}
					while (st.hasMoreTokens()) {
						int val = Integer.parseInt(st.nextToken());
	
						if (isJava) {
							mem[len++] = (byte) (val>>>24);
							mem[len++] = (byte) (val>>>16);
							mem[len++] = (byte) (val>>>8);
							mem[len++] = (byte) val;
						} else {
							mem[len++] = (byte) val;
						}
	
						if (len==MAX_MEM) {
							System.out.println("too many words");
							System.exit(-1);
						}
						if (isJava && len==MAX_JAVA) {
							System.out.println("too many words for this Flash");
							System.exit(-1);
						}
					}
				}
				in.close();

//				if (len>MAX_ACEX) {							// file is big, so it must be for Cyclone
//					start= START_CYC_TTF;					// change start address
//				}

			} else {					// 'binary' file

				InputStream in = new FileInputStream(f);
				len = in.read(mem);
				if (in.available()!=0) {
					System.out.println("file to long");
					System.exit(-1);
				}
				if (isHtml) {
					mem[len] = 0;			// end character for html files
					++len;
				} else if (isAppl) {		// length of class file in first two bytes
					for (int i=len-1; i>=0; --i) {
						mem[i+2] = mem[i];
					}
					mem[0] = (byte) (len>>>8);
					mem[1] = (byte) len;
					len += 2;
				}
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		System.out.println(len+" bytes to program");
	}

	public void program() {

		int i, j;

		byte[] buf = new byte[SECTOR_SIZE];
		byte[] inbuf = new byte[SECTOR_SIZE];


		for (i=0; i<len; i+=SECTOR_SIZE) {

			int slen = SECTOR_SIZE;
			if (len-i<slen) slen = len-i;

			System.out.println("programming");
			for (j=0; j<slen ; ++j) {
				buf[j] = mem[i+j];
			}
			byte s = (byte) ('0'+((i+start)>>SECTOR_SHIFT));
System.out.println("sector "+(s-'0'));

			try {
				if (!tftp.write((byte) 'f', (byte) s, buf, slen)) {
					System.out.println();
					System.out.println("programming error");
					System.exit(-1);
				}
				System.out.println();
				System.out.println("compare");
				if (tftp.read((byte) 'f', s, inbuf)!=SECTOR_SIZE) {
					System.out.println();
					System.out.println("read error");
					System.exit(-1);
				}
			} catch (Exception e) {
				System.out.println(e);
				System.exit(-1);
			}

			for (j=0; j<slen; ++j) {
				if (buf[j] != inbuf[j]) {
					System.out.println("wrong data: "+(i+j)+" "+buf[j]+" "+inbuf[j]);
					System.exit(-1);
				}
			}
		}
		System.out.println("programming ok");
	}

	public static void main (String[] args) {

		if (args.length < 2) {
			System.out.println("usage: java Flash file host");
			System.exit(-1);
		}

		Flash fl = new Flash(new Tftp(args[1]), args[0]);
		fl.read();
		fl.program();
	}
}
