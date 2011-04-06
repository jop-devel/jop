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
package papabench.core.commons.data.impl;

import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.data.Position3D;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.UTMPosition;
import papabench.core.utils.PPRZUtils;


/**
 * Simple flight plan.
 * 
 * @author Michal Malohlava
 * 
 */
public class Simple01FlightPlan extends AbstractFlightPlan {
	
	protected static final int GROUND_ALTITUDE = 125;
	protected static final int SECURE_ALTITUDE = 150;
	

	public String getName() {
		return "Simple 01 flight plan";
	}
	
	public float getGroundAltitude() {
		return GROUND_ALTITUDE;
	}
	
	public float getSecureAltitude() {		
		return SECURE_ALTITUDE;
	}

	@Override
	protected int getNumberOfNavBlocks() {
		return 2;
	}

	@Override
	protected int getNumberOfWaypoints() {
		return 2;
	}

	@Override
	protected void initNavigationBlocks() {
		addNavBlock(new NavigationBlock(3))
			.addNavStage(new NavigationStage() {				
				@Override
				protected void execute() {	
					// launch airplane
					status().setLaunched(true);
					nextStage();
				}
			}).addNavStage(new NavigationStage() {				
				@Override
				protected void execute() {
					// climb for a while with course 15degrees and then switch to the next stage
					if (estimator.getFlightTime() > 8) {
						nextStage();
					} else {
						navigator().setDesiredCourse((float) Math.toRadians(15));
						navigator().setAutoPitch(false);
						navigator().setDesiredPitch(0.15f);						
						navigator().setDesiredGaz((int) PPRZUtils.trimuPPRZ(0.8f * RadioConf.MAX_PPRZ));
						status.setVerticalFlightMode(VerticalFlightMode.AUTO_GAZ);
					}																									
				}
			}).addNavStage(new NavigationStage() {
				// climb to desired secure altitude with course 270degrees
				@Override
				protected void execute() {					
					if (estimator.getPosition().z > getSecureAltitude()) {
						nextStage(); // --> nextBlock
					} else {
						navigator().setDesiredCourse((float) Math.toRadians(270));
						navigator().setAutoPitch(false);
						navigator().setDesiredPitch(0.0f);
						status.setVerticalFlightMode(VerticalFlightMode.AUTO_CLIMB);
						status.setClimb(8f);
					}
				}
			});
		
		addNavBlock(new NavigationBlock(1))
			.addNavStage(new NavigationStage() { // stage 0				
				@Override
				protected void execute() {
					if (approaching(1)) { 
						nextStageFrom(1);
					} else {
						flyToWP(1); // 0,0,200
						navigator().setAutoPitch(false);
						navigator().setDesiredPitch(0f);						
						navigator().setDesiredAltitude(WPALT(1));
						navigator().setPreClimb(0);
						status().setVerticalFlightMode(VerticalFlightMode.AUTO_ALTITUDE);
					}
				}
			});

	}

	@Override
	protected void initWaypoints() {		
		addWaypoint(new Position3D(0f, 0f, 200f)); // 0	
		addWaypoint(new Position3D(115f, -75f, 200f)); // 1		
	}

	@Override
	protected UTMPosition getCenterUTMPosition() {
		return null;
	}
}
