/*---------------------------------------------------------------------*\
 *
 * aicas GmbH, Karlsruhe, 2010
 *
 * This file may be modified for and distributed with the JSR 282 reference
 * implementation under any license deemed appropriate by the specification
 * leader provided that this notice and authorship is maintained.  aicas GmbH
 * reserves the right to use this class as it is or in any derived form.
 *
 * $Source: /home/cvs/jsr302/scj/specsrc/javax/realtime/ScopedAllocationContext.java,v $
 * $Revision: 1.4 $
 * $Author: jjh $
 * Contents: javax.safetycritical.jsr282.ScopedAllocationContext
 *
\*---------------------------------------------------------------------*/

package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;

/**
 * This is the base interface for all scoped memory areas.  Scoped memory is
 * a region based memory management strategy that can only be cleared when
 * no thread is executing in the area.  The exact deallocation semantics depend
 * depend on the implementing class.
 *
 * @author James J. Hunt, aicas GmbH, 2010
 *
 * getMaximumSize, visitScopedChildren
 */
@SCJAllowed
public interface ScopedAllocationContext extends AllocationContext
{
  /**
   * Get this memory area's portal object.  The portal provides a means of
   * passing information between Schedulable objects in a memory area.
   * Assignment rules are enforced on the value returned by getPortal as if
   * the return value were first stored in an object allocated in the current
   * allocation context, then moved to its final destination.
   *
   * @return the portal object.
   *
   * @throws MemoryAccessError when a reference to the portal object cannot
   *         be stored in the caller's allocation context; that is, if this
   *         is "inner" relative to the current allocation context or not on
   *         the caller's scope stack.
   *
   * @throws IllegalAssignmentError when caller is a Java thread.
   */
  @SCJAllowed
  public Object getPortal() throws MemoryAccessError, IllegalAssignmentError;

  /**
   * Sets the portal object of the memory area to the given object.  The object
   * must have been allocated in this ScopedMemory instance.
   * 
   * @param object the new portal object
   *
   * @throws IllegalThreadStateException when the caller is a Java Thread.
   *
   * @throws IllegalAssignmentError when the object is not allocated in this
   *         scoped memory instance and not null.
   *
   * @throws InaccessibleAreaException when the caller is a Schedulable object,
   *         this memory area is not in the caller's scope stack, and object
   *         is not null.
   */
  @SCJAllowed
  public void setPortal(Object object)
    throws IllegalThreadStateException,
           IllegalAssignmentError,
           InaccessibleAreaException;

  /**
   * Change the guarenteed and maximum size of the scoped memory area.  The
   * method may only be called when the memory area is empty, i.e., all
   * objects have been reclaimed and no Schedulable object has the area as
   * its allocation context.
   *
   * @param size is the new size for this memory area.
   *
   * @throws IllegalStateException when the area is not empty.
   */
  @SCJAllowed
  public void resize(long size)
    throws IllegalStateException;
}
