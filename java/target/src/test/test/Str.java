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

package test;

import util.Dbg;

/**
*	Str.java ... test internal strings
*/

public class Str {

	public static void main(String[] args) {

		int i, j, k;

		char[] x = new char[10];

		x[0] = 'a';

		Dbg.initSer();

		f("ABC");
		f("XYZ0123");
		f("ABC");
		f("AB");
		f("uni\u2297xyz");
		f("last");
		f(new String());

		Dbg.wr('a');

		for (;;) ;
	}

	static void f(String s) {

		int i = s.length();
		for (int j=0; j<i; ++j) {
			Dbg.wr(s.charAt(j));
		}
	}
}
