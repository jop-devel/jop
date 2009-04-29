/**
 * 
 */
package com.jopdesign.timing.jop;

public class JOPInstrParam {
	public JOPInstrParam(boolean isHit, int words) {
		this.hit = isHit;
		this.methodLoadWords = words;
	}
	boolean hit;
	int methodLoadWords;
}