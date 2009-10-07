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
//Short, Integer, Long


public class PrimitiveClasses2 extends TestCase {

	public String toString() {
		return "PrimitiveClasses2";
	}
	
	public boolean testShort(){
		boolean Ok=true;
		String myString1, myString2, myString3;
		
		//Short
		Short myShort1=new Short((short)100);
		Short myShort2=new Short("25");
		Short myShort3=new Short("32767");
		//check exception
		//Short myShort4=new Short("32768");
		
		//test constructors
		Ok=Ok && (short)100==myShort1.shortValue();
		Ok=Ok && (short)25==myShort2.shortValue();
		Ok=Ok && (short)32767==myShort3.shortValue();
		myShort3=new Short("-32768");
		Ok=Ok && (short)-32768==myShort3.shortValue();
		//test max, min
		Ok=Ok && Short.MAX_VALUE==(short)32767;
		Ok=Ok && Short.MIN_VALUE==(short)-32768;
			
		//equals()
		myShort3=new Short("100");
		Ok=Ok &&  !myShort1.equals(myShort2);
		Ok=Ok &&  myShort1.equals(myShort1);
		Ok=Ok &&  myShort1.equals(myShort3);
		//hashCode()
		Ok=Ok && myShort1.hashCode()==myShort1.hashCode();
		Ok=Ok && myShort1.hashCode()!=myShort2.hashCode();
		Ok=Ok && myShort1.hashCode()==myShort3.hashCode();
		
		//decode(String)--not found
		/*Ok= Ok && (short)44==Short.decode("44");
		Ok= Ok && (short)44==Short.decode("44");
		*/
		
		//toString()/toString(Long)
		myString1=new String("32767");
		myString2=new String("-32768");
		myShort3=new Short("32767");
		Ok= Ok && myString1.equals(myShort3.toString());
		
		//parseShort()
		Ok=Ok && (short)32767==Short.parseShort("32767");
		Ok=Ok && (short)-32768==Short.parseShort("-32768");
		//check exception		
		//Ok=Ok && (short)-32768==Short.parseShort("-32f69");
		
		return Ok;
	}
	
	public boolean testLong(){
		String myString1, myString2, myString3;
		boolean Ok=true;
		//Long
	Long myLong1=new Long(100l);
	Long myLong2=new Long(666l);
	Long myLong3=new Long(Long.MAX_VALUE);
	
	//equals()
	Ok=Ok &&  !myLong1.equals(myLong2);
	Ok=Ok &&  myLong1.equals(myLong1);
	Ok=Ok &&  !myLong1.equals(myLong3);
	
	//hashCode()
	Ok=Ok && myLong1.hashCode()==myLong1.hashCode();
	Ok=Ok && myLong1.hashCode()!=myLong2.hashCode();
	Ok=Ok && myLong1.hashCode()!=myLong3.hashCode();
	
	//parseLong()
	Ok= Ok&& 9223372036854775807l==Long.parseLong("9223372036854775807");
	Ok= Ok&& -9223372036854775808l==Long.parseLong("-9223372036854775808");
	
	Ok= Ok&& 9223372036854775807l==Long.parseLong("9223372036854775807",10);
	Ok= Ok&& -9223372036854775808l==Long.parseLong("-9223372036854775808",10);
	
	/* works only for radix 10
	Ok= Ok && 0x7FFFFFFFFFFFFFFFl==Long.parseLong("9223372036854775807",16);
	Ok= Ok && 0x1000000000000000l==Long.parseLong("-9223372036854775808",16);
	*/
	
	//toString()/toString(Long)
	myString1=new String("9223372036854775807");
	myString2=new String("-9223372036854775808");
	Ok= Ok && myString1.equals(myLong3.toString());
	Ok= Ok && (Long.toString(9223372036854775807l)).equals(myString1);
	Ok= Ok && (Long.toString(-9223372036854775808l)).equals(myString2);
	return Ok;
	}
	
