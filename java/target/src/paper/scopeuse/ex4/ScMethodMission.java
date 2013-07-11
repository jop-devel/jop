/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package scopeuse.ex4;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

/**
 * 
 * @author jrri
 *
 */

public class ScMethodMission extends Mission{
	
	ScMethodMission single = this;

	@Override
	protected void initialize() {
		
		PriorityParameters tempPrio = new PriorityParameters(13);
		
		RelativeTime tStart = new RelativeTime(0,0);
		RelativeTime tPeriod = new RelativeTime(1000, 0);
		PeriodicParameters tempPeriod = new PeriodicParameters(tStart, tPeriod);
		
		StorageParameters tempStorage = new StorageParameters(1024, null, 0, 0);
				
		ScMethodHandler t = new ScMethodHandler(tempPrio, tempPeriod, tempStorage, 512);
		t.register();
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
