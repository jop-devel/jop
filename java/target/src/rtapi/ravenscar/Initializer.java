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

package ravenscar;
//import javax.realtime.*;
import joprt.*;

public class Initializer // extends javax.realtime.RealtimeThread
{
	
	public Initializer() {
		/*
		 super( new javax.realtime.PriorityParameters( 
		 ((javax.realtime.PriorityScheduler)javax.realtime.Scheduler.getDefaultScheduler()).
		 getMaxPriority()), null, null, javax.realtime.ImmortalMemory.instance(), null, null);
		 */
		// to avoid stack issues we invoke it here
		RtThread.initClass();
	}
 
 	public void run() {
 	}
 	
 	public void start() {
 		run();
 		RtThread.startMission();
 		// perhaps do the WD thing
 		for (;;)
 			;
 	}
}
