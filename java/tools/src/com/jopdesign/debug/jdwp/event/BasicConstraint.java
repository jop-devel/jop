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
 * BasicConstraint.java
 * 
 * A base class for all possible kinds of constraints which can
 * be used to filter JDWP events.
 * Constraints are used to restrict which events should be sent
 * or not from the Java machine to the debugger.
 * 
 * Other specific constraints inherit from this class.
 * 
 * @author Paulo Abadie Guedes
 *
 * 15/06/2007 - 12:50:15
 * 
 */
public abstract class BasicConstraint implements Constraint
{
  private byte constraintType;
  private boolean valid;
  
  /**
   * Create a basic constraint with the given type.
   * 
   * If the type is not valid, set it to a valid type and
   * mark the constraint as invalid.
   * 
   * @param type
   */
  public BasicConstraint(byte type)
  {
    if(ConstraintKindConstants.isValidConstraintKind(type))
    {
      valid = true;
      constraintType = type;
    }
    else
    {
      valid = false;
      constraintType = ConstraintKindConstants.Count;      
    }
  }
  
  /**
   * Check if an event satisfy a constraint.
   * 
   * The default behaviour is to restrict nothing, if the constraint it valid.
   * So, if it's valid return true by default. Return false otherwise,
   * without checking anything.
   * 
   * Subclasses should override this method to provide specific
   * behaviour for the constraint test.
   */
//  public boolean satisfyConstraint(Event event)
//  {
//    boolean result = true;
//    
//    if(isValid() == false)
//    {
//      result = false;
//    }
//    
//    return result;
//  }
  
  /**
   * Check if the constraint is still valid, and hence,if it can be used.
   * @return
   */
  public boolean isValid()
  {
    return valid;
  }
  
  /**
   * Set the constraint as invalid.
   */
  public void setInvalid()
  {
    valid = false;
  }
  
  /**
   * Return the constraint type.
   * 
   * @return
   */
  public byte getConstraintType()
  {
    return constraintType;
  }
}
