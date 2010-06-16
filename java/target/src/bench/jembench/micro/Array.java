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


public class Array extends SerialBenchmark {

  private final static int[]  ARRAY = new int[1024];
  static {
    for(int  i = 1024; --i >= 0; ARRAY[i] = i + 27);
  }

  public String toString() {
    return "aload iaload";
  }

  /*
   13:  iload_3
   14:  aload_2
   15:  iload_1
   16:  sipush  1023
   19:  iand
   20:  iaload
   21:  iadd
   22:  istore_3
  */
  public int perform(int cnt) {
    final int[]  arr = ARRAY;

    int  a = 0;
    while(--cnt >= 0)  a += arr[cnt&0x3ff];
    return  a;
  }

  /*
   9:   iload_2
   10:  iload_1
   11:  sipush  1023
   14:  iand
   15:  iadd
   16:  istore_2
  */
  public int overhead(int cnt) {
    int a = 0;
    while(--cnt >= 0)  a += cnt&0x3ff;
    return  a;
  }

}
