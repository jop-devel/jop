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

/**
 * InverseConstraint.java
 * 
 * A constraint which invert the result of another constraint.
 * 
 * @author Paulo Abadie Guedes
 *
 * 19/06/2007 - 22:20:48
 * 
 */
public class InverseConstraint implements Constraint
{
  private Constraint constraint;
  
  /**
   * @param type
   */
  public InverseConstraint(Constraint constraint)
  {
    this.constraint = constraint;
  }
  
  /**
   * 
   */
  public boolean satisfyConstraint(Event event)
  {
    return (constraint.satisfyConstraint(event) == false);
  }
}
