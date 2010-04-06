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
 * abstract class Node
 */
abstract class Node 
{
	
	/** type: true = master, false = slave */
	private boolean type;
	
	/** ifs file name */
	private int fileName;	
	
	/** logical name of the node */
	private byte logName;			
	
	/** io array (for sending and receiving bytes) */
	private byte[] ioArray;
	
	/** object to store the next operation of a MP slot */
	private PrepareAction nextWork = new PrepareAction();
	
	/** msa object */
	private Msa msaObject = new Msa();
	/** msd object */
	private Msd msdObject = new Msd();
	/** multi-partner object */
	private MultiPartner mpObject = new MultiPartner();
	
	private int slotCounter;		// in which slot are we now?
	private boolean lastSlot;		// was this the last slot of the round?
		
	/** it creates a node object (neede by creating a nodemaster or nodeslave object)
	 * 
	 * @param myType Master or Slave
	 * @param myFileName file names 
	 * @param myRecAnz size of the IO-Array in records
	 * @param myLogName logical name of the node
	 */
	public Node(boolean myType, int myFileName, int myRecAnz, byte myLogName)
	{
		this.type = myType;
		this.setFileName(myFileName);
		this.logName = myLogName;
		ioArray = new byte[(myRecAnz * 4)];			// ein Record besteht aus 4 Bytes
	}
	
	/**
	 * @return true if the object is a master
	 */
	public boolean isMaster()
	{
		return type;
	}

	/**
	 * @param value value to set in the array
	 * @param pos the position for the value in the array
	 */
	public void setIoArrayPos(byte value, int pos)
	{
		ioArray[pos] = value;
	}

	/**
	 * @param pos postion of the wanted value in the array
	 * @return the value from the given position
	 */
	public byte getIoArrayPos(int pos)
	{
		return ioArray[pos];
	}
	
	/**
	 * @return logical name of the node
	 */
	public byte getLogName() 
	{
		return this.logName;
	}

	/**
	 * @param fileName file name of the node
	 */
	private void setFileName(int fileName) 
	{
		this.fileName = fileName;
	}
	
	/**
	 * @return file name of the node
	 */
	public int getFileName() 
	{
		return fileName;
	}

	/**
	 * @return nextWork to do
	 */
	public PrepareAction getNextWork()
	{
		return nextWork;
	}

	/**
	 * @return the MSA object
	 */
	public Msa getMsaObject()
	{
		return msaObject;
	}

	/**
	 * @return the MSD object
	 */
	public Msd getMsdObject()
	{
		return msdObject;
	}

	/**
	 * @return MP object
	 */
	public MultiPartner getMpObject()
	{
		return mpObject;
	}

	/**
	 * @param slotCounter slot counter value
	 */
	public void setSlotCounter(int slotCounter)
	{
		this.slotCounter = slotCounter;
	}

	/**
	 * @return slot counter
	 */
	public int getSlotCounter()
	{
		return slotCounter;
	}

	/**
	 * @param lastSlot true if this is the last slot, false else
	 */
	public void setLastSlot(boolean lastSlot)
	{
		this.lastSlot = lastSlot;
	}

	/**
	 * @return true if this is the last slot, false else
	 */
	public boolean isLastSlot()
	{
		return lastSlot;
	}
	
	/**
	 * do the action of this slot
	 */
	public void doMpSlotAction()
	{
		// send FW byte
		if ( Start.node.getNextWork().getAction() == TtpaConst.SLOT_SEND_FW)
		{
			Transmit.sendFWByte(Start.node.getNextWork().getSendFWValue());
		}
		// SlotSend: send byte
		else if (Start.node.getNextWork().getAction() == TtpaConst.SLOT_SEND)
		{
			Transmit.sendByte(Start.node.getNextWork().getSendValue());
		}
		// SlotRecv: receive byte
		else if (Start.node.getNextWork().getAction() == TtpaConst.SLOT_RECV)
		{
			Start.node.setIoArrayPos(Transmit.recvByte(), Start.node.getNextWork().getRecvPos());
		}
		// SlotExec: call the runnable function of this exec slot
		else if (Start.node.getNextWork().getAction() == TtpaConst.SLOT_EXEC)
		{
			Start.node.getNextWork().getExec().getFunction().run();
		}
		// empty slot
		else if (Start.node.getNextWork().getAction() == TtpaConst.SLOT_EMPTY)
		{
			// nothing to do
		}
	}
	
	/**
	 * calculate checksum of a given array (the last element of the array will be ignored, because it contains the checksum)
	 * 
	 * @param myArray array to calculate the checksum of
	 * @param myArrayLength length of the array
	 * @return checksum of the array elements (except of the last element)
	 */
	public static byte calcCheck(byte[] myArray, byte myArrayLength)
	{
		int i;
		byte checksum;
		/* xor the array elements */
		checksum = myArray[0];
		for ( i = 1; i < myArrayLength - 1; i++) {
			checksum ^= myArray[i]; 
		}
		return checksum;
	}
	
	abstract public void doNextSlot();
	abstract public void rodlStart();
	abstract public void getSectionValues();
	abstract public void getRodlValues();
	
}
