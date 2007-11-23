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

import com.sun.tools.jdi.PacketWrapper;

/**
 * A queue to hold JDWP packets during the debug process.
 * 
 * This object is thread-safe (or so I hope), and 
 * will wait if there is no more room available
 * or if there is too many objects stored.
 * 
 * So, external Thread objects may share this queue and
 * use it to produce and consume packets as needed, without
 * the need for any complex event model.
 * 
 * @author Paulo Abadie Guedes
 */
public class PacketQueue //extends Observable
{
  private PacketList list;
  
  // a large number of messages can be hold on this queue.
  // this is somewhat arbitrary, but can be changed using one
  // of the constructors.
  private static final int DEFAULT_SIZE_LIMIT = 1024;
  private static final int MAX_SIZE_LIMIT = 64 * 1024;
  
  private int sizeLimit = DEFAULT_SIZE_LIMIT;
  
  /**
   * Create a packet queue with a default size.
   */
  public PacketQueue()
  {
    list = new PacketList();
  }
  
  /**
   * Create a packet queue with a given size.
   * If the size is out of expected bounds, the default size is used instead.
   * 
   * @param sizeLimit
   */
  public PacketQueue(int sizeLimit)
  {
    this();
    setSizeLimit(sizeLimit); 
  }
  
  /**
   * Set the size limit of this queue. When this size is reached,
   * the object waits until there is more room available. 
   * 
   * @param limit
   */
  private void setSizeLimit(int limit)
  {
    if(limit < 1 || limit > MAX_SIZE_LIMIT)
    {
      limit = DEFAULT_SIZE_LIMIT;
    }
    
    sizeLimit = limit;
  }
  
  /**
   * Check if the queue is full.
   * 
   * @return
   */
  public synchronized boolean isQueueFull()
  {
    boolean result;
    result = list.size() >= sizeLimit;
    return result;
  }
  
  /**
   * Check if there is any packet still waiting to be handled.
   * 
   * @return
   */
  public synchronized boolean isAvailable()
  {
    return (list.size() > 0);
  }
  
  /** 
   * Add a packet to the end of the queue and then notify all threads.
   * 
   * If there is a Thread waiting (possibly a consumer), 
   * it will be notified about the new object to be consumed.
   */ 
  public synchronized void add(PacketWrapper packet)
  {
    while(isQueueFull())
    {
      try
      {
        wait();
      }
      catch(InterruptedException e)
      {
        // do nothing: ignore
      }
    }
    // now there is enough room. Add the packet.
    list.add(packet);
    
    //the lines below are just for development.
//    Debug.println("  Object added. Size: " + list.size());
//    packet.printInformation();
    
    // notify everyone that may be waiting on the lock of this object
    notifyAll();
  }
  
  /**
   * Remove the next available packet.
   * 
   * If the queue is currently empty, wait until a new packet arrives
   * before returning.
   * 
   * @return
   */
  public synchronized PacketWrapper removeNext()
  {
    PacketWrapper packet;
    
    while(isAvailable() == false)
    {
      try
      {
        wait();
      }
      catch(InterruptedException e)
      {
        // do nothing: ignore
      }
    }
    
    packet = list.remove(0);
    
    // after changing the shared data, notify threads interested on this lock
    notifyAll();
    
    return packet;
  }
}
