/*
 * Copyright (c) 2010  Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * For any additional information or questions, please, contact:
 *
 *    Chair for VLSI-EDA
 *    Fakultaet Informatik
 *    Technische Universitaet Dresden
 *    01062 Dresden
 *    GERMANY
 */
package ite.test.access;

import  ite.test.access.other.*;

/**
 * Main Class of a test bench to verify the proper construction of dispatch
 * tables.
 *
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 */
public class AccessTest {

  private static String callA(final ClsA a, final StringBuilder bld) {
    return  a.f(bld) + " / " + a.g(bld);
  }
  private static String callB(final ClsB b, final StringBuilder bld) {
    return  b.f(bld) + " / " + b.g(bld);
  }
  private static String callC(final ClsC c, final StringBuilder bld) {
    return  c.f(bld) + " / " + c.g(bld);
  }


  public static void main(String[] args) {
    final StringBuilder  log = new StringBuilder();

    final ClsA  a = new ClsA();
    final ClsB  b = new ClsB();
    final ClsC  c = new ClsC();

    System.out.println("A as A: " + callA(a, log));
    System.out.println();

    System.out.println("B as A: " + callA(b, log));
    System.out.println("B as B: " + callB(b, log));
    System.out.println();

    System.out.println("C as A: " + callA(c, log));
    System.out.println("C as B: " + callB(c, log));
    System.out.println("C as C: " + callC(c, log));
    System.out.println();

    System.out.println(log.toString().equals("AaAaBbCaCbCb")? "PASS" : "FAIL");
  }
}
