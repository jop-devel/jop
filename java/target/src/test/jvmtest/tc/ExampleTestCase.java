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

public class ExampleTestCase extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "ExampleTestCase";
	}
	
	/**
	 * Demo field
	 */
	public int AField;

	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	/* @Override */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeInt(AField);
	}
	
	/**
	 * Test case method
	 */
	/* @Override */
	public TestCaseResult run() {
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		AField = 0x120;		
		FResult.calcResult(AField==0x120, this);
		
		return FResult;
	}

}