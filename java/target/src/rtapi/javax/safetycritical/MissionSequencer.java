package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_2;
import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Level.INFRASTRUCTURE;

import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import joprt.RtThread;
import joprt.SwEvent;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * A MissionSequencer runs a sequence of independent Missions interleaved with
 * repeated execution of certain Missions.
 */
@SCJAllowed
public abstract class MissionSequencer<SpecificMission extends Mission> extends
		ManagedEventHandler {

	private SwEvent clean;
	private boolean cleanupDidRun;
	
	// why is this static?
	// ok, in level 1 we have only one mission.
	static boolean terminationRequest = false;

	/**
	 * The constructor just sets the initial state.
	 * 
	 * @param priority
	 * @param storage
	 * @param name
	 */
	@SCJAllowed
	@MemoryAreaEncloses(inner = { "this", "this", "this" }, outer = {
			"priority", "storage", "name" })
	@SCJRestricted(phase = INITIALIZATION)
	public MissionSequencer(PriorityParameters priority,
			StorageParameters storage, String name) {
		// MS: just to make the system happy, but we don't want to
		// really extend the handler.... We want to run in the
		// plain Java thread!
		super(priority, null, storage, name);
		// just an idle thread that watches the tremination request
		new RtThread(0, 10000) {
			public void run() {
				for (;;) {
					if (!cleanupDidRun && terminationRequest) {
						// where is the cleanup now?
						// cleanup();
						cleanupDidRun = true;
					}
					waitForNextPeriod();
				}
			}
		};

		// clean = new SwEvent(0, 100) {
		// public void handle() {
		// if (!cleanupDidRun && terminationRequest) {
		// cleanup();
		// }
		// }
		// };
	}

	/**
	 * Construct a MissionSequencer to run at the priority and with the memory
	 * resources specified by its parameters.
	 * 
	 * @throws IllegalStateException
	 *             if invoked at an inappropriate time. The only appropriate
	 *             times for instantiation of a new MissionSequencer are (a)
	 *             during execution of Safelet.getSequencer() by SCJ
	 *             infrastructure during startup of an SCJ application, or (b)
	 *             during execution of Mission.initialize() by SCJ
	 *             infrastructure during initialization of a new Mission in a
	 *             LevelTwo configuration of the SCJ run-time environment.
	 */
	@SCJAllowed
	@MemoryAreaEncloses(inner = { "this" }, outer = { "priority" })
	@SCJRestricted(phase = INITIALIZATION)
	public MissionSequencer(PriorityParameters priority,
			StorageParameters storage) {
		super(priority, null, storage, null);
	}

	/**
	 * This method is called by infrastructure to select the initial Mission to
	 * execute, and subsequently, each time one Mission terminates, to determine
	 * the next Mission to execute.
	 * <p>
	 * Prior to each invocation of getNextMission() by infrastructure,
	 * infrastructure instantiates and enters a very large MissionMemory
	 * allocation area. The typical behavior is for getNextMission() to return a
	 * Mission object that resides in this MissionMemory area.
	 * 
	 * @return the next Mission to run, or null if no further Missions are to
	 *         run under the control of this MissionSequencer.
	 */
	@SCJAllowed(SUPPORT)
	protected abstract SpecificMission getNextMission();

	/**
	 * This method is declared final because the implementation is provided by
	 * the vendor of the SCJ implementation and shall not be overridden. This
	 * method performs all of the activities that correspond to sequencing of
	 * Missions by this MissionSequencer.
	 */
	@Override
	@SCJAllowed(INFRASTRUCTURE)
	public final void handleAsyncEvent() {
	}

	@Override
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	public final void register() {
	}

	/**
	 * Try to finish the work of this mission sequencer soon by invoking the
	 * currently running Mission's requestTermination method. Upon completion of
	 * the currently running Mission, this MissionSequencer shall return from
	 * its eventHandler method without invoking getNextMission and without
	 * starting any additional missions.
	 * <p>
	 * Note that requestSequenceTermination does not force the sequence to
	 * terminate because the currently running Mission must voluntarily
	 * relinquish its resources.
	 * <p>
	 * TBD: shouldn't we also have a sequenceTerminationPending() method? We
	 * need something like this in order to implement
	 * Mission.sequenceTerminationPending().
	 * <p>
	 * TBD: why restrict this to level_2? in level_0 and level_1, requesting
	 * sequence termination represents a mechanism to request "graceful"
	 * shutdown of an application.
	 */
//	@SCJAllowed(LEVEL_2)
//	public final void requestSequenceTermination() {
//	}
//
//	@SCJAllowed(LEVEL_2)
//	public final boolean sequenceTerminationPending() {
//		return false;
//	}
}
