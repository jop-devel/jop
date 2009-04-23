package com.jopdesign.timing.jop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.tools.JopInstr;

/** Table of micropathes.
 *  Will be used for CMP timing.
 *
 */
public class MicropathTable {
	final static String OPCODE_NAMES[] = new String[256];
	static {
		for(int i = 0; i < 256; i++) {
			OPCODE_NAMES[i] = JopInstr.name(i);
		}
	}
	static boolean isReserved(int opcode) {
		return OPCODE_NAMES[opcode].matches("res[0-9a-zA-F]{2,2}");
	}
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
		return ! (isReserved(opcode) || notImplemented.contains(opcode));
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
			if(isReserved(i)) {
				continue;
			}
			try {
				Integer addr = ana.getStartAddress(i);
				
				if(addr != null) {
					pt.hasMicrocode.add(i);
					pt.paths.put(i,ana.getMicrocodePaths(OPCODE_NAMES[i], addr));
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
