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


public class BasicWorker extends Thread implements Worker
{
  private boolean shouldWork = true;
  private WorkerList list;
  
  public BasicWorker()
  {
    this("");
  }

  public BasicWorker(String name)
  {
    super(name);
//    Debug.println("BasicWorker -> constructor()");
     
    // careful! don't create the WorkerList here, otherwise this
    // constructor will never return. It will cause a stack overflow.
    // A WorkerList is itselft a BasicWorker, and hence also needs
    // to call this constructor. Creating it here would result 
    // in a cyclic sequence of calls.
  }
  
  /**
   * Check is this object is still active. Since once it stops
   * it cannot resume again, this method is not synchronized.
   */
  public boolean isWorking()
  {
    // TODO: remove when not needed
//    Debug.println("BasicWorker -> isWorking()");
    return shouldWork;
  }
  
  /**
   * This method change the working flag and will synchronize
   * 
   * only once, if needed. Once a Worker stops, it cannot resume.
   * Hence, this method will never need to acquire a lock again,
   * after the first call.
   */
  public void stopWorking()
  {
//    Debug.println("BasicWorker -> stopWorking()");
    
    if(isWorking())
    {
      synchronized (this)
      {
        shouldWork = false;
//      notifyListeners();
        WorkerList list = getList();
        if(list != null)
        {
          list.stopWorking();
        }
        notifyAll();
      }
    }
  }
  
  public synchronized void registerListener(Worker worker)
  {
    WorkerList list = getList();
    if(list == null)
    {
      list = createList();
    }
    list.addWorker(worker);
  }
  
  private WorkerList createList()
  {
    if(list == null)
    {
      list = new WorkerList();
    }
    
    return list;
  }

  private WorkerList getList()
  {
    return list;
  }
//  private synchronized void notifyListeners()
//  {
//    if(isWorking())
//    {
//      stopWorking();
//      list.stopWorking();
//    }
//  }
}
