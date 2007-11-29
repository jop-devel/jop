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
public final class JopDebugKernel
{
  private static DataInputStream inputStream;
  private static DataOutputStream outputStream;
  
  private static int mainMethodFramePointer = 0;
//  private static final int STACK_BASE_POINTER = RtThreadImpl.MAX_STACK + 5;
//  private static final int STACK_BASE_POINTER = RtThreadImpl.MAX_STACK + 6;
//  private static final int STACK_BASE_POINTER = Const.STACK_SIZE + 6;
  private static final int STACK_BASE_POINTER = Const.STACK_OFF + 6;
  
  private static final int MASK_FIRST_BYTE  = 0x00FFFFFF;
  private static final int MASK_SECOND_BYTE = 0xFF00FFFF;
  private static final int MASK_THIRD_BYTE  = 0xFFFF00FF;
  private static final int MASK_FOURTH_BYTE = 0xFFFFFF00;
  
  private static final int INVALID_INSTRUCTION = -1;
  
  private static final int NOP_INSTRUCTION = 0x00;
  private static final int BREAKPOINT_INSTRUCTION = 0x00CA;
  
  // currently the maximum number of local variables is 32.
  private static final int MAX_LOCAL_VARIABLES = 32;
  
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
  public static final void breakpoint()
  {
    if(initialized == false)
    {
      initialize();
    }
    int commandset, command;
    
    System.out.print("Breakpoint! Current stack depth: ");
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
          System.out.println("Exit: stop execution.");
          handleExitCommmand();
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
          System.out.println("Invoke static");
          handleInvokeStaticCommand();
          continue;
        }
        
        // resume execution. Finish this method and continue.
        if((commandset == 11) && (command == 3))
        {
          System.out.println("Resume execution");
          handleResumeExecutionCommand();
          
          // stop the loop
          break;
        }

        // return a list of all stack frame locations
        if((commandset == 11) && (command == 6))
        {
          System.out.println("Get stack frames");
          handleGetStackFramesCommand();
          
          continue;
        }
        
        // calculate the stack depth of the caller method.
        if((commandset == 11) && (command == 7))
        {
          System.out.println("Get stack depth");
          handleGetStackDepthCommand();
          continue;
        }
        
        // dump the call stack. For development ONLY.
        // NOT a standard JDWP command set/command pair.
        if((commandset == 11) && (command == 13))
        {
          System.out.println("Print the call stack.");
          handlePrintCallStackCommand();
          continue;
        }
        
        // dump one stack frame. For development ONLY.
        // NOT a standard JDWP command set/command pair.
        if((commandset == 11) && (command == 14))
        {
          System.out.println("Print a stack frame.");
          handlePrintStackFrameCommand();
          continue;
        }
        
        // ----------------------
        // breakpoint commands
        // ----------------------
        
        // set breakpoint
        if((commandset == 15) && (command == 1))
        {
          System.out.println("set breakpoint");
          handleSetBreakPointCommand();
          continue;
        }
        
        // clear breakpoint
        if((commandset == 15) && (command == 2))
        {
          System.out.println("clear breakpoint");
          handleClearBreakPointCommand();
          continue;
        }
        
        // get the method pointer from a stack frame
        if((commandset == 16) && (command == 0))
        {
          System.out.println("Get method pointer");
          handleGetStackFrameMPCommand();
          
          continue;
        }
        
        // get a local variable value
        if((commandset == 16) && (command == 1))
        {
          System.out.println("Get local variable");
          handleGetLocalVariableCommand();
          
          continue;
        }
        
        // set a local variable value
        if((commandset == 16) && (command == 2))
        {
          System.out.println("Set local variable");
          handleSetLocalVariableCommand();
          
          continue;
        }
        
        // return the nuber of local variables
        if((commandset == 16) && (command == 5))
        {
          System.out.println("Get number of local variables");
          handleGetNumberOfLocalVariablesCommand();
          
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
    
    System.out.println("Returning from \"breakpoint\".");
  }

  /**
   * @throws IOException
   */
  private static void handleGetStackDepthCommand() throws IOException
  {
    // get current stack depth (this frame)
    int count = getStackDepth();
    // remove one to return the caller's depth
    count --;
    System.out.print(" Stack depth of caller method: " );
    System.out.println(count);
    
    outputStream.writeInt(count);
  }
  
