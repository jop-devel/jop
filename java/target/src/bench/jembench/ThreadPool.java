/*
  Copyright (C) Thomas B. Preusser <thomas.preusser@tu-dresden.de>

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package jembench;

/**
 * A simple ThreadPool implementation for the execution of the
 * multi-threaded benchmarks of the JBE suite.
 *
 * @author   Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 */
public class ThreadPool {
  private final class Worker extends Thread {
    Worker() {}

    public void run() {
      Runnable  task;
      while((task = getTask()) != null)  task.run();
    }
  }

  private int  cap;
  private int  busy;

  // Sentinel for Completion
  private final static Runnable  KILL = new Runnable() { public void run() {} };
  private Runnable  pending;

  public ThreadPool(int  cap) {
    this.cap  = cap;
    this.busy = cap;
    while(--cap >= 0)  new Worker().start();
  }

  public synchronized int getSize() {
    return  cap;
  }
  public synchronized void ensure(final int  cap) {
    for(int  i = this.cap; i < cap; i++) {
      this.busy++;
      new Worker().start();
    }
    this.cap = cap;
  }


  public synchronized boolean isIdle() {
    return  busy == 0;
  }
  public synchronized void pushTask(final Runnable  task) {
    if(pending == KILL)  throw  new IllegalStateException();
    while(pending != null) {
      try { wait(); } catch(final InterruptedException e) {}
    }
    pending = (task == null)? KILL : task;
    notifyAll();
  }
  public synchronized void waitForAll() {
    while((busy > 0) || ((pending != null) && (pending != KILL))) {
      try { wait(); } catch(final InterruptedException e) {}
    }
  }
  public void die() {
    pushTask(null);
  }

  private synchronized Runnable getTask() {
    // Local Copy of Sentinel
    final Runnable  KILL = ThreadPool.KILL;
    busy--;
    notifyAll();

    // Wait for Task to arrive
    Runnable  pending;
    while((pending = this.pending) == null) {
      try { wait(); } catch(final InterruptedException e) {}
    }

    // Return Task to Worker
    if(pending == KILL)  return  null;
    this.pending = null;
    busy++;
    notifyAll();
    return  pending;
  }
}
