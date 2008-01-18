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

import java.io.IOException;

import com.jopdesign.sys.Native;

import debug.io.EmbeddedOutputStream;

public class TestJopDebugKernel
{
  public static void test1()
  {
    System.out.println("Hello JOP world!!!");
    System.out.println("Testing make now.");
    
    Exception e = new Exception("Message:)");
    System.out.println("Stack trace: " + e.getMessage());
    e.printStackTrace();
  }
  
  public static void printValue(int value)
  {
    printLine();
    printCurrentFramePointer("printValue(int value)");
    System.out.print(" Value is: ");
    System.out.println(value);
  }

  public static void test2_read_write()
  {
    int sp, x;
    
//    int x1, x2, x3, x4,x5,x6;
//    x1 = 1;
//    x2 = 2;
//    x3 = 3;
//    x4 = 4444;
//    x5 = 5;
//    x6 = 6;
//    dummyCall(x1+x2+x3+x4+x5+x6);
    
    printLine();
    System.out.println("Hello JOP world!!!");
    System.out.println("  2) Testing read/write now.");
    
    sp = Native.getSP();
//    System.out.println("Current SP for test2_read_write(): " + sp);
    
    printCurrentFramePointer("test2_read_write()");
    
    x = 16;
    printValue(x);
    
    readPrintAndIncrement(1);
    if(x == 17)
    {
      System.out.println("Worked as expected.");
    }
    else
    {
      System.out.println("Failure: wrong value!!! Shoulb be 17.");
    }
    
    printValue(x);
    
    int framePointer = JopDebugKernel.getCurrentFramePointer();
    printStackDepth("test2_read_write ", framePointer);
    
    sp = sp + x;
    System.out.println(" Making the two variables live until here (x+sp): " + sp);
  }

  /**
   * 
   */
  static void printLine()
  {
    System.out.println("----------------------------------------");
  }
  
  /**
   * Get the variable at index "index" on the stack frame of the 
   * method calling this method. Read, print and increment it. 
   * 
   * @param index
   */
  public static void readPrintAndIncrement(int index)
  {
    int x1, x2, x3, x4,x5,x6;
    x1 = 1;
    x2 = 2;
    x3 = 3;
    x4 = 5555;
    x5 = 5;
    x6 = 6;
//    x1 = x1 + x2 + x3 + x4 + x5 + x6;
    dummyCall(x1+x2+x3+x4+x5+x6);

    int sp;
    int callersFP;
    int frame;
    int value; 
    
    printLine();
//    sp = Native.getSP();
//    System.out.println("Current SP for readPrintAndIncrement(): " + sp);
    
    printCurrentFramePointer("readPrintAndIncrement(int index)");
    
    callersFP = JopDebugKernel.getFramePointerOfCallerMethod();
    System.out.println("  getFramePointerOfCallerMethod(): " + callersFP);
    
//    frame = getCurrentFramePointer();
//    frame = getNextFramePointer(frame);
//    System.out.println("  Should be the same as: " + frame);
    System.out.println("  Will get field now...");
    index= 1;
    value = JopDebugKernel.getLocalVariable(callersFP, index);
    System.out.println("  The value of field #" + index + " is :" + value);
    
//    dumpCallStack();
    
    value++;
    JopDebugKernel.setLocalVariable(callersFP, index, value);
  }

  /**
   * 
   */
  private static void printCurrentFramePointer(String message)
  {
//    int x1, x2, x3, x4,x5,x6;
//    x1 = 1;
//    x2 = 2;
//    x3 = 3;
//    x4 = 4;
//    x5 = 5;
//    x6 = 6;
//    x1 = x1 + x2 + x3 + x4 + x5 + x6;
//    dummyCall(x1+x2+x3+x4+x5+x6);
      
    int currentFramePointer = JopDebugKernel.getCurrentFramePointer();
    currentFramePointer = JopDebugKernel.getNextFramePointer(currentFramePointer);
    System.out.print(message);
    System.out.println("  Current frame pointer: " + currentFramePointer);
  }
  
