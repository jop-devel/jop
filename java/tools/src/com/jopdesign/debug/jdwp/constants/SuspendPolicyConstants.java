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
 * SuspendPolicyConstants.java
 * 
 * 
 * @author Paulo Abadie Guedes
 *
 * 12/06/2007 - 17:41:24
 * 
 */
public interface SuspendPolicyConstants
{
  // Suspend no threads when this event is encountered.
  public static final byte NONE = 0;
  
  // Suspend the event thread when this event is encountered.
  public static final byte EVENT_THREAD = 1;
  
  // Suspend all threads when this event is encountered.
  public static final byte ALL = 2;
}
