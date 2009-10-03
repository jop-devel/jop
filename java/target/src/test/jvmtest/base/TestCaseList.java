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
 * Base class to store a collection of TestCase
 * objects. 
 * @author Günther Wimpassinger
 *
 */
public abstract class TestCaseList {
	protected MyArrayList<TestCase> tcList;
	protected MyArrayList<TestCaseResult> tcrList;
	
	protected int tcPassed;
	protected int tcFailed;
	protected int tcException;
	
	/**
	 * Default constructor for <code>TestCaseList</code>
	 */
	public TestCaseList() {
		super();
		
		tcList = new MyArrayList<TestCase>();
		tcrList = new MyArrayList<TestCaseResult>();
		tcPassed = 0;
		tcFailed = 0;
		tcException = 0;
	}

	/**
	 * Constructor for <code>TestCaseList</code>
	 */
	public TestCaseList(TestCase... tcNewList) {
		this();
		
		for (TestCase tc : tcNewList) {
			tcList.add(tc);
		}
	}
	
	/**
	 * Get the number of the failed test cases.
	 * Include the test cases which have failed because
	 * of an not handled exception.
	 * @return The number of failed test cases (incl. Exceptions)
	 */
	public int getFailed() {
		return tcFailed;
	}

	/**
	 * Get the number of exception raised by test cases
	 * @return The number of exceptions
	 */
	public int getException() {
		return tcException;
	}

	/**
	 * Get the number of the passed test cases
	 * @return The number of passed test cases
	 */
	public int getPassed() {
		return tcPassed;
	}
	
	/**
	 * Get the number of executable test cases
	 * @return The number of executable test cases
	 */
	public int getCount() {
		return tcList.size();
	}

	/**
	 * Return the <cod>TestCase</code> at index <code>Index</index>
	 * from the list
	 * @param Index Position in the list
	 * @return the <code>TestCase</code) object
	 */
	public TestCase getTestCase(int Index) {
		return tcList.get(Index);
	}

	/**
	 * Return the <cod>TestCase</code> correspond to the
	 * <code>TestCaseResult</code> provided at the parameter
	 * <code>tcr</code>
	 * @param tcr Result of the test case. 
	 * @return the <code>TestCase</code) object which correspond
	 * to <code>tcr</code> or <code>null</code> if not found
	 */
	public TestCase getTestCase(TestCaseResult tcr) {
		for (int i=0; i<tcrList.size(); i++) {
			if (getTestCaseResult(i) == tcr) {
				return getTestCase(i);
			}
		}
		return null;
	}
	
	/**
	 * Return the <cod>TestCaseResult</code> at index <code>Index</index>
	 * from the list
	 * @param Index Position in the list
	 * @return the <code>TestCaseResult</code) object
	 */
	public TestCaseResult getTestCaseResult(int Index) {
		return tcrList.get(Index);
	}

	/**
	 * Return the <cod>TestCaseResult</code> belonging to the
	 * <code>TestCase</index> object
	 * @param <code>TestCase</code> object for which the result
	 * have to be searched
	 * @return the <code>TestCaseResult</code) object which
	 * correspond to the <code>TestCase</code> object provided
	 * in the parameter <code>tc</code>.
	 */
	public TestCaseResult getTestCaseResult(TestCase tc) {
		for (int i=0; i<tcList.size(); i++) {
			if (getTestCase(i) == tc) {
				return getTestCaseResult(i);
			}
		}
		return null;
	}
	
	/**
	 * Run the all the test cases in the list, may be more times
	 * in multiple threads
	 * @return An array of test case results for each test case
	 */
	public abstract void run();

}
