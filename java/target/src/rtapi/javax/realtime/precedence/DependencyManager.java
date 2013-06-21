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

	// Map for task properties
	Map<Runnable, TaskProperties> taskMap
		= new HashMap<Runnable, TaskProperties>();

	// Class for task properties and state
	private static class TaskProperties {
		public PeriodicParameters periodicParams;
		public List<Runnable> succs;
		public volatile long job;
		public volatile boolean pending;
		public TaskProperties(PeriodicParameters p) {
			periodicParams = p;
			succs = new ArrayList<Runnable>();
			job = 0;
			pending = false;
		}
	}

	// Register a simple precedence constraint between two Runnables
	public void register(Runnable pred,
						 Runnable succ) {
		register(pred, null, succ, null, null);
	}

	// Register an extended precedence constraint
	public void register(Runnable pred,
						 PeriodicParameters predParams,
						 Runnable succ,
						 PeriodicParameters succParams,
						 Precedence prec) {

		//System.out.println("register");
		//System.out.println(Native.toInt(pred));
		//System.out.println(Native.toInt(succ));
		
		// Build map of precedences
		if (!precMap.containsKey(succ)) {
			precMap.put(succ, new ArrayList<PrecEntry>());
		}
		List<PrecEntry> precs = precMap.get(succ);
		precs.add(new PrecEntry(pred, prec));

		// Build map with task properties
		TaskProperties t;
		t = taskMap.get(pred);
		if (t == null) {
			taskMap.put(pred, new TaskProperties(predParams));
		} else if (t.periodicParams == null) {
			t.periodicParams = predParams;
		}
		t = taskMap.get(succ);
		if (t == null) {
			taskMap.put(succ, new TaskProperties(succParams));
		} else if (t.periodicParams == null) {
			t.periodicParams = succParams;			
		}
		// Add successors
		List<Runnable> succs = taskMap.get(pred).succs;
		succs.add(succ);
	}

	// Check if dependencies are fulfilled
	public boolean isFree(Runnable s) {

		//System.out.println("isFree");
		//System.out.println(Native.toInt(s));

		List<PrecEntry> precs = precMap.get(s);
		if (precs != null) {
			TaskProperties t = taskMap.get(s);
			long job = t.job;
			long period = t.periodicParams == null ? 1
				: t.periodicParams.getPeriod().getMilliseconds();

			for (int k = precs.size()-1; k >= 0; --k) {
				PrecEntry prec = precs.get(k);
				TaskProperties predProps = taskMap.get(prec.pred);
				long predJob = predProps.job;

				if (prec.prec == null) {
					return predJob > job;
				} else {
					long predPeriod = predProps.periodicParams == null ? 1
						: predProps.periodicParams.getPeriod().getMilliseconds();
					long lcm = lcm(period, predPeriod);
					for (int i = 0; i < prec.prec.pattern.length; i++) {
						DepWord dw = prec.prec.pattern[i];
						// TODO: fix for corner cases (T==HP, etc)
						if (dw.predJob == predJob % (lcm/predPeriod) &&
							dw.succJob == job % (lcm/period) &&
							predJob / (lcm/predPeriod) == job / (lcm/period)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	// Notify that a job was executed
	public void doneJob(Runnable s, Runnable[] r) {
		int i = 0;

		// Update job counter
		TaskProperties t = taskMap.get(s);
		if (t != null) {
			t.job++;

			// Return freed successors
			List<Runnable> succs = t.succs;
			if (succs != null) {
				for (int k = succs.size()-1; k >= 0; --k) {
					Runnable succ = succs.get(i);
					TaskProperties succProps = taskMap.get(succ);
					if (succProps.pending && isFree(succ)) {
						r[i++] = succ;
						// Implicitly clear pending flag
						succProps.pending = false;
					}
				}
			}
		}

		// return array is null-terminated
		if (i < r.length) {
			r[i] = null;
		}
	}

	// Register that the scheduler should be notified as soon as the
	// Runnable becomes free
	public void setPending(Runnable s) {
		TaskProperties t = taskMap.get(s);
		t.pending = true;
	}
	// Clear pending flag
	public void clearPending(Runnable s) {
		TaskProperties t = taskMap.get(s);
		t.pending = false;
	}

	// The DependencyManager is a singleton for now
	// TODO: Maybe we should relax this for level 2 or RTSJ
	static final private DependencyManager INSTANCE = new DependencyManager();
	private DependencyManager() {
	}
	public static DependencyManager instance() {
		return INSTANCE;
	}

	public int getMaxFreed() {
		return taskMap.size();
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