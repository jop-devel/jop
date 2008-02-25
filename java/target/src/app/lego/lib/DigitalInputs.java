/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Peter Hilber and Alexander Dejaco

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

package lego.lib;

import com.jopdesign.sys.*;

/**
 * Provides access to general purpose digital inputs (I0-I2).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class DigitalInputs
{
	public static final int IO_DIGITALINPUTS = Const.IO_LEGO + 5;

	/**
	 * Read digital input.
	 * @param index Valid indices are 0, 1, 2.
	 */	
	public static boolean getDigitalInput(int index)
	{
		//if (index<0 || index>3)
		//	throw new RuntimeException("Invalid button index specified!");
		return ((Native.rd(IO_DIGITALINPUTS) >> index) & 1) != 0;
	}
	
	/**
	 * Reads all digital inputs into the respective bits.
	 * @return The digital inputs are numbered from 0 to 2.
	 * All other bits are set to zero. 
	 */
	public static int getDigitalInputs()
	{
		return Native.rd(IO_DIGITALINPUTS);
	}
}
