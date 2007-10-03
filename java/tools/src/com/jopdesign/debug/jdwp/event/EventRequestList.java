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

import java.util.List;
import java.util.Vector;

/**
 * EventRequestList.java
 * 
 * A list of EventRequest type.
 * 
 * @author Paulo Abadie Guedes
 *
 * 14/06/2007 - 23:21:28
 * 
 */
public class EventRequestList
{
  private List list;
  
  public EventRequestList()
  {
    clear();
  }
  
  public void clear()
  {
    list = new Vector();
  }
  
  public void add(EventRequest eventRequest)
  {
    list.add(eventRequest);
  }
  
  public void remove(EventRequest eventRequest)
  {
    list.remove(eventRequest);
  }
  
  public int size()
  {
    return list.size();
  }
  
  public EventRequest get(int index)
  {
    return (EventRequest) list.get(index);
  }
  
  public EventRequest getRequest(int id)
  {
    EventRequest eventRequest = null;
    EventRequest result = null;
    int index;
    int size = size();
    
    for(index = 0; index < size; index++)
    {
      eventRequest = (EventRequest) list.get(index);
      if(id == eventRequest.getId())
      {
        result = eventRequest;
        break;
      }
    }
    
    return result;
  }
}
