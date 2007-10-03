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

import com.jopdesign.debug.jdwp.util.Util;

/**
 * ConstraintKindConstants.java
 * 
 * Constants for the possible kinds of constraints which may restrict
 * events created by the Java machine.
 * 
 * @author Paulo Abadie Guedes
 *
 * 14/06/2007 - 15:13:18
 * 
 */
public class ConstraintKindConstants
{
  public static final byte Count = 1;
  public static final byte Conditional = 2;
  public static final byte ThreadOnly = 3;
  public static final byte ClassOnly = 4;
  public static final byte ClassMatch = 5;
  public static final byte ClassExclude = 6;
  public static final byte LocationOnly = 7;
  public static final byte ExceptionOnly = 8;
  public static final byte FieldOnly = 9;
  public static final byte Step = 10;
  public static final byte InstanceOnly = 11;
  
  private static final byte INITIAL_KIND_ID = Count;
  private static final byte FINAL_KIND_ID = InstanceOnly;
  
  //------------------------------------------------------------
  // constants to check compatibility
  //------------------------------------------------------------
  
  // compatible with any possible event kind
  private static final byte[] CountCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
    EventKindConstants.BREAKPOINT,
    EventKindConstants.FRAME_POP,
    EventKindConstants.EXCEPTION,
    EventKindConstants.USER_DEFINED,
    EventKindConstants.THREAD_START,

    EventKindConstants.THREAD_END,

    EventKindConstants.CLASS_PREPARE,
    EventKindConstants.CLASS_UNLOAD,
    EventKindConstants.CLASS_LOAD,
    EventKindConstants.FIELD_ACCESS,
    EventKindConstants.FIELD_MODIFICATION,
    EventKindConstants.EXCEPTION_CATCH,
    EventKindConstants.METHOD_ENTRY,
    EventKindConstants.METHOD_EXIT,

    EventKindConstants.VM_INIT,

    EventKindConstants.VM_DEATH,
    EventKindConstants.VM_DISCONNECTED,
  };
  
  // reserved for the future
  private static final byte[] ConditionalCompatibleList = new byte[] {};
//  {
//    EventKindConstants.SINGLE_STEP,
//    EventKindConstants.BREAKPOINT,     
//    EventKindConstants.FRAME_POP,      
//    EventKindConstants.EXCEPTION,      
//    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     
//
//    EventKindConstants.CLASS_PREPARE,  
//    EventKindConstants.CLASS_UNLOAD,   
//    EventKindConstants.CLASS_LOAD,     
//    EventKindConstants.FIELD_ACCESS,   
//    EventKindConstants.FIELD_MODIFICATION,                    
//    EventKindConstants.EXCEPTION_CATCH,
//    EventKindConstants.METHOD_ENTRY,   
//    EventKindConstants.METHOD_EXIT,    
//
//    EventKindConstants.VM_INIT,        
//
//    EventKindConstants.VM_DEATH,
//    EventKindConstants.VM_DISCONNECTED,
//  };

  // compatible with all but CLASS_UNLOAD
  private static final byte[] ThreadOnlyCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
    EventKindConstants.BREAKPOINT,     
    EventKindConstants.FRAME_POP,      
    EventKindConstants.EXCEPTION,      
    EventKindConstants.USER_DEFINED,   
    EventKindConstants.THREAD_START,   

    EventKindConstants.THREAD_END,     
       
    EventKindConstants.CLASS_PREPARE,  
//    EventKindConstants.CLASS_UNLOAD,   
    EventKindConstants.CLASS_LOAD,     
    EventKindConstants.FIELD_ACCESS,   
    EventKindConstants.FIELD_MODIFICATION,                    
    EventKindConstants.EXCEPTION_CATCH,
    EventKindConstants.METHOD_ENTRY,   
    EventKindConstants.METHOD_EXIT,    

    EventKindConstants.VM_INIT,        

    EventKindConstants.VM_DEATH,
    EventKindConstants.VM_DISCONNECTED,
  };
  
  //compatible with all except CLASS_UNLOAD, THREAD_START and THREAD_END
  private static final byte[] ClassOnlyCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
    EventKindConstants.BREAKPOINT,     
    EventKindConstants.FRAME_POP,      
    EventKindConstants.EXCEPTION,      
    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     
       
    EventKindConstants.CLASS_PREPARE,  
