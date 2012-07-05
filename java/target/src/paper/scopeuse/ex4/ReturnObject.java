/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package scopeuse.ex4;

/**
 * 
 * @author jrri
 *
 */

public class ReturnObject {
	
	Integer[] keys;
	
	public ReturnObject() {
		
		keys = new Integer[10];
		
		for(int i = 0; i<keys.length; i++){
			keys[i] = new Integer(0);
		}
	}
	
	public void update(int i, Integer I){
		keys[i] = I;
	}
}
