/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package jvm;
import java.lang.*;

public class SystemCopy extends TestCase
{
	public String getName () 
	{
		return "SystemCopy";
	}
	public boolean test ()
	{
		boolean bOk = true;
		bOk = bOk && testCopy ();
		return bOk;
	}
	private boolean compare (Object[] a,Object[] b)
	{
		for (int i=0;i<a.length&&i<b.length;++i)
			if ( a[i] != b[i] )
				return false;
		return true;
	}
	private boolean compare (int[] a,int[] b)
	{
		for (int i=0;i<a.length&&i<b.length;++i)
			if ( a[i] != b[i] )
				return false;
		return true;
	}
	private boolean testCopy ()
	{
		Object[] oSrc1;
		Object[] oSrc = { "ABC" };
		Object[] oDest = { null };
		oSrc = new Object[]  { "ABC", "BDE" };
		oSrc1 = new Object[] { oSrc[0], oSrc[1] };
		oDest = new Object[] { null, null };
		System.arraycopy (oSrc,0,oDest,0,2);
		if ( !compare (oSrc,oDest) )
			return false;
		if ( !compare (oSrc,oSrc1) )
			return false;
		
		int[] nSrc =  { 1,2,3,4,5 };
		int[] nDest = { 1,1,2,4,5 };
		System.arraycopy (nSrc,0,nSrc,1,2);
		if ( !compare (nSrc,nDest) )
			return false;
		
		nSrc = new int []  { 1,2,3,4,5 };
		nDest = new int [] { 1,2,4,5,5 };
		System.arraycopy (nSrc,3,nSrc,2,2);
		if ( !compare (nSrc,nDest) )
			return false;
		
		String s = "aa";
		Object o = null;
		foo(s, o);
		return true;
	}
	
	void foo(Object a, Object b) {
		a = b;
	}
	
}
