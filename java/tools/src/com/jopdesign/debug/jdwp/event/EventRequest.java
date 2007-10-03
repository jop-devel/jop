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
 * EventRequest.java
 *
 * A class to model an event request from the debugger.
 * 
 * A request has an ID, a kind, suspend policy and a set
 * of constraints which need to be satisfied in order to
 * the event be sent to the debugger.
 * 
 * @author Paulo Abadie Guedes
 *
 * 14/06/2007 - 11:39:17
 * 
 */
public class EventRequest
{
  private int requestId;
  
  private byte eventKind;
  private byte suspendPolicy;
  
  private ConstraintList list;
  
  public EventRequest(int requestId)
  {
    this.requestId = requestId;
    list = new ConstraintList();
  }
  
  public int getId()
  {
    return requestId;
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
  
  public void addConstraint(Constraint constraint)
  {
    list.add(constraint);
  }
  
//  public 
}
