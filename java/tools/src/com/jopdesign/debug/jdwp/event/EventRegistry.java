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
 * EventRegistry.java
 * 
 * A class to manage event requests sent from the debugger.
 * 
 * @author Paulo Abadie Guedes
 *
 * 14/06/2007 - 11:35:32
 * 
 */
public class EventRegistry
{
  private EventRequestList mainList;
  
  private EventRequestList[] listArray;
  
  public EventRegistry()
  {
    clear();
  }
  
  public void clear()
  {
    mainList = new EventRequestList();
  }
  
  public void add(EventRequest eventRequest)
  {
    mainList.add(eventRequest);
  }
  
  public void remove(EventRequest eventRequest)
  {
    mainList.remove(eventRequest);
  }
  
  public int size()
  {
    return mainList.size();
  }
  
  public EventRequest getRequest(int id)
  {
    EventRequest eventRequest = null;
    EventRequest result = null;
    int index;
    int size = size();
    
    for(index = 0; index < size; index++)
    {
      eventRequest = (EventRequest) mainList.get(index);
      if(id == eventRequest.getId())
      {
        result = eventRequest;
        break;
      }
    }
    
    return result;
  }
}
