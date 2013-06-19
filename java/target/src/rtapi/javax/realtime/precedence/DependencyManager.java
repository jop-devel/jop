package javax.realtime.precedence;

import javax.realtime.PeriodicParameters;
import javax.realtime.Scheduler;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.jopdesign.sys.*;

public class DependencyManager {

	// Map for precedences
	Map<Runnable, List<PrecEntry>> precMap
		= new HashMap<Runnable, List<PrecEntry>>();

	// Class for precedence entries
	private static class PrecEntry {
		public final Runnable pred;
		public final Precedence prec;
		public PrecEntry(Runnable pred, Precedence prec) {
			this.pred = pred;
			this.prec = prec;
		}
	}

	// Map for periodic parameters
	Map<Runnable, PeriodicParameters> periodMap
		= new HashMap<Runnable, PeriodicParameters>();
	// Map for job counters
	Map<Runnable, JobCounter> jobMap
		= new HashMap<Runnable, JobCounter>();

	// We need a mutable object for the job counters, Integer is immutable
	private static class JobCounter { public long cnt; }

	// Register a precedences constraint between to Runnables
	public void register(Runnable pred,
						 PeriodicParameters predParams,
						 Runnable succ,
						 PeriodicParameters succParams,
						 Precedence prec) {

		//System.out.println("register");
		//System.out.println(Native.toInt(pred));
		//System.out.println(Native.toInt(succ));
		
		if (!precMap.containsKey(succ)) {
			precMap.put(succ, new ArrayList<PrecEntry>());
		}
		List<PrecEntry> precs = precMap.get(succ);
		precs.add(new PrecEntry(pred, prec));

		periodMap.put(pred, predParams);
		periodMap.put(succ, succParams);

		if (jobMap.get(pred) == null) {
			jobMap.put(pred, new JobCounter());
		}
		if (jobMap.get(succ) == null) {
			jobMap.put(succ, new JobCounter());
		}
	}

	// Check if dependencies are fulfilled
	public boolean isFree(Runnable s) {

		//System.out.println("isFree");
		//System.out.println(Native.toInt(s));

		List<PrecEntry> precs = precMap.get(s);
		if (precs != null) {
			long job = jobMap.get(s).cnt;
			long period = periodMap.get(s).getPeriod().getMilliseconds();

			for (int k = precs.size()-1; k >= 0; --k) {
				PrecEntry p = precs.get(k);
				long predJob = jobMap.get(p.pred).cnt;
				long predPeriod = periodMap.get(p.pred).getPeriod().getMilliseconds();
				long lcm = lcm(period, predPeriod);
				for (int i = 0; i < p.prec.pattern.length; i++) {
				 	DepWord dw = p.prec.pattern[i];
					// TODO: fix for corner cases (T==HP, etc)
					if (dw.predJob == predJob % (lcm/predPeriod) &&
						dw.succJob == job % (lcm/period) &&
						predJob / (lcm/predPeriod) == job / (lcm/period)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// Notify that a job was executed
	public void doneJob(Runnable s) {
		JobCounter j = jobMap.get(s);
		if (j != null) {
			j.cnt++;
		}
	}

	// Register that the scheduler should be notified as soon as the
	// Runnable becomes free
	public void setPending(Runnable s) {
	}
	// Clear pending flag
	public void clearPending(Runnable s) {
	}

	// The DependencyManager is a singleton for now
	// TODO: Maybe we should relax this for level 2 or RTSJ
	static final private DependencyManager INSTANCE = new DependencyManager();
	private DependencyManager() {
	}
	public static DependencyManager instance() {
		return INSTANCE;
	}

	// Helper method to compute least common multiple
	private long lcm(long a, long b) {
        long x, z, y = 1, i = 2;
        /* x = min(a,b)
         * z = max(a,b)
         */
        if (a < b) {
			x = a;
			z = b;
        } else {
			x = b;
			z = a;
        }

        /* find LCM = Least Common Multiple of x and z */
        do {
			if (x % i == 0) {
				if (z % i == 0) {
					y = y * i;
					x = x / i;
					z = z / i;
					i = 2;
				} else {
					i++;
				}
			} else {
				i++;
			}
        } while (i <= x);

        return y * z * x;
	}
}