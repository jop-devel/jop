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
 * ThreadStatusConstants.java
 * 
 * Possible status for a Thread object.
 * 
 * @author Paulo Abadie Guedes
 *
 * 08/06/2007 - 13:36:44
 * 
 */
public interface ThreadStatusConstants
{
  public static final int ZOMBIE = 0;
  public static final int RUNNING = 1;
  public static final int SLEEPING = 2;
  public static final int MONITOR = 3;
  public static final int WAIT = 4;
}
