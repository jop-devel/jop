package rttm.WaitFreeQueue;

public class SemHolder extends Semaphore {

	public SemHolder(int paramInt) {
		super(paramInt);
		System.out.println("Semholder constructor");
	}

	public void up(QueueThread queueThread) {
		super.release(queueThread);

	}

	public boolean tryDown(QueueThread queueThread) {
		try {
			return super.attempt(0, queueThread);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void down(QueueThread queueThread) {
		try {
			super.acquire(queueThread);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getCount() {
		return (int) super.permits();
	}
}