  /**
   * @throws IOException
   */
  private static void handlePrintCallStackCommand() throws IOException
  {
    // get current stack depth (this frame)
    int count = getStackDepth();
    
    prettyPrintStack();
    
    // just for development
    //TestJopDebugKernel.dumpCallStack();
    
    outputStream.writeInt(count);
  }
  
  private static void handlePrintStackFrameCommand() throws IOException
  {
    // get stack frame index to be printed (this frame)
    int frameIndex;
    int previousFrameIndex;
    int framePointer,previousFramePointer;
    
//    System.out.println("Starting handlePrintStackFrameCommand");
    
    frameIndex = inputStream.readInt();
    previousFrameIndex = frameIndex + 1;
    
//    System.out.print("Frame index to print: ");
//    System.out.println(frameIndex);
    
    if(previousFrameIndex < getStackDepth())
    {
      previousFramePointer = getFramePointerAtIndex(previousFrameIndex);
      framePointer = getNextFramePointer(previousFramePointer);
      
      prettyPrintStackFrame(framePointer, previousFramePointer);
    }
    else
    {
      System.out.println("Failure: invalid index -> " + frameIndex);
    }
    
    //  Will return the frame index just to sync
    outputStream.writeInt(frameIndex);
  }
  
  /**
   * Handle the "Frames" command.
   * 
   * @throws IOException
   */
  private static void handleGetStackFramesCommand() throws IOException
  {
    System.out.println(" Will read startFrame");
    
    int startFrame = inputStream.readInt();
    System.out.print("  startFrame: ");
    System.out.println(startFrame);
    
    // get current stack depth (this frame)
    int count = getStackDepth();
    int framePointer = getCurrentFramePointer();
    
//    if(startFrame > -1)
//    {
//      for(int i = 0; i < (count - startFrame); i++)
//      {
//        pointer = getNextFramePointer(pointer);
//        count --;
//      }
//    }
//    count = 1;
    System.out.print(" Stack depth:");
    System.out.println(count);
    
    outputStream.writeInt(count);
    
    boolean shouldContinue = true;
    while(shouldContinue)
    {
      int programCounter = getPCFromFrame(framePointer);
      
//      System.out.print("  Program counter: ");
//      System.out.println(programCounter);
      outputStream.writeInt(programCounter);
      int methodPointer = getMPFromFrame(framePointer);
      
//      System.out.print("  Method pointer: ");
//      System.out.println(methodPointer);
      outputStream.writeInt(methodPointer);
      
      framePointer = getNextFramePointer(framePointer);
      
//      System.out.print("  Frame Pointer: ");
//      System.out.println(framePointer);
      outputStream.writeInt(framePointer);
      
//      System.out.println(" Sent frame.");
//      System.out.println();
      
      if(isFirstFrame(framePointer))
      {
        shouldContinue = false;
      }
    }
    
    System.out.println("Done! ");
  }
  
  /**
   * Handle the "Resume execution" command.
   * 
   * @throws IOException
   */
  private static void handleResumeExecutionCommand() throws IOException
  {
    // acknowledge the command and resume execution.
    outputStream.writeInt(0);
    
    System.out.println(" Received \"Resume (11, 3)\" command.");
    
    //TODO: now how can I run the bytecode that was standing where this
    // breakpoint is, now?
    
    // just for development,dump the call stack.
    TestJopDebugKernel.dumpCallStack();
  }
  
  /**
   * Handle the "Invoke static" command.
   * 
   * @throws IOException
   */
  private static void handleInvokeStaticCommand() throws IOException
  {
    int methodId = inputStream.readInt();
//  int numArguments = inputStream.readInt();
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
  }

  /**
   * Handle the "Exit" command.
   * 
   * @throws IOException
   */
  private static void handleExitCommmand() throws IOException
  {
    // acknowledge the command and exit.
    outputStream.writeInt(0);
    
    System.out.println(" Received \"Exit (10)\" command.");
    System.out.println(" Shutting down...     ");
    
    System.exit(0);
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
      getMPFromFrame(getCurrentFramePointer());
    
    EmbeddedOutputStream embeddedStream = new EmbeddedOutputStream(System.out);
    setDebugStreams(System.in, embeddedStream);
    
    initialized = true;
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

  }
  
  private static int getCPLocalsArgsFromMP(int mp)
  {
    return Native.rdMem(mp + 1); // cp, locals, args
  }

  private static int getArgCountFromVal(int val)
  {
    return val & 0x1f;
  }

  private static int getLocalsCountFromVal(int val)
  {
    return ((val >>> 5) & 0x1f);
  }

  private static int getCPFromMP(int mp)
  {
    int value = getCPLocalsArgsFromMP(mp);
    value = value >>> 10;
    return value; // cp
  }
  
  private static int getMPFromFrame(int frame)
  {
    return Native.rdIntMem(frame + 4);
  }
  
  private static int getCPFromFrame(int frame)
  {
    return Native.rdIntMem(frame + 3);
  }

  private static int getVPFromFrame(int frame)
  {
    return Native.rdIntMem(frame + 2);
  }

  private static int getPCFromFrame(int frame)
  {
    return Native.rdIntMem(frame + 1);
  }
  
  private static int getSPFromFrame(int frame)
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
      mp = getMPFromFrame(framePointer);
      
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
   * It consider the "this" reference, parameters and locals. 
   * 
   * @param frame
   * @return
   */
  private static int getNumLocalsAndParametersFromFrame(int frame)
  {
    int loc;
    
    int localsPointer = getLocalsPointerFromFrame(frame);
    loc = frame - localsPointer;
    
//    System.out.print("Frame: ");
//    System.out.print(frame);
//    System.out.print("  localsPointer: ");
//    System.out.print(localsPointer);
//    
//    System.out.print("  loc: ");
//    System.out.print(loc);
//    if(isFirstFrame(frame))
//    {
//      System.out.println("First frame!");
//    }
    
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
  private static int getLocalsPointerFromFrame(int frame)
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
    
    numLoc = getNumLocalsAndParametersFromFrame(frame);
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
    
    numLoc = getNumLocalsAndParametersFromFrame(frame);
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
  
  public static final int getFramePointerOfCallerMethod()
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
  public static final boolean isFirstFrame(int framePointer)
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
  private static int getStackDepth()
  {
    int framePointer = getFramePointerOfCallerMethod();
    return getStackDepth(framePointer);
  }

  private static int getInstanceSize(int object)
  {
    int classReference = getClassReference(object);
    return Native.rdMem(classReference);
  }

  private static int getClassReference(int object)
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

  private static int getConstantPoolFromClassReference(int classReference)
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
   * @throws IOException 
   */
  private static boolean handleSetBreakPointCommand() throws IOException
  {
    int methodStructPointer;
    int instructionOffset;
    int result;
    
    // read the method pointer
    methodStructPointer = inputStream.readInt();
    
    // read the instruction offset
    instructionOffset = inputStream.readInt();
    
    System.out.println("Method body before:");
    dumpMethodBody(methodStructPointer);
    
    // set the breakpoint
    result = setBreakPoint(methodStructPointer, instructionOffset);
    
    System.out.println("Method body after:");
    dumpMethodBody(methodStructPointer);
    
    // send an ack back to keep it in sync. This also inform which instruction
    // was overwritten, so the debugger can undo it later. 
    
    outputStream.writeInt(result);
    
    if(result != INVALID_INSTRUCTION)
    {
      return true;
    }
    return false;
  }
  
  /**
   * Handle a "clear breakpoint" command.
   * 
   * @return
   * @throws IOException 
   */
  private static boolean handleClearBreakPointCommand() throws IOException
  {
    int methodStructPointer;
    int instructionOffset;
    int newInstruction;
    int result;
    
    // read the method pointer
    methodStructPointer = inputStream.readInt();
    
    // read the instruction offset
    instructionOffset = inputStream.readInt();
    
    // read the new instruction
    newInstruction = inputStream.readInt();
    
    System.out.println("Method body before:");
    dumpMethodBody(methodStructPointer);
    
    // clear the breakpoint
    result = clearBreakPoint(methodStructPointer, instructionOffset, newInstruction);
    
    System.out.println("Method body after:");
    dumpMethodBody(methodStructPointer);
    
    // send an ack back to keep it in sync. This also inform which instruction
    // was overwritten, so the debugger can undo it later. 
    
    outputStream.writeInt(result);
    
    if(result != INVALID_INSTRUCTION)
    {
      return true;
    }
    return false;
  }
  
  /**
   * Handle a "get method pointer" command.
   * 
   * This is not a standard JDWP command but is necessary to implement
   * debugging support in JOP.
   * 
   * @throws IOException
   */
  private static void handleGetStackFrameMPCommand() throws IOException
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
    pointer = getMPFromFrame(pointer);
    
    System.out.print("  Method pointer: ");
    System.out.println(pointer);
    
    outputStream.writeInt(pointer);
  }
  
  /**
   * Handle a "Get local variable" command.
   * 
   * @throws IOException
   */
  private static void handleGetLocalVariableCommand() throws IOException
  {
    int frameIndex = inputStream.readInt();
    int fieldIndex = inputStream.readInt();
    int value;
    int pointer;
    
    System.out.print(" Frame index: " );
    System.out.print(frameIndex);
    System.out.print(" Variable index: " );
    System.out.println(fieldIndex);
    
    pointer = getFramePointerAtIndex(frameIndex);
    value = getLocalVariable(pointer, fieldIndex);
    
    System.out.print("  Value: ");
    System.out.println(value);
    System.out.print(" Pointer: ");
    System.out.println(pointer);
    
    outputStream.writeInt(value);
  }

  /**
   * Get the frame pointer at the given index.
   * 
   * This method allows accesing the call stack as an array,
   * with the stack frame for the "main" method call
   * at index zero, the next method (called inside main) at
   * index one and so on.
   * 
   * @param frameIndex
   * @return
   */
  private static int getFramePointerAtIndex(int frameIndex)
  {
    int count = getStackDepth();
    int pointer = getCurrentFramePointer();
    
    // traverse the stack to find the frame pointer
    for(int i = 0; i < (count - frameIndex); i++)
    {
      pointer = getNextFramePointer(pointer);
    }
    return pointer;
  }
  
  /**
   * Handle a "Set local variable" command.
   * 
   * @throws IOException
   */
  private static void handleSetLocalVariableCommand() throws IOException
  {
    int frameIndex = inputStream.readInt();
    int fieldIndex = inputStream.readInt();
    int value = inputStream.readInt();
    int pointer;
    
    System.out.print(" Frame index: " );
    System.out.print(frameIndex);
    System.out.print(" Variable index: " );
    System.out.println(fieldIndex);
    
    pointer = getFramePointerAtIndex(frameIndex);
    setLocalVariable(pointer, fieldIndex, value);
    
    System.out.print("  Value: ");
    System.out.println(value);
    System.out.print(" Pointer: ");
    System.out.println(pointer);
    
//    writeInt(4);
    outputStream.writeInt(value);
  }
  
  /**
   * Handle a "Get number of local variables" command.
   * 
   * @throws IOException
   */
  private static void handleGetNumberOfLocalVariablesCommand() throws IOException
  {
    int frameIndex = inputStream.readInt();
    int framePointer;
    int numLocals;
    
    System.out.print(" Frame index: " );
    System.out.print(frameIndex);
    
    framePointer = getFramePointerAtIndex(frameIndex);
    numLocals = getNumLocalsAndParametersFromFrame(framePointer);
    
    System.out.print(" Number of local variables: " );
    System.out.println(numLocals);
    
    outputStream.writeInt(numLocals);
  }
  
  /**
   * Set a breakpoint instruction.
   * 
   * Note: this method DOES NOT check instruction boundaries.
   * 
   * @param methodStructPointer pointer to the method structure
   * @param instruction
   * @return
   */
  private static int setBreakPoint(int methodStructPointer, int instructionOffset)
  {
    int instruction;
    
//    System.out.println("setBreakPoint(int methodPointer, int instructionOffset)");
    
    instruction = overwriteInstruction(methodStructPointer, instructionOffset,
        BREAKPOINT_INSTRUCTION);
    
    return instruction;
  }
  
  /**
   * Clear a breakpoint instruction.
   * 
   * @param methodStructPointer pointer to the method structure
   * @param instructionOffset
   * @param newInstruction
   * @return
   */
  private static int clearBreakPoint(int methodStructPointer, 
    int instructionOffset, int newInstruction)
  {
    int instruction;
    
//    System.out.println("clearBreakPoint(int methodStructPointer,int instructionOffset, int newInstruction)");
    
    instruction = overwriteInstruction(methodStructPointer, instructionOffset,
        newInstruction);
    
    return instruction;
  }
  
