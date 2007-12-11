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

package debug.constants;

/**
 * CommandConstants.java
 * 
 *  One class to hold constants related to JDWP commands.
 *  This is a short version of the desktop "CommandConstants" class.
 * 
 * @author Paulo Abadie Guedes
 *
 * 16/05/2007 - 16:48:52
 * 
 */
public class CommandConstants
{
  //  Well, really need a simple way to share some code 
  // between the server and the client side...
  
  // Event Command Set (64)
  public static final int Event_Command_Set = 64;
  
  // the Event Composite command
  public static final int Event_Composite = 100;
}
