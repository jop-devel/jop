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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * We test dynamic dispatch via exceptions 
 * Three exception classes, two (ExS,ExT)  can be by measure.
 */

public class DispatchViaException {

	static int ts, te, to;

	public static void main(String[] _cmdlineargs) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		S s = new S();
		T t = new T();
		int min = 0x7fffffff;
		int max = 0;
		int val = 0;
		Base[] args = { s,t };
		for(int i = 0; i < args.length; i++) {
		    measure(args[i]);
		    val = te-ts-to;
		    if (val<min) min = val;
		    if (val>max) max = val;		
		}
		System.out.print("min: ");
		System.out.println(min);
		System.out.print("max: ");
		System.out.println(max);
	}

	static int measure(Base obj) {
		ts = Native.rdMem(Const.IO_CNT);
		int val = 123;
		try {
		    obj.dispatchE();
		} catch(ExT e) {
		    for (int i=0; i<100; ++i) { // @WCA loop=100
			val += i;
			val += i;
		    }
		} catch(ExS e) {
		    for (int i=0; i<10; ++i) { // @WCA loop=10
			val += i;
			val += i;
		    }
		} catch(ExU e) {
		    for (int i=0; i<1000; ++i) { // @WCA loop=1000
			val += i;
			val += i;
		    }
		} catch(ExBase e) {
		}
		te = Native.rdMem(Const.IO_CNT);
		return val;
	}
        static class ExBase extends Exception {}
        static class ExS extends ExBase {}
        static class ExT extends ExBase {}
        static class ExU extends ExBase {}
	static abstract class Base  {
	    abstract void dispatchE() throws ExBase;
	}
	static class S extends Base  {
	    void dispatchE() throws ExBase {
		throw new ExS();
	    } 
	}
	static class T extends Base  {
	    void dispatchE() throws ExBase {
		throw new ExT();
	    } 
	}
       static class U { /* doesn't extend Base ! */
	    void dispatchE() throws ExBase {
		throw new ExU();
	    } 
	}
}
