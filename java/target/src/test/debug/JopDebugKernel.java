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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * JopDebugKernel.java
 * 
 * This class is responsible to provide all debug services needed
 * inside the JOP machine.
 * 
 * @author Paulo Abadie Guedes
 *
 * 03/06/2007 - 12:00:48
 * 
 */
public class JopDebugKernel
{
  private static DataInputStream inputStream;
  private static DataOutputStream outputStream;
  
  private static int mainMethodFramePointer = 0;
//  private static final int STACK_BASE_POINTER = RtThreadImpl.MAX_STACK + 5;
//  private static final int STACK_BASE_POINTER = RtThreadImpl.MAX_STACK + 6;
//  private static final int STACK_BASE_POINTER = Const.STACK_SIZE + 6;
  private static final int STACK_BASE_POINTER = Const.STACK_OFF + 6;
  
  
  private static final int NOP_INSTRUCTION = 0x00;
  private static final int BREAKPOINT_INSTRUCTION = 0x00CA;
  
  private static boolean initialized = false;
  
  private static int breakpointMethodPointer = 0;
  
//  the code below does not work. Why?
//  static
//  {
//    inputStream = new DataInputStream(System.in);
//    outputStream = System.out;
//  }
  
  /**
   * This is the main method for this class. It is resposible
   * to answer debug requests from the desktop and can be used
   * to interactively manipulate the stack.
   * 
   * The services implemented here does NOT correspond exactly
   * to those designed by the JDWP specification. However, they are
   * at least the basic blocks with which those services can be 
   * fully implemented on the server side.
   * 
   * Currently it is possible to:
   * 
   * 1) Exit the method
   * 2) Invoke a static method given its address and one parameter
   * 3) Calculate the stack depth of the caller method
   * 4) Get a local variable value
   * 5) Set a local variable value
   * 
   * Todo:
   * 6) Request a stack frame
   * 
   * @throws IOException 
   * 
   */
  public static void breakpoint()
  {
    if(initialized == false)
    {
      initialize();
    }
    int commandset, command;
    
    System.out.print("Debug server. Current stack depth: ");
    System.out.println(getStackDepth());
    
    commandset = 0;
    while(commandset >= 0)
    {
      TestJopDebugKernel.printLine();
      try
      {
        commandset = inputStream.read();
        command = inputStream.read();
        
        System.out.print("CommandSet:");
        System.out.print(commandset);
        System.out.print("  Command:");
        System.out.println(command);
        
        // exit from this method.
        if((commandset == 1) && (command == 10))
        {
          // acknowledge the command and exit.
          outputStream.writeInt(0);
          
          System.out.println(" Received \"Exit (10)\" command.");
          System.out.println(" Shutting down...     ");
          
          System.exit(0);
          break;
        }
        
//        // instead of returning the value of one static field, now is used to
//        // read the value of one position of memory.
//        if((commandset == 2) && (command == 6))
//        {
////          under development - should not compile now.
////          
//          int value = 0;
//          
//          // get start address 
//          int address = inputStream.readInt();
//          
//          // get number of words to read
//          int size = inputStream.readInt();
//          
//          while(size > 0)
//          {
//            value = Native.rdMem(address);
//            outputStream.writeInt(value);
//            size --;
//            address ++;
//          }
//          
//          // get field index. May be the start address of a reference or not.
////          int fieldIndex = inputStream.read();
////          
////          // the number of bytes to read
////          int fieldSize = inputStream.read();
////          
////          int addr = Native.rdMem(cp+idx);
////          stack[++sp] = Native.rdMem(addr);
////
////          
//////          // get field size
//////          int fieldSize = inputStream.read();
//////          
//////          if(fieldSize == 1)
//////          {
//////            int 
//////          }
//
//          continue;
//        }
        
        // invoke a static method with one integer parameter on the stack 
        if((commandset == 3) && (command == 3))
        {
          int methodId = inputStream.readInt();
//          int numArguments = inputStream.readInt();
          int argument = inputStream.readInt();
          
          System.out.print(" Method ID: " );
          System.out.print(methodId);
          System.out.print(" Argument: " );
          System.out.println(argument);
          
          if(breakpointMethodPointer == methodId)
          {
            System.out.println("Invalid method pointer!");
            System.out.println("Cannot call breakpoint from itself.");
          }
          else
          {
            int sp = Native.getSP();
            System.out.print("SP = ");
            System.out.println(sp);
//          sp++;
//          Native.wrIntMem(argument, sp);
//        Native.setSP(sp);
//        test just to see if it breaks. It doesn't if SP is restored later.
//          Native.setSP(sp + 4);
            
            System.out.println("Calling method now:");
            Native.invoke(argument, methodId);
            
            // now restore SP to its previous value. Doing this without
            // getting the top value just ignores anything left on the stack.
//          sp = sp - 1;
            Native.setSP(sp);
            
//          System.out.println("Right after return.");
//          sp = Native.getSP();
//          System.out.print("SP = ");
//          System.out.println(sp);
          }
          
          // return the argument just to sync with the caller and inform that
          // execution has finished.
          outputStream.writeInt(argument);
          continue;
        }
        
        // resume execution. Finish this method and continue.
        if((commandset == 11) && (command == 3))
        {
          // acknowledge the command and resume execution.
          outputStream.writeInt(0);
          
          System.out.println(" Received \"Resume (11, 3)\" command.");
          
          //TODO: now how can I run the bytecode that was standing where this
          // breakpoint is, now?
          
          // stop the loop
          break;
        }

        // return a list of all stack frame locations
        if((commandset == 11) && (command == 6))
        {
          System.out.println(" Will read startFrame");
          
          int startFrame = inputStream.readInt();
          System.out.print("  startFrame: ");
          System.out.println(startFrame);
          
          // get current stack depth (this frame)
          int count = getStackDepth();
          int framePointer = getCurrentFramePointer();
          
//          if(startFrame > -1)
//          {
//            for(int i = 0; i < (count - startFrame); i++)
//            {
//              pointer = getNextFramePointer(pointer);
//              count --;
//            }
//          }
//          count = 1;
          System.out.print(" Stack depth:");
          System.out.println(count);
          
          outputStream.writeInt(count);
          
          boolean shouldContinue = true;
          while(shouldContinue)
          {
            int programCounter = getPCFromPreviousMethodCall(framePointer);
            
//            System.out.print("  Program counter: ");
//            System.out.println(programCounter);
            outputStream.writeInt(programCounter);
            int methodPointer = getMPFromPreviousMethodCall(framePointer);
            
//            System.out.print("  Method pointer: ");
//            System.out.println(methodPointer);
            outputStream.writeInt(methodPointer);
            
            framePointer = getNextFramePointer(framePointer);
            
//            System.out.print("  Frame Pointer: ");
//            System.out.println(framePointer);
            outputStream.writeInt(framePointer);
            
//            System.out.println(" Sent frame.");
//            System.out.println();
            
            if(isFirstFrame(framePointer))
            {
              shouldContinue = false;
            }
          }
          
          System.out.println("Done! ");
          
          continue;
        }
        
        // calculate the stack depth of the caller method.
        if((commandset == 11) && (command == 7))
        {
          // get current stack depth (this frame)
          int count = getStackDepth();
          // remove one to return the caller's depth
          count --;
          System.out.print(" Stack depth of caller method: " );
          System.out.println(count);
          
          outputStream.writeInt(count);
          continue;
        }
        
        // get the method pointer from a stack frame
        if((commandset == 16) && (command == 0))
        {
          int frameIndex = inputStream.readInt();
          
          System.out.print(" Frame index: " );
          System.out.print(frameIndex);
          
          int count = getStackDepth();
          int pointer = getCurrentFramePointer();
          for(int i = 0; i < (count - (frameIndex + 1)); i++)
          {
            pointer = getNextFramePointer(pointer);
          }
          pointer = getMPFromPreviousMethodCall(pointer);
          
          System.out.print("  Method pointer: ");
          System.out.println(pointer);
          
          outputStream.writeInt(pointer);
          continue;
        }
        
        // get a local variable value
        if((commandset == 16) && (command == 1))
        {
          int frameIndex = inputStream.readInt();
          int fieldIndex = inputStream.readInt();
          
          System.out.print(" Frame index: " );
          System.out.print(frameIndex);
          System.out.print(" Variable index: " );
          System.out.println(fieldIndex);
          
          
          int count = getStackDepth();
          int pointer = getCurrentFramePointer();
          for(int i = 0; i < (count - frameIndex); i++)
          {
            pointer = getNextFramePointer(pointer);
          }
          count = getLocalVariable(pointer, fieldIndex);
          
          System.out.print("  Value: ");
          System.out.println(count);
          System.out.print(" Pointer: ");
          System.out.println(pointer);
          
          outputStream.writeInt(count);
          continue;
        }
        // set a local variable value
        if((commandset == 16) && (command == 2))
        {
          int frameIndex = inputStream.readInt();
          int fieldIndex = inputStream.readInt();
          int value = inputStream.readInt();
          
          System.out.print(" Frame index: " );
          System.out.print(frameIndex);
          System.out.print(" Variable index: " );
          System.out.println(fieldIndex);
          
          
          int count = getStackDepth();
          int pointer = getCurrentFramePointer();
          for(int i = 0; i < (count - frameIndex); i++)
          {
            pointer = getNextFramePointer(pointer);
          }
          setLocalVariable(pointer, fieldIndex, value);
          
          System.out.print("  Value: ");
          System.out.println(value);
          System.out.print(" Pointer: ");
          System.out.println(pointer);
          
//          writeInt(4);
          outputStream.writeInt(value);
          continue;
        }
        System.out.println("Received invalid command or command set! ");
        System.out.print("Command: ");
        System.out.print(command);
        System.out.print(" Command set: ");
        System.out.print(commandset);
        System.out.println();
      }
      catch(IOException exception)
      {
        System.out.println("Failure: " + exception.getMessage());
        exception.printStackTrace();
        break;
      }
    }
  }
  
