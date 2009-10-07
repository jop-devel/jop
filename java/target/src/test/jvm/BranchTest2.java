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

/*
 * BranchTest2:
 * 
 */
package jvm;



public class BranchTest2  extends TestCase {

	
	public String toString() {
		return "BranchTest2";
	}
	
	public boolean test() {
		//temp: variable for avoiding Javac's static optimization when evaluating
		//boolean expressions
		boolean Ok=true,temp=false;
		//need variables, to avoid static optimizations
		int one=1, two=2, three=3; 
		
		//test if_icmp<cond>, true conditions
		//test eq
		temp=!(one==1);		//force Javac to insert the desired bytecode
		temp=!temp;
		Ok= Ok && temp;
		//test ne
		temp=!(one!=2);
		temp=!temp;
		Ok= Ok && temp;
		//test lt
		temp=!(one<2);
		temp=!temp;
		Ok= Ok && temp;
		//test gt
		temp=!(three>2);
		temp=!temp;
		Ok= Ok && temp;
		
		//we split in two the test of "<=", providing both possible true conditions
		//test le
		temp=!(one<=2);
		temp=!temp;
		Ok= Ok && temp;
		
		temp=!(two<=2);
		temp=!temp;
		Ok= Ok && temp;
		
		//we split in two the test of ">=", providing both possible true conditions
		//test ge
		temp=!(two>=1);
		temp=!temp;
		Ok= Ok && temp;
		
		temp=!(two>=2);
		temp=!temp;
		Ok= Ok && temp;
		
		//test if_icmp<cond>, false conditions
		
		Ok= Ok && !(one==2);
		Ok= Ok && !(one!=1);
		Ok= Ok && !(three<2);
		Ok= Ok && !(two>3);
		//here we have just one possible false condition for each le,ge
		Ok= Ok && !(two<=1);
		Ok= Ok && !(two>=3);
		return Ok;
		}

}