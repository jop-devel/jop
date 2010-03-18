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

//import ttpa.user.UserRodl;

public class MultiPartner 
{	
	/** the active rodl */
	private Rodl activeRodl;
	/** helping variable to get the next slot */
	private int slotArrayCounter;
	/** end of round */
	private int eor;
	
	/** number of bytes that will be sent or received */
	private int ioLen = 0;
	/** counts the bytes that already have been sent or received */
	private int ioLenCnt = 0;
	/** position in the io array of the first byte to be sent or received */
	private int firstPos = 0;
	private int action = 0;				// send, receive or execute
	
	/** next active slot */
	private Slot nextSlot = null;
	private SlotSend send = null;		// send slot
	private SlotRecv recv = null;		// recv slot
	
	public MultiPartner() 
	{
	}
	
	/**
	 * @param myActiveRodl the active rodl
	 * @param mySlot_array_counter counts the slots in the slot array
	 */
	public void init(Rodl myActiveRodl) 
	{
		this.activeRodl = myActiveRodl;
		this.slotArrayCounter = 0;
		this.eor = myActiveRodl.getRodlSlot(myActiveRodl.getSlotAnz()-1).getSlotNr();
	}
	
	/**
	 * prepare the multipartner slot
	 * 
	 * @return true if this was eor, false else
	 */
	public boolean mpSlot()
	{
		nextSlot = activeRodl.getRodlSlot(slotArrayCounter);		// next active slot
		
		// is this is the master and the first slot then send a FW byte
		if (Start.node.isMaster() && Start.node.getSlotCounter() == 0)
		{
			Start.node.getNextWork().setAction(TtpaConst.SLOT_SEND_FW);
			Start.node.getNextWork().setSendFWValue(TtpaConst.FIREWORK[activeRodl.getRodlNr()]);
		}
		else
		{
			/* there is something left to send/receive
			   (more than one value can be sent/received in consecutive slots,
			   without making more than one slot object in the user rodl) */
			if ( ioLen > ioLenCnt )
			{
				if (action == TtpaConst.SLOT_SEND)			// send, store action and value to send
				{
					Start.node.getNextWork().setAction(TtpaConst.SLOT_SEND);
					Start.node.getNextWork().setsendValue((byte) send.getSendValue(firstPos + ioLenCnt));
					
				}
				else if (action == TtpaConst.SLOT_RECV)	// receive, store action and position to save data
				{
					Start.node.getNextWork().setAction(TtpaConst.SLOT_RECV);
					Start.node.getNextWork().setRecvPos(recv.getStartPos() + ioLenCnt);
				}
				ioLenCnt++;	// increase counter because another value was sent/received
			}
			else
			{
				// active Slot
				if ( nextSlot.getSlotNr() == Start.node.getSlotCounter() )
				{
					/* determine the type of slot */
					// this is a send slot
					if (nextSlot.getSlotAction() == TtpaConst.SLOT_SEND)
					{
						send = (SlotSend) nextSlot;
						ioLen = send.getLength();			// number of bytes that will be sent
						firstPos = send.getStartPos();	// position of the first byte to be sent
						ioLenCnt = 1;						// initialize the counter to 1
						action = TtpaConst.SLOT_SEND;		// action is to send
						/* prepare what there will be done in the next slot */
						Start.node.getNextWork().setAction(action);
						Start.node.getNextWork().setsendValue((byte) send.getSendValue(firstPos));
					}
					// this is a receive slot
					else if (nextSlot.getSlotAction() == TtpaConst.SLOT_RECV)
					{	
						recv = (SlotRecv) nextSlot;
						ioLen = recv.getLength();			// number of bytes that will be received
						firstPos = recv.getStartPos();	// position of the first byte to be received
						ioLenCnt = 1;						// initialize the counter to 1
						action = TtpaConst.SLOT_RECV;		// action is to receive
						/* prepare what there will be done in the next slot */
						Start.node.getNextWork().setAction(action);
						Start.node.getNextWork().setRecvPos(firstPos);
					}
					// this is an execute slot
					else if (nextSlot.getSlotAction() ==  TtpaConst.SLOT_EXEC)
					{
						/* prepare what will be done in the next slot */
						Start.node.getNextWork().setAction(TtpaConst.SLOT_EXEC);
						Start.node.getNextWork().setExec( (SlotExec) nextSlot );
					}
					slotArrayCounter++;		// increase counter to the next position in the array of slots
				}
				// there is nothing to do in this slot
				else
				{
					Start.node.getNextWork().setAction(TtpaConst.SLOT_EMPTY);	// empty slot
				}
			}
		}

		if (Start.node.getSlotCounter() == eor - 1)		// eor is reached
		{
			return true;
		}
		
		return false;
	}
	
}