  /**
   * Set the default debug streams.
   * 
   * // WARNING! this method should not be called outside breakpoint();
   * method.  
   */
  private static void initialize()
  {
    breakpointMethodPointer = 
      getMPFromPreviousMethodCall(getCurrentFramePointer());
    
    EmbeddedOutputStream embeddedStream = new EmbeddedOutputStream(System.out);
    setDebugStreams(System.in, embeddedStream);
  }
  
  /**
   * This method can be used in the future to set the streams which will be
   * used to communicate with the JOP machine.
   * 
   * @param in
   * @param out
   */
  private static void setDebugStreams(InputStream in, OutputStream out)
  {
    inputStream = new DataInputStream(in);
    outputStream = new DataOutputStream(out);
    
    initialized = true;
  }
  
  public static final int getMPFromPreviousMethodCall(int frame)
  {
//    System.out.println("getMPFromPreviousMethodCall(int frame)");
    return Native.rdIntMem(frame + 4);
  }

  public static final int getCPLocalsArgsFromMP(int mp)
  {
    return Native.rdMem(mp + 1); // cp, locals, args
  }

  public static final int getArgCountFromVal(int val)
  {
    return val & 0x1f;
  }

  public static final int getLocalsCountFromVal(int val)
  {
    return ((val >>> 5) & 0x1f);
  }

