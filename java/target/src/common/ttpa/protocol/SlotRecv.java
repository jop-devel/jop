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
 * SlotRecv
 */
public class SlotRecv extends Slot
{
	/** start position of the values to receive in the io_array */
	private int startPos;
	
	/** how many values will be received */
	private int length;
	
	/**
	 * @param mySlotNr slot number
	 * @param myStartPos position of the first value to receive in the io array
	 * @param myLength how many values will be received consecutively
	 */
	public SlotRecv(int mySlotNr, int myStartPos, int myLength)
	{
		super(mySlotNr, TtpaConst.SLOT_RECV);
		this.startPos = myStartPos;
		this.length = myLength;
	}
	
	/**
	 * @return start position of the values to receive in the io_array
	 */
	public int getStartPos()
	{
		return startPos;
	}

	/**
	 * @return how many values will be received
	 */
	public int getLength()
	{
		return length;
	}
	

}
