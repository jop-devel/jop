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

/**
 * Base class for results of a test case. It contains
 * a truth value and a hash value. The truth value from
 * <code>getResult()</code> show if the test case have
 * run successfully. The hash value provided by <code>getHash()</code>
 * is used to compare the result of different VM implementations.
 * 
 * This is an abstract class and is not allowed to be instanced. It is
 * intended to be used by an subclass of the TestCase class.
 * 
 * @author Günther Wimpassinger
 * 
 * @see TestCase
 *
 */
public abstract class TestCaseResult {
	
	/**
	 * Result value of the test case. Should be set to <code>true</code> if
	 * the test case succeed by a call to <code>calcResult(true, tc)</code>
	 */
	protected boolean Result;
	
	/**
	 * To store a Message specified by the method which run the test case
	 * (need not be the <code>TestCase.run</code> method.
	 */
	protected String RunMessage;

	/**
	 * Constructor for TestCaseResult
	 * @param AResult new result value for this test case
	 * @param ATestCase provide a TestCase class to calculate the hash value
	 * 			 of it as a part of the result
	 */
	public TestCaseResult(boolean AResult, TestCase ATestCase) {
		setRunMessage("");
		resetHash();
		calcResult(AResult, ATestCase);
	}

	/**
	 * Default constructor for TestCaseResult
	 */
	public TestCaseResult() {
		this(false, null);
	}
	
	/**
	 * Get the Run Message provided by an external method
	 * @return The <code>RunMessage</code>
	 */
	public String getRunMessage() {
		return RunMessage;
	}
	
	/**
	 * Set the Run Message for the <code>TestCaseResult</code>
	 * @param ANewRunMessage The new message
	 */
	public void setRunMessage(String ANewRunMessage) {
		RunMessage = ANewRunMessage;
	}
	
	
	/**
	 * Getter for the boolean result value
	 * @return <code>true</code> if the test case succeeded otherwise <code>false</code>
	 */
	public boolean getResult() {
		return Result;
	}

	/**
	 * Getter for the hash value of the test case
	 * @return the hash value of the test case calculated by overridden method calcHash
	 */
	public String getHash() {
		return "no hash";
	}
	
	/**
	 * Abstract method to reset the hash value to an initial value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	public abstract void resetHash();
	
	/**
	 * Abstract method to calculate the hash value. Must be implemented
	 * by the subclasses.
	 * 
	 * This function can be called several times to
	 * include more states of an object
	 * or different objects in the same hash value
	 * 
	 * @param ATestCase The test case for which the hash should be calculated
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	public abstract void calcHash(TestCase ATestCase);
	
	/**
	 * Abstract method to include the int value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an int
	 * 
	 * @param iValue integer value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	public abstract void calcHashInt(int iValue);

	/**
	 * Abstract method to include the long value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an long
	 * 
	 * @param lValue long value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	public abstract void calcHashLong(long lValue);
	
	/**
	 * Abstract method to include the float value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an float
	 * 
	 * @param fValue float value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	public abstract void calcHashFloat(float fValue);

	/**
	 * Abstract method to include the double value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an double
	 * 
	 * @param dValue double value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	public abstract void calcHashDouble(double dValue);
	
	/**
	 * Abstract method to include the String value in the hash
	 * 
	 * This function can be called several times to
	 * include more states of an String
	 * 
	 * @param sValue String value
	 * @see SimpleTestCaseResult
	 * @see SerializedTestCaseResult
	 */
	public abstract void calcHashString(String sValue);
	
	

	/**
	 * Reset the result and the hash value to an initial value.
	 * Result is set to <code>false</code> and the abstract method
	 * <code>resetHash()</code> is called
	 */
	public void resetResult() {
		Result = false;
		resetHash();
	}
	
	/**
	 * Set the result and calculates the hash value of an test case.
	 * Result is set to <code>AResult</code> and the abstract method
	 * <code>calcHash(ATestCase)</code> is called.
	 * 
	 * This function can be called several times to
	 * include more states of an object
	 * or different objects in the same hash value.
	 * 
	 * The method <code>calchash(ATestCase)</code> is called even if 
	 * <code>AResult == false</code>.
	 * 
	 * @param AResult new result value for this test case
	 * @param ATestCase test case for which the hash value is calculated
	 */
	public void calcResult(boolean AResult, TestCase ATestCase) {
		if (ATestCase != null) {
			calcHash(ATestCase);
			Result = AResult;
		} else {
			resetResult();
		}
	}

}