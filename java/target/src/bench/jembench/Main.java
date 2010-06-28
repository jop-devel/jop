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

package jembench;

import java.io.PrintStream;

/**
 * Main class to start all benchmarks.
 * 
 * @author
 * 
 */
public final class Main {

	/** As default we disable floating point for small systems */
	static final boolean USE_FLOAT = false;

	// + Static Constants +++++++++++++++++++++++++++++++++++++++++++++++++
	private static final int BENCH_MICRO = 1;
	private static final int BENCH_KERNEL = 2;
	private static final int BENCH_APPLICATION = 4;
	private static final int BENCH_PARALLEL = 8;
	private static final int BENCH_STREAM = 0x10;
	private static final String SPACES = "?           ";

	private final boolean verbose;

	// private final Executor exec;

	private Main(final boolean verbose) {
		this.verbose = verbose;
		// this.exec = new Executor();
	}

	// TODO: printout should go to Util
	private void execute(final Benchmark bench) {
		final PrintStream out = System.out;

		{ // Print Label
			final String label = bench.toString();
			out.print(label);
			for (int i = label.length(); i < 24; i += 8)
				out.print('\t');
		}

		// Measurement
		String s;

		// int res = exec.measure(bench);
		int result = bench.measure();
		if (verbose) {
			s = String.valueOf(bench.getCheckSum());
			out.print(SPACES.substring(s.length()));
			out.print(s);
		}

		// Output Score
		s = (result == -1) ? "n/a" : String.valueOf(result);
		out.print(SPACES.substring(s.length()));
		out.print(s);
		if (result < 100 && result > -1) {
			out.print(".");
			int v = bench.getTwoDecimal();
			if (v < 10) {
				out.print("0");
			}
			out.print(v);
		}
		out.println();
	}

	private void printVersion() {
		System.out.println("JemBench V 1.8");
		System.out.println();
	}
	
	private void runMicroBenchmarks() {
		System.out.println("Micro Benchmarks:");
		execute(new jembench.micro.Add());
		execute(new jembench.micro.Mul());
		execute(new jembench.micro.Div());
		if (USE_FLOAT)
			execute(new jembench.micro.Fadd());
		execute(new jembench.micro.Ldc());
		execute(new jembench.micro.Iinc());
		execute(new jembench.micro.Array());
		execute(new jembench.micro.BranchNotTaken());
		execute(new jembench.micro.BranchTaken());
		execute(new jembench.micro.Checkcast());
		execute(new jembench.micro.GetField());
		execute(new jembench.micro.GetStatic());
		execute(new jembench.micro.InvokeInterface());
		execute(new jembench.micro.InvokeStatic());
		execute(new jembench.micro.InvokeVirtual());
		execute(new jembench.micro.SyncThis());
		execute(new jembench.micro.SyncMethod());
		System.out.println();
	}

	private void runKernelBenchmarks() {
		System.out.println("Kernel Benchmarks:");
		execute(new jembench.kernel.Sieve());
		execute(new jembench.kernel.BubbleSort());
		System.out.println();
	}

	private void runApplicationBenchmarks() {
		System.out.println("Application Benchmarks:");
		execute(new jembench.application.BenchKfl());
		execute(new jembench.application.BenchLift());
		execute(new jembench.application.BenchUdpIp());
		System.out.println();
	}

	private void runParallelBenchmarks() {
		System.out.println("Parallel Benchmarks:");
		execute(new jembench.EnumeratedParallelBenchmark());
		execute(new jembench.parallel.MatrixMul());
		execute(new jembench.parallel.NQueens());
		if (USE_FLOAT)
			execute(new jembench.parallel.raytrace.Raytrace());
		System.out.println();
	}

	private void runStreamBenchmarks() {
		System.out.println("Stream Benchmarks:");
		execute(new jembench.stream.AES());
		System.out.println();
	}

	public static void main(String[] args) {

		// Evaluate Parameters
		boolean verbose = false;
		int benches = 0;
		// How do you expect that an embedded system gets command line
		// arguments?
		// With JOP I simply pass a null pointer to main.
		if (args != null) {
			for (int i = args.length; --i >= 0;) {
				final String arg = args[i];
				if ("-micro".startsWith(arg))
					benches |= BENCH_MICRO;
				else if ("-kernel".startsWith(arg))
					benches |= BENCH_KERNEL;
				else if ("-application".startsWith(arg))
					benches |= BENCH_APPLICATION;
				else if ("-parallel".startsWith(arg))
					benches |= BENCH_PARALLEL;
				else if ("-stream".startsWith(arg))
					benches |= BENCH_STREAM;
				else if ("-verbose".startsWith(arg))
					verbose = true;
				else {
					System.err
							.println("jbe.Main [-verbose] [-<bench_group> ...]\n\n"
									+ "The JBE Benchmark Suite.\n"
									+ "\tOutput: Benchmark [Complexity Checksum] Score\n"
									+ "\t-verbose\tverbose output messages\n\n"
									+ "Benchmark Groups <bench_group>:\n"
									+ "\t-micro\n"
									+ "\t-kernel\n"
									+ "\t-application\n"
									+ "\t-parallel\n"
									+ "\t-stream\n\n"
									+ "Without the selection of benchmark groups, all benchmarks will be executed.\n"
									+ "All Options may be given as unique prefix.\n");
					return;
				}

			}
		}
		if (benches == 0)
			benches = -1;

		// Execute Benchmark Groups
		final Main main = new Main(verbose);
		main.printVersion();
//		main.runParallelBenchmarks();
		
		if ((benches & BENCH_MICRO) != 0)
			main.runMicroBenchmarks();
		if ((benches & BENCH_KERNEL) != 0)
			main.runKernelBenchmarks();
		if ((benches & BENCH_APPLICATION) != 0)
			main.runApplicationBenchmarks();
		Executor.getExecutor().startParallel();
		if ((benches & BENCH_PARALLEL) != 0)
			main.runParallelBenchmarks();
		if ((benches & BENCH_STREAM) != 0)
			main.runStreamBenchmarks();
		Executor.getExecutor().stopParallel();
	}
}
