/*******************************************************************************
 *
 *   This file is part of JOP, the Java Optimized Processor
 *     see <http://www.jopdesign.com/>
 * 
 *   Copyright (C) 2007, Peter Hilber and Alexander Dejaco
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 * 
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Casper Jensen <semadk@gmail.com> - Changes for use as simulation
 *     Christian Frost <thecfrost@gmail.com> - Changes for use as simulation
 *     Kasper Søe Luckow <luckow@cs.aau.dk> - Changes for use as simulation
 *
 ******************************************************************************/

package minepump.legosim.lib;



/**
 * Provides access to sensor ADC values (sensors S0-S2).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Sensors
{
	/* Reads all sensor values into a buffer during the same cycle.
	 * The buffered values may be read using {@linkplain #getBufferedSensor(int)}.
	 */
	public static void synchronizedReadSensors()
	{
		
 	}
	
	/**
	 * Return the value read from the respective sensor.
	 * @param index 0-2.
	 * @return 9 bit ADC value. Expected value range is {@linkplain #MIN_VALUE}-{@linkplain #MAX_VALUE}.
	 */
	public static int readSensor(int index)
	{
		
	return 0;
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
		return 0;
	}
	
	/**
	 * Returns value read by {@linkplain #synchronizedReadSensors()}
	 * @param index 0-2.
	 * @return 9 bit ADC value. Expected value range is {@linkplain #MIN_VALUE}-{@linkplain #MAX_VALUE}.
	 */
	public static int getBufferedSensor(int index)
	{
		return 0;
	}	
}
