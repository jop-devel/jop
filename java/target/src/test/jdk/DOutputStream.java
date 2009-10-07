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
/*
import JOPlibrary.io.DataOutputStream;
import JOPlibrary.io.OutputStream;
import JOPlibrary.io.IOException;
 * 
 */


public class DOutputStream extends TestCase {

	class myUTFInputStream extends OutputStream{
		private int myInt;
		private boolean initialized;
		public void write(int a){
			myInt++;
			if(!initialized){ initialized=true; myInt=32; }
		}
	}
	
	
	class myOutputStream extends OutputStream{
		private int myIndex;
		private boolean buggy,end;
		byte  myIntTarget[];
		
		public  void write(int a) throws IOException {
			if (buggy) throw new IOException();
			myIntTarget[myIndex]=(byte)a;
			myIndex++;
			if(myIndex>99){myIndex=0;};
		}
		
		public void flush() throws IOException
		{
			reset();
		}
		
		public void write(byte b[], int off, int len) throws IOException {
			if (off+len>b.length) {return;}
			for(int i=0;i<len;i++){
				write(b[i+off]);
			}
		}
		
		public byte read(){
			myIndex++;
			return myIntTarget[myIndex-1];
		}
		
		public myOutputStream(){
			myIndex=0;
			myIntTarget=new byte[100];
		}
		public int length(){
			return myIndex;
		}
		
		public boolean checkData(byte data,int position){
			if(position>99){return false;}
			return myIntTarget[position]==data; 
						
		}
		
		public boolean checkData(byte data[],int offset, int len){
			boolean Ok=true;
			if(offset+len>99){return false;}
			for(int i=0; i<len; i++){
			Ok=Ok && checkData(data[i],offset+i);
			}
			return Ok;
		}
		
		public void setError(){
			buggy=true;
		}
		
		public void setCorrect(){
			buggy=false;
		}
		
		public void reset(){
		myIndex=0;				
		}
		
		public void setEndReached(){
			end=true;			
		}
		
		public void setEndNotReached(){
			end=false;
		}
	}
	
	public String toString() {
		return "DOutputStream";
	}

	public boolean test() {
		boolean Ok=true;
		DataOutputStream myStream1,myStream2;
		myOutputStream myOutputStream1;
		myUTFInputStream myInputStream2;
		byte myDataSource[];
		myOutputStream1=new myOutputStream();
		myStream1 = new DataOutputStream(myOutputStream1);
		
		myDataSource=new byte [30];
		
		//Initialize array of source data
		for(byte i=0;i<30;i++){
		myDataSource[i]=i;
		}
				
		//write(int)
		myOutputStream1.setCorrect();
		try{
		for(int i=0;i<30;i++){
			myStream1.write(myDataSource[i]);
		}
		
		} catch(IOException e){}
		
		Ok=Ok && myOutputStream1.checkData(myDataSource,0,30);
		
		//size() method not found
		//Ok=Ok && myStream1.size()==30;
		
		//write(byte[], int,int)
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
		myStream1.write(myDataSource,0,15);
		
		} catch(IOException e){}

		Ok=Ok && myOutputStream1.checkData(myDataSource,0,14);
		
		//writeBoolean(boolean v)
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
			myStream1.writeBoolean(true);
			myStream1.writeBoolean(false);
			myStream1.writeBoolean(false);
			myStream1.writeBoolean(true);
				
			} catch(IOException e){}
		
		Ok=Ok && myOutputStream1.checkData((byte)1,0);	
		Ok=Ok && myOutputStream1.checkData((byte)0,1);
		Ok=Ok && myOutputStream1.checkData((byte)0,2);
		Ok=Ok && myOutputStream1.checkData((byte)1,3);
		
		//writeByte(int)
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
			myStream1.writeByte(127);
			myStream1.writeByte(-128);
			} catch(IOException e){}
		Ok=Ok && myOutputStream1.checkData((byte)127,0);
		Ok=Ok && myOutputStream1.checkData((byte)-128,1);
		
		//writeByte(int)
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
			myStream1.writeShort(32767);
			myStream1.writeShort(-32768);
			} catch(IOException e){}
		Ok=Ok && myOutputStream1.checkData((byte)127,0);
		Ok=Ok && myOutputStream1.checkData((byte)-1,1);
		Ok=Ok && myOutputStream1.checkData((byte)-128,2);
		Ok=Ok && myOutputStream1.checkData((byte)0,3);
		
		
		//writeUTF
		String myString="abcdefghi";
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
		myStream1.writeUTF(myString);
		} catch (IOException e) {}
		Ok=Ok && myOutputStream1.checkData((byte)0,0);
		Ok=Ok && myOutputStream1.checkData((byte)9,1);
		Ok=Ok && myOutputStream1.checkData((byte)'a',2);
		Ok=Ok && myOutputStream1.checkData((byte)'b',3);
		Ok=Ok && myOutputStream1.checkData((byte)'i',10);
		
		//writeLong
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
		myStream1.writeLong(0x01000a0b0c0d0e0fL);
		} catch (IOException e) {}
		Ok=Ok && myOutputStream1.checkData((byte)0x1,0);
		Ok=Ok && myOutputStream1.checkData((byte)0xa,2);
		Ok=Ok && myOutputStream1.checkData((byte)0xf,7);
		
		//writeInteger
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
		myStream1.writeInt(0x7FFFFFFF);
		myStream1.writeInt(0x80000000);
		} catch (IOException e) {}
		Ok=Ok && myOutputStream1.checkData((byte)0x7f,0);
		Ok=Ok && myOutputStream1.checkData((byte)0xff,2);
		Ok=Ok && myOutputStream1.checkData((byte)0xff,3);
		Ok=Ok && myOutputStream1.checkData((byte)0x80,4);
		
		//writeChars()
		myOutputStream1.setCorrect();
		myOutputStream1.reset();
		try{
		myStream1.writeChars(myString);
		
		} catch (IOException e) {}
		
		Ok=Ok && myOutputStream1.checkData((byte)0,0);
		Ok=Ok && myOutputStream1.checkData((byte)'a',1);
		Ok=Ok && myOutputStream1.checkData((byte)0,2);
		Ok=Ok && myOutputStream1.checkData((byte)'b',3);
		Ok=Ok && myOutputStream1.checkData((byte)0,4);
		Ok=Ok && myOutputStream1.checkData((byte)'c',5);
		
		//flush()
		try{
			myStream1.flush();
			} catch (IOException e) {}
		Ok=Ok && myOutputStream1.length()==0;
		return Ok;
	}
}
