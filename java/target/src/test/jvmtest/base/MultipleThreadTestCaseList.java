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
 * exactly one time in each thread. 
 * @author Günther Wimpassinger
 *
 */
public class MultipleThreadTestCaseList extends TestCaseList {
	
	private class TestThread extends Thread {
		TestCase  tc;
		TestCaseResult tcr;
		
		void setTestCase(TestCase aTestCase) {
			tc = aTestCase;
		}
		
		TestCaseResult getTestCaseResult() {
			return tcr;
		}
		
		public void run() {
			if (tc!=null) {
				tcr=tc.run();
			}
		}
		
	}
	
	private int threadCount;
	
	/**
	 * Constructor for <code>MultipleThreadTestCaseList</code>
	 */
	public MultipleThreadTestCaseList(
			int threadCount,
			TestCase... tcNewList) {
		super(tcNewList);
		this.threadCount = threadCount;
	}
	

	/* (non-Javadoc)
	 * @see jvmtest.base.TestCaseList#run()
	 */
	/* @Override */
	public void run() {
		tcrList.clear();
		TestThread[] tct = new TestThread[threadCount];
		int maxThreadCount;
		
		for (int i=0; i<tcList.size(); i++) {
			
			TestCase tc = tcList.get(i);
			TestCaseResult tcr = null;
			TestCaseResult tcrHelp = null;
			
			try {
				
				if (tc.allowMultipleInstances()) {
					maxThreadCount = threadCount;
				} else {
					maxThreadCount = 1;
				}
				
				for (int k=0;k<maxThreadCount;k++) {
					/* TODO: use thread pools instead of creating
					 * new threads each time
					 */
					tct[k]=new TestThread();
					if (k==0) {
						tct[k].setTestCase(tc);
					} else {
						tct[k].setTestCase(tc.getClass().newInstance());
					}
					tct[k].start();
				}
				for (int k=0;k<maxThreadCount;k++) {
					tct[k].join();
					tcrHelp = tct[k].getTestCaseResult();
					if (tcr == null) {
						tcr = tcrHelp;						
					} else {
						if (!tcrHelp.getResult()) {
							tcr = tcrHelp;
						}
					}
				}

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
