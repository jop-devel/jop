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



import com.jopdesign.sys.*;

/**
 * RoundTimer
 */
public class RoundTimer implements Runnable 
{

	/** value to be written into the timer */
	private static int startTime;
	
	/** show if the timer is started or not */
	private static boolean isStarted = false;
	
	/**
	 * @param delayUs the delay (timer value) in us
	 */
	public RoundTimer(int delayUs)
	{	
		// disable interrupts globally and locally
		Native.wr(0, Const.IO_INT_ENA);
		Native.wr(0, Const.IO_INTMASK);
		
		// calculate start time of timer
		startTime = Native.rd(Const.IO_US_CNT) + delayUs;
		
		Native.wr(1, Const.IO_INTCLEARALL);		// clear pending interrupts
		Native.wr(startTime, Const.IO_TIMER);	// write value to timer
		Native.wr(1, Const.IO_INTCLEARALL);		// clear pending interrupts
		
		// enable all Interrupts
		Native.wr(-1, Const.IO_INTMASK);
		Native.wr(1, Const.IO_INT_ENA);	
	}
	
	/**
	 * @param myStartTime value for the timer
	 */
	public static void setStartTime(int myStartTime)
	{
		startTime = myStartTime;
	}
	
	/**
	 * @return value of the timer
	 */
	public static int getStartTime()
	{
		return startTime;
	}
	
	/**
	 * @param isStarted true if the timer is started, false else
	 */
	public static void setStarted(boolean isStarted)
	{
		RoundTimer.isStarted = isStarted;
	}

	/**
	 * @return true if the timer is started, false else
	 */
	public static boolean isStarted()
	{
		return isStarted;
	}
	
	/**
	 * reset timer to the given delay
	 * 
	 * @param delayUs delay in us
	 */
	public void resetTimer(int delayUs)
	{
		// calculate start time of timer
		startTime = Native.rd(Const.IO_US_CNT) + delayUs;
		Native.wr(1, Const.IO_INTCLEARALL);					// clear pending interrupts
		Native.wr(startTime, Const.IO_TIMER);				// write value to timer
		Native.wr(1, Const.IO_INTCLEARALL);					// clear pending interrupts
	}
	
	/**
	 * this function is called by the timer
	 */
	public void run()
	{		
		// disable interrupts globally and locally
		Native.wr(0, Const.IO_INT_ENA);
		Native.wr(0, Const.IO_INTMASK);
		
		startTime += TtpaConst.SLOT_LENGTH;		// calculate start time of timer
		
		Native.wr(1, Const.IO_INTCLEARALL);		// clear pending interrupts
		Native.wr(startTime, Const.IO_TIMER);	// write value to timer
		Native.wr(1, Const.IO_INTCLEARALL);		// clear pending interrupts
		
		// enable all interrupts
		Native.wr(-1, Const.IO_INTMASK);
		Native.wr(1, Const.IO_INT_ENA);
		
		// do whatever has to be done in this slot ;)
		Start.node.doNextSlot();
	}
	
}
