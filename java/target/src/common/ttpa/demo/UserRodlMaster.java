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

import ttpa.protocol.NodeMaster;
import ttpa.protocol.Rodl;
import ttpa.protocol.RodlDesc;
import ttpa.protocol.RoseSect;
import ttpa.protocol.RoseTime;
import ttpa.protocol.Rosefile;
import ttpa.protocol.SlotEOR;
import ttpa.protocol.SlotExec;
import ttpa.protocol.SlotRecv;
import ttpa.protocol.SlotSend;
import ttpa.protocol.TtpaConst;

public class UserRodlMaster {
	
	/********************* DEFINE RODLS ********************* */
	
		/** RODL1 and RODL5 are MSD and MSA Rounds **/
	
		public static Rodl myRodl01 = new Rodl(0, 6);
		public static Rodl myRodl02 = new Rodl(2, 3);
		
		public static Rodl myRodlMSA = new Rodl(5, 0);	// MSA RODL
		public static Rodl myRodlMSD = new Rodl(1, 0);	// MSD RODL
		
	/********************* DEFINE SLOTS ********************* */
	
		/*** for Rodl00 ***/
		
		public static SlotRecv s3RecvRodl01 = new SlotRecv(3, 0, 1);
		public static SlotRecv s4RecvRodl01 = new SlotRecv(4, 1, 1);
		public static SlotExec s5ExecRodl01 = new SlotExec(5, new myRunnables() );
		public static SlotExec s6ExecRodl01 = new SlotExec(6, new exec2() );
		public static SlotSend s7SendRodl01 = new SlotSend(7, 2, 1);
		public static SlotEOR s8EORRodl01 = new SlotEOR(8);
			
		/*** for Rodl02 ***/
		public static SlotSend s0SendRodl02 = new SlotSend(1, 2, 1);
		public static SlotSend s1SendRodl02 = new SlotSend(3, 3, 1);
		public static SlotEOR s4EORRodl02 = new SlotEOR(4);
	
	/********************* DEFINE ROSE TIME ********************* */
		
		/*** for Sect01 ***/
		public static RoseTime roseTimeSect01 = new RoseTime(100, 100);
		/*** for Sect02 ***/
		public static RoseTime roseTimeSect02 = new RoseTime(100, 100);
	
	/********************* DEFINE ROSE DESCRIPTION ********************* */
		
		/*** for Rodl01 ***/
		public static RodlDesc rodlDescRodl01 = new RodlDesc(myRodl01, false, 1);
		/*** for Rodl02 ***/
		public static RodlDesc rodlDescRodl02 = new RodlDesc(myRodl02, false, 10);
		/*** for MSA ***/
		public static RodlDesc rodlDescMSA = new RodlDesc(myRodlMSA, false, 1);
		/*** for MSD ***/
		public static RodlDesc rodlDescMSD = new RodlDesc(myRodlMSD, false, 1);
			
	/********************* DEFINE ROSE SECTION ********************* */
		
		public static RoseSect roseSect01 = new RoseSect(3, roseTimeSect01);
		public static RoseSect roseSect02 = new RoseSect(3, roseTimeSect02);
		
	/********************* DEFINE ROSE FILE ********************* */
		
		public static Rosefile myRoseFile = new Rosefile(roseSect01, roseSect01, roseSect02);
	
	/********************* NODE ALLOCATION ********************* */
	
		public static NodeMaster node = new NodeMaster(myRoseFile, 0x20, 2, TtpaConst.LOG_NAME_MASTER);
	
	/********************* END OF DEFINITION ********************* */

		
	public static void start() {
			
		/********************* Allocate Slots ********************* */
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
				myRodl02.setRodlSlot(s4EORRodl02, 2);
		
		/********************* ALLOCATE THE RODL DESCS TO THE ROSE SECTION ********************* */
		
			/*** for SECTION 1 ***/	
				roseSect01.setRodlDesc(rodlDescMSA, 0);
				roseSect01.setRodlDesc(rodlDescRodl01, 1);
				roseSect01.setRodlDesc(rodlDescMSD, 2);
								
			/*** for SECTION 2 ***/	
				roseSect02.setRodlDesc(rodlDescMSA, 0);
				roseSect02.setRodlDesc(rodlDescRodl02, 1);
				roseSect02.setRodlDesc(rodlDescMSD, 2);
				
		/********************* END OF DEFINITION ********************* */
			
	}
	
	/**
	 * set the values to send
	 */
	public static void setVariableValues()
	{
		s7SendRodl01.setSendValue((byte)Appl.countExec2, 2);
	}
	
}
