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
 * Created on 24.06.2005
 *
 */
package gctest;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class RefArray {

	static RefArray ra[];
	
	int abc;
	
	public RefArray(int val) {
		abc = val;
	}
	
	public int getVal() {
		return abc;
	}
	
	public static void main(String[] args) {
		
		for (int i=0; i<4000; i+=10) {
			
			System.out.print("*");
			ra = new RefArray[i];
			for (int j=0; j<i; ++j) {
				ra[j] = new RefArray(i*1000+j);
			}
			
			for (int j=0; j<i; ++j) {
				if (ra[j].abc != i*1000+j) {
					System.out.println("Error: RefArray problem.");
					System.exit(1);
				}
			}
		}
	}
}
