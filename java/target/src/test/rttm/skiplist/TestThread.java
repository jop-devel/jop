package rttm.skiplist;

import java.util.Random;

public class TestThread implements Runnable {
	private volatile Object lock;
	AbstractSkipList skipList = null;
	public volatile boolean finished;

	public TestThread(AbstractSkipList skipList) {
		this.skipList = skipList;
	}

	public void run() {
		Random random = new Random();
		int my_shadow = 0;

		boolean r;
		int i;

		try {
			for (i = 0; ((Const.OP_COUNT == -1) || (i < Const.OP_COUNT)); i++) {
				int n = random.nextInt();
				int v = (n >> 8) & Const.KEY_SPACE_MASK;
				r = false;
				if ((n & 0xff) < Const.LOOKUP_FRAC) {
					r = skipList.Contains(v);
				} else if ((n & 0xff) < (Const.LOOKUP_FRAC + Const.REMOVE_FRAC)) {
					r = skipList.Remove(v);
					if (r)
						my_shadow -= v;
				} else {
					r = skipList.Insert(v);
					if (r)
						my_shadow += v;
				}
			}
		} catch (Exception e) {

		}

		synchronized (lock) {
			skipList.Shadow += my_shadow;
		}

		this.finished = true;
	}
}
