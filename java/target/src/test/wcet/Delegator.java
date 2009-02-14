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
/* Testing the Receiver Type analysis */
public class Delegator {
    public static final boolean MEASURE = true;
    
    interface Reader {
        char getChar();
    }
    static class SubstReader implements Reader {
        private Reader reader;
        private char pattern,replace;
        public SubstReader(Reader r, char pat, char repl) {
            reader = r;
            pattern = pat;
            replace = repl;
        }
        public char getChar() {
            char c = reader.getChar();
            if(c == pattern) return replace;
            else return c;
        }
    }
    /* Linebreak conversion: "\r\n" -> "\n" */
    static class LBCReader implements Reader {
        private Reader reader;
        private int buf;
        public LBCReader(Reader r) {
            reader = r;
            buf = -1;
        }
        public char getChar() {
            char c;
            if(buf >= 0) {
                c = (char)buf; buf = -1;
            } else {
                c = reader.getChar();
            }
            if(c != '\r') {
                return c;
            } else {
                char c2 = reader.getChar();
                if(c2 == '\n') {
                    return c2;
                } else {
                    buf = c2;
                    return c;
                }
            }
        }        
    }
    static class ConstReader implements Reader {
        private int state;
        public ConstReader() {
            state = 1;
        }
        public char getChar() { 
            return (state == 0 ? '\r' : '\n');
        }
    }
	static int ts, te, to;
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// measurement + return takes 22+22+21=65 cycles
		// WCET measured: 2312
		// WCET analysed: 2377
		Reader r1,r2;
		r1 = new SubstReader(new ConstReader(),'\n','\t');
		r2 = new SubstReader(new LBCReader(new ConstReader()),'\n','\t');
		if(to < 20) { /* confuse DFA a little */
		    Reader tmp = r1;
		    r1 = r2;
		    r2 = tmp;
		}
	    measure(r1);
		System.out.print("t1: ");
		System.out.println(te-ts-to);
	    measure(r2);
		System.out.print("t2: ");
		System.out.println(te-ts-to);
	}
	
	static int measure(Reader r) {
	    int val = 1;
		if(MEASURE) ts = Native.rdMem(Const.IO_CNT);		
		for (int i=0; i<100; ++i) { // @WCA loop=100
			val += (int) r.getChar();
		}
		if(MEASURE) te = Native.rdMem(Const.IO_CNT);
		return val;
	}
	
}
