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

package wcet;

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class ManyLoops {

  /**
   * @param args
   */
  public static void main(String[] args) {
    loop(false,0);
    loop11(true, 123);
    loop2(true, 123);
    loop1(true, 123);
    
    loop9(true, 123);
    loop10(true, 123);
    loop11(true, 123);
    // System.out.println("hej");
  }

  //
   public static int loop(boolean b, int val) {
    for (int i = 0; i < 10; ++i) { // @WCA loop=10
      if (b) {
        for (int j = 0; j < 3; ++j) { // @WCA loop=3
          val *= val;
        }
        return val;
      } else {
        for (int j = 0; j < 7; ++j) { // @WCA loop=7
          val += val;

        }
        return val;
      }
    }
    return val;
  }
  
   public static int loop1(boolean b, int val) {
    val = 1;
    for (; val < 2 & val < 3;) { // @WCA loop=2
      int l = 7;
      if (l == 6)
        break;
      if (l == 5)
        break;
      l = 3;
    }
    return val;
  }
  
  public static int loop2(boolean b, int val) {
    for (; val < 2;) { // @WCA loop=2
      val++;
    }

    loop3(b, val);

    return val;
  }

  //
  public static int loop3(boolean b, int val) {
    int test = 6;
    for (int i = 0; i < 2; i++) { // @WCA loop=20
      val++;
    }
    int ol = 9;

    return loop4(b, val);
  }

  public static int loop4(boolean b, int val) {
    for (int i = 0; i < 2; i++) { // @WCA loop=2
      val += loop5(b, val);
    }
    return val;
  }

  public static int loop5(boolean b, int val) {
    val = 1;
    for (int i = 8; val < 2 & val < 1;) { // @WCA loop=2

      int l = 7;
      if (l == 6)
        break;
      if (l == 5)
        return val;
      l = 3;
    }
    loop6(b, loop7(b, val));
    return val;
  }

  public static int loop6(boolean b, int val) {
    for (int i = 8; val < 2 && val < 1;) { // @WCA loop=2
      val++;
    }
    return val;
  }

  public static int loop7(boolean b, int val) {
    for (int i = 8; val < 2 && val < 1;) { // @WCA loop=2

    }
    loop8(b, val);
    return val;
  }

  //
  public static int loop8(boolean b, int val) {
    for (int i = 8; val < 2 && val < 1;) { // @WCA loop=2
      if (true)
        ;
    }
    return val;
  }

  //
  public static int loop9(boolean b, int val) {
    for (int i = 8; val < 1;) { // @WCA loop=7
      for (int j = 80; val < 1;) { // @WCA loop=80

      }

    }
    return val;
  }

  public static int loop10(boolean b, int val) {
    for (int i = 8; val < 1;) { // @WCA loop=10
      val++;
    }
    return val;
  }

  public static int loop11(boolean b, int val) {
    val++;// System.out.println("hello");
    if (val > 8)
      val--;

    val = loop12(b, val);

    return val;
  }

  public static int loop12(boolean b, int val) {
    val++;// System.out.println("hello");
    return val;
  }

}
