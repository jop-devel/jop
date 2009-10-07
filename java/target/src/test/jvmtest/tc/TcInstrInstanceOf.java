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

public class TcInstrInstanceOf extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcInstrInstanceOf";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	/* some classes */
	class A {
		int exec(TestCaseResult tcr) {
			tcr.calcHashInt(2);
			return 2;
		}
	}


	class B extends A {
		int exec(TestCaseResult tcr) {
			tcr.calcHashInt(3);
			return super.exec(tcr)*3;
		}
	}


	class C extends B {
		int exec(TestCaseResult tcr) {
			tcr.calcHashInt(5);
			return super.exec(tcr)*5;
		}
	}
	
	class D extends A {
		int exec(TestCaseResult tcr) {
			tcr.calcHashInt(7);
			return super.exec(tcr)*7;
		}
	}
	
	class E {
		int noexec(TestCaseResult tcr) {
			tcr.calcHashInt(13);
			return 13;
		}
	}

	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result = true;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		A a = new A();
		B b = new B();
		C c = new C();
		D d = new D();
		E e = new E();
		
		A Da = new A();
		A Db = new B();
		A Dc = new C();
		A Dd = new D();
		E De = new E();
		
		Result = a.exec(FResult) == 2;
		Result = Result && b.exec(FResult) == 2*3;
		Result = Result && c.exec(FResult) == 2*3*5;
		Result = Result && d.exec(FResult) == 2*7;
		Result = Result && e.noexec(FResult) == 13;
		
		Result = Result && a instanceof A;
		Result = Result && b instanceof A;
		Result = Result && c instanceof A;
		Result = Result && b instanceof B;
		Result = Result && c instanceof B;
		Result = Result && c instanceof C;
		Result = Result && d instanceof A;
		Result = Result && e instanceof E;
		
		Result = Result && !(a instanceof B);
		Result = Result && !(b instanceof C);
		
		Result = Result && Da.exec(FResult) == 2;
		Result = Result && Db.exec(FResult) == 2*3;
		Result = Result && Dc.exec(FResult) == 2*3*5;
		Result = Result && Dd.exec(FResult) == 2*7;		
		Result = Result && De.noexec(FResult) == 13;	
		
		Result = Result && Da instanceof A;
		Result = Result && Db instanceof A;
		Result = Result && Dc instanceof A;
		Result = Result && Db instanceof B;
		Result = Result && Dc instanceof B;
		Result = Result && Dc instanceof C;
		Result = Result && Dd instanceof A;
		Result = Result && De instanceof E;
		Result = Result && !(Da instanceof B);
		Result = Result && !(Db instanceof C);
		Result = Result && !(Dc instanceof D);
		Result = Result && !(Dd instanceof B);
		
		FResult.calcResult(Result, this);
		
		return FResult;
	}
}