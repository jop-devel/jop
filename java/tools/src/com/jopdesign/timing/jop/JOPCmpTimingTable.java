package com.jopdesign.timing.jop;

import java.io.File;
import java.io.IOException;

import com.jopdesign.timing.WCETInstruction;
/** Chip multiprocessing timing.
 *  Currently delgates to {@code WCETInstruction}
 *
 */
public class JOPCmpTimingTable extends JOPTimingTable {

	public static JOPCmpTimingTable getTimingTable( 
			File asmFile, int rws, int wws, int cpus, int timeslot) throws IOException {
		MicropathTable mpt = MicropathTable.getTimingTable(asmFile);
		JOPCmpTimingTable tt = new JOPCmpTimingTable(mpt);
		tt.configureWaitStates(rws, wws);
		tt.configureMultiprocessor(cpus, timeslot);
		return tt;
	}
	public void configureMultiprocessor(int cpus, int timeslot) {
		WCETInstruction.initCMP(cpus, timeslot);
	}
	protected JOPCmpTimingTable(MicropathTable mpt) {
		super(mpt);
	}
	
    @Override
	public long getCycles(int opcode, JOPInstrParam info) {
    	return WCETInstruction.getCycles(opcode, ! info.hit, info.methodLoadWords);
    }
    /* TODO: improve WCET by taking basic blocks into account */
    // @Override
	// public long getCycles(List<Instruction> opcodes) {

}
