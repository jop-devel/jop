package cdx.cdx;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.PeriodicParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.CyclicExecutive; 
import javax.safetycritical.CyclicSchedule;
import javax.safetycritical.Frame;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import com.jopdesign.sys.Memory;

import cdx.cdx.unannotated.NanoClock;
import cdx.simulator.immortal.Simulator;

public class Level0Mission extends CyclicExecutive {

	@Override
	@SCJAllowed(Level.SUPPORT)
	protected void initialize() {

		try {
			ImmortalEntry.detectorThreadStart = NanoClock.now();
			AbsoluteTime releaseAt = NanoClock
					.roundUp(Clock.getRealtimeClock().getTime()
							.add(Constants.DETECTOR_STARTUP_OFFSET_MILLIS, 0));
			ImmortalEntry.detectorFirstRelease = NanoClock.convert(releaseAt);
			CollisionDetectorHandler cdh = new CollisionDetectorHandler(null,
					new PeriodicParameters(null, new RelativeTime(10, 0)),
					new StorageParameters(Constants.PERSISTENT_DETECTOR_BS_SIZE, null), Constants.PERSISTENT_DETECTOR_SCOPE_SIZE);

			cdh.register();

			if (Constants.DEBUG_DETECTOR) {
				System.out.println("Detector thread is ");// +
															// Thread.currentThread());
				System.out
						.println("Entering detector loop, detector thread priority is "
								// + +Thread.currentThread().getPriority() +
								// " (NORM_PRIORITY is " + Thread.NORM_PRIORITY
								+ " XX "
								+ " (NORM_PRIORITY is "
								+ Thread.NORM_PRIORITY
								+ ", MIN_PRIORITY is "
								+ Thread.MIN_PRIORITY
								+ ", MAX_PRIORITY is "
								+ Thread.MAX_PRIORITY + ")");
			}

		} catch (Throwable e) {
			System.out.println("e: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public CyclicSchedule getSchedule(PeriodicEventHandler[] handlers) {
		Frame[] frames = new Frame[1];
		frames[0] = new Frame(new RelativeTime(Constants.DETECTOR_PERIOD, 0),
				handlers);
		CyclicSchedule schedule = new CyclicSchedule(frames);
		return schedule;
	}

	public void cleanUp() {
		ImmortalEntry.memStats();
		dumpResults();
	}

	@Override
	@SCJAllowed
	public long missionMemorySize() {
		return Constants.PERSISTENT_DETECTOR_SCOPE_SIZE;
	}

	public void dumpResults() {

		// String space = " ";
		// String triZero = " 0 0 0 ";

		if (Constants.PRINT_RESULTS) {
			System.out
					.println("Dumping output [ timeBefore timeAfter detectedCollisions suspectedCollisions] for "
							+ ImmortalEntry.recordedRuns
							+ " recorded detector runs, in ns");
		}
		System.out.println("=====DETECTOR-STATS-START-BELOW====");
		for (int i = 0; i < ImmortalEntry.recordedRuns; i++) {
			ImmortalEntry.DS.index = i;
			Memory.getCurrentMemory()
					.enterPrivateMemory(1280, ImmortalEntry.DS);

			// System.out.print(ImmortalEntry.timesBefore[i]);
			// System.out.print(space);
			// System.out.print(ImmortalEntry.timesAfter[i]);
			// System.out.print(space);
			// System.out.print(ImmortalEntry.detectedCollisions[i]);
			// System.out.print(space);
			// System.out.print(ImmortalEntry.suspectedCollisions[i]);
			// System.out.print(triZero);
			// System.out.println(i);
		}

		System.out.println("=====DETECTOR-STATS-END-ABOVE====");

		System.out.println("Generated frames: " + Constants.MAX_FRAMES);
		System.out.println("Received (and measured) frames: "
				+ ImmortalEntry.recordedRuns);
		System.out.println("Frame not ready event count (in detector): "
				+ ImmortalEntry.frameNotReadyCount);
		System.out.println("Frames dropped due to full buffer in detector: "
				+ ImmortalEntry.droppedFrames);
		System.out.println("Frames processed by detector: "
				+ ImmortalEntry.framesProcessed);
		// System.out.println("Detector stop indicator set: "+
		// ImmortalEntry.persistentDetectorScopeEntry.stop);
		System.out
				.println("Reported missed detector periods (reported by waitForNextPeriod): "
						+ ImmortalEntry.reportedMissedPeriods);
		System.out.println("Detector first release was scheduled for: "
				+ NanoClock.asString(ImmortalEntry.detectorFirstRelease));
		// heap measurements
		Simulator.dumpStats();

		// detector release times
		if (Constants.DETECTOR_RELEASE_STATS != "") {
			System.out.println("=====DETECTOR-RELEASE-STATS-START-BELOW====");
			for (int i = 0; i < ImmortalEntry.recordedDetectorReleaseTimes; i++) {
				ImmortalEntry.DRS.index = i;
				Memory.getCurrentMemory().enterPrivateMemory(1280,
						ImmortalEntry.DRS);

				// System.out.print(ImmortalEntry.detectorReleaseTimes[i]);
				// System.out.print(space);
				// System.out.print(i * Constants.DETECTOR_PERIOD * 1000000L
				// + ImmortalEntry.detectorReleaseTimes[0]);
				// System.out.print(space);
				// System.out.print(ImmortalEntry.detectorReportedMiss[i] ? 1 :
				// 0);
				// System.out.print(space);
				// System.out.println(i);
			}
			System.out.println("=====DETECTOR-RELEASE-STATS-END-ABOVE====");
		}
	}
}