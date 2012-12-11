/**
 *  This file is part of miniCDx benchmark of oSCJ.
 *
 *   miniCDx is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   miniCDx is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with miniCDx.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2009, 2010 
 *   @authors  Daniel Tang, Ales Plsek
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */

/*
 * Changes to support JSR-302 0.9:
 * 
 * - CyclicExecutive does not implement safelet
 * 
 */
package cdx.cdx;

import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;
import cdx.simulator.immortal.Simulator;

//import java.io.PrintWriter;

//import javax.realtime.AbsoluteTime;
//import javax.realtime.Clock;
import javax.realtime.PriorityParameters;
//import javax.realtime.RelativeTime;
//import javax.realtime.Scheduler;
import javax.safetycritical.CyclicExecutive;
//import javax.safetycritical.CycClicSchedule;
//import javax.safetycritical.Frame;
import javax.safetycritical.MissionSequencer;
//import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

//import cdx.cdx.unannotated.NanoClock;

/*@javax.safetycritical.annotate.Scope("immortal")*/
//public class Level0Safelet extends CyclicExecutive implements Safelet{
public class Level0Safelet implements Safelet<CyclicExecutive>{
    
	public Level0Safelet() {
        super();
    }

    
    //---------------------- Safelet methods
	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		// TODO Auto-generated method stub
		Constants.PRESIMULATE = true;
        new ImmortalEntry().run();
        new Simulator().generate();
		
	}
	
	@Override
	public MissionSequencer<CyclicExecutive> getSequencer() {

		PriorityParameters sequencerPrio = new PriorityParameters(10);
		StorageParameters sequencerSto = new StorageParameters(1024, null);
		return new SingleMissionSequencer(sequencerPrio, sequencerSto);
	}


	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
//	   public void initialize() {
//	        Constants.PRESIMULATE = true;
//	        new ImmortalEntry().run();
//	        new Simulator().generate();
//	    }

    
    
//    //---------------------- Mission methods
//    
//    
//    
//    public void setup() {
//        Constants.PRESIMULATE = true;
//        new ImmortalEntry().run();
//        new Simulator().generate();
//    }

//    public void teardown() {
//        dumpResults();
//    }

//    public CyclicSchedule getSchedule(PeriodicEventHandler[] handlers) {
//        Frame[] frames = new Frame[1];
//        CyclicSchedule schedule = new CyclicSchedule(frames);
//        frames[0] = new Frame(new RelativeTime(Constants.DETECTOR_PERIOD, 0), handlers);
//        return schedule;
//    }

    /*@javax.safetycritical.annotate.RunsIn("cdx.Level0Safelet")*/
//    protected void initialize() {
//        try {
//            ImmortalEntry.detectorThreadStart = NanoClock.now();
//            AbsoluteTime releaseAt = NanoClock.roundUp(Clock.getRealtimeClock().getTime().add(
//                Constants.DETECTOR_STARTUP_OFFSET_MILLIS, 0));
//            ImmortalEntry.detectorFirstRelease = NanoClock.convert(releaseAt);
//            new CollisionDetectorHandler().register();
//
//            if (Constants.DEBUG_DETECTOR) {
//                System.out.println("Detector thread is " );//+  Thread.currentThread());
//                System.out
//                    .println("Entering detector loop, detector thread priority is "
////                            + +Thread.currentThread().getPriority() + " (NORM_PRIORITY is " + Thread.NORM_PRIORITY
//                            + " XX " + " (NORM_PRIORITY is " + Thread.NORM_PRIORITY
//                            + ", MIN_PRIORITY is " + Thread.MIN_PRIORITY + ", MAX_PRIORITY is " + Thread.MAX_PRIORITY
//                            + ")");
//            }
//
//        } catch (Throwable e) {
//            System.out.println("e: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

//    public long missionMemorySize() {
//        return Constants.PERSISTENT_DETECTOR_SCOPE_SIZE;
//    }

//    public void dumpResults() {
//        
//        String space = " ";
//        String triZero = " 0 0 0 ";
//
//        if (Constants.PRINT_RESULTS) {
//            System.out
//                .println("Dumping output [ timeBefore timeAfter heapFreeBefore heapFreeAfter detectedCollisions ] for "
//                        + ImmortalEntry.recordedRuns + " recorded detector runs, in ns");
//        }
//        System.out.println("=====DETECTOR-STATS-START-BELOW====");
//        for (int i = 0; i < ImmortalEntry.recordedRuns; i++) {
//            System.out.print(ImmortalEntry.timesBefore[i]);
//            System.out.print(space);
//            System.out.print(ImmortalEntry.timesAfter[i]);
//            System.out.print(space);
//            System.out.print(ImmortalEntry.detectedCollisions[i]);
//            System.out.print(space);
//            System.out.print(ImmortalEntry.suspectedCollisions[i]);
//            System.out.print(triZero);
//            System.out.println(i);
//        }
//
//        System.out.println("=====DETECTOR-STATS-END-ABOVE====");
//
//        System.out.println("Generated frames: " + Constants.MAX_FRAMES);
//        System.out.println("Received (and measured) frames: " + ImmortalEntry.recordedRuns);
//        System.out.println("Frame not ready event count (in detector): " + ImmortalEntry.frameNotReadyCount);
//        System.out.println("Frames dropped due to full buffer in detector: " + ImmortalEntry.droppedFrames);
//        System.out.println("Frames processed by detector: " + ImmortalEntry.framesProcessed);
//        // System.out.println("Detector stop indicator set: "
//        // + ImmortalEntry.persistentDetectorScopeEntry.stop);
//        System.out.println("Reported missed detector periods (reported by waitForNextPeriod): "
//                + ImmortalEntry.reportedMissedPeriods);
//        System.out.println("Detector first release was scheduled for: "
//                + NanoClock.asString(ImmortalEntry.detectorFirstRelease));
//        // heap measurements
//        Simulator.dumpStats();
//
//        // detector release times
//        if (Constants.DETECTOR_RELEASE_STATS != "") {
//            System.out.println("=====DETECTOR-RELEASE-STATS-START-BELOW====");
//            for (int i = 0; i < ImmortalEntry.recordedDetectorReleaseTimes; i++) {
//                System.out.print(ImmortalEntry.detectorReleaseTimes[i]);
//                System.out.print(space);
//                System.out.print(i * Constants.DETECTOR_PERIOD * 1000000L + ImmortalEntry.detectorReleaseTimes[0]);
//                System.out.print(space);
//                System.out.print(ImmortalEntry.detectorReportedMiss[i] ? 1 : 0);
//                System.out.print(space);
//                System.out.println(i);
//            }
//            System.out.println("=====DETECTOR-RELEASE-STATS-END-ABOVE====");
//        }
//    }



}

class SingleMissionSequencer extends MissionSequencer<CyclicExecutive> {

	boolean served = false;
	private CyclicExecutive mission = null;

	public SingleMissionSequencer(PriorityParameters priority,
			StorageParameters storage) {
		super(priority, storage);
	}
	
	CyclicExecutive newMission() {
		
		CyclicExecutive single = new Level0Mission();
		return single;
	}

	@SCJAllowed(SUPPORT)
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	@Override
	protected CyclicExecutive getNextMission() {
		if (!served) {
			mission = newMission();

			// Comment the following line to have an infinite
			// stream of missions
			served = true;

			return mission;
		} else {
			mission = null;
			return null;
		}
	}
}
