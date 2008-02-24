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
*	calculate 'real' value for sigma delta ADC with NTC.
*/

public class Temp {

	private static int[] tab;		// starts with -55o C for real value

	public static void init() {

		int[] x = { -1,
			17287, 
			17296, 
			17307, 
			17322, 
			17342, 
			17369, 
			17405, 
			17450, 
			17510, 
			17586, 
			17684, 
			17806, 
			17958, 
			18145, 
			18373, 
			18650, 
			18994, 
			19397, 
			19887, 
			20470, 
			21162, 
			21976, 
			22927, 
			24036, 
			25315, 
			26792, 
			28510, 
			30485, 
			32747, 
			35329, 
			38237, 
			41527, 
			45218, 
			49364, 
		};
		tab = x;
	}

	public static int calc(int val) {

		int i, t;
		int x, y;

		t = -60;
		for (i=0; val>tab[i]; ++i) {
			t += 5;
		}
		x = val-tab[i];
		y = tab[i+1] - tab[i];
		t += 5*x/y;

		return t;
	}
}
