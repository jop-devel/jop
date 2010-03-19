/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
    
  Copyright (C) 2010, Thomas Hassler, Lukas Marx

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
 * @author Thomas Hassler	e0425918@student.tuwien.ac.at
 * @author Lukas Marx	lukas.marx@gmail.com
 * @version 1.0
 */

package ttpa.protocol;

/**
 * RoseTime
 */
public class RoseTime 
{
	
	/** 64 bit value containing the start time */
	private long startTime;
	
	/** 64 bit value containing the period time */
	private long periodTime;
	
	/**
	 * @param myStartTime 64 bit value containing the start time
	 * @param myPeriodTime 64 bit value containing the peroid time
	 */
	public RoseTime(long myStartTime, long myPeriodTime)
	{
		this.startTime = myStartTime;
		this.periodTime = myPeriodTime;
	}
	
	/**
	 * @return 64 bit value containing the start time
	 */
	public long getStartTime()
	{
		return startTime;
	}
	
	/**
	 * @return 64 bit value containing the period time
	 */
	public long getPeriodTime()
	{
		return periodTime;
	}

}
