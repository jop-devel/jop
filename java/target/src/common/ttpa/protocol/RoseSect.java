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
 * a section of the rose file
 * the section contains a sequence of rounds (rodls) that are executed 
 */
public class RoseSect 
{
	
	/** number of rodls in the array */
	private int rodlDescAnz;
	
	/** rose time: contains start time and period time */
	private RoseTime time;
	
	/** array containing the rodls of the section */
	private RodlDesc rodlDescs[];
	
	/**
	 * @param myRodlDescAnz number of active rodls in the section
	 * @param myTime rose time, containing start time and period time
	 */
	public RoseSect(int myRodlDescAnz, RoseTime myTime)
	{
		this.rodlDescAnz = myRodlDescAnz;
		this.time = myTime;
		this.rodlDescs = new RodlDesc[myRodlDescAnz];
	}
	
	/**
	 * @return number of rodls in the array
	 */
	public int getRodlDescAnz()
	{
		return rodlDescAnz;
	}

	/**
	 * @return RoseTime containing start and peroid time
	 */
	public RoseTime getTime()
	{
		return time;
	}

	/**
	 * @param myRodlDesc rodl description
	 * @param myPos position in the array rodl_descs[]
	 */
	public void setRodlDesc(RodlDesc myRodlDesc, int myPos)
	{
		this.rodlDescs[myPos] = myRodlDesc;
	}
	
	/**
	 * @param myPos position in the array rodl_descs[]
	 * @return the rodl description in the array at the given position
	 */
	public RodlDesc getRodlDesc(int myPos) 
	{
		return rodlDescs[myPos];
	}
	

}
