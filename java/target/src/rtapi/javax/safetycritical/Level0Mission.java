package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;

/**
 * A Level-Zero Safety Critical Java application is comprised of one or more
 * Level0Missions.  Each Level0Mission is implemented as a subclass of this
 * abstract Level0Mission class.
 */

@SCJAllowed
public abstract class Level0Mission extends Mission 
{
  /**
   * Constructor for a Level0Mission.  Normally, application-specific code
   * found within the application-defined subclass of MissionSequencer
   * instantiates a new Level0Mission in the MissionMemory area that
   * is dedicated to that 
   * Level0Mission. Upon entry into the constructor, this same MissionMemory
   * area is the current allocation area.
   * <p>
   * Note that this class inherits missionMemorySize(),
   * initialize(), 
   * requestTermination(), terminationRequested(),
   * requestSequenceTermination(), sequenceTerminationRequested(),
   * and cleanUp()
   * methods from Mission.
   * <p>
   * TBD: Under what conditions would we want to prohibit construction
   * of a new Level0Mission.  Presumably, it is "harmless" for a PEH to
   * instantiate a new Level0Mission.  But what if the PEH instantiates a
   * Mission, and then tries to "start" it?  Kelvin suggests to resolve
   * this problem by hiding the start method.  
   */
  @Allocate( { Area.THIS })
  // initializer_ initialization
  @SCJAllowed
  public Level0Mission() {}

  /**
   * Return the CyclicSchedule for this Level0Mission, residing in 
   * the same scope as this Level0Mission object.  Under normal
   * circumstances, this method is only invoked from the SCJ
   * infrastructure.  It is listed as public because this method is
   * typically overridden in application-specific subclasses which
   * reside outside the javax.safetycritical package.
   */
  @SCJAllowed
  protected abstract CyclicSchedule getSchedule();

}
