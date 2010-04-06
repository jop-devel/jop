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
 * PrepareAction
 */
public class PrepareAction 
{
	
	private byte sendValue;			// value to be sent
	private int action;				// action to be done (send, receive, execute)
	private int recvPos;			// position to save received value in the io array
	private byte sendFWValue;		// FW to send		
	private SlotExec exec;			// execute slot is stored here
	
	public PrepareAction()
	{		
	}

	/**
	 * @param value this byte will be sent
	 */
	public void setsendValue(byte value)
	{
		this.sendValue = value;
	}

	/**
	 * @return value that will be sent
	 */
	public byte getSendValue()
	{
		return sendValue;
	}

	/**
	 * @param action this action will be done (send, receive, execute)
	 */
	public void setAction(int action)
	{
		this.action = action;
	}

	/**
	 * @return action that will be done (send, receive, execute)
	 */
	public int getAction()
	{
		return action;
	}

	/**
	 * @param recvPos position in the io array where the value to receive will be stored
	 */
	public void setRecvPos(int recvPos)
	{
		this.recvPos = recvPos;
	}

	/**
	 * @return position in the io array where the value to receive will be stored
	 */
	public int getRecvPos()
	{
		return recvPos;
	}

	/**
	 * @param sendFWValue FW byte that will be sent
	 */
	public void setSendFWValue(byte sendFWValue)
	{
		this.sendFWValue = sendFWValue;
	}

	/**
	 * @return FW byte that will be sent
	 */
	public byte getSendFWValue()
	{
		return sendFWValue;
	}

	/**
	 * @param exec SlotExec that will be done
	 */
	public void setExec(SlotExec exec)
	{
		this.exec = exec;
	}

	/**
	 * @return SlotExec that will be done
	 */
	public SlotExec getExec()
	{
		return exec;
	}
	
}
