package javax.realtime.precedence;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;
import static javax.safetycritical.annotate.Level.INFRASTRUCTURE;

import javax.realtime.PeriodicParameters;
import javax.realtime.Scheduler;
import javax.realtime.Schedulable;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@SCJAllowed
public class DependencyManager {

	// Map for precedences
	Map<Schedulable, List<PrecEntry>> precMap
		= new HashMap<Schedulable, List<PrecEntry>>();

	// Class for precedence entries
	private static class PrecEntry {
		public final Schedulable pred;
		public final Precedence prec;
		public final long predRate;
		public final long succRate;
		public PrecEntry(Schedulable pred, Schedulable succ, Precedence prec,
						 Map<Schedulable, TaskProperties> taskMap) {
			this.pred = pred;
			this.prec = prec;

			TaskProperties predProps = taskMap.get(pred);
			long predPeriod = predProps.periodicParams == null ? 1
				: predProps.periodicParams.getPeriod().getMilliseconds();
			TaskProperties succProps = taskMap.get(succ);
			long succPeriod = succProps.periodicParams == null ? 1
				: succProps.periodicParams.getPeriod().getMilliseconds();

			long lcm = lcm(predPeriod, succPeriod);
			this.predRate = lcm/predPeriod;
			this.succRate = lcm/succPeriod;
		}
	}

	// Map for task properties
	Map<Schedulable, TaskProperties> taskMap
		= new HashMap<Schedulable, TaskProperties>();

	// Class for task properties and state
	private static class TaskProperties {
		public PeriodicParameters periodicParams;
		public List<Schedulable> succs;
		public volatile long job;
		public volatile boolean pending;
		public TaskProperties(PeriodicParameters p) {
			periodicParams = p;
			succs = new ArrayList<Schedulable>();
			job = 0;
			pending = false;
		}
	}

	// Register a simple precedence constraint between two Schedulables
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false, phase = INITIALIZATION)
	public void register(Schedulable pred,
						 Schedulable succ) {
		register(pred, null, succ, null, null);
	}

	// Register an extended precedence constraint
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false, phase = INITIALIZATION)
	public void register(Schedulable pred,
						 PeriodicParameters predParams,
						 Schedulable succ,
						 PeriodicParameters succParams,
						 Precedence prec) {

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
		List<Schedulable> succs = taskMap.get(pred).succs;
		succs.add(succ);

		// Build map of precedences
		if (!precMap.containsKey(succ)) {
			precMap.put(succ, new ArrayList<PrecEntry>());
		}
		List<PrecEntry> precs = precMap.get(succ);
		precs.add(new PrecEntry(pred, succ, prec, taskMap));
	}

	// Check if dependencies are fulfilled
	private boolean isFree(Schedulable s) {

		List<PrecEntry> precs = precMap.get(s);
		if (precs != null) {
			TaskProperties t = taskMap.get(s);
			long job = t.job;

			for (int k = precs.size()-1; k >= 0; --k) {
				PrecEntry prec = precs.get(k);
				TaskProperties predProps = taskMap.get(prec.pred);
				long predJob = predProps.job;

				if (prec.prec == null) {
					return predJob > job;
				} else {
					long succRate = prec.succRate;
					long predRate = prec.predRate;
					for (int i = 0; i < prec.prec.pattern.length; i++) {
						DepWord dw = prec.prec.pattern[i];
						// TODO: check corner cases (T==HP, etc)
						if (dw.predJob == predJob % predRate &&
							dw.succJob == job % succRate &&
							predJob / predRate == job / succRate) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	// Check if dependencies are fulfilled and handle pending flag
	// appropriately
	@SCJAllowed(INFRASTRUCTURE)
	@SCJRestricted(maySelfSuspend = false, mayAllocate = false)
	public boolean checkFree(Schedulable s) {

		TaskProperties t = taskMap.get(s);
		if (t != null) {
			// Set pending flag
			t.pending = true;
			// Check dependencies
			boolean free = isFree(s);		
			// Clear pending flag again if task is free
			if (free) {
				t.pending = false;
			}
			return free;
		} else {
			return true;
		}
	}

	// Notify that a job was executed
	@SCJAllowed(INFRASTRUCTURE)
	@SCJRestricted(maySelfSuspend = false, mayAllocate = false)
	public void doneJob(Schedulable s, Schedulable[] r) {
		int i = 0;

		// Update job counter
		TaskProperties t = taskMap.get(s);
		if (t != null) {
			t.job++;

			// Return freed successors
			List<Schedulable> succs = t.succs;
			if (succs != null) {
				for (int k = succs.size()-1; k >= 0; --k) {
					Schedulable succ = succs.get(k);
					TaskProperties succProps = taskMap.get(succ);
					if (succProps.pending && isFree(succ)) {
						r[i++] = succ;
						// Clear pending flag
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

	// Get current state of pending flag
	@SCJAllowed(INFRASTRUCTURE)
	@SCJRestricted(maySelfSuspend = false, mayAllocate = false)
	public boolean getPending(Schedulable s) {
		TaskProperties t = taskMap.get(s);
		return t.pending;
	}
	// Clear pending flag
	@SCJAllowed(INFRASTRUCTURE)
	@SCJRestricted(maySelfSuspend = false, mayAllocate = false)
	public void clearPending(Schedulable s) {
		TaskProperties t = taskMap.get(s);
		t.pending = false;
	}

	// The DependencyManager is a singleton for now
	// TODO: Maybe we should relax this for level 2 or RTSJ
	static final private DependencyManager INSTANCE = new DependencyManager();
	private DependencyManager() {
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false, mayAllocate = false)
	public static DependencyManager instance() {
		return INSTANCE;
	}

	@SCJAllowed(INFRASTRUCTURE)
	@SCJRestricted(maySelfSuspend = false, mayAllocate = false)
	public int getMaxFreed() {
		return taskMap.size();
	}

	// Helper method to compute least common multiple
	private static long lcm(long a, long b) {
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