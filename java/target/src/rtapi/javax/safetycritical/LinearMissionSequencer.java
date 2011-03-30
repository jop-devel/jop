package javax.safetycritical;

import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Level.SUPPORT;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * LinearMissionSequencer
 *
 * @param <SpecificMission>
 */
@SCJAllowed
public class LinearMissionSequencer<SpecificMission extends Mission>
  extends MissionSequencer<SpecificMission>
{
  boolean returnedInitialMission;

  /**
   * Throws IllegalStateException if invoked during initialization of
   * a level-zero or level-one mission.
   * 
   * @param priority 
   * @param storage 
   * @param m 
   */
  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
  public LinearMissionSequencer(PriorityParameters priority,
                                StorageParameters storage,
                                SpecificMission m)
  {
    super(priority, storage);
    returnedInitialMission = false;
  }

  /**
   * Throws IllegalStateException if invoked during initialization of
   * a level-zero or level-one mission.
   * 
   * @param priority 
   * @param storage 
   * @param missions 
   */
  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
  public LinearMissionSequencer(PriorityParameters priority,
                                StorageParameters storage,
                                SpecificMission [] missions)
  {
    super(priority, storage);
    returnedInitialMission = false;
  }

  /**
   * @see javax.safetycritical.MissionSequencer#getNextMission()
   */
  @SCJAllowed(SUPPORT)
  @SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
  @Override
  protected SpecificMission getNextMission()
  {
    return null;
  }
}

