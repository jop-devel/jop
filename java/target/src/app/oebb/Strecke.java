/*
 * Created on 10.08.2005
 *
 */
package oebb;

import joprt.SwEvent;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Strecke extends SwEvent {
	
	static Strecke find;

	/**
	 * @param priority
	 * @param minTime
	 */
	public Strecke(int priority, int minTime) {
		super(priority, minTime);
		find = this;
	}

	public void handle() {
		System.out.println("Strecke fired!");
	}
}
