/**
 * @author Frank Zeyda, Kun Wei
 */
package hijac.cdx;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RelativeTime;

//import javax.safetycritical.AperiodicEvent;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import hijac.cdx.CDxMission;
import hijac.cdx.CDxSafelet;
import hijac.cdx.CallSign;
import hijac.cdx.Constants;
import hijac.cdx.RawFrame;
import hijac.cdx.StateTable;
import hijac.cdx.Vector3d;
import hijac.cdx.javacp.utils.Iterator;
//import hijac.cdx.javacp.utils.Iterator;
import hijac.cdx.javacp.utils.Set;

//import hijac.cdx.javacp.utils.Set;

//import java.util.Iterator;
//import java.util.Set;

/**
 * InputFrameHandler is a periodic handler that reads and stores radar frames as
 * they arrive. It also updates the shared variable "state" for previous
 * aircraft positions.
 */
public class InputFrameHandler extends PeriodicEventHandler {
	public final CDxMission mission;
	// public final AperiodicEvent reduce;
	public final AperiodicEventHandler reduceHandler;

	// public InputFrameHandler(CDxMission mission, AperiodicEvent event) {
	public InputFrameHandler(CDxMission mission,
			AperiodicEventHandler eventHandler) {
		super(new PriorityParameters(PriorityScheduler.instance()
				.getMaxPriority()), new PeriodicParameters(null,
				new RelativeTime(Constants.DETECTOR_PERIOD, 0)),
				new StorageParameters(Constants.INPUT_FRAME_HANDLER_BS_SIZE,
						null, 0, 0), Constants.INPUT_FRAME_HANDLER_SCOPE_SIZE,
				"InputHandler");
		this.mission = mission;
		// reduce = event;
		reduceHandler = eventHandler;
	}

	@Override
	public void handleAsyncEvent() {
		CDxSafelet.terminal.writeln("[InputHandler] called");

		/* Terminate mission when enough frames have been processes. */
		if ((mission.simulator.framesProcessed + mission.simulator.droppedFrames) == Constants.MAX_FRAMES) {
			mission.requestSequenceTermination();
			mission.dumpResults();
			return;
		}

		/* Performs device access to read the next radar frame. */
		mission.simulator.frameBuffer.readFrame();

		if (mission.simulator.detectorReady) {
			RawFrame frame = mission.simulator.frameBuffer.getFrame();

			/* Store the frame and update previous positions. */
			StoreFrame(frame);

			/* Increment the number of processed frames. */
			mission.simulator.framesProcessed++;

			/* Release ReducerHandler to perform the voxel hashing. */
			// reduce.fire();
			reduceHandler.release();
		} else {
			/* If the detector is not ready, read the next frame and drop it. */
			RawFrame drop = mission.simulator.frameBuffer.getFrame();

			System.out.println("A frame has been dropped.");

			/* Increment the number of dropped frames. */
			mission.simulator.droppedFrames++;
		}

	}

	/* This method correspond to the StoreFrame action in the S anchor. */

	public void StoreFrame(RawFrame frame) {
		/* Update shared data that holds previous aircraft positions. */
		updateState();

		/* Read the frame into the shared variable. */
		mission.getFrame().copy(frame);

		System.out.println("A new frame has been read.");
	}

	/**
	 * This method records the current positions of all aircrafts in the shared
	 * state variable of type StateTable.
	 */
	public void updateState() {
		RawFrame frame = mission.getFrame();

		/* Used to determine disappeared aircrafts in the state table. */
		Set vanished = mission.getState().getCallSigns();
		
		for (int i = 0, pos = 0; i < frame.planeCnt; i++) {
			/* Get the current position of the next aircraft. */
			final float x = frame.positions[3 * i];
			final float y = frame.positions[3 * i + 1];
			final float z = frame.positions[3 * i + 2];

			/* Get the call sign of the next aircraft. */
			final byte[] cs = new byte[Constants.LENGTH_OF_CALLSIGN];
			for (int j = 0; j < cs.length; j++) {
				cs[j] = frame.callsigns[pos + j];
			}

			/* Advance index for call sign. */
			pos += cs.length;

			/* Create a new call sign. */
			CallSign callsign = new CallSign(cs);

			/* The aircrafts is in view; remove from the vanished set. */
			vanished.remove(callsign);

			/* Get old position from the state */
			final Vector3d old_pos = mission.getState().get(new CallSign(cs));

			if (old_pos == null) {
				/* We have detected a new aircraft. */
				/*
				 * Note that the callsign object is not actually inserted into
				 * the StateTable but duplicated by the logic of the put method
				 * below.
				 */
				mission.getState().put(callsign, x, y, z);
			} else {
				/*
				 * If the aircraft is already recorded in the stable table
				 * simply update its position.
				 */
				old_pos.set(x, y, z);
			}
		}

		StateTable state = mission.getState();

		/*
		 * Finally remove all aircrafts from the state table that have
		 * disappeared from the radar in the current frame. This is important in
		 * order to give them a zero velocity when they re-enter, a requirement
		 * of our model.
		 */
		for (Iterator iter = vanished.iterator(); iter.hasNext();) {
			state.remove((CallSign) iter.next());
		}
	}
}
