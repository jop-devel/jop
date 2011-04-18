/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) Martin Schoeberl   <martin@jopdesign.com>
                Thomas B. Preusser <thomas.preusser@tu-dresden.de>

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

package jembench.parallel;

import jembench.ParallelBenchmark;

/**
 * Benchmark based on the N-Queens Puzzle. It calculates the number of valid
 * solutions for the given problem size N. The search space is explored entirely
 * - without exploiting any optimizations due to symmetries. The parallel
 * implementation partitions the search space by the pre-placement of the L
 * initial columns.
 * 
 * The check value returned by execute() is the determined solution count. It
 * may be verified by sequence A000170 of Sloane's On-Line Encyclopedia of
 * Integer Sequences:
 * 
 * http://www.research.att.com/~njas/sequences/A000170
 * 
 * Be aware of the computational complexity as you increase N!
 * 
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 */
public class NQueens extends ParallelBenchmark implements Runnable {

	// Problem Size
	private final int N;
	private final int L;

	// Preplacement Calculation
	private final int[] bh;
	private final int[] bu;
	private final int[] bd;
	private final int[] sl;
	private int col;

	// Work Collection
	private long total;

	/** Create Benchmark for Standard Size. */
	public NQueens() {
		this(9, 3);
	}

	public NQueens(final int N, final int L) {

		// Check Problem Spec
		if ((L <= 0) || (L > N) || (N > 32))
			throw new IllegalArgumentException();
		this.N = N;
		this.L = L;

		// Memory for Calculation of Pre-Placement
		bh = new int[L];
		if (N < 32)
			bh[0] = -1 << N;
		bu = new int[L];
		bd = new int[L];
		sl = new int[L];
	}

	public String toString() {
		return "NQueens(N=" + N + ";L=" + L + ")";
	}

	public Runnable getWorker() {
		// Reset State
		sl[0] = ~bh[0];
		col = 0;
		total = 0;
		return this; // use myself as worker Thread
	}

	public void run() {
		final int[] cs = new int[3];
		while (true) {
			if (get(cs) == null)
				break;
			add(q(cs[0], cs[1], cs[2]));
		}
	}

	// Calculate & Fetch next Pre-Placement
	private synchronized int[] get(final int[] store) {
		final int[] bh = this.bh;
		final int[] bu = this.bu;
		final int[] bd = this.bd;

		int col;
		if ((col = this.col) < 0)
			return null;

		while (true) {
			int slot;

			if ((slot = sl[col]) == 0) {
				// Retreat one Column
				if (--col < 0) {
					this.col = -1;
					return null;
				}
			} else {
				// Explore next free Position of Column
				sl[col] ^= (slot = slot & -slot);

				if (col < L - 1) {
					// Calculate Blocking for Next Column
					final int p = col++;
					sl[col] = ~((bh[col] = bh[p] | slot)
							| (bu[col] = (bu[p] | slot) << 1) | (bd[col] = (bd[p] | slot) >>> 1));
				} else {
					this.col = col;
					store[0] = bh[col] | slot;
					store[1] = (bu[col] | slot) << 1;
					store[2] = (bd[col] | slot) >>> 1;
					return store;
				}
			}
		}
	}

	// Add a Subtotal
	private synchronized void add(final long subtotal) {
		total += subtotal;
	}

	// Recursively count the Completions of a Subboard
	private static long q(final int bh, final int bu, final int bd) {
		int slots = ~(bh | bu | bd);
		if (slots == 0)
			return (bh == -1) ? 1 : 0;

		long cnt = 0;
		while (slots != 0) {
			final int slot;
			slots ^= (slot = slots & -slots);
			cnt += q(bh | slot, (bu | slot) << 1, (bd | slot) >>> 1);
		}
		return cnt;
	}

}
