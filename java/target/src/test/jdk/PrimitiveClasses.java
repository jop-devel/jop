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

package jdk;

import jvm.TestCase;

public class PrimitiveClasses extends TestCase {

	public String toString() {
		return "PrimitiveClasses";
	}
	public boolean test() {
	boolean Ok=true;	
	
	//BOOLEAN
	Boolean myBoolean1=new Boolean(true);
	////possible issue when parameter is the string "TRuE"
	Boolean myBoolean2=new Boolean("true");
	//possible issue when parameter is null
	Boolean myBoolean3=new Boolean(false);
	Boolean myBoolean4=new Boolean("fAlSe");
	
	//test constructors
	Ok=Ok&& myBoolean1.booleanValue();
	Ok=Ok&& myBoolean2.booleanValue();
	Ok=Ok&& ! myBoolean3.booleanValue();
	
	//equals()
	Ok=Ok &&  myBoolean1.equals(myBoolean2);
	Ok=Ok &&  myBoolean3.equals(myBoolean4);
	Ok=Ok &&  !myBoolean1.equals(myBoolean3);
	
	//hashCode
	Ok=Ok && myBoolean1.hashCode()==myBoolean1.hashCode();
	Ok=Ok && myBoolean1.hashCode()==myBoolean2.hashCode();
	Ok=Ok && myBoolean3.hashCode()==myBoolean4.hashCode();
	Ok=Ok && myBoolean1.hashCode()!=myBoolean3.hashCode();

	//toString
	//TODO:check String.equalsIgnoreCase, possible issue.
	//Avoid the use of equalsIgnoreCase
		
	Ok=Ok && (myBoolean1.toString()).equals("true");
	Ok=Ok && (myBoolean3.toString()).equals("false");
	Ok=Ok &&!(myBoolean1.toString()).equals("false");
	Ok=Ok &&!(myBoolean3.toString()).equals("true");
		
	//valueOf(String)
	//this method should receive a String instead of a boolean
	//Ok=Ok && (Boolean.valueOf("true")).equals(myBoolean1);
	
	//BYTE
	Byte myByte1=new Byte((byte)122);
	Byte myByte2=new Byte((byte)122);
	//The constructor Byte(string) is not defined
	//Byte myByte2=new Byte("22");
	Ok=Ok && ((byte)122)==myByte1.byteValue();
	//Ok=Ok && ((byte)22)==myByte1.byteValue();
	//check if NumberFormatException is thrown 
	//new Byte("myWrongString");
	
	//parseByte(String)
	Ok=Ok && 123==Byte.parseByte("123");
	Ok=Ok && 127==Byte.parseByte("127");
	Ok=Ok && -128==Byte.parseByte("-128");
	Ok=Ok && !(123==Byte.parseByte("121"));
	
	//check for exception
	//Ok=Ok && !(123==Byte.parseByte("129"));
	
	//equals(Object)
	Byte myByte3=myByte2;
	Ok=Ok && myByte2.equals(myByte3);
	Ok=Ok && myByte2.equals(myByte1);
	myByte3=new Byte((byte)123);
	Ok=Ok && !myByte2.equals(myByte3);
	
	//hashCode
	Ok=Ok && myByte1.hashCode()==myByte1.hashCode();
	Ok=Ok && (myByte1.hashCode()==myByte2.hashCode());
	Ok=Ok && !(myByte3.hashCode()==myByte1.hashCode());
	
	//toString
	Ok=Ok && (myByte3.toString()).equals("123");
	return Ok;
	}
}
