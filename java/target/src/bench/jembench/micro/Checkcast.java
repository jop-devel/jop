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


public class Checkcast extends SerialBenchmark {

  private final Checkcast  cself;
  private final Object     oself;

  public Checkcast() {
    oself = cself = this;
  }

  public String toString() {
    return "checkcast";
  }

  /*
   9:   aload_2
   10:  getfield        #3; //Field oself:Ljava/lang/Object;
   13:  checkcast       #5; //class jbe/micro/Checkcast
   16:  astore_2
  */
  public int perform(int  cnt) {
    Checkcast  t = this;
    while(--cnt >= 0)  t = (Checkcast)t.oself;
    return  t.hashCode();
  }

  /*
   9:   aload_2
   10:  getfield        #2; //Field cself:Ljbe/micro/Checkcast;
   13:  astore_2
  */
  public int overhead(int  cnt) {
    Checkcast  t = this;
    while(--cnt >= 0)  t = t.cself;
    return  t.hashCode();
  }
}
