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



import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Bubble {

	static int[] b;

	public static void main(String[] args) {

		int i;
		int[] a = new int[N];
		b = new int[N];
		for (i=0; i<N; ++i) {
			a[i] = i+1;
		}

/* BCET and WCET

		sort(a);
		for (i=0; i<N; ++i) {
			a[N-i-1] = i+1;
		}
		sort(a);
*/

		mix(N, a);
/*
		for (i=0; i<N; ++i) System.out.println(a[i]);
		sort(a);
		for (i=0; i<N; ++i) System.out.println(a[i]);
*/

	}

	static void mix(int nr, int[] a) {

		int i, j;

		for (i=0; i<nr; ++i) {
			if (nr > 2) {
				mix(nr-1, a);
			} else {
				for (j=0; j<N; ++j) {
//					System.out.print(a[j]);
//					System.out.print(" ");
					b[j] = a[j];
				}
//				System.out.print("- ");
				sort(b);
				for (j=0; j<N; ++j) {
//					System.out.print(b[j]);
//					System.out.print(" ");
				}
//				System.out.println();
			}
			int tmp = a[N-nr];
			for (j=N-nr; j<N-1; ++j) {
				a[j] = a[j+1];
			}
			a[j] = tmp;
		}
	}

	final static int N = 5;

	static void sort(int[] a) {

		int i, j, v1, v2;

		int t1, diff;
		t1 = Native.rd(Const.IO_CNT);
		t1 = Native.rd(Const.IO_CNT)-t1;
		diff = t1;

		t1 = Native.rd(Const.IO_CNT);

		// loop count = N-1
		for (i=N-1; i>0; --i) {
			// loop count = (N-1)*N/2
			for (j=1; j<=i; ++j) {
				v1 = a[j-1];
				v2 = a[j];
				if (v1 > v2) {
					a[j] = v1;
					a[j-1] = v2;
				}
			}
		}

		t1 = Native.rd(Const.IO_CNT)-t1;
		System.out.println(t1-diff);
	}

}
