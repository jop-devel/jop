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

//Character, String,  StringBuffer

public class PrimitiveClasses3 extends TestCase {

	public String toString() {
		return "PrimitiveClasses3";
	}
	
	public boolean testCharacter(){
		boolean Ok=true;	
	
	//Character
	Character myCharacter1=new Character('a');
	Character myCharacter2=new Character('f');
	Character myCharacter3=new Character('f');
	
	//equals()
	Ok=Ok &&  !myCharacter1.equals(myCharacter2);
	Ok=Ok &&  !myCharacter3.equals(myCharacter1);
	Ok=Ok &&  myCharacter2.equals(myCharacter3);
	
	//hashCode()
	Ok=Ok && myCharacter1.hashCode()!=myCharacter2.hashCode();
	Ok=Ok && myCharacter1.hashCode()!=myCharacter3.hashCode();
	Ok=Ok && myCharacter3.hashCode()==myCharacter2.hashCode();
			
	//CharValue()
	Ok= Ok && 'a'==myCharacter1.charValue();
	Ok= Ok && 'f'==myCharacter2.charValue();
	Ok= Ok && 'w'!=myCharacter3.charValue();
	
	//digit--only implemented for radix 10
	//Ok=Ok && 10==Character.digit('a',16);
	//Ok=Ok && 15==Character.digit('f',16);
	Ok=Ok && -1==Character.digit('a',10);
	Ok=Ok && 7==Character.digit('7',10);
	
	//is(Lower|Upper)Case
	Ok=Ok && Character.isLowerCase('a');
	Ok=Ok && !Character.isLowerCase('A');
	Ok=Ok && Character.isUpperCase('A');
	Ok=Ok && !Character.isUpperCase('a');
	
	//to(Lower|Upper)Case
	Ok=Ok && 'a'==Character.toLowerCase('A');
	Ok=Ok && 'A'==Character.toUpperCase('a');
	
	//isDigit-- produce some "Bengali" digits in unicode (fails in JOP) 
	//Ok=Ok && Character.isDigit((char)0x09E7);
	//Ok=Ok && Character.isDigit((char)0x09E8);
	//produce some ISO-LATIN-1 digits in unicode
	Ok=Ok && Character.isDigit((char)0x0031);
	Ok=Ok && Character.isDigit((char)0x0033);
	
	return Ok;
	}
	
