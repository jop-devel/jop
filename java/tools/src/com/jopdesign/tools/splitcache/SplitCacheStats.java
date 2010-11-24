package com.jopdesign.tools.splitcache;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import com.jopdesign.tools.DataMemory;

/** Compute statistics for one access type.
 * They include: <ul>
 * <li/>read,write,invalidate count
 * <li/>spatial and temporal locality histogram
 * <li/>offset histogram
 * </ul>
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class SplitCacheStats extends DataMemory {

	public static final int BUFFER_SIZE = 32;
	public static final int SPATIAL_LOCALITY_MAX_INTERSPERSE = 3;
	public static final int HISTO_SIZE = 8;
	
	public static class Histogram {

		private long count[];
		private String name;
		
		public Histogram(String name, int n) {
			this.name = name;
			count = new long[n];
		}
		
		@Override
		public Histogram clone() {
			Histogram histo = new Histogram(name,count.length);
			System.arraycopy(count, 0, histo.count, 0, count.length);
			return histo;
		}
		
		public void reset() {
			Arrays.fill(count, 0);
		}
		
		public void add(Histogram other) {
			if(other.count.length != count.length) throw new AssertionError("Incompatible Histograms");
			for(int i = 0; i < count.length; i++) count[i] += other.count[i];
		}
		
		public void divBy(int k) {
			for(int i = 0; i < count.length; i++) count[i] /= k;
		}
		
		public void recordLd(int v) {
			if(v==0) {
				count[0]++;
				return;
			}
			for(int i = 1; i < count.length - 1; i++) {
				long threshold = 1<<(i-1); // i->threshold: 1 -> 1, 2 -> 2, 3 -> 4 etc.
				if(v <= threshold) {
					count[i]++;
					return;
				}
			}
			count[count.length-1]++;
			return;			
		}
		
		public void dump(PrintStream out) {
			int k=1;
			out.print(String.format("%-16s",name));
			for(int i = 0; i < count.length; i++) {
				out.print(String.format(" & %7d",count[i]));
				k<<=1;
			}
			out.println(" \\\\");
		}
	}

	public enum StatType { ReadCount, HitCount, HitQuadCount, WriteCount, DataInvalidates, HandleInvalidates };
	public enum HistoType { TempLocality, SpatialLocality, Offset };

	private static class AccessStats implements DataMemoryStats {
		
		private long stats[] = new long[StatType.values().length];
		private Histogram histos[] = new Histogram[HistoType.values().length];
		private String name;

		public AccessStats(String name, int histoSize) {
			this.name = name;
			for(int i = 0; i < HistoType.values().length; i++) histos[i] = new Histogram(HistoType.values()[i].name(), histoSize);
		}

		@Override
		public void reset() {
			Arrays.fill(stats, 0);
			for(int i = 0; i < HistoType.values().length; i++) histos[i].reset();
		}

		public long get(StatType ty) {
			return stats[ty.ordinal()];
		}
		
		public void incrStat(StatType ty) {
			stats[ty.ordinal()]++;
		}
		
		public Histogram getHisto(HistoType ty) {
			return histos[ty.ordinal()];
		}
		
		public void updateLocalityHistos(int tLocality, int sLocality) {
			histos[HistoType.TempLocality.ordinal()].recordLd(tLocality);
			histos[HistoType.SpatialLocality.ordinal()].recordLd(sLocality);
		}

		@Override
		public void dump(PrintStream out) {
			out.println(String.format("%-16s & %7s & %7s & %7s & %7s & %7s & %7s & %7s & %7s\\\\",
					"name", "rdcnt","wrcnt","hitcnt","hitrate","hitcntQW", "hitrateQW", "invdat","invhnd"));
			out.println(this.toString());
			dumpHistogramHeader(out, getHisto(HistoType.TempLocality).count.length);
			for(HistoType ht : HistoType.values()) {
				getHisto(ht).dump(out);
			}
		}
		
		public String toString() {
			double hitpt = 100 * (double)get(StatType.HitCount) / (double)get(StatType.ReadCount);
			double hit4pt = 100 * (double)get(StatType.HitQuadCount) / (double)get(StatType.ReadCount);
			return String.format("%-16s & %7d & %7d & %7d & %2.4f & %7d & %2.4f & %7d & %7d \\\\",
					name, get(StatType.ReadCount), get(StatType.WriteCount),
					get(StatType.HitCount), hitpt, get(StatType.HitQuadCount), hit4pt,
					get(StatType.DataInvalidates), get(StatType.HandleInvalidates));			
		}

		private void dumpHistogramHeader(PrintStream out,int n) {
			out.print(String.format("%-16s","Histogram"));
			int k=1;
			for(int i = 0; i < n; i++) {
				out.print(" & ");
				String key = (i==n-1) ? "rest" : ("in"+(k>>1));
				out.print(String.format("%7s",key));
				k<<=1;
			}
			out.println(" \\\\");		
		}

		@Override
		public AccessStats addAverage(Collection<? extends DataMemoryStats> statList) {
			int n = statList.size();
			if(n == 0) return this;
			for(DataMemoryStats elem : statList) {
				AccessStats stat = (AccessStats) elem;
				for(int i = 0; i < stats.length; i++) stats[i] += stat.stats[i];				
				for(int i = 0; i < histos.length; i++) histos[i].add(stat.histos[i]);				
			}
			for(int i = 0; i < stats.length; i++) stats[i] /= n;
			for(int i = 0; i < histos.length; i++) histos[i].divBy(n);				
			return this;
		}
		
		@Override
		public AccessStats clone() {
			AccessStats as = new AccessStats(name, histos.length);
			System.arraycopy(this.stats, 0, as.stats, 0, stats.length);
			for(int i = 0; i < histos.length; i++) {
				as.histos[i] = histos[i].clone();
			}
			return as;
		}

	}

	public static SplitCache splitAllStatistics(DataMemory defaultMemory) {
		SplitCache splitCache = new SplitCache("split-all-stats", defaultMemory);
		Access handled[] = new Access[1];
		
		for(Access ty : Access.values()) {
			handled[0] = ty;			
			splitCache.addCache(new SplitCacheStats(defaultMemory, ty.toString(), 
					            handled, ty.isMutableData()), handled);
		}
		return splitCache;
	}

	/** Statistics for a 3-split constant / static / heap data cache */
	public static SplitCache splitCSHStatistics(DataMemory backingMem) {
		SplitCache splitCache = new SplitCache("split-csh-stats", backingMem);
		return splitCache;
	}

	// configuration
	private String name;
	private Set<Access> handledTypes;
	private boolean processesType[];
	private DataMemory memory;
	private boolean hasMutableData;

	// statistics
	private AccessStats stats;
	private Stack<AccessStats> recordedStats = new Stack<AccessStats>();
	private LinkedList<Integer> lastAccessBuffer;
	private Set<Integer> allAccessed, allQuadsAccessed;


	
	/* Set hasMutableData to false to ignore the effects cache invalidation */
	public SplitCacheStats(DataMemory memory, String cacheName, Access[] types, boolean hasMutableData) {
		this.name = cacheName;
		this.memory = memory;
		processesType = new boolean[Access.getMaxOrdinal() + 1];
		this.handledTypes = new HashSet<Access>();
		for(Access ty : types) {
			handledTypes.add(ty);
			processesType[ty.ordinal()] = true;
		}
		this.hasMutableData = false; //hasMutableData;
		this.resetStats();
	}

	@Override
	public void invalidateData() {
		if(! hasMutableData) return;
		allAccessed.clear();
		allQuadsAccessed.clear();
		stats.incrStat(StatType.DataInvalidates);
	}

	// FIXME: should we distinguish between handle and data invalidation?
	@Override
	public void invalidateHandles() {		
		if(! hasMutableData) return;
		allAccessed.clear();
		allQuadsAccessed.clear();
		stats.incrStat(StatType.HandleInvalidates);
	}

	@Override
	public int read(int addr, Access type) {
		if(! processesType[type.ordinal()]) return memory.read(addr, type);

		if(allAccessed.contains(addr)) stats.incrStat(StatType.HitCount);
		if(allQuadsAccessed.contains(addr>>2)) stats.incrStat(StatType.HitQuadCount);
		allAccessed.add(addr);
		allQuadsAccessed.add(addr>>2);
		
		int tLocality = 0;
		for(int oAddr : lastAccessBuffer) {
			tLocality++;
			if(oAddr == addr) break;
		}
		
		int i = 0, sLocality = Integer.MAX_VALUE;
		for(int oAddr : lastAccessBuffer) {
			if(oAddr != addr) sLocality = Math.min(sLocality, Math.abs(addr-oAddr));
			if(i++ == SPATIAL_LOCALITY_MAX_INTERSPERSE) break;
		}
		
		stats.incrStat(StatType.ReadCount);
		stats.updateLocalityHistos(tLocality, sLocality);
		updateLastAccessBuffer(addr);
		return memory.read(addr, type);
	}

	private void updateLastAccessBuffer(int addr) {
		lastAccessBuffer.remove((Object)new Integer(addr));
		lastAccessBuffer.addFirst(addr);
		if(lastAccessBuffer.size() > BUFFER_SIZE) {
			lastAccessBuffer.removeLast();
		}
	}


	@Override
	public int readField(int handle, int offset, Access type) {
		// offset histogram
		stats.histos[HistoType.Offset.ordinal()].recordLd(offset);

		// split cache should resolve indirection if we do not handle HANDLE
		if(! this.processesType[Access.HANDLE.ordinal()]) {
			throw new AccessTypeUnsupported("readIndirect: not supported by statistics");
		} else {
			int addr = read(handle, Access.HANDLE);
			return read(addr+offset, type);
		}
	}

	@Override
	public void write(int addr, int value, Access type) {
		if(! processesType[type.ordinal()]) return;
		stats.incrStat(StatType.WriteCount);
	}

	@Override
	public void writeField(int handle, int offset, int value, Access type) {
		// split cache should resolve indirection if we do not handle HANDLE
		if(! this.processesType[Access.HANDLE.ordinal()]) {
			throw new AccessTypeUnsupported("writeIndirect: not supported by statistics");
		} else {
			int addr = read(handle, Access.HANDLE);
			write(addr+offset, value, type);
		}
	}

	@Override
	public void resetStats() {
		stats = new AccessStats(this.name, HISTO_SIZE);		
		allAccessed = new HashSet<Integer>();
		allQuadsAccessed = new HashSet<Integer>();
		lastAccessBuffer = new LinkedList<Integer>();		
	}
	@Override
	public void recordStats() {
		recordedStats.push(stats.clone());
	}
	public AccessStats getAverageStats() {
		return new AccessStats(name, HISTO_SIZE).addAverage(this.recordedStats);
	}

	@Override
	public String getName() {
		return name+"-statistitics" + (hasMutableData ? "" : " IMMUTABLE");
	}


	@Override
	public void dump(PrintStream out) {
		StringBuffer sb = new StringBuffer();
		sb.append("----- ");
		sb.append(getName());
		for(Access a : this.handledTypes) {
			sb.append(" " + a);
		}
		sb.append(" -----");
		SplitCacheSim.printHeader(System.out, sb.toString());
		getAverageStats().dump(System.out);
	}

}
