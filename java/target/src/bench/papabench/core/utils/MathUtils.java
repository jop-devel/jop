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

import papabench.core.autopilot.data.Position2D;


/**
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
final public class MathUtils {
	
	public static final double TWO_PI = 2*Math.PI; 
	 
	public static float symmetricalLimiter(float value, float limit) {
		if (value > limit) return limit;
		if (value < -limit) return -limit;
		
		return value;
	}
	
	public static float asymmetricalLimiter(float value, float lowLimit, float highLimit) {
		if (value > highLimit) return highLimit;
		if (value < lowLimit) return lowLimit;
		
		return value;
	}
	
	public static float normalizeRadAngle(float angle) {
		while(angle > Math.PI) angle -= TWO_PI;
		while(angle < - Math.PI) angle += TWO_PI;
		
		return angle;
	}
	
	public static float scalarProduct(Position2D a, Position2D b, Position2D c) {
		return (a.x - b.x) * (a.x-c.x) + (a.y-b.y)*(a.y - c.y);
	}
	
}
