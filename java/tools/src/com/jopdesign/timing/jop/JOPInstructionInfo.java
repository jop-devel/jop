/**
 * 
 */
package com.jopdesign.timing.jop;

public class JOPInstructionInfo {
	JOPInstructionInfo() {
		this.hit = true;
		this.methodLoadWords = 0;
	}
	public JOPInstructionInfo(boolean isHit, int words) {
		this.hit = isHit;
		this.methodLoadWords = words;
	}
	boolean hit;
	int methodLoadWords;
}