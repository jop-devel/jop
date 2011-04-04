package javax.safetycritical.test;

import com.jopdesign.sys.RtThreadImpl;

public class Minimal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SafeletImpl si = new SafeletImpl();
		System.out.println("Hello SCJ World");
		RtThreadImpl.startMission();
	}

}
