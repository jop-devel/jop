package com.jopdesign.timing.jamuth;

import com.jopdesign.timing.InstructionInfo;

/** 
 * InstructionInfo for Jamuth (currently plain InstructionInfo)
 */
public class JamuthInstructionInfo extends InstructionInfo {
	private Integer jumpTargetAddress;
	public JamuthInstructionInfo(int opcode, Integer jumpTargetAddress) {
		super(opcode);
		this.jumpTargetAddress = jumpTargetAddress;
	}
	public boolean hasJumpTargetAddress()
	{
		return this.jumpTargetAddress != null;
	}
	public int getJumpTargetAddress()
	{
		return this.jumpTargetAddress;
	}
}
