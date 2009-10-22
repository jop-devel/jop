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
 * TestCaseList implementation which executes each <code>TestCase</code>
 * exactly one at a time. The test cases are executed in the thread
 * which called the <code>run</code> method.
 * @author Günther Wimpassinger
 *
 */
public class SingleThreadTestCaseList extends TestCaseList {
	
	/**
	 * Constructor for <code>SingleThreadTestCaseList</code>
	 */
	public SingleThreadTestCaseList(TestCase... tcNewList) {
		super(tcNewList);
	}
	

	/* (non-Javadoc)
	 * @see jvmtest.base.TestCaseList#run()
	 */
	/* @Override */
	public void run() {
		tcrList.clear();
		
		for (int i=0; i<tcList.size(); i++) {
			
			TestCase tc = tcList.get(i);
			TestCaseResult tcr = null;
			
			try {
				tcr = tc.run();
				if (tcr.Result) {
					tcPassed++;
				} else {
					tcFailed++;
				}
			} catch(Exception e) {
				tcFailed++;
				tcException++;
				// if the run method throws an exception, we do not get
				// a TestCaseResult object from it
				if (tcr==null) {
					tcr = TestCaseResultFactory.createResult();
				}
				tcr.setRunMessage(e.toString());
			}
			tcrList.add(i, tcr);
		}
		
	}

}
