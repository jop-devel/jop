/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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

package examples.scopes;

import javax.realtime.ScopedMemory;
import javax.realtime.ScratchpadScope;

public class LocalScope {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		ScopedMemory scope = new ScratchpadScope();
		System.out.print("Size of the scratchpad RAM is ");
		System.out.println(scope.size());
		Runnable run = new Runnable() {
			public void run() {
				// just generate garbage
				for (int i=0; i<5; ++i) {
					System.out.print("i="+i+" ");
					System.out.println();
				}
			}			
		};

		for (int i=0; i<5; ++i) {
			System.out.println("enter");
			scope.enter(run);
		}
	}

}
