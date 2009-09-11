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
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class TestControl {
	
	static class MyControl extends Control {
		int x, y;
		
		public String toString() {
			return "x="+x+" y="+y;
		}
	}

	/**
	 * Test the Control class.
	 * @param args
	 */
	public static void main(String[] args) {
		
		MyControl ctrl = new MyControl();
		ctrl.x = 123;
		ctrl.y = 456;
		
		System.out.println(ctrl);
		ctrl.send();
		ctrl.x = 1;
		ctrl.y = 2;
		ctrl.receive();
		System.out.println(ctrl);
	}

}
