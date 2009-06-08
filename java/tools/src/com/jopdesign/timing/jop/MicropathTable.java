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
package com.jopdesign.timing.jop;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.tools.JopInstr;

/** 
 *  Table of micropathes.
 *  Used to generate the JOPTimingTable and will be used to calculate CMP timings.
 *  If the assembler File is not available, we use a static timing table.
 */
public class MicropathTable implements Serializable {
	private static final long serialVersionUID = 1L;

	private HashSet<Integer> hasMicrocode = new HashSet<Integer>();	
	private HashMap<Integer,Vector<MicrocodePath>> paths = new HashMap<Integer, Vector<MicrocodePath>>();
	private HashSet<Integer> notImplemented = new HashSet<Integer>();
	private TreeMap<Integer, MicrocodeVerificationException> analysisErrors =
		new TreeMap<Integer, MicrocodeVerificationException>();
	
	public boolean hasMicrocodeImpl(int opcode) {
		return hasMicrocode.contains(opcode);
	}

	public Vector<MicrocodePath> getMicroPaths(int opcode) {
		return paths.get(opcode);
	}
	
	
	public boolean isImplemented(int opcode) {
		return ! (JopInstr.isReserved(opcode) || notImplemented.contains(opcode));
	}
	
	public boolean hasTiming(int opcode) {
		return paths.containsKey(opcode);
	}
	
	public MicrocodeVerificationException getAnalysisError(int opcode) { 
		return analysisErrors.get(opcode); 
	}
	public TreeMap<Integer, MicrocodeVerificationException> getAnalysisErrors() {
		return analysisErrors;
	}
	/** Get the timing table for JOP
	 * 
	 * @param asm the assembler file, or null if no File is available
	 * @return the path table
	 */
	public static MicropathTable getTimingTable(File asm) {		
		MicropathTable mpt = null;
		try {
			mpt = getTimingTableFromAsmFile(asm);
		} catch (IOException e) {
			throw new AssertionError("Failed to read assembler file");
		}
		return mpt;
	}


	public static MicropathTable getTimingTableFromAsmFile(File asm) throws IOException  {
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
