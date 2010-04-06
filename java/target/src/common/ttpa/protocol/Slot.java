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
 * Slot
 */
public abstract class Slot
{
	
	/** slot number */
	private int slotNr;
	
	/** action that will be done in this slot (send, receive, execute) */
	private int slotAction; 
	
	/**
	 * @param mySlotNr slot number
	 * @param mySlotAction send, receive or execute
	 */
	public Slot(int mySlotNr, int mySlotAction)
	{
		this.slotNr = mySlotNr;
		this.slotAction = mySlotAction;
	}
	
	/**
	 * @return slot number
	 */
	public int getSlotNr()
	{
		return slotNr;
	}
	
	/**
	 * @return slot action (send, receive, execute)
	 */
	public int getSlotAction()
	{
		return slotAction;
	}

}
