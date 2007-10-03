/*******************************************************************************

    An implementation of the Java Debug Wire Protocol (JDWP) for JOP
    Copyright (C) 2007 Paulo Abadie Guedes

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
    
*******************************************************************************/

package debug;

class Test
{

/*
One way to see what the compiler is doing is to add "verbose" to it,
like the command below.

$ javac -verbose -d java/target/dist/classes -sourcepath
java/target/src/common\;java/target/src/jdk_base\;
java/target/src/jdk11\;java/target/src/rtapi\;
java/target/src/test/HelloWorld/source\;
java/target/src/app\;java/target/src/bench
-bootclasspath "" -extdirs "" -classpath "" -source 1.4
java/target/src/test/debug/source/debug/Test.java

Since it compiles fine with the default classpath, it
should not be an issue with Sun's compiler. 
It's some strange "side effect" (a.k.a. "bug")
of changing basic classes such as Object or String.
*/
  public static void checkCompiler(String message, int value)
  {
    boolean result = true;

    String auxMessage = message;

   // this works
    auxMessage = message + " result: ";

    // this is the line where things break
    auxMessage = message + " result: " + result;


//  this also fails
//    auxMessage = " result: " + result;
//    auxMessage = " " + result;

    // but this works fine. Why accessing a boolean variable breaks javac,
    // when EXTERNAL classes are used instead of Sun's classes?
    auxMessage = " " + true;

    // this line is ok
    System.out.println(auxMessage);
  }
  
  public static void testShiftRight()
  {
    byte result; 
    byte data = (byte) 0xff;
    byte expected = 0x0f;
    
    result = (byte) ((data >>> 4) & 0x0f);
    
    if(expected != result)
    {
      System.out.print("Failure: ");
      System.out.print(expected);
      System.out.print(" != ");
      System.out.println(result);
    }
    else
    {
      System.out.println("Passed.");
    }
  }
  
  public static void testShiftRightInt()
  {
    int result; 
    int data = 0xff;
    int expected = 0x0f;
    
    result = (data >>> 4);
    
    if(expected != result)
    {
      System.out.print("Failure: ");
      System.out.print(expected);
      System.out.print(" != ");
      System.out.println(result);
    }
    else
    {
      System.out.println("Passed.");
    }
  }
  
  public static void main(String args[])
  {
    testShiftRightInt();
    testShiftRight();
  }
}
