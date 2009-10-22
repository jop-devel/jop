/* jvmtest - Testing your VM 
  Copyright (C) 20009, Guenther Wimpassinger

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
package jvmtest.tc;

import jvmtest.base.*;

public class TcInstrXipush extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcInstrXipush";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	private boolean ism1(int v) {
		return v==-1;
	}
	
	private boolean is0(int v) {
		return v==0;
	}
	
	private boolean is1(int v) {
		return v==1;
	}
	
	private boolean is6(int v) {
		return v==6;
	}
	
	private boolean ism6(int v) {
		return v==-6;
	}	

	private boolean is127(int v) {
		return v==127;
	}
	
	private boolean ism127(int v) {
		return v==-127;
	}
	
	private boolean is128(int v) {
		return v==128;
	}
	
	private boolean ism128(int v) {
		return v==-128;
	}
	
	private boolean is255(int v) {
		return v==255;
	}

	private boolean ism255(int v) {
		return v==-255;
	}
	
	private boolean is32767(int v) {
		return v==32767;
	}

	private boolean ism32767(int v) {
		return v==-32767;
	}

	private boolean is32768(int v) {
		return v==32768;
	}

	private boolean ism32768(int v) {
		return v==-32768;
	}
	
	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result = true;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		/* const */
		Result = Result && is0(0);
		Result = Result && is0(-0);
		Result = Result && !is0(1);
		FResult.calcHashInt(0);
		
		Result = Result && is1(1);
		Result = Result && !is1(-1);
		Result = Result && ism1(-1);
		Result = Result && !ism1(1);
		FResult.calcHashInt(1);
		FResult.calcHashInt(-1);
		
		/* bipush */
		Result = Result && is6(6);
		Result = Result && !is6(-6);		
		Result = Result && ism6(-6);
		Result = Result && !ism6(6);		
		FResult.calcHashInt(6);
		FResult.calcHashInt(-6);

		Result = Result && is127(127);
		Result = Result && !is127(-127);		
		Result = Result && ism127(-127);
		Result = Result && !ism127(127);
		FResult.calcHashInt(127);
		FResult.calcHashInt(-127);
		
		/* sipush - bipush */
		Result = Result && is128(128);
		Result = Result && !is128(-128);		
		Result = Result && ism128(-128);
		Result = Result && !ism128(128);
		FResult.calcHashInt(128);
		FResult.calcHashInt(-128);

		/* sipush */
		Result = Result && is255(255);
		Result = Result && !is255(-255);		
		Result = Result && ism255(-255);
		Result = Result && !ism255(255);
		FResult.calcHashInt(255);
		FResult.calcHashInt(-255);
		
		Result = Result && is32767(32767);
		Result = Result && !is32767(-32767);		
		Result = Result && ism32767(-32767);
		Result = Result && !ism32767(32767);
		FResult.calcHashInt(32767);
		FResult.calcHashInt(-32767);
		
		/* ldc - sipush */
		Result = Result && is32768(32768);
		Result = Result && !is32768(-32768);		
		Result = Result && ism32768(-32768);
		Result = Result && !ism32768(32768);
		FResult.calcHashInt(32768);
		FResult.calcHashInt(-32768);

		FResult.calcResult(Result, this);
		
		return FResult;
	}
}