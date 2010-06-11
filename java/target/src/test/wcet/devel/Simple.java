/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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
 * Simple test cases for the object cache WCET analysis.
 * 
 * measure() is the minimal test case.
 * 
 * measure2() tests correct handling of line size and
 * line or word fill.
 * 
 * @author martin
 *
 */
public class Simple {
	
	int a, b;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Simple simp = new Simple();
		simp.measure();
		simp.measure2();
		simp.measure3();

	}

	/**
	 * This should result in 1 miss and 10 hits.
	 * TODO: at the moment 10 misses.
	 */
	public void loop() {
		
		int val = 0;
		
		for (int i=0; i<10; ++i) {
			val += a;
		}
	}
	
	/**
	 * With one more scope the loop method gives
	 * the correct result ;-)
	 */
	public void measure() {
		loop();
	}
	
	/**
	 * Depending on line size this should give 2
	 * misses or 12. In case of fill whole line
	 * there should be one miss.
	 */
	public void loop2() {
		int val = 0;
		
		for (int i=0; i<10; ++i) {
			val += a;
			val += b;
		}
	}
	
	public void measure2() {
		loop2();
	}
	
	/**
	 * Straight line code. Should be one miss and three hits.
	 */
	public void straight() {
		
		int val = 0;
		val += a;
		val += a;
		val += a;
		val += a;
	}
	
	public void measure3() {
		straight();
	}
}
