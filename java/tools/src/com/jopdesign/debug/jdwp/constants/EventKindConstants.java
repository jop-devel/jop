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
 * EventKindConstants.java
 * 
 * 
 * @author Paulo Abadie Guedes
 *
 * 08/06/2007 - 17:12:00
 * 
 */
public class EventKindConstants
{
  public static final byte SINGLE_STEP = 1;
  public static final byte BREAKPOINT = 2;
  public static final byte FRAME_POP = 3;
  public static final byte EXCEPTION = 4;
  public static final byte USER_DEFINED = 5;
  public static final byte THREAD_START = 6;
  
  public static final byte THREAD_END = 7;
  public static final byte THREAD_DEATH = THREAD_END;
  
  public static final byte CLASS_PREPARE = 8;
  public static final byte CLASS_UNLOAD = 9;
  public static final byte CLASS_LOAD = 10;
  public static final byte FIELD_ACCESS = 20;
  public static final byte FIELD_MODIFICATION = 21;
  public static final byte EXCEPTION_CATCH = 30;
  public static final byte METHOD_ENTRY = 40;
  public static final byte METHOD_EXIT = 41;
  
  public static final byte VM_INIT = 90;
  public static final byte VM_START = VM_INIT;
  
  public static final byte VM_DEATH = 99;
  public static final byte VM_DISCONNECTED = 100;

}
