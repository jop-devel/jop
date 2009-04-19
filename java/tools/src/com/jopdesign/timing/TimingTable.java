/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.timing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import com.jopdesign.timing.ConsoleTable.Alignment;
import com.jopdesign.timing.ConsoleTable.TableRow;
import com.jopdesign.timing.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.tools.JopInstr;

/** Microcode Timing Table 
 * Before generating the timing table do not forget to run e.g.
 * {@code make gen_mem -e ASM_SRC=jvm JVM_TYPE=USB @} 
 */
public class TimingTable {
	final static String OPCODE_NAMES[] = new String[256];
	static {
		for(int i = 0; i < 256; i++) {
			OPCODE_NAMES[i] = JopInstr.name(i);
		}
	}
	static boolean isReserved(int opcode) {
		return OPCODE_NAMES[opcode].matches("res[0-9a-zA-F]{2,2}");
	}
	public static TimingTable getTimingTable(File asm) throws IOException {
		MicrocodeAnalysis ana = new MicrocodeAnalysis(asm.getPath());
		TimingTable tt = new TimingTable();
		for(int i = 0; i < 256; i++) {
			if(isReserved(i)) {
				continue;
			}
			try {
				Integer addr = ana.getStartAddress(i);
				if(addr != null) {
					tt.timingTable.put(i,ana.getTiming(OPCODE_NAMES[i],addr));
				} else {
					tt.notImplemented.add(i);
				}
			} catch(MicrocodeVerificationException e) {
				tt.analysisErrors.put(i,e);
			} 
		}
		return tt;
	}
	public static TimingTable getTimingTable(File asm, int r, int w) throws IOException, InterruptedException {
		TimingTable tt = TimingTable.getTimingTable(asm);
		tt.configureDelays(r,w);
		return tt;
	}

	private int readCycles;
	private int writeCycles;
	
	private HashSet<Integer> notImplemented;
	public boolean isImplemented(int opcode) {
		return ! (isReserved(opcode) || notImplemented.contains(opcode));
	}
	
	private TreeMap<Integer, MicrocodeVerificationException> analysisErrors;	
	public MicrocodeVerificationException getAnalysisError(int opcode) { 
		return analysisErrors.get(opcode); 
	}
	
	private TreeMap<Integer, Vector<MicropathTiming>> timingTable;
	
	private TimingTable() {
		timingTable = new TreeMap<Integer,Vector<MicropathTiming>>();
		notImplemented = new HashSet<Integer>();
		analysisErrors = new TreeMap<Integer,MicrocodeVerificationException>();
		this.readCycles = -1; // not configured
	}
	private void configureDelays(int r, int w) {
		this.readCycles = r;
		this.writeCycles = w;
	}
	private boolean isConfigured() {
		return readCycles >= 1;
	}
	
	public Vector<MicropathTiming> getTiming(int opcode) {
		if(analysisErrors.containsKey(opcode)) {
			throw new AssertionError("Failed to analyse microcode timing: "+opcode);			
		}
		if(notImplemented.contains(opcode)) {
			return this.timingTable.get(254);
		} else {
			return this.timingTable.get(opcode);			
		}
	}
	
	public long getCycles(int opcode) {
		Vector<MicropathTiming> timing = this.getTiming(opcode);
		if(hasBytecodeLoad(timing)) {
			throw new AssertionError("getCycles(opcode) : bytecode load requires to specify size of Java method");
		}
		return getCycles(timing,0);
	}
	
	public long getCycles(int opcode, boolean hit, int bytecodeWords)  {
		Vector<MicropathTiming> timing = this.getTiming(opcode);
		long bcLoadDelay = calculateBcLoadDelay(hit, bytecodeWords);
		return getCycles(timing, bcLoadDelay);
	}
		
	private long getCycles(Vector<MicropathTiming> timing, long bytecodeDelay) {
		if(! isConfigured()) throw new AssertionError("getCycles(): read/write delay not set");
		long maxCycles = 0;
		for(MicropathTiming mtiming : timing) {
			maxCycles = Math.max(maxCycles, mtiming.getCycles(readCycles, writeCycles, bytecodeDelay));
		}
		return maxCycles;
	}
	private static boolean hasBytecodeLoad(Vector<MicropathTiming> timings) {
		for(MicropathTiming ptime : timings) {
			if(ptime.hasBytecodeLoad()) return true;
		}
		return false;
	}

