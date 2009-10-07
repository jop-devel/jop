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

public class TcFloatField extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcFloatField";
	}
	
	/**
	 * Field to test getfield/putfield instruction on float fields
	 */
	public float AField;

	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeFloat(AField);
	}
	
	/**
	 * Test case method, use not explicitly fp-strict floats
	 */
	public TestCaseResult run() {
		boolean Result = true;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		AField = 0;		// exact representation 0.0b
		someDummy();
		Result = AField == 0;
		FResult.calcResult(Result, this);
		
		AField = 1;		// exact representation 1.0b
		someDummy();
		Result = Result && (AField == 1);
		FResult.calcResult(Result, this);
		
		AField = -1;	// exact representation -1.0b
		someDummy();
		Result = Result && (AField == -1);
		FResult.calcResult(Result, this);

		AField = (float)-0.5; // exact representation -0.1b
		someDummy();
		Result = Result && (AField == -0.5);
		FResult.calcResult(Result, this);
		
		AField++;			// exact representation 0.1b
		someDummy();
		Result = Result && (AField == 0.5);
		FResult.calcResult(Result, this);
		
		AField=(float)3.141592;
		someDummy();
		Result = Result && (3.141591 <= AField && AField <=3.141593);
		FResult.calcResult(Result, this);
				
		return FResult;
	}

}