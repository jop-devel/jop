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
package papabench.core.simulator.model.impl;

import papabench.core.autopilot.data.Position3D;
import papabench.core.commons.data.RadioCommands;
import papabench.core.simulator.conf.SimulatorConf;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.model.State;

/**
 * Simulator implementation.
 * 
 * @author Michal Malohlava
 *
 */
public class FlightModelImpl implements FlightModel {
	
	private State state;
	private Position3D wind;

	public void init() {
		state = new StateImpl();
		state.init();
		
		wind = new Position3D(0, 0, 0);		
	}
	
	public State getState() {
		return this.state;
	}

	public void updateState() {
		state.updateState(SimulatorConf.FM_PERIOD, wind);		
	}
	
	public void processCommands(RadioCommands commands) {
		state.updateState(commands);
	}
}
