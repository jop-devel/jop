package edu.purdue.scjtck;

import javax.realtime.AperiodicParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RelativeTime;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.Terminal;
import javax.safetycritical.annotate.Level;

public abstract class MainSafelet implements Safelet {

	protected abstract String getArgs();

	protected Properties _prop = new Properties();

	protected Thread _launcher;

	/* Parameters generated from Properties.java */
	// jrri: Are these used for something?
	protected PriorityParameters _priorityParam;
	protected AperiodicParameters _aperiodicParam;
	protected PeriodicParameters _periodicParam;
	protected StorageParameters _storageParam;

	// protected StorageConfigurationParameters _storageParam;

	/* ----------------- Methods ------------------- */

	public void initializeApplication() {
		// _launcher = Thread.currentThread();
		// _prop.parseArgs(PropFileReader.readAll());
		_prop.parseArgs(getArgs());
		_priorityParam = new PriorityParameters(_prop._priority);
		_periodicParam = new PeriodicParameters(new RelativeTime(_prop._iDelay,
				0), new RelativeTime(_prop._period, 0));
		// _aperiodicParam = new AperiodicParameters();
		_aperiodicParam = new AperiodicParameters(null, null);
		// _storageParam = new StorageConfigurationParameters(0, 0, 0);
		// _storageParam = new StorageParameters(0, 0, 0);
		_storageParam = new StorageParameters(0, null);

		Terminal.getTerminal().writeln(getInfo());
	}

	// Current spec has no teardown() method as part of the Safelet interface
	public void teardown() {
		Terminal.getTerminal().writeln(report());
	}

//	public Level getLevel() {
//		return _prop._level;
//	}

	protected abstract String getInfo();

	protected abstract String report();

	/* -------------- Wrapped Classes -------------- */

	public abstract class GeneralMission extends Mission {
		public long missionMemorySize() {
			return _prop._missionMemSize;
		}

		@Override
		protected void cleanUp() {
			super.cleanUp();
			// _launcher.interrupt();
		}
	}

	// public class GeneralSingleMissionSequencer extends SingleMissionSequencer
	// {
	public class GeneralSingleMissionSequencer extends MissionSequencer {
		private Mission mission;

		public GeneralSingleMissionSequencer(Mission mission) {
			super(new PriorityParameters(_prop._priority),
					new StorageParameters(_prop._schedObjBackStoreSize, null));
			this.mission = mission;
		}

		@Override
		protected Mission getNextMission() {
			return mission;
		}
	}

	public abstract class GeneralMissionSequencer extends MissionSequencer {
		public GeneralMissionSequencer() {
			super(new PriorityParameters(_prop._priority),
					new StorageParameters(_prop._schedObjBackStoreSize, null));
		}
	}

	public abstract class GeneralPeriodicEventHandler extends
			PeriodicEventHandler {

		public GeneralPeriodicEventHandler() {
			super(new PriorityParameters(_prop._priority),
					new PeriodicParameters(new RelativeTime(_prop._iDelay, 0),
							new RelativeTime(_prop._period, 0)),
					new StorageParameters(_prop._schedObjBackStoreSize, null), _prop._schedObjScopeSize);
		}

	}

	public abstract class GeneralAperiodicEventHandler extends
			AperiodicEventHandler {

		public GeneralAperiodicEventHandler() {
			super(new PriorityParameters(_prop._priority),
					new AperiodicParameters(null, null), new StorageParameters(
							_prop._schedObjBackStoreSize, null) , _prop._schedObjScopeSize );
		}

		public GeneralAperiodicEventHandler(String name) {
			super(new PriorityParameters(_prop._priority),
					new AperiodicParameters(null, null), new StorageParameters(
							_prop._schedObjBackStoreSize, null), _prop._schedObjScopeSize, name);
		}
	}

	// No managed threads for now...
	// public class GeneralManagedThread extends ManagedThread {
	//
	// public GeneralManagedThread() {
	// super(new PriorityParameters(_prop._priority),
	// // new StorageConfigurationParameters(0, 0, 0), null);
	// new StorageParameters(0, 0, 0), null);
	// }
	// }

	public class Terminator extends PeriodicEventHandler {

		public Terminator() {
			super(new PriorityParameters(PriorityScheduler.instance()
					.getMaxPriority()), new PeriodicParameters(
					new RelativeTime(_prop._duration, 0), new RelativeTime(
							Long.MAX_VALUE, 0)), new StorageParameters(_prop._schedObjBackStoreSize,
					null), _prop._schedObjScopeSize);
		}

		@Override
		public void handleAsyncEvent() {
			Mission.getCurrentMission().requestSequenceTermination();
			teardown();
			// getSequencer().requestSequenceTermination();
			// ((ManagedMemory) RealtimeThread.getCurrentMemoryArea())
			// .getManager().getMission().requestSequenceTermination();
		}
	}
}
