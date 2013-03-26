/**
 * @author Frank Zeyda, Kun Wei
 */
package scjlibs.examples.hijac.cdx;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.StorageParameters;

import scjlibs.examples.hijac.cdx.CDxMission;
import scjlibs.examples.hijac.cdx.Constants;
import scjlibs.examples.hijac.cdx.DetectorControl;
import scjlibs.examples.hijac.cdx.Motion;
import scjlibs.examples.hijac.cdx.Vector3d;
import scjlibs.util.Iterator;
import scjlibs.util.List;

/**
 * DetectorHandler instances carry out the actual collisions detection.
 */
public class DetectorHandler extends AperiodicEventHandler {

	public final CDxMission mission;
	public final DetectorControl control;
	public final int id;

	public DetectorHandler(CDxMission mission, DetectorControl control, int id) {
		super(new PriorityParameters(PriorityScheduler.instance()
				.getNormPriority()), new AperiodicParameters(),
				new StorageParameters(2048, null, 0, 0),
				Constants.TRANSIENT_DETECTOR_SCOPE_SIZE, "DetectorHandler");
		this.mission = mission;
		this.control = control;
		this.id = id;
	}

	@Override
	public void handleAsyncEvent() {
		System.out.println("[DetectorHandler] (id=" + id + ") called");

		/* Local variable to hold the collisions result for the partition. */
		int colls;

		/* Calculate the number of collisions for this detector's work. */
		colls = CalcPartCollisions();

		System.out.println("Collisions detected by DetectorHandler (id=" + id
				+ "): " + colls);

		/* Record collisions result with the mission. */
		mission.recColls(colls);

		/* Notify DetectorControl that this handler has finished. */
		control.notify(id);

	}

	/* Implementation of the CalcPartCollisions action in the S anchor. */

	public int CalcPartCollisions() {
		int colls = 0;

		/* Get work for this detector via the shared Partition object. */
		List work = mission.work.getDetectorWork(id);

		for (Iterator iter = work.iterator(); iter.hasNext();) {
			colls += determineCollisions((List) iter.next());
		}

		return colls;
	}

	/* Compute the number of collisions for a List of Motion objects. */

	public int determineCollisions(final List motions) {
		int colls = 0;
		for (int i = 0; i < motions.size() - 1; i++) {
			Motion one = (Motion) motions.get(i);
			for (int j = i + 1; j < motions.size(); j++) {
				Motion two = (Motion) motions.get(j);
				Vector3d v = one.findIntersection(two);
				if (v != null) {
					colls++;
				}
			}
		}
		return colls;
	}

}
