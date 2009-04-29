package com.jopdesign.timing.jop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.tools.JopInstr;

/** 
 *  Table of micropathes.
 *  Used to generate the JOPTimingTable and will be used to calculate CMP timings.
 */
public class MicropathTable {
	private HashSet<Integer> hasMicrocode = new HashSet<Integer>();	
	public boolean hasMicrocodeImpl(int opcode) {
		return hasMicrocode.contains(opcode);
	}

	private HashMap<Integer,Vector<MicrocodePath>> paths = new HashMap<Integer, Vector<MicrocodePath>>();
	public Vector<MicrocodePath> getMicroPaths(int opcode) {
		return paths.get(opcode);
	}
	
	private HashSet<Integer> notImplemented = new HashSet<Integer>();
	
	public boolean isImplemented(int opcode) {
		return ! (JopInstr.isReserved(opcode) || notImplemented.contains(opcode));
	}
	
	public boolean hasTiming(int opcode) {
		return paths.containsKey(opcode);
	}
	
	private TreeMap<Integer, MicrocodeVerificationException> analysisErrors =
		new TreeMap<Integer, MicrocodeVerificationException>();
	public MicrocodeVerificationException getAnalysisError(int opcode) { 
		return analysisErrors.get(opcode); 
	}
	public TreeMap<Integer, MicrocodeVerificationException> getAnalysisErrors() {
		return analysisErrors;
	}


	public static MicropathTable getTimingTable(File asm) throws IOException {
		MicrocodeAnalysis ana = new MicrocodeAnalysis(asm.getPath());
		MicropathTable pt = new MicropathTable();
		for(int i = 0; i < 256; i++) {
			if(JopInstr.isReserved(i)) {
				continue;
			}
			try {
				Integer addr = ana.getStartAddress(i);
				
				if(addr != null) {
					pt.hasMicrocode.add(i);
					pt.paths.put(i,ana.getMicrocodePaths(JopInstr.OPCODE_NAMES[i], addr));
				} else {
					pt.notImplemented.add(i);
				}
			} catch(MicrocodeVerificationException e) {
				pt.analysisErrors.put(i,e);
			} 
		}
		return pt;
	}

	
}
