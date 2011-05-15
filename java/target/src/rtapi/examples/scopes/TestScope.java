/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Martin Schoeberl (martin@jopdesign.com)

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
package examples.scopes;

import com.jopdesign.sys.Memory;

/**
 * Test the JOP specific scoped memory.
 * 
 * @author martin
 *
 */
public class TestScope implements Runnable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Memory sc = new Memory(10000, 10000);
		Memory sc2 = new Memory(20000, 20000);
		TestScope t = new TestScope();
		for (int i=0; i<10; ++i) {
			sc.enter(t);
		}
	}

	@Override
	public void run() {
		for (int i=0; i<50; ++i) {
			String s = "Hello "+i+" ";
			System.out.print(s);
		}
	}

}
