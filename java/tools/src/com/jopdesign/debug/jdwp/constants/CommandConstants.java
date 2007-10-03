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

package com.jopdesign.debug.jdwp.constants;


/**
 * One class to hold constants related to JDWP commands.
 * 
 * It was an interface at first, but it is better to hide the internal 
 * representation of the tables and just expose the services
 * needed to query descriptions.
 * 
 * @author Paulo Abadie Guedes
 *
 */
public class CommandConstants
{
  //------------------------------------------------------------
  // constants for the command sets
  //------------------------------------------------------------
  public static final int VirtualMachine_Command_Set = 1;
  public static final int ReferenceType_Command_Set = 2;
  public static final int ClassType_Command_Set = 3;
  public static final int ArrayType_Command_Set = 4;
  public static final int InterfaceType_Command_Set = 5;
  public static final int Method_Command_Set = 6;

  public static final int Field_Command_Set = 8;
  public static final int ObjectReference_Command_Set = 9;
  public static final int StringReference_Command_Set = 10;
  public static final int ThreadReference_Command_Set = 11;
  public static final int ThreadGroupReference_Command_Set = 12;
  public static final int ArrayReference_Command_Set = 13;
  public static final int ClassLoaderReference_Command_Set = 14;
  public static final int EventRequest_Command_Set = 15;
  public static final int StackFrame_Command_Set = 16;
  public static final int ClassObjectReference_Command_Set = 17;

  public static final int Event_Command_Set = 64;
  
  // constants with the maximum values for command sets and 
  // commands (first command set is the biggest:19)
  // but use 100 to consider Composite events.
  
  public static final int MAX_COMMAND_SET_INDEX = 64;
//  public static final int MAX_COMMAND_INDEX = 19;
  public static final int MAX_COMMAND_INDEX = 100;
  
  //------------------------------------------------------------  
  // constants for the commands inside each set
  //------------------------------------------------------------
  
  // VirtualMachine Command Set (1)
  public static final int VirtualMachine_Version = 1;
  public static final int VirtualMachine_ClassesBySignature = 2;
  public static final int VirtualMachine_AllClasses = 3;
  public static final int VirtualMachine_AllThreads = 4;
  public static final int VirtualMachine_TopLevelThreadGroups = 5;
  public static final int VirtualMachine_Dispose = 6;
  public static final int VirtualMachine_IDSizes = 7;
  public static final int VirtualMachine_Suspend = 8;
  public static final int VirtualMachine_Resume = 9;
  public static final int VirtualMachine_Exit = 10;
  public static final int VirtualMachine_CreateString = 11;
  public static final int VirtualMachine_Capabilities = 12;
  public static final int VirtualMachine_ClassPaths = 13;
  public static final int VirtualMachine_DisposeObjects = 14;
  public static final int VirtualMachine_HoldEvents = 15;
  public static final int VirtualMachine_ReleaseEvents = 16;
  public static final int VirtualMachine_CapabilitiesNew = 17;
  public static final int VirtualMachine_RedefineClasses = 18;
  public static final int VirtualMachine_SetDefaultStratum = 19;
  
  // ReferenceType Command Set (2)
  public static final int ReferenceType_Signature = 1;
  public static final int ReferenceType_ClassLoader = 2;
  public static final int ReferenceType_Modifiers = 3;
  public static final int ReferenceType_Fields = 4;
  public static final int ReferenceType_Methods = 5;
  public static final int ReferenceType_GetValues = 6;
  public static final int ReferenceType_SourceFile = 7;
  public static final int ReferenceType_NestedTypes = 8;
  public static final int ReferenceType_Status = 9;
  public static final int ReferenceType_Interfaces = 10;
  public static final int ReferenceType_ClassObject = 11;
  public static final int ReferenceType_SourceDebugExtension = 12;

  // ClassType Command Set (3)
  public static final int ClassType_Superclass = 1;
  public static final int ClassType_SetValues = 2;
  public static final int ClassType_InvokeMethod = 3;
  public static final int ClassType_NewInstance = 4;

  // ArrayType Command Set (4)
  public static final int ArrayType_NewInstance = 1;
  
  // InterfaceType Command Set (5)
  // none
  
  // Method Command Set (6)
  public static final int Method_LineTable = 1;
  public static final int Method_VariableTable = 2;
  public static final int Method_Bytecodes = 3;
  public static final int Method_IsObsolete = 4;
  
  // Field Command Set (8)
  // none
  
