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

/**
 * Configuration of IR sensor.
 * 
 * TODO this should be generated according to airplane configuration
 * 
 * @author Michal Malohlava
 *
 */
public interface IRConf {

	public static final int DEFAULT_IR_CONTRAST = 200; 
	
//	public static final int DEFAULT_IR_ROLL_NEUTRAL = -915; // FIXME this value is not handled correctly by simulator
	public static final int DEFAULT_IR_ROLL_NEUTRAL = 0;
	
//	public static final int DEFAULT_IR_PITCH_NEUTRAL = 110; // FIXME this value is not handled correctly by simulator
	public static final int DEFAULT_IR_PITCH_NEUTRAL = 0;

	
	public static final float IR_RAD_OF_IR_CONTRAST = 0.75f;
	
	public static final float IR_RAD_OF_IR_MAX_VAL = 0.0045f;
	
	public static final float IR_RAD_OF_IR_MIN_VAL = 0.00075f;

}
