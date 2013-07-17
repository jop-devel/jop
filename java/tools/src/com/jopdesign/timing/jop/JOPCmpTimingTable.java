package com.jopdesign.timing.jop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import com.jopdesign.timing.ConsoleTable;
import com.jopdesign.timing.ConsoleTable.Alignment;
import com.jopdesign.timing.ConsoleTable.TableRow;
import com.jopdesign.tools.JopInstr;
/**
 *  Chip multiprocessing timing.
 *  Currently delegates to {link com.jopdesign.timing.WCETInstruction}
 *  FIXME: Create a microcode analysis based class in com.jopdesign.timing.jop.MultiCoreTiming
 */
public class JOPCmpTimingTable extends JOPTimingTable {

	public static JOPCmpTimingTable getCmpTimingTable(
			File asmFile, int rws, int wws, int cpus, int timeslot) throws IOException {
		MicropathTable mpt = MicropathTable.getTimingTableFromAsmFile(asmFile);
		return new JOPCmpTimingTable(mpt, cpus, timeslot, rws, wws);
	}

	private WCETInstruction instTimingInfo;
	private int cpus;
	private int timeslot;
	
	protected JOPCmpTimingTable(MicropathTable mpt, int cpus, int timeslot, int rws, int wws) {
		super(mpt);
		instTimingInfo = new WCETInstruction(cpus, timeslot, rws, wws);
	}

	@Override
	public void configureWaitStates(int rws, int wws) {
		configure(cpus, timeslot, rws, wws);
	}

	/**
	 * @param cpus number of CPUs
	 * @param timeslot timeslot length
	 * @param rws read wait states  (r)
	 * @param wws write wait states (w)
	 */
	public void configure(int cpus, int timeslot, int rws, int wws) {
		super.configureWaitStates(rws, wws);
		this.cpus     = cpus;
		this.timeslot = timeslot;
		instTimingInfo.configureCMP(cpus, timeslot, rws, wws);		
	}
	
	@Override
	protected long getCycles(int opcode, boolean isHit, int methodLoadWords) {
		long cycles = instTimingInfo.getCycles(opcode, ! isHit, methodLoadWords);
		if(JopInstr.isInJava(opcode)) {
			cycles = instTimingInfo.getCycles(org.apache.bcel.Constants.INVOKESTATIC, ! isHit, methodLoadWords);
		}
		return cycles;
	}

	// should be inherited, but currently we delegate to the old impl,
	// so fewer bytecodes are supported
	@Override
	public boolean hasTimingInfo(int opcode) {
		if(! super.hasTimingInfo(opcode)) return false;
		return getCycles(opcode,false,32) != WCETInstruction.WCETNOTAVAILABLE;
	}


	/* TODO: improve WCET by taking basic blocks into account */
	// @Override
	// public long getCycles(List<Instruction> opcodes) {
	// }

	public static void main(String argv[]) {
		String head = "JOP CMP Timing Table on " + new Date();
		System.out.println(head);
		System.out.println(ConsoleTable.getSepLine('=',head.length()));
		System.out.println();


		System.out.println("  Loading " + MicrocodeAnalysis.DEFAULT_ASM_FILE);
		System.out.println("    Before generating the timing table do not forget to run e.g.");
		System.out.println("    > make gen_mem -e ASM_SRC=jvm JVM_TYPE=USB\n");

		JOPCmpTimingTable tt = null;
		try {
			tt = JOPCmpTimingTable.getCmpTimingTable(MicrocodeAnalysis.DEFAULT_ASM_FILE,3,5,3,15);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		// build table
		ConsoleTable table = dumpCmpTimingTable(tt);

		System.out.println(table.render());
	}

	// TODO: there is some duplication with JOPTimingTable (naturally),
	// but too lazy now to extract common stuff.
	private static ConsoleTable dumpCmpTimingTable(JOPCmpTimingTable tt) {
		//              r  w  cpus timeslot
		int conf1[] = { 1, 2, 3, 4 };
		int conf2[] = { 1, 2, 3, 5 };
		int conf3[] = { 1, 2, 3, 6 };
		int conf4[] = { 1, 2, 3, 7 };
		int[][] cmpTestConfig = { conf1, conf2, conf3, conf4 };
		ConsoleTable table = new ConsoleTable();
		table.addColumn("opcode", Alignment.ALIGN_RIGHT)
			.addColumn("name", Alignment.ALIGN_LEFT);
		for(int[] conf : cmpTestConfig) {
			table.addColumn(String.format("(%d,%d,%d,%d)",conf[0],conf[1],conf[2],conf[3]),Alignment.ALIGN_RIGHT);
		}
		for(int i = 0; i < 256; i++) {
			int opcode = i;
			if(JopInstr.isReserved(opcode)) continue;
			TableRow row = table.addRow();
			row.addCell(opcode)
			.addCell(JopInstr.OPCODE_NAMES[i]);
			if(! tt.hasTimingInfo(opcode)) {
				row.addCell("... not supported ...",cmpTestConfig.length,Alignment.ALIGN_LEFT);
			} else {
				for(int[] conf : cmpTestConfig) {
					tt.configure(conf[2], conf[3], conf[0], conf[1]);
					long timingHit = tt.getCycles(opcode, true, 0);
					long timingMiss1 = tt.getCycles(opcode, false, 1);
					long timingMiss2 = tt.getCycles(opcode, false, 2);
					long timingMiss32 = tt.getCycles(opcode, false, 32);
					if(timingHit == timingMiss32) {
						row.addCell(timingHit);
					} else {
						row.addCell(timingHit + " / " + timingMiss32);
					}
				}
			}
		}
		table.addLegendTop("  (x,y,c,t) ~ (read delay, write delay, cpus, timeslot)");
		table.addLegendTop("  c1 / c2 ... cycles cache hit / cycles cache miss 32 words");
		table.addLegendTop("  infeasible branches: "+Arrays.toString(MicrocodeAnalysis.INFEASIBLE_BRANCHES));
//		table.addLegendBottom(String.format("  hidden cycles on invoke (including JavaImplBCs) and return: %d / %d",
//				tt.minCyclesHiddenOnInvoke,tt.minCyclesHiddenOnReturn));
		return table;
	}

}
