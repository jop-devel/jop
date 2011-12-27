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

import com.jopdesign.wcet.jop.ObjectCache;
import com.jopdesign.wcet.jop.ObjectCache.ObjectCacheCost;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Purpose: helper classes for the evaluation of the object cache
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class ObjectCacheEvaluationResult {
	/* small helper for partitioning result data */
	private interface Selector<S,T> {
		T getKey(S obj);
	}
	// The JDK 1.5 Java compiler is not as smart as eclipse :(
	// Also, this utility function would be way cooler if we had a reasonable type inference
	public static<S,T,M extends Map<T,List<S>>> Map<T,List<S>> partitionBy(
			Collection<S> col, 
			Selector<S,T> select,
			M resultMap) {
		for(S r : col) {
			T sel = select.getKey(r);
			List<S> list = resultMap.get(sel);
			if(list == null) list  = new ArrayList<S>();
			list.add(r);
			resultMap.put(sel, list);
		}
		return resultMap;		
	}

	public enum OCacheMode { BLOCK_FILL, SINGLE_FIELD };
	public static class OCacheAnalysisResult {
		public int lineWords;
		public int ways;
		public double hitrate;
		public int configId;
		public double cyclesPerAccess;
		private ObjectCache.ObjectCacheCost ocCost;
		private int blockSize;		
		/**
		 * @param ways
		 * @param lineSize
		 * @param ocCost 
		 * @param blockSize
         * @param configId
		 * @param hitRate
		 * @param cyclesPerAccess
		 */
		public OCacheAnalysisResult(int ways,								
									int lineSize,
									int blockSize,
				                    int configId, 
				                    double hitRate,
				                    double cyclesPerAccess, 
				                    ObjectCache.ObjectCacheCost ocCost) {
			this.ways = ways;
			this.lineWords = lineSize;
			this.blockSize = blockSize;
			this.configId = configId;
			this.hitrate = hitRate;
			this.cyclesPerAccess = cyclesPerAccess;
			this.ocCost = ocCost;
		}
		private long cacheSize() {
			return lineWords * 4 * ways;
		}
		public static Comparator<OCacheAnalysisResult> wayLineComperator() {
			return new Comparator<OCacheAnalysisResult>() {				
				public int compare(OCacheAnalysisResult arg0, OCacheAnalysisResult arg1) {
					int cmpWays = new Integer(arg0.ways).compareTo(arg1.ways);
					int cmpLineSz = new Integer(arg0.lineWords).compareTo(arg1.lineWords);
					int cmpWordSz = new Integer(arg0.blockSize).compareTo(arg1.blockSize);
					int cmpConfig = new Integer(arg0.configId).compareTo(arg1.configId);
					if(cmpWays != 0) return cmpWays;
					if(cmpLineSz != 0) return cmpLineSz;
					if(cmpWordSz != 0) return cmpWordSz;
					return cmpConfig;
				}
			};
		}
		
		public static Map<Integer,List<OCacheAnalysisResult>> partitionByLineSize(List<OCacheAnalysisResult> results) {
			return partitionBy(results, new Selector<OCacheAnalysisResult,Integer>() {
				public Integer getKey(OCacheAnalysisResult r) { return r.lineWords; }
			}, new TreeMap<Integer,List<OCacheAnalysisResult>>());
		}

		public static Map<Integer,List<OCacheAnalysisResult>> partitionByBlockSize(List<OCacheAnalysisResult> results) {
			return partitionBy(results, new Selector<OCacheAnalysisResult,Integer>() {
				public Integer getKey(OCacheAnalysisResult r) { return r.blockSize; }
			}, new TreeMap<Integer,List<OCacheAnalysisResult>>());
		}
		
		private static Map<Integer, List<OCacheAnalysisResult>> partitionByConfig(List<OCacheAnalysisResult> samples) {
			return partitionBy(samples, new Selector<OCacheAnalysisResult,Integer>() {
				public Integer getKey(OCacheAnalysisResult r) { return r.configId; }
			}, new TreeMap<Integer,List<OCacheAnalysisResult>>());
		}

		public static void dumpBarPlot(List<OCacheAnalysisResult> samples, PrintStream out) {
			for(Entry<Integer, List<OCacheAnalysisResult>> entryConfig : partitionByConfig(samples).entrySet()) {
				int config = entryConfig.getKey();				
				/* Bar Plot for Blocksize=1 : Group By Line Size, Associtativity on X, CMC on Y */
				out.println("# PLOT DATA for config= " + config +" with block size 1 ");
				for(Entry<Integer, List<OCacheAnalysisResult>> entry : partitionByLineSize(entryConfig.getValue()).entrySet()) {
					int lineSize = entry.getKey();
					out.printf("L=%d",lineSize);
					List<OCacheAnalysisResult> results = entry.getValue();
					Collections.sort(results, OCacheAnalysisResult.wayLineComperator());
					Vector<Integer> sampleWays = new Vector<Integer>();
					for(OCacheAnalysisResult r : results) {
						if(r.blockSize > 1) continue;
						out.printf(",%.2f",r.cyclesPerAccess);
						sampleWays.add(r.ways);
					}
					out.println(" # Ways: "+sampleWays);
				}
				out.println("# PLOT DATA for config= " + config +" with different block sizes");
				List<OCacheAnalysisResult> results = entryConfig.getValue();
				Collections.sort(results, OCacheAnalysisResult.wayLineComperator());
				int oldWays = -1;
				int oldLineSize = -1;
				for(OCacheAnalysisResult r : results) {
					if(r.ways != oldWays || r.lineWords != oldLineSize) {
						oldWays = r.ways;
						oldLineSize = r.lineWords;
						out.printf("\nN=%d L=%d",r.ways, r.lineWords);
					}
					out.printf(",%.2f", r.cyclesPerAccess);
				}
				out.println("");
			}
		}

		public static void dumpPlot(List<OCacheAnalysisResult> samples, PrintStream out) {
			for(Entry<Integer, List<OCacheAnalysisResult>> entryConfig : partitionByConfig(samples).entrySet()) {
				int config = entryConfig.getKey();				
				for(Entry<Integer, List<OCacheAnalysisResult>> entry : partitionByBlockSize(entryConfig.getValue()).entrySet()) {
					int blockSize = entry.getKey();
					out.println("# PLOT DATA for config= "+config+" and block size "+blockSize);
					List<OCacheAnalysisResult> results = entry.getValue();
					Collections.sort(results, OCacheAnalysisResult.wayLineComperator());
					Iterator<OCacheAnalysisResult> it = results.iterator();
					OCacheAnalysisResult oldSample = null;
					OCacheAnalysisResult sample;
					while(it.hasNext()) {
						sample = it.next();
						if(oldSample == null || oldSample.ways != sample.ways) out.println("\n");
						out.println(String.format("%-8d\t%.2f",
								sample.cacheSize(),sample.cyclesPerAccess));
						oldSample = sample; 
					}
				}
			}
		}
		
		public static void dumpLatex(List<OCacheAnalysisResult> samples, PrintStream out) {
			Collections.sort(samples, OCacheAnalysisResult.wayLineComperator());
			Iterator<OCacheAnalysisResult> it = samples.iterator();
			OCacheAnalysisResult oldSample = null;
			OCacheAnalysisResult sample;
			if(it.hasNext()) sample = it.next();
			else             sample = null;
			while(sample != null) {
				if(oldSample == null || oldSample.blockSize != sample.blockSize) {
					out.println("\\midrule "+sample.blockSize * 4);
				}
				// size, line, assoc, hitrate
				String comment = sample.ocCost.toString();
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
				out.println("\\\\ %"+comment);
			}
		}
	}
}
