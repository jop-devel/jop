package prelude;

import javax.realtime.precedence.DependencyManager;
import javax.realtime.PeriodicParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;

public class PreludeMission extends Mission {

	PreludeTask [] taskSet;
	PreludePrecedence [] precSet;
	
	public PreludeMission(PreludeTask [] taskSet, 
						  PreludePrecedence [] precSet) {
		this.taskSet = taskSet;
		this.precSet = precSet;
	}

	private int getTaskIdx(String name) {
		for (int i = 0; i < taskSet.length; i++) {
			if (taskSet[i].name.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void initialize() {
		PreludeHandler [] handler = new PreludeHandler[taskSet.length];
		for (int i = 0; i < taskSet.length; i++) {
			PeriodicParameters p = new PeriodicParameters
				(new RelativeTime(PreludeSafelet.TICK_SCALE*taskSet[i].initialRelease, 0),
				 new RelativeTime(PreludeSafelet.TICK_SCALE*taskSet[i].period, 0),
				 new RelativeTime(PreludeSafelet.TICK_SCALE*taskSet[i].deadline, 0), null);
			handler[i] = new PreludeHandler(taskSet[i], p, i % 3);
			handler[i].register();
		}

		DependencyManager dm = DependencyManager.instance();
		for (int i = 0; i < precSet.length; i++) {

			PreludeHandler pred = handler[getTaskIdx(precSet[i].pred)];
			PreludeHandler succ = handler[getTaskIdx(precSet[i].succ)];

			if (precSet[i].prec == null) {
				dm.register(pred.getThread(), succ.getThread());
			} else {
				dm.register(pred.getThread(), pred.getPeriodicParams(),
							succ.getThread(), succ.getPeriodicParams(),
							precSet[i].prec);
			}
		}
	}

	@Override
	public long missionMemorySize() {
		return PreludeSafelet.MISSION_MEMORY_SIZE;
	}
}
