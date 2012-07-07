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

public class Method implements Runnable{
	
	ParamObject params;
	int K;
	
	Method(ParamObject params){
		
		this.params = params;
		
	}

	@Override
	public void run() {
		
		// Use parameters, do work, create garbage...
		// calculate a constant K that will be used latter...
		// A primitive, so safe to use its value in the
		// runnable
		K = params.param_X * params.param_X;
		
		// Change context, create return object
		params.mem.executeInArea(new Runnable() {
			
			@Override
			public void run() {
				ReturnObject rObject = new ReturnObject();
				
				// Update object fields
				for (int i=0; i<rObject.keys.length; i++){
					rObject.update(i, new Integer(K+i+10));
				}
				
				// Store reference to returned object
				params.retObject = rObject;
				
			}
		});
	}
}
