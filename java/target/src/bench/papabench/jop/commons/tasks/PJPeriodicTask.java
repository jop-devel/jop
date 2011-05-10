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
package papabench.jop.commons.tasks;

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
public class PJPeriodicTask extends RtThread {
	
	public static final boolean INSTRUMENTATION = true;
	public static final int INSTRUMENTATION_RUNS = 100;

	private Runnable taskHandler;
	private int priority;
	private int releaseMs;
	private int periodMs;
	private Scope scope;
	private String name;

	public PJPeriodicTask(Runnable taskHandler, int priority, int releaseMs, int periodMs, String name) {
		super(priority, periodMs*1000, releaseMs*1000);
		this.taskHandler = taskHandler;
		this.priority = priority;
		this.releaseMs = releaseMs;
		this.periodMs = periodMs;
		this.name = name;
	}
	
	public Scope getScope() {
		return scope;
	}

	public void setScope(int words) {
		scope = new Scope(new int[words]);
	}

	public void setScope(Scope s) {
		scope = s;
	}

	public void run() {
		
		if (INSTRUMENTATION) initInstrumentation();
		int cnt = 0;

		for (;;) {

			if (INSTRUMENTATION) startMeasurement();

			if (scope != null) {
				scope.enter(taskHandler);
			} else {
				taskHandler.run();
			}

			if (INSTRUMENTATION) endMeasurement();			
			if (INSTRUMENTATION
				&& cnt++ == INSTRUMENTATION_RUNS) {
				printInstrumentation();
				cnt = 0;
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

	private static class MeasurementStatistic {
		int minElapsed, maxElapsed, totalRuns;		
		long totalElapsed;
		int minICacheCost, maxICacheCost;
		String name;
		public MeasurementStatistic(String str) {
			minElapsed = Integer.MAX_VALUE;
			maxElapsed = 0;
			totalElapsed = 0;
			totalRuns = 0;
			minICacheCost = Integer.MAX_VALUE;
			maxICacheCost = 0;
			name = str;
		}
		public void recordRun(int elapsed) {
			totalRuns++;
			totalElapsed += elapsed;
			if(minElapsed > elapsed) minElapsed = elapsed;
			if(maxElapsed < elapsed) maxElapsed = elapsed;
		}
		public void recordICacheCost(int cost) {
			if(minICacheCost > cost) minICacheCost = cost;
			if(maxICacheCost < cost) maxICacheCost = cost;
		}
		public void dump(PrintStream out) {
			out.print(name);
			out.print(":\t");
			out.print("min:\t"); out.print(minElapsed);
			out.print("\tmax:\t"); out.print(maxElapsed);
			out.print("\ttotal:\t"); out.print(totalElapsed);
			out.print("/\t"); out.print(totalRuns);
			if(maxICacheCost > 0) {
				out.print("\ti$-min:\t"); out.print(minICacheCost);
				out.print("\ti$-max:\t"); out.print(maxICacheCost);
			}
			out.println("");
		}
		public String toString() {
			return "min:\t"+minElapsed+"\tmax:\t"+maxElapsed
				+"\ttotal:\t"+totalElapsed+"/"+totalRuns;
		}
	}

	private MeasurementStatistic problemStats;
	
	private void initInstrumentation() {
		/* initialize statistics */
		problemStats = new MeasurementStatistic(name);
		/* JOP Specific instrumentation */
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
	}
	
	private void printInstrumentation() {
		problemStats.dump(System.out);
	}

	private int ts, te, to;

	public void startMeasurement() {
		Native.wrMem(0, Const.IO_INT_ENA);
		ts = Native.rdMem(Const.IO_CNT);
	}

	public void endMeasurement() {
		/* JOP Specific instrumentation */
		te = Native.rdMem(Const.IO_CNT);			
		problemStats.recordRun(te-ts-to);
		Native.wrMem(-1, Const.IO_INT_ENA);
	}
}
