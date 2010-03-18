/*
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


import com.jopdesign.sys.JVMHelp;

/**
 * NodeSlave
 */
public class NodeSlave extends Node 
{
	
	private static int slaveRodlNr;					// rodl nr of the executed rodl
	
	private static Rodl slaveRodls[] = new Rodl[8];	// array containing all rodls

	/**
	 * @param myFileName file name
	 * @param myRecAnz number of records the file has
	 * @param myLogName logical name of the node
	 */
	public NodeSlave(int myFileName, int myRecAnz, byte myLogName)
	{
		super(TtpaConst.SLAVE, myFileName, myRecAnz, myLogName);
	}
	
	/**
	 * adds a rodl to the array containing all rodls
	 * 
	 * @param myRodl rodl to add to the array
	 */
	public void addRodl(Rodl myRodl)
	{
		slaveRodls[myRodl.getRodlNr()] = myRodl;
	}
	
	/**
	 * @param rodl_nr position in the slave_rodls array
	 * @return the rodl that was at the given position in the slave_rodls array
	 */
	public static Rodl getSlaveRodl(int rodl_nr)
	{
		return slaveRodls[rodl_nr];
	}
	
	/**
	 * in the NodeSlave this method has nothing to do
	 */
	public void getSectionValues()
	{
		// nothing to do
	}
	
	/**
	 * in the NodeSlave this method has nothing to do
	 */
	public void getRodlValues()
	{
		// nothing to do
	}
	
	/**
	 * @param mySlaveRodlNr rodl nr
	 */
	public static void setSlaveRodlNr(int mySlaveRodlNr)
	{
		slaveRodlNr = mySlaveRodlNr;
	}

	/**
	 * @return slave_rodl_nr
	 */
	public static int getSlaveRodlNr()
	{
		return slaveRodlNr;
	}

	/**
	 * is called when a new rodl is started
	 */
	public void rodlStart()
	{
		// slot counter is reseted to 1 because slot 0 is the receiving of the FW byte
		Start.node.setSlotCounter(1);
	}

	/**
	 * this function is called by the timer each slot
	 * it executes whatever in this slot has to be done
	 */
	public void doNextSlot()
	{
		if (getSlaveRodlNr() == TtpaConst.MSA)		// MSA slot
		{
			setLastSlot(getMsaObject().msaRecvFrame());
		}
		else if (getSlaveRodlNr() == TtpaConst.MSD)	// MSD slot
		{			
				setLastSlot(getMsdObject().doMsdSlot());
		}
		else								// MP slot
		{
			Start.node.doMpSlotAction();
		}
		
		// increase slot counter
		Start.node.setSlotCounter(Start.node.getSlotCounter() + 1);
		
		// this is the last slot
		if ( isLastSlot() )
		{
			rodlEnd();		// end of rodl, wait for new one
		}
		// this was not the last slot and it is a MP round
		else if ( !isLastSlot() & getSlaveRodlNr() != TtpaConst.MSD && getSlaveRodlNr() != TtpaConst.MSA )
		{
			// calculate the next MP slot
			setLastSlot(getMpObject().mpSlot());
		} 
	}
	
	/**
	 * is called at the end of a rodl
	 * slot counter is resetted and timer deactivated to wait for new round
	 */
	public void rodlEnd()
	{
		rodlStart();		// resets slot counter
		// deactivate Timer
		JVMHelp.removeInterruptHandler(0);
		RoundTimer.setStarted(false);
	}

}
