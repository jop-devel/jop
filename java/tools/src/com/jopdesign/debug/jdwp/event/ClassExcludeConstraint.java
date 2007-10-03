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
 * ClassExcludeConstraint.java
 * 
 * Restricts reported events to those for classes whose name 
 * does not match the given restricted regular expression.
 * 
 * Matches are limited to exact matches of the given class pattern 
 * and matches of patterns that begin or end with '*'; 
 * for example, "*.Foo" or "java.*".  
 * 
 * @author Paulo Abadie Guedes
 *
 * 19/06/2007 - 22:27:45
 * 
 */
public class ClassExcludeConstraint implements Constraint
{
  private Constraint constraint;
  
  public ClassExcludeConstraint(String classPattern)
  {
    Constraint matchConstraint = new ClassMatchConstraint(classPattern);
    Constraint excludeConstraint = new InverseConstraint(matchConstraint);
    
    this.constraint = excludeConstraint;
  }
  
  /**
   * Check if an event does not match a given string pattern.
   */
  public boolean satisfyConstraint(Event event)
  {
    return constraint.satisfyConstraint(event);
  }
}
