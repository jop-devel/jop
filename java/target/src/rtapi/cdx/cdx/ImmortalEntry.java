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
package cdx.cdx;

import java.io.DataOutputStream;

import com.jopdesign.sys.Memory;

/**
 * This thread runs only during start-up to run other threads. It runs in immortal memory, is allocated in immortal
 * memory, and it's constructor runs in immortal memory. It is a singleton, allocation from the Main class Ales: this
 * thread allocates - the scopes - the PersistentDetectorScope and TransientDetectorScope -
 */
/*@javax.safetycritical.annotate.Scope("immortal")*/
public class ImmortalEntry implements Runnable {
	
	static public String MemArea = "";
	static public Memory mem = null;
	static public DetectorStats DS = null;
	static public DetectorReleaseStats DRS = null;

    static public Object           initMonitor                  = new Object();
    static public boolean          detectorReady                = false;
    static public boolean          simulatorReady               = false;

    static public int              maxDetectorRuns;

    static public long             detectorFirstRelease         = -1;

    static public long[]           timesBefore;
    static public long[]           timesAfter;
    static public long[]           heapFreeBefore;
    static public long[]           heapFreeAfter;
    static public int[]            detectedCollisions;
    static public int[]            suspectedCollisions;

    static public long             detectorThreadStart;
    static public long[]           detectorReleaseTimes;
    static public boolean[]        detectorReportedMiss;

    static public int              reportedMissedPeriods        = 0;
    static public int              frameNotReadyCount           = 0;
    static public int              droppedFrames                = 0;
    static public int              framesProcessed              = 0;
    static public int              recordedRuns                 = 0;

    static public int              recordedDetectorReleaseTimes = 0;

    static public FrameBuffer      frameBuffer                  = null;

    static public DataOutputStream binaryDumpStream             = null;

    public ImmortalEntry() {
        // super(new PriorityParameters(Constants.DETECTOR_STARTUP_PRIORITY));

        maxDetectorRuns = Constants.MAX_FRAMES;

        timesBefore = new long[maxDetectorRuns];
        timesAfter = new long[maxDetectorRuns];
        heapFreeBefore = new long[maxDetectorRuns];
        heapFreeAfter = new long[maxDetectorRuns];
        detectedCollisions = new int[maxDetectorRuns];
        suspectedCollisions = new int[maxDetectorRuns];

        detectorReleaseTimes = new long[maxDetectorRuns + 10]; // the 10 is for missed deadlines
        detectorReportedMiss = new boolean[maxDetectorRuns + 10];
    }

    /** Called only once during initialization. Runs in immortal memory */
    public void run() {

        System.out.println("Detector: detector priority is " + Constants.DETECTOR_PRIORITY);
        System.out.println("Detector: detector period is " + Constants.DETECTOR_PERIOD);

        frameBuffer = new FrameBuffer();
        
        DS = new DetectorStats();
        DRS = new DetectorReleaseStats();
        
        /* start the detector at rounded-up time, so that the measurements are not subject
         * to phase shift
         */
    }
    
	class DetectorStats implements Runnable {
		
		int index = 0;

		@Override
		public void run() {
			System.out.print(ImmortalEntry.timesBefore[index]);
			System.out.print(" ");
			System.out.print(ImmortalEntry.timesAfter[index]);
			System.out.print(" ");
			System.out.print(ImmortalEntry.detectedCollisions[index]);
			System.out.print(" ");
			System.out.print(ImmortalEntry.suspectedCollisions[index]);
			System.out.print(" 0 0 0 ");
			System.out.println(index);
		}
	}
	
	class DetectorReleaseStats implements Runnable {

		int index = 0;

		@Override
		public void run() {
			System.out.print(ImmortalEntry.detectorReleaseTimes[index]);
			System.out.print(" ");
			System.out.print(index * Constants.DETECTOR_PERIOD * 1000000L
					+ ImmortalEntry.detectorReleaseTimes[0]);
			System.out.print(" ");
			System.out.print(ImmortalEntry.detectorReportedMiss[index] ? 1 : 0);
			System.out.print(" ");
			System.out.println(index);
		}
	}


    
    public static void memStats() {
    	
		mem = Memory.getCurrentMemory();

		switch (mem.level) {

		case 0:
			MemArea = "Immortal";
			break;
		case 1:
			MemArea = "Mission";
			break;
		case 2:
			MemArea = "Private";
			break;
		default:
			MemArea = "Nested private";
			break;

		}
    	
    	System.out.println("");
    	System.out.println("============ "+MemArea+" memory stats ============");
		System.out.println("Mem. size: "+ mem.size());
		System.out.println("Mem. remaining: "+ mem.memoryRemaining());
		System.out.println("Mem. consumed: "+ mem.memoryConsumed());
		System.out.println("Bs. remaining: "+ mem.bStoreRemaining());
		System.out.println("============ "+MemArea+" memory stats ============");
		System.out.println("");
    }
    
}
