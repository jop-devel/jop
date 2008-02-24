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

package udp;
/**
*	BG id write.
*
*	write current time in sector 3
*
*	Bgid overview (in 64 KB sectors):
*
*		0x30000 :	Applet for TAL, bgid in oebb
*/

public class Bgid extends Flash {

	public Bgid(Tftp t) {
		super(t, "");
	}


	void setInt(int pos, int val) {

		pos *= 4;		// pos counts in 32 bit words
		mem[pos++] = (byte) (val>>>24);
		mem[pos++] = (byte) (val>>>16);
		mem[pos++] = (byte) (val>>>8);
		mem[pos++] = (byte) val;
		if (pos>len) len = pos;
	}

	public static void usage() {
		System.out.println("usage: java Bgid [master] host");
		System.exit(-1);
	}

	public static void main (String[] args) {

		boolean master = false;
		String host = "";

		
		if (args.length<1 || args.length>2) {
			usage();
		}
		if (args.length==2) {
			if (!args[0].equals("master")) {
				usage();
			} else {
				master = true;
			}
			host = args[1];
		} else {
			host = args[0];
		}

		Bgid fl = new Bgid(new Tftp(host));
		fl.start = START_CONFIG;
		fl.len = 0;
		int val = (int) (System.currentTimeMillis()/1000);
System.out.println("id: "+val);
		fl.setInt(CONFIG_ID, val);
		fl.setInt(CONFIG_NOTID, ~val);
		fl.setInt(CONFIG_CORE, 2);
		fl.setInt(CONFIG_IO, 0);
		fl.setInt(CONFIG_APP, 0);
		// logbook starts at byte addess 256
		// len is in words?
		fl.setInt(CONFIG_LEN, 64);
		fl.setInt(CONFIG_CHECK, 0);
		fl.setInt(CONFIG_IP_ADDR, 0);
		fl.setInt(CONFIG_IP_MASK, 0);
		fl.setInt(CONFIG_IP_GW, 0);
		val = master ? BG_MASTER_MAGIC : 0;
		fl.setInt(CONFIG_BG_MASTER, val);
		fl.setInt(CONFIG_TAL_PARAM, 0);
		fl.program();
	}
}
