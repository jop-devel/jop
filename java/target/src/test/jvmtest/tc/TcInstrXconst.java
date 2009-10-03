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

public class TcInstrXconst extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcInstrXconst";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	private boolean runReference(TestCaseResult tcr) {
		boolean Result = true;
		Object ref;
		
		ref = null;
		Result = Result && ref == null;
		tcr.calcHashString("null");
		
		return Result;
	}
	
	private boolean runOrdinals(TestCaseResult tcr) {
		boolean Result = true;
		byte bVar;
		short sVar;
		char cVar;
		int iVar;
		long lVar;

		/* *** Byte */
		bVar = -1;
		Result = Result && bVar == -1;
		tcr.calcHashInt(bVar);

		bVar = 0;
		Result = Result && bVar == 0;
		tcr.calcHashInt(bVar);

		bVar = 1;
		Result = Result && bVar == 1;
		tcr.calcHashInt(bVar);

		bVar = 2;
		Result = Result && bVar == 2;
		tcr.calcHashInt(bVar);
		
		bVar = 3;
		Result = Result && bVar == 3;
		tcr.calcHashInt(bVar);
		
		bVar = 4;
		Result = Result && bVar == 4;
		tcr.calcHashInt(bVar);
		
		bVar = 5;
		Result = Result && bVar == 5;
		tcr.calcHashInt(bVar);
		
		/* *** Short */
		sVar = -1;
		Result = Result && sVar == -1;
		tcr.calcHashInt(sVar);

		sVar = 0;
		Result = Result && sVar == 0;
		tcr.calcHashInt(sVar);

		sVar = 1;
		Result = Result && sVar == 1;
		tcr.calcHashInt(sVar);

		sVar = 2;
		Result = Result && sVar == 2;
		tcr.calcHashInt(sVar);
		
		sVar = 3;
		Result = Result && sVar == 3;
		tcr.calcHashInt(sVar);
		
		sVar = 4;
		Result = Result && sVar == 4;
		tcr.calcHashInt(sVar);
		
		sVar = 5;
		Result = Result && sVar == 5;
		tcr.calcHashInt(sVar);
		
		/* *** Char */
		sVar = -1;
		cVar = (char)sVar;
		Result = Result && cVar == 65535;
		tcr.calcHashInt(cVar);
		Result = Result && cVar != -1;		// as int operation 65535 is not -1
		tcr.calcHashInt(cVar);
		
		cVar = 0;
		Result = Result && cVar == 0;
		tcr.calcHashInt(cVar);

		cVar = 1;
		Result = Result && cVar == 1;
		tcr.calcHashInt(cVar);

		cVar = 2;
		Result = Result && cVar == 2;
		tcr.calcHashInt(cVar);
		
		cVar = 3;
		Result = Result && cVar == 3;
		tcr.calcHashInt(cVar);
		
		cVar = 4;
		Result = Result && cVar == 4;
		tcr.calcHashInt(cVar);
		
		cVar = 5;
		Result = Result && cVar == 5;
		tcr.calcHashInt(cVar);

		/* *** Integer */
		iVar = -1;
		Result = Result && iVar == -1;
		tcr.calcHashInt(iVar);

		iVar = 0;
		Result = Result && iVar == 0;
		tcr.calcHashInt(iVar);

		iVar = 1;
		Result = Result && iVar == 1;
		tcr.calcHashInt(iVar);

		iVar = 2;
		Result = Result && iVar == 2;
		tcr.calcHashInt(iVar);
		
		iVar = 3;
		Result = Result && iVar == 3;
		tcr.calcHashInt(iVar);
		
		iVar = 4;
		Result = Result && iVar == 4;
		tcr.calcHashInt(iVar);
		
		iVar = 5;
		Result = Result && iVar == 5;
		tcr.calcHashInt(iVar);
		
		/* *** Long */
		lVar = -1;
		Result = Result && lVar == -1;
		tcr.calcHashLong(lVar);

		lVar = 0;
		Result = Result && lVar == 0;
		tcr.calcHashLong(lVar);

		lVar = 1;
		Result = Result && lVar == 1;
		tcr.calcHashLong(lVar);

		lVar = 2;
		Result = Result && lVar == 2;
		tcr.calcHashLong(lVar);
		
		lVar = 3;
		Result = Result && lVar == 3;
		tcr.calcHashLong(lVar);
		
		lVar = 4;
		Result = Result && lVar == 4;
		tcr.calcHashLong(lVar);
		
		lVar = 5;
		Result = Result && lVar == 5;
		tcr.calcHashLong(lVar);
		
		return Result;
	}
	
	private boolean runFloats(TestCaseResult tcr) {
		boolean Result = true;
		float fVar;
		double dVar;
		
		/* *** Float */ 

		fVar = -1;
		Result = Result && fVar == -1;
		tcr.calcHashFloat(fVar);

		fVar = 0.0f;
		Result = Result && fVar == 0.0f;
		tcr.calcHashFloat(fVar);

		fVar = 1.0f;
		Result = Result && fVar == 1.0f;
		tcr.calcHashFloat(fVar);

		/* *** Double */ 
		dVar = -1;
		Result = Result && dVar == -1;
		tcr.calcHashDouble(dVar);

		dVar = 0.0;
		Result = Result && dVar == 0.0;
		tcr.calcHashDouble(dVar);

		dVar = 1.0;
		Result = Result && dVar == 1.0;
		tcr.calcHashDouble(dVar);		
		
		return Result;
	}
		
	
	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		Result =
			runReference(FResult) &&
			runOrdinals(FResult) &&
			runFloats(FResult);
		
		FResult.calcResult(Result, this);

		return FResult;
	}

}