	public boolean testInteger(){
		boolean Ok=true;
		String myString1, myString2, myString3;
		
	//Integer
	Integer myInteger1=new Integer(100);
	Integer myInteger2=new Integer(100);
	Integer myInteger3=new Integer(10000);
	
	//Constructor Integer(String) not found
	//Integer myInteger4=new Integer("10000");
	
	//byteValue()
	Ok=Ok && (byte)100==myInteger1.byteValue();
	Ok=Ok && (byte)10000==myInteger3.byteValue();
	
	//intValue
	Ok=Ok && 100==myInteger1.intValue();
	Ok=Ok && 10000==myInteger3.intValue();
		
	//longValue
	Ok=Ok && 100l==myInteger1.intValue();
	Ok=Ok && 10000l==myInteger3.intValue();
	
	//public static int parseInt(String str, int radix)
	Ok= Ok&& 2147483647==Integer.parseInt("2147483647",10);
	Ok= Ok&& -2147483648==Integer.parseInt("-2147483648",10);
	//check exception
	//Ok= Ok&& 2147483647==Integer.parseInt("7fffffff",10);

		
	//public static int parseInt(String str)
	Ok= Ok && 2147483647==Integer.parseInt("2147483647");
	Ok= Ok && -2147483648==Integer.parseInt("-2147483648");
	//check exception
	//Ok= Ok && -2147483648==Integer.parseInt("-2147483649");
	
	//shortValue()
	Ok=Ok && (short)100==myInteger1.shortValue();
	Ok=Ok && (short)10000==myInteger3.shortValue();
	
	//toBinaryString(int)
	myString1=new String("1010");
	myString2=new String("10000000000000000000000000000000");
	myString3=new String("1111111111111111111111111111111");
	Ok=Ok && myString1.equals(Integer.toBinaryString(10));
	Ok=Ok && myString2.equals(Integer.toBinaryString(-2147483648));
	Ok=Ok && myString3.equals(Integer.toBinaryString(2147483647));
	
	//String toHexString(int)
	myString1=new String("a");
	myString2=new String("80000000");
	myString3=new String("7fffffff");
	Ok=Ok && myString1.equals(Integer.toHexString(10));
	Ok=Ok && myString2.equals(Integer.toHexString(-2147483648));
	Ok=Ok && myString3.equals(Integer.toHexString(2147483647));
		
	//String toOctalString(int)
	myString1=new String("12");
	myString2=new String("20000000000");
	myString3=new String("17777777777");
	Ok=Ok && myString1.equals(Integer.toOctalString(10));
	Ok=Ok && myString2.equals(Integer.toOctalString(-2147483648));
	Ok=Ok && myString3.equals(Integer.toOctalString(2147483647));
	
	
	//public String toString() 
	myString1=new String("10");
	myString2=new String("100");
	Ok= Ok && myString2.equals(myInteger1.toString());
	Ok= Ok && (Integer.toString(10)).equals(myString1);
	
	//static String toString(int)
	myString1=new String("10");
	Ok= Ok && (Integer.toString(10)).equals(myString1);
	
	//static String toString(int, int)
	myString1=new String("10");
	Ok= Ok && (Integer.toString(10,10)).equals(myString1);
	myString2=new String("10");
	Ok= Ok && (Integer.toString(2,2)).equals(myString1);
	
	//public static Integer valueOf(String s)
	//equals()
	Ok=Ok &&  myInteger1.equals(myInteger2);
	Ok=Ok &&  myInteger1.equals(myInteger1);
	Ok=Ok &&  !myInteger1.equals(myInteger3);
	
	//hashCode()
	Ok=Ok && myInteger1.hashCode()==myInteger2.hashCode();
	Ok=Ok && myInteger1.hashCode()!=myInteger3.hashCode();
	Ok=Ok && myInteger2.hashCode()!=myInteger3.hashCode();
	return Ok;
	
	}
	
	public boolean test() {
	boolean Ok=true;
	Ok=testInteger() && testShort() && testLong();
	return Ok;
	
	}
}