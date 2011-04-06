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
package papabench.core.simulator.model;

import papabench.core.autopilot.data.Attitude;
import papabench.core.autopilot.data.Position2D;
import papabench.core.autopilot.data.Position3D;
import papabench.core.commons.data.RadioCommands;

/**
 * Flight model state managed by the simulator.
 * 
 * @author Michal Malohlava
 *
 */
public interface State {
	
	void init();

	float getTime();
	
	Position3D getPosition();
	
	float getAirSpeed();
	
	/**
	 * Z-velocity m/s.
	 * 
	 * @return
	 */
	float getZDot();
	
	Attitude getAttitude();
	
	Attitude getRotationalSpeed(); /* psi, phi theta -> rad/s */
	
	Position2D getDelta();
	
	float getThrust();
	
	void updateState(float dt, Position3D wind);

	void updateState(RadioCommands commands);
}
