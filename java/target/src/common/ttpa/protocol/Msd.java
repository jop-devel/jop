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
 * MSD
 */
public class Msd 
{
	/** data to send */
	private byte[] msdArray = new byte[TtpaConst.MSD_LENGTH];
	
	/** was this node addressed to send or receive? */
	private boolean isAddressed = false;
	
	/**
	 * initialize msd_array if an existing file was addressed
	 * 
	 * @param myFileOp file name and operation
	 * @param myRec record name
	 */
	public void init(int myRec)
	{
		// if the file that was addressed exists, then send data
		if ( Start.node.getMsaObject().getFile() == Start.node.getFileName() )
		{
			msdArray[0] = TtpaConst.FIREWORK[1];
			msdArray[1] = Start.node.getIoArrayPos(myRec*4 - 4);
			msdArray[2] = Start.node.getIoArrayPos(myRec*4 - 3);
			msdArray[3] = Start.node.getIoArrayPos(myRec*4 - 2);
			msdArray[4] = Start.node.getIoArrayPos(myRec*4 - 1);
			msdArray[5] = Node.calcCheck(msdArray, TtpaConst.MSD_LENGTH);
		}
		// documentation file was addressed
		else if ( Start.node.getMsaObject().getFile() == TtpaConst.FILE_DOC )
		{
			msdArray[0] = TtpaConst.FIREWORK[1];
			msdArray[1] = TtpaConst.DOCFILE[myRec*4 - 4];
			msdArray[2] = TtpaConst.DOCFILE[myRec*4 - 3];
			msdArray[3] = TtpaConst.DOCFILE[myRec*4 - 2];
			msdArray[4] = TtpaConst.DOCFILE[myRec*4 - 1];
			msdArray[5] = Node.calcCheck(msdArray, TtpaConst.MSD_LENGTH);
		}
	}
	
	/**
	 * if there was a checksum error, then set all data bytes and the checksum are set to 0xFF
	 */
	public void initChecksumError()
	{
		msdArray[0] = TtpaConst.FIREWORK[1];
		for (int i = 1; i < TtpaConst.MSD_LENGTH ; i++)
		{
			msdArray[i] = (byte) 0xFF;
		}
	}

	/**
	 * do a msd slot
	 * 
	 * @return true, if this was the last slot of the round, false else
	 */
	public boolean doMsdSlot()
	{
		/* Master */
		if ( Start.node.isMaster() )
		{
			// first slot, send FW byte
			if ( Start.node.getSlotCounter() == 0 )
			{
				Transmit.sendFWByte(TtpaConst.FIREWORK[1]);
			}
			// master sends
			else if ( isAddressed )
			{
				Transmit.sendByte(msdArray[Start.node.getSlotCounter()]);
			}
			// master receives
			else
			{
				msdArray[Start.node.getSlotCounter()] = Transmit.recvByte();
			}
		}
		/* Slave */
		else
		{
			// send a byte if this node was addressed and operation is to send
			if ( isAddressed && Start.node.getMsaObject().getOp() == TtpaConst.OP_WRITE )
			{
				Transmit.sendByte(msdArray[Start.node.getSlotCounter()]);
			}
			// receive a byte if this node was addressed and operation is read
			else if ( isAddressed && Start.node.getMsaObject().getOp() == TtpaConst.OP_READ )
			{
				msdArray[Start.node.getSlotCounter()] = Transmit.recvByte();
			}
			
		}
		
		// is this the last slot?
		if (Start.node.getSlotCounter() == TtpaConst.MSD_LENGTH - 1) 
		{
			isAddressed = false;	// reset the variable
			return true;			// this was the last slot
		}
		
		return false;
	}

	/**
	 * @param isAddressed true if this node was addressed false else
	 */
	public void setAddressed(boolean isAddressed)
	{
		this.isAddressed = isAddressed;
	}
	
	/**
	 * @param pos position in the msdArray
	 * @return byte of the msdArray at the given position
	 */
	public byte getMsdArrayPos(int pos)
	{
		return msdArray[pos];
	}
	
}
