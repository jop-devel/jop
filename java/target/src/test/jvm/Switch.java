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

package jvm;

public class Switch extends TestCase {
	
	public String toString() {
		return "Switch";
	}
	
	public boolean test() {

		boolean ok = true;
		
		ok = ok && (sw(2)==20);
		ok = ok && (sw(3)==3);
		ok = ok && (sw(4)==4);
		ok = ok && (sw(5)==5);
		ok = ok && (sw(6)==60);

		ok = ok && (lsw(0)==0);
		ok = ok && (lsw(1)==1);
		ok = ok && (lsw(4)==40);
		ok = ok && (lsw(5)==5);
		ok = ok && (lsw(6)==60);
		ok = ok && (lsw(7)==7);

		return ok;
	}

	public static int sw(int i) {

		int x = 999;
		switch (i) {
			case 3:
				x = 3;
				break;
			case 4:
				x = 4;
				break;
			case 5:
				x = 5;
				break;
			default:
				x = i*10;
		}
		
		return x;
	}

	public static int lsw(int i) {

		int x = 0;
		switch (i) {
			case 1:
				x = 1;
				break;
			case 7:
				x = 7;
				break;
			case 5:
				x = 5;
				break;
			default:
				x = i*10;
		}
		return x;
	}
}
