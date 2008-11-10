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

import com.jopdesign.sys.*;

public class Child extends Parent{
	static int ts, te, to;

	public static void main(String[] args){
	    ts = Native.rdMem(Const.IO_CNT);
	    te = Native.rdMem(Const.IO_CNT);
	    to = te-ts;
	    // WCET with var. block cache: 12279
	    measure();    
	    System.out.println(te-ts-to);
  }
  
  public static void measure(){
      ts = Native.rdMem(Const.IO_CNT);      
      loop();
      te = Native.rdMem(Const.IO_CNT);
  }
}