  public static void dumpCallStack()
  {
    int i, value;
    int count = 0;
    
//    for(i = 0; i < 256; i++)
//    for(i = 0; i < 512; i++)
    for(i = 0; i < 256; i++)
    {
      value = Native.rdIntMem(i);
//      value = Native.rdMem(i);
      
      if(value == 0)
      {
        count ++;
      }
      else
      {
        if(count > 0)
        {
          System.out.println(count + " values are zero");
          count = 0;
        }
        
        System.out.println("stack[" + i + "] = " + value);
      }
    }
    
    if(count > 0)
    {
      System.out.println(count + " values are zero");
      count = 0;
    }
    
//    Exception e = new Exception();
//    e.printStackTrace();
    
    //testStack(12);
    // ok, we still have room for 12 empty frames
    
    //testStack(13);
    // but with 13 stack... overflow!
    
    printCurrentFramePointer("dumpCallStack()");
    
//    System.out.println("  Tracing: ");
//    JVMHelp.trace();
  }
  
//  public static void printCallStackInfo()
//  {
//    int currentFrame = getCurrentFramePointer();
//    
//    while(isFirstFrame(currentFramePointer))
//  }
//  

  public static void checkIsFirstFrame(String message, int framePointer, boolean expected)
  {
    boolean result = JopDebugKernel.isFirstFrame(framePointer);
    String auxMessage = message;
    
    // TODO: someone need to check JOP basic classes 
    // compiling the line below with the modified 
    // classpath BREAKS THE javac COMPILER!!!. 
    // It is not a compiler issue since
    // it compiles without problems with the regular Java classpath. 
    // Really strange, need to be checked inside JOP classes.
    // probably something with Object, String or StringBuffer.
//    System.out.println(message + " result: " + result);
//    auxMessage = message + " result: " + result;
    
    auxMessage = message + " result: ";
    System.out.print(auxMessage);
    System.out.println(result);
    
    if(result != expected)
    {
      System.out.println("  NOT AS EXPECTED!!!   FAILURE HERE");
    }
  }
  
  /**
   * A simple method to test how much space is still left on the stack.
   * Compile and call it with different values until the stack overflows.
   * Simple. Raw. Direct. Maybe a hack... but worked;).
   * 
   * @param index
   */
  private static void testStack(int index)
  {
//      int framePointer = getCurrentFramePointer();
//      printStackDepth("testStack(" + index + "): ", framePointer);
    printStackDepth("testStack(" + index + "): ", JopDebugKernel.getCurrentFramePointer());
    
    if(index > 1)
    {
      testStack(index - 1);
    }
  }
  
  private static void dummyCall(int x)
  {
    
  }
  
  public static final void test3_checkFirstFrame()
  {
    int framePointer = JopDebugKernel.getCurrentFramePointer();
    checkIsFirstFrame("test3_checkFirstFrame()  ", framePointer, false);
  }
  
  public static final void traverseStack(int framePointer)
  {
    int count = 0;
    while(JopDebugKernel.isFirstFrame(framePointer) == false)
    {
      System.out.println(" Frame " + count + ": pointer = " + framePointer);
      framePointer = JopDebugKernel.getNextFramePointer(framePointer);
    }
  }
  
  public static final void printStackDepth(String message, int framePointer)
  {
    int depth = JopDebugKernel.getStackDepth(framePointer);
    System.out.println(message + " Stack depth: " + depth + " Frame: " + framePointer);
  }
  
  public static void test4_read_write()
  {
    printLine();
    System.out.println("test4_read_write()");
//    TestObject object;
    int objectRef;
    int frameIndex;
    TestObject object= new TestObject();
    
//    object= new TestObject();
//    object.setValue(16);
    
    frameIndex = JopDebugKernel.getCurrentFramePointer();
    objectRef = JopDebugKernel.getLocalVariable(frameIndex, 0);
    
//    System.out.println(" Value: " + object.getValue());
    
//    readPrintAndIncrementField(objectRef, 0);
//    readPrintAndIncrementField(object, 0);
    
//    System.out.println(" Value: " + object.getValue());
    JopDebugKernel.breakpoint();
  }
  
  public static final void readPrintAndIncrementField(int object, int fieldIndex)
  {
    printLine();
    int num;
    
    // handle needs indirection
    object = Native.rdMem(object);
    num = Native.rdMem(object + fieldIndex);
    System.out.println("  Field value: " + num);
    
    num++;
    Native.wrMem(num, object + fieldIndex);
    
//    num = getClassReference(object);
//    System.out.println("Class reference: " + num);
//    int constantPool = getConstantPoolFromClassReference(classReference);
    
    JopDebugKernel.breakpoint();
  }
  
