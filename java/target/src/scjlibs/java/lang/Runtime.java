/* Runtime.java -- access to the VM process
   Copyright (C) 1998, 2002, 2003, 2004, 2005 Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.lang;

import com.jopdesign.io.IOFactory;



/**
 * Runtime represents the Virtual Machine.
 *
 * @author John Keiser
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Jeroen Frijters
 */
// No idea why this class isn't final, since you can't build a subclass!
public class Runtime
{

  /**
   * The one and only runtime instance.
   */
  private static final Runtime current = new Runtime();

  /**
   * Not instantiable by a user, this should only create one instance.
   */
  private Runtime()
  {
   //TODO: not implemented
  }

  /**
   * Get the current Runtime object for this JVM. This is necessary to access
   * the many instance methods of this class.
   *
   * @return the current Runtime object
   */
  public static Runtime getRuntime()
  {
    return current;
  }

  /**
   * Exit the Java runtime. This method will either throw a SecurityException
   * or it will never return. The status code is returned to the system; often
   * a non-zero status code indicates an abnormal exit. Of course, there is a
   * security check, <code>checkExit(status)</code>.
   *
   * <p>First, all shutdown hooks are run, in unspecified order, and
   * concurrently. Next, if finalization on exit has been enabled, all pending
   * finalizers are run. Finally, the system calls <code>halt</code>.</p>
   *
   * <p>If this is run a second time after shutdown has already started, there
   * are two actions. If shutdown hooks are still executing, it blocks
   * indefinitely. Otherwise, if the status is nonzero it halts immediately;
   * if it is zero, it blocks indefinitely. This is typically called by
   * <code>System.exit</code>.</p>
   *
   * @param status the status to exit with
   * @throws SecurityException if permission is denied
   * @see #addShutdownHook(Thread)
   * @see #runFinalizersOnExit(boolean)
   * @see #runFinalization()
   * @see #halt(int)
   */
  public void exit(int status)
  {
	  com.jopdesign.sys.Startup.exit();
  }

  /**
   * Find out how much memory is still free for allocating Objects on the heap.
   *
   * @return the number of bytes of free memory for more Objects
   */
  public long freeMemory()
  {
	  return com.jopdesign.sys.GC.freeMemory();
  }

  /**
   * Find out how much memory total is available on the heap for allocating
   * Objects.
   *
   * @return the total number of bytes of memory for Objects
   */
  public long totalMemory()
  {
	  return com.jopdesign.sys.GC.totalMemory();
  }

  /**
   * Run the garbage collector. This method is more of a suggestion than
   * anything. All this method guarantees is that the garbage collector will
   * have "done its best" by the time it returns. Notice that garbage
   * collection takes place even without calling this method.
   */
  public void gc()
  {
	 System.gc();
  }
  
  public int availableProcessors() {
	  return IOFactory.getFactory().getSysDevice().nrCpu;
  }
} // class Runtime
