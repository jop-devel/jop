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
package papabench.core.autopilot.modules.impl;

import papabench.core.autopilot.data.Attitude;
import papabench.core.autopilot.data.HorizSpeed;
import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.commons.conf.AirframeParametersConf;
import papabench.core.commons.modules.Module;
import papabench.core.utils.LogUtils;
import papabench.core.utils.MathUtils;

/**
 * A module which estimates a position of plane in a space based on data from 
 * sensors.
 * 
 * Notes:
 *  - internal data are allocated in the same memory as this module.
 *  
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class EstimatorModuleImpl implements Estimator, Module {
	
	protected static final float RHO = 0.999f; /* The higher, the slower the estimation is changing */
	protected static final float INIT_WEIGHT = 100.0f; /* The number of times the initial value has to be taken */
	
	private GPSDevice gpsDevice;
	private IRDevice irDevice;
	
	/* position in meters */
	private Position3D position; 
	
	/* 
	 * airplane attitude in radians
	 * x -> + = right	(PHI) 
     * y -> CW, 0 = N	(PSI)
     * z -> + = up		(THETA)
     * theta = The pitch in degrees relative to a plane(mathematical) normal to the earth radius line at the point the plane(aircraft) is at
	 * phi = The roll of the aircraft in degrees
	 * psi = The true heading of the aircraft in degrees
	 */
	private Attitude attitude; 	
	
	/* speed in meters per second */
	private Position3D speed;   

	/* rotational speed in radians per second (phi,psi,theta) */
	private Position3D rotationalSpeed;
		
	/* flight time in seconds */
	private int flightTime = 0;

	/* horizontal ground speed in module and dir (m/s, rad (CW/North)) */
	private HorizSpeed horizontalSpeed; // (estimator_hspeed_mod, estimator_hspeed_dir)
   

	/* Wind and airspeed estimation sent by the GCS */
	private float windEast = 0f, windNorth = 0f; /* m/s */     
	private float airspeed = 0f; /* m/s */
	
	/* IR related values */
	private boolean irEstimationModeEnabled = false;
	private float radOfIR;
	private float ir;
	private float rad;
	private boolean irInitialized = false;
	private float lastHSpeedDir;
	private float lastGPSTow;
	private float sumXX, sumXY;

	public EstimatorModuleImpl() {
		position = new Position3D(0.0f, 0.0f, 0.0f);
		attitude = new Attitude(0.0f, 0.0f, 0.0f);
		speed    = new Position3D(0.0f, 0.0f, 0.0f);
		rotationalSpeed = new Position3D(0.0f, 0.0f, 0.0f);
		horizontalSpeed = new HorizSpeed(0.0f, 0.0f);
	}

	public void init() {
		if (gpsDevice == null || irDevice == null) {
			throw new IllegalArgumentException("Estimator modules is not properly configured");
		}
	}
	
	
	public void updateIR() { // -> estimator_update_ir_estim
		if (irInitialized) {
//			float dt = gpsDevice.getTow() - lastGPSTow;
			// FIXME
			float dt = 0.5f;
			if (dt > 0.1) {
				float phi = (this.horizontalSpeed.direction - lastHSpeedDir);
				phi = MathUtils.normalizeRadAngle(phi);
				phi = phi / dt * AirframeParametersConf.NOMINAL_AIRSPEED / AirframeParametersConf.G;
				phi = MathUtils.normalizeRadAngle(phi);
				
				ir = irDevice.getIrRoll();
				rad = phi;
				
				float absphi = Math.abs(phi);
				if (absphi < 1.0f && absphi > 0.05f 
						&& 
					(-irDevice.getIrContrast()/2 < irDevice.getIrRoll() && irDevice.getIrRoll() < irDevice.getIrContrast()/2) ) {
					
					sumXY = rad * ir + RHO*sumXY;
					sumXY = ir * ir + RHO*sumXX;
					
					radOfIR = sumXY / sumXY;
				}				
			}
			
		} else {
			irInitialized = true;
			
			float initIR = irDevice.getIrContrast();
			initIR = initIR * initIR;
			sumXY = INIT_WEIGHT * radOfIR * initIR;
			sumXX = INIT_WEIGHT * initIR;
		}
		
		lastHSpeedDir = this.horizontalSpeed.direction;
		lastGPSTow = this.gpsDevice.getTow();
	}
	
	public void updateIRState() {
		float radofir; 
		if (irEstimationModeEnabled) 
			radofir = this.radOfIR;
		else 
			radofir = irDevice.getIrRadOfIr();
		
		attitude.phi = radofir * irDevice.getIrRoll(); // phi updated
		attitude.theta = radofir * irDevice.getIrPitch(); // theta updated
	}
	
	public void updatePosition() {
		if (true) { // FIXME
		//if ((gpsDevice.getMode() & 1<<5) == 1) {
			updatePosition(gpsDevice.getEast(), gpsDevice.getNorth(), gpsDevice.getAltitude());
			updateSpeedPol(gpsDevice.getSpeed(), gpsDevice.getCourse(), gpsDevice.getClimb());
						
			// airplane is flying => update roll information
			if (flightTime > 0) {
				updateIR();
			}
		}
	}
	
	public void updateFlightTime() {
		flightTime++;		
		LogUtils.log(this, "Flight time = " + flightTime);
	}
	
	public void setGPSDevice(GPSDevice gpsDevice) {
		this.gpsDevice = gpsDevice;		
	}
	
	public void setIRDevice(IRDevice irDevice) {
		this.irDevice = irDevice;		
	}
	
	public Position3D getPosition() {		
		return this.position;
	}
	
	public Attitude getAttitude() {
		return this.attitude;
	}
	
	public HorizSpeed getHorizontalSpeed() {
		return this.horizontalSpeed;
	}
	
	public Position3D getSpeed() {
		return this.speed;
	}

	public void setFlightTime(int flightTime) {
		this.flightTime = flightTime;
	}

	public int getFlightTime() {
		return flightTime;
	}
	
	protected void updatePosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}
	
	protected void updateAttitude(float phi, float psi, float theta) {
		attitude.psi = phi;
		attitude.phi = psi;
		attitude.theta = theta;
	}
	
	protected void updateSpeedPol(float vhmod, float vhdir, float vz) {
		horizontalSpeed.module = vhmod;
		horizontalSpeed.direction = vhdir;
		speed.z = vz;				
	}
}
