/* jvmtest - Testing your VM 
  Copyright (C) 20009, Guenther Wimpassinger
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
package jvmtest;

import jvmtest.base.*;
import jvmtest.tc.*;


/**
 * Test suite for a Java VM.
 * @author Günther Wimpassinger
 *
 */
public class TestSuite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//TestCaseList tcList = new MultipleThreadTestCaseList( 4,
		TestCaseList tcList = new SingleThreadTestCaseList(
				new ExampleTestCase(),
				new TcIntField(),
				new TcLongField(),
				new TcShortField(),
				new TcCharField(),
				new TcByteField(),
				new TcBooleanField(),
				new TcObjectField(),
				new TcFloatField(),
				new TcDoubleField(),
				new TcAutoBoxObjectField(),
				
				new TcStaticIntField(),
				new TcStaticLongField(),
				new TcStaticByteField(),
				new TcStaticShortField(),
				new TcStaticCharField(),
				new TcStaticBooleanField(),
				new TcStaticFloatField(),
				new TcStaticDoubleField(),
				new TcStaticObjectField(),
				
				new TcInstrXconst(),
				new TcInstrXipush(),
				new TcInstrLdc(),
				new TcInstrXloadXstore(),
				
				new TcIntArithmetic(),
				new TcLongArithmetic(),
				new TcInstrX2Y(),
				
				new TcSwitchTest(),
				
				new TcClassSuper(),
				new TcInstrAthrow(),
				new TcInstrInstanceOf()
		);
		
		System.out.println("Start TestSuite");	
		tcList.run();		
	
		for (int i=0;i<tcList.getCount();i++) {
			TestCase tc = tcList.getTestCase(i);
			TestCaseResult tcResult = tcList.getTestCaseResult(i);
			
			System.out.print("tcr ");
			System.out.print(StringUtils.LeftAdjust(tc.getTestCaseName(),25,' '));
			System.out.print(' ');
			System.out.print(BooleanUtils.toAdjustString(tcResult.getResult()));
			System.out.print(' ');
			System.out.print(tcResult.getHash());
			if (!tcResult.getResult()) {
				System.out.print(' ');
				System.out.print(tcResult.getRunMessage());
			}
			System.out.println();
		}
		
		System.out.print("Test cases executed ");
		System.out.println(Integer.toString(tcList.getCount()));
		System.out.print("  passed     ");
		System.out.println(Integer.toString(tcList.getPassed()));
		System.out.print("  failed     ");
		System.out.println(Integer.toString(tcList.getFailed()));
		System.out.print("  exception  ");
		System.out.println(Integer.toString(tcList.getException()));
		
	}

}
