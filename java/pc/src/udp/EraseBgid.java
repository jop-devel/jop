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
*	Erase sector with bgid to test correction in BG.
*
*	write -1 in sector 3
*
*	Bgid overview (in 64 KB sectors):
*
*		0x30000 :	Applet for TAL, bgid in oebb
*/

public class EraseBgid extends Bgid {

	public EraseBgid(Tftp t) {
		super(t);
	}


	public static void usage() {
		System.out.println("usage: java EraseBgid host");
		System.exit(-1);
	}

	public static void main (String[] args) {

		boolean master = false;
		String host = "";

		
		if (args.length!=1) {
			usage();
		}
		host = args[0];

		EraseBgid fl = new EraseBgid(new Tftp(host));
		fl.start = START_CONFIG;
		fl.len = 0;
		fl.setInt(CONFIG_ID, -1);
		fl.program();
	}
}
