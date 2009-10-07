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
import java.io.*;

public class DInputStream extends TestCase {

	class myUTFInputStream extends InputStream{
		private int myInt;
		private boolean initialized;
		public int read(){
			
			myInt++;
			if(!initialized){ initialized=true; myInt=32; return 0;}
			
			return (myInt -1);
			
		}
	}
	
	
	
	class myInputStream extends InputStream{
		private int myInt;
		private boolean buggy,end;
		public int read() throws IOException {
			if (buggy) throw new IOException();
			myInt++;
			if(myInt>255){myInt=0;}
			if (end) return -1;
			return (myInt -1);
		}
		
		public void setError(){
			buggy=true;
		}
		
		public void setCorrect(){
			buggy=false;
		}
		
		public void reset(){
			myInt=0;			
		}
		
		public void setEndReached(){
			end=true;			
		}
		
		public void setEndNotReached(){
			end=false;
		}
	}
	
	public String toString() {
		return "DInputStream";
	}

	public boolean test() {
		boolean Ok=true;
		DataInputStream myStream1,myStream2;
		myInputStream myInputStream1;
		myUTFInputStream myInputStream2;
		
		byte  myByteTarget[];
		String  myStringTarget;
		myInputStream1=new myInputStream();
		myInputStream2=new myUTFInputStream();
		
		myStream1 = new DataInputStream(myInputStream1);
		myStream2 = new DataInputStream(myInputStream2);
		//read(int)
		myByteTarget=new byte[13];
		try{
		Ok=Ok && myStream1.read(myByteTarget)==13;
		} catch (IOException e) {}
		//check
		for(int i=0;i<13;i++){
		Ok= Ok && myByteTarget[i]==i;
		}
		//simulate end of stream
		myInputStream1.setEndReached();
		try{
		Ok=Ok && myStream1.read(myByteTarget)==-1;
		} catch (IOException e) {}	
		//verify that IOException is thrown, set buggy I/O
		//myInputStream1.setError();
		try{
		myStream1.read(myByteTarget);
		} catch (IOException e) {}	
			
		//read(byte[],int,int)
		myInputStream1.reset();
		myInputStream1.setEndNotReached();
			try{
			myStream1.read(myByteTarget,6,5);	
			} catch (IOException e) {
			System.out.println("Exception thrown");
			}
		for(int i=0;i<5;i++){
				Ok= Ok && myByteTarget[i+6]==i;
				}	
		//simulate end of stream
		myInputStream1.setEndReached();
		try{
		Ok=Ok && myStream1.read(myByteTarget)==-1;
		} catch (IOException e) {}
		
		
		//readBoolean
		myInputStream1.reset();
		myInputStream1.setEndNotReached();
		try{
		Ok=Ok && myStream1.readBoolean()==false;	
		Ok=Ok && myStream1.readBoolean()==true;
		} catch (IOException e) {}
		//myInputStream1.setEndReached();
		try{			
			myStream1.read();	
			} catch (IOException e) {System.out.println("End of stream");}
		//myInputStream1.setError();
		try{			
			myStream1.read();	
			} catch (IOException e) {System.out.println("Error");}
		
		//readChar
		myInputStream1.reset();
		myInputStream1.setCorrect();
		try{
		for(int i=0;i<18;i++) {myInputStream1.read();}
		Ok=Ok && myStream1.readChar()=='\u1213';
		myInputStream1.read();
		Ok=Ok && myStream1.readChar()=='\u1516';
		} catch (IOException e) {}
		
		//readDouble - MISSING METHOD, (no support for double in JOP)
		
		/*
		myInputStream1.reset();
		myInputStream1.setCorrect();
		try{
		Ok=Ok && myStream1.readDouble()==Double.longBitsToDouble(0x01020304050607l);
		} catch (IOException e) {}
		*/

		//readFully(byte[])
		myInputStream1.reset();
		myInputStream1.setCorrect();
		try{
		myStream1.read(myByteTarget);
		} catch (IOException e) {}
		for(int i=0;i<13;i++){
			Ok=Ok && myByteTarget[i]==i;
		}
		
		//readFully(byte[],off,len)
		myInputStream1.reset();
		myInputStream1.setCorrect();
		try{
		myStream1.readFully(myByteTarget,5,7);
		} catch (IOException e) {System.out.println("thrown");}
		for(int i=0;i<7;i++){
			Ok=Ok && myByteTarget[i+5]==i;
		}
		
		//readLong() readShort() readUnsignedByte() readByte()
		//readUnsignedShort() readShort()
		myInputStream1.reset();
		myInputStream1.setCorrect();
		try{
		Ok=Ok&&myStream1.readLong()==0x0001020304050607l;
		Ok=Ok&&myStream1.readUnsignedByte()==0x08;
		Ok=Ok&&myStream1.readByte()==0x09;
		Ok=Ok&&myStream1.readUnsignedShort()==0x0A0B;
		Ok=Ok&&myStream1.readShort()==0x0C0D;
		
		//produce signed result
		for(int i=0;i<128;i++){myStream1.read();}
		Ok=Ok&&myStream1.readShort()==(short)0x8e8f; //-29041
		} catch (IOException e) {System.out.println("thrown");}
		
		//skip, readInt
		myInputStream1.reset();
		myInputStream1.setCorrect();
		try{
		myStream1.skipBytes(10);
		Ok=Ok&&myStream1.read()==10;
		Ok=Ok&&myStream1.read()==11;
		Ok=Ok&&myStream1.read()==12;
		Ok=Ok&&myStream1.readInt()==0x0D0E0F10;	
		} catch (IOException e) {System.out.println("thrown");}
		
		//readUTF (), test with a String in the ASCII equivalent range
		myInputStream1.reset();
		myInputStream1.setCorrect();
		try{
		String myUTFString;
		myUTFString=myStream2.readUTF();
		Ok=Ok && myUTFString.length()==32;
		Ok=Ok && myUTFString.charAt(0)==(char)33;
		Ok=Ok && myUTFString.charAt(1)==(char)34;
		Ok=Ok && myUTFString.charAt(2)==(char)35;
		Ok=Ok && myUTFString.charAt(22)==(char)55;
			
		//System.out.println(myUTFString.length());
		//System.out.println(myUTFString.charAt(0));
			
		} catch (IOException e) {System.out.println("thrown");}
		
		
		
		return Ok;
	}
}
