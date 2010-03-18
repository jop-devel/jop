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
 * Rodl
 */
public class Rodl 
{
	
	/** rodl number */
	private int rodlNr;
	
	/** number of used slots (to declare array) */
	private int slotAnz;
	
	/** array containing used slots */
	private Slot rodlSlot[];
	

	/**
	 * @param myRodlNr rodl number
	 * @param mySlotAnz number of used slots (to declare array)
	 */
	public Rodl(int myRodlNr, int mySlotAnz)
	{
		this.rodlNr = myRodlNr;
		this.slotAnz = mySlotAnz;
		this.rodlSlot = new Slot[mySlotAnz];
	}

	/**
	 * @return rodl_nr (0-7)
	 */
	public int getRodlNr()
	{
		return rodlNr;
	}

	/**
	 * @return number of used slots
	 */
	public int getSlotAnz()
	{
		return slotAnz;
	}
	
	/**
	 * @param myRodl_slot a slot of the rodl
	 * @param myPos position in the slot array
	 */
	public void setRodlSlot(Slot myRodl_slot, int myPos)
	{
		this.rodlSlot[myPos] = myRodl_slot;
	}

	/**
	 * @param myPos position in the array rodl_slot
	 * @return slot that is at the given position in the array
	 */
	public Slot getRodlSlot(int myPos)
	{
		return rodlSlot[myPos];
	}
	
}
