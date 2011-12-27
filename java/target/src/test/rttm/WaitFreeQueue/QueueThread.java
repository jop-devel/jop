package rttm.WaitFreeQueue;

import java.util.Vector;

public abstract class QueueThread implements Runnable {

	public static Vector waitingThreads = new Vector();

	public volatile boolean isWaiting = false;

	public QueueThread() {
	}

	public abstract void run();

//	public void wait() {
//		synchronized (waitingThreads) {
//			waitingThreads.addElement(this);
//			isWaiting = true;
//		}
////		System.out.println("waiting..");
//		while (isWaiting) {
////			System.out.println("isWaiting: " + isWaiting);
////			try {
////				Thread.sleep(1000);
////			} catch (InterruptedException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//		}
//	}
//
//	public void notify() {
////		System.out.println("notify..");
//		synchronized (waitingThreads) {
//			if (waitingThreads.size() > 0) {
//				QueueThread awakenedThread = (QueueThread) waitingThreads
//						.remove(0);
//				awakenedThread.isWaiting = false;
//			}
//		}
//	}

	public void tryWait() {
		synchronized (waitingThreads) {
			waitingThreads.addElement(this);
			isWaiting = true;
		}
		System.out.println("tryWait..");		
	}
}
