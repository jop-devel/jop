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
*	Cache.java ... test program for cache
*/

public class Cache {

	public static void main(String[] args) {

//f();
		Dbg.initSer();

		StringBuffer s1, s2, s3;

		s1 = new StringBuffer();
		s2 = new StringBuffer();
		s3 = new StringBuffer();

		for (int i=0;;++i) {
			s1.setLength(0);
			s2.setLength(0);
			s3.setLength(0);
			dofun();

			doit(s1, s2, s3);
			Dbg.wr(s3);
			Dbg.intVal(i);
			Dbg.wr('\r');
		}

	}

	static void doit(StringBuffer s1, StringBuffer s2, StringBuffer s3) {
		s1.append("Hello ");
		s2.append("World! ");
		s3.append(s1);
		s3.append(s2);
	}
	static void dofun() {
		a();
		b();
		c();
		d();
	}
	static void f() {

		int a = 1;
		int b = 2;
		int c = a+b+a+b;
		for (;;) {
			a();
			b();
			c();
		}
	}

	static void a() {
		int a = 1;
		int b = 2;
		int c = a+b+a;
		a = b+c;
		b = a+c;
	}
	static void b() {
		int a = 1;
		int b = 2;
		int c = a+b+a+b;
		a = b+c;
		b = a+c;
	}
	static void c() {
		int a = 1;
		int b = 2;
		int c = a+b+a+b;
		a = b+c;
		b = a+c;
	}
	static int d() {
		int a = 123;
		int b = 45;
		return a/b + a%b;
	}

}
