/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package jembench.micro;

import jembench.SerialBenchmark;

public class SyncMethod extends SerialBenchmark {

	public String toString() {

		return "synchronized Method";
	}


	public int perform(int cnt) {

		int i;

		for (i=0; i<cnt; ++i) {
			sync_meth();
		}
		return 0;
	}


	public int overhead(int cnt) {

		int i;

		for (i=0; i<cnt; ++i) {
			nosync_meth();
		}
		return 0;
	}
			
	
	
	private synchronized void sync_meth(){
		int a = 0;
		int b = 123;
		a = a+b;			
	}

	private void nosync_meth(){
		int a = 0;
		int b = 123;
		a = a+b;			
	}

}