  // ObjectReference Command Set (9)
  public static final int ObjectReference_ReferenceType = 1;
  public static final int ObjectReference_GetValues = 2;
  public static final int ObjectReference_SetValues = 3;
  // none here: no command with code "4" on this set.
  public static final int ObjectReference_MonitorInfo = 5;
  public static final int ObjectReference_InvokeMethod = 6;
  public static final int ObjectReference_DisableCollection = 7;
  public static final int ObjectReference_EnableCollection = 8;
  public static final int ObjectReference_IsCollected = 9;

  // StringReference Command Set (10)
  public static final int StringReference_Value = 1;

  // ThreadReference Command Set (11)
  public static final int ThreadReference_Name = 1;
  public static final int ThreadReference_Suspend = 2;
  public static final int ThreadReference_Resume = 3;
  public static final int ThreadReference_Status = 4;
  public static final int ThreadReference_ThreadGroup = 5;
  public static final int ThreadReference_Frames = 6;
  public static final int ThreadReference_FrameCount = 7;
  public static final int ThreadReference_OwnedMonitors = 8;
  public static final int ThreadReference_CurrentContendedMonitor = 9;
  public static final int ThreadReference_Stop = 10;
  public static final int ThreadReference_Interrupt = 11;
  public static final int ThreadReference_SuspendCount = 12;

  // ThreadGroupReference Command Set (12)
  public static final int ThreadGroupReference_Name = 1;
  public static final int ThreadGroupReference_Parent = 2;
  public static final int ThreadGroupReference_Children = 3;

  // ArrayReference Command Set (13)
  public static final int ArrayReference_Length = 1;
  public static final int ArrayReference_GetValues = 2;
  public static final int ArrayReference_SetValues = 3;

  // ClassLoaderReference Command Set (14)
  public static final int ClassLoaderReference_VisibleClasses = 1;

  // EventRequest Command Set (15)
  public static final int EventRequest_Set = 1;
  public static final int EventRequest_Clear = 2;
  public static final int EventRequest_ClearAllBreakpoints = 3;

  // StackFrame Command Set (16)
  public static final int StackFrame_GetValues = 1;
  public static final int StackFrame_SetValues = 2;
  public static final int StackFrame_ThisObject = 3;
  public static final int StackFrame_PopFrames = 4;

  // ClassObjectReference Command Set (17)
  public static final int ClassObjectReference_ReflectedType = 1;

  // Event Command Set (64)
  public static final int Event_Composite = 100;
  
  //------------------------------------------------------------
  // End of constants to identify the commands inside sets.
  // There are a total of 77 distinct commands, including
  // the "Event_Composite" used to report events. 
  //------------------------------------------------------------
  
  /**
   * The set description is build this way so it's easy to access the
   * description using the set ID.
   */
  private static final String[] commandSetDescription = 
  {
      "",
      "VirtualMachine Command Set (1)",
      "ReferenceType Command Set (2)",
      "ClassType Command Set (3)",
      "ArrayType Command Set (4)",
      "InterfaceType Command Set (5)",
      "Method Command Set (6)",
      "",
      "Field Command Set (8)",
      "ObjectReference Command Set (9)",
      "StringReference Command Set (10)",
      "ThreadReference Command Set (11)",
      "ThreadGroupReference Command Set (12)",
      "ArrayReference Command Set (13)",
      "ClassLoaderReference Command Set (14)",
      "EventRequest Command Set (15)",
      "StackFrame Command Set (16)",
      "ClassObjectReference Command Set (17)",
      "", "","",
      "","","","","","","","","","",
      "","","","","","","","","","",
      "","","","","","","","","","",
      "","","","","","","","","","",
      "","","",
      "Event Command Set (64)"    
  };

  //----------------------------------------------------------------------
  
   // For each command set, there is a set of descriptions of its
   // respective commands.
   
  private static final String[] VirtualMachineCommandSet = 
  {
    "",
    "Version (1)",
    "ClassesBySignature (2)",
    "AllClasses (3)",
    "AllThreads (4)",
    "TopLevelThreadGroups (5)",
    "Dispose (6)",
    "IDSizes (7)",
    "Suspend (8)",
    "Resume (9)",
    "Exit (10)",
    "CreateString (11)",
    "Capabilities (12)",
    "ClassPaths (13)",
    "DisposeObjects (14)",
    "HoldEvents (15)",
    "ReleaseEvents (16)",
    "CapabilitiesNew (17)",
    "RedefineClasses (18)",
    "SetDefaultStratum (19)"
  };

  private static final String[] ReferenceTypeCommandSet = 
  {
    "",
    "Signature (1)",
    "ClassLoader (2)",
    "Modifiers (3)",
    "Fields (4)",
    "Methods (5)",
    "GetValues (6)",
    "SourceFile (7)",
    "NestedTypes (8)",
    "Status (9)",
    "Interfaces (10)",
    "ClassObject (11)",
    "SourceDebugExtension (12)"
  };

