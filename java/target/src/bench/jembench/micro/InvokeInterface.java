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


public class InvokeInterface extends SerialBenchmark implements IfaceValue {

  private volatile int  value;
  private IfaceValue    iface;
  
  public InvokeInterface() {
    value =  127;
    iface = this;
  }

  public String toString() {
    return "invokeinterface"; //"aload invokeinterface ireturn";
  }

  public int getValue() {
    return  value;
  }

/*
   14:  iload_2
   15:  aload_3
   16:  invokeinterface #5,  1; //InterfaceMethod jbe/micro/IfaceValue.getValue:()I
      0:   aload_0
      1:   getfield        #3; //Field value:I
      4:   ireturn
   21:  iadd
   22:  istore_2
*/
  public int perform(int cnt) {
    int a = 0;

    final IfaceValue  iface = this.iface;
    while(--cnt >= 0)  a += iface.getValue();
    return  a;
  }

/*
   14:  iload_2
   15:  aload_0
   16:  getfield        #3; //Field value:I
   19:  iadd
   20:  istore_2
*/
  public int overhead(int cnt) {
    int a = 0;

    final IfaceValue  iface = this.iface;
    while(--cnt >= 0)  a += value;
    return  a;
  }
			
}