  public static final int getCPFromMP(int mp)
  {
    int value = getCPLocalsArgsFromMP(mp);
    value = value >>> 10;
    return value; // cp
  }

  public static int getCPFromFrame(int frame)
  {
    return Native.rdIntMem(frame + 3);
  }

  public static int getVPFromFrame(int frame)
  {
    return Native.rdIntMem(frame + 2);
  }

  public static int getSPFromFrame(int frame)
  {
    return Native.rdIntMem(frame);
  }

  /**
   * Return the frame pointer on the previous stack frame,
   * based on the given frame pointer.
   * 
   * @param framePointer the pointer to the frame under inspection
   * @return
   */  
  public static final int getNextFramePointer(int framePointer)
  {
    int vp, args, loc;
    int mp, val;
    
//    System.out.print("getNextFramePointer(int frame)");
//    System.out.println(framePointer);
    if(isFirstFrame(framePointer))
    {
      System.err.println(" Error! called getNextFramePointer with first pointer.");
      return 0;
    }
    else
    {
      mp = getMPFromPreviousMethodCall(framePointer);
      
      val= getCPLocalsArgsFromMP(mp);
      args = getArgCountFromVal(val);
      loc = getLocalsCountFromVal(val);
      
      vp = getVPFromFrame(framePointer);
      
      return vp + args + loc;
    }
  }