//    EventKindConstants.CLASS_UNLOAD,   
    EventKindConstants.CLASS_LOAD,     
    EventKindConstants.FIELD_ACCESS,   
    EventKindConstants.FIELD_MODIFICATION,                    
    EventKindConstants.EXCEPTION_CATCH,
    EventKindConstants.METHOD_ENTRY,   
    EventKindConstants.METHOD_EXIT,    

    EventKindConstants.VM_INIT,        

    EventKindConstants.VM_DEATH,
    EventKindConstants.VM_DISCONNECTED,
  };
  
  // compatible with all except THREAD_START and THREAD_END
  private static final byte[] ClassMatchCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
    EventKindConstants.BREAKPOINT,
    EventKindConstants.FRAME_POP,      
    EventKindConstants.EXCEPTION,      
    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     
       
    EventKindConstants.CLASS_PREPARE,  
    EventKindConstants.CLASS_UNLOAD,   
    EventKindConstants.CLASS_LOAD,     
    EventKindConstants.FIELD_ACCESS,   
    EventKindConstants.FIELD_MODIFICATION,                    
    EventKindConstants.EXCEPTION_CATCH,
    EventKindConstants.METHOD_ENTRY,   
    EventKindConstants.METHOD_EXIT,    

    EventKindConstants.VM_INIT,        

    EventKindConstants.VM_DEATH,
    EventKindConstants.VM_DISCONNECTED,
  };
  
  //compatible with all except THREAD_START and THREAD_END
  private static final byte[] ClassExcludeCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
    EventKindConstants.BREAKPOINT,     
    EventKindConstants.FRAME_POP,      
    EventKindConstants.EXCEPTION,      
    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     

    EventKindConstants.CLASS_PREPARE,  
    EventKindConstants.CLASS_UNLOAD,   
    EventKindConstants.CLASS_LOAD,     
    EventKindConstants.FIELD_ACCESS,   
    EventKindConstants.FIELD_MODIFICATION,                    
    EventKindConstants.EXCEPTION_CATCH,
    EventKindConstants.METHOD_ENTRY,   
    EventKindConstants.METHOD_EXIT,    

    EventKindConstants.VM_INIT,        

    EventKindConstants.VM_DEATH,
    EventKindConstants.VM_DISCONNECTED,
  };
  
  // compatible only with breakpoint, field access, field modification, step, 
  // and exception event kinds.  
  private static final byte[] LocationOnlyCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
    EventKindConstants.BREAKPOINT,     
//    EventKindConstants.FRAME_POP,      
    EventKindConstants.EXCEPTION,      
//    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     
//
//    EventKindConstants.CLASS_PREPARE,  
//    EventKindConstants.CLASS_UNLOAD,   
//    EventKindConstants.CLASS_LOAD,     
    EventKindConstants.FIELD_ACCESS,   
    EventKindConstants.FIELD_MODIFICATION,                    
//    EventKindConstants.EXCEPTION_CATCH,
//    EventKindConstants.METHOD_ENTRY,   
//    EventKindConstants.METHOD_EXIT,    
//
//    EventKindConstants.VM_INIT,        
//       
//    EventKindConstants.VM_DEATH,
//    EventKindConstants.VM_DISCONNECTED,
  };
  
  // This modifier can be used with exception event kinds only.  
  private static final byte[] ExceptionOnlyCompatibleList = new byte[]
  {
//    EventKindConstants.SINGLE_STEP,
//    EventKindConstants.BREAKPOINT,     
//    EventKindConstants.FRAME_POP,      
    EventKindConstants.EXCEPTION,      
//    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     
//       
//    EventKindConstants.CLASS_PREPARE,  
//    EventKindConstants.CLASS_UNLOAD,   
//    EventKindConstants.CLASS_LOAD,     
//    EventKindConstants.FIELD_ACCESS,   
//    EventKindConstants.FIELD_MODIFICATION,                    
//    EventKindConstants.EXCEPTION_CATCH,
//    EventKindConstants.METHOD_ENTRY,   
//    EventKindConstants.METHOD_EXIT,    
//
//    EventKindConstants.VM_INIT,        
//       
//    EventKindConstants.VM_DEATH,
//    EventKindConstants.VM_DISCONNECTED,
  };
  
  // This modifier can be used with field access and field modification 
  // event kinds only.  
  private static final byte[] FieldOnlyCompatibleList = new byte[]
  {
//    EventKindConstants.SINGLE_STEP,
//    EventKindConstants.BREAKPOINT,     
//    EventKindConstants.FRAME_POP,      
//    EventKindConstants.EXCEPTION,      
//    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     
//       
//
//    EventKindConstants.CLASS_PREPARE,  
//    EventKindConstants.CLASS_UNLOAD,   
//    EventKindConstants.CLASS_LOAD,     
    EventKindConstants.FIELD_ACCESS,   
    EventKindConstants.FIELD_MODIFICATION,                    
//    EventKindConstants.EXCEPTION_CATCH,
//    EventKindConstants.METHOD_ENTRY,   
//    EventKindConstants.METHOD_EXIT,    
//
//    EventKindConstants.VM_INIT,        
//
//    EventKindConstants.VM_DEATH,
//    EventKindConstants.VM_DISCONNECTED,
  };
  
  // This modifier can be used with step event kinds only.
  private static final byte[] StepCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
