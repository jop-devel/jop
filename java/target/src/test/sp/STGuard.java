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

import sp.STSampler;
import com.jopdesign.sys.Native;

/**
 * A single path programming example on JOP.
 * Walks through an array of tasks to be monitored and checks if the
 * alive flag is set and deletes the alive flags afterwards.
 * 
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 *
 */
public class STGuard extends SimpleTask {

    /**
     * Helper class to allow cond. method call for single path code
     */
    class DummyHBTask extends SimpleHBTask {
	/**
	 * gets always the status of an alive task
	 */
	public boolean alive() {
	    return true;
	}
    }

    SimpleHBTask dummyTsk = new DummyHBTask();
    SimpleHBTask tmpTsk2;
    SimpleHBTask tmpTsk;

    int nWCETread    = 300;
    int nWCETexecute = 300;
    int nWCETwrite   = 300;
    
    SimpleHBTask[] tsk;

    final int MAXTASK = 20; // maximum number of tasks to be observed
    int size = 0;
    int ipos = 0; // the current fill position

    boolean cond;
    int nError = 0;
    int tmp1 = 0;
    int tmp;
    int i;
    SharedIMem iwrt;

    // Constructor 
    public STGuard(SharedIMem iwrt, int size) {
	cond = (size <= 0 || size >= MAXTASK); // size must me at least 1 and at maximum MAXTASK.
	nError = Native.condMove(1, nError, cond);

	this.size = size;
	tsk = new SimpleHBTask[size];
        this.iwrt = iwrt;
    }

    /**
     * Add a task to be monitored.
     */
    public void addTask(SimpleHBTask task) {
	cond = (ipos >= size);
	nError = Native.condMove(1, nError, cond);
	tmp1 = Native.condMove(0, ipos, cond); // bound insert index

	/* update insert index */
	tmp = ipos+1;
	ipos = Native.condMove(ipos, tmp, cond); 

	/* insert new task is array slot is free */
	tmpTsk = tsk[tmp1];
	tsk[tmp1] = (SimpleHBTask) Native.condMoveRef(tmpTsk, task, cond);
    }
	
    /**
     * Reads the error status.
     */
    public boolean error() {
	return (nError != 0);
    }
    
    /**
     * Perform read access to shared data.
     */
    public void read() {
	for (i = 0; i < MAXTASK; i++) { //@WCA loop=20
	    /* check whether the current index represents a valid task */
	    cond = (i >= ipos);
	    tmp = Native.condMove(0, i, cond); // bound i to an index with valid tasks

	    /* check whether the task of current index is valid */
	    tmpTsk2 = tsk[tmp];
	    cond = (tmpTsk2 != null);
	    tmpTsk = (SimpleHBTask) Native.condMoveRef(tmpTsk2, dummyTsk, cond);

	    /* check whether the task is alive */
	    cond = (tmpTsk.alive == false);
	    nError = Native.condMove(1, nError, cond);	    
	}
	//System.out.println("STGuard.read()");
    }
	
    /**
     * Execute task logic. Read and write access to shared data is forbidden.
     */
    public void execute() {
    }
	
    /**
     * Write results to the shared memory.
     */
    public void write() {
	for (i = 0; i < MAXTASK; i++) { //@WCA loop=20
	    /* check whether the current index represents a valid task */
	    cond = (i >= ipos);
	    tmp = Native.condMove(0, i, cond); // bound i to an index with valid tasks

	    /* check whether the task of current index is valid */
	    tmpTsk2 = tsk[tmp];
	    cond = (tmpTsk2 != null);
	    tmpTsk = (SimpleHBTask) Native.condMoveRef(tmpTsk2, dummyTsk, cond);

	    /* clear the alive of the task */
	    tmpTsk.clearAlive();
	}

	cond = (nError != 0);
	// -1 ... fatal error --> start the hunt for a safe state
        //  0 ... everything is ok, continue as normal
	tmp = Native.condMove(-1, 0, cond);
	iwrt.set(tmp);
    }

    /**
     * Some wrapper methods to enable WCET analysis including cache loading.
     */

    public void readWrapperWCET() {
	read();
    }

    public void executeWrapperWCET() {
	execute();
    }

    public void writeWrapperWCET() {
	write();
    }

}
