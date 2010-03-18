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

/**
 * MSA
 */
public class Msa 
{
	
	/** firework, epoch counter, log_name, file name / op, rec_name, checksum */
	private byte[] msaArray = new byte[TtpaConst.MSA_LENGTH];
	/** file name */
	private byte file;	
	/** operation */
	private byte op;		
	/** if there was a checksum error in the msa round all msd data bytes are set to 0xFF */
	private boolean checksumError = false;
	
	/** log name that the master will send in MSA round */
	private byte masterMsaLog = 0x01;
	/** record name that the master will send in MSA round */
	private byte masterMsaRec = 0x01;
	/** file/op byte that the master will send in MSA round */
	private byte masterMsaFileOp;
	
	public Msa() {
		msaArray[0] = TtpaConst.FIREWORK[5];
	}
	
	/**
	 * initializes the MSA Round
	 * 
	 * @param ectrCnt epoch counter counts every roudn
	 * @param logName logical name of the node
	 * @param fileOp filename and operation
	 * @param recName record name
	 */
	public void init(byte ectrCnt, byte logName, byte fileOp, byte recName) 
	{
		this.msaArray[0] = TtpaConst.FIREWORK[5];
		this.msaArray[1] = ectrCnt;		// epoch counter
		this.msaArray[2] = logName;		// logical name of the node
		this.msaArray[3] = fileOp;		// file name and operation (read, write, execute) (bit 7...2 = file, bit 0..1 = op)
		this.msaArray[4] = recName;		// record name
		this.msaArray[5] = Node.calcCheck(msaArray, TtpaConst.MSA_LENGTH);
	}
	
	/**
	 * @return logical name that was sent in the MSA round
	 */
	public byte getMsaLogName()
	{
		return msaArray[2];
	}
	
	/**
	 * @return file name and operation that was sent in the MSA round
	 */
	public byte getMsaFileOp() 
	{
		return msaArray[3];
	}
	
	/**
	 * @return record name that was sent in the MSA round
	 */
	public byte getMsaRecName() 
	{
		return msaArray[4];
	}
	
	/**
	 * sends the MSA frame
	 * 
	 * @return true if this was the last slot, false else
	 */
	public boolean msaSendFrame() 
	{
		// first slot (send FW byte)
		if (Start.node.getSlotCounter() == 0) 
		{
			Transmit.sendFWByte(msaArray[0]);
		}
		// all other slots
		else
		{
			Transmit.sendByte(msaArray[Start.node.getSlotCounter()]);
		}
		// this is the last slot
		if (Start.node.getSlotCounter() == TtpaConst.MSA_LENGTH - 1) 
		{
			return true;
		}
		return false;
	}
	 
	/**
	 * @param myChecksumError the boolean value for the object
	 */
	public void setChecksumError(boolean myChecksumError) 
	{
		checksumError = myChecksumError;
	}
	
	/**
	 * @return checksum_error
	 */
	public boolean isChecksumError()
	{
		return checksumError;
	}
	
	/**
	 * saves the received data in the msa_array
	 * 
	 * @return true if there was a checksum error, false else
	 */
	public boolean msaRecvFrame()
	{
		msaArray[Start.node.getSlotCounter()] = Transmit.recvByte();
		// this is the last slot
		if (Start.node.getSlotCounter() == TtpaConst.MSA_LENGTH - 1) 
		{
			// calculate checksum
			if ( Node.calcCheck(msaArray, (byte) (TtpaConst.MSA_LENGTH )) != msaArray[TtpaConst.MSA_LENGTH - 1] ) 
			{
				setChecksumError(true);
				System.out.println("Checksum Error");
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @param file file name
	 */
	public void setFile(byte file) 
	{
		this.file = file;
	}

	/**
	 * @return file name
	 */
	public byte getFile() 
	{
		return file;
	}
	
	/**
	 * @return type of operation
	 */
	public byte getOp() 
	{
		return op;
	}
	
	/**
	 * @param myOp type of operation
	 */
	public void setOp(byte myOp) 
	{
		this.op = myOp;
	}
	
	/**
	 * calculates the file name and operation of the received MSA bytes
	 * (file name and operation are sent in one byte and must be separated)
	 */
	public void divideFileNameOp()
	{
		byte myFile, myOp;
		byte myByte = getMsaFileOp();		// get value to split
		
		myOp = (byte) (myByte & ((byte) 3));	// get operation (the two LSB)
		
		myFile = myByte >>>= 2;					// shift out the op bits
		myFile &= (byte) 63;					// get file name
		
		setFile(myFile);
		setOp(myOp);
	}
	
	/**
	 * calculate the combined file, op value to send for MSA round of a byte file and op
	 * the 6 most significant bits are file the 2 least significant bits are op
	 * 
	 * @param myFile file name to address
	 * @param myOp operation to do
	 */
	public void calcFileNameOp(byte myFile, byte myOp)
	{
		masterMsaFileOp = myFile <<= 2;	// shift 2 to left for operation to fit in
		myOp &= (byte) 3;					// make sure the 6 msb bits are 0
		masterMsaFileOp |= myOp;			// combine file and op
	}

	/**
	 * @param master_msa_log logical name that the master will send in MSA
	 */
	public void setMasterMsaLog(byte master_msa_log)
	{
		this.masterMsaLog = master_msa_log;
	}

	/**
	 * @return logical name that the master will send in MSA
	 */
	public byte getMasterMsaLog()
	{
		return masterMsaLog;
	}

	/**
	 * @param master_msa_rec record name that the master will send in MSA
	 */
	public void setMasterMsaRec(byte master_msa_rec)
	{
		this.masterMsaRec = master_msa_rec;
	}

	/**
	 * @return record name that the master will send in MSA
	 */
	public byte getMasterMsaRec()
	{
		return masterMsaRec;
	}

	/**
	 * @return combined file/op byte that the master will send in MSA
	 */
	public byte getMasterMsaFileOp()
	{
		return masterMsaFileOp;
	}

}