//    EventKindConstants.BREAKPOINT,     
//    EventKindConstants.FRAME_POP,      
//    EventKindConstants.EXCEPTION,      
//    EventKindConstants.USER_DEFINED,   
//    EventKindConstants.THREAD_START,   
//
//    EventKindConstants.THREAD_END,     
//       
//
//    EventKindConstants.CLASS_PREPARE,  
//    EventKindConstants.CLASS_UNLOAD,   
//    EventKindConstants.CLASS_LOAD,     
//    EventKindConstants.FIELD_ACCESS,   
//    EventKindConstants.FIELD_MODIFICATION,                    
//    EventKindConstants.EXCEPTION_CATCH,
//    EventKindConstants.METHOD_ENTRY,   
//    EventKindConstants.METHOD_EXIT,    
//
//    EventKindConstants.VM_INIT,        
//
//    EventKindConstants.VM_DEATH,
//    EventKindConstants.VM_DISCONNECTED,
  };

  // This modifier can be used with any event kind except class prepare, 
  // class unload, thread start, and thread end.
  private static final byte[] InstanceOnlyCompatibleList = new byte[]
  {
    EventKindConstants.SINGLE_STEP,
    EventKindConstants.BREAKPOINT,
    EventKindConstants.FRAME_POP,
    EventKindConstants.EXCEPTION,
    EventKindConstants.USER_DEFINED,
//    EventKindConstants.THREAD_START,
//
//    EventKindConstants.THREAD_END,
    

//    EventKindConstants.CLASS_PREPARE,
//    EventKindConstants.CLASS_UNLOAD,
    EventKindConstants.CLASS_LOAD,
    EventKindConstants.FIELD_ACCESS,
    EventKindConstants.FIELD_MODIFICATION,
    EventKindConstants.EXCEPTION_CATCH,
    EventKindConstants.METHOD_ENTRY,
    EventKindConstants.METHOD_EXIT,

    EventKindConstants.VM_INIT,

    EventKindConstants.VM_DEATH,
    EventKindConstants.VM_DISCONNECTED,
  };
  
  public static boolean isValidConstraintKind(byte kind)
  {
    boolean result = false;
    
    result = ((INITIAL_KIND_ID <= kind) && (kind <= FINAL_KIND_ID));
    
    return result;
  }
  
  public static boolean isCompatibleWithEventKind(byte eventKind,
      byte constraintKind)
  {
    boolean result = false;

    switch(constraintKind)
    {
      case Count: // 1
      {
        result = isCompatibleWithCount(eventKind);
        break;
      }
      case Conditional: // 2
      {
        result = isCompatibleWithConditional(eventKind);
        break;
      }
      case ThreadOnly: // 3
      {
        result = isCompatibleWithThreadOnly(eventKind);
        break;
      }
      case ClassOnly: // 4
      {
        result = isCompatibleWithClassOnly(eventKind);
        break;
      }
      case ClassMatch: // 5
      {
        result = isCompatibleWithClassMatch(eventKind);
        break;
      }
      case ClassExclude: // 6
      {
        result = isCompatibleWithClassExclude(eventKind);
        break;
      }
      case LocationOnly: // 7
      {
        result = isCompatibleWithLocationOnly(eventKind);
        break;
      }
      case ExceptionOnly: // 8
      {
        result = isCompatibleWithExceptionOnly(eventKind);
        break;
      }
      case FieldOnly: // 9
      {
        result = isCompatibleWithFieldOnly(eventKind);
        break;
      }
      case Step: // 10
      {
        result = isCompatibleWithStep(eventKind);
        break;
      }
      case InstanceOnly: // 11
      {
        result = isCompatibleWithInstanceOnly(eventKind);
        break;
      }

      default:
      {
        // wrong constraint type. Hence, incompatible.
        result = false;
        break;
      }
    }

    return result;
  }
  
  public static boolean isCompatibleWithCount(byte eventKind){return Util.contains(           CountCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithConditional(byte eventKind){return Util.contains(     ConditionalCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithThreadOnly(byte eventKind){return Util.contains(      ThreadOnlyCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithClassOnly(byte eventKind){return Util.contains(       ClassOnlyCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithClassMatch(byte eventKind){return Util.contains(      ClassMatchCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithClassExclude(byte eventKind){return Util.contains(    ClassExcludeCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithLocationOnly(byte eventKind){return Util.contains(    LocationOnlyCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithExceptionOnly(byte eventKind){return Util.contains(   ExceptionOnlyCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithFieldOnly(byte eventKind){return Util.contains(       FieldOnlyCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithStep(byte eventKind){return Util.contains(            StepCompatibleList            ,eventKind);}
  public static boolean isCompatibleWithInstanceOnly(byte eventKind){return Util.contains(    InstanceOnlyCompatibleList            ,eventKind);}
}
