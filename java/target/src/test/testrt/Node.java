/*
 * Created on 13.12.2005
 *
 */
package testrt;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import joprt.*;

public class Node {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtThread(10, 100000) {
			public void run() {

			    for (;;) {
			        Node n = new Node();
			        work(n);
			        waitForNextPeriod();
			    }
			}
		};
	}
	
	static void work(Node n) {
		
	}

}
