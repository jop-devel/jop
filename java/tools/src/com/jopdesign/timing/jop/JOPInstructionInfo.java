/**
 * 
 */
package com.jopdesign.timing.jop;

import com.jopdesign.timing.InstructionInfo;

public class JOPInstructionInfo extends InstructionInfo {
	public JOPInstructionInfo(int opcode) {
		super(opcode);
		this.hit = false;
		this.wordsLoaded = -1;
	}
	public JOPInstructionInfo(int opcode,
                              boolean isHit,
                              int wordsLoaded) {
		super(opcode);
		this.hit = isHit;
		this.wordsLoaded = wordsLoaded;
	}
	boolean hit;
	int wordsLoaded;
}