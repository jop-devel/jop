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

public class TcInstrX2Y extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcInstrX2Y";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	private boolean i2x(TestCaseResult tcr) {
		boolean res;
		
		int a;
		byte b;
		long l;
		short s;
		char c;
		float f;
		double d;
		
		a=0;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		l=a;
		f=a;
		d=a;
		tcr.calcHashInt(a);
		
		res= a==0 &&
		     b==0 &&
		     s==0 &&
		     c==0 &&
		     l==0L &&
		     f==0.0f &&
		     d==0.0;
		
		a=1;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		l=a;
		f=a;
		d=a;
		tcr.calcHashInt(a);
		
		res=res &&
		     a==1 &&
		     b==1 &&
		     s==1 &&
		     c==1 &&
		     l==1L &&
		     f==1.0f &&
		     d==1.0;
		     
		a=2;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		l=a;
		f=a;
		d=a;
		tcr.calcHashInt(a);
		
		res=res &&
		     a==2 &&
		     b==2 &&
		     s==2 &&
		     c==2 &&
		     l==2L &&
		     f==2.0f &&
		     d==2.0;
		     
		a=-1;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		l=a;
		f=a;
		d=a;
		tcr.calcHashInt(a);
		
		res=res &&
			 a==-1 &&
		     b==-1 &&
		     s==-1 &&
		     c==65535 &&
		     l==-1L &&
		     f==-1.0f &&
		     d==-1.0;
		
		a=Integer.MAX_VALUE;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		l=a;
		f=a;
		d=a;
		tcr.calcHashInt(a);
		
		res=res &&
			 a==Integer.MAX_VALUE &&
		     b==-1&&
		     s==-1 &&
		     c==65535 &&
		     l==Integer.MAX_VALUE;
		
		return res;		
	}
	
	
	private boolean l2x(TestCaseResult tcr) {
		boolean res;
		
		long a;
		byte b;
		short s;
		char c;
		int i;
		float f;
		double d;
		
		a=0;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		i=(int)a;
		f=a;
		d=a;
		tcr.calcHashLong(a);
		
		res= a==0 &&
		     b==0 &&
		     s==0 &&
		     c==0 &&
		     i==0 &&
		     f==0.0f &&
		     d==0.0;
		
		a=1;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		i=(int)a;
		f=a;
		d=a;
		tcr.calcHashLong(a);
		
		res=res &&
		     a==1 &&
		     b==1 &&
		     s==1 &&
		     c==1 &&
		     i==1 &&
		     f==1.0f &&
		     d==1.0;
		     
		a=2;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		i=(int)a;
		f=a;
		d=a;
		tcr.calcHashLong(a);
		
		res=res &&
		     a==2 &&
		     b==2 &&
		     s==2 &&
		     c==2 &&
		     i==2 &&
		     f==2.0f &&
		     d==2.0;
		     
		a=-1;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		i=(int)a;
		f=a;
		d=a;
		tcr.calcHashLong(a);
		
		res=res &&
			 a==-1 &&
		     b==-1 &&
		     s==-1 &&
		     c==65535 &&
		     i==-1 &&
		     f==-1.0f &&
		     d==-1.0;
		
		a=Integer.MAX_VALUE;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		i=(int)a;
		f=a;
		d=a;
		tcr.calcHashLong(a);
		
		res=res &&
			 a==Integer.MAX_VALUE &&
		     b==-1&&
		     s==-1 &&
		     c==65535 &&
		     i==Integer.MAX_VALUE;

		a=Long.MAX_VALUE;
		b=(byte)a;
		s=(short)a;
		c=(char)a;
		i=(int)a;
		f=a;
		d=a;
		tcr.calcHashLong(a);
		
		res=res &&
			 a==Long.MAX_VALUE &&
		     b==-1&&
		     s==-1 &&
		     c==65535 &&
		     i==-1;
		
		return res;		
	}
	
	private boolean f2x(TestCaseResult tcr) {
		boolean res;
		
		float f;
		int i;
		long l;
		double d;
		
		f=0.0f;
		i=(int)f;
		l=(long)f;
		d=f;
		tcr.calcHashFloat(f);
		
		res = f == 0.0 &&
			i == 0 &&
			l == 0 &&
			d == 0.0;
		
		f=1.0f;
		i=(int)f;
		l=(long)f;
		d=f;
		tcr.calcHashFloat(f);
		
		res = f == 1.0 &&
			i == 1 &&
			l == 1 &&
			d == 1.0;
		
		f=-0.0f;
		i=(int)f;
		l=(long)f;
		d=f;
		tcr.calcHashFloat(f);
		
		res = f == 0.0 &&
			i == 0 &&
			l == 0 &&
			d == 0.0;
		
		f=-1.0f;
		i=(int)f;
		l=(long)f;
		d=f;
		tcr.calcHashFloat(f);
		
		res = f == -1.0 &&
			i == -1 &&
			l == -1 &&
			d == -1.0;
		
		
		f=1.5f;
		i=(int)f;
		l=(long)f;
		d=f;
		tcr.calcHashFloat(f);
		
		res = f == 1.5 &&
			i == 1 &&
			l == 1 &&
			d == 1.5;
		
		f=-1.5f;
		i=(int)f;
		l=(long)f;
		d=f;
		tcr.calcHashFloat(f);
		
		res = f == -1.5 &&
			i == -1 &&
			l == -1 &&
			d == -1.5;
		
		return res;
	}
	
	
	private boolean d2x(TestCaseResult tcr) {
		boolean res;
		
		double d;
		float f;
		int i;
		long l;
		
		d=0.0;
		i=(int)d;
		l=(long)d;
		f=(float)d;
		tcr.calcHashDouble(d);
		
		res = d == 0.0 &&
			i == 0 &&
			l == 0 &&
			f == 0.0;
		
		d=-0.0;
		i=(int)d;
		l=(long)d;
		f=(float)d;
		tcr.calcHashDouble(d);
		
		res = d == 0.0 &&
			i == 0 &&
			l == 0 &&
			f == 0.0;
		
		d=1.0;
		i=(int)d;
		l=(long)d;
		f=(float)d;
		tcr.calcHashDouble(d);
		
		res = d == 1.0 &&
			i == 1 &&
			l == 1 &&
			f == 1.0;
		
		d=-1.0;
		i=(int)d;
		l=(long)d;
		f=(float)d;
		tcr.calcHashDouble(d);
		
		res = d == -1.0 &&
			i == -1 &&
			l == -1 &&
			f == -1.0;
		
		d=1.5;
		i=(int)d;
		l=(long)d;
		f=(float)d;
		tcr.calcHashDouble(d);
		
		res = d == 1.5 &&
			i == 1 &&
			l == 1 &&
			f == 1.5;
		
		d=-1.5;
		i=(int)d;
		l=(long)d;
		f=(float)d;
		tcr.calcHashDouble(d);
		
		res = d == -1.5 &&
			i == -1 &&
			l == -1 &&
			f == -1.5;
		
		return res;
	}
	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result = true;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		Result = i2x(FResult) &&
			l2x(FResult) &&
			f2x(FResult) &&
			d2x(FResult);
		
		FResult.calcResult(Result, this);
		
		return FResult;
	}
}