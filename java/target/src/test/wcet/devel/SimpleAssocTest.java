/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)
                      Benedikt Huber   (benedikt.huber@gmail.com)

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
package wcet.devel;

/**
 * Testing associativity and line size for the object cache WCET analysis.
 * 
 * objs_i_j() accesses i objects with j fields each
 *
 * measure_i_j() invokes objs_i_j() in a loop
 * measure() invokes all objs_i_j() in a loop
 * 
 * @author martin
 *
 */
public class SimpleAssocTest {
    static class Obj {
        int f1;
        int f2;
        int f3;
        int f4;
        int f5;
    }
    /* objects */
    static Obj objs[] = new Obj[10];
    static volatile int out;
    static int seed_w = 234;
    static int seed_z = 567;
    /* random gen from http://en.wikipedia.org/wiki/Random_number_generation */
    static int nrand() {
        seed_z = 36969 * (seed_z & 65535) + (seed_z >>> 16);
        seed_w = 18000 * (seed_w & 65535) + (seed_w >> 16);
        return (seed_z << 16) + seed_w;
    }

    static void init() {
        for(int i = 0; i < 10; i++) {
            objs[i] = new Obj();
            objs[i].f1 = nrand();
            objs[i].f2 = nrand();
            objs[i].f3 = nrand();
            objs[i].f4 = nrand();
            objs[i].f5 = nrand();
        }

    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        SimpleAssocTest test = new SimpleAssocTest();
        init();
        test.measure();
    }
	
