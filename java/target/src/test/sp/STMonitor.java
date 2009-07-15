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

    SharedIMem ShmSetVal;
    SharedIMem ShmCurrVal;
    SharedIMem ShmCtrlVal;
    STGuard tskGuard;

    int nSetVal;
    int nCurrVal;
    int nCtrlVal;
    boolean bError;

    // Constructor 
    public STMonitor(SharedIMem SetVal, SharedIMem CurrVal, 
			  SharedIMem CtrlVal, STGuard tskGuard) {
	ShmSetVal  = SetVal;
	ShmCurrVal = CurrVal;
	ShmCtrlVal = CtrlVal;
	this.tskGuard = tskGuard;
    }
    
    /**
     * Perform read access to shared data.
     */
    public void read() {
	nSetVal  = ShmSetVal.get();
	nCurrVal = ShmCurrVal.get();
	nCtrlVal = ShmCtrlVal.get();
	bError = tskGuard.error();
	System.out.println("STMonitor.read()");
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

}
