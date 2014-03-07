package jembench.stream;

import jembench.Runner;
import jembench.StreamBenchmark;
import jembench.Util;


public class Increment extends StreamBenchmark {

	private final IncrementWork[]  work; //Should be more than the number of cores
	private final int load;
	private int iterations = 1;
	private int finished_cnt = 0;

	public Increment(int work_cnt, int load) {
		this.load = load;
		work = new IncrementWork[work_cnt];
		for (int i = 0; i < work.length; i++) {
			work[i] = new IncrementWork(i);
		}
	}

	public String toString() {
		return "Increment Load="+load;
	}

	public Runnable[] getWorkers() {
		return work;
	}
	
	protected int getDepth() {
		return work.length;
	}

	public void reset(int cnt) {
		iterations = cnt;
		finished_cnt = 0;
	}

	public boolean isFinished() {
		if(finished_cnt == work.length) {
			return true;
		}
		return false;
	}
	
	private synchronized void addFinished() {
		finished_cnt++;
	}

	private class IncrementWork implements Runnable {
		
		private int index;
		private int cnt;
		
		private IncrementWork(int index) {
			this.index = index;
		}

		public void run() {
			for (int i = 0; i < iterations; i++) {
				for (int j = index; j < work.length; j++) {
					synchronized (work[j]) {
						for (int k = 0; k < load; k++) {
							work[j].cnt++;
						}
					}
				}
				for (int j = 0; j < index; j++) {
					synchronized (work[j]) {
						for (int k = 0; k < load; k++) {
							work[j].cnt++;
						}
					}
				}
			}
			addFinished();
			if(isFinished()) {
				Runner.stop();
			}
		}
	}
}
