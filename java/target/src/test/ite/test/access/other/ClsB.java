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
package ite.test.access.other;

import  ite.test.access.*;

/**
 * Part of a test bench to verify the proper construction of dispatch tables.
 *
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 */
public class ClsB extends ClsA {
  public char f(final StringBuilder bld) {
    bld.append('B');
    return 'B';
  }
  public char g(final StringBuilder bld) {
    bld.append('b');
    return 'b';
  }
}
