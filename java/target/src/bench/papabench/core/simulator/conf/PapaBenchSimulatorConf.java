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
package papabench.core.simulator.conf;

import papabench.core.commons.conf.CommonTaskConfiguration;

/**
 * Simulator tasks configuration parameters.
 * 
 * TODO this part should be generated from AADL design.
 * 
 * @author Michal Malohlava
 *
 */
public interface PapaBenchSimulatorConf {
	
	public static final int SLOWMOTION = 3;

	public static interface SimulatorFlightModelTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SimulatorFlightModelTask";		
		public static final int PRIORITY = 26;		
		public static final int PERIOD_MS = 25*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	public static interface SimulatorGPSTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SimulatorGPSTask";		
		public static final int PRIORITY = 28;		
		public static final int PERIOD_MS = 250*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
	
	public static interface SimulatorIRTaskConf extends CommonTaskConfiguration {
		public static final String NAME = "SimulatorIRTask";		
		public static final int PRIORITY = 27;		
		public static final int PERIOD_MS = 50*SLOWMOTION;
		public static final int RELEASE_MS = 0;
		public static final int SIZE = 0;
	}
}
