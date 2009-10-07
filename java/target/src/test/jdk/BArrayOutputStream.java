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
//import JOPlibrary.io.ByteArrayOutputStream;
import java.io.*;

public class BArrayOutputStream extends TestCase {

	public String toString() {
		return "BArrayOutputStream";
	}

	public boolean test() {
		boolean Ok=true;
		ByteArrayOutputStream myStream1,myStream2;
		byte  myByteTarget[],myByteSource[];
		String  myStringTarget;
		myStream1 = new ByteArrayOutputStream();
		myStream2 = new ByteArrayOutputStream(30);
		myByteSource=new byte[30];
		
			
		//write(int)
		for(int i=0;i<30;i++){
			myStream1.write(i);
			myStream2.write(i+48); //begin with the "0" in ASCII
			myByteSource[i]=(byte)(i+48);
		}
		//check size
		Ok=Ok&& myStream1.size()==30;
		Ok=Ok&& myStream2.size()==30;
		
		//toByteArray
		myByteTarget=myStream1.toByteArray();
		for(int i=0;i<30;i++){
			Ok=Ok && myByteTarget[i]==i;
		}
		Ok=Ok&& myStream1.size()==30;
		
		
		//toString
			myStringTarget=myStream2.toString();
			for(int i=0;i<30;i++){
				Ok=Ok && myStringTarget.charAt(i)==(char)(i+48);
			}
		
		//toString(String)
		/*	try{
				myStringTarget=myStream2.toString("ASCII");
			}catch(UnsupportedEncodingException e){}
			
			for(int i=0;i<30;i++){
				Ok=Ok && myStringTarget.charAt(i)==(char)(i+48);
			}
		 	*/
		//reset()
			myStringTarget=myStream2.toString();
			Ok=Ok && myStream2.size()==30;
			Ok=Ok && myStream1.size()==30;
			myStream2.reset();
			myStream1.reset();	
			Ok=Ok && myStream2.size()==0;
			Ok=Ok && myStream1.size()==0;
		
		//write(byte[],off,len)
		//now, with the Stream reseted, fill myStream2 again 	
		myStream2.write(myByteSource,14,15);
		
		myStringTarget=myStream2.toString();
		for(int i=0;i<14;i++){
			Ok=Ok && myStringTarget.charAt(i)==(char)(i+62);
		}
		
		//TODO: what happens if off or len are bigger than the actual size of the array??
		
		//Copy from myStream2 to myStream1
		//method not found
		
		/*try{
		myStream2.writeTo(myStream1);
		} catch(IOException e){}
		
		myStringTarget=myStream1.toString();
		for(int i=0;i<14;i++){
			Ok=Ok && myStringTarget.charAt(i)==(char)(i+62);
		}
		*/
		return Ok;
		
	}

}
