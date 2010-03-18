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
 * constants for TTP/A protocol
 */
public class TtpaConst
{
	
	/** Documentation File 0x3D */
	public static final byte[] DOCFILE = { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 };	
	/** address of documentation file */
	public static final int FILE_DOC = 0x3D;
	
	/** all firework bytes */
	public static final byte[] FIREWORK = {(byte) 0x78, (byte) 0x49, (byte) 0xBA, (byte) 0x8B, (byte) 0x64, (byte) 0x55, (byte) 0xA6, (byte) 0x97};
	
	/** epoch counter initialization */
	public static final byte ECTR_INIT = 0;
	
	/** Master */
	public static final boolean MASTER = true;
	/** Slave */
	public static final boolean SLAVE = false;
	
	/** logical name of the master */
	public static final byte LOG_NAME_MASTER = (byte) 0xFE;
	
	/** length of a slot (13 bits) with baud rate 9600 */
	public static final int SLOT_LENGTH = 1355;
	/** length of a uart frame (11 bits) with baud rate 9600 */
	public static final int RECV_LENGTH = 1174;
	
	/** number of slots of a MSA round */
	public static final byte MSA_LENGTH = 6;
	/** number of slots of a MSD round */
	public static final byte MSD_LENGTH = 6;

	/** rodl number of MSA */
	public static final int MSA = 5;
	/** rodl number of MSD */
	public static final int MSD = 1;
	
	/** broadcast round */
	public static final byte BROADCAST = 0;
	
	/** empty slot */
	public static final int SLOT_EMPTY = 0;
	/** send fw byte */
	public static final int SLOT_SEND_FW = 1;
	/** send slot */
	public static final int SLOT_SEND = 2;
	/** receive slot */
	public static final int SLOT_RECV = 3;
	/** execution slot */
	public static final int SLOT_EXEC = 4;
	/** end of round */
	public static final int SLOT_EOR = 5;
	
	/** operation read */
	public static final byte OP_READ = 0;
	/** operation write */
	public static final byte OP_WRITE = 1;
	
}
