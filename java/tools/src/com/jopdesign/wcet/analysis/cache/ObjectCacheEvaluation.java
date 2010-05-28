/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.analysis.cache;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Purpose: helper classes for the evaluation of the object cache
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class ObjectCacheEvaluation {
	public enum OCacheMode { WORD_FILL, LINE_FILL, SINGLE_FIELD };
	public static class OCacheAnalysisResult {
		public OCacheMode mode;
		public int lineWords;
		public int ways;
		public double hitrate;
		public int configId;
		public double cyclesPerAccess;		
		/**
		 * @param mode2
		 * @param ways2
		 * @param lineSize
		 * @param ocConfig
		 * @param hitRate2
		 * @param bestCyclesPerAccessForConfig
		 */
		public OCacheAnalysisResult(OCacheMode mode, int ways, int lineSize,
				                    int configId, double hitRate,
				                    double cyclesPerAccess) {
			this.mode = mode;
			this.ways = ways;
			this.lineWords = lineSize;
			this.configId = configId;
			this.hitrate = hitRate;
			this.cyclesPerAccess = cyclesPerAccess;
		}
		private long cacheSize() {
			return lineWords * 4 * ways;
		}
		public static Comparator<OCacheAnalysisResult> wayLineComperator() {
			return new Comparator<OCacheAnalysisResult>() {				
				@Override
				public int compare(OCacheAnalysisResult arg0, OCacheAnalysisResult arg1) {
					int cmpMode = arg0.mode.compareTo(arg1.mode);
					int cmpWays = new Integer(arg0.ways).compareTo(arg1.ways);
					int cmpLineSz = new Integer(arg0.lineWords).compareTo(arg1.lineWords);
					int cmpConfig = new Integer(arg0.configId).compareTo(arg1.configId);
					if(cmpMode != 0) return cmpMode;
					if(cmpWays != 0) return cmpWays;
					if(cmpLineSz != 0) return cmpLineSz;
					return cmpConfig;
				}
			};
		}
		/**
		 * @param samples
		 */
		public static void dumpLatex(List<OCacheAnalysisResult> samples, PrintStream out) {
			Collections.sort(samples, OCacheAnalysisResult.wayLineComperator());
			Iterator<OCacheAnalysisResult> it = samples.iterator();
			OCacheAnalysisResult oldSample = null;
			OCacheAnalysisResult sample;
			if(it.hasNext()) sample = it.next();
			else             sample = null;
			while(sample != null) {
				if(oldSample == null || ! oldSample.mode.equals(sample.mode))
					out.println("\\midrule "+sample.mode);
				// size, line, assoc, hitrate
				out.print(String.format(" & %d B",sample.cacheSize()));
				out.print(String.format(" & %d B",sample.lineWords*4));
				out.print(String.format(" & %d way",sample.ways));
				out.print(String.format(" & %.2f \\%%",sample.hitrate*100));
				do {
					out.print(String.format(" & %.2f",sample.cyclesPerAccess));
					/* Stay in the same line while linesz and ways do not change */
					oldSample = sample; 
					if(it.hasNext()) sample = it.next();
					else             sample = null;
				} while(sample != null && sample.lineWords == oldSample.lineWords && sample.ways == oldSample.ways);
				out.println("\\\\");
			}
		}
	}
}