  /**
   * Calculate the number of local variables based on the 
   * frame pointer to a call stack frame.
   * Does not consider the "this" reference or parameters:
   * it just count the number of locals as declared on the source. 
   * 
   * @param frame
   * @return
   */
  public static final int getNumLocalsFromFrame(int frame)
  {
    int loc;
    
    int localsPointer = getLocalsPointerFromFrame(frame);
    loc = frame - localsPointer;
    
//    int mp, val;
//    mp = getMPFromFrame(frame);
//    System.out.println("mp = getMPFromFrame(frame); : " + mp);
//    
//    val= getCPLocalsArgsFromMP(mp);
//    System.out.println("val= getCPLocalsArgsFromMP(mp); : " + val);
//    
//    loc = getLocalsCountFromVal(val);
//    System.out.println("loc = getLocalsCountFromVal(val); : " + loc);
    
    return loc;
  }

  /**
   * Calculate the pointer to local variables based on the frame
   * pointer and the stack pointer of the previous frame.
   * 
   * This assume that the previous stack pointer will always be
   * pointing to one position before the local variables 
   * of the given frame. 
   * 
   * @param frame
   * @return
   */
  public static int getLocalsPointerFromFrame(int frame)
  {
    int previous_sp = getSPFromFrame(frame);
    int localsPointer = previous_sp + 1;
    return localsPointer;
  }

  public static final int getLocalVariable(int frame, int fieldIndex)
  {
    int numLoc;
    int vp;
    int value = 0;
    
    numLoc = getNumLocalsFromFrame(frame);
//    System.out.println("  getField(int frame, int fieldIndex) frame = " + frame);
//    System.out.println("Num. Locals: " + numLoc);
    if(fieldIndex < numLoc && fieldIndex >= 0)
    {
//      vp = getVPFromFrame(frame);
      vp = getLocalsPointerFromFrame(frame);
//      System.out.println("  Calculating VP: " + vp);
      
//      value = Native.rdMem(vp + fieldIndex);
      value = Native.rdIntMem(vp + fieldIndex);
//      System.out.println("  Value read from stack frame is: " + value);
      
//      value ++;
//      Native.wrMem(value, vp + fieldIndex);
    }
    else
    {
      System.out.println("  Invalid index: " + fieldIndex);
      System.out.println("  Num. locals is: " + numLoc);
    }
    return value;
  }

