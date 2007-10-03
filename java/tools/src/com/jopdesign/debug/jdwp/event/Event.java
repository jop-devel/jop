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

import com.jopdesign.debug.jdwp.model.Location;


/**
 * Event.java
 * 
 * 
 * @author Paulo Abadie Guedes
 *
 * 14/06/2007 - 11:47:16
 * 
 */
public class Event
{
  private byte suspendPolicy;
  private byte eventKind;
  
  private int threadId;
  private Location location;
  private String className;
  
  public String getClassName()
  {
    return className;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public Event(byte kind)
  {
    this.eventKind = kind;
  }

  public byte getEventKind()
  {
    return eventKind;
  }

  public void setEventKind(byte eventKind)
  {
    this.eventKind = eventKind;
  }

  public byte getSuspendPolicy()
  {
    return suspendPolicy;
  }

  public void setSuspendPolicy(byte suspendPolicy)
  {
    this.suspendPolicy = suspendPolicy;
  }

  public void setThreadId(int threadId)
  {
    this.threadId = threadId;
  }
  
  /**
   * Return the id of the thread which created this event.
   *   
   * @return
   */
  public int getThreadId()
  {
    return threadId;
  }
  
  public int getClassId()
  {
    return (int) location.getClassId();
  }

  public void setLocation(Location location)
  {
    this.location = location;
  }

  /**
   * @return
   */
  public Location getLocation()
  {
    return location;
  }
}
