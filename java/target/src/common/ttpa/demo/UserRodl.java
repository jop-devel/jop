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

package ttpa.demo;

import ttpa.protocol.*;



public class UserRodl
{
	
	/********************* RODL DEFININIEREN ********************* */
	
		/** RODL1 bzw. RODL5 sind MSD bzw. MSA Runden **/
	
		public static Rodl myRodl01 = new Rodl(0, 6);
		public static Rodl myRodl02 = new Rodl(2, 2);
		
		public static Rodl myRodlMSA = new Rodl(5, 5);  // MSA RODL
		public static Rodl myRodlMSD = new Rodl(1, 5);  // MSD RODL
		
	/********************* SLOTS DEFINIEREN ********************* */
	
		/*** for Rodl01 ***/

		public static SlotRecv s3RecvRodl01 = new SlotRecv(3, 0, 1);
		public static SlotRecv s4RecvRodl01 = new SlotRecv(4, 1, 1);
		public static SlotExec s5ExecRodl01 = new SlotExec(5, new myRunnables() );
		public static SlotExec s6ExecRodl01 = new SlotExec(6, new exec2());
		public static SlotSend s7SendRodl01 = new SlotSend(7, 2, 1);
		public static SlotEOR s8EORRodl01 = new SlotEOR(8);
		
		/*** for Rodl02 ***/
		public static SlotSend s0SendRodl02 = new SlotSend(1, 2, 1);
		public static SlotSend s1SendRodl02 = new SlotSend(3, 3, 1);
	
	/********************* NODE ZUWEISUNG ********************* */
	
		public static NodeSlave node = new NodeSlave(0x20, 2, (byte) 0x02);

	/********************* END OF DEFINITION ********************* */

		
	public static void start()
	{
		
		/********************* RODL ZUWEISEN ********************* */
			node.addRodl(myRodl01);
			node.addRodl(myRodl02);
			node.addRodl(myRodlMSD);
			node.addRodl(myRodlMSA);
		
		/********************* SLOTS ZUWEISEN ********************* */
			/*** for Rodl01 ***/
			myRodl01.setRodlSlot(s3RecvRodl01, 0);
			myRodl01.setRodlSlot(s4RecvRodl01, 1);
			myRodl01.setRodlSlot(s5ExecRodl01, 2);
			myRodl01.setRodlSlot(s6ExecRodl01, 3);
			myRodl01.setRodlSlot(s7SendRodl01, 4);
			myRodl01.setRodlSlot(s8EORRodl01, 5);
		
			/*** for Rodl02 ***/
			myRodl02.setRodlSlot(s0SendRodl02, 0);
			myRodl02.setRodlSlot(s1SendRodl02, 1);
		
		/********************* END OF DEFINITION ********************* */
			
	}
	
	public static void setVariableValues() 
	{
		s7SendRodl01.setSendValue((byte)Appl.countExec2, (byte) 2);
	}
	
}
