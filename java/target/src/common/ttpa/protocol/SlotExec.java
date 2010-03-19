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
 * SlotExec
 */
public class SlotExec extends Slot {
	
	/** Runnable function of the SlotExec */
	private Runnable function;
	
	/**
	 * @param mySlotNr slot number
	 * @param myFunction the Runnable function that will be executed
	 */
	public SlotExec(int mySlotNr, Runnable myFunction)
	{
		super(mySlotNr, TtpaConst.SLOT_EXEC);
		this.function = myFunction;
	}
	
	/**
	 * @return the Runnable function that will be executed
	 */
	public Runnable getFunction()
	{
		return function;
	}

}
