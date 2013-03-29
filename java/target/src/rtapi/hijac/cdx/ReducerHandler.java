/**
 * @author Frank Zeyda, Kun Wei
 */
package hijac.cdx;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
//import javax.realtime.ImmortalMemory;

//import javax.safetycritical.AperiodicEvent;
import javax.safetycritical.AperiodicEventHandler;
//import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

import hijac.cdx.Aircraft;
import hijac.cdx.CDxMission;
import hijac.cdx.CallSign;
import hijac.cdx.Constants;
import hijac.cdx.DetectorControl;
import hijac.cdx.Motion;
import hijac.cdx.PersistentData;
import hijac.cdx.RawFrame;
import hijac.cdx.Reducer;
import hijac.cdx.Vector3d;

import hijac.cdx.javacp.utils.HashMap;
import hijac.cdx.javacp.utils.Iterator;
import hijac.cdx.javacp.utils.LinkedList;
import hijac.cdx.javacp.utils.List;

/**
 * ReducerHandler performs two preparational tasks prior to the parallel
 * detection phase. First, it carries out voxel hashing which results in
 * aircrafts being assigned to voxels in a way that it is sufficient to check
 * for collisions within each voxel in order to obtain an upper bound on the
 * number of actual collisions. Secondly, it divides and distributes the
 * computational work between the parallel DetectorHandler instances.
 */
public class ReducerHandler extends AperiodicEventHandler {
	public final CDxMission mission;
	public final AperiodicEventHandler[] detectHandlers;
	public final DetectorControl control;
	// public final AperiodicEvent detect;

	/**
	 * Pseudo object factory to manage pre-allocated objects in mission memory.
	 */
	public final PersistentData factories;

	// public ReducerHandler(
	// CDxMission mission, AperiodicEvent event, DetectorControl control) {
	public ReducerHandler(CDxMission mission,
			AperiodicEventHandler[] eventHandlers, DetectorControl control) {
		super(new PriorityParameters(PriorityScheduler.instance()
				.getNormPriority()), new AperiodicParameters(),
				new StorageParameters(Constants.REDUCER_HANDLER_BS_SIZE, null,
						0, 0), Constants.REDUCER_HANDLER_SCOPE_SIZE,
				"ReducerHandler");

		this.mission = mission;
		// this.detect = event;
		// this.control = control;
		this.detectHandlers = eventHandlers;
		this.control = control;

		/*
		 * The use of newI() below is part of a work-around due to using a
		 * reference implementation in which the mission object resides in
		 * immortal memory rather than mission memory.
		 */
		// factories = (PersistentData) MemUtils.newI(PersistentData.class);
		// For now, there is no MissionMemory in JOP
		factories = new PersistentData();
	}

	@Override
	public void handleAsyncEvent() {
		System.out.println("[ReducerHandler] called");

		/* Signal that detectors are busy. */
		mission.simulator.detectorReady = false;

		/* Clear the number of collisions (set to zero). */
		mission.initColls();

		/* Discard partitioning of computational work. */
		mission.getWork().clear();

		/* Release all objects in mission memory managed by the factories. */
		factories.getListFactory().clear();

		factories.getMotionFactory().clear();

		/* Generate a list of Motion objects from currentFrame and state. */
		final List motions = createMotions();
		System.out.println("Motions: :" + motions.size());

		/* The Reducer class carries out the voxel hashing algorithm. */
		final Reducer reducer = new Reducer(Constants.GOOD_VOXEL_SIZE);

		/* Utility method that initiates the reduction step. */

		reduceAndPartitionMotions(reducer, motions);

		/* Initialise the detector control object. */
		control.start();

		/* Release all detector handlers. */
		// detect.fire();

		for (int i = 0; i < detectHandlers.length; i++) {
			detectHandlers[i].release();
		}

	}

	/**
	 * Creates a list of persistent Motion objects (in mission memory) from the
	 * current and previous aircraft positions in currentFrame and state. We
	 * note that though the returned list is allocated in per release memory,
	 * the Motion objects being elements of the list live in mission memory.
	 */
	public List createMotions() {
		List result = new LinkedList();
		Aircraft aircraft;
		Vector3d new_pos;

		RawFrame currentFrame = mission.getFrame();
		for (int i = 0, pos = 0; i < currentFrame.planeCnt; i++) {
			/* Get the current position of the next aircraft. */
			final float x = currentFrame.positions[3 * i];
			final float y = currentFrame.positions[3 * i + 1];
			final float z = currentFrame.positions[3 * i + 2];

			/* Get the call sign of the next aircraft. */
			final byte[] cs = new byte[Constants.LENGTH_OF_CALLSIGN];
			for (int j = 0; j < cs.length; j++) {
				cs[j] = currentFrame.callsigns[pos + j];
			}

			/* Advance index for callsign. */
			pos += cs.length;

			/* Get the last known position of this aircraft. */
			final Vector3d old_pos = mission.getState().get(new CallSign(cs));

			/* Allocated aircraft in per release memory. */
			aircraft = new Aircraft(cs);

			/* Allocated new position in per release memory. */
			new_pos = new Vector3d(x, y, z);

			/* Obtain pre-allocated Motion object. */
			Motion motion = factories.getMotionFactory().getNewMotion();

			if (old_pos == null) {
				/* If no previous position recorded use current position. */
				motion.copyfrom(aircraft, new_pos, new_pos);
			} else {
				/* Otherwise record current and previous position. */
				motion.copyfrom(aircraft, old_pos, new_pos);
			}

			/* Add motion object to the resulting list. */
			result.add(motion);
		}

		return result;
	}

	public void reduceAndPartitionMotions(Reducer reducer, List motions) {
		HashMap voxel_map = new HashMap();
		HashMap graph_colors = new HashMap();

		/* Perform the voxel hashing using the Reducer instance. */
		for (Iterator iter = motions.iterator(); iter.hasNext();) {
			reducer.performVoxelHashing((Motion) iter.next(), voxel_map,
					graph_colors);
		}

		/* Distributes the voxel motion lists between the detector handlers. */
		for (Iterator iter = voxel_map.values().iterator(); iter.hasNext();) {
			List voxel_motions = (List) iter.next();

			if (voxel_motions.size() > 1) {
				/* Obtain pre-allocated List object. */
				List list = factories.getListFactory().getNewList();

				/* Copy elements from voxel_motions. */
				list.addAll(voxel_motions);

				/* Assign motion list to one of the detector handlers. */
				mission.getWork().recordMotionList(list);
			}
		}
	}

}
