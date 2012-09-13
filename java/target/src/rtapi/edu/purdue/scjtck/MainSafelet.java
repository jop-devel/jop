package edu.purdue.scjtck;

import javax.realtime.*;
import javax.realtime.PriorityScheduler;
import javax.safetycritical.*;

public abstract class MainSafelet implements Safelet {

	protected abstract String getArgs();
	
    protected Properties _prop = new Properties();

    protected Thread _launcher;

    /* Parameters generated from Properties.java */
    protected PriorityParameters _priorityParam;
    protected AperiodicParameters _aperiodicParam;
    protected PeriodicParameters _periodicParam;
//    protected StorageConfigurationParameters _storageParam;
    protected StorageParameters _storageParam;

    /* ----------------- Methods ------------------- */

    public void setup() {
//        _launcher = Thread.currentThread();
//        _prop.parseArgs(PropFileReader.readAll());
    	_prop.parseArgs(getArgs());
        _priorityParam = new PriorityParameters(_prop._priority);
        _periodicParam = new PeriodicParameters(new RelativeTime(_prop._iDelay,
                0), new RelativeTime(_prop._period, 0));
//        _aperiodicParam = new AperiodicParameters();
        _aperiodicParam = new AperiodicParameters(null,null);
//        _storageParam = new StorageConfigurationParameters(0, 0, 0);
//        _storageParam = new StorageParameters(0, 0, 0);
        _storageParam = new StorageParameters(0, null);

        Terminal.getTerminal().writeln(getInfo());
    }

    public void teardown() {
        Terminal.getTerminal().writeln(report());
    }

    /*public Level getLevel() {
        return null;//_prop._level;
    }*/

    protected abstract String getInfo();

    protected abstract String report();

    /* -------------- Wrapped Classes -------------- */
    public abstract class GeneralMission extends Mission {
        public long missionMemorySize() {
            return _prop._missionMemSize;
        }

        @Override
//        protected void cleanup() {
        protected void cleanUp() {
//            super.cleanup();
        	super.cleanUp();
//            _launcher.interrupt();
        }
    }

//    public class GeneralSingleMissionSequencer extends SingleMissionSequencer {
    public class GeneralSingleMissionSequencer extends MissionSequencer {
    	private Mission mission;
        public GeneralSingleMissionSequencer(Mission mission) {
            super(new PriorityParameters(_prop._priority),
//                    new StorageConfigurationParameters(0, 0, 0), mission);
//            		new StorageParameters(0, 0, 0));
            		new StorageParameters(0, null));
            this.mission = mission;
        }
        @Override
        protected Mission getNextMission()
        {
        	return mission;
        }
    }

    public abstract class GeneralMissionSequencer extends MissionSequencer {
        public GeneralMissionSequencer() {
            super(new PriorityParameters(_prop._priority),
//                    new StorageConfigurationParameters(0, 0, 0));
            		new StorageParameters(0, null));
        }
    }

    public abstract class GeneralPeriodicEventHandler extends
            PeriodicEventHandler {

		public GeneralPeriodicEventHandler(){
			super(new PriorityParameters(_prop._priority), 
					new PeriodicParameters(new RelativeTime(_prop._iDelay, 0), new RelativeTime(_prop._period, 0)), 
							new StorageParameters(0, null), 100);
		}
    	
    	

//        public GeneralPeriodicEventHandler() {
//            super(new PriorityParameters(_prop._priority),
//                    new PeriodicParameters(new RelativeTime(_prop._iDelay, 0),
//                            //new RelativeTime(_prop._period, 0)),
////                    new StorageConfigurationParameters(0, 0, 0),
//                    new StorageParameters(0, null),100);
////                    _prop._schedObjMemSize);
//        }
    }

    public abstract class GeneralAperiodicEventHandler extends
            AperiodicEventHandler {

        public GeneralAperiodicEventHandler() {
            super(new PriorityParameters(_prop._priority),
//                    new AperiodicParameters(),
            		new AperiodicParameters(null,null),
//                    new StorageConfigurationParameters(0, 0, 0),
            		new StorageParameters(0, null));
//                    _prop._schedObjMemSize);
//            		new AperiodicEvent[0]);
        }

        public GeneralAperiodicEventHandler(String name) {
            super(new PriorityParameters(_prop._priority),
//                    new AperiodicParameters(),
            		new AperiodicParameters(null,null),
//                    new StorageConfigurationParameters(0, 0, 0),
            		new StorageParameters(0, null));
//                    _prop._schedObjMemSize, name);
//            		new AperiodicEvent[0],name);
        }
    }

//	  No managed threads for now...
//    public class GeneralManagedThread extends ManagedThread {
//
//        public GeneralManagedThread() {
//            super(new PriorityParameters(_prop._priority),
////                    new StorageConfigurationParameters(0, 0, 0), null);
//            		new StorageParameters(0, 0, 0), null);
//        }
//    }

    public class Terminator extends PeriodicEventHandler {

        public Terminator() {
            super(new PriorityParameters(PriorityScheduler.instance()
                    .getMaxPriority()), new PeriodicParameters(
                    new RelativeTime(_prop._duration, 0), new RelativeTime(
                            Long.MAX_VALUE, 0)),
//                    new StorageConfigurationParameters(0, 0, 0), 0);
                      new StorageParameters(0, null),100);
        }

        @Override
//        public void handleEvent() {
        public void handleAsyncEvent() {
        	getSequencer().requestSequenceTermination();
//            ((ManagedMemory) RealtimeThread.getCurrentMemoryArea())
//                    .getManager().getMission().requestSequenceTermination();
        }
    }
}
