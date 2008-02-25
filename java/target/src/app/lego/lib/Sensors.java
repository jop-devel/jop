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
 * Provides access to sensor ADC values (sensors S0-S2).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Sensors
{
	public static final int IO_SENSORS = Const.IO_LEGO;
	
	/**
	 * XXX
	 */
	public static final int MIN_VALUE = 135;
	
	/**
	 * XXX
	 */
	public static final int MAX_VALUE = 361;
	public static final int VALUE_RANGE = MAX_VALUE - MIN_VALUE;
	
	/**
	 * percentage calculation factor * 2**20
	 */
	protected static final int PERCENTAGE_CALC_FACTOR = 463972;
	protected static final int PERCENTAGE_CALC_FACTOR_SHIFT = 20;
	
	protected static int value = 0;

	protected static final int[] OFFSET_SENSOR = { 0, 9, 2*9 };
	protected static final int MASK_SENSOR = 0x1FF;
	
	/**
	 * Reads all sensor values into a buffer during the same cycle.
	 * The buffered values may be read using {@linkplain #getBufferedSensor(int)}.
	 */
	public static void synchronizedReadSensors()
	{
		value = Native.rd(IO_SENSORS);
 	}
	
	/**
	 * Return the value read from the respective sensor.
	 * @param index 0-2.
	 * @return 9 bit ADC value. Expected value range is {@linkplain #MIN_VALUE}-{@linkplain #MAX_VALUE}.
	 */
	public static int readSensor(int index)
	{
		
		return (Native.rd(IO_SENSORS) >> OFFSET_SENSOR[index]) & MASK_SENSOR;
	}
	
	/**
	 * Return the value read from the respective sensor as a percentage value.
	 * @param index 0-2.
	 * @return 0-100. 
	 * Values smaller than {@linkplain #MIN_VALUE} or greater than
	 * {@linkplain #MAX_VALUE} are cut.
	 */
	public static int readSensorValueAsPercentage(int index)
	{
		//return (Math.min(Math.max(readSensor(index)-MIN_VALUE, 0), MAX_VALUE) * 100) / VALUE_RANGE;
		return (Math.min(Math.max(readSensor(index)-MIN_VALUE, 0), MAX_VALUE) * PERCENTAGE_CALC_FACTOR) >> PERCENTAGE_CALC_FACTOR_SHIFT;
	}
	
	/**
	 * Returns value read by {@linkplain #synchronizedReadSensors()}
	 * @param index 0-2.
	 * @return 9 bit ADC value. Expected value range is {@linkplain #MIN_VALUE}-{@linkplain #MAX_VALUE}.
	 */
	public static int getBufferedSensor(int index)
	{
		return (value >> OFFSET_SENSOR[index]) & MASK_SENSOR;
	}	
}
