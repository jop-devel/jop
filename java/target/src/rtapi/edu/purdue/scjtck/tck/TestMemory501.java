package edu.purdue.scjtck.tck;

import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import javax.realtime.MemoryArea;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PrivateMemory;

/**
 * @author leizhao
 * 
 *         Assertions:
 * 
 *         - A scope may only be entered from the memory area in which it is
 *         created;
 * 
 *         - Each application uses a global mission scope in the place of
 *         immortal memory to hold global objects used during a mission;
 * 
 *         - In initialization phase and mission phase all objects are allocated
 *         in mission memory;
 * 
 *         - Each schedulable object has its private scoped memory;
 * 
 *         - Object creation in mission scoped memory or immortal memory during
 *         the mission phase is allowed;
 * 
 *         - PrivateMemory is based on LTMemory
 * 
 *         - Deep nested private memory is supported;
 * 
 *         - MissionMemory is ScopedMemory;
 */

public class TestMemory501 extends TestCase {

	public MissionSequencer getSequencer() {

		/*
		 * FIXME: I don't think there is a clear description of the place where
		 * a PrivateMemory can be created. They say one can create PrivateMemory
		 * during (mission) initialization or during mission phase, but do not
		 * explicitly prohibit the creation in other place, e.g ImmortalMemory.
		 * (RI does so though via throw exception, see PrivateMemory.java)
		 */
		
		/*
		 * PrivateMemory is not accessible to the user. Memory areas shall not
		 * be created by the user.
		 */
		
//		try {
//			new PrivateMemory(5);
//			fail("PrivateMemory illegally created in "
//					+ RealtimeThread.getCurrentMemoryArea());
//		} catch (Throwable t) {
//			// PrivateMemory is not supposed to be created out side
//			// MissionMemory or PrivateMemory
//		}

		return new GeneralSingleMissionSequencer(new GeneralMission() {

			private int _depth;

			@Override
			public void initialize() {
				final MemoryArea missionMem = RealtimeThread
						.getCurrentMemoryArea();

				if (!(missionMem instanceof ScopedMemory)) {
					fail("Mission memory is not instance of ScopedMemory");
				}

				new GeneralPeriodicEventHandler() {
					@Override
					public void handleAsyncEvent() {
						MemoryArea privateMem = RealtimeThread
								.getCurrentMemoryArea();
						if (!(privateMem instanceof PrivateMemory))
							fail("Schedulable objects not run in private memory");
						if (!(privateMem instanceof ScopedMemory))
							fail("Private memory is not instance of ScopedMemory");
						if (!(privateMem instanceof LTMemory))
							fail("Private memory is not instance of LTMemory");

						_depth = 0;

						new PrivateMemory(5000).enter(new Runnable() {
							public void run() {
								final MissionManager mngrL1 = ((PrivateMemory) RealtimeThread
										.getCurrentMemoryArea()).getManager();
								new PrivateMemory(5000).enter(new Runnable() {
									public void run() {
										MissionManager mngrL2 = ((PrivateMemory) RealtimeThread
												.getCurrentMemoryArea())
												.getManager();
										if (!mngrL1.equals(mngrL2))
											fail("Error occured in PrivateMemory.getManager()");
										_depth++;
									}
								});
								_depth++;
								if (_depth != 2)
									fail("Unable to enter nested private memory");
							}
						});
					}
				};

				/*
				 * Object creation in mission scoped memory or immortal memory
				 * during the mission phase is allowed
				 */
				new GeneralPeriodicEventHandler() {
					@Override
					public void handleAsyncEvent() {
						try {
							missionMem.newInstance(Object.class);
						} catch (Throwable e) {
							fail("Error occured during object creation in mission memory during mission phase");
						}
						try {
							ImmortalMemory.instance().newInstance(Object.class);
						} catch (Throwable e) {
							fail("Error occured during object creation in immortal memory during mission phase");
						}
					}
				};

				/*
				 * A scope may only be entered from the memory area in which it
				 * is created
				 */
				final PrivateMemory scopeL1 = new PrivateMemory(5000);
				final PrivateMemory scopeL2 = new PrivateMemory(5000);

				new GeneralPeriodicEventHandler() {
					@Override
					public void handleAsyncEvent() {
						scopeL1.enter(new Runnable() {
							public void run() {
								try {
									scopeL2.enter(new Runnable() {
										public void run() {
											fail("Private memory not entered from its parent scope");
										}
									});
								} catch (Throwable t) {
									/*
									 * scopeL2 is not allowed to be entered in
									 * scopeL1; there should be some exceptions
									 * thrown here
									 */
								}
							}
						});
					}
				};

				/*
				 * A mission global object, which should be able to be touched
				 * by PEHs
				 */
				final int nValueReaders = 3;
				final int value = 42;
				final Integer missionGlobalNum = new Integer(value);

				for (int i = 0; i < nValueReaders; i++)
					new GeneralPeriodicEventHandler() {
						@Override
						public void handleAsyncEvent() {
							if (missionGlobalNum.intValue() != value) {
								fail("Mission global object inaccessible or incorrect");
							}
						}
					};

				new Terminator();
			}
		});
	}

	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getArgs() {
		// TODO Auto-generated method stub
		return null;
	}
}
