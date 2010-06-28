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

package jembench.micro;

import jembench.SerialBenchmark;


public class GetStatic extends SerialBenchmark {

  private static volatile int  VAL = 4711;

  public String toString() {
    return "getstatic iadd";
  }

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	getstatic	#2; //Field VAL:I
   20:	iadd
   21:	istore_2
*/
  public int perform(int cnt) {
    int  a = 0;
    int  b = 123;
    while(--cnt >= 0)  a = a+b+VAL;
    return  a;
  }

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	istore_2
*/
  public int overhead(int cnt) {
    int  a = 0;
    int  b = 123;
    while(--cnt >= 0)  a = a+b;
    return  a;
  }
			
}
