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
 * It represents a PID controller (proportional, integral, and differential part).
 * 
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 *
 */
public class STController extends SimpleHBTask {
    int nWCETread    = 300;
    int nWCETexecute = 300;
    int nWCETwrite   = 300;

    int nSetVal = 0;
    int nCurrVal = 0;
    int nCtrlVal = 0;
    SharedIMem ShmSetVal;
    SharedIMem ShmCurrVal;
    SharedIMem ShmCtrlVal;
    int integral = 0;
    int derivative;
    int lastErr = 0;
    int newErr;
    int dt = 1;
    float kp = 1.0F;
    float ki = 1.0F;
    float kd = 1.0F;

    // Constructor 
    public STController(SharedIMem SetVal, SharedIMem CurrVal, SharedIMem CtrlVal) {
        ShmSetVal  = SetVal;
	ShmCurrVal = CurrVal;
	ShmCtrlVal = CtrlVal;
    }

    // Set the amplification values of the controller
    public void setAmplification(float kp, float ki, float kd) {
	this.kp = kp;
	this.ki = ki;
	this.kd = kd;
    }
    
    /**
     * Perform read access to shared data.
     */
    public void read() {
	nSetVal  = ShmSetVal.get();
	nCurrVal = ShmCurrVal.get();
	//System.out.println("STController.read()");
    }
	
    /**
     * Execute task logic. Read and write access to shared data is forbidden.
     */
    public void execute() {
	newErr     = nSetVal = nCurrVal;
	integral   = integral + newErr * dt;
	derivative = (newErr - lastErr) / dt;
	nCtrlVal   = (int)(kp*newErr + ki*integral + kd*derivative);
	lastErr    = newErr;
	this.setAlive();
    }
	
    /**
     * Write results to the shared memory.
     */
    public void write() {
	ShmCtrlVal.set(nCtrlVal);
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
