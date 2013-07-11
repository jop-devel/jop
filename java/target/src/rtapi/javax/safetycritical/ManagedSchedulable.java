/*---------------------------------------------------------------------*\
 *
 * aicas GmbH, Karlsruhe, Germany 2010
 *
 * This code is provided to the JSR 302 group for evaluation purpose
 * under the LGPL 2 license from GNU.  This notice must appear in all
 * derived versions of the code and the source must be made available
 * with any binary version.  Viewing this code does not prejudice one
 * from writing an independent version of the classes within.
 *
 * $Source: /home/cvs/jsr302/scj/specsrc/javax/safetycritical/ManagedSchedulable.java,v $
 * $Revision: 1.4 $
 * $Author: jjh $
 * Contents: Java source of HIJA Safety Critical Java interface
 *           ManagedTask
 *
\*---------------------------------------------------------------------*/


package javax.safetycritical;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;
import static javax.safetycritical.annotate.Phase.CLEANUP;
/**
 * An interface implemented by all Safety Critical Java Schedulable classes.
 * It defines the register mechanism.
 */
@SCJAllowed
public interface ManagedSchedulable extends javax.realtime.Schedulable
{

	// jrri: Not in v0.90 of spec
	//  /**
//   * Register the task with its Mission.
//   */
//  @SCJAllowed
//  @SCJRestricted(phase = INITIALIZATION)
//  public void register();

  @SCJAllowed(SUPPORT)
  @SCJRestricted(phase = CLEANUP)
  public void cleanUp();
}
