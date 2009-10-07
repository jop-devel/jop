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

public class TcInstrAthrow extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcInstrAthrow";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	/* @Override */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	/**
	 * Test case method
	 */
	/* @Override */
	public TestCaseResult run() {
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		boolean Result;	
		
		boolean exp=false;
		try {
			throw new ArithmeticException();
		} catch (ArithmeticException ae) {
			exp=true;
			FResult.calcHashInt(0x132523);
		}
		Result = exp;
		
		exp=false;
		try {
			try {
				throw new ArithmeticException();
			} catch(IndexOutOfBoundsException ie) {
				Result = false;				
			}
		} catch (ArithmeticException ae) {
			exp=true;
			FResult.calcHashInt(0x4352352);
		}
		Result = Result && exp;
		
		FResult.calcResult(Result, this);
		
		return FResult;
	}

}