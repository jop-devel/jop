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

package jbe;

public class BenchSieve extends BenchMark {

	final static int SIZE = 100;
	static boolean flags[];

	public BenchSieve() {

		flags = new boolean[SIZE+1];
	}

	public int test(int cnt) {

		int i, prime, k, count;
		count=0;

		for (int j=0; j<cnt; ++j) {
			count=0;
			for(i=0; i<=SIZE; i++) flags[i]=true;
			for (i=0; i<=SIZE; i++) {
				if(flags[i]) {
					prime=i+i+3;
					for(k=i+prime; k<=SIZE; k+=prime)
						flags[k]=false;
					count++;
				}
			}
		}

		return count;
	}


	public String toString() {

		return "Sieve";
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchSieve();

		Execute.perform(bm);
	}
			
}
