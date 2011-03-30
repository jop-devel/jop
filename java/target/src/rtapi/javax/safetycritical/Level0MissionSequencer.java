package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_2;

import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * A MissionSequencer runs a sequence of independent Missions
 * interleaved with repeated execution of certain Missions.
 */
@SCJAllowed
public abstract class Level0MissionSequencer extends MissionSequencer {

  /**
   * Construct a Level0MissionSequencer to run at the priority and with the
   * memory resources specified by its parameters.
   *
   * @throws IllegalStateException if invoked at an inappropriate
   * time.  The only appropriate times for instantiation of a new
   * MissionSequencer are (a) during execution of
   * Safelet.getSequencer() by SCJ infrastructure during startup of an
   * SCJ application, or (b) during execution of Mission.initialize()
   * by SCJ infrastructure during initialization of a new Mission in
   * a LevelTwo configuration of the SCJ run-time environment.
   */
  @MemoryAreaEncloses(inner = { "this" }, outer = { "priority" })
  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION)
  public Level0MissionSequencer(PriorityParameters priority,
                                StorageParameters storage)
  {
    super(priority, storage);
  }

  /**
   * This method is called by infrastructure to select the initial
   * Mission to execute, and subsequently, each time one Mission
   * terminates, to determine the next Mission to execute.
   * <p>
   * Prior to each invocation of getNextMission() by infrastructure,
   * infrastructure instantiates and enters a very large MissionMemory
   * allocation area.  The typical behavior is for getNextMission() to
   * return a Mission object that resides in this MissionMemory area.
   *
   * @return the next Mission to run, or null if no further Missions
   * are to run under the control of this MissionSequencer.
   */
  @SCJAllowed
  protected abstract Level0Mission getNextMission();

}
