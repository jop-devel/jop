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
package papabench.core.fbw.conf;

import papabench.core.commons.conf.CommonTaskConfiguration;

/**
 * Fly-by-wire unit tasks configuraiton.
 * 
 * TODO this should be generated according to airplane configuration
 * 
 * @author Michal Malohlava
 *
 */
public interface PapaBenchFBWConf {
	
	public static final int SLOWMOTION = 3;

	/**
	 * Test PPM (?) task configuration.
	 */
	public static interface TestPPMTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "TestPPM";		
		public static final int PRIORITY = 35;		
		public static final int PERIOD_MS = 25*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	/**
	 * Check Failsafe task configuration.
	 */
	public static interface CheckFailsafeTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "CheckFailsafe";		
		public static final int PRIORITY = 23;		
		public static final int PERIOD_MS = 50*SLOWMOTION;	
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	/**
	 * Check mega128 values task configuration.
	 */
	public static interface CheckMega128ValuesTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "CheckMega128Values";		
		public static final int PRIORITY = 22;		
		public static final int PERIOD_MS = 50*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	/**
	 * Send data to autopilot task configuration.
	 */
	public static interface SendDataToAutopilotTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SendDataToAutopilot";		
		public static final int PRIORITY = 34;		
		public static final int PERIOD_MS = 25*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	
}
