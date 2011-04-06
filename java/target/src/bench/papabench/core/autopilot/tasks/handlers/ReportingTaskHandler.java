/* $Id$
 * 
 * This file is a part of jPapaBench providing a Java implementation 
 * of PapaBench project.
 * Copyright (C) 2010  Michal Malohlava <michal.malohlava_at_d3s.mff.cuni.cz>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.modules.AutopilotModule;

/**
 * Reports airplane state to ground central.
 * 
 * Notes:
 *   - called as 'downlink' in original PapaBench implementation
 *   
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class ReportingTaskHandler implements Runnable {
	
	private AutopilotModule autopilotModule;
	int counter = 0;
	
	public ReportingTaskHandler(AutopilotModule autopilotModule) {	
		this.autopilotModule = autopilotModule;
	}

	public void run() {
		// This task has period 100ms => count the number of executions and send 
		// different information according to a counter.
		
		// send attitude (500ms)		
		// send GPS position
		// send IR status
		// send ability to take-off
	}
	
	protected void sendGPS() {	
		
	}
	
	protected void sendIRStatus() {		
	}
	
	protected void sendTakeOff() {		
	}
}
