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
 * Benchmark execution environment for JBE.
 *
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 * 
 * TODO: only pool is used - all other code is now in BenchmarkX
 * should be removed
 */

public class Executor {

  private ThreadPool  pool;
  private static Executor exec;
  
  static Executor getExecutor() {
	  if (exec==null) {
		  exec = new Executor();
	  }
	  return exec;
  }

  //+ Creation +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  public Executor() {}

  //+ Parallel Execution Control +++++++++++++++++++++++++++++++++++++++++++++
  public void startParallel() {
    stopParallel();
    pool = new ThreadPool(Util.getNrOfCores()-1);
  }

  public void stopParallel() {
    if(pool != null)  pool.die();
    pool = null;
  }
  
  public ThreadPool getPool() {
	  return pool;
  }

  //+ Benchmark Measurement ++++++++++++++++++++++++++++++++++++++++++++++++++
//  public int measure(final Benchmark  bench) {
//    // Complexity
//    int  c = 1;
//
//    // Calibrating Measurements
//    while(true) {
//
//      // Measure with Overhead
//      long  time = bench.accept(this, c);
//
//      // Quit pointless Measurement
//      if(time <     0L)  return  0;
//
//      // Check if minimum Run Time of 1 Sec reached
//      if(time >= 1000L)  return (int)(1000L*c / time);
//
//      // Increase Complexity exponentially
//      if(time <   120L)  time = 120L;
//      do {
//	// Fail when maximum Complexity is reached
//	if((c <<= 1) < 0)  return  0;
//	time <<= 1;
//      }
//      while(time <= 800L);
//    }
//  }

  //+ Benchmark executions dispatched on Benchmark Type
  long execute(SerialBenchmark  bench,
	       int              complexity) {

    final long  t0 = System.currentTimeMillis();
    bench.perform (complexity);
    long  t1 = System.currentTimeMillis();
    bench.overhead(complexity);
    long  t2 = System.currentTimeMillis();

    t2 -= t1;       // Overhead
    t1 -= t0 + t2;  // True Time
    if(t1 <          0L)  t1 = 0L;
    if(t2 > ((t1+1)<<5))  return  -1;
    return  t1;
  }

  long execute(ParallelBenchmarkPool  bench,
	       int                complexity) {

    final ThreadPool  pool = this.pool;

    final long  start = System.currentTimeMillis();
    while(--complexity >= 0) {
      // Initialize Benchmark
      final Runnable  worker = bench.getWorker();

      // Start Workers
      for(int  i = pool.getSize(); --i >= 0; pool.pushTask(worker));
      worker.run();

      // Join Threads
      pool.waitForAll();
    }
    return  System.currentTimeMillis() - start;
  }

  long execute(StreamBenchmark  bench,
	       int              complexity) {

    final ThreadPool  pool = this.pool;
    pool.ensure(bench.getDepth()-1);

    final long  start = System.currentTimeMillis();
    while(--complexity >= 0) {
      // Start Workers
      final Runnable[]  workers = bench.getWorkers();
      for(int  i = workers.length; --i > 0; pool.pushTask(workers[i]));
      workers[0].run();

      // Join Threads
      pool.waitForAll();
    }
    return  System.currentTimeMillis() - start;
  }

}