    /**
     * for i,j in (1,1) ... (10,5)
     *   loop_i_j();
     * 
     */
 static void measure() {
      loop_1_1(objs[0]);
      loop_1_2(objs[0]);
      loop_1_3(objs[0]);
      loop_1_4(objs[0]);
      loop_1_5(objs[0]);
      loop_2_1(objs[0],objs[1]);
      loop_2_2(objs[0],objs[1]);
      loop_2_3(objs[0],objs[1]);
      loop_2_4(objs[0],objs[1]);
      loop_2_5(objs[0],objs[1]);
      loop_3_1(objs[0],objs[1],objs[2]);
      loop_3_2(objs[0],objs[1],objs[2]);
      loop_3_3(objs[0],objs[1],objs[2]);
      loop_3_4(objs[0],objs[1],objs[2]);
      loop_3_5(objs[0],objs[1],objs[2]);
      loop_4_1(objs[0],objs[1],objs[2],objs[3]);
      loop_4_2(objs[0],objs[1],objs[2],objs[3]);
      loop_4_3(objs[0],objs[1],objs[2],objs[3]);
      loop_4_4(objs[0],objs[1],objs[2],objs[3]);
      loop_4_5(objs[0],objs[1],objs[2],objs[3]);
      loop_5_1(objs[0],objs[1],objs[2],objs[3],objs[4]);
      loop_5_2(objs[0],objs[1],objs[2],objs[3],objs[4]);
      loop_5_3(objs[0],objs[1],objs[2],objs[3],objs[4]);
      loop_5_4(objs[0],objs[1],objs[2],objs[3],objs[4]);
      loop_5_5(objs[0],objs[1],objs[2],objs[3],objs[4]);
  }
  static void measure_1_1(Obj obj0) {
    loop_1_1(obj0);
  }
  static void loop_1_1(Obj obj0) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1;
    }
    out = sum;
  }
  static void measure_1_2(Obj obj0) {
    loop_1_2(obj0);
  }
  static void loop_1_2(Obj obj0) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2;
    }
    out = sum;
  }
  static void measure_1_3(Obj obj0) {
    loop_1_3(obj0);
  }
  static void loop_1_3(Obj obj0) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3;
    }
    out = sum;
  }
  static void measure_1_4(Obj obj0) {
    loop_1_4(obj0);
  }
  static void loop_1_4(Obj obj0) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4;
    }
    out = sum;
  }
  static void measure_1_5(Obj obj0) {
    loop_1_5(obj0);
  }
  static void loop_1_5(Obj obj0) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4+obj0.f5;
    }
    out = sum;
  }
  static void measure_2_1(Obj obj0,Obj obj1) {
    loop_2_1(obj0,obj1);
  }
  static void loop_2_1(Obj obj0,Obj obj1) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1;
     sum += obj1.f1;
    }
    out = sum;
  }
  static void measure_2_2(Obj obj0,Obj obj1) {
    loop_2_2(obj0,obj1);
  }
  static void loop_2_2(Obj obj0,Obj obj1) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2;
     sum += obj1.f1+obj1.f2;
    }
    out = sum;
  }
  static void measure_2_3(Obj obj0,Obj obj1) {
    loop_2_3(obj0,obj1);
  }
  static void loop_2_3(Obj obj0,Obj obj1) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3;
     sum += obj1.f1+obj1.f2+obj1.f3;
    }
    out = sum;
  }
  static void measure_2_4(Obj obj0,Obj obj1) {
    loop_2_4(obj0,obj1);
  }
  static void loop_2_4(Obj obj0,Obj obj1) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4;
    }
    out = sum;
  }
  static void measure_2_5(Obj obj0,Obj obj1) {
    loop_2_5(obj0,obj1);
  }
  static void loop_2_5(Obj obj0,Obj obj1) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4+obj0.f5;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4+obj1.f5;
    }
    out = sum;
  }
  static void measure_3_1(Obj obj0,Obj obj1,Obj obj2) {
    loop_3_1(obj0,obj1,obj2);
  }
  static void loop_3_1(Obj obj0,Obj obj1,Obj obj2) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1;
     sum += obj1.f1;
     sum += obj2.f1;
    }
    out = sum;
  }
  static void measure_3_2(Obj obj0,Obj obj1,Obj obj2) {
    loop_3_2(obj0,obj1,obj2);
  }
  static void loop_3_2(Obj obj0,Obj obj1,Obj obj2) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2;
     sum += obj1.f1+obj1.f2;
     sum += obj2.f1+obj2.f2;
    }
    out = sum;
  }
  static void measure_3_3(Obj obj0,Obj obj1,Obj obj2) {
    loop_3_3(obj0,obj1,obj2);
  }
  static void loop_3_3(Obj obj0,Obj obj1,Obj obj2) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3;
     sum += obj1.f1+obj1.f2+obj1.f3;
     sum += obj2.f1+obj2.f2+obj2.f3;
    }
    out = sum;
  }
  static void measure_3_4(Obj obj0,Obj obj1,Obj obj2) {
    loop_3_4(obj0,obj1,obj2);
  }
  static void loop_3_4(Obj obj0,Obj obj1,Obj obj2) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4;
     sum += obj2.f1+obj2.f2+obj2.f3+obj2.f4;
    }
    out = sum;
  }
  static void measure_3_5(Obj obj0,Obj obj1,Obj obj2) {
    loop_3_5(obj0,obj1,obj2);
  }
  static void loop_3_5(Obj obj0,Obj obj1,Obj obj2) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4+obj0.f5;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4+obj1.f5;
     sum += obj2.f1+obj2.f2+obj2.f3+obj2.f4+obj2.f5;
    }
    out = sum;
  }
  static void measure_4_1(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    loop_4_1(obj0,obj1,obj2,obj3);
  }
  static void loop_4_1(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1;
     sum += obj1.f1;
     sum += obj2.f1;
     sum += obj3.f1;
    }
    out = sum;
  }
  static void measure_4_2(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    loop_4_2(obj0,obj1,obj2,obj3);
  }
  static void loop_4_2(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2;
     sum += obj1.f1+obj1.f2;
     sum += obj2.f1+obj2.f2;
     sum += obj3.f1+obj3.f2;
    }
    out = sum;
  }
  static void measure_4_3(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    loop_4_3(obj0,obj1,obj2,obj3);
  }
  static void loop_4_3(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3;
     sum += obj1.f1+obj1.f2+obj1.f3;
     sum += obj2.f1+obj2.f2+obj2.f3;
     sum += obj3.f1+obj3.f2+obj3.f3;
    }
    out = sum;
  }
  static void measure_4_4(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    loop_4_4(obj0,obj1,obj2,obj3);
  }
  static void loop_4_4(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4;
     sum += obj2.f1+obj2.f2+obj2.f3+obj2.f4;
     sum += obj3.f1+obj3.f2+obj3.f3+obj3.f4;
    }
    out = sum;
  }
  static void measure_4_5(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    loop_4_5(obj0,obj1,obj2,obj3);
  }
  static void loop_4_5(Obj obj0,Obj obj1,Obj obj2,Obj obj3) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4+obj0.f5;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4+obj1.f5;
     sum += obj2.f1+obj2.f2+obj2.f3+obj2.f4+obj2.f5;
     sum += obj3.f1+obj3.f2+obj3.f3+obj3.f4+obj3.f5;
    }
    out = sum;
  }
  static void measure_5_1(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    loop_5_1(obj0,obj1,obj2,obj3,obj4);
  }
  static void loop_5_1(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1;
     sum += obj1.f1;
     sum += obj2.f1;
     sum += obj3.f1;
     sum += obj4.f1;
    }
    out = sum;
  }
  static void measure_5_2(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    loop_5_2(obj0,obj1,obj2,obj3,obj4);
  }
  static void loop_5_2(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2;
     sum += obj1.f1+obj1.f2;
     sum += obj2.f1+obj2.f2;
     sum += obj3.f1+obj3.f2;
     sum += obj4.f1+obj4.f2;
    }
    out = sum;
  }
  static void measure_5_3(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    loop_5_3(obj0,obj1,obj2,obj3,obj4);
  }
  static void loop_5_3(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3;
     sum += obj1.f1+obj1.f2+obj1.f3;
     sum += obj2.f1+obj2.f2+obj2.f3;
     sum += obj3.f1+obj3.f2+obj3.f3;
     sum += obj4.f1+obj4.f2+obj4.f3;
    }
    out = sum;
  }
  static void measure_5_4(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    loop_5_4(obj0,obj1,obj2,obj3,obj4);
  }
  static void loop_5_4(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4;
     sum += obj2.f1+obj2.f2+obj2.f3+obj2.f4;
     sum += obj3.f1+obj3.f2+obj3.f3+obj3.f4;
     sum += obj4.f1+obj4.f2+obj4.f3+obj4.f4;
    }
    out = sum;
  }
  static void measure_5_5(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    loop_5_5(obj0,obj1,obj2,obj3,obj4);
  }
  static void loop_5_5(Obj obj0,Obj obj1,Obj obj2,Obj obj3,Obj obj4) {
    int sum=0;
    for(int i = 0; i < 10; i++) {
     sum += obj0.f1+obj0.f2+obj0.f3+obj0.f4+obj0.f5;
     sum += obj1.f1+obj1.f2+obj1.f3+obj1.f4+obj1.f5;
     sum += obj2.f1+obj2.f2+obj2.f3+obj2.f4+obj2.f5;
     sum += obj3.f1+obj3.f2+obj3.f3+obj3.f4+obj3.f5;
     sum += obj4.f1+obj4.f2+obj4.f3+obj4.f4+obj4.f5;
    }
    out = sum;
  }

    

}
