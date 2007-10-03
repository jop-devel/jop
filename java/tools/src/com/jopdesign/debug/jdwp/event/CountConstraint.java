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
 * CountConstraint.java
 * 
 * A constraint with a counter.
 * 
 * Everytime a new event is created, the constraint reject it and reduce
 * the counter. When the counter reaches zero, the constraint accept
 * the event.
 * 
 * Then the constraint is disabled. All events after the accepted one 
 * are then rejected.  
 * 
 * @author Paulo Abadie Guedes
 *
 * 18/06/2007 - 20:31:23
 * 
 */
public class CountConstraint extends BasicConstraint
{
  private int counter;
  
  /**
   * Build a constraint which has a counter.
   */
  public CountConstraint(byte type, int counter)
  {
    super(ConstraintKindConstants.Count);
    this.counter = counter;
  }
  
  public int getCounter()
  {
    return counter;
  }
  
  public void decrementCounter()
  {
    if(counter > -1)
    {
      counter --;
    }
    
    if(counter < 0)
    {
      setInvalid();
    }
  }
  
  /**
   * Check if an event satisfy a constraint.
   * 
   * The default behaviour is to reject all events and decrement the
   * counter until it reaches zero. When this happens, accept the event and
   * after this, invalidate the constraint.
   */
  public boolean satisfyConstraint(Event event)
  {
    boolean result = false;
    
    if(isValid() && getCounter() == 0)
    {
      result = true;
    }
    decrementCounter();
    
    return result;
  }
}
