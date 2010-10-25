/*---------------------------------------------------------------------*\
 *
 * aicas GmbH, Karlsruhe, 2010
 *
 * This file may be modified for and distributed with the JSR 282 reference
 * implementation under any license deemed appropriate by the specification
 * leader provided that this notice and authorship is maintained.  aicas GmbH
 * reserves the right to use this class as it is or in any derived form.
 *
 * $Source: /home/cvs/jsr302/scj/specsrc/javax/realtime/ChildScopeVisitor.java,v $
 * $Revision: 1.1 $
 * $Author: jjh $
 * Contents: javax.safetycritical.jsr282.ScopedAllocationContext
 *
\*---------------------------------------------------------------------*/

package javax.realtime;

/**
 * This is a visitor for children scoped allocation contexts.  It defines some
 * work to be performed on each child.  It is used by
 * {@link AllocationContext#visitScopedChildren(ChildScopeVisitor)}.
 */
public interface ChildScopeVisitor
{
  /**
   * The method to be called when using this visitor.  
   *
   * @param scope is a child scoped allocation context.
   *
   * @return some instance of an Object
   */
  Object visit(ScopedAllocationContext scope);
}
