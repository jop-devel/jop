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
 * Provides access to microphone ADC input (MIC1).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class Microphone
{
	//public static final int IO_MICROPHONE = Const.IO_MICRO;
	
	
	/**
	 * Returns the value last read from the microphone.
	 * @return 9 bit ADC value.
	 * XXX Expected value range.
	 */
	public static int readMicrophone()
	{
		return 0;
	}
}
