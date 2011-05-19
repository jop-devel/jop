/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jcopter;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class SimpleInlinerTest {

    public static int calcA(int a, int b) {
        return a * b;
    }

    public static void empty(int a, int b) {
    }


    public static void main(String[] args) {

        int a,b,c;

        b = 2;
        c = 3;

        // static invoke
        a = calcA(b, c);

        // empty method
        empty(a, b);

        A cA = new A();
        B cB = new B();
        A cC = new B();

        // constant
        a = cA.constant(a);

        // Stack
        a = a * cA.constant(b + 2) - cB.getField();

        // getter, setter
        cB.setField(a);
        b = cB.getField();

        // wrapper
        c = cB.wrapper1(a, b);
        a = cB.wrapper2(a, 2, 3);

        // long arguments
        long l = cB.longA(2, 0.5f, a);


        // can be inlined only with DFA
        a = cA.override(b, c);
        b = cC.override(b, c);

    }


}

class A {

    private int field;

    public int constant(int a) {
        return 4;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public int override(int a, int b) {
        return a + b;
    }

}


class B extends A {

    public int override(int a, int b) {
        return a - b;
    }

    public int wrapper1(int a, int b) {
        return override(a, b);
    }

    public int wrapper2(int a, int b, int c) {
        return complex(c, a);
    }

    public int complex(int a, int b) {
        long c = override(a, b) * getField();
        return (int) c;
    }

    public long longA(long a, double d, int b) {
        return a + b;
    }


}