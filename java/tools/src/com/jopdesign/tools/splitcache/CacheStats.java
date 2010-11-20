/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.tools.splitcache;

public class CacheStats {
	private int rdCnt;
	private int hitCnt;
	private int invalidateCnt;

	public int getRdCnt()  { return rdCnt;  }
	public int getHitCnt() { return hitCnt; }
	public int getInvalidateCnt() { return invalidateCnt; }

	public CacheStats() {			
	}
	
	public void read(boolean hit) {
		rdCnt++;
		if(hit) hitCnt++;
	}
	
	public void invalidate() {
		this.invalidateCnt++;
	}
	
	public void reset() {
		rdCnt=0;
		hitCnt=0;
		invalidateCnt=0;
	}

	public String toString() {
		double percent = 0;
		if (rdCnt!=0) {
			percent = (hitCnt*100.0/(double)rdCnt);
		}
		return String.format("%10d & %10d & %10d & %.2f\\%% \\\\", rdCnt, hitCnt, invalidateCnt, percent);
	}
}