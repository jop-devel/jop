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
 * Provides access to the speaker output. 
 * @author Alexander Dejaco (alexander.dejaco@student.tuwien.ac.at)
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Speaker
{
	public static final int IO_SPEAKER= Const.IO_LEGO + 7;	
	
	public static final int MAX_VALUE = 0xff;
	
	protected static int value;
	
	/**
	 * Sets speaker output.
	 */
	public static final void write(boolean value)
	{
		Native.wr(value ? MAX_VALUE : 0, IO_SPEAKER);
	}
	
	/**
	 * Sets speaker output.
	 * @param value Valid input range is 0..0xff. 
	 */
	public static final void write(int value)
	{
		Native.wr(value, IO_SPEAKER);
	}
}
