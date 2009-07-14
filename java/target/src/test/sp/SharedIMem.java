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

package sp;

import com.jopdesign.sys.Native;

/**
 * A class to make wrap around primitive int values to get shared memory elements
 * (with reference parameter passing). Using a wrapper class like Integer is no
 * alternative, since the assignment of integer values is only possible via the
 * constructor, which creates a new reference.
 * 
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 *
 */
class SharedIMem {
    int size = 0;
    int[] dat;
    int nError = 0;
    boolean cond;
    int idx = 0;
    int tmp1;
    int tmp;

    // Standard Constructor 
    public SharedIMem() {
	size = 1;
	dat = new int[size];
    }

    // Constructor with Allocation Size 
    public SharedIMem(int size) {
	this.size = size;
	dat = new int[size];
    }

    // return size of shared mem
    public int size() {
	return size;
    }

    // set data index for default access of shared mem
    public void setIndex(int idx) {
	cond = (idx >= size);
	nError   = Native.condMove(1, nError, cond);
	this.idx = Native.condMove(this.idx, idx, cond);
    }

    // get data from default index from shared mem
    public int get() {
	return dat[idx];
    }

    // get data from shared mem
    public int get(int idx) {
	cond = (idx >= size);
	nError = Native.condMove(1, nError, cond);
	tmp1 = Native.condMove(0, idx, cond); // bound index
	tmp1 = dat[tmp1];
	tmp = Native.condMove(-1, tmp1, cond);
	return tmp;
    }

    // set data at default index in shared mem
    public void set(int val) {
	dat[idx] = val;
    }

    // set data in shared mem
    public void set(int val, int idx) {
	cond = (idx >= size);
	nError = Native.condMove(1, nError, cond);
	tmp1 = Native.condMove(0, idx, cond); // bound index
        tmp = dat[tmp1];
	dat[tmp1] = Native.condMove(tmp, val, cond);
    }

    // return the error status of the shared mem
    public boolean error() {
	return (nError != 0);
    }

    // resets the error status of the shared mem
    public void clearError() {
	nError = 0;
    }




}



