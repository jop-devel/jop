/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) Martin Schoeberl   <martin@jopdesign.com>
                Thomas B. Preusser <thomas.preusser@tu-dresden.de>

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


public class Mul extends SerialBenchmark {

  public String toString() {
    return "iload_3 imul";
  }

/*
   12:  iload_2
   13:  iload_3
   14:  iadd
   15:  iload_3
   16:  imul
   17:  istore_2
*/
  public int perform(int cnt) {
    int  a = 0;
    int  b = 123;
    while(--cnt >= 0)  a = (a+b)*b;
    return  a;
  }

/*
   12:  iload_2
   13:  iload_3
   14:  iadd
   15:  istore_2
*/
  public int overhead(int cnt) {
    int  a = 0;
    int  b = 123;
    while(--cnt >= 0)  a = (a+b);
    return  a;
  }
}