  /**
   * 
   * @param frame
   * @param fieldIndex
   * @param value
   */
  public static final void setLocalVariable(int frame, int fieldIndex, int value)
  {
    int numLoc;
    int vp;
    
    // get MP
    // get method structure
    // get number of local variables
    // check if the index is valid for this method
    //  if so:
    //    get previous CP
    //    calculate variable location (location = CP + index)
    //    get variable at location
    //    print
    //    increment, set

    
    numLoc = getNumLocalsFromFrame(frame);
//    System.out.println("  setField(int frame, int fieldIndex, value) frame = " + frame);
//    System.out.println("Num. Locals: " + numLoc);
    if(fieldIndex < numLoc && fieldIndex >= 0)
    {
//      vp = getVPFromFrame(frame);
      vp = getLocalsPointerFromFrame(frame);
//      System.out.println("  Calculating VP: " + vp);
      
//      value = Native.rdMem(vp + fieldIndex);
      Native.wrIntMem(value, vp + fieldIndex);
//      System.out.println("  Value written to stack frame is: " + value);
    }
    else
    {
      System.out.println("  Invalid index: " + fieldIndex);
      System.out.println("  Num. locals is: " + numLoc);
    }
  }

  /**
   * Return the frame pointer of the caller's method.
   * 
   * @return
   */
  public static final int getCurrentFramePointer()
  {
    int frame;
    frame = Native.getSP() - 4;
    return getNextFramePointer(frame);
  }

  public static final int getCurrentVP()
  {
    int frame = getCurrentFramePointer();
    frame = getNextFramePointer(frame);
    return getVPFromFrame(frame);
  }
  
  public static int getPCFromPreviousMethodCall(int framePointer)
  {
//    System.out.println("getPCFromPreviousMethodCall(int framePointer)");
//    return framePointer + 1;
    return Native.rdIntMem(framePointer + 1);
  }
  
//  public static int getSPFromFrameStart(int frame)
//  {
//    return frame;
//  }
//
//  public static int getMethodPointrFromStackPointer(int sp)
//  {
//    int mp = Native.rdIntMem(sp);
//    return mp;
//  }

  public static int getFramePointerOfCallerMethod()
  {
    int frame;
    
    // get pointer of this method and previous ones
    frame = getCurrentFramePointer();
    frame = getNextFramePointer(frame);
    frame = getNextFramePointer(frame);
    
    return frame;
  }
  
  /**
   * Check if the given frame is the first one on the call stack.
   * 
   * Ignore some internal structures used to call the "main"
   * method. Consider the call stack frame for "main"
   * as the first one in the stack.  
   */
  public static boolean isFirstFrame(int framePointer)
  {
    // assume it's not the first and try to show otherwise
    boolean result = false;
    
//    System.out.println("isFirstFrame()");
    if(mainMethodFramePointer == 0)
    {
      //called only once to calculate the main method pointer.
      mainMethodFramePointer = calculateMainMethodFramePointer();
      
//      System.out.print("main method calculated: ");
//      System.out.println(mainMethodFramePointer);
    }
    
    if(framePointer <= mainMethodFramePointer)
    {
      // this happens only on the "main" call
      result = true;
    }

//    if(framePointer > (RtThreadImpl.MAX_STACK + 5))
//    {
//      framePointer = getNextFramePointer(framePointer);
//      if(framePointer <= (RtThreadImpl.MAX_STACK + 5))
//      {
//        // this happens only on the "main" call
//        result = true;
//      }
//    }
    
    return result;
  }

  /**
   * Method to calculate the stack frame pointer of the 'main' method.
   * 
   * @return
   */
  private static int calculateMainMethodFramePointer()
  {
    // the pointer to "main" is at position (6 + numLocals). Anything greater
    // than this is not pointing to the first frame.
    
    // the first 5 bytes are the stack frame for the "boot" call to main.
    // The next bytes are the argument plus local variables.
    // Then comes the location of the first frame pointer.
    
    int var = Native.rdMem(1);      // pointer to 'special' pointers
    System.out.println("Pointer to 'special' pointers:" + var);
    
    var = Native.rdMem(var+3);  // pointer to main method struct
    System.out.println("Pointer to main method struct:" + var);
    
    
    var= getCPLocalsArgsFromMP(var); // get val from 'main' method structure
    
    System.out.println("Main arguments:" + getArgCountFromVal(var));
    System.out.println("Main locals:" + getLocalsCountFromVal(var));
    
    // get the number of local variables in the stack frame
    var = getArgCountFromVal(var) + getLocalsCountFromVal(var);
    
    System.out.println("Stack base pointer: " + STACK_BASE_POINTER);
    
    // return the main method pointer using the stack base pointer as reference
    return (STACK_BASE_POINTER + var);
  }

