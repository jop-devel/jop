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
 * It samples an input value and performs sliding averaging.
 * 
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 *
 */
public class STSampler extends SimpleTask {
    int nWCETread    = 300;
    int nWCETexecute = 300;
    int nWCETwrite   = 300;

    int nDat = 0;
    int nDat2 = 0;
    Integer IRd;
    Integer IWrt;

    // Standard Constructor 
    public void STSampler(Integer Rd, Integer Wrt) {
        IRd  = Rd;
        IWrt = Wrt;
    }
    
    /**
     * Perform read access to shared data.
     */
    public void read() {
	nDat2 = IRd.intValue;
    }
	
    /**
     * Execute task logic. Read and write access to shared data is forbidden.
     */
    public void execute() {
	nDat = (nDat + nRd) / 2;
    }
	
    /**
     * Write results to the shared memory.
     */
    public void write() {
	nWrt = nDat;
    }

}
