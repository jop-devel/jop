package prelude;

import javax.safetycritical.Safelet;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.JopSystem;

import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public class PreludeSafelet implements Safelet {

	public static final int TICK_SCALE = 10;
	public static final int PRIVATE_MEMORY_SIZE = 1024;
	public static final int MISSION_MEMORY_SIZE = 64*1024;
	public static final int IMMORTAL_MEMORY_SIZE = 64*1024;

	private static PreludeTask [] taskSet;
	private static PreludePrecedence [] precSet;

	@Override
	public MissionSequencer getSequencer() {
		return new PreludeSequencer(new PreludeMission(taskSet, precSet));
	}

	@Override
	public long immortalMemorySize() {
		return IMMORTAL_MEMORY_SIZE;
	}

	/**
	 * Within the JOP SCJ version we use a main method instead of a command line
	 * parameter or configuration file.
	 * 
	 * @param args
	 */
	public static void start(PreludeTask[] tasks, PreludePrecedence[] precs) {
		taskSet = tasks;
		precSet = precs;
		JopSystem.startMission(new PreludeSafelet());
	}


	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		// TODO
	}
}
