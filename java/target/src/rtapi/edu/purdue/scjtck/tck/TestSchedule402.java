/* Change log:
 
 September 2012: Adapted to JSR-302 v0.90, June 2012 with the following changes:
 				 - Removed AperiodicEvent class
 				 - Removed setup() and teardown() methods
 				 - Added release() method to AEH

 */

package edu.purdue.scjtck.tck;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RelativeTime;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.Services;
import javax.safetycritical.StorageParameters;

/**
 * 
 * Level 1
 * 
 * - The default ceiling for locks is
 * PriorityScheduler.instance().getMaxPriority()
 * 
 * - Nested calls from one synchronized method to another are allowed
 * 
 * - The only scheduler is the default RTSJ preemptive priority-based scheduler
 * with at least 28 priorities; (although if portability is a main concern, no
 * more than 28 priorities should be used). There is no support for changing
 * base priorities.
 * 
 */
public class TestSchedule402 extends TestCase {

	@Override
	protected String getArgs() {
		return "-L 1";
	}

	// ------------ Safelet Methods -------------------

	@Override
	public long immortalMemorySize() {
		return 0;
	}

	public MissionSequencer getSequencer() {

		return new GeneralSingleMissionSequencer(new GeneralMission() {

			private long _counter;
			private volatile long _counter_expected;
			private long _AEHCounter;
			private long _PEHCounter;

			public void initialize() {

				/*
				 * == Test 1 ==
				 */
				final AperiodicEventHandler aeh = new GeneralAperiodicEventHandler(
						"AEH") {
					public void handleAsyncEvent() {
						System.out.println("TEST1A");
						_AEHCounter++;
					}
				};

				new PeriodicEventHandler(
						new PriorityParameters(_prop._priority),
						new PeriodicParameters(null, new RelativeTime(200, 0)),
						new StorageParameters(256, new long[] { 100 }), 100) {
					public void handleAsyncEvent() {
						System.out.println("TEST1P");
						aeh.release();
						_PEHCounter++;
					}
				};

				/*
				 * == Test 2 ==
				 * 
				 * Test nested synchronized function calls. Several threads
				 * compete for the mission object, the func3 should be
				 * eventually invoked.
				 */
				for (int i = 0; i < 2; i++)
					new GeneralPeriodicEventHandler() {
						public void handleAsyncEvent() {
							System.out.println("TEST2");
							func1();
							_counter_expected++;
						}
					};

				/* == Test 3 == */
				new GeneralPeriodicEventHandler() {
					public void handleAsyncEvent() {
						System.out.println("TEST3");
						if (Services.getDefaultCeiling() != PriorityScheduler
								.instance().getMaxPriority())
							fail("Default ceiling should equal to the max priority");
						if (PriorityScheduler.instance().getMaxPriority()
								- PriorityScheduler.instance().getMinPriority() < 28)
							fail("Scheduler should provide at least 28 priorities");

						// TODO: this is just signature tests. Need works to
						// test the effects of setCeiling()
						Object obj = new Object();
						Services.setCeiling(obj, 20);
					}
				};

				new Terminator();
			}

			@Override
			protected void cleanUp() {
				if (_AEHCounter != _PEHCounter)
					fail("Error occurred in AEH or PEH");
				if (_counter != _counter_expected)
					fail("Error occrred in nested synchronization");
				super.cleanUp();
			}

			// ------ Methods particular to this mission -----------

			private synchronized void func1() {
				func2();
			}

			private synchronized void func2() {
				func3();
			}

			private synchronized void func3() {
				_counter++;
			}

		});
	}
}
