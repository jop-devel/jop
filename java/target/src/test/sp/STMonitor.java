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

//import com.jopdesign.sys.Native;

/**
 * A single path programming example on JOP.
 * This task simply collects some key values from the control
 * application. To make the task useful in practice, one has to
 * fill the write() Method with extra code to transmit the 
 * monitored values.
 * 
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 *
 */
public class STMonitor extends SimpleHBTask {
    int nWCETread    = 300;
    int nWCETexecute = 300;
    int nWCETwrite   = 300;

    SharedIMem shmSetVal;
    SharedIMem shmCurrVal;
    SharedIMem shmCtrlVal;
    STGuard tskGuard;

    int nSetVal;
    int nCurrVal;
    int nCtrlVal;
    boolean bError;

    // Constructor 
    public STMonitor(SharedIMem setVal, SharedIMem currVal, 
			  SharedIMem ctrlVal, STGuard tskGuard) {
	shmSetVal  = setVal;
	shmCurrVal = currVal;
	shmCtrlVal = ctrlVal;
	this.tskGuard = tskGuard;
    }
    
    /**
     * Perform read access to shared data.
     */
    public void read() {
	nSetVal  = shmSetVal.get();
	nCurrVal = shmCurrVal.get();
	nCtrlVal = shmCtrlVal.get();
	bError = tskGuard.error();
	//System.out.println("STMonitor.read(SetVal="+nSetVal+", CurrVal="+nSetVal+", CtrlVal="+nCtrlVal+")");
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
	/* send the data to a host computer... */
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
