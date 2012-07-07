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

package scopeuse.ex1;

public class Data {
	
	static final int N_BLOCKS = 1;
	static int[] data;
	static int size;
	
	public static void init(int s){
		
		size = s;
		data = new int[size];
		
		for(int i = 0; i<data.length; i++){
			data[i] = 0;
		}

	}
	
	public void update(int[] block){
		
		if(block.length != data.length){
			
		}
		
		for(int i = 0; i<data.length; i++){
			data[i] = block[i];
		}
		
	}
}
