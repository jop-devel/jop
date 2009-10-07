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

public class TcSwitchTest extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcSwitchTest";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	boolean checkTS(int var) {
		boolean res;
		
		switch(var) {
		case 0:
			res = var == 0;
			break;
		case 1:
			res = var == 1;
			break;
		case 2:
			res = var == 2;
			break;
		case 3:
			res = var == 3;
			break;
		case 4:
			res = var == 4;
			break;
		case 5:
			res = var == 5;
			break;
		case 6:
			res = var == 6;
			break;
		default:
			res = false;
		    break;
		}
		
		return res;	
	}
	
	boolean checkTSDefault(int var) {
		boolean res;
		
		switch(var) {
		case 0:
			res = false;
			break;
		case 1:
			res = false;
			break;
		case 2:
			res = false;
			break;
		case 3:
			res = false;
			break;
		case 4:
			res = false;
			break;
		case 5:
			res = false;
			break;
		case 6:
			res = false;
			break;
		default:
			res = true;
		    break;
		}
		
		return res;			
	}
	
	boolean checkLS(int var) {
		boolean res=false;
		
		switch(var) {
		case 1:
			res = var==1;
			break;
		case 100:
			res = var==100;
			break;
		case 1000:
			res = var==1000;
			break;
		case 10000:
			res = var==10000;
			break;
		case Integer.MAX_VALUE:
			res = var==Integer.MAX_VALUE;
			break;
		default:
			res = false;				
		}		
		
		return res;
	}
	
	boolean checkUnordered(int var) {
		boolean res=false;
		
		switch(var) {
		case -100: 
			res = var==-100;
			break;
		case -200:
			res = var==-200;
			break;
		case 100:
			res = var==100;
			break;
		case 200:
			res = var==200;
			break;
		case -300:
			res = var==-300;
			break;
		default:
			res = false;
		}
		
		return res;		
	}
	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result = true;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		Result = 
			checkTS(0) &&
			checkTS(1) &&
			checkTS(2) &&
			checkTS(3) &&
			checkTS(5) &&
			checkTS(6) &&
			!checkTS(7);
		
		Result =
			Result &&
			!checkTSDefault(0) &&
			!checkTSDefault(1) &&
			!checkTSDefault(5) &&
			!checkTSDefault(6) &&
			checkTSDefault(7) &&
			checkTSDefault(Integer.MAX_VALUE);
		
		Result = 
			Result &&
			!checkLS(0) &&
			checkLS(1) &&
			!checkLS(2) &&
			!checkLS(3) &&
			!checkLS(5) &&
			!checkLS(6) &&
			!checkLS(7) &&
			checkLS(100) &&
			checkLS(1000) &&
			checkLS(10000) &&
			checkLS(Integer.MAX_VALUE);
		
		Result = 
			Result &&
			checkUnordered(100) &&
			checkUnordered(200) &&
			checkUnordered(-300) &&
			checkUnordered(-200) &&
			checkUnordered(-100) &&
			!checkUnordered(0);
			
		FResult.calcResult(Result, this);
		
		return FResult;
	}
}