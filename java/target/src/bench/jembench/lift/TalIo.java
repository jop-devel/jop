/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * Created on 12.07.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jembench.lift;

/**
 * @author martin
 *
 */
public class TalIo {
	
	boolean[] in;
	boolean[] out;
	int[] analog;
	boolean[] led;
	
	public TalIo() {
		in = new boolean[10];
		out = new boolean[4];
		analog = new int[3];
		led = new boolean[14];
		for (int i=0; i<10; ++i) in[i]	= false;	
		for (int i=0; i<4; ++i) out[i]	= false;	
		for (int i=0; i<3; ++i) analog[i]	= 0;	
		for (int i=0; i<14; ++i) led[i]	= false;	
	}

}
