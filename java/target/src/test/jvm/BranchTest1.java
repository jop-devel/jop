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

package jvm;

public class BranchTest1   extends TestCase {

	static class A {}
	static class B {}
	
	public String toString() {
		return "BranchTest1";
	}
	
	public boolean test() {
		A a,b,nullReference;
		
		boolean Ok=true;
		nullReference=new A();
		a=new A();
		b=new A();
		nullReference=null;
		
	
		Ok=Ok && !(a==b); //eq,false
		Ok=Ok &&  (a!=b); //eq,true
		a=b;
		Ok=Ok && (a==b); //ne,false
		Ok=Ok && !(a!=b);//ne,true
	
		//ifnull
		
		Ok=Ok && !(a==null);				//false cond
		Ok=Ok && (a!=null);					//true cond
		
		
		//ifnonull
		Ok=Ok && !(nullReference!=null); 	//false cond
		Ok=Ok && (nullReference==null); 	//true cond
		return Ok;
		}

}