  /**
   * 
   */
  private static void test5_checkJopSimIO()
  {
    EmbeddedOutputStream stream = new EmbeddedOutputStream(System.out);
    try
    {
      int test = System.in.read();
      
      System.out.println("  Success! read one byte");
      System.out.println("  Echo:");
      System.out.println(test);
      
//      sendPacketPrintByte((byte)test);
      stream.writeInt((byte)test);
      
      test = 0x0f0f0f0f;
      stream.writeInt(test);
    }
    catch (IOException exception)
    {
      exception.printStackTrace();
    }
  }
  
  private static void test6_checkCurrentMethodStructure()
  {
    // the parameters below are not used. They exist just to allow
    // the test6_helpMethod() to check if it's possible to query
    // the number of parameters of a method.
    test6_helpMethod(1,2);
  }
  
  /**
   * This method exists just to be called by 
   * test6_checkCurrentMethodStructure() method, in order to test the
   * methods (from JopDebugKernel) which inspect data inside the 
   * method structure. Wow, that was confusing.
   * 
   * @param x
   * @param y
   */
  private static void test6_helpMethod(int x, int y)
  {
    int methodPointer, methodArgCount, methodLocalsCount;
    
    methodPointer = JopDebugKernel.getCurrentMethodPointer();
    
    printLine();
    System.out.print("Current method pointer: ");
    System.out.println(methodPointer);
    
    methodArgCount = JopDebugKernel.getMethodArgCount(methodPointer);
    if(methodArgCount != 2)
    {
      System.out.print("Failure! should be 2 but found: ");
      System.out.println(methodArgCount);
    }
    
    methodLocalsCount = JopDebugKernel.getMethodLocalsCount(methodPointer);
    if(methodLocalsCount != 3)
    {
      System.out.print("Failure! should be 3 but found: ");
      System.out.println(methodLocalsCount);
    }
    
    JopDebugKernel.dumpMethodStruct(methodPointer);
    JopDebugKernel.dumpMethodBody(methodPointer);
    printLine();
  }
  
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    int x = 10, y = 10, z = 10;
    
    System.out.println("  Starting...");
    
    int sp = Native.getSP();
    System.out.println(" SP of main(String[] args) is: " + sp);
    printCurrentFramePointer("main(String[] args)");
    
//    dumpCallStack();
    
//    int x = 3333;
//    int threadID = Native.
//    System.out.println(" Thread ID of main: " + threadID);

//        test1();
//    test2_read_write();
    
    // get the current frame pointer and 
    // assign "20" to the local field at index 1 (the second one).
    // the first field is at index 0 and ir the "args" array.
//    int framePointer;
//    framePointer = getCurrentFramePointer();
//    setLocal_Variable(framePointer, 1, 20);
//    System.out.println(" after direct assignment to the stack frame, x = " + x);
//    
//    checkIsFirstFrame("main ", framePointer, true);
//    
//    test3_checkFirstFrame();
//    
//    printStackDepth("main ", framePointer);
//    
//    x = getNumLocalsFromFrame(framePointer);
//    System.out.println(" Num.locals in main: " + x);
//    
//    testStack(8);
//    
      //test4_read_write();
//      test5_checkJopSimIO();
    
    test6_checkCurrentMethodStructure();
      
      System.out.print(" LocalVP:");
      System.out.print(JopDebugKernel.getCurrentVP());
      System.out.print(" x = ");
      System.out.println(x);
      
//      JopDebugKernel.breakpoint();
//      EmbeddedOutputStream.testEmbeddedPrinter();
    TestObject testObject = new TestObject();
    System.out.print("TestObject value is: ");
    System.out.println(testObject.getValue0());
    
    test4_read_write();
    
    System.out.println("Will run method: identity");
    x = TestObject.identity(x);
    
    System.out.println("Will run method: increment");
    x = TestObject.increment(x);
    
    System.out.println("Will run method: testInc");
    x = TestObject.testInc();
    
    System.out.println("Will run method: getConstant");
    x = TestObject.getConstant();
  }
}
