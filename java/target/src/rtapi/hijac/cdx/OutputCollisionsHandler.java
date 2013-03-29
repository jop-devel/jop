/**
 * @author Frank Zeyda, Kun Wei
 */
package hijac.cdx;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;

import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.StorageParameters;

import hijac.cdx.CDxMission;

/**
 * OutputCollisionsHandler outputs the number of detected collisions to an
 * external device. For the purpose of the simulation, we merely print it on the
 * screen.
 */
public class OutputCollisionsHandler extends AperiodicEventHandler {
	public final CDxMission mission;

	public OutputCollisionsHandler(CDxMission mission) {
		super(new PriorityParameters(PriorityScheduler.instance()
				.getMaxPriority()), new AperiodicParameters(),
				new StorageParameters(2048, null, 0, 0), 1024, "OutputHandler");
		this.mission = mission;
	}

	@Override
	public void handleAsyncEvent() {
		System.out.println("[OutputHandler] called");

		/* The the collisions result form the CDxMission class. */
		int colls = mission.getColls();

		System.out.println(colls + " collisions have been detected!");

		/* In a real program: write collisions to an external device. */

		/* Signal to InputFrameHandler we are ready to process the next frame. */
		mission.simulator.detectorReady = true;

	}

}
