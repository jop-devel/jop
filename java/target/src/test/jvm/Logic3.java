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

public class Logic3  extends TestCase{

	public String toString() {
		return "Logic3";
	}
	
	public boolean test() {	
		boolean Ok=true;
		int i,j; 
		i= 1073741828;
		j= -1073741824;
		
		//test branch on integer comparison
		//issue when  i-j>=2^31
		Ok= Ok && i>j;
		
		//The following should be useful to verify that the issue was corrected
		i++;
		Ok= Ok && i>j;
		
		i++;
		Ok= Ok && i>j;
		
		i++;
		Ok= Ok && i>j;
		
	   return Ok;
	}
}