  /**
   * Overwrite one method instruction and return the instruction which was
   * set at that address previously.
   * 
   * This method DOES NOT check instruction boundaries, it just check 
   * the method length. It allows changing anything inside the method body,
   * as long as it fits inside. This includes bytecode parameters.
   * 
   * @param methodStructPointer
   * @param instructionOffset
   * @param instruction
   * @return
   */
  private static int overwriteInstruction(int methodStructPointer, 
    int instructionOffset, int newInstruction)
  {
    int methodSize;
    int startAddress;
    int instruction = INVALID_INSTRUCTION;
    int instructionAddress;
    int word;
    
    System.out.println("setBreakPoint(int methodPointer, int instructionOffset)");
    dumpMethodStruct(methodStructPointer);
    
    startAddress = getMethodStartAddress(methodStructPointer);
    // beware: method sizes are in words, not in bytes. And 1 word = 4 bytes. 
    methodSize = getMethodSize(methodStructPointer);
    // calculate the method size in bytes. 
    methodSize *= 4;
    
    // check if the address is inside the method body.
    if(instructionOffset >= 0 && instructionOffset < methodSize)
    {
      instruction = readByte(startAddress, instructionOffset);
      System.out.println("Old instruction: " + instruction);
      
      writeByte(newInstruction, startAddress, instructionOffset);

      
//      instructionAddress = startAddress + instructionOffset;
//      
//      instruction = Native.rdMem(instructionAddress);
//      System.out.println("Old instruction: " + instruction);
//      
//      Native.wrMem(newInstruction, instructionAddress);
    }
    else
    {
      System.out.print("Wrong instruction offset: ");
      System.out.println(instructionOffset);
      
      System.out.print("Method size:");
      System.out.println(methodSize);
      
      System.out.println();
    }
    
    return instruction;
  }
  
  /**
   * Read one byte from the method code.
   *  
   * Useful to manipulate compiled code, for operations 
   * such as "set breakpoint" or "clear breakpoint".
   * 
   * @param startAddress
   * @param instructionOffset
   * @return
   */
  private static int readByte(int startAddress, int instructionOffset)
  {
    int word, index;
    int data;
    int address;
    int result = 0;
    
    // divide by four. Same as the first line.
//     word = instructionOffset / 4;  
    word = instructionOffset >> 2;  
    
    // get the remainder. Same as the first line.
//    index = instructionOffset % 4;
    index = instructionOffset & 0x03;
    
    address = startAddress + word;
    data = Native.rdMem(address);
    
//    System.out.println("Word:      " + word);
//    System.out.println("Remainder: " + index);
//    System.out.println("address:   " + address);
//    System.out.print("data:      ");
//    printIntHex(data);
//    System.out.println();
    
    switch(index)
    {
      case 0:
      {
        result = data >>> (3 * 8);
        break;
      }
      case 1:
      {
        result = data >>> (2 * 8);
        break;
      }
      case 2:
      {
        result = data >>> (1 * 8);
        break;
      }
      case 3:
      default:
      {
        result = data;
      }
    }
    
    result = result & 0x00ff;
    
//    System.out.print("Result: ");
//    printIntHex(result);
//    System.out.println();
    
    return result;
  }
  
