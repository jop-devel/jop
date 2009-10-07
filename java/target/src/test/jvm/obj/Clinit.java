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

/*
 * Created on 26.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jvm.obj;

import jvm.TestCase;

public class Clinit extends TestCase {

static int abc = 123;
static int def = 456;

static int[] a = { 0, 1, 2, 3, 4, 5, -1 };

static int[] b = { 123, -123, 456, -456, 50000, -50000 };

static Object o = new Object();

	public String toString() {
		return "Clinit";
	}

	public boolean test() {		

		boolean ok = true;

		int val = abc+def;

		if (val!=123+456) ok = false;

		if (a[0]!=0) ok = false;
		if (a[1]!=1) ok = false;
		if (a[2]!=2) ok = false;
		if (a[3]!=3) ok = false;
		if (a[4]!=4) ok = false;
		if (a[5]!=5) ok = false;
		if (a[6]!=-1) ok = false;

		if (b[0]!=123) ok = false;
		if (b[1]!=-123) ok = false;
		if (b[2]!=456) ok = false;
		if (b[3]!=-456) ok = false;
		if (b[4]!=50000) ok = false;
		if (b[5]!=-50000) ok = false;
		
		if (o==null) ok = false;

		return ok;

	}
}
