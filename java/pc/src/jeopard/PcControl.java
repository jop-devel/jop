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

import java.lang.reflect.Field;

/**
 * Communication between JOP and jamaicaVM for the JEOPARD project.
 * Extend this class for the communication of control values.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class PcControl {
	
	int[] data;
	boolean avail;
	
	public PcControl() {
		data = new int[getSize()];
	}
	
	final int getSize() {
		Class<? extends PcControl> clazz = this.getClass();
		return clazz.getFields().length;
	}
	
	/**
	 * TODO: support all primitive types.
	 */
	void pack() {
		Class<? extends PcControl> clazz = this.getClass();
		Field f[] = clazz.getFields();
		for (int i=0; i<f.length; ++i) {
			try {
				int val = f[i].getInt(this);
				data[i] = val;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * TODO: support all primitive types.
	 */
	void unpack() {
		Class<? extends PcControl> clazz = this.getClass();
		Field f[] = clazz.getFields();
		for (int i=0; i<f.length; ++i) {
			try {
				f[i].setInt(this, data[i]);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send() {
		pack();
		avail = true;
	}
	
	public boolean dataAvail() {
		return avail;
	}
	
	public void receive() {
		if (!avail) {
			System.out.println("Data not available - use old value");
		}
		unpack();
		avail = false;
	}
}
