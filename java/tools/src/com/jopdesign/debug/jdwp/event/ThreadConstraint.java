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

package com.jopdesign.debug.jdwp.event;

import com.jopdesign.debug.jdwp.constants.ConstraintKindConstants;

/**
 * ThreadConstraint.java
 * 
 * Constraint events reported to those which are created by an specific Thread.
 * 
 * This constraint has a Thread ID and only accept events which were created
 * by one specific thread. 
 * 
 * @author Paulo Abadie Guedes
 *
 * 18/06/2007 - 20:45:23
 * 
 */

public class ThreadConstraint extends BasicConstraint
{
  private int threadId;
  
  /**
   * @param type
   */
  public ThreadConstraint(byte type, int threadId)
  {
    super(ConstraintKindConstants.ThreadOnly);
    this.threadId = threadId;
  }
  
  /**
   * Check if an event is created by one specific Thread.
   * 
   * 
   */
  public boolean satisfyConstraint(Event event)
  {
    boolean result = false;
    
    if(event.getThreadId() == threadId)
    {
      result = true;
    }
    
    return result;
  }
}
