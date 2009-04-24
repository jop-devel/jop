package com.jopdesign.timing.jop;

import com.jopdesign.timing.WCETInstruction;
/** Chip multiprocessing timing.
 *  Currently delgates to {@code WCETInstruction}
 *
 */
public class JOPCmpTimingTable extends JOPTimingTable {

	protected JOPCmpTimingTable(MicropathTable mpt) {
		super(mpt);
	}
	
    @Override
	public long getCycles(int opcode, JOPInstructionInfo info) {
    	return WCETInstruction.getCycles(opcode, ! info.hit, info.methodLoadWords);
    }
    /* TODO: improve WCET by taking basic blocks into account */
    // @Override
	// public long getCycles(List<Instruction> opcodes) {

}
