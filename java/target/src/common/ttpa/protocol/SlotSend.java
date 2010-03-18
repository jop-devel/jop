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
 * SlotSend
 */
public class SlotSend extends Slot
{
	/** start position of the values to send in the io_array */
	private int startPos;
	
	/** how many values will be sent */
	private int length;
	
	/**
	 * @param mySlotNr slot number
	 * @param myStartPos position of the first value to send in the io array
	 * @param myLength how many values will be sent consecutively
	 */
	public SlotSend(int mySlotNr, int myStartPos, int myLength)
	{
		super(mySlotNr,TtpaConst.SLOT_SEND);
		this.startPos = myStartPos;
		this.length = myLength;
	}
	
	/**
	 * @param mySendValue value to send
	 * @param myPos position in the io array
	 */
	public void setSendValue(byte mySendValue, int myPos)
	{
		Start.node.setIoArrayPos(mySendValue, myPos);
	}
	
	/**
	 * @param myPos position of the value in the io_array
	 * @return value at the given position
	 */
	public byte getSendValue(int myPos)
	{
		return Start.node.getIoArrayPos(myPos);
	}

	/** 
	 * @return start position of the values to send in the io_array
	 */
	public int getStartPos()
	{
		return startPos;
	}

	/**
	 * @return how many values will be sent
	 */
	public int getLength()
	{
		return length;
	}
	
}
