package edu.purdue.scjtck.tck;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RelativeTime;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
//import javax.safetycritical.StorageConfigurationParameters;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * Level 1
 * 
 * - full preemptively scheduling shall be supported Level 1+
 */
public class TestSchedule405 extends TestCase {

	@Override
	protected String getArgs() {
		return "-L 1";
	}

	public MissionSequencer getSequencer() {
		return new GeneralSingleMissionSequencer(new GeneralMission() {
			/*
			 * the time of lowStart, highStart, highEnd, medianStart, medianEnd,
			 * lowEnd in order
			 */
			private long[] _timeRecord = new long[6];

			public void initialize() {
				int MAX_PRIORITY = PriorityScheduler.instance()
						.getMaxPriority();
				int NOR_PRIORITY = PriorityScheduler.instance()
						.getNormPriority();
				int MIN_PRIORITY = PriorityScheduler.instance()
						.getMinPriority();

				PriorityParameters high = new PriorityParameters(
						(MAX_PRIORITY - NOR_PRIORITY) / 2 + NOR_PRIORITY);
				PriorityParameters median = new PriorityParameters(NOR_PRIORITY);
				PriorityParameters low = new PriorityParameters(NOR_PRIORITY
						- (NOR_PRIORITY - MIN_PRIORITY) / 2);

				/*
				 * start time order: low -> high -> median
				 * 
				 * high and median are supposed to preempt the execution of low,
				 * so we should have following order:
				 * 
				 * lowStart -> highStart -> highEnd -> medianStart -> medianEnd
				 * -> lowEnd
				 */
				new PeriodicEventHandler(low, new PeriodicParameters(null,
						new RelativeTime(Long.MAX_VALUE, 0)),
						new StorageParameters(_prop._schedObjBackStoreSize,
								null), _prop._schedObjScopeSize) {

					@Override
					public void handleAsyncEvent() {
						// Terminal.getTerminal().writeln("low starts");
						_timeRecord[0] = getCurrentTimeInNano();
						doWorks();
						_timeRecord[5] = getCurrentTimeInNano();
						// Terminal.getTerminal().writeln("low finishes");
					}
				};

				new PeriodicEventHandler(high, new PeriodicParameters(
						new RelativeTime(5, 0), new RelativeTime(
								Long.MAX_VALUE, 0)), new StorageParameters(
						_prop._schedObjBackStoreSize, null),
						_prop._schedObjScopeSize) {

					@Override
					public void handleAsyncEvent() {
						// Terminal.getTerminal().writeln("high starts");
						_timeRecord[1] = getCurrentTimeInNano();
						doWorks();
						_timeRecord[2] = getCurrentTimeInNano();
						// Terminal.getTerminal().writeln("high finishes");
					}
				};

				new PeriodicEventHandler(median, new PeriodicParameters(
						new RelativeTime(10, 0), new RelativeTime(
								Long.MAX_VALUE, 0)), new StorageParameters(
						_prop._schedObjBackStoreSize, null),
						_prop._schedObjScopeSize) {

					@Override
					public void handleAsyncEvent() {
						// Terminal.getTerminal().writeln("median starts");
						_timeRecord[3] = getCurrentTimeInNano();
						doWorks();
						_timeRecord[4] = getCurrentTimeInNano();
						// Terminal.getTerminal().writeln("median finishes");
					}
				};

				new Terminator();
			}

			// take around ? ms
			private void doWorks() {
				// for (int i = 0; i < 100000000; i++)
				for (int i = 0; i < 100000; i++)
					;
			}

			@Override
			// public void cleanup() {
			public void cleanUp() {
				for (int i = 0; i < 5; i++)
					if (_timeRecord[i] > _timeRecord[i + 1])
						fail("Error in preemptive scheduling");
				// super.cleanup();
				super.cleanUp();
			}

			private long getCurrentTimeInNano() {
				AbsoluteTime time = Clock.getRealtimeClock().getTime();
				long nanos = time.getMilliseconds() * 1000000
						+ time.getNanoseconds();
				if (nanos < 0)
					nanos = Long.MAX_VALUE;

				return nanos;
			}
		});
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}
}
