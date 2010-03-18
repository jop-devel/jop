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
 * RodlDesc
 */
public class RodlDesc 
{
	
	/** rodl */
	private Rodl rodl;
	
	/** is it the last round of the sequence */
	private boolean lastRound;
	
	/** length of the IRG in slots: 1-15 */
	private int irg;
	
	/**
	 * @param myRodl rodl
	 * @param myLastRound last round of the sequence?
	 * @param myIrg length of IRG in slots (1-15)
	 */
	public RodlDesc(Rodl myRodl, boolean myLastRound, int myIrg)
	{
		this.rodl = myRodl;
		this.lastRound = myLastRound;
		this.irg = myIrg;
	}

	/**
	 * @return rodl
	 */
	public Rodl getRodl() {
		return rodl;
	}

	/**
	 * @return last_round true if this was the last round of the sequence, false else
	 */
	public boolean isLastRound() {
		return lastRound;
	}

	/**
	 * @return irg length of this round in slot (1-15)
	 */
	public int getIrg() {
		return irg;
	}
	
}
