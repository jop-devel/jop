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
package papabench.core.autopilot.conf;

import papabench.core.commons.conf.CommonTaskConfiguration;

/**
 * Configuration for Autopilot subsystem - declares timing properties for the tasks.
 * 
 * @see CommonTaskConfiguration
 * 
 * @author Michal Malohlava
 *
 * TODO this class should be automatically generated according to AADL design
 */
public class PapaBenchAutopilotConf {
	
	public static final int SLOWMOTION = 3;

	/**
	 * Navigation task configuration
	 */
	public static interface NavigationTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "Navigation";		
		public static final int PRIORITY = 32;
		public static final int PERIOD_MS = 250*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	/**
	 * Altitude control task configuration.
	 *
	 */
	public static interface AltitudeControlTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "AltitudeControl";		
		public static final int PRIORITY = 31;		
		public static final int PERIOD_MS = 250*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;		
	}
	
	/**
	 * Climb control task configuration.
	 *
	 */
	public static interface ClimbControlTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "ClimbControl";		
		public static final int PRIORITY = 30;		
		public static final int PERIOD_MS = 250*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;		
	}
	
	/**
	 * Stabilization task configuration.
	 *
	 */
	public static interface StabilizationTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "Stabilization";		
		public static final int PRIORITY = 29; /* has to have higher prior than LinkFBWSendTaskConf */		
		public static final int PERIOD_MS = 50*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;		
	}
	
	/**
	 * Radio control task configuration.
	 */
	public static interface RadioControlTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "RadioControl";		
		public static final int PRIORITY = 33;		
		public static final int PERIOD_MS = 25*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;		
	}
	
	/**
	 * Link fbw send task configuration
	 */
	public static interface LinkFBWSendTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "LinkFBWSend";		
		public static final int PRIORITY = 21;		
		public static final int PERIOD_MS = 50*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;		
	}
	
	/**
	 * Reporting task configuration
	 */
	public static interface ReportingTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "Reporting";
		public static final int PRIORITY = 19;		
		public static final int PERIOD_MS = 100*SLOWMOTION; // TODO: check this in AADL there is f=10Hz, in code f=20Hz
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;		
	}

}
