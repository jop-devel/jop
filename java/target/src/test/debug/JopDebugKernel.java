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

import debug.constants.TagConstants;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import debug.constants.CommandConstants;
import debug.constants.ErrorConstants;
import debug.io.DebugKernelChannel;
import debug.io.EmbeddedOutputStream;

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
  
  private static DebugKernelChannel debugChannel;
  
  // a variable to hold the frame pointer of the breakpoint method
  private static int breakpointFramePointer;
  
  // internal flag to turn on/off tracing messages
  //private static boolean shouldPrintInternalMessages = false;
  private static boolean shouldPrintInternalMessages = true;
  
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
    
    // for tracing.
    enableDevelopmentInternalMessages();
    
    // update the frame pointer for the breakpoint method.
    breakpointFramePointer = getCurrentFramePointer();
    
    // notify the debug server that a breakpoint was reached.
    // Use the current frame pointer because this is the frame 
    // which holds data related to the previous method call. 
//    int framePointer = getCurrentFramePointer();
//    sendBreakpointEvent(framePointer);
    
    int commandset, command;
    
    debugPrint("Breakpoint! Current stack depth: ");
    debugPrintln(getStackDepth());
    
    commandset = 0;
    while(commandset >= 0)
    {
      TestJopDebugKernel.printLine();
      try
      {
        // receive the next JDWP packet
        debugChannel.receivePacket();
        
        // get the "command set" and "command" fields
        commandset = debugChannel.readInputCommandSet();
        command = debugChannel.readInputCommand();

//        commandset = inputStream.read();
//        command = inputStream.read();
        
        debugPrint("CommandSet:");
        debugPrint(commandset);
        debugPrint("  Command:");
        debugPrintln(command);
        
        // exit from this method.
        if((commandset == CommandConstants.VirtualMachine_Command_Set) &&
           (command == CommandConstants.VirtualMachine_Exit))
        {
          debugPrintln("Exit: stop execution.");
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
//            writeInt(value);
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
          debugPrintln("Invoke static");
          handleInvokeStaticCommand();
          continue;
        }
        
        // resume execution. Finish this method and continue.
        if((commandset == 11) && (command == 3))
        {
          debugPrintln("Resume execution");
          handleResumeExecutionCommand();
          
          // stop the loop
          break;
        }
        
        // return a list of all stack frame locations
        if((commandset == 11) && (command == 6))
        {
          debugPrintln("Get stack frames");
          handleGetStackFramesCommand(breakpointFramePointer);
          
          continue;
        }
        
        // calculate the stack depth of the caller method.
        if((commandset == 11) && (command == 7))
        {
          debugPrintln("Get stack depth");
          handleGetStackDepthCommand(breakpointFramePointer);
          
          continue;
        }
        
        // dump the call stack. For development ONLY.
        // NOT a standard JDWP command set/command pair.
        if((commandset == 11) && (command == 13))
        {
          debugPrintln("Print the call stack.");
          handlePrintCallStackCommand();
          continue;
        }
        
        // dump one stack frame. For development ONLY.
        // NOT a standard JDWP command set/command pair.
        if((commandset == 11) && (command == 14))
        {
          debugPrintln("Print a stack frame.");
          handlePrintStackFrameCommand();
          continue;
        }
        
        // ----------------------
        // breakpoint commands
        // ----------------------
        
        // set breakpoint
        if((commandset == 15) && (command == 1))
        {
          debugPrintln("set breakpoint");
          handleSetBreakPointCommand();
          continue;
        }
        
        // clear breakpoint
        if((commandset == 15) && (command == 2))
        {
          debugPrintln("clear breakpoint");
          handleClearBreakPointCommand();
          continue;
        }
        
        // get the method pointer from a stack frame
        // TODO: STILL NEED TO BE FIXED
        if((commandset == 16) && (command == 0))
        {
          debugPrintln("Get method pointer");
          handleGetStackFrameMPCommand();
          
          continue;
        }
        
        // get a local variable value
        if((commandset == 16) && (command == 1))
        {
          debugPrintln("Get local variable");
          handleGetLocalVariableCommand();
          
          continue;
        }
        
        // set a local variable value
        if((commandset == CommandConstants.StackFrame_Command_Set) &&
           (command == CommandConstants.StackFrame_SetValues))
        {
          debugPrintln("Set local variable");
          handleSetLocalVariableCommand();
          
          continue;
        }
        
        // return the nuber of local variables
        if((commandset == 16) && (command == 5))
        {
          debugPrintln("Get number of local variables");
          handleGetNumberOfLocalVariablesCommand();
          
          continue;
        }
        
        // specific command just to help development.
        if((commandset == 100) && (command == 1))
        {
          debugPrintln("Debug development command: Send JDWP packets");
          handleTestSendJDWPPackets();
          
          continue;
        }
        
        // specific command just to help development.
        // TODO: STILL NEED TO BE FIXED
        if((commandset == 100) && (command == 2))
        {
          debugPrintln("Debug development command: receive JDWP packets");
          handleTestReceiveJDWPPackets(breakpointFramePointer);
          
          continue;
        }
        
        debugPrintln("Received invalid command or command set! ");
        debugPrint("Command: ");
        debugPrint(command);
        debugPrint(" Command set: ");
        debugPrint(commandset);
        debugPrintln();
      }
      catch(IOException exception)
      {
        debugPrintln("Failure: " + exception.getMessage());
        exception.printStackTrace();
        break;
      }
    }
    
    debugPrintln("Returning from \"breakpoint\".");
  }
  
  /**
   * Send a breakpoint event to the debug server.
   * 
   * By using the frame pointer as unique reference, it's
   * possible to send arbitrary breakpoint events related
   * to any frames present in the call stack. That's good.
   */
  private static void sendBreakpointEvent(int framePointer)
  {
    int typeTag, classId, methodId, methodLocation; 
    
    // calculate all fields needed to be sent through the channel
    typeTag = getTypeTag(framePointer);
    classId = getClassReferenceFromFrame(framePointer);
    methodId = getMPFromFrame(framePointer);
    methodLocation = getPCFromFrame(framePointer);
    
    // request the channel to send a message to the debug server.
    try
    {
      debugChannel.sendBreakpointEvent(typeTag, classId, methodId, methodLocation);
    }
    catch (IOException exception)
    {
      System.out.println("Failure: " + exception.getMessage());
      exception.printStackTrace();
    }
  }
  
  /**
   * @param framePointer
   * @return
   */
  private static int getTypeTag(int framePointer)
  {
    // for now, just return 1 (CLASS)
    return 1;
  }
  
  /**
   * @param framePointer
   * @return
   */
  private static int getClassReferenceFromFrame(int framePointer)
  {
    int cp; 
    
    // get the pointer to the constant pool
    cp = getCPFromFrame(framePointer);
    
    //go back one word. Now it points to the class reference!
    cp --;
    
    // return a pointer to the class structure.
    return Native.rdMem(cp);
  }
  
  /**
   * @param breakpointFramePointer 
   * @throws IOException
   */
  private static void handleGetStackDepthCommand(int breakpointFramePointer)
    throws IOException
  {
    // get stack depth of the breakpoint method
    int count = getStackDepth(breakpointFramePointer);
    
    debugPrint(" Stack depth of caller method: ");
    debugPrintln(count);
    
    debugChannel.sendReplyFrameCount(count);
  }
  
  /**
   * @throws IOException
   */
  private static void handlePrintCallStackCommand() throws IOException
  {
    prettyPrintStack();
    
    // just for development
    //TestJopDebugKernel.dumpCallStack();
    
    debugChannel.sendReply();
  }
  
  private static void handlePrintStackFrameCommand() throws IOException
  {
    // get stack frame index to be printed (this frame)
    int frameIndex;
    int previousFrameIndex;
    int framePointer,previousFramePointer;
    
//    debugPrintln("Starting handlePrintStackFrameCommand");
    
    debugChannel.skipInt();
    frameIndex = debugChannel.readIntValue();
    
    previousFrameIndex = frameIndex + 1;
    
//    debugPrint("Frame index to print: ");
//    debugPrintln(frameIndex);
    
    if(previousFrameIndex < getStackDepth())
    {
      previousFramePointer = getFramePointerAtIndex(previousFrameIndex);
      framePointer = getNextFramePointer(previousFramePointer);
      
      prettyPrintStackFrame(framePointer, previousFramePointer);
      
      debugChannel.sendReply();
    }
    else
    {
      debugPrintln("Failure: invalid index -> " + frameIndex);
      
      debugChannel.sendReplyWithErrorCode(ErrorConstants.ERROR_INVALID_FRAMEID);
    }
  }
  
  /**
   * Handle the "Frames" command. 
   * The current implementation of this method always answer 
   * with the entire stack: it just ignore partial requests
   * (and the internal fields).
   * 
   * @throws IOException
   */
  private static void handleGetStackFramesCommand(int breakpointFramePointer)
    throws IOException
  {
    debugPrintln(" Will read startFrame");
    
    int startFrame;
//    int numFrames;
    
    // for now, ignore the thread ID and startFrame
    debugChannel.skipInt();
    
    startFrame = debugChannel.readIntValue();
//    debugChannel.skipInt();
    debugPrint("  startFrame: ");
    debugPrintln(startFrame);
    
    //numFrames =  debugChannel.readIntValue();
    // for now, ignore the number of frames. Consider always "all frames".
    debugChannel.skipInt();
    
    int framePointer, count;
    
    // get the (current) maximum value for the stack depth
    count = getStackDepth(breakpointFramePointer);
    
    // index out of valid bounds. Ignore request, send error message and return.
    if(startFrame < 0 || startFrame >= count)
    {
      debugChannel.sendReplyWithErrorCode(ErrorConstants.ERROR_INVALID_THREAD);
      return;
    }
    // now we know it's a valid start frame index.
    // get the first frame pointer and send all frames to the server
    //framePointer = getFramePointerAtIndex(startFrame);
    
    // just to test. The line below will expose also the frame for breakpoint.
    //framePointer = getCurrentFramePointer();
    framePointer = breakpointFramePointer;
    count = getStackDepth(framePointer);
    
    debugPrint(" Stack depth:");
    debugPrintln(count);
    
    debugChannel.prepareStackFrameListPacket(count);
    
    boolean shouldContinue = true;
    while(shouldContinue)
    {
      debugPrintln(" Will add one frame.");
      // all registers inside the frame are related to the previous frame
      // context. So, send data about one frame based on the 
      // frame pointer right above it.
      
      // write the pointer for the next frame
      debugChannel.writeStackFrameId(getNextFramePointer(framePointer));
      
      // write the register values which are inside this frame
      writeLocation(framePointer);
      
      // walk to the next stack position
      framePointer = getNextFramePointer(framePointer);
      if(isFirstFrame(framePointer))
      {
        shouldContinue = false;
      }
      
      // just for development.
      //prettyPrintStack();
    }
    
    // and finally send the reply packet with all data inside.
    debugChannel.sendStackFrameListReply();
//  debugPrintln(" Sent frame.");
//  debugPrintln();
    
    debugPrintln("Done! ");
  }
  
  private static void writeLocation(int framePointer)
  {
    int typeTag, classId, methodId, methodLocation; 
    
    // calculate all fields needed to be sent through the channel
    typeTag = getTypeTag(framePointer);
    classId = getClassReferenceFromFrame(framePointer);
    methodId = getMPFromFrame(framePointer);
    methodLocation = getPCFromFrame(framePointer);
    
    debugChannel.writeExecutableLocation(typeTag, classId, methodId, 
      methodLocation);
  }
  
  /**
   * Handle the "Resume execution" command.
   * 
   * @throws IOException
   */
  private static void handleResumeExecutionCommand() throws IOException
  {
    // acknowledge the command and resume execution.
    debugChannel.sendReply();
    
    debugPrintln(" Received \"Resume (11, 3)\" command.");
    
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
    int methodId;
    int argument;
    
    methodId = debugChannel.readIntValue();
    argument = debugChannel.readIntValue();
    
    debugPrint(" Method ID: " );
    debugPrint(methodId);
    debugPrint(" Argument: " );
    debugPrintln(argument);
    
    if(breakpointMethodPointer == methodId)
    {
      debugPrintln("Invalid method pointer!");
      debugPrintln("Cannot call breakpoint from itself.");
    }
    else
    {
      int sp = Native.getSP();
      debugPrint("SP = ");
      debugPrintln(sp);
//          sp++;
//          Native.wrIntMem(argument, sp);
//        Native.setSP(sp);
//        test just to see if it breaks. It doesn't if SP is restored later.
//          Native.setSP(sp + 4);
      
      debugPrintln("Calling method now:");
      Native.invoke(argument, methodId);
      
      // now restore SP to its previous value. Doing this without
      // getting the top value just ignores anything left on the stack.
//          sp = sp - 1;
      Native.setSP(sp);
      
//          debugPrintln("Right after return.");
//          sp = Native.getSP();
//          debugPrint("SP = ");
//          debugPrintln(sp);
    }
    
    // send a reply packet to report that this command finished successfully.
    debugChannel.sendReply();
  }
  
  /**
   * Handle the "Exit" command.
   * 
   * @throws IOException
   */
  private static void handleExitCommmand() throws IOException
  {
    int exitCode;
    
    exitCode = debugChannel.readIntValue();
    // acknowledge the command and exit.
    debugChannel.sendReply();
    
    // TODO: where is the proper place to send a VMDeath event?
    // maybe after the main method return, too? ;)
    debugChannel.sendVMDeathEvent();

    debugPrint(" Received \"Exit (10)\" command. Code: ");
    debugPrintln(exitCode);
    debugPrintln(" Shutting down...     ");
    
    System.exit(exitCode);
  }
  
  /**
   * Write one int value to the output stream.
   * 
   * @param value
   * @throws IOException
   */
  private static void writeInt(int value) throws IOException
  {
	// TODO: remove this method in the future.
	System.out.println("WARNING!!! don't use this method directly!");
    outputStream.writeInt(value);
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
    
    // initialize the debug channel
    debugChannel = new DebugKernelChannel(inputStream, outputStream);
    
    // send a VM Start event
//    try
//    {
//      // TODO uncomment to send this event!
//      debugChannel.sendVMStartEvent();
//    }
//    catch (IOException exception)
//    {
//      debugPrintln("Failure: " + exception.getMessage());
//      exception.printStackTrace();
//    }
    
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
    
//    debugPrint("getNextFramePointer(int frame)");
//    debugPrintln(framePointer);
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
    
//    debugPrint("Frame: ");
//    debugPrint(frame);
//    debugPrint("  localsPointer: ");
//    debugPrint(localsPointer);
//    
//    debugPrint("  loc: ");
//    debugPrint(loc);
//    if(isFirstFrame(frame))
//    {
//      debugPrintln("First frame!");
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
//    debugPrintln("  getField(int frame, int fieldIndex) frame = " + frame);
//    debugPrintln("Num. Locals: " + numLoc);
    if(fieldIndex < numLoc && fieldIndex >= 0)
    {
//      vp = getVPFromFrame(frame);
      vp = getLocalsPointerFromFrame(frame);
//      debugPrintln("  Calculating VP: " + vp);
      
//      value = Native.rdMem(vp + fieldIndex);
      value = Native.rdIntMem(vp + fieldIndex);
//      debugPrintln("  Value read from stack frame is: " + value);
      
//      value ++;
//      Native.wrMem(value, vp + fieldIndex);
    }
    else
    {
      debugPrintln("  Invalid index: " + fieldIndex);
      debugPrintln("  Num. locals is: " + numLoc);
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
//    debugPrintln("  setField(int frame, int fieldIndex, value) frame = " + frame);
//    debugPrintln("Num. Locals: " + numLoc);
    if(fieldIndex < numLoc && fieldIndex >= 0)
    {
//      vp = getVPFromFrame(frame);
      vp = getLocalsPointerFromFrame(frame);
//      debugPrintln("  Calculating VP: " + vp);
      
//      value = Native.rdMem(vp + fieldIndex);
      Native.wrIntMem(value, vp + fieldIndex);
//      debugPrintln("  Value written to stack frame is: " + value);
    }
    else
    {
      debugPrintln("  Invalid index: " + fieldIndex);
      debugPrintln("  Num. locals is: " + numLoc);
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
    
//    debugPrintln("isFirstFrame()");
    if(mainMethodFramePointer == 0)
    {
      //called only once to calculate the main method pointer.
      mainMethodFramePointer = calculateMainMethodFramePointer();
      
//      debugPrint("main method calculated: ");
//      debugPrintln(mainMethodFramePointer);
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
    debugPrintln("Pointer to 'special' pointers:" + var);
    
    var = Native.rdMem(var+3);  // pointer to main method struct
    debugPrintln("Pointer to main method struct:" + var);
    
    
    var= getCPLocalsArgsFromMP(var); // get val from 'main' method structure
    
    debugPrintln("Main arguments:" + getArgCountFromVal(var));
    debugPrintln("Main locals:" + getLocalsCountFromVal(var));
    
    // get the number of local variables in the stack frame
    var = getArgCountFromVal(var) + getLocalsCountFromVal(var);
    
    debugPrintln("Stack base pointer: " + STACK_BASE_POINTER);
    
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
    methodStructPointer = debugChannel.readIntValue();
    
    // read the instruction offset
    instructionOffset = debugChannel.readIntValue();
    
    debugPrintln("Method body before:");
    dumpMethodBody(methodStructPointer);
    
    // set the breakpoint
    result = setBreakPoint(methodStructPointer, instructionOffset);
    
    debugPrintln("Method body after:");
    dumpMethodBody(methodStructPointer);
    
    // send an ack back to keep it in sync. This also inform which instruction
    // was overwritten, so the debugger can undo it later. 
    if(result != INVALID_INSTRUCTION)
    {
      debugChannel.sendReplySetBreakpoint(result);
      return true;
    }
    else
    {
      debugChannel.sendReplyWithErrorCode(ErrorConstants.ERROR_INVALID_LOCATION);
      return false;
    }
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
    methodStructPointer = debugChannel.readIntValue();
    
    // read the instruction offset
    instructionOffset = debugChannel.readIntValue();
    
    // read the new instruction
    newInstruction = debugChannel.readIntValue();
    
    debugPrintln("Method body before:");
    dumpMethodBody(methodStructPointer);
    
    // clear the breakpoint
    result = clearBreakPoint(methodStructPointer, instructionOffset, newInstruction);
    
    debugPrintln("Method body after:");
    dumpMethodBody(methodStructPointer);
    
    // inform if execution was successful or not.
    if(result != INVALID_INSTRUCTION)
    {
      debugChannel.sendReply();
      return true;
    }
    else
    {
      debugChannel.sendReplyWithErrorCode(ErrorConstants.ERROR_INVALID_LOCATION);
      return false;
    }
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
    
    debugPrint(" Frame index: " );
    debugPrint(frameIndex);
    
    int pointer;
    
    pointer = getFramePointerAtIndex(frameIndex + 1);
    pointer = getMPFromFrame(pointer);
    
    debugPrint("  Method pointer: ");
    debugPrintln(pointer);
    
    writeInt(pointer);
  }
  
  /**
   * Handle a "Get local variable" command.
   * 
   * @throws IOException
   */
  private static void handleGetLocalVariableCommand() throws IOException
  {
    int frameIndex;
    int fieldIndex;
    int value;
    int pointer;
    
    // skip the thread index for now...
    debugChannel.skipBytes(4);
    
    // read the frame index
    frameIndex = debugChannel.readIntValue();
    
    // skip the number of variables for now. Assume just one.
    debugChannel.skipBytes(4);
    
    // read the field index
    fieldIndex = debugChannel.readIntValue();
    
    
    debugPrint(" Frame index: " );
    debugPrint(frameIndex);
    debugPrint(" Variable index: " );
    debugPrintln(fieldIndex);
    
    pointer = getFramePointerAtIndex(frameIndex);
    value = getLocalVariable(pointer, fieldIndex);
    
    debugPrint("  Value: ");
    debugPrintln(value);
    debugPrint(" Pointer: ");
    debugPrintln(pointer);
    
    debugChannel.sendReplyGetLocalVariable(value);
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
    int frameIndex;
    int fieldIndex;
    int slotId;
    int value;
    int pointer;
    boolean error = false;
    
    // just to make the compier happy;)
    value = 0;
    
    // skip the thread ID for now
    debugChannel.skipInt();
    
    frameIndex = debugChannel.readIntValue();
    
    // skip the number of variables for now. Assume 1 always.
    debugChannel.skipInt();
    
    // get the field index
    fieldIndex = debugChannel.readIntValue();
    
    // get the frame pointer
    pointer = getFramePointerAtIndex(frameIndex);
    
    // get the field type
    slotId = debugChannel.readByteValue();
    
    debugPrint(" Frame index: " );
    debugPrint(frameIndex);
    debugPrint(" Variable index: " );
    debugPrint(fieldIndex);
    debugPrint(" Slot ID: " );
    debugPrint(slotId);
    debugPrint(" Pointer: " );
    debugPrintln(pointer);
    
    switch (slotId)
    {
      // 1 byte:
      case TagConstants.BOOLEAN:
      case TagConstants.BYTE:
      {
        debugPrintln("1 byte: ");
        
        value = debugChannel.readByteValue();
        setLocalVariable(pointer, fieldIndex, value);
        break;
      }
      
      // 2 bytes:
      case TagConstants.CHAR:
      case TagConstants.SHORT:
      {
        debugPrintln("2 bytes: ");
        
        value = debugChannel.readShortValue();
        setLocalVariable(pointer, fieldIndex, value);
        break;
      }
      
      // 4 bytes:
      case TagConstants.OBJECT:
      case TagConstants.ARRAY:
      case TagConstants.STRING:
      case TagConstants.FLOAT:
      case TagConstants.INT:
      {
        debugPrintln("4 bytes: ");
        
        value = debugChannel.readIntValue();
        setLocalVariable(pointer, fieldIndex, value);
        break;
      }
      
      // 8 bytes:
      case TagConstants.DOUBLE:
      case TagConstants.LONG:
      {
        debugPrintln("8 bytes: ");
        
        value = debugChannel.readIntValue();
        setLocalVariable(pointer, fieldIndex, value);
        
        value = debugChannel.readIntValue();
        setLocalVariable(pointer, fieldIndex + 1, value);
        break;
      }
      
      // error: those values should not happen.
      case TagConstants.VOID:
      case TagConstants.THREAD:
      case TagConstants.THREAD_GROUP:
      case TagConstants.CLASS_LOADER:
      case TagConstants.CLASS_OBJECT:
      default:
      {
        debugPrint("Error. ");
        
        error = true;
        break;
      }
    }
    
    if(error == false)
    {
      debugPrint("  Value: ");
      debugPrintln(value);
      debugPrint(" Pointer: ");
      debugPrintln(pointer);
    }
    else
    {
      debugPrintln("  Failure setting a local variable!");
    }
    
    // finally, send a reply to report success or failure.
    if(error)
    {
      debugChannel.sendReplyWithErrorCode(ErrorConstants.ERROR_INVALID_FRAMEID);
    }
    else
    {
      debugChannel.sendReply();
    }
  }
  
  /**
   * Handle a "Get number of local variables" command.
   * 
   * @throws IOException
   */
  private static void handleGetNumberOfLocalVariablesCommand() throws IOException
  {
    int frameIndex;
    int framePointer;
    int numLocals;
    
    frameIndex = debugChannel.readIntValue();
    
    debugPrint(" Frame index: " );
    debugPrint(frameIndex);
    
    framePointer = getFramePointerAtIndex(frameIndex);
    numLocals = getNumLocalsAndParametersFromFrame(framePointer);
    
    debugPrint(" Number of local variables: " );
    debugPrintln(numLocals);
    
    debugChannel.sendReplyFrameCount(numLocals);
  }
  
  /**
   * Test to check if some types of packets are being properly 
   * created.
   * @throws IOException 
   */
  private static void handleTestSendJDWPPackets() throws IOException
  {
    int framePointer = getCurrentFramePointer();
    int numPackets = 3;
    
    debugChannel.sendReplyTestJDWPPackets(numPackets);
    
    debugChannel.sendVMStartEvent();
    sendBreakpointEvent(framePointer);
    debugChannel.sendVMDeathEvent();
  }
  
  /**
   * Another test. 
   * 
   * @throws IOException 
   */
  private static void handleTestReceiveJDWPPackets(int framePointer) throws IOException
  {
    int commandSet, command;
    
    debugChannel.receivePacket();
    
    commandSet = debugChannel.readInputCommandSet();
    command = debugChannel.readInputCommand();
    
    handleJDWPRequest(commandSet, command, framePointer);
  }
  
  /**
   * @param commandSet
   * @param command
   * @throws IOException 
   */
  private static void handleJDWPRequest(int commandSet, int command,
    int framePointer) throws IOException
  {
    if((commandSet == 1) && (command == 10))
    {
      debugPrintln("Exit: stop execution.");
      debugChannel.sendReply();
      
      // TODO: where is the proper place to send a VMDeath event?
      // maybe after the main method return, too? ;)
      debugChannel.sendVMDeathEvent();
      
      System.exit(0);
    }
    
    // calculate the stack depth of the caller method.
    if((commandSet == CommandConstants.ThreadReference_Command_Set) &&
       (command == CommandConstants.ThreadReference_FrameCount))
    {
      debugPrintln("Get stack depth");
      
      // get stack depth of the frame for the breakpoint call
      int frameCount = getStackDepth(framePointer);
      
      debugPrint(" Stack depth of caller method: ");
      debugPrintln(frameCount);

      debugChannel.sendReplyFrameCount(frameCount);
    }
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
    
//    debugPrintln("setBreakPoint(int methodPointer, int instructionOffset)");
    
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
    
//    debugPrintln("clearBreakPoint(int methodStructPointer,int instructionOffset, int newInstruction)");
    
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
    
    debugPrintln("setBreakPoint(int methodPointer, int instructionOffset)");
    dumpMethodStruct(methodStructPointer);
    
    startAddress = getMethodStartAddress(methodStructPointer);
    // beware: method sizes are in words, not in bytes. And 1 word = 4 bytes. 
    methodSize = getMethodSizeInWords(methodStructPointer);
    // calculate the method size in bytes. 
    methodSize *= 4;
    
    // check if the address is inside the method body.
    if(instructionOffset >= 0 && instructionOffset < methodSize)
    {
      instruction = readBytecode(startAddress, instructionOffset);
      debugPrintln("Old instruction: " + instruction);
      
      writeBytecode(newInstruction, startAddress, instructionOffset);

      
//      instructionAddress = startAddress + instructionOffset;
//      
//      instruction = Native.rdMem(instructionAddress);
//      debugPrintln("Old instruction: " + instruction);
//      
//      Native.wrMem(newInstruction, instructionAddress);
    }
    else
    {
      debugPrint("Wrong instruction offset: ");
      debugPrintln(instructionOffset);
      
      debugPrint("Method size:");
      debugPrintln(methodSize);
      
      debugPrintln();
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
  private static int readBytecode(int startAddress, int instructionOffset)
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
    
//    debugPrintln("Word:      " + word);
//    debugPrintln("Remainder: " + index);
//    debugPrintln("address:   " + address);
//    debugPrint("data:      ");
//    printIntHex(data);
//    debugPrintln();
    
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
    
//    debugPrint("Result: ");
//    printIntHex(result);
//    debugPrintln();
    
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
  private static void writeBytecode(int newInstruction, int startAddress, int instructionOffset)
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
    
//    debugPrintln("Word:      " + word);
//    debugPrintln("Remainder: " + index);
//    debugPrintln("address:   " + address);
    debugPrint("data:      ");
    printIntHex(data);
    debugPrintln();
    
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
    
    debugPrint("new data:  ");
    printIntHex(data);
    debugPrintln();
  }
  
  private static void printIntHex(int value)
  {
    EmbeddedOutputStream.printIntHex(value, System.out);
  }
  
  private static void testReadInstruction(int instructionAddress)
  {
    int instruction;
    
    debugPrintln("----------------------------------------");
    instruction = Native.rdMem(instructionAddress + 1);
    debugPrintln("Instruction: " + instruction);
    
    instruction = Native.rdMem(instructionAddress + 1);
    debugPrintln("Instruction: " + instruction);
    
    instruction = Native.rdMem(instructionAddress + 2);
    debugPrintln("Instruction: " + instruction);
    
    instruction = Native.rdMem(instructionAddress + 3);
    debugPrintln("Instruction: " + instruction);
    debugPrintln("----------------------------------------");
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
  private static int getMethodSizeInWords(int methodPointer)
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
    
    debugPrint("Method structure:  ");
    
    data = Native.rdMem(methodPointer);
    EmbeddedOutputStream.printIntHex(data, System.out);
    
    data = Native.rdMem(methodPointer + 1);
    EmbeddedOutputStream.printIntHex(data, System.out);
    
    debugPrintln();
    
    debugPrintln();
    
    debugPrint("Start address: ");
    debugPrintln(getMethodStartAddress(methodPointer));
    
    debugPrint("Size (words):  ");
    debugPrintln(getMethodSizeInWords(methodPointer));
    
    debugPrint("Constant pool: ");
    debugPrintln(getMethodConstantPool(methodPointer));
    
    debugPrint("Local count:   ");
    debugPrintln(getMethodLocalsCount(methodPointer));
    
    debugPrint("Arg count:     ");
    debugPrintln(getMethodArgCount(methodPointer));
    
    debugPrintln();
  }
  
  public static final int getCurrentMethodPointer()
  {
    int data;
    
    // get the current frame pointer
    data = getCurrentFramePointer();
    
    // get the method pointer from the previous method directly from the stack
    data = getMPFromFrame(data);
    
//    debugPrintln("getCurrentMethodPointer()");
    
    return data;
  }
  
  public static final void dumpMethodBody(int methodPointer)
  {
    int index, start, size, data;
    
    start = getMethodStartAddress(methodPointer);
    size = getMethodSizeInWords(methodPointer);
    
    debugPrintln("Method body:");
    debugPrintln();
    
    for(index = 0; index < size; index++)
    {
      data = Native.rdMem(start + index);
      EmbeddedOutputStream.printIntHex(data, System.out);
      debugPrint(" ");
//      if((index & 0x07) == 0 && (index > 0))
      if(((index + 1)% 0x08) == 0)
      {
//        debugPrint("Index:");
//        debugPrintln(index);
        debugPrintln();
      }
    }
    
    debugPrintln();
    debugPrintln();
  }
  
  /**
   * Methods to print the stack content in a way that is
   * easy to inspect.
   */
  private static void prettyPrintStack()
  {
    int framePointer = getCurrentFramePointer();
    prettyPrintStack(framePointer);
  }
  
  /**
   * Methods to print the stack content in a way that is
   * easy to inspect.
   * 
   * @param frame
   * @param previousFrame
   */
  private static void prettyPrintStack(int frame)
  {
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
        debugPrintln("Stack frame for main method (next):");
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
    debugPrintln();
    
    printRegistersFromFrame(frame);
    debugPrintln();
    
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
    
//    debugPrint("  Local pointer: ");
//    debugPrint(localPointer);
//    debugPrint("  Frame pointer: ");
//    debugPrint(frame);
//    debugPrint("  Length: ");
//    debugPrint(length);
//    debugPrintln();
    
    if(length < 0 || length > MAX_LOCAL_VARIABLES)
    {
      debugPrintln("FAILURE!!! wrong local pointer!");
      debugPrint("  Local pointer: ");
      debugPrint(localPointer);
      
      debugPrint("  Frame: ");
      debugPrint(frame);
      
      debugPrint("  Max. variables: ");
      debugPrint(MAX_LOCAL_VARIABLES);
      return;
    }
    
    debugPrintln("Local variables:");
    
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
    
    debugPrint("Previous registers - SP: ");
    value = getSPFromFrame(frame);
    debugPrint(value);
    
    debugPrint("  PC: ");
    value = getPCFromFrame(frame);
    debugPrint(value);
    
    debugPrint("  VP: ");
    value = getVPFromFrame(frame);
    debugPrint(value);
    
    debugPrint("  CP: ");
    value = getCPFromFrame(frame);
    debugPrint(value);
    
    debugPrint("  MP: ");
    value = getMPFromFrame(frame);
    debugPrint(value);
    
    debugPrintln();
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
    
//    debugPrint("Frame: ");
//    debugPrint(frame);
//    debugPrint("  localStackPointer: ");
//    debugPrint(localStackPointer);
//    debugPrint("  previousFrame: ");
//    debugPrint(previousFrame);
//    
//    debugPrint("  length: ");
//    debugPrint(length);
    
    if(length > 0)
    {
      debugPrint("Local execution stack size: ");
      debugPrintln(length);
      printStackArea(localStackPointer, length);
    }
    else
    {
      debugPrintln("Local execution stack: empty");
      // debugPrintln("Length: " + length);
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
      debugPrint(" ");
      
      if(((index + 1)% 0x08) == 0)
      {
//        debugPrint("Index:");
//        debugPrintln(index);
        debugPrintln();
      }
    }
    debugPrintln();
  }
  
  public static void debugPrint(Object data)
  {
    if(shouldPrintInternalMessages)
    {
      System.out.print(data);
    }
  }
  
  public static void debugPrintln(Object data)
  {
    if(shouldPrintInternalMessages)
    {
      System.out.print(data);
      System.out.println();
    }
  }
  
  public static void debugPrint(int data)
  {
    if(shouldPrintInternalMessages)
    {
      System.out.print(data);
    }
  }
  
  public static void debugPrintln(int data)
  {
    if(shouldPrintInternalMessages)
    {
      System.out.println(data);
    }
  }
  
  public static void debugPrintln()
  {
    debugPrintln("");
  }
  
  /**
   * For JDWP support development ONLY.
   * 
   * This method enable internal messages. 
   * It turn on the tracing flag, so internal messages
   * will be sent to the standard output to help during
   * development. 
   */
  private static void enableDevelopmentInternalMessages()
  {
    shouldPrintInternalMessages = true;
  }
  
  /**
   * For JDWP support development ONLY.
   * 
   * This method disable internal messages. 
   * It turn off the tracing flag, so internal messages
   * will *not* be sent to the standard output. 
   */
  private static void disableDevelopmentInternalMessages()
  {
    shouldPrintInternalMessages = true;
  }
  
  /**
   * Inform if should print or not messages related to development.
   *  
   * @return
   */
  public static boolean shouldPrintInternalMessages()
  {
	return shouldPrintInternalMessages;
  }
}
