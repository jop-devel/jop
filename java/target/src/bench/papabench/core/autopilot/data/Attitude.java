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
package papabench.core.autopilot.data;

/**
 * Structure holding airplane attitude.
 * 
 * The values of fields should be expressed in radians.
 * 
 * For information about flight dynamics see <a
 * href="http://inventors.about.com/library/inventors/blairplanedynamics.htm"
 * >this article</a> or <a href="http://en.wikipedia.org/wiki/Flight_dynamicss">
 * wikipedia</a> or <a href="http://www.princeton.edu/~stengel/MAE331Lectures.html">this page</a>.
 * 
 * @author Michal Malohlava
 * 
 */
public class Attitude {

	public Attitude(float phi, float psi, float theta) {
		super();
		this.phi = phi;
		this.psi = psi;
		this.theta = theta;
	}

	/**
	 * Phi angle - the pitch of airplane.
	 * 
	 * The range is (-PI .. PI). Positive value means the nose of airplane is up else down.
	 * 
	 * 
	 * To roll the plane to the right or left, the ailerons are raised on one
	 * wing and lowered on the other. The wing with the lowered aileron rises
	 * while the wing with the raised aileron drops.
	 * 
	 */
	public float phi;

	/**
	 * Psi angle - the yaw of airplane.
	 * 
	 * The range is (-PI .. PI). Value 0 means North, positive value represents clockwise direction.
	 * 
	 * Yaw is the turning of a plane. When the rudder is turned to one side, the
	 * airplane moves left or right. The airplane's nose is pointed in the same
	 * direction as the direction of the rudder. The rudder and the ailerons are
	 * used together to make a turn.
	 * 
	 * <it>Note: the papabench airplane does not have rudder</it>
	 */
	public float psi;

	/**
	 * Theta angle - the pitch of airplane.
	 * 
	 * The range is (-PI .. PI).  Positive value means the nose of airplane is up else down.
	 * 
	 * Pitch is to make a plane descend or climb. The pilot adjusts the
	 * elevators on the tail to make a plane descend or climb. Lowering the
	 * elevators caused the airplane's nose to drop, sending the plane into a
	 * down. Raising the elevators causes the airplane to climb.
	 */
	public float theta;

	@Override
	public String toString() {
		return "Attitude(phi:" + phi + ", psi:" + psi + ", theta:" + theta
				+ ")";
	}
}
