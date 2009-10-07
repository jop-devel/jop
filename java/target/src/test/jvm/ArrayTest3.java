/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

/* ArrayTest3: test arrays of primitives.
 * Bytecodes exercised: 
 * 
 * 
 */
package jvm;
public class ArrayTest3 extends TestCase {

	public String toString() {
		return "ArrayTest3";
	}
	
	public boolean test() {
		boolean Ok=true;
		
		boolean[] a = new boolean[10];
		//boolean aa;
		byte[] b = new byte[10];
		//byte bb;
		char[] c = new char[10];
		//char cc;
		//double[] d = new double[10];
		//double dd;
		//float [] f = new float[10];
		//float ff;
		int [] e= new int[10];
		//int ee;
		long [] l= new long[10];
		//long ll;
		short [] s= new short[10];
		//short ss;
		
				//Check Correct Array Initialization
		for(int i=0; i<10;i++)
			{
				Ok=Ok && a[i]==false;
				Ok=Ok && b[i]==0;
				Ok=Ok && c[i]=='\u0000';
			//	Ok=Ok && d[i]==0.0;
			//	Ok=Ok && f[i]==0.0;
				Ok=Ok && e[i]==0;
				Ok=Ok && l[i]==0;
				Ok=Ok && s[i]==0;
				
			}
		//Check for length (bytecode:arraylength)
		Ok=Ok && a.length==10;
		Ok=Ok && b.length==10;
		Ok=Ok && c.length==10;
		Ok=Ok && e.length==10;
		Ok=Ok && l.length==10;
		Ok=Ok && s.length==10;
		
		
		//test the bytecodes, stores
		a[2]=true;
		b[3]=124;
		c[4]='c';
		//d[6]=0.321;
		//f[3]=1.21f;
		e[1]=23;
		l[4]=65;
		s[4]=11;
		
		
		//test the bytecodes, loads
		Ok=Ok && a[2]==true;
		Ok=Ok && b[3]==124;
		Ok=Ok && c[4]=='c';
		//Ok=Ok && d[6]==0.321;
		//Ok=Ok && f[3]==1.21f;
		Ok=Ok && e[1]==23;
		Ok=Ok && l[4]==65;
		Ok=Ok && s[4]==11;
		
				
/* catch not implemented		
//Provide a null pointer to test if a NullPointerException is thrown
		a=null;
		try{aa=a[1]; Ok=false; }catch (NullPointerException ex){}
		b=null;
		try{bb=b[1]; Ok=false; }catch (NullPointerException ex){}
		c=null;
		try{cc=c[1]; Ok=false; }catch (NullPointerException ex){}
		d=null;
		try{dd=d[1]; Ok=false; }catch (NullPointerException ex){}
		f=null;
		try{ff=f[1]; Ok=false; }catch (NullPointerException ex){}
		e=null;
		try{ee=e[1]; Ok=false; }catch (NullPointerException ex){}
		l=null;
		try{ll=l[1]; Ok=false; }catch (NullPointerException ex){}
		s=null;
		try{ss=s[1]; Ok=false; }catch (NullPointerException ex){}
// the same, but now for stores
		a=null;
		try{a[1]=aa; Ok=false; }catch (NullPointerException ex){}
		b=null;
		try{b[1]=bb; Ok=false; }catch (NullPointerException ex){}
		c=null;
		try{c[1]=cc; Ok=false; }catch (NullPointerException ex){}
		d=null;
		try{d[1]=dd; Ok=false; }catch (NullPointerException ex){}
		f=null;
		try{f[1]=ff; Ok=false; }catch (NullPointerException ex){}
		e=null;
		try{e[1]=ee; Ok=false; }catch (NullPointerException ex){}
		l=null;
		try{l[1]=ll; Ok=false; }catch (NullPointerException ex){}
		s=null;
		try{s[1]=ss; Ok=false; }catch (NullPointerException ex){}		
		
	*/	
		return Ok;
	}

}