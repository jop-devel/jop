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

package com.jopdesign.debug.jdwp.util;

import java.util.Enumeration;
import java.util.Hashtable;

import com.jopdesign.debug.jdwp.io.SwitchPrintStream;

/**
 * An object to help during debugging.
 * 
 * When there is plenty of time available and other resources,
 * a symbolic debugger is one of the best tools to find errors.
 * 
 * However, sometimes when there is not much time available 
 * or actions depend upon timely events (such as protocol 
 * timeouts), it may not be possible to use a debugger 
 * directly. Then, a tracer or a simple "println" approach
 * may be really interesting (or even the only way out).
 * 
 * Other approaches such as static analysis are also very interesting.
 * However, what matters is to use the proper tool for each situation.
 * Sometimes more than one tool will do the job, but not always.
 * 
 * @author Paulo Abadie Guedes
 */
public class Debug
{
  private SwitchPrintStream out;
  private Hashtable table;
  
  // the singleton debug object.
  private static Debug debug;  
  
  private Debug()
  {
    out = new SwitchPrintStream();
    out.setEnabled(true);
    table = new Hashtable();
  }
  
  public static void recordLocation(String key)
  {
    Debug debug = getDebug();
    DebugLocation debugLocation;
    if(debug.table.containsKey(key) == false)
    {
      debugLocation = new DebugLocation(key);
      debug.table.put(key, debugLocation);
    }
    debugLocation = (DebugLocation) debug.table.get(key);
    debugLocation.increment();
  }
  
  public static void printExecutionSummary()
  {
    Enumeration elements = debug.table.elements();
    while(elements.hasMoreElements())
    {
      Object next = elements.nextElement();
      println(next);
    }
  }
  
  private static class DebugLocation
  {
    private int count;
    private String id;
    
    public DebugLocation(String id)
    {
      count = 0;
      this.id = id;
    }
    
    public int getCount()
    {
      return count;
    }
    
    public void increment()
    {
      count++;
    }
    public String getId()
    {
      return id;
    }
    
    public String toString()
    {
      return ("  Id: " + id + "  Count: " + count);
    }
  };
  
  private static Debug getDebug()
  {
    if(debug == null)
    {
      debug = new Debug();
    }
    
    return debug;
  }
  
  public static void println(Object data)
  {
    Debug debug = getDebug();
    String name = Thread.currentThread().getName();
    debug.out.print(name);
    debug.out.print(" - ");
    debug.out.println(data);
    debug.out.flush();
  }
  
  public static void println()
  {
    Debug debug = getDebug();
    debug.out.println();
    debug.out.flush();
  }
  
  public static void print(Object data)
  {
    Debug debug = getDebug();
    debug.out.print(data);
    debug.out.flush();
  }
  
  public static void print(int data)
  {
    Debug debug = getDebug();
    debug.out.print(data);
    debug.out.flush();
  }
  
  public static void println(int data)
  {
    Debug debug = getDebug();
    debug.out.println(data);
    debug.out.flush();
  }
  
  public static void setDebugging(boolean value)
  {
    Debug debug = getDebug();
    debug.out.setEnabled(value);
  }
}
