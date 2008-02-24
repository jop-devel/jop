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
*	A simple clock and calendar.
*/

public class Clock {

	private static int next;
	private static int s;
	private static int day;
	private static int year;
	private static boolean leap;
	private static int[] mtab;
	private static int[] buf;

/*
	public static void main(String args[]) {


		Display.line1();
		for (int i=0; i<20; ++i) Display.data(' ');
		Display.line2();
		for (int i=0; i<20; ++i) Display.data(' ');

		Clock.init();
		Timer.start();

		for (;;) {
			if (Clock.loop()) {
				Display.line1();

				disp02(year/100);
				disp02(year%100);
				Display.data('-');
				disp02(getMonth());
				Display.data('-');
				disp02(getDay());

				Display.data(' ');
				int i = s;

				disp02(i/3600);
				Display.data(':');
				i %= 3600;
				disp02(i/60);
				Display.data(':');
				disp02(i%60);
			}
			Timer.waitForNextInterval();
		}
	}

	private static void disp02(int i) {

		Display.data('0'+(i/10));
		Display.data('0'+(i%10));
	}
*/

	public static void init() {

		mtab = new int[12];
/*	dup_x2 fehlt
		mtab[0] = mtab[2] = mtab[4] = mtab[6] = mtab[7] = mtab[9] = mtab[11] = 31;
		mtab[3] = mtab[5] = mtab[8] = mtab[10] = 30;
		mtab[1] = 28;
*/
		mtab[0] = 31;
		mtab[1] = 28;
		mtab[2] = 31;
		mtab[3] = 30;
		mtab[4] = 31;
		mtab[5] = 30;
		mtab[6] = 31;
		mtab[7] = 31;
		mtab[8] = 30;
		mtab[9] = 31;
		mtab[10] = 30;
		mtab[11] = 31;

		buf = new int[20];

		setDate(2002, 01, 01);
		setTime(00, 00, 00);

		next = JopSys.rd(JopSys.IO_CNT)+JopSys.ONE_SECOND;
	}

	public static boolean loop() {

		if (next-JopSys.rd(JopSys.IO_CNT) < 0) {

			++s;
			if (s==86400) {
				s = 0;
				++day;
				if ((leap && day==367) || (!leap && day==366)) {
					day = 1;
					++year;
					leap = leapYear(year);
					mtab[1] = leap ? 29 : 28;
				}
			}
			next += JopSys.ONE_SECOND;
			return true;
		}
		return false;
	}

	public static boolean setDate(int y, int m, int d) {

		boolean oldLeap = leap;

		if (m<1 || m>12) return false;
		leap = leapYear(y);
		mtab[1] = leap ? 29 : 28;
		if (d<1 || d>mtab[m-1]) {
			leap = oldLeap;
			mtab[1] = leap ? 29 : 28;
			return false;
		}
		year = y;
		day = 0;
		for (int i=0; i<m-1; ++i) {
			day += mtab[i];
		}
		day += d;
		return true;
	}

	public static boolean setTime(int hour, int min, int sec) {

		if (hour<0 || hour>23) return false;
		if (min<0 || min>59) return false;
		if (sec<0 || sec>59) return false;
		s = hour*3600 + min*60 +sec;
		return true;
	}

	public static int getYear() {
		return year;
	}

	public static int getMonth() {
	
		int m;
		int d = mtab[0];
		for (m=1; m<12 && d<day; ++m) {
			d += mtab[m];
		}
		return m;
	}

	public static int getDay() {
	
		int m;
		int d = mtab[0];
		for (m=1; m<12 && d<day; ++m) {
			d += mtab[m];
		}
		return day-d+mtab[m-1];
	}

	public static int getSec() {

		return s;
	}


	public static void getDate(int[] buf) {

		int i;

		buf[4] = '-';
		buf[7] = '-';
		buf[10] = ' ';
		buf[13] = ':';
		buf[16] = ':';
		buf[19] = ' ';

		buf[0] = '0'+year/1000;
		buf[1] = '0'+year%1000/100;
		buf[2] = '0'+year%100/10;
		buf[3] = '0'+year%10;
		i = getMonth();
		buf[5] = '0'+i/10;
		buf[6] = '0'+i%10;
		i = getDay();
		buf[8] = '0'+i/10;
		buf[9] = '0'+i%10;
		i = s/3600;
		buf[11] = '0'+i/10;
		buf[12] = '0'+i%10;
		i = s%3600/60;
		buf[14] = '0'+i/10;
		buf[15] = '0'+i%10;
		i = s%60;
		buf[17] = '0'+i/10;
		buf[18] = '0'+i%10;
		buf[19] = ' ';

	}

	private static boolean leapYear(int y) {

		if (y%4 == 0) {
			if (y%100 == 0) {
				if (y%400 == 0) {
					return true;
				}
				return false;
			}
			return true;
		}
		return false;
	}


/*
	public static void main(String[] args) {

		init();

		System.out.println(setDate(2000, 1, 1)+" "+day+" "+getMonth()+" "+getDay());
		System.out.println(setDate(2000, 0, 1)+" "+day+" "+getMonth()+" "+getDay());
		System.out.println(setDate(2000, 1, 31)+" "+day+" "+getMonth()+" "+getDay());
		System.out.println(setDate(2000, 2, 1)+" "+day+" "+getMonth()+" "+getDay());
		System.out.println(setDate(2000, 12, 31)+" "+day+" "+getMonth()+" "+getDay());
		System.out.println(setDate(2000, 2, 29)+" "+day+" "+getMonth()+" "+getDay());
		System.out.println(setDate(2001, 2, 29)+" "+day+" "+getMonth()+" "+getDay());
		System.out.println(setDate(2001, 12, 31)+" "+day+" "+getMonth()+" "+getDay());

		setDate(2000, 1, 1);
		for (day=1; day<368; ++day) {
			System.out.println(getYear()+"-"+getMonth()+"-"+getDay());
		}
		setDate(2001, 1, 1);
		for (day=1; day<368; ++day) {
			System.out.println(getYear()+"-"+getMonth()+"-"+getDay());
		}
	}
*/
}
