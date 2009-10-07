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
package jvmtest.base;

import java.io.*;

/**
 * Calculate a simple hash value for a test case for VMs where
 * serialization is fully supported. The <code>writeToStream</code>
 * method from the class <code>TestCase</code> is not necessary in that case.
 * 
 * @author Günther Wimpassiner
 *
 * @see JOP, The Java Optimized Processor at http://www.jopdesign.com
 * @see TestCaseResult
 * @see TestCase
 */
public class SerializeTestCaseResult extends TestCaseResult {

	/**
	 * internal hash value
	 */
	private long Hash;

	/**
	 * Constructor
	 * @param AResult new result value for test case
	 * @param ATestCase test case for which the hash value should be calculated
	 */
	public SerializeTestCaseResult(boolean AResult, TestCase ATestCase) {
		super(AResult, ATestCase);
	}

	/**
	 * Return the hash value from the internal hash value
	 */
	/* @Override */
	public String getHash() {
		String s = Long.toHexString(Hash);
		char[] head = new char[20-s.length()];
		
		for (int i=0;i<head.length;i++) {
			head[i]='0';
		}
		
		return (new String(head,0,head.length))+s;
	}

	/**
	 * Reset the internal hash value to <code>0L</code>
	 */
	/* @Override */
	public void resetHash() {
		Hash = 0L;
	}
	
	/**
	 * Calculate the hash value for a given byte stream.
	 *  
	 * This function can be called several times to
	 * include more states of a stream
	 *  
	 * @param baStream Stream to calculate the hash from
	 */
	private void calcStream(ByteArrayOutputStream baStream) {
		for (byte bData : baStream.toByteArray()) {
			Hash = (Hash << 8) | ((Hash >>> 56)^ bData);
		}
	}
	

	/**
	 * Calculate the has value for a given test case.
	 *  
	 * This function can be called several times to
	 * include more states of an object
	 * or different objects in the same hash value
	 *  
	 * @param ATestCase test case for the calculation
	 */
	/* @Override */
	public void calcHash(TestCase ATestCase) {

		if (ATestCase != null) {		
			ByteArrayOutputStream baStream = new ByteArrayOutputStream(1024);

			try {
				ObjectOutputStream objStream = new ObjectOutputStream(baStream);
				objStream.writeObject(ATestCase);
				calcStream(baStream);
			} catch (IOException e) {
				Result = false;		
				Hash = 0L;

				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Method to include the int value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an int
	 * 
	 * @param iValue integer value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	/* @Override */
	public void calcHashInt(int iValue){
		ByteArrayOutputStreamEx baStream = new ByteArrayOutputStreamEx();
		baStream.writeInt(iValue);
		calcStream(baStream);		
	}

	/**
	 * Method to include the long value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an long
	 * 
	 * @param lValue long value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	/* @Override */
	public void calcHashLong(long lValue) {
		ByteArrayOutputStreamEx baStream = new ByteArrayOutputStreamEx();
		baStream.writeLong(lValue);
		calcStream(baStream);	
	}
	
	/**
	 * Method to include the float value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an float
	 * 
	 * @param fValue float value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	/* @Override */
	public void calcHashFloat(float fValue) {
		ByteArrayOutputStreamEx baStream = new ByteArrayOutputStreamEx();
		baStream.writeFloat(fValue);
		calcStream(baStream);	
	}

	/**
	 * Method to include the double value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an double
	 * 
	 * @param dValue double value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	/* @Override */
	public void calcHashDouble(double dValue) {
		ByteArrayOutputStreamEx baStream = new ByteArrayOutputStreamEx();
		baStream.writeDouble(dValue);
		calcStream(baStream);			
	}
	
	/**
	 * Method to include the String value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an String
	 * 
	 * @param sValue String value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	/* @Override */
	public void calcHashString(String sValue) {
		ByteArrayOutputStreamEx baStream = new ByteArrayOutputStreamEx();
		baStream.writeString(sValue);
		calcStream(baStream);		
	}
	

}
