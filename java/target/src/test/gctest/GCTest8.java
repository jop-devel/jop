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
// TODO: Why does the led not turn red. JOP writes "Stack overflow" to the serial line.

public class GCTest8 {

  public static void main(String s[]) {
	  
    int c = 0;
		for(int i=0;i<1000;i++){
			c++;
			//GC.gc(); // This line makes it work
			System.out.println("no crash.no crash.no crash.no crash.no crash.no crash.no crash.no crash.no crash:"+c);
		}
		System.out.println("Test 8 ok");
	} //main
}
