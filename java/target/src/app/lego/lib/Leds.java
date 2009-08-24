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
 * Provides access to diagnostic LEDs (LED0-LED3). 
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Leds
{
	public static final int IO_LEDS = Const.IO_LEGO + 0;
	public static final int LED_COUNT = 4;
	protected static int value;
	
	/**
	 * Turns the LED on and off several times a second (if called that often).<br/>
	 * The LED is set if the 18^th bit of us is on.
	 * Therefore, the update has to be called at least 8 times a second.
	 * @param index 0-3.
	 */
	public static final void blinkUpdate(int index)
	{
		setLed(index, (Native.rd(Const.IO_US_CNT) & 0x40000) != 0);
	}
	
	/**
	 * Sets diagnostic LEDs.
	 * @param state LEDs 0-3 state is set by the corresponding bits.
	 * All other bits are ignored.
	 */			
	public static final void setLeds(int state)
	{
		value = state;
		Native.wr(state, IO_LEDS);
	}
	
	/**
	 * Returns the state of the diagnostic LEDs.
	 * @return State of the LEDs 0-3 in the corresponding bit.
	 * All other bits are set to zero.
	 */
	public static final int getLeds()
	{
		return value;
	}
	
	/**
	 * Returns the state of a diagnostic LED.
	 * @param index 0-3.
	 */
	public static final boolean getLed(int index)
	{
		return ((value >> index) & 1) != 0;
	}
	
	/**
	 * Sets the state of a diagnostic LED.
	 * @param index 0-3.
	 * @param on
	 */
	public static final void setLed(int index, boolean on)
	{
		setLeds((getLeds() & ~(1<<index)) | ((on?1:0)<<index));
	}
}