  /**
   * Calculate how many frames are before the given one.
   * The depth of the first frame (for the main method) is zero.
   * Methods called inside "main" will have depth = 1
   * and so on.
   * 
   * @param framePointer
   * @return
   */
  public static final int getStackDepth(int framePointer)
  {
    int count = 0;
    while(isFirstFrame(framePointer) == false)
    {
      count++;
      framePointer = getNextFramePointer(framePointer);
    }
    
    return count;
  }
  
  /**
   * Get the framePointer at the given index.
   * The depth of the first frame (for the main method) is zero.
   * Methods called inside "main" will have depth = 1
   * and so on.
   * 
   * @param framePointer
   * @return
   */
//  public static final int getFramePointerAt(int framePointer, int index)
//  {
//    int count = getStackDepth();
//    while(isFirstFrame(framePointer) == false && (index < count))
//    {
//      count--;
//      framePointer = getNextFramePointer(framePointer);
//    }
//    
//    return framePointer;
//  }
  
  /**
   * Return the current stack depth.
   * 
   * @return
   */
  public static final int getStackDepth()
  {
    int framePointer = getFramePointerOfCallerMethod();
    return getStackDepth(framePointer);
  }

  public static int getInstanceSize(int object)
  {
    int classReference = getClassReference(object);
    return Native.rdMem(classReference);
  }

  public static int getClassReference(int object)
  {
    int classreference = 0;
    
    // return one byte and read the pointer to the virtual method table
    object --;
    classreference = Native.rdMem(object);
    
    // adjust to point to the "instance size" field
    classreference--;
    classreference--;
    
    return classreference;
  }

  public static int getConstantPoolFromClassReference(int classReference)
  {
    // get reference to the first method. There will always be some,
    // due to generated/synthetic methods such as the default constructur
    int methodReference = Native.rdMem(classReference + 2);
    int constantPoolReference = getCPFromMP(methodReference);
    
    return constantPoolReference;
  }
  
  /**
   * Handle a "set breakpoint" command.
   * 
   * @return
   */
//  public static final boolean handleSetBreakPointCommand()
//  {
//    
//  }
  
  /**
   * Set a breakpoint instruction.
   * Still need to test it. 
   * 
   * @param methodStructPointer pointer to the method structure
   * @param instruction
   * @return
   */
  public static final boolean setBreakPoint(int methodStructPointer, int instructionOffset)
  {
    int methodSize;
    int startAddress;
    int instruction;
    int instructionAddress;
    boolean result = false;
    
    System.out.println("setBreakPoint(int methodPointer, int instructionOffset)");
    
    startAddress = getMethodStartAddress(methodStructPointer);
    methodSize = getMethodSize(methodStructPointer);
    
    // check if the address is correct
    if(instructionOffset >= 0 && instructionOffset < methodSize)
    {
      instructionAddress = startAddress + instructionOffset;
      instruction = Native.rdMem(instructionAddress);
      
      // the instruction to be overwritten SHOULD be NOP in this implementation
      if(instruction == NOP_INSTRUCTION)
      {
        instruction = BREAKPOINT_INSTRUCTION;
        Native.wrMem(instruction, instructionAddress);
        
        System.out.println("Wrote breakpoint!");
        result = true;
      }
      else
      {
        System.out.print("Wrong instruction: NOP expected, but found this: ");
        System.out.println(instruction);
      }
    }
    else
    {
      System.out.print("Wrong instruction offset: ");
      System.out.println(instructionOffset);
      
      System.out.print("Method size:");
      System.out.println(methodSize);
      
      System.out.println();
    }
    
    return result;
  }
  
