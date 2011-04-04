package javax.safetycritical;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Level.SUPPORT;

import static javax.safetycritical.annotate.Phase.CLEANUP;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * A safety-critical application consists of one or more missions,
 * executed concurrently or in sequence.  Every safety-critical
 * application is represented by an implementation of Safelet which
 * identifies the outer-most MissionSequencer.  This outer-most
 * MissionSequencer takes responsibility for running the sequence of
 * Missions that comprise this safety-critical application. 
 * <p>
 * The mechanism used to identify the Safelet to a particular SCJ
 * environment is implementation defined.
 * <p>
 * Given the implementation s of Safelet that represents a particular
 * SCJ application, the SCJ infrastructure invokes
 * in sequence s.setUp() followed by s.getSequencer().
 * For the MissionSequencer q returned from s.getSequencer(), the SCJ
 * infrastructure arranges for an independent thread to begin
 * executing the code forthat sequencer and then waits for that thread
 * to terminate its execution.  Upon termination of the
 * MissionSequencer's thread, the SCJ infrastructure invokes
 * s.tearDown(). 
 */
@SCJAllowed
public interface Safelet<MissionLevel extends Mission>
{
  /**
   * @return the MissionSequencer that oversees execution of Missions
   * for this application.
   */
  @SCJAllowed(SUPPORT)
  @SCJRestricted(phase = INITIALIZATION)
  public MissionSequencer<MissionLevel> getSequencer();
  
  /**
   * Code to execute before the sequencer starts.
   */
  @SCJAllowed(SUPPORT)
  @SCJRestricted(phase = INITIALIZATION)
  public void setUp();
  
  /**
   * Code to execute after the sequencer ends.
   */
  @SCJAllowed(SUPPORT)
  @SCJRestricted(phase = CLEANUP)
  public void tearDown();
}
