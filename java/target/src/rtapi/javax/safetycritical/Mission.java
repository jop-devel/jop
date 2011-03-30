package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;
import static javax.safetycritical.annotate.Level.SUPPORT;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;

/**
 * A Safety Critical Java application is comprised of one or more
 * Missions.  Each Mission is implemented as a subclass of this
 * abstract Mission class.
 */
@SCJAllowed
public abstract class Mission
{
  /**
   * Constructor for a Mission.  Normally, application-specific
   * code found within the application-defined subclass of
   * MissionSequencer instantiates
   * a new Mission in the MissionMemory area that is dedicated to that
   * Mission. Upon entry into the constructor, this same MissionMemory
   * area is the current allocation area.
   *
   * TBD: Under what conditions would we want to prohibit construction
   * of a new Mission.  Presumably, it is "harmless" for a PEH to
   * instantiate a new Mission.  But what if the PEH instantiates a
   * Mission, and then tries to "start" it?  Kelvin suggests to resolve
   * this problem by hiding the start method.  
   */
  @Allocate( { Area.THIS })
  // initializer_ initialization
  @SCJAllowed
  public Mission() {}

  /**
   * Method to clean up after an application
   * terminates. Infrastructure calls cleanup after all ManagedSchedulables
   * associated with this Mission have terminated, but before control
   * leaves the dedicated MissionMemory area.  The default
   * implementation of cleanUp does nothing.  User-defined
   * subclasses may override its implementation.
   */
  @SCJAllowed(SUPPORT)
  protected void cleanUp() {}

  /**
   * Perform initialization of the Mission. Infrastructure calls
   * initialize after the Mission has been instantiated and the
   * MissionMemory has been resized to match the size returned from
   * Mission.missionMemorySize.  Upon entry into the initialize()
   * method, the current allocation context is the MissionMemory area
   * dedicated to this particular Mission.
   * <p>
   * The default implementation of
   * initialize() does nothing.  User-defined subclasses may override
   * its implementation.
   * <p>
   * The typical implementation of initialize() instantiates and
   * registers all ManagedSchedulable objects that consitute this
   * Mission.  The infrastructure enforces that ManagedSchedulables
   * can only be instantiated and registered if the currently
   * executing ManagedSchedulable is running a Mission.initialize()
   * method under the direction of the Safety Critical Java
   * infrastructure.  The infrastructure arranges to begin executing
   * the registered ManagedSchedulable objects
   * associated with a particular Mission upon return from the
   * initialize() method.
   * <p>
   * Besides initiating the associated ManagedSchedulable objects,
   * this method may also instantiate and/or initialize certain
   * Mission-level data structures. Note that objects shared between
   * ManagedSchedulables typically reside within the MissionMemory
   * scope.  Individual ManagedSchedulables can gain access to these
   * objects by passing references to their constructors, or by
   * obtaining a reference to the current mission (by invoking
   * Mission.getCurrentMission()) and coercing this
   * reference to the known Mission subclass.
   */
  @SCJAllowed(SUPPORT)
  protected abstract void initialize();

  // not sure what this does. i believe this is an implementation
  // artifact, not SCJAllowed
  static Mission instance() { return null; }

  /**
   * This method provides a standard interface for requesting
   * termination of a Mission.  The default implementation has the
   * effect of setting internal state so that subsequent invocations
   * of terminationPending() shall return true.  The additional effects
   * are to (1) arrange for all of the periodic event handlers associated
   * with this Mission to be disabled so that no further firings will
   * occur, and (2) arranging to disable all AperiodicEventHandlers so
   * that no further firings will be honored, and (3) decrementing the
   * pending fire count for each event handler so that the event
   * handler can be effectively shut down following completion of any
   * event handling that is currently active.
   * <p>
   * An application-specific subclass of Mission may override this
   * method in order to insert application-specific code to
   * communicate the intent to shutdown to specific
   * ManagedSchedulables.  It is especially useful to override
   * requestTermination() within Missions that
   * include ManagedThread or inner-nested MissionSequencers.
   * <p>
   * TBD: there's no mention of pending fire count in the @SCJAllowed
   * API of BoundAsyncEventHandler.  What is our intended treatment of this?
   * <p>
   * TBD: James made this method final.  Ok?
   */
  @SCJAllowed
  public final void requestTermination() {}

  /**
   * Ask for termination of the current mission and its sequencer. The
   * effect of this method is to invoke requestSequenceTermination() on
   * the MissionSequencer that is responsible for execution of this
   * Mission.
   * <p>
   * TBD: Kelvin made this method final.  Ok?
   */
  @SCJAllowed
  public final void requestSequenceTermination() {}

  /**
   * Check if the current mission is trying to terminate.
   *
   * @return true if and only if this Mission's requestTermination()
   * method has been invoked.
   */
  @SCJAllowed
  public final boolean terminationPending()
  {
    return false;
  }

  /**
   * Check if the current MissionSequencer is trying to terminate.
   *
   * @return true if and only if the requestSequenceTermination()
   * method for the MissionSequencer that controls execution of this
   * Mission has been invoked.
   */
  @SCJAllowed
  public final boolean sequenceTerminationPending()
  {
    return false;
  }

  /**
   * Obtain the current mission.
   * 
   * @return the current mission instance.
   */
  @SCJAllowed
  public static Mission getCurrentMission()
  {
    return null;
  }
}
