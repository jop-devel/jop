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
 * ClassMatchConstraint.java
 * 
 * Constraint events reported to those which are created by a
 * class which match an specific string pattern.
 * 
 * Matches are limited to exact matches of the given class pattern 
 * and matches of patterns that begin or end with '*'; 
 * for example, "*.Foo" or "java.*".
 * 
 * @author Paulo Abadie Guedes
 *
 * 19/06/2007 - 22:04:35
 * 
 */
public class ClassMatchConstraint extends BasicConstraint
{
  private String classPattern;
  
  /**
   * Create a constraint which accept only events from a given class
   * or set of classes.
   * 
   * @param type
   * @param classPattern
   */
  public ClassMatchConstraint(String classPattern)
  {
    super(ConstraintKindConstants.ClassMatch);
    
    if(classPattern == null)
    {
      classPattern = "*";
    }
    
    this.classPattern = classPattern;
  }

  /**
   * Check if the event happened into a class whose name match 
   * a specified pattern.
   */
  public boolean satisfyConstraint(Event event)
  {
    boolean result;
    
    String className = event.getClassName();
    result = matchPattern(className);
    
    return result;
  }
  
  /**
   * Check if the class name satisfy the pattern.
   * Pattens may be only an exact match, strings which begin
   * with * or strings which end with *.
   * 
   * @param className
   * @return
   */
  private boolean matchPattern(String className)
  {
    boolean result = false;
    
    if(classPattern.startsWith("*"))
    {
      String tail = classPattern.substring(1);
      result = className.endsWith(tail);
    }
    else
    {
      if(classPattern.endsWith("*"))
      {
        int size = className.length();
        String head = classPattern.substring(0, size - 1);
        result = className.startsWith(head);
      }
      else
      {
        result = className.equals(classPattern);
      }
    }
    
    return result;
  }
}