  /**
   * Set one byte from the method code area.
   *
   * Basic approach to set a bytecode:
   * - get the address of the word
   * - read the word
   * - clear the old byte
   * - set the new byte
   * - write back the word
   * 
   * @param newInstruction
   * @param startAddress
   * @param instructionOffset
   */
  private static void writeByte(int newInstruction, int startAddress, int instructionOffset)
  {
    int word, index;
    int data;
    int address;
    int result = 0;
    
    // divide by four.
    word = instructionOffset >> 2;  
    
    // get the remainder.
    index = instructionOffset & 0x03;
    
    address = startAddress + word;
    data = Native.rdMem(address);
    
//    System.out.println("Word:      " + word);
//    System.out.println("Remainder: " + index);
//    System.out.println("address:   " + address);
    System.out.print("data:      ");
    printIntHex(data);
    System.out.println();
    
    // clear all the other bytes, just in case. 
    newInstruction &= 0x00ff;
    
    // clear the old byte using a mask and shift the new byte accordingly
    switch(index)
    {
      case 0:
      {
        newInstruction = newInstruction << (3 * 8);
        data = data & MASK_FIRST_BYTE;
        break;
      }
      case 1:
      {
        newInstruction = newInstruction << (2 * 8);
        data = data & MASK_SECOND_BYTE;
        break;
      }
      case 2:
      {
        newInstruction = newInstruction << (1 * 8);
        data = data & MASK_THIRD_BYTE;
        break;
      }
      case 3:
      default:
      {
        data = data & MASK_FOURTH_BYTE;
      }
    }
    // merge the (already shifted) new byte into the word
    data = data | newInstruction;
    
    // finally, write it back into memory. Bytecode changed!
    Native.wrMem(data, address);
    
    System.out.print("new data:  ");
    printIntHex(data);
    System.out.println();
  }
  
  private static void printIntHex(int value)
  {
    EmbeddedOutputStream.printIntHex(value);
  }
  
  private static void testReadInstruction(int instructionAddress)
  {
    int instruction;
    
    System.out.println("----------------------------------------");
    instruction = Native.rdMem(instructionAddress + 1);
    System.out.println("Instruction: " + instruction);
    
    instruction = Native.rdMem(instructionAddress + 1);
    System.out.println("Instruction: " + instruction);
    
    instruction = Native.rdMem(instructionAddress + 2);
    System.out.println("Instruction: " + instruction);
    
    instruction = Native.rdMem(instructionAddress + 3);
    System.out.println("Instruction: " + instruction);
    System.out.println("----------------------------------------");
  }
  
  /**
   * Return the method start address.
   * 
   * @param methodPointer
   * @return
   */
  private static int getMethodStartAddress(int methodPointer)
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
  private static int getMethodSize(int methodPointer)
  {
    int startAddress;
    int methodSize;
    
    startAddress = Native.rdMem(methodPointer);
    
    // get the last 10 bits: the method length. Hence, size can be up to 1kb.
    methodSize = startAddress & 0x000003ff;
    
    return methodSize;
  }
  
  private static int getMethodConstantPool(int methodPointer)
  {
    int data;
    
    methodPointer++;
    data = Native.rdMem(methodPointer);
    
    // shift the variable 10 bits to the right (unsigned). This is the 
    // constant pool address.
    data = data >>> 10;
    
    return data;
  }
  
  public static final int getMethodArgCount(int methodPointer)
  {
    int data;
    
    methodPointer++;
    data = Native.rdMem(methodPointer);
    
    // get the last 5 bits. This is the arg count.
    data = data & 0x0000001f;
    
    return data;
  }
  
  public static final int getMethodLocalsCount(int methodPointer)
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
  
  public static final void dumpMethodStruct(int methodPointer)
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
    
    System.out.print("Size (words):  ");
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
    data = getMPFromFrame(data);
    
//    System.out.println("getCurrentMethodPointer()");
    
