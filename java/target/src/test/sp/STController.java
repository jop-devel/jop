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
    float KP = 1.0;
    float KI = 1.0;
    float KD = 1.0;

    // Constructor 
    public void STController(SharedIMem SetVal, SharedIMem CurrVal, SharedIMem CtrlVal) {
        ShmSetVal  = SetVal;
	ShmCurrVal = CurrVal;
	ShmCtrlVal = CtrlVal;
    }

    // Set the amplification values of the controller
    public void setAmplification(float KP, float KI, float KD) {
	this.KP = KP;
	this.KI = KI;
	this.KD = KD;
    }
    
    /**
     * Perform read access to shared data.
     */
    public void read() {
	nSetVal  = ShmSetVal.get();
	nCurrVal = ShmCurrVal.get();
    }
	
    /**
     * Execute task logic. Read and write access to shared data is forbidden.
     */
    public void execute() {
	newErr     = nSetVal = nCurrVal;
	integral   = integral + newErr * dt;
	derivative = (newErr - lastErr) / dt;
	nCtrlVal   = KP*newErr + KI*integral + KD*derivative;
	lastErr    = newErr;
	self.setAlive();
    }
	
    /**
     * Write results to the shared memory.
     */
    public void write() {
	ShmCtrlVal.set(nCtrlVal);
    }

}
