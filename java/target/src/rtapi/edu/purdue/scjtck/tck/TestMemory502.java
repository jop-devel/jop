package s3scj.tck;

import javax.realtime.MemoryInUseException;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.realtime.ScopedCycleException;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.PrivateMemory;

/**
 * @author leizhao
 * 
 *         Assertion:
 * 
 *         - A given scoped memory can only be entered by a single thread at any
 *         given time;
 */
public class TestMemory502 extends TestCase {

	public MissionSequencer getSequencer() {
		return new GeneralSingleMissionSequencer(new GeneralMission() {

			private final int _nScopeInvaders = 5;

			final private MyLock _myLock = new MyLock();

			@Override
			public void initialize() {
				final PrivateMemory scope = new PrivateMemory(5000);

				class ScopeInvader extends PeriodicEventHandler {

					public ScopeInvader() {
						super(new PriorityParameters(_prop._priority),
								new PeriodicParameters(null, new RelativeTime(
										500000, 0)), _prop._schedObjMemSize);
					}

					@Override
					public void handleEvent() {
						try {
							scope.enter(new Runnable() {
								public void run() {
									try {
										_myLock.doWait();
									}
									catch (InterruptedException e) {
										fail(e.getMessage());
									}
								};
							});
						}
						catch (MemoryInUseException miue) {
							// These are expected, since they should be thrown
							// when a thread is already in a scope
						}
						catch (ScopedCycleException sce) {
							fail("Multiple handlers entered a private memory simultaneously (scope cycle)");
						}
						catch (Throwable t) {
							fail(t.getMessage());
						}
					}
				}

				/*
				 * Create more than one scope memory invaders which will try to
				 * enter the "scope" memory at the same time. They should fail
				 * to do that.
				 */
				for (int i = 0; i < _nScopeInvaders; i++)
					new ScopeInvader();

				/*
				 * Wait for reasonable long a time so that all invaders should
				 * have entered the scope if they can. Notify all invaders and
				 * terminate the mission.
				 */
				new PeriodicEventHandler(
						new PriorityParameters(_prop._priority),
						new PeriodicParameters(null,
								new RelativeTime(500000, 0)),
						_prop._schedObjMemSize) {

					@Override
					public void handleEvent() {
						try {
							Thread.sleep(2000);
						}
						catch (InterruptedException e) {
							fail(e.getMessage());
						}
						_myLock.doNotifyAll();
						requestSequenceTermination();
					}
				};
			}

			@Override
			public long missionMemorySize() {
				return 5000 * _nScopeInvaders;
			}

			class MyLock {
				public synchronized void doWait() throws InterruptedException {
					this.wait();
				}

				public synchronized void doNotifyAll() {
					this.notifyAll();
				}
			}
		});
	}
}
