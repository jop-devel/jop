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

package com.jopdesign.debug.jdwp.jop;

/**
 * JopLauncher.java
 * 
 * This interface should be implemented by classes which 
 * will be responsible to launch an instance of a JOP
 * machine.
 * 
 * The communication channel with the machine will be 
 * abstracted by the standard input and output streams.
 * 
 * @author Paulo Abadie Guedes
 *
 * 01/06/2007 - 17:49:58
 * 
 */
public interface JopLauncher
{
  // currently available launchers.
  // for now, only JopSim. Add at least one more in the future.
  //public static String[] AVAILABLE_JOP_LAUNCHERS= {"com.jopdesign.tools.JopSim"};
  public static String[] AVAILABLE_JOP_LAUNCHERS= {"com.jopdesign.debug.jdwp.jop.JopSimLauncher"};
  
  
  public static String JOP_LAUNCHER_PREFIX = "jopLauncher:";
  public static String DEFAULT_JOP_LAUNCHER = AVAILABLE_JOP_LAUNCHERS[0];
  public static String DEFAULT_SETTINGS_JOP_LAUNCHER = 
    JOP_LAUNCHER_PREFIX + DEFAULT_JOP_LAUNCHER;
  
  // launch an instance of a JOP machine (e.g. JopSim or a real JOP processor).
  // this call should not return until the machine has finished execution.
  public void launchJop(String[] args);
  
  // return the communication channels to the machine
//  public InputStream getInputStream();
//  public PrintStream getOutputStream();
}
