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

import java.util.LinkedList;


/**
 * This class models a list of workers.
 * 
 * It is a simple and convenient way to ask other Threads to
 * stop their activities.
 * 
 * The list itself is a Worker, so lists of workers may 
 * also contain other lists of workers.
 * This uses the "Composite" pattern. Not strictly necessary now,
 * but may be useful later. 
 * 
 * @author Paulo Abadie Guedes
 */
public class WorkerList extends BasicWorker
{
  private LinkedList list;
  
  public WorkerList()
  {
    list = new LinkedList();
  }
  
  public synchronized void addWorker(Worker worker)
  {
    if(list.contains(worker) == false)
    {
      list.add(worker);
    }
  }
  
  public synchronized void removeWorker(Worker worker)
  {
    if(list.contains(worker))
    {
      list.remove(worker);
    }
  }
  
  public synchronized Worker getWorker(int index)
  {
    Worker worker = null;
    
    worker = (Worker) list.get(index);
    return worker;
  }
  
  public synchronized int size()
  {
    return list.size();
  }
  
  public synchronized void stopWorking()
  {
    if(isWorking())
    {
      super.stopWorking();
      
      int i, size;
      size = size();
      for(i = 0; i < size; i++)
      {
        Worker worker = getWorker(i);
        worker.stopWorking();
      }
    }
  }
}
