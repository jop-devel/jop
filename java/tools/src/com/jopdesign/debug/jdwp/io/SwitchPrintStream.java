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

package com.jopdesign.debug.jdwp.io;

import java.io.PrintStream;

/**
 * A PrintStream which can be switched on or off.
 * 
 * This class models a PrintStream object which 
 * can choose to deliver or ignore each call to 
 * its methods based on the value of an internal flag.
 * 
 * When enabled, the stream will act as a regular stream
 * and will allow data to flow through.
 * When disabled, all write commands will be ignored.
 * 
 * Useful for logging and debugging. It can be used also for every 
 * situation where there is a need for strict control over 
 * what should be delivered or not.
 *  
 * @author Paulo Abadie Guedes
 */
public class SwitchPrintStream extends PrintStream
{
  private boolean isEnabled = false;
  
  public SwitchPrintStream()
  {
    this(System.out);
  }

  public SwitchPrintStream(PrintStream stream)
  {
    super(stream);
  }
  
  public void println()
  {
    if(isEnabled)
    {
      super.println();
    }
  }
  
  public void println(Object object)
  {
    if(isEnabled)
    {
      super.println(object);
    }
  }
  
  public void print(Object object)
  {
    if(isEnabled)
    {
      super.print(object);
    }
  }
  
  public void print(String object)
  {
    if(isEnabled)
    {
      super.print(object);
    }
  }
  
  public void write(int b)
  {
    if(isEnabled)
    {
      super.write(b);
    }    
  }
  
  public void setEnabled(boolean value)
  {
    isEnabled = value;
  }
}