  /**
   * Return the method start address.
   * 
   * @param methodPointer
   * @return
   */
  public static int getMethodStartAddress(int methodPointer)
  {
    int startAddress;
    
    startAddress = Native.rdMem(methodPointer);
    
    // shift the variable 10 bits to the right (unsigned). This is the 
    // method start address.
    startAddress = startAddress >>> 10;
    
    return startAddress;
  }
  
  /**
   * Return the method size.
   * 
   * @param methodPointer
   * @return
   */
  public static int getMethodSize(int methodPointer)
  {
    int startAddress;
    int methodSize;
    
    startAddress = Native.rdMem(methodPointer);
    
    // get the last 10 bits: the method length. Hence, size can be up to 1kb.
    methodSize = startAddress & 0x000003ff;
    
    return methodSize;
  }
  
  public static int getMethodConstantPool(int methodPointer)
  {
    int data;
    
    methodPointer++;
    data = Native.rdMem(methodPointer);
    
    // shift the variable 10 bits to the right (unsigned). This is the 
    // constant pool address.
    data = data >>> 10;
    
    return data;
  }
  
  public static int getMethodArgCount(int methodPointer)
  {
    int data;
    
    methodPointer++;
    data = Native.rdMem(methodPointer);
    
    // get the last 5 bits. This is the arg count.
    data = data & 0x0000001f;
    
    return data;
  }
  
  public static int getMethodLocalsCount(int methodPointer)
  {
    int data;
    
    methodPointer++;
    data = Native.rdMem(methodPointer);
    
    // shift the variable 5 bits to the right (unsigned). Cut the rest.
    // This is the locals count.
    data = data >>> 5;
    data = data & 0x0000001f;
    
    return data;
  }
  
  public static void dumpMethodStruct(int methodPointer)
  {
    int data;
    
    System.out.print("Method structure:  ");
    
    data = Native.rdMem(methodPointer);
    EmbeddedOutputStream.printIntHex(data);
    
    data = Native.rdMem(methodPointer + 1);
    EmbeddedOutputStream.printIntHex(data);
    
    System.out.println();
    
    System.out.println();
    
    System.out.print("Start address: ");
    System.out.println(getMethodStartAddress(methodPointer));
    
    System.out.print("  Method size: ");
    System.out.println(getMethodSize(methodPointer));
    
    System.out.print("Constant pool: ");
    System.out.println(getMethodConstantPool(methodPointer));
    
    System.out.print("Local count:   ");
    System.out.println(getMethodLocalsCount(methodPointer));
    
    System.out.print("Arg count:     ");
    System.out.println(getMethodArgCount(methodPointer));
    
    System.out.println();
  }
  
  public static final int getCurrentMethodPointer()
  {
    int data;
    
    // get the current frame pointer
    data = getCurrentFramePointer();
    
    // get the method pointer from the previous method directly from the stack
    data = getMPFromPreviousMethodCall(data);
    
//    System.out.println("getCurrentMethodPointer()");
    
    return data;
  }
  
  public static void dumpMethodBody(int methodPointer)
  {
    int index, start, size, data;
    
    start = getMethodStartAddress(methodPointer);
    size = getMethodSize(methodPointer);
    
    System.out.println("Method body:");
    System.out.println();
    
    for(index = 0; index < size; index++)
    {
      data = Native.rdMem(start + index);
      EmbeddedOutputStream.printIntHex(data);
      System.out.print(" ");
//      if((index & 0x07) == 0 && (index > 0))
      if(((index + 1)% 0x08) == 0)
      {
//        System.out.print("Index:");
//        System.out.println(index);
        System.out.println();
      }
    }
    
    System.out.println();
    System.out.println();
  }
}
