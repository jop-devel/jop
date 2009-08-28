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

public class Logic1 extends TestCase {
	public String toString() {
		return "Logic1";
	}

	public boolean test() {
		boolean Ok=true;
		int myInteger;
		int shift1,shift2;
		myInteger=0x13521143; //0001 0011 0101 0010 0001 0001 0100 0011
	
		//check shift
		myInteger=myInteger<<5; 	//ishl
		Ok=Ok && myInteger==0x6a422860;
		myInteger=myInteger>>5; 	//ishr
		Ok=Ok && myInteger==0x03521143;
		
		//check ishr for sign extension 
		myInteger=0x93521143; //1001 0011 0101 0010 0001 0001 0100 0011
		myInteger=myInteger>>2;
		Ok=Ok && myInteger==0xE4D48450;//-455834544;
		
		//check iushr for zero extension
		myInteger=0x93521143; //1001 0011 0101 0010 0001 0001 0100 0011	
		myInteger=myInteger>>>4;	//iushr
		Ok=Ok && myInteger==0x09352114;
		
		//check the shift by "value & 0x1f" feature
		//use variables to avoid possible static optimizations
		shift1=0x4564644;
		shift2=0x04;
		Ok=Ok && myInteger<<shift1==myInteger<<shift2;
		Ok=Ok && myInteger>>shift1==myInteger>>shift2;
		Ok=Ok && myInteger>>>shift1==myInteger>>>shift2;
		
		return Ok;
	}



}