    return data;
  }
  
  public static final void dumpMethodBody(int methodPointer)
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
  
  /**
   * Methods to print the stack content in a way that is
   * easy to inspect.
   * 
   * @param frame
   * @param previousFrame
   */
  private static void prettyPrintStack()
  {
    int frame;
    int previousFrame;
    
//    previousFrame = getCurrentFramePointer();
//    frame = getNextFramePointer(previousFrame);
    
    frame = getCurrentFramePointer();
    
    while(isFirstFrame(frame) == false)
    {
      previousFrame = frame;
      frame = getNextFramePointer(previousFrame);
      
      if(isFirstFrame(frame))
      {
        System.out.println("Stack frame for main method (next):");
      }
      
      prettyPrintStackFrame(frame, previousFrame);
    }
    // almost done now, but still need to do the last step:
    // print information for the frame of the main method. 
//    prettyPrintStackFrame(frame, previousFrame);
  }
  
  /**
   * Pretty print method to show a stack frame internal structure.
   * Useful for debugging.
   * 
   * @param frame
   */
  private static void prettyPrintStackFrame(int frame, int previousFrame)
  {
    TestJopDebugKernel.printLine();
    
    printVariablesFromFrame(frame);
    System.out.println();
    
    printRegistersFromFrame(frame);
    System.out.println();
    
    printLocalStackFromFrame(frame, previousFrame);
    
    TestJopDebugKernel.printLine();
  }
  
  /**
   * @param frame
   */
  private static void printVariablesFromFrame(int frame)
  {
    int localPointer;
    int length;
    
    localPointer = getLocalsPointerFromFrame(frame);
    length = frame - localPointer;
    
//    System.out.print("  Local pointer: ");
//    System.out.print(localPointer);
//    System.out.print("  Frame pointer: ");
//    System.out.print(frame);
//    System.out.print("  Length: ");
//    System.out.print(length);
//    System.out.println();
    
    if(length < 0 || length > MAX_LOCAL_VARIABLES)
    {
      System.out.println("FAILURE!!! wrong local pointer!");
      System.out.print("  Local pointer: ");
      System.out.print(localPointer);
      
      System.out.print("  Frame: ");
      System.out.print(frame);
      
      System.out.print("  Max. variables: ");
      System.out.print(MAX_LOCAL_VARIABLES);
      return;
    }
    
    System.out.println("Local variables:");
    
    // print all variables before the frame pointer
    printStackArea(localPointer, length);
  }
  
  /**
   * Print the five register fields based on the frame pointer. 
   * 
   * @param frame
   */
  private static void printRegistersFromFrame(int frame)
  {
    int value;
    
    System.out.print("Previous registers - SP: ");
    value = getSPFromFrame(frame);
    System.out.print(value);
    
    System.out.print("  PC: ");
    value = getPCFromFrame(frame);
    System.out.print(value);
    
    System.out.print("  VP: ");
    value = getVPFromFrame(frame);
    System.out.print(value);
    
    System.out.print("  CP: ");
    value = getCPFromFrame(frame);
    System.out.print(value);
    
    System.out.print("  MP: ");
    value = getMPFromFrame(frame);
    System.out.print(value);
    
    System.out.println();
  }
  
  /**
   * Print the local execution stack based on the current frame.
   * 
   * The previousFrame parameter points to the frame on top of
   * the one pointed by frame. It is used to delimit the local stack.
   * It should be greater than frame. 
   * 
   * @param frame
   */
  private static void printLocalStackFromFrame(int frame, int previousFrame)
  {
    int localStackPointer, length;
    
    // the pointer to the local stack, right after the five frame fields.
    // Be careful here: this is *NOT* the SP field 
    // (which points to the previous stack top).
    localStackPointer = frame + 5;
    
    // the local execution stack (after a method call has started)
    // goes from the 5th byte (right after "previous MP" location)
    // until the position pointed by the "previous SP" field of the
    // next stack frame (the one of the called method).
    // Since the "previous SP" in the next frame points to the
    // stack top, it will point to the "previous MP" in the
    // current frame when the local stack is empty.
    length = getSPFromFrame(previousFrame) - localStackPointer + 1;
    
//    System.out.print("Frame: ");
//    System.out.print(frame);
//    System.out.print("  localStackPointer: ");
//    System.out.print(localStackPointer);
//    System.out.print("  previousFrame: ");
//    System.out.print(previousFrame);
//    
//    System.out.print("  length: ");
//    System.out.print(length);
    
    if(length > 0)
    {
      System.out.print("Local execution stack size: ");
      System.out.println(length);
      printStackArea(localStackPointer, length);
    }
    else
    {
      System.out.println("Local execution stack: empty");
      // System.out.println("Length: " + length);
    }
  }
  
  /**
   * Print a set of stack positions in hex format.
   * It formats the output by inserting a new line for every 
   * 8 words printed.
   * 
   * @param initialPosition the initial position to be printed.
   * @param length the number of stack slots to be printed.
   */
  private static void printStackArea(int initialPosition, int length)
  {
    int index, data;
    
    if(length <= 0)
    {
      return;
    }
    
    // print a set of stack words. Do nothing and return, in case of a negative
    // value for length.
    for(index = 0; index < length; index++)
    {
      data = Native.rdIntMem(initialPosition + index);
      printIntHex(data);
      System.out.print(" ");
      
      if(((index + 1)% 0x08) == 0)
      {
//        System.out.print("Index:");
//        System.out.println(index);
        System.out.println();
      }
    }
    System.out.println();
  }
}
