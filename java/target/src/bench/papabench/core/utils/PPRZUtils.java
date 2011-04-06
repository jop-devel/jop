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
package papabench.core.utils;

import papabench.core.commons.conf.RadioConf;

/**
 * PPRZ converter.
 * 
 * Should be generated according to a target PPRZ device.
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
final public class PPRZUtils {
	
//	@SCJAllowed
	public static final float floatOfPPRZ(int value, float center, float travel) {
		return center + (value / RadioConf.MAX_PPRZ * travel);		
	}
	
//	@SCJAllowed
	public static final float trimPPRZ(float value) {
		return MathUtils.asymmetricalLimiter(value, RadioConf.MIN_PPRZ, RadioConf.MAX_PPRZ);
	}
	
	public static final float trimuPPRZ(float value) {
		return MathUtils.asymmetricalLimiter(value, 0, RadioConf.MAX_PPRZ);
	}
	
}