  private static final String[] ClassTypeCommandSet =
  {
    "",
    "Superclass (1)",
    "SetValues (2)",
    "InvokeMethod (3)",
    "NewInstance (4)"
  };

  private static final String[] ArrayTypeCommandSet = 
  {
    "",
    "NewInstance (1)"
  };

  private static final String[] InterfaceTypeCommandSet = {""};

  private static final String[] MethodCommandSet = 
  {
    "",
    "LineTable (1)",
    "VariableTable (2)",
    "Bytecodes (3)",
    "IsObsolete (4)"
  };

  private static final String[] FieldCommandSet = {""};

  private static final String[] ObjectReferenceCommandSet =
  {
    "",
    "ReferenceType (1)",
    "GetValues (2)",
    "SetValues (3)",
    "",
    "MonitorInfo (5)",
    "InvokeMethod (6)",
    "DisableCollection (7)",
    "EnableCollection (8)",
    "IsCollected (9)"
  };

  private static final String[] StringReferenceCommandSet =
  {
    "",
    "Value (1)"
  };

  private static final String[] ThreadReferenceCommandSet =
  {
    "",
    "Name (1)",
    "Suspend (2)",
    "Resume (3)",
    "Status (4)",
    "ThreadGroup (5)",
    "Frames (6)",
    "FrameCount (7)",
    "OwnedMonitors (8)",
    "CurrentContendedMonitor (9)",
    "Stop (10)",
    "Interrupt (11)",
    "SuspendCount (12)"
  };

  private static final String[] ThreadGroupReferenceCommandSet =
  {
    "",
    "Name (1)",
    "Parent (2)",
    "Children (3)"
  };

  private static final String[] ArrayReferenceCommandSet =
  {
    "",
    "Length (1)",
    "GetValues (2)",
    "SetValues (3)"
  };

  private static final String[] ClassLoaderReferenceCommandSet =
  {
    "",
    "VisibleClasses (1)"
  };

  private static final String[] EventRequestCommandSet = 
  {
    "",
    "Set (1)",
    "Clear (2)",
    "ClearAllBreakpoints (3)"
  };

  private static final String[] StackFrameCommandSet = 
  {
    "",
    "GetValues (1)",
    "SetValues (2)",
    "ThisObject (3)",
    "PopFrames (4)"
  };

  private static final String[] ClassObjectReferenceCommandSet = 
  {
    "",
    "ReflectedType (1)"
  };

  private static final String[] EventCommandSet = 
  {
    "",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","","",
    "","","","","","","","","",
    "Composite (100)"
  };
  
  //----------------------------------------------------------------------
  
  private static final String[][] commandDescription = 
  {
    {""},
    VirtualMachineCommandSet,
    ReferenceTypeCommandSet,
    ClassTypeCommandSet,
    ArrayTypeCommandSet,
    InterfaceTypeCommandSet,
    MethodCommandSet,
    {""},
    FieldCommandSet,
    ObjectReferenceCommandSet,
    StringReferenceCommandSet,
    ThreadReferenceCommandSet,
    ThreadGroupReferenceCommandSet,
    ArrayReferenceCommandSet,
    ClassLoaderReferenceCommandSet,
    EventRequestCommandSet,
    StackFrameCommandSet,
    ClassObjectReferenceCommandSet,
    {""}, {""},{""},
    {""},{""},{""},{""},{""},{""},{""},{""},{""},{""},
    {""},{""},{""},{""},{""},{""},{""},{""},{""},{""},
    {""},{""},{""},{""},{""},{""},{""},{""},{""},{""},
    {""},{""},{""},{""},{""},{""},{""},{""},{""},{""},
    {""},{""},{""},
    EventCommandSet    
  };

  private static String[] getStringArray(String[][] list, int index)
  {
    String[] result;
    if(index < 0 || index >= list.length)
    {
      result = new String[] {"  Error: index out of bounds. Index: " + index};
    }
    else
    {
      result = list[index];
    }
    return result;
  }

  private static String getString(String[] list, int index)
  {
    String result;
    if(index < 0 || index >= list.length)
    {
      result = "  Error: index out of bounds. Index: " + index;
    }
    else
    {
      result = list[index];
    }
    return result;
  }

  public static String getCommandDescription(int set, int index)
  {
    String[] list = getStringArray(commandDescription, set);
    String result = getString(list, index);
    return result;
  }

  public static String getSetDescription(int index)
  {
    String result = getString(commandSetDescription, index);
    return result;
  }
}