	/**
	 * Method load time on invoke or return if there is a cache miss (see pMiss).
	 * 
	 * @see ms thesis p 232
	 */
	public long calculateBcLoadDelay(boolean hit, int bytecodeWords) {
		int b = -1;
		int c = readCycles-1;
		if (hit) {
			b = 4;
		} else {
			b = 6 + (bytecodeWords+1) * (2+c);
		}
		return b;
	}
	/**
	 * Print the microcode timing table from asm/generated/jvmgen.asm
	 * @param argv
	 */
	public static void main(String argv[]) {
		File asmFile = new File("asm/generated/jvmgen.asm");

		System.out.println("Loading "+asmFile);
		System.out.println("  Before generating the timing table do not forget to run e.g.");
		System.out.println("  > make gen_mem -e ASM_SRC=jvm JVM_TYPE=USB"); 

		TimingTable tt = null;
		try {
			tt = TimingTable.getTimingTable(asmFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		// demo config
		tt.configureDelays(1, 2);
		// build table
		ConsoleTable table = new ConsoleTable();
		table.addColumn("opcode", Alignment.ALIGN_RIGHT)
		     .addColumn("name", Alignment.ALIGN_LEFT)
		     .addColumn("timing path", Alignment.ALIGN_LEFT)
		     .addColumn("(1,2,H)",Alignment.ALIGN_RIGHT)
	         .addColumn("(1,2,32)",Alignment.ALIGN_RIGHT)
			 .addColumn("(3,5,H)",Alignment.ALIGN_RIGHT)
             .addColumn("(3,5,32)",Alignment.ALIGN_RIGHT);
		for(int i = 0; i < 256; i++) {
			int opcode = i;
			if(isReserved(opcode)) continue;
			TableRow row = table.addRow();
			row.addCell(opcode)
			   .addCell(OPCODE_NAMES[i]);
			if(tt.getAnalysisError(i) != null) {
				row.addCell("... FAILED: "+tt.getAnalysisError(i).getMessage()+" ...",5,Alignment.ALIGN_LEFT);
			} else if (! tt.isImplemented(i)) {
				row.addCell("... no microcode implementation ...",5,Alignment.ALIGN_LEFT);
			} else {
				String timingPath = MicropathTiming.toString(tt.getTiming(opcode));
				tt.configureDelays(1, 2);
				long exampleTiming1 = tt.getCycles(opcode, true, 0); 
				long exampleTiming2 = tt.getCycles(opcode, false, 32); 
				tt.configureDelays(3, 5);
				long exampleTiming3 = tt.getCycles(opcode, true, 0); 
				long exampleTiming4 = tt.getCycles(opcode, false, 32); 
				row.addCell(timingPath)
				   .addCell(exampleTiming1)
				   .addCell(exampleTiming2)
				   .addCell(exampleTiming3)
				   .addCell(exampleTiming4);
			}
		}
		table.addLegendTop("  (x,y,z) ~ (read delay, write delay, bytecode access)");
		table.addLegendTop("  z = H ... cache hit, n ... load n words into cache");
		table.addLegendTop("  infeasible branches: "+Arrays.toString(MicrocodeAnalysis.INFEASIBLE_BRANCHES));
		table.addLegendBottom("  [expr] denotes max(0,expr)");
		table.addLegendBottom("  r = read delay, w = write delay, b = bytecode load delay");
		System.out.println(table.render());

		/* for now, check if we agree with WCETInstruction */
		checkWithWCETInstruction(tt);
	}
	
	private static void checkWithWCETInstruction(TimingTable tt) {
		boolean[] testHit  = { true, true, false, false, false };
		int[]     testLoad = {    1,  128,     1,    11,    57 };
		Map<Integer,String> failures = new TreeMap<Integer,String>();
		tt.configureDelays(WCETInstruction.r, WCETInstruction.w);
		outer:
		for(int i = 0; i < 256; i++) {
			long wiTime0 = WCETInstruction.getCycles(i, false, 0);
			if(isReserved(i)) {
				if(wiTime0 >= 0) {
					failures.put(i,"WCETInstruction has timing: "+wiTime0+", but JopInstr says this is a RESERVED INSTRUCTION");
				} 
				continue;				
			}
			if(! tt.isImplemented(i)) {
				if(wiTime0 >= 0) {
					failures.put(i,"WCETInstruction has timing: "+wiTime0+", but there is NO MICROCODE IMPLEMENTATION");
				} 
				continue;
			}
			if(tt.getAnalysisError(i) != null) {
				if(wiTime0 >= 0) {
					failures.put(i, "WCETInstruction has timing: "+wiTime0+", but we FAILED TO ANALYSE the microcode");
				} 
				continue;				
			}
			String tString = MicropathTiming.toString(tt.getTiming(i));
			if(wiTime0 < 0) {
				failures.put(i,"WCETInstruction HAS NO TIMING information, but we have: "+tString);
				continue;
			}
			for(int j = 0; j < testHit.length; j++) {
				long wiT = WCETInstruction.getCycles(i, ! testHit[j], testLoad[j]);
				long wiMA = tt.getCycles(i, testHit[j], testLoad[j]);
				if(wiT != wiMA) {
					failures.put(i,"WCETInstruction has DIFFERENT TIMING INFO: "+
							     "microcodeanalysis := "+wiMA+ " /= "+ wiT + " =: wcetinstruction"); 
					continue outer;
				}
			}
		}
		for(Entry<Integer, String> fail : failures.entrySet()) {
			int opc = fail.getKey();
			System.err.println("["+opc+"] "+OPCODE_NAMES[opc]+" : "+fail.getValue());
		}
	}

}