	public boolean testStringBuffer(){
		boolean Ok=true;
		int i;
		//StringBuffer
		StringBuffer myStringBuffer1=new StringBuffer();
		StringBuffer myStringBuffer2=new StringBuffer(30);
		StringBuffer myStringBuffer3=new StringBuffer("My character sequence");
		for(i=0; i<21; i++)
		{Ok=Ok && myStringBuffer3.charAt(i)=="My character sequence".charAt(i);}
			
		//length()
		Ok=Ok && myStringBuffer1.length()==0;
		Ok=Ok && myStringBuffer2.length()==0;
		Ok=Ok && myStringBuffer3.length()==21;
		
		//append(Object)
		String myString1=new String("myString");
		myStringBuffer1.append(myString1);
		for(i=0; i<myString1.length(); i++)
		{Ok=Ok && myStringBuffer1.charAt(i)==myString1.charAt(i);}
		
		//append(boolean)/insert(int, boolean)
		myStringBuffer1=new StringBuffer();
		myStringBuffer1.append(true);
		for(i=0; i<4; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="true".charAt(i);}
		myStringBuffer1.insert(2, false);
		for(i=0; i<9; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="trfalseue".charAt(i);}
		
		//append(char)/insert(int,char)
		//System.out.println(myStringBuffer1.toString());
		myStringBuffer1.append('a');
		//System.out.println(myStringBuffer1.toString());
		Ok=Ok && myStringBuffer1.charAt(myStringBuffer1.length()-1)=='a';
		
		//possible issue in insert(int,char)
		//trfalseue trfalseuea trfxaaaaaaa
		
		//myStringBuffer1.insert(3,'x');
		//System.out.println(myStringBuffer1.toString());
		//for(i=0; i<9; i++)
		//{Ok=Ok && myStringBuffer1.charAt(i)=="trfxalseuea".charAt(i);}
		
		
		//append(String)/insert(int,String)
		myStringBuffer2.append("myString");
		for(i=0; i<myString1.length(); i++)
		{Ok=Ok && myStringBuffer2.charAt(i)==myString1.charAt(i);}
		myStringBuffer2.insert(3,"myString");
		for(i=0; i<16; i++)
		{Ok=Ok && myStringBuffer2.charAt(i)=="mySmyStringtring".charAt(i);}
		
		//append(char[])/insert(int,char[])
		myStringBuffer1=new StringBuffer();
		char[] myByteArray={'m','y','S','t','r','i','n','g'};
		myStringBuffer1.append(myByteArray);
		for(i=0; i<myString1.length(); i++)
		{Ok=Ok && myStringBuffer1.charAt(i)==myString1.charAt(i);}
		myStringBuffer1.insert(2,myByteArray);
		for(i=0; i<16; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="mymyStringString".charAt(i);}
		
		//append(char[],int,int)
		myStringBuffer1=new StringBuffer();
		myStringBuffer1.append(myByteArray,3,5);
		for(i=0; i+3<myString1.length(); i++)
		{Ok=Ok && myStringBuffer1.charAt(i)==myString1.charAt(i+3);}
		
		//produces the f2d bytecode which is not implemented
		//append(float)/insert(int,float)
		/*
		float myFloat=444433.34f;
		myStringBuffer1=new StringBuffer();
		myStringBuffer1.append(myFloat);
		for(i=0; i<9; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="444433.34".charAt(i);}
		*/
			
		/* Not implemented
		myStringBuffer1.insert(3,myFloat);
		for(int i=0; i<18; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="444444433.34433.34".charAt(i);}*/
			
		//append(int)/ insert(int, int)
		int myInt=444435424;
		myStringBuffer1=new StringBuffer();
		myStringBuffer1.append(myInt);
		for(i=0; i<9; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="444435424".charAt(i);}
		myStringBuffer1.insert(9,myInt);
		for(i=0; i<18; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="444435424444435424".charAt(i);}
		
		
		//append(long)/ insert(int,long)
		long myLong=4444354248989l;
		myStringBuffer1=new StringBuffer();
		myStringBuffer1.append(myLong);
		for(i=0; i<13; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="4444354248989".charAt(i);}
		myStringBuffer1.insert(5,myLong);
		for(i=0; i<26; i++)
		{Ok=Ok && myStringBuffer1.charAt(i)=="44443444435424898954248989".charAt(i);}
		
		//setCharAt(int) / reverse()
		//capacity() / ensureCapacity
		
		/* Not implemented
		int oldCapacity=myStringBuffer1.capacity();
		myStringBuffer1.ensureCapacity(oldCapacity*2);
		Ok=Ok && myStringBuffer1.capacity()==oldCapacity*2+2;
		
		oldCapacity=myStringBuffer1.capacity();
		myStringBuffer1.ensureCapacity(oldCapacity*2+3);
		Ok=Ok && myStringBuffer1.capacity()==oldCapacity*2+3;
		*/
		//getChars(int srcBegin,int srcEnd,char dst[],int dstBegin)
		myStringBuffer1=new StringBuffer();
		myStringBuffer1.append(myString1);
		myStringBuffer1.getChars(1,5,myByteArray,2);
		for(i=0; i<4; i++)
		{Ok=Ok && myByteArray[i+2]=="yStr".charAt(i);}
		return Ok;
		
	}
	
	
	public boolean testString2(){
		boolean Ok=true;
		int i;
		//String
		byte myByteArray[]={'M','y','S','t','r','i','n','g'};
		char myCharArray[]={'M','y','S','t','r','i','n','g'};
		String myString1=new String("My test string1");
		String myString2,myString3,myString4,myString5,myString6,myString7,
		myString8,myString9;
		StringBuffer myStb=new StringBuffer("myString");
		
		//to avoid uninitialized Strings
		myString2= myString1; myString3= myString1; 
		myString4= myString1; myString5= myString1;
		
		try{
		myString2=new String(myByteArray);
		myString3=new String(myByteArray,2,5);
		
		myString4=new String(myByteArray,2,5,"ASCII");
		myString5=new String(myByteArray,"ASCII");
			
		} catch(UnsupportedEncodingException e ) {}
		
		myString6=new String(myCharArray);
		myString7=new String(myCharArray,2,5);
		myString8=new String();
		myString9=new String(myStb);
		myString6="mystring";
		
		
		
		//lastIndexOf(int)
		Ok=Ok && 2==myString2.lastIndexOf('S');
		Ok=Ok && -1==myString1.lastIndexOf('P');
		
		// public int lastIndexOf(int ch,int fromIndex)	
		Ok=Ok && 3==myString2.lastIndexOf(116,3);
		Ok=Ok && -1==myString2.lastIndexOf(116,2);
		
		//regionMatches (boolean, int, String, int, int)
		Ok=Ok && myString2.regionMatches(true,2,myString3,0,5);
		Ok=Ok && !myString2.regionMatches(true,2,myString3,0,6);
		//here the case differs for the first element
		Ok=Ok && myString5.regionMatches(true,0,myString9,0,8);
		Ok=Ok && !myString5.regionMatches(false,0,myString9,0,8);
		
		//replace(char,char)
		Ok=Ok && (myString1.replace('s','x')).equals("My text xtring1");
		
		//startsWith(String)
		Ok=Ok && myString1.startsWith("My t");
		Ok=Ok && !myString1.startsWith(myString3);
		
		//startsWith(String, int)
		Ok=Ok && myString1.startsWith(" t",2);
		Ok=Ok && !myString1.startsWith("My t",2);
		
		//substring(int)
		Ok=Ok && (myString1.substring(4)).equals("est string1");
		Ok=Ok && !(myString2.substring(4)).equals("est string1");

		//substring(int,int)
		Ok=Ok && (myString2.substring(2,7)).equals(myString3);
		Ok=Ok && !(myString2.substring(2,6)).equals(myString3);
		
		//toCharArray ()
		myCharArray=myString2.toCharArray();
		for(i=0;i<myString2.length();i++)
		{Ok=Ok && myString5.charAt(i)==myCharArray[i];}
		
		//toLowerCase()
		Ok=Ok && (myString2.toLowerCase()).equals(myString6);
		
		//toUpperCase()
		Ok=Ok && (myString2.toUpperCase()).equals("MYSTRING");
		
		//trim()
		Ok=Ok && (" spacedString ".trim()).equals("spacedString");
		
		
		//charAt()
		Ok=Ok && 'M'==myString1.charAt(0);
		Ok=Ok && 'y'==myString1.charAt(1);
		Ok=Ok && 'g'==myString2.charAt(7);
		
		//valueOf(boolean)
		Ok=Ok && (String.valueOf(true)).equals("true");
		Ok=Ok && !(String.valueOf(false)).equals("true");
		
		//valueOf(char)
		Ok=Ok && (String.valueOf('x')).equals("x");
		
		//valueOf(int)
		Ok=Ok && (String.valueOf(321)).equals("321");
		
		//valueOf(long)
		Ok=Ok && (String.valueOf(321321l)).equals("321321");
		//valueOf(float)--Not found
		//Ok=Ok && (String.valueOf(321.32f)).equals("321.32");
			
	return Ok;
	}
	public boolean testString1(){
	boolean Ok=true;
	int i;
	//String
	byte myByteArray[]={'M','y','S','t','r','i','n','g'};
	char myCharArray[]={'M','y','S','t','r','i','n','g'};
	String myString1=new String("My test string1");
	String myString2,myString3,myString4,myString5,myString6,myString7,
	myString8,myString9;
	StringBuffer myStb=new StringBuffer("myString");
	
	//to avoid uninitialized Strings
	myString2= myString1; myString3= myString1; 
	myString4= myString1; myString5= myString1;
	
	try{
	myString2=new String(myByteArray);
	myString3=new String(myByteArray,2,5);
	
	myString4=new String(myByteArray,2,5,"ASCII");
	myString5=new String(myByteArray,"ASCII");
		
	} catch(UnsupportedEncodingException e ) {}
	
	myString6=new String(myCharArray);
	myString7=new String(myCharArray,2,5);
	myString8=new String();
	myString9=new String(myStb);
	
	//check constructors
	Ok=Ok && myString1.equals("My test string1");
	Ok=Ok && myString2.equals("MyString");
	Ok=Ok && myString3.equals("Strin");
	Ok=Ok && myString4.equals(myString3);
	Ok=Ok && myString5.equals(myString2);
	Ok=Ok && myString6.equals(myString2);
	Ok=Ok && myString7.equals(myString3);
	Ok=Ok && myString8.equals("");
	Ok=Ok && myString9.equals("myString");
	myString6="mystring";
	
	//compareTo(String)
	Ok=Ok && 0>myString9.compareTo("myStrinh");
	Ok=Ok && 0==myString1.compareTo("My test string1");
		
	//concat(String)
	Ok=Ok && (myString9.concat(myString4)).equals("myStringStrin");
	
	//copyValueOf(char[])--Not implemented
	//Ok=Ok && (String.copyValueOf(myCharArray)).equals("MyString");
	
	//copyValueOf(char[], int, int)--Not implemented
	//Ok=Ok && (String.copyValueOf(myCharArray,2,4)).equals("Stri");
	
	//endsWith
	Ok=Ok && myString7.endsWith("rin");
	Ok=Ok && !myString4.endsWith(myString6);
	
	//getBytes
	myByteArray=myString9.getBytes();
	for(i=0;i<myString9.length();i++)
	{Ok=Ok && myByteArray[i]==myString9.charAt(i);}
	
	//getBytes(String)
	try{myByteArray=myString9.getBytes("ASCII");} 
	catch(UnsupportedEncodingException e){}
	for(i=0;i<myString9.length();i++)
	{Ok=Ok && myByteArray[i]==myString9.charAt(i);}
	
	//public void getChars(int srcBegin,int srcEnd,char dst[],int dstBegin)
	myString9.getChars(2,4,myCharArray,3);
	for(i=0;i<2;i++)
	{Ok=Ok && myCharArray[i+3]==myString9.charAt(i+2);}
	//hashCode
	
	//indexOf (String)
	Ok=Ok && 2==myString5.indexOf(myString7);
	Ok=Ok && -1==myString5.indexOf(myString1);
	
	//indexOf (String, int)
	Ok=Ok && 2==myString2.indexOf(myString3,0);
	Ok=Ok && -1==myString1.indexOf(myString5,4);
	
	//indexOf (int)
	Ok=Ok && 2==myString2.indexOf(83);
	Ok=Ok && -1==myString2.indexOf(130);
	
	//indexOf (int,int)
	Ok=Ok && 5==myString1.indexOf(115,4);
	Ok=Ok && -1==myString1.indexOf(130,4);
	return Ok;	
	}
		
	public boolean test() {
	boolean Ok=true;
	Ok= testStringBuffer() && testString1() && testString2()&& testCharacter();
	return Ok;
	}}