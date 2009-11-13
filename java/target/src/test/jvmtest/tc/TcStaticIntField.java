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

public class TcStaticIntField extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcStaticIntField";
	}
	
	/**
	 * only allow one instance
	 */
	public boolean allowMultipleInstance() {
		return false;
	}
	
	/**
	 * Field to test getfield/putfield instruction on int fields
	 */
	private static int AField;

	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeInt(AField);
	}
	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result = true;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		AField = 0;
		someDummy();
		Result = AField == 0;
		FResult.calcResult(Result, this);
		
		AField = 1;
		someDummy();
		Result = Result && (AField == 1);
		FResult.calcResult(Result, this);
		
		AField = -1;
		someDummy();
		Result = Result && (AField == -1);
		FResult.calcResult(Result, this);

		AField = 0x7FFFFFFF;
		someDummy();
		Result = Result && (AField == 0x7FFFFFFF);
		FResult.calcResult(Result, this);
		
		AField++;
		someDummy();
		Result = Result && (AField == 0x80000000);
		FResult.calcResult(Result, this);
		
		AField = 0xFFFFFFFF;
		someDummy();
		Result = Result && (AField == 0xFFFFFFFF);
		FResult.calcResult(Result, this);

		AField++;
		someDummy();
		Result = Result && (AField == 0);
		FResult.calcResult(Result, this);

		return FResult;
	}

}