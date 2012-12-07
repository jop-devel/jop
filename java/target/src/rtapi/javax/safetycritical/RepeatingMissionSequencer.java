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
  
	Mission single;
	Mission[] missions_;
//	Mission next_mission;
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
	missions_ = new Mission[missions.length];
	System.arraycopy(missions, 0, missions_, 0, missions.length);	
  }

  @SCJAllowed
  @SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
  public RepeatingMissionSequencer(PriorityParameters priority,
                                StorageParameters storage,
                                SpecificMission [] missions, String name)
  {
    super(priority, storage);
//    returnedInitialMission = false;
	missions_ = new Mission[missions.length];
	System.arraycopy(missions, 0, missions_, 0, missions.length);
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
			
			current_mission = missions_[mission_id];
			handleAsyncEvent();
			mission_id++;
		
		// For a single mission, always return the same mission
		}else{
			current_mission = single;
			handleAsyncEvent();
		}
		
		return (SpecificMission) current_mission;
  }
}

