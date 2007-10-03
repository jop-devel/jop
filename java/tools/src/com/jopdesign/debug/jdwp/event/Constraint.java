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
 * Constraint.java
 * 
 * An interface to model a constraint to be applied to an event.
 * 
 * Classes which implement this interface can decide if an Event
 * object satisfy some given constraint or not.
 * 
 * @author Paulo Abadie Guedes
 *
 * 14/06/2007 - 11:49:12
 * 
 */
public interface Constraint
{
  // this method's purpose is to check if a given event satisfy a constraint.
  public boolean satisfyConstraint(Event event);
  
//  // this method check if a constraint is still valid. At least one constraint
//  //(count) has internal state and can become invalid.
//  public boolean isValid();
}
