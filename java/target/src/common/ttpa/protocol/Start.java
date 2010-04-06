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


import com.jopdesign.sys.JVMHelp;


/**
 * Main
 */
public class Start implements Runnable
{	
	public static Node node;

	
	public void run(NodeMaster nodeMaster){

		node = nodeMaster;
		
		Transmit.initConnection();	// initialize serial port
		
		node.getSectionValues();
		node.getRodlValues();
		node.rodlStart();
		
		/* start timer */
		RoundTimer rt = new RoundTimer(TtpaConst.SLOT_LENGTH);
		JVMHelp.addInterruptHandler(0, rt);
		
		for (;;) 
		{
			/*
			 *  do nothing
			 */
		}	
	}
	
	public void run(NodeSlave nodeSlave) {
	
		node = nodeSlave;
		
		byte firework;				// stores firework byte
		boolean correctFW = true;	// is received FW byte a correct FW byte?
		Transmit.initConnection();	// initialize serial port
		
		/* initialize and start the RODL */
		node.rodlStart();
		RoundTimer rt = null;							// timer object
		RoundTimer.setStarted(false);

		for (;;)
		{
			firework = Transmit.recvFWByte();			// wait for firework byte
	
			// start if FW byte is a MSA FW byte
			if (firework == TtpaConst.FIREWORK[5])		
			{	
				NodeSlave.setSlaveRodlNr(5);			// MSA has RODL nr 5
			
				/* start timer */
				rt = new RoundTimer(TtpaConst.SLOT_LENGTH - TtpaConst.RECV_LENGTH);
				JVMHelp.addInterruptHandler(0, rt);
				RoundTimer.setStarted(true);
				break;
			}
		}
				
		for (;;)
		{
			/* timer is not active => wait for new FW byte */
			if ( !RoundTimer.isStarted() )
			{
				firework = Transmit.recvFWByte();		// wait for FW byte
				correctFW = true;			
				/* FW byte is a MSD FW byte */
				if (firework == TtpaConst.FIREWORK[1])
				{
					NodeSlave.setSlaveRodlNr(1);		// MSD RODL has rodl nr 1
					
					// calculate file name and operation to do from received byte in MSA round
					node.getMsaObject().divideFileNameOp();

					// this node is addressed
					if ( node.getMsaObject().getMsaLogName() == node.getLogName() || node.getMsaObject().getMsaLogName() == 0 )
					{
						node.getMsdObject().setAddressed(true);
						
						// only send data if operation is to write and this node was addressed or it is a broadcast round
						if ( (node.getMsaObject().getOp() == TtpaConst.OP_WRITE) )
						{
							// only send data if there was no checksum error
							if (!(node.getMsaObject().isChecksumError()))
							{
								/* initialize MSD round */
								node.getMsdObject().init(node.getMsaObject().getMsaRecName());
							}
							else
							{
								// there was a checksum error => all data bytes are 0xFF
								node.getMsdObject().initChecksumError();
							}
						}
					}
					node.getMsaObject().setChecksumError(false);	// reset checksum error
				}
				/* FW byte is a MSA FW byte */
				else if (firework == TtpaConst.FIREWORK[5])
				{
					NodeSlave.setSlaveRodlNr(5);		// MSA RODL has RODL nr 5
				}
				/* FW byte is a MP FW byte (RODL 0) */
				else if (firework == TtpaConst.FIREWORK[0])
				{
					NodeSlave.setSlaveRodlNr(0);		// RODL nr 0
					// initialize MP object and calculate first MP slot
					node.getMpObject().init( NodeSlave.getSlaveRodl(0) );
					node.getMpObject().mpSlot();
				}
				/* FW byte is a MP FW byte (RODL 2) */
				else if (firework == TtpaConst.FIREWORK[2])
				{
					NodeSlave.setSlaveRodlNr(2);
					node.getMpObject().init( NodeSlave.getSlaveRodl(2) );
					node.getMpObject().mpSlot();
				}
				/* FW byte is a MP FW byte (RODL 3) */
				else if (firework == TtpaConst.FIREWORK[3])
				{
					NodeSlave.setSlaveRodlNr(3);
					node.getMpObject().init( NodeSlave.getSlaveRodl(3) );
					node.getMpObject().mpSlot();
				}
				/* FW byte is a MP FW byte (RODL 4) */
				else if (firework == TtpaConst.FIREWORK[4])
				{
					NodeSlave.setSlaveRodlNr(4);
					node.getMpObject().init( NodeSlave.getSlaveRodl(4) );
					node.getMpObject().mpSlot();
				}
				/* FW byte is a MP FW byte (RODL 6) */
				else if (firework == TtpaConst.FIREWORK[6])
				{
					NodeSlave.setSlaveRodlNr(6);
					node.getMpObject().init( NodeSlave.getSlaveRodl(6) );
					node.getMpObject().mpSlot();
				}
				/* FW byte is a MP FW byte (RODL 7) */
				else if (firework == TtpaConst.FIREWORK[7])
				{
					NodeSlave.setSlaveRodlNr(7);
					node.getMpObject().init( NodeSlave.getSlaveRodl(7) );
					node.getMpObject().mpSlot();
				}
				/* wrong FW byte */
				else 
				{
					// falsches FB wurde empfangen!
					correctFW = false;
				}
				
				/* correct FW byte, start timer */
				if (correctFW) 
				{
					rt.resetTimer(TtpaConst.SLOT_LENGTH - TtpaConst.RECV_LENGTH);
					JVMHelp.addInterruptHandler(0, rt);
					RoundTimer.setStarted(true);
					node.setLastSlot(false);
				}

			}
		}
	}
	
	public void run() {	
	}
}
