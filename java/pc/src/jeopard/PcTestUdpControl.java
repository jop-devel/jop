/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package jeopard;


/**
 * Test UDP based JOP/PC JVM communicatoin.
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class PcTestUdpControl {
	
	/**
	 * Just a test class. Should actually be shared by JOP
	 * and jamaica to avoid any inconsistencies.
	 * 
	 * @author Martin Schoeberl (martin@jopdesign.com)
	 *
	 */
	static class MyControl extends PcUdpControl {
		// need to be public to enable reflection
		public int x, y;
		
		public MyControl() {
			super();
		}
		public String toString() {
			return "x="+x+" y="+y;
		}
	}

	/**
	 * Test the Control class.
	 * @param args
	 */
	public static void main(String[] args) {


		final MyControl ctrl = new MyControl();
		
		ctrl.x = 123;
		ctrl.y = 456;		
		System.out.println(ctrl);
		ctrl.send();
		ctrl.x = ctrl.y = 0;
		for (;;) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ctrl.send();
			System.out.println("Send: "+ctrl);
			++ctrl.x;
			--ctrl.y;
		}		
	}
}
