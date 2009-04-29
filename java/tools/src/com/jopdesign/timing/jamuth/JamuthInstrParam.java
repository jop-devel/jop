package com.jopdesign.timing.jamuth;
/** parameters jamuth needs for WCET calculation
 *  Currently: Jump Target 
 */
public class JamuthInstrParam {
	private int jumpTarget; // -1 == unknown
	public JamuthInstrParam() { this(-1); }
	public JamuthInstrParam(int jumpTarget) {
		this.jumpTarget = jumpTarget;
	}
	public boolean hasJumpTarget() { return jumpTarget >= 0; }
	public int getJumpTarget() { return jumpTarget; }
}
