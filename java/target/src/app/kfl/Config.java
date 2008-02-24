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

package kfl;

/**
*	Mast Konfigurationsdaten.
*
*/

public class Config {

//
//	offsets of data in flash
//
	private static final int OFF_MSCNT = 0;
	private static final int OFF_LANG = 1;

	private static final int MS_LEN = 32;
	private static final int OFF_MAXCNT = 0;
	private static final int OFF_DWNCNT = 2;
	private static final int OFF_UPCNT = 3;


	public static int getCnt() {

		int i = Flash.read(Flash.MS_DATA+OFF_MSCNT);
		if (i<0 || i>15) i = 0;
		return i;
	}

	public static void setCnt(int cnt) {

		if (cnt<0 || cnt>15) return;
		Flash.write(Flash.MS_DATA+OFF_MSCNT, cnt);
	}

	public static int getLang() {

		int i = Flash.read(Flash.MS_DATA+OFF_LANG);
		if (i<0 || i>1) i = 0;
		return i;
	}

	public static void setLang(int i) {

		if (i<0 || i>1) i = 0;
		Flash.write(Flash.MS_DATA+OFF_LANG, i);
	}
/**
*	ms is 1 based
*/
	public static int getMSmaxCnt(int ms) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_MAXCNT;
		return Flash.read16(i);
	}

	public static void setMSmaxCnt(int ms, int val) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_MAXCNT;
		Flash.write16(i, val);
	}

	public static int getMSdwnCnt(int ms) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_DWNCNT;
		return Flash.read(i);
	}

	public static void setMSdwnCnt(int ms, int val) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_DWNCNT;
		Flash.write(i, val);
	}

	public static int getMSupCnt(int ms) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_UPCNT;
		return Flash.read(i);
	}

	public static void setMSupCnt(int ms, int val) {

		int i = Flash.MS_DATA+ms*MS_LEN+OFF_UPCNT;
		Flash.write(i, val);
	}
}
