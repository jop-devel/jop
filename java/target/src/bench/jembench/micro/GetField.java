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


public class GetField extends SerialBenchmark {

  private final GetField  field;

  public GetField() {
    field = this;
  }

  public String toString() {
    return "getfield";
  }

  /*
   9:   aload_2
   10:  getfield        #2; //Field field:Ljbe/micro/GetField;
   13:  getfield        #2; //Field field:Ljbe/micro/GetField;
   16:  astore_2
  */
  public int perform(int  cnt) {
    GetField  t = this;
    while(--cnt >= 0)  t = t.field.field;
    return  t.hashCode();
  }

  /*
   9:   aload_2
   10:  getfield        #2; //Field field:Ljbe/micro/GetField;
   13:  astore_2
  */
  public int overhead(int  cnt) {
    GetField  t = this;
    while(--cnt >= 0)  t = t.field;
    return  t.hashCode();
  }
}
