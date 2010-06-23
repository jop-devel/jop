/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) Thomas B. Preusser <thomas.preusser@tu-dresden.de>

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

package jembench;

/**
 * Base class of ParallelBenchmarks with independent subroblems
 * numerated to the interval [0, getSize()).
 *
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 */
public abstract class NumeratedParallelPool extends ParallelBenchmarkPool {

  /**
   * Determines the remaining subproblems of a benchmark run as [0, todo),
   */
  private int  todo;

  protected NumeratedParallelPool() {
    todo = -1; // Nothing to do
  }

  protected final synchronized Runnable getWorker() {
    todo = getSize();
    return  new Runnable() {
      public void run() {
	int  sub;
	while((sub = getSubproblem()) >= 0)  execute(sub);
      }
    };
  }
  /**
   * Obtain the number of an outstanding subproblem.
   *
   * @return an outstandign subproblem number or a negative result if done
   */
  final synchronized int getSubproblem() {
    return --todo;
  }

  /**
   * Returns the total subproblem count.
   */
  protected abstract int  getSize();

  /**
   * Executes the provided subproblem.
   */
  protected abstract void execute(int sub);
}