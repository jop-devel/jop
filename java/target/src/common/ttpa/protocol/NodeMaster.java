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


import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * NodeMaster
 */
public class NodeMaster extends Node 
{
	
	/** the rose file of the node master */
	private Rosefile rosefile;
	/** declares which section of the rose file is active */
	private RoseSect masterActiveSection;
	/** the active rodl of the node */
	private Rodl masterActiveRodl;
	/** irg length of a round */
	private int masterIrgLength;
	/** number of rodls */
	private int masterRodlAnz;
	/** rodl counter */
	private int masterRodlCounter = 0;
	/** stores the number of a rodl */
	private int masterRodlNr;
	/** epoch counter, is increased every round */
	private byte epochCounter;
	
	/**
	 * @param myRosefile rose file of the node master
	 * @param myFileName file name of the node
	 * @param myRecAnz number of records
	 * @param myLogName logical name of the node
	 */
	public NodeMaster(Rosefile myRosefile, int myFileName, int myRecAnz, byte myLogName) 
	{
		super(TtpaConst.MASTER, myFileName, myRecAnz, myLogName);
		this.rosefile = myRosefile;
	}
	
	/**
	 * is called when a new round is started
	 * increases the epoch counter
	 */
	private void newRound()
	{
		epochCounter++;
	}
	
	/**
	 * stores the active section in master_active_section
	 * and the number of rodls in master_rodl_anz
	 */
	public void getSectionValues()
	{
		masterActiveSection = rosefile.getActiveSection();
		masterRodlAnz = masterActiveSection.getRodlDescAnz();
	}

	/**
	 * stored the active rodl in master_active_rodl,
	 * the irg length of the comming round in master_irg_length
	 * and the number of the active rodl in master_rodl_nr
	 */
	public void getRodlValues()
	{
		masterActiveRodl = masterActiveSection.getRodlDesc(masterRodlCounter).getRodl();
		masterIrgLength = masterActiveSection.getRodlDesc(masterRodlCounter).getIrg();
		masterRodlNr = masterActiveRodl.getRodlNr();
	}
	
	/**
	 * is called when a new rodl begins
	 */
	public void rodlStart()
	{
		Start.node.setSlotCounter(0);		// reset slot counter
		newRound();								// increase epoch counter
		
		// MSA
		if ( masterRodlNr == TtpaConst.MSA )
		{
			byte msa_log = Start.node.getMsaObject().getMasterMsaLog();	// read log name to send
			byte msa_rec = Start.node.getMsaObject().getMasterMsaRec();	// read record name to send
			/* calculate the combined file name and operation byte
			 * file name is 0x3D (documentation file) and operation is to write */
			Start.node.getMsaObject().calcFileNameOp( (byte) 0x3D, TtpaConst.OP_WRITE );
			// initialize MSA object
			getMsaObject().init( epochCounter, msa_log, Start.node.getMsaObject().getMasterMsaFileOp(), msa_rec );	
			/* increase logical name and return to 0x01 if 0xFB is reached
			 * because 0xFB is the first value that is not used to address slave nodes and 0x00 broadcast */
			if ( msa_log++ == 0xFB - 1 )
			{
				msa_log = (byte) 0x01;		// return to log name 0x01
				if ( msa_rec++ == 0x02 )	// increase record name
				{
					msa_rec = 0x01;			// return to record name 0x01 if 0x02 was reached
				}
			}
			Start.node.getMsaObject().setMasterMsaLog(msa_log);		// save new logical name to be sent
			Start.node.getMsaObject().setMasterMsaRec(msa_rec);		// save new record name to be sent
		}
		// MSD
		else if ( masterRodlNr == TtpaConst.MSD )
		{
			// this node is addressed
			if ( Start.node.getMsaObject().getMsaLogName() == Start.node.getLogName() || Start.node.getMsaObject().getMsaLogName() == TtpaConst.BROADCAST )
			{
				Start.node.getMsdObject().setAddressed(true);
				
				// operation is write
				if ( Start.node.getMsaObject().getOp() == TtpaConst.OP_WRITE )
				{
					/* initialize MSD round */
					Start.node.getMsdObject().init(Start.node.getMsaObject().getMsaRecName());
				}
			}
		}
		// coming round is a MP round
		else
		{
			// initialize MP object
			getMpObject().init(masterActiveRodl);
			// calculate next MP slot
			setLastSlot(getMpObject().mpSlot());
		}
	}
	
	/**
	 * this function is called by the timer each slot
	 * it executes whatever in this slot has to be done
	 */
	public void doNextSlot()
	{
		if ( masterRodlNr == TtpaConst.MSA )		// MSA slot
		{
			setLastSlot(getMsaObject().msaSendFrame());
		}
		else if ( masterRodlNr == TtpaConst.MSD )	// MSD slot
		{
			// do msd slots
			setLastSlot(Start.node.getMsdObject().doMsdSlot());
		}
		else							// MP slot
		{
			Start.node.doMpSlotAction();
		}
		
		// increase slot counter
		Start.node.setSlotCounter(Start.node.getSlotCounter() + 1);
		
		// this was not the last slot and it is a MP round
		if ( !isLastSlot() && masterRodlNr != 1 && masterRodlNr != 5 )
		{
			// calculate the next MP slot
			setLastSlot(getMpObject().mpSlot());
		}
		// this is the last slot
		else if ( isLastSlot() )
		{
			rodlEnd();		// end of the round, calculate next round
		}

	}
	
	/**
	 * is called when a round ends
	 * sets the timer to IRG and calculates values for next round
	 */
	public void rodlEnd()
	{
		setTimerToIrg(masterIrgLength);		// IRG
		
		// increaste master_rodl_counter or reset the counter if all rodls are done
		if (masterRodlCounter++ == masterRodlAnz - 1) {
			masterRodlCounter = 0;
		}
				
		/* calculate values for the next round */
		getRodlValues();
		rodlStart();
	}
	
	/**
	 * sets the timer to length of the IRG
	 * 
	 * @param irg_length length of irg in slots (1-15)
	 */
	public void setTimerToIrg(int irg_length)
	{
		// set timer to value
		RoundTimer.setStartTime( RoundTimer.getStartTime() + (irg_length * TtpaConst.SLOT_LENGTH) );
		Native.wr(1, Const.IO_INTCLEARALL);		// clear pending interrupts
		Native.wr(RoundTimer.getStartTime(), Const.IO_TIMER);	// write value to timer
		Native.wr(1, Const.IO_INTCLEARALL);
	}

}
