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

import java.util.List;
import java.util.Vector;

import com.sun.tools.jdi.PacketWrapper;

public class PacketList
{
  private List list;
  
  public PacketList()
  {
//    list = Collections.synchronizedList(list);
//    list = new LinkedList();
    list = new Vector();
  }
  
  /**
   * Add the given packet to the list.
   *  This method ignores null objects.
   * 
   * @param packet
   */
  public synchronized void add(PacketWrapper packet)
  {
    if(packet != null)
    {
      list.add(packet);
    }
  }

  public synchronized PacketWrapper get(int index)
  {
    PacketWrapper packet;
    try
    {
      packet = (PacketWrapper) list.get(index);
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      packet = null;
    }
    
    return packet;
  }
  
  public synchronized int size()
  {
    return list.size();
  }
  
  public synchronized PacketWrapper remove(int index)
  {
    PacketWrapper packet;
    packet = (PacketWrapper) list.remove(index);
    return packet;
  }
  
  /**
   * This method retur a list of PacketWrapper objects which are 
   * answers to the given packet.
   * 
   * @param packet
   * @return
   */
  public synchronized PacketList getAnswerSublist(PacketWrapper packet)
  {
    PacketList list = new PacketList();
    PacketWrapper possibleAnswer;
    int i, count;
    
    count = size();
    for(i = 0; i < count; i++)
    {
      possibleAnswer = get(i);
      
      if(possibleAnswer.isReplyTo(packet))
      {
        possibleAnswer = possibleAnswer.createCopy();
        list.add(possibleAnswer);
      }
    }
    
    return list;
  }
  
  public synchronized PacketList removeAnswerSublist(PacketWrapper packet)
  {
    PacketList list;
    list = getAnswerSublist(packet);

    int i, count;    
    count = list.size();
    for(i = 0; i < count; i++)
    {
      packet = list.get(i);
      removePacket(packet);
    }
    
    return list;
  }
  
  public synchronized void removePacket(PacketWrapper packet)
  {
    list.remove(packet);
  }
  
  public int getIndexOf(PacketWrapper packet)
  {
    int key = list.indexOf(packet);
    return key;
  }
  
  public synchronized String getIdList()
  {
    int i, count;
    int estimatedSize;
    
    count = size();
    estimatedSize = 4 * count;
    StringBuffer list = new StringBuffer(estimatedSize);
    
    for(i = 0; i < count; i++)
    {
      PacketWrapper wrapper = get(i);
      list.append(wrapper.getID());
      list.append(" ");
    }
    
    return list.toString();
  }

  public synchronized void add(PacketList list)
  {
    int i, count;
    PacketWrapper packet;
    
    count = list.size();
    for(i = 0; i < count; i++)
    {
      packet = list.get(i);
      add(packet);
    }
  }

  /**
   * @param i
   * @param j
   * @return
   */
  public PacketList getSublist(int initial, int number)
  {
    PacketList list = new PacketList();
    PacketWrapper packet; 
    int index, size;
    size = initial + number;
    for (index = initial; index < size; index++)
    {
      packet = get(index);
      packet = packet.createCopy();
      list.add(packet);
    }
    
    return list;
  }
  
  public PacketWrapper getFirstPacketInCommandSet(int commandSet, int command)
  {
    return getFirstPacketInCommandSet(0, commandSet, command);
  }
  
  public PacketWrapper getFirstPacketInCommandSet(int index, int commandSet, int command)
  {
    PacketWrapper packet = null; 
    PacketWrapper reply = null;
    int size = size();
    
    for (; index < size; index++)
    {
      packet = get(index);
      if((packet.getCmdSet() == commandSet) &&
         (packet.getCmd() == command)) 
      {
        reply = packet.createCopy();
        break;
      }
    }
    
    return reply;
  }

  public PacketWrapper getReplyPacket(PacketWrapper packet)
  {
    int index; 
    int size = size();
    PacketWrapper currentPacket;
    PacketWrapper replyPacket = null;
    
    for (index = 0; index < size; index++)
    {
      currentPacket = get(index);
      if(currentPacket.isReplyTo(packet))
      {
        replyPacket = currentPacket.createCopy();
        break;
      }
    }
    
    return replyPacket;
  }
}
