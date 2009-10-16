package rttm.bytecode;

public class SynchronizedTest {
	static Object lock = new Object();
	
	public static void main(String[] args) {
		synchronized (lock) {
			new SynchronizedTest().synchedMethod();
		}
	}
	
	synchronized int synchedMethod() {
		return 0;
	}
}
