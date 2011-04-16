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
package papabench.core.commons.conf;

import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.data.impl.RadioCommandsImpl;

/**
 * Radio configuration.
 * 
 * TODO this should be generated according to airplane configuration
 * 
 * @author Michal Malohlava
 *
 */
public interface RadioConf {
	
	public static final RadioCommands safestateRadioCommands = new RadioCommandsImpl(); 
	
	/* depends on selected airframe - in Paparazzi implementation it is generated */
	public static final byte RADIO_CTL_NB = 9;
	
	// FIXME - see link_autopilot.h
	public static final int MAX_PPRZ = 600*16; // depends on CPU frequency
	public static final int MIN_PPRZ = - MAX_PPRZ;
	
	public static final int GAZ_THRESHOLD_TAKEOFF = (int)(MAX_PPRZ * 0.9f);
	
	public static final int STALLED_TIME = 30;  // ~500ms with a 60Hz timer
	public static final int REALLY_STALLED_TIME = 300; // 5s with a 60Hz timer
	
	public static final float MAX_THRUST = MAX_PPRZ;
	public static final float MIN_THRUST = 0;
	
}
