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
 * Factory to create new instances of one subclass of. 
 * It is only necessary to change this function to generate
 * a different/more complex result object for test cases.
 * <code>TestCaseResult</code>.
 * 
 * @author Günther Wimpassinger
 *
 */
public class TestCaseResultFactory {
	
	/**
	 * Create a new <code>TestCaseResult</code> object with no
	 * reference to an <code>TestCase</code> object 
	 * @return a new <code>TestCaseResult</code> object 
	 */
	public static TestCaseResult createResult() {
		return new SimpleTestCaseResult(false, null);
	}
	
	/**
	 * Create a new <code>TestCaseResult</code> object with no
	 * reference to an <code>TestCase</code> object
	 * @param AResult result value for this test case
	 * @param ATestCAse <code>TestCase</code> object for this test case 
	 * @return a new <code>TestCaseResult</code> object 
	 */
	public static TestCaseResult createResult(boolean AResult, TestCase ATestCase) {
		return new SimpleTestCaseResult(AResult, ATestCase);
	}

}
