/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package sp;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

/**
 * Some low-level measurements for single-path CMP programming.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Measure {

	final static int TDMA_LENGTH = 3*6;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		lowLevel();
	}
	
	public static void lowLevel() {

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		int frequ = new STScheduler(1).getMsCycles()*1000;
		int shift = frequ/10/(TDMA_LENGTH)*TDMA_LENGTH+1;
		
		int a[] = new int[3];
		
		for (int i=0; i<a.length; ++i) {
			a[i] = 1;
		}
		
		int time = sys.cntInt;
		time = sys.cntInt - time;
		int off = time;
		
		int start = sys.cntInt + shift;
		for (int i=0; i<30; ++i) {
			sys.deadLine = start;
			time = sys.cntInt;
			int sum=0;
			for (int j=0; j<a.length; ++j) {
				a[j] += 12345678;
			}
			time = sys.cntInt - time;
			System.out.println(time-off);
			start += shift;
		}
	}
	
	public static void sort() {

		SPBubbleSort bs = new SPBubbleSort();
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		int frequ = new STScheduler(1).getMsCycles()*1000;
		int shift = frequ/10/(TDMA_LENGTH)*TDMA_LENGTH+1;
		
		
		bs.read();
		
		int time;
		int start = sys.cntInt + shift;
		for (int i=0; i<100; ++i) {
			sys.deadLine = start;
			time = sys.cntInt;
			bs.execute();
			time = sys.cntInt - time;
			System.out.println(time);
			start += shift;
		}

	}

}
