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

/**
 * A single path programming example on JOP. It samples an input value and
 * performs sliding averaging.
 * 
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 * 
 */
public class STSampler extends SimpleHBTask {
	int nWCETread = 300;
	int nWCETexecute = 300;
	int nWCETwrite = 300;

	int buf[];
	int inval;
	int nDat = 0;
	SharedIMem ird;
	SharedIMem iwrt;

	public STSampler(SharedIMem ird, SharedIMem iwrt) {
		this.ird = ird;
		this.iwrt = iwrt;
		buf = new int[2];
	}

	/**
	 * Perform read access to shared data.
	 */
	public void read() {
		inval = ird.get();
	}

	/**
	 * Execute task logic. Read and write access to shared data is forbidden.
	 * MS: a running average uses a buffer and not the last average value. Gives
	 * also different r/x/w cycles ;-)
	 */
	public void execute() {
		buf[1] = buf[0];
		buf[0] = inval;
		nDat = (buf[1] + buf[0]) >> 1;
		this.setAlive();
	}

	/**
	 * Write results to the shared memory.
	 */
	public void write() {
		iwrt.set(nDat);
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
