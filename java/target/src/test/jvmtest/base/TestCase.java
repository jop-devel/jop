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

import java.io.Serializable;

/**
 * Base class for test cases.
 * 
 * @author Günther Wimpassinger
 *
 */
public abstract class TestCase implements Serializable {
	
	/**
	 * Serialization Version UID
	 */	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TestCase";
	}
	
	/**
	 * This function does nothing useful. It is provided
	 * to have some operations between some test steps
	 */
	public static void someDummy() {
		int a = 0x2345;
		int b = 0x3425;
		int c = a ^ b ^ 0x5234;
		
		b = c / (a >> 2);
	}
	
	/**
	 * If a test case operate on static fields/methods they
	 * must not be called from different threads "at the same
	 * time", as long as they are not protected.
	 * A multi-threaded test suite can check if a
	 * test case is allowed to be instantiated in more threads   
	 * @return If a test case class is allowed to be instantiated in
	 * multiple threads
	 */
	public boolean allowMultipleInstances() {
		return true;
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public abstract void writeToStream(ByteArrayOutputStreamEx os);
	
	/**
	 * Run the test case
	 * @return The result of the test case
	 */
	public abstract TestCaseResult run();
	
}
