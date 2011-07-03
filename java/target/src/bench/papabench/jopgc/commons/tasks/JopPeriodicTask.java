/* $Id$
 * 
 * This file is a part of jPapaBench providing a Java implementation 
 * of PapaBench project.
 * Copyright (C) 2010  Michal Malohlava <michal.malohlava_at_d3s.mff.cuni.cz>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package papabench.jopgc.commons.tasks;

import java.io.PrintStream;

import com.jopdesign.sys.*;
import joprt.*;

/**
 * This is a version witha JOP RT periodic thread. 
 * 
 * It carries information about periodic invocation, but it does not do it.
 *
 * @author Michal Malohlava
 *
 */
public class JopPeriodicTask extends RtThread {
	
	private Runnable taskHandler;
	private int priority;
	private int releaseMs;
	private int periodMs;
	private int processor;
	private String name;

	private int periodCycles;
	
	public JopPeriodicTask(Runnable taskHandler,
						   int priority, int releaseMs, int periodMs,
						   int processor, String name) {
		super(priority, periodMs*1000, releaseMs*1000);
		setProcessor(processor);
		this.taskHandler = taskHandler;
		this.priority = priority;
		this.releaseMs = releaseMs;
		this.periodMs = periodMs;
		this.processor = processor;
		this.name = name;

		this.periodCycles = periodMs*100000;
	}
	
	public void run() {
		
		if (Config.MEASURE) initInstrumentation();

		waitForNextPeriod();

		for (;;) {

			if (Config.MEASURE) startMeasurement();

			taskHandler.run();

			if (Config.MEASURE) endMeasurement();			

			if (papabench.jopgc.PapaBenchJopGcImpl.halt) {
				if (Config.MEASURE) printInstrumentation();
				for(;;) {
					/* stop doing anything */
					waitForNextPeriod();
				}
			}

			waitForNextPeriod();
		}
	}

	public Runnable getTaskHandler() {
		return taskHandler;
	}

	public int getPriority() {
		return priority;
	}

	public int getReleaseMs() {
		return releaseMs;
	}

	public int getPeriodMs() {
		return periodMs;
	}

	public int getProcessor() {
		return processor;
	}

	static class MeasurementStatistic {
		int overhead;
		int minElapsed, maxElapsed, totalRuns;		
		long totalElapsed;
		int minJitter, maxJitter;
		String name;
		Dumper dumper;

		public MeasurementStatistic(String str, int o) {
			overhead = o;
			minElapsed = Integer.MAX_VALUE;
			maxElapsed = Integer.MIN_VALUE;
			totalElapsed = 0;
			totalRuns = 0;
			maxJitter = 0;
			minJitter = 0;
			name = str;
			dumper = new Dumper();
		}
		public void recordRun(int elapsed, int jitter) {
			totalRuns++;
			totalElapsed += elapsed;
			if(minElapsed > elapsed) minElapsed = elapsed;
			if(maxElapsed < elapsed) maxElapsed = elapsed;
			if(minJitter > jitter) minJitter = jitter;
			if(maxJitter < jitter) maxJitter = jitter;
		}

		private class Dumper implements Runnable {
			PrintStream out;
			private long toMu(long t) {
				return (t+50)/100;
			}
			private int toMu(int t) {
				return (t+50)/100;
			}
			public void run() {
				synchronized(out) {
					out.print(name); out.print(" & ");
					out.print(toMu(overhead)); out.print(" & ");
					out.print(toMu(minElapsed)); out.print(" & ");
					out.print(toMu(totalElapsed/totalRuns)); out.print(" & ");
					out.print(toMu(maxElapsed)); out.print(" & ");
					out.print(toMu(minJitter)); out.print(" & ");
					out.print(toMu(maxJitter)); out.print(" & ");
					out.print(toMu(maxJitter - minJitter)); out.println(" \\\\");
				}
			}
		}

		public void dump(PrintStream out) {
			dumper.out = out;
			dumper.run();
		}

		public String toString() {
			return name
				+"\tovrhd:\t"+overhead
				+"\tmin:\t"+minElapsed+"\tmax:\t"+maxElapsed
				+"\ttotal:\t"+totalElapsed+"/"+totalRuns
				+"\tjit<:\t"+minJitter
				+"\tjit>:\t"+maxJitter;
		}
	}

	static class EmptyRunnable implements Runnable {
		public void run() {
		};
	}

	private MeasurementStatistic problemStats;
	private Runnable emptyRunnable;
	
	void initInstrumentation() {
		/* determine first release here to minimize disturbances */
		if (t0 == 0) { t0 = Native.rdMem(Const.IO_CNT); }
		tr = t0+releaseMs*100000-2000;
		/* initialize statistics */
		problemStats = new MeasurementStatistic(name, 0);
		/* create a dummy runnable */
		emptyRunnable = new EmptyRunnable();
		/* measure overhead for empty call */
		Native.lock();
		startMeasurement();
		emptyRunnable.run();
		endMeasurement();
		Native.unlock();
		/* initialize statistics again to get rid of overhead measurement artifacts */
		problemStats = new MeasurementStatistic(name, te-ts);
	}
	
	void printInstrumentation() {
		problemStats.dump(System.out);
	}

	// first release
	private static int t0;

	// start, end, jitter, release
	private int ts, te, tj, tr;

	public void startMeasurement() {
		int s = ts = Native.rdMem(Const.IO_CNT);
		tj = s-tr;
	}

	public void endMeasurement() {
		/* JOP Specific instrumentation */
		int e = te = Native.rdMem(Const.IO_CNT);
		int r = tr;
		problemStats.recordRun(e-r, tj);
		tr = r+periodCycles;
	}
}
