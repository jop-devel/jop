/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen

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

package gctest;

import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker
// One must insert a log statement the last line in GC.push
// log("Adding to grey list", ref); 
// If the root scan is enableb it should not add the "ref"
// If it is disables for conservative scanning it should add it it

public class GCTest9 {

  public static void main(String s[]) {
	  System.out.println("GC");
	  int i = 53800; // this value depends on the handle area
	  GC.gc();
	} //main
}
