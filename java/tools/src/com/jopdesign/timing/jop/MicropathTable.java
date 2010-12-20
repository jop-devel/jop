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
 *  Table of microcode instruction sequences -- each analyzed opcode is associated with
 *  a set of microcode instruction sequences. The analysis also classifies the opcodes as
 *  either
 *  <ul>
 *  <li/> Implemented in micro code
 *    <ul>
 *    <li/> has valid microcode: if there is a microcode implementation and we can prove that it terminates
 *    <li/> analysis failed: if the analysis could not determine a set of finite microcode paths
 *     </ul>
 *  <li/> Not implemented in micro code
 *  <li/> Reserved opcode
 *  </ul>
 *
 */
public class MicropathTable implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Get the timing table for JOP
	 *
	 * @param asm the assembler file, or null if no File is available
	 * @return the path table
	 */
	public static MicropathTable getTimingTableFromAsmFile(File asm) throws IOException  {
		MicrocodeAnalysis ana = new MicrocodeAnalysis(asm.getPath());
		MicropathTable pt = new MicropathTable();
		for(int i = 0; i < 256; i++) {
			if(JopInstr.isReserved(i)) {
				continue;
			}
			Integer addr = ana.getStartAddress(i);
			if(addr != null) {
				pt.hasMicrocode.add(i);
				try {
					pt.paths.put(i,ana.getMicrocodePaths(JopInstr.OPCODE_NAMES[i], addr));
				} catch(MicrocodeVerificationException e) {
					pt.analysisErrors.put(i,e);
				}
			} else {
				pt.notImplemented.add(i);
			}
		}
		return pt;
	}

	private HashSet<Integer> hasMicrocode = new HashSet<Integer>();
	private HashSet<Integer> notImplemented = new HashSet<Integer>();
	private TreeMap<Integer, MicrocodeVerificationException> analysisErrors =
		new TreeMap<Integer, MicrocodeVerificationException>();

	private HashMap<Integer,Vector<MicrocodePath>> paths = new HashMap<Integer, Vector<MicrocodePath>>();

	/**
	 * Check whether the given instruction is implemented in microcode
	 * @param opcode
	 * @return {@code true} if we know the microcode implementation of the given instruction
	 */
	public boolean hasMicrocodeImpl(int opcode) {
		return hasMicrocode.contains(opcode);
	}

	/**
	 * Query whether there is a <b>analyzable</b> micro code implementation of the given instruction
	 * @param opcode
	 * @return
	 */
	public boolean hasTiming(int opcode) {
		return paths.containsKey(opcode);
	}

	public boolean hasBytecodeLoad(int i) {
		Vector<MicrocodePath> mps = getMicroPaths(i);
		if(mps == null) return false;
		for(MicrocodePath path : mps) {
			if(path.hasBytecodeLoad()) return true;
		}
		return false;
	}

	/**
	 * Query whether the analysis for the given instruction failed.
	 * Assumes {@code hasMicroCodeImpl(opcode)}
	 */
	public boolean timingAnalysisFailed(int opcode) {
		return analysisErrors.containsKey(opcode);
	}

	/**
	 * Get the error occurred during the analysis of the given instruction.
	 * @param opcode
	 * @return The error, or {@code null} if {@code !timingAnalysisFailed(opcode)}
	 */

	public MicrocodeVerificationException getAnalysisError(int opcode) {
		return analysisErrors.get(opcode);
	}

	/**
	 * Get the set of microcode instruction sequences for the given instruction.
	 * Note that we enumerate all paths for a given implementation.

	 * @param opcode
	 * @return
	 */
	public Vector<MicrocodePath> getMicroPaths(int opcode) {
		return paths.get(opcode);
	}

	/**
	 * Get the list of analysis errors, indexed by instruction.
	 * @return
	 */
	public TreeMap<Integer, MicrocodeVerificationException> getAnalysisErrors() {
		return analysisErrors;
	}

}
