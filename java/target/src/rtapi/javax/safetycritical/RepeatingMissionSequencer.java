package javax.safetycritical;

import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Level.SUPPORT;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * RepeatingMissionSequencer
 *
 * @param <SpecificMission>
 */
@SCJAllowed
public class RepeatingMissionSequencer<SpecificMission extends Mission>
  extends MissionSequencer<SpecificMission>
{
//  boolean returnedInitialMission;
  
	SpecificMission single;
	SpecificMission[] missions_;
	SpecificMission next_mission;
	String name_;
	
	int mission_id = 0;

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
  public RepeatingMissionSequencer(PriorityParameters priority,
                                StorageParameters storage,
                                SpecificMission m)
  {
    super(priority, storage);
    single = m;
    
//    returnedInitialMission = false;
  }
  
  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
  public RepeatingMissionSequencer(PriorityParameters priority,
                                StorageParameters storage,
                                SpecificMission m, String name)
  {
    super(priority, storage);
    single = m;
    name_ = name;
    
//    returnedInitialMission = false;
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
  public RepeatingMissionSequencer(PriorityParameters priority,
                                StorageParameters storage,
                                SpecificMission [] missions)
  {
    super(priority, storage);
//    returnedInitialMission = false;
    missions_ = missions;
	

  }

  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
  public RepeatingMissionSequencer(PriorityParameters priority,
                                StorageParameters storage,
                                SpecificMission [] missions, String name)
  {
    super(priority, storage);
//    returnedInitialMission = false;
    missions_ = missions;
    name_ = name;
  }

  
  /**
   * @see javax.safetycritical.MissionSequencer#getNextMission()
   */
  @SCJAllowed(SUPPORT)
  @SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
  @Override
  protected SpecificMission getNextMission()
  {
	  
		// For an array of missions
		if (missions_ != null){
			
			if (mission_id >= missions_.length){
				mission_id = 0;
			}
			
			next_mission = missions_[mission_id];
			mission_id++;

		
		// For a single mission, always return the same mission
		}else{
			next_mission = single;
		}
		
		current_mission = next_mission;
		
//		if (next_mission != null)
//			next_mission.initialize();
		
		return next_mission;
  }
}

