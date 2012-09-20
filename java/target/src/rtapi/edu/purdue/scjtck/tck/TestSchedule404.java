package edu.purdue.scjtck.tck;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RelativeTime;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

/**
 * Level 1
 * 
 * - Priority Ceiling Emulation supported one level 1+
 * 
 * TODO: this is not really testing PCE, instead it just tests that there is a
 * priority inversion prevention mechanism in the RI. (Priority inheritance
 * satisfy this test)
 */
public class TestSchedule404 extends TestCase {

	@Override
	protected String getArgs() {
		return "-L 1";
	}

	public MissionSequencer getSequencer() {
		return new GeneralSingleMissionSequencer(new GeneralMission() {
			/*
			 * the time of lowStart, lowEnd, highStart, highEnd, medianStart,
			 * medianEnd in order
			 */
			private long[] _timeRecord = new long[6];

			@Override
			public void initialize() {
				final MyLock lock = new MyLock();
				final MyFreeLock freelock = new MyFreeLock();

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
				 * "high" will be blocked on "lock"; but "median" never.If the
				 * priority ceiling protocol is implemented correctly, the
				 * priorities of "median" and "high" should not be inverted,
				 * i.e., they should finish in the order that they start
				 */
				new PeriodicEventHandler(low, new PeriodicParameters(null,
						new RelativeTime(Long.MAX_VALUE, 0)),
						new StorageParameters(256, new long[] { 100 }), 100) {
					public void handleAsyncEvent() {
						lock.doLow();
					}
				};

				new PeriodicEventHandler(high, new PeriodicParameters(
						new RelativeTime(5, 0), new RelativeTime(
								Long.MAX_VALUE, 0)), new StorageParameters(256,
						new long[] { 100 }), 100) {
					public void handleAsyncEvent() {
						lock.doHigh();
					}
				};

				new PeriodicEventHandler(median, new PeriodicParameters(
						new RelativeTime(10, 0), new RelativeTime(
								Long.MAX_VALUE, 0)), new StorageParameters(256,
						new long[] { 100 }), 100) {
					public void handleAsyncEvent() {
						freelock.doMedian();
					}
				};

				new Terminator();
			}

			private long getCurrentTimeInNano() {
				AbsoluteTime time = Clock.getRealtimeClock().getTime();
				long nanos = time.getMilliseconds() * 1000000
						+ time.getNanoseconds();
				if (nanos < 0)
					nanos = Long.MAX_VALUE;

				return nanos;
			}

			// take around ? ms
			private void doWorks() {
				// for (int i = 0; i < 100000000; i++)
				// Above code takes to long in jop simulator - Tórur 22/6/2011
				for (int i = 0; i < 200000; i++)
					;
			}

			@Override
			// public void cleanup() {
			public void cleanUp() {
				for (int i = 0; i < 5; i++)
					if (_timeRecord[i] > _timeRecord[i + 1])
						fail("Error in priority ceiling emulation");
				
				// super.cleanup();
				super.cleanUp();
			}

			class MyLock {
				public synchronized void doLow() {
					_timeRecord[0] = getCurrentTimeInNano();
					doWorks();
					_timeRecord[1] = getCurrentTimeInNano();
				}

				public synchronized void doHigh() {
					_timeRecord[2] = getCurrentTimeInNano();
					doWorks();
					_timeRecord[3] = getCurrentTimeInNano();
				}
			}

			class MyFreeLock {
				public synchronized void doMedian() {
					_timeRecord[4] = getCurrentTimeInNano();
					doWorks();
					_timeRecord[5] = getCurrentTimeInNano();
				}
			}
		});
	}

	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}
}
