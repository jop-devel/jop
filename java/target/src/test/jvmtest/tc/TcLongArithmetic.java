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

public class TcLongArithmetic extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcLongArithmetic";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	private boolean addCheck(TestCaseResult tcr, long a, long b, long r) {
		long help = a+b;
		tcr.calcHashLong(a);
		tcr.calcHashLong(b);
		tcr.calcHashLong(r);
		tcr.calcHashLong(help);
		return (help==r) && (a+b==r);
	}
	
	private boolean addTest(TestCaseResult tcr) {
		boolean Result;
		
		Result = addCheck(tcr,0,0,0);
		Result = Result && addCheck(tcr,0,-1,-1);
		Result = Result && addCheck(tcr,-1,0,-1);
		Result = Result && addCheck(tcr,-1,-1,-2);
		Result = Result && addCheck(tcr,0,1,1);
		Result = Result && addCheck(tcr,1,0,1);
		Result = Result && addCheck(tcr,1,1,2);
		Result = Result && addCheck(tcr,736,2784,3520);
		Result = Result && addCheck(tcr,-255,+254,-1);
		Result = Result && addCheck(tcr,0x7fffFFFFL,-1,0x7fffFFFEL);
		Result = Result && addCheck(tcr,0x7fffFFFFL, 1,0x80000000L);
		Result = Result && addCheck(tcr,-2147483648,-1,-2147483649L);
		Result = Result && addCheck(tcr,Long.MAX_VALUE,1,Long.MIN_VALUE);
		Result = Result && addCheck(tcr,Long.MIN_VALUE,-1,Long.MAX_VALUE);
		Result = Result && addCheck(tcr,0,Long.MAX_VALUE,Long.MAX_VALUE);
		Result = Result && addCheck(tcr,0,Long.MIN_VALUE,Long.MIN_VALUE);
		
		
		return Result;				
	}
	
	private boolean subCheck(TestCaseResult tcr, long a, long b, long r) {
		long help = a-b;
		
		tcr.calcHashLong(a);
		tcr.calcHashLong(b);
		tcr.calcHashLong(r);
		tcr.calcHashLong(help);
		
		return (help==r) && (a-b==r);		
	}
	
	private boolean subTest(TestCaseResult tcr) {
		boolean Result;
		
		Result = subCheck(tcr,0,0,0);
		Result = Result && subCheck(tcr,0,-1,1);
		Result = Result && subCheck(tcr,-1,0,-1);
		Result = Result && subCheck(tcr,-1,-1,0);
		Result = Result && subCheck(tcr,0,1,-1);
		Result = Result && subCheck(tcr,1,0,1);
		Result = Result && subCheck(tcr,1,1,0);
		Result = Result && subCheck(tcr,3520,2784,736);
		Result = Result && subCheck(tcr,-255,-254,-1);
		Result = Result && subCheck(tcr,0x7fffFFFF,-1,2147483648L);
		Result = Result && subCheck(tcr,0x7fffFFFF,1,2147483646);
		Result = Result && subCheck(tcr,0x80000000L,-1,0x80000001L);
		Result = Result && subCheck(tcr,Long.MAX_VALUE,-1,Long.MIN_VALUE);
		Result = Result && subCheck(tcr,Long.MIN_VALUE,1,Long.MAX_VALUE);
		Result = Result && subCheck(tcr,0,Long.MAX_VALUE,-Long.MAX_VALUE);
		Result = Result && subCheck(tcr,0,Long.MIN_VALUE,Long.MIN_VALUE);
		
		return Result;		
	}
	
	private boolean mulCheck(TestCaseResult tcr, long a, long b, long r) {
		long help = a*b;
		
		tcr.calcHashLong(a);
		tcr.calcHashLong(b);
		tcr.calcHashLong(r);
		tcr.calcHashLong(help);
		
		return (help==r) && (a*b==r);		
	}
	
	private boolean mulTest(TestCaseResult tcr) {
		boolean Result;
		
		Result = mulCheck(tcr,0,0,0);
		Result = Result && mulCheck(tcr,0,-1,0);
		Result = Result && mulCheck(tcr,-1,0,0);
		Result = Result && mulCheck(tcr,-1,-1,1);
		Result = Result && mulCheck(tcr,0,1,0);
		Result = Result && mulCheck(tcr,1,0,0);
		Result = Result && mulCheck(tcr,1,1,1);
		Result = Result && mulCheck(tcr,-1,1,-1);
		Result = Result && mulCheck(tcr,1,-1,-1);
		Result = Result && mulCheck(tcr,736,2784,2049024);
		Result = Result && mulCheck(tcr,-255,254,-64770);
		Result = Result && mulCheck(tcr,255,-254,-64770);
		Result = Result && mulCheck(tcr,255,254,64770);
		Result = Result && mulCheck(tcr,0x40000000,2,0x80000000L);
		Result = Result && mulCheck(tcr,0x80000001L,2,0x100000002L);
		
		return Result;				
		
	}

	private boolean divCheck(TestCaseResult tcr, long a, long b, long r) {
		long help = a/b;
		
		tcr.calcHashLong(a);
		tcr.calcHashLong(b);
		tcr.calcHashLong(r);
		tcr.calcHashLong(help);
		
		return (help==r) && (a/b==r);		
	}
	
	private boolean divTest(TestCaseResult tcr) {
		boolean Result;
		boolean exp;
		
		exp = false;
		try {
			divCheck(tcr,0,0,0);
		} catch (ArithmeticException ae) {
			exp = true;
		}
		Result = exp;
		exp = false;
		try {
			divCheck(tcr,1,0,0);
		} catch (ArithmeticException ae) {
			exp = true;
		}
		Result = Result && exp;
		exp = false;
		try {
			divCheck(tcr,-1,0,0);
		} catch (ArithmeticException ae) {
			exp = true;
		}
		Result = Result && exp;
		
		Result = Result && divCheck(tcr,0,1,0);
		Result = Result && divCheck(tcr,0,2,0);
		Result = Result && divCheck(tcr,0,100,0);
		
		Result = Result && divCheck(tcr,5,2,2);
		Result = Result && divCheck(tcr,-5,2,-2);
		Result = Result && divCheck(tcr,5,-2,-2);
		Result = Result && divCheck(tcr,-5,-2,2);
		Result = Result && divCheck(tcr,6,2,3);
		Result = Result && divCheck(tcr,6,-2,-3);
		Result = Result && divCheck(tcr,-6,2,-3);
		Result = Result && divCheck(tcr,-6,-2,3);
		Result = Result && divCheck(tcr,2,6,0);
		Result = Result && divCheck(tcr,-2,6,0);
		Result = Result && divCheck(tcr,2,-6,0);
		Result = Result && divCheck(tcr,-2,-6,0);
		Result = Result && divCheck(tcr,2049024,736,2784);
		Result = Result && divCheck(tcr,0x80000000L,2,0x40000000);
		Result = Result && divCheck(tcr,0x80000002L,2,0x40000001);
		Result = Result && divCheck(tcr,0x80000001L,2,0x40000000);		
		Result = Result && divCheck(tcr,Long.MIN_VALUE+2,2,0xC000000000000001L);
		Result = Result && divCheck(tcr,Long.MIN_VALUE+1,2,0xC000000000000001L);
		Result = Result && !divCheck(tcr,Long.MIN_VALUE+1,2,0xC000000000000000L);
		
		return Result;				
		
	}
	
	
	private boolean andCheck(TestCaseResult tcr, long a, long b, long r) {
		long help = a & b;
		
		tcr.calcHashLong(a);
		tcr.calcHashLong(b);
		tcr.calcHashLong(r);
		tcr.calcHashLong(help);
		
		return (help==r) && ((a & b)==r);		
	}
	
	private boolean andTest(TestCaseResult tcr) {
		boolean Result;
		
		Result = andCheck(tcr,0,1,0);
		Result = Result && andCheck(tcr,0,2,0);
		Result = Result && andCheck(tcr,0,100,0);
		Result = Result && andCheck(tcr,1,1,1);
		Result = Result && andCheck(tcr,2,1,0);
		Result = Result && andCheck(tcr,1,2,0);
		
		Result = Result && andCheck(tcr,0xAAAAaaaa,0x55555555,0);
		Result = Result && andCheck(tcr,0x55555555,0xAAAAaaaa,0);
		Result = Result && andCheck(tcr,0xAAAAaaaa,0x5555aaaa,0x0000aaaa);
		Result = Result && andCheck(tcr,0xAAAAaaaa,0x5555ffff,0x0000aaaa);
		Result = Result && andCheck(tcr,0x5555ffff,0xAAAAaaaa,0x0000aaaa);
		Result = Result && andCheck(tcr,0xAAAAaaaa,0xFFFFffff,0xAAAAaaaa);
		Result = Result && andCheck(tcr,0xFFFFffff,0xAAAAaaaa,0xAAAAaaaa);
		Result = Result && andCheck(tcr,0x55555555AAAAaaaaL,0xAAAAaaaa55555555L,0);
		Result = Result && andCheck(tcr,0xAAAAaaaa55555555L,0x55555555AAAAaaaaL,0);
		Result = Result && andCheck(tcr,0x55555555FFFFffffL,0xAAAAaaaa55555555L,0x55555555L);
		Result = Result && andCheck(tcr,0xAAAAaaaaFFFFffffL,0x55555555AAAAaaaaL,0xAAAAaaaaL);
		Result = Result && andCheck(tcr,0xFFFFffff55555555L,0xAAAAaaaaAAAAaaaaL,0xAAAAaaaa00000000L);
		Result = Result && andCheck(tcr,0xFFFFffffAAAAaaaaL,0x55555555AAAAaaaaL,0x55555555AAAAaaaaL);
		
		return Result;
	}
	
	private boolean orCheck(TestCaseResult tcr, long a, long b, long r) {
		long help = a | b;
		
		tcr.calcHashLong(a);
		tcr.calcHashLong(b);
		tcr.calcHashLong(r);
		tcr.calcHashLong(help);
		
		return (help==r) && ((a | b)==r);		
	}
	
	private boolean orTest(TestCaseResult tcr) {
		boolean Result;
		
		Result = orCheck(tcr,0,1,1);
		Result = orCheck(tcr,1,0,1);
		Result = orCheck(tcr,-1,0,-1);
		Result = Result && orCheck(tcr,0,2,2);
		Result = Result && orCheck(tcr,0,100,100);
		Result = Result && orCheck(tcr,1,1,1);
		Result = Result && orCheck(tcr,2,1,3);
		Result = Result && orCheck(tcr,1,2,3);
		
		Result = Result && orCheck(tcr,0xAAAAaaaa,0x55555555,0xFFFFffff);
		Result = Result && orCheck(tcr,0x55555555,0xAAAAaaaa,0xFFFFffff);
		Result = Result && orCheck(tcr,0xAAAAaaaa,0x5555aaaa,0xFFFFaaaa);
		Result = Result && orCheck(tcr,0xAAAAaaaa,0x5555ffff,0xFFFFffff);
		Result = Result && orCheck(tcr,0x5555ffff,0xAAAAaaaa,0xFFFFffff);
		Result = Result && orCheck(tcr,0xAAAAaaaa,0xFFFFffff,0xFFFFffff);
		Result = Result && orCheck(tcr,0xFFFFffff,0xAAAAaaaa,0xFFFFffff);		
		Result = Result && orCheck(tcr,0xAAAAaaaa,0xAAAA0000,0xAAAAaaaa);		
		Result = Result && orCheck(tcr,0x5555aaaa,0xAAAA0000,0xFFFFaaaa);		
		Result = Result && orCheck(tcr,0x55555555AAAAaaaaL,0xAAAAaaaa55555555L,0xFFFFffffFFFFffffL);
		Result = Result && orCheck(tcr,0xAAAAaaaa55555555L,0x55555555AAAAaaaaL,0xFFFFffffFFFFffffL);
		Result = Result && orCheck(tcr,0x55555555FFFFffffL,0xAAAAaaaa55555555L,0xFFFFffffFFFFffffL);
		Result = Result && orCheck(tcr,0xAAAAaaaaFFFFffffL,0x55555555AAAAaaaaL,0xFFFFffffFFFFffffL);
		Result = Result && orCheck(tcr,0xFFFFffff55555555L,0xAAAAaaaaAAAAaaaaL,0xFFFFffffFFFFffffL);
		Result = Result && orCheck(tcr,0xFFFFffffAAAAaaaaL,0x55555555AAAAaaaaL,0xFFFFffffAAAAaaaaL);
		
		return Result;
	}
	
	
	private boolean xorCheck(TestCaseResult tcr, long a, long b, long r) {
		long help = a ^ b;
		
		tcr.calcHashLong(a);
		tcr.calcHashLong(b);
		tcr.calcHashLong(r);
		tcr.calcHashLong(help);
		
		return (help==r) && ((a ^ b)==r);		
	}
	
	private boolean xorTest(TestCaseResult tcr) {
		boolean Result;
		
		Result = xorCheck(tcr,0,1,1);
		Result = xorCheck(tcr,1,0,1);
		Result = xorCheck(tcr,-1,0,-1);
		Result = Result && xorCheck(tcr,0,2,2);
		Result = Result && xorCheck(tcr,0,100,100);
		Result = Result && xorCheck(tcr,1,1,0);
		Result = Result && xorCheck(tcr,2,1,3);
		Result = Result && xorCheck(tcr,1,2,3);
		
		Result = Result && xorCheck(tcr,0xAAAAaaaa,0x55555555,0xFFFFffff);
		Result = Result && xorCheck(tcr,0x55555555,0xAAAAaaaa,0xFFFFffff);
		Result = Result && xorCheck(tcr,0xAAAAaaaa,0x5555aaaa,0xFFFF0000);
		Result = Result && xorCheck(tcr,0xAAAAaaaa,0x5555ffff,0xFFFF5555);
		Result = Result && xorCheck(tcr,0x5555ffff,0xAAAAaaaa,0xFFFF5555);
		Result = Result && xorCheck(tcr,0xAAAAaaaa,0xFFFFffff,0x55555555);
		Result = Result && xorCheck(tcr,0xFFFFffff,0xAAAAaaaa,0x55555555);		
		Result = Result && xorCheck(tcr,0xAAAAaaaa,0xAAAA0000,0x0000aaaa);		
		Result = Result && xorCheck(tcr,0x5555aaaa,0xAAAA0000,0xFFFFaaaa);		
		Result = Result && xorCheck(tcr,0x55555555AAAAaaaaL,0xAAAAaaaa55555555L,0xFFFFffffFFFFffffL);
		Result = Result && xorCheck(tcr,0xAAAAaaaa55555555L,0x55555555AAAAaaaaL,0xFFFFffffFFFFffffL);
		Result = Result && xorCheck(tcr,0x55555555FFFFffffL,0xAAAAaaaa55555555L,0xFFFFffffAAAAaaaaL);
		Result = Result && xorCheck(tcr,0xAAAAaaaaFFFFffffL,0x55555555AAAAaaaaL,0xFFFFffff55555555L);
		Result = Result && xorCheck(tcr,0xFFFFffff55555555L,0xAAAAaaaaAAAAaaaaL,0x55555555FFFFffffL);
		Result = Result && xorCheck(tcr,0xFFFFffffAAAAaaaaL,0x55555555AAAAaaaaL,0xAAAAaaaa00000000L);
		
		return Result;
	}
	
	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result = true;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
        Result = 
        	addTest(FResult) &&
        	subTest(FResult) &&
        	mulTest(FResult) &&
        	divTest(FResult) &&
        	andTest(FResult) &&
        	orTest(FResult)  &&
        	xorTest(FResult);
		
		FResult.calcResult(Result,this);

		return FResult;
	}

}