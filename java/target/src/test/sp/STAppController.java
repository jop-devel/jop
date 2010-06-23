/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/**
 * 
 */
package sp;

import sp.STScheduler;
import com.jopdesign.sys.Startup;
import com.jopdesign.io.SysDevice;
import com.jopdesign.io.IOFactory;

/**
 * The sheduler API for the single-path based CMP system.
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 *
 */
public class STAppController extends STScheduler {

    // Constructor 
    public STAppController(int maxtask) {
	super(maxtask);
	/* see constructor of			tmeas = -(tstop-tstart);
 super class */
    }

    static SysDevice sys = IOFactory.getFactory().getSysDevice();

    static boolean measureExecTimes(STAppController tskList[]) {
	int i,j;
	int tsum=0;
	int tmeas;
	//SysDevice sys = IOFactory.getFactory().getSysDevice();

	System.out.println("Measured execution times of task set:\\\\");
	System.out.println("\\begin{tabular}{l|r|r|r|r}");
	System.out.println("{\\em Task} & {\\em R} & {\\em E} & {\\em W} & {\\em Total}\\\\");
	for (i=0; i<tskList.length; i++) {
	    for (j=0; j<tskList[i].tabCyclicExec.length; j++)
	    {
		if (j%3 == 0) {
		    tsum = 0;
		    System.out.println("\\hline");
		    System.out.print("Task (");
		    System.out.print(i);
		    System.out.print(",");
		    System.out.print(j/3);
		    System.out.print(") & ");
		}
		//System.out.println("measuring ["+i+","+j+"]...");
		syncWithMEMTDMA(); 
		tskList[i].tabCyclicExec[j].tsk.run(); /* enforce measurement with cache hit */
		tskList[i].tabCyclicExec[j].tsk.measure();
		tmeas = tskList[i].tabCyclicExec[j].tsk.getMeasResult();

		tsum = tsum + tmeas;
		System.out.print(tmeas);
		System.out.print(" & ");
		if (j%3 == 0 || 
		    j%3 == 1) {
		}
		if (j%3 == 2) {
		    System.out.print(tsum);
		    System.out.println(" \\\\");
		}
		//System.out.println("exec time measured ..."+(tstop-tstart)+"");
	    }
	}
	System.out.println("\\end{tabular}");
	return true;
    }
    
    static Runner wcetrun;

    /**
     * Creation of all tasks, shared memory objects, and scheduling tables
     */
    public static void main(String[] args) {

	STAppController tskList[] = new STAppController[3];
	tskList[0] = new STAppController(2*3);  // SampleSet + SampleCurr (double period)
	tskList[1] = new STAppController(1*3);  // Controller
	tskList[2] = new STAppController(2*3);  // Guard + Monitor

	/* create all tasks and shared memory objects */
        SharedIMem InSetVal   = new SharedIMem();
	SharedIMem InCurrVal  = new SharedIMem();
        SharedIMem ShmSetVal  = new SharedIMem();
	SharedIMem ShmCurrVal = new SharedIMem();
	SharedIMem ShmCtrlVal = new SharedIMem();
	SharedIMem ShmStatus  = new SharedIMem();

	STSampler TskSampleSet = new STSampler(InSetVal, ShmSetVal);
	STSampler TskSampleCurr = new STSampler(InCurrVal, ShmCurrVal);
	STController TskController = new STController(ShmSetVal, ShmCurrVal, ShmCtrlVal);
	STGuard TskGuard = new STGuard(ShmStatus, 3);
	STMonitor TskMonitor = new STMonitor(ShmSetVal, ShmCurrVal, ShmCtrlVal, TskGuard);

	//System.out.println("STAppController.main().A");System.out.flush();

	TskGuard.addTask(TskSampleSet);
	TskGuard.addTask(TskSampleCurr);
	TskGuard.addTask(TskController);

	tskList[0].setMajorCycle(100000);
	tskList[1].setMajorCycle(200000);
	tskList[2].setMajorCycle(200000);

	// WCET estimation
	wcetrun = new RRunner(TskSampleSet);


	/* Construction of task list 0 */
	tskList[0].tabCyclicExec[0].tsk = new RRunner(TskSampleSet);
	tskList[0].tabCyclicExec[0].tactivation = 0;
	tskList[0].tabCyclicExec[1].tsk = new XRunner(TskSampleSet);
	tskList[0].tabCyclicExec[1].tactivation = 0;
	tskList[0].tabCyclicExec[2].tsk = new WRunner(TskSampleSet);
	tskList[0].tabCyclicExec[2].tactivation = 0;
	tskList[0].tabCyclicExec[3].tsk = new RRunner(TskSampleCurr);
	tskList[0].tabCyclicExec[3].tactivation = 0;
	tskList[0].tabCyclicExec[4].tsk = new XRunner(TskSampleCurr);
	tskList[0].tabCyclicExec[4].tactivation = 0;
	tskList[0].tabCyclicExec[5].tsk = new WRunner(TskSampleCurr);
	tskList[0].tabCyclicExec[5].tactivation = 0;

	/* Construction of task list 1 */
	tskList[1].tabCyclicExec[0].tsk = new RRunner(TskController);
	tskList[1].tabCyclicExec[0].tactivation = 0;
	tskList[1].tabCyclicExec[1].tsk = new XRunner(TskController);
	tskList[1].tabCyclicExec[1].tactivation = 0;
	tskList[1].tabCyclicExec[2].tsk = new WRunner(TskController);
	tskList[1].tabCyclicExec[2].tactivation = 0;

	/* Construction of task list 2 */
	tskList[2].tabCyclicExec[0].tsk = new RRunner(TskGuard);
	tskList[2].tabCyclicExec[0].tactivation = 0;
	tskList[2].tabCyclicExec[1].tsk = new XRunner(TskGuard);
	tskList[2].tabCyclicExec[1].tactivation = 0;
	tskList[2].tabCyclicExec[2].tsk = new WRunner(TskGuard);
	tskList[2].tabCyclicExec[2].tactivation = 0;
	tskList[2].tabCyclicExec[3].tsk = new RRunner(TskMonitor);
	tskList[2].tabCyclicExec[3].tactivation = 0;
	tskList[2].tabCyclicExec[4].tsk = new XRunner(TskMonitor);
	tskList[2].tabCyclicExec[4].tactivation = 0;
	tskList[2].tabCyclicExec[5].tsk = new WRunner(TskMonitor);
	tskList[2].tabCyclicExec[5].tactivation = 0;


	//--- tskList[0].syncWithMEMTDMA(); 
	measureExecTimes(tskList);

	//System.out.println("STAppController.main().G");System.out.flush();

	/* start the other CPUs */
	Startup.setRunnable(tskList[1],0);
	Startup.setRunnable(tskList[0],1);


	/* Set a reasonable start time for each task */
        /* f_clk=60MHz --> 60000=1ms */
	SysDevice sys = IOFactory.getFactory().getSysDevice();
	int localtime = ((sys.cntInt + 60000*100) / MEM_TDMA_ROUND) * MEM_TDMA_ROUND;  // (current time + 100ms)
	tskList[0].time = localtime;
	tskList[1].time = localtime;
	tskList[2].time = localtime;

        /* calc. starting time... */
	//tskList[2].startCPUs();
	//tskList[2].run();
    }
    
    void wrapWCET() {
    	wcetrun.run();
    }
    

}
