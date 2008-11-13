/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2006, Rasmus Ulslev Pedersen
  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet08.frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.graphutils.Pair;

/**
 * Parsing source annotations for WCET analysis.
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Martin Schoeberl (martin@jopdesign.com)
 * @author Rasmus Ulslev Pedersen
 */
public class SourceAnnotations {
	private static final Logger logger = Logger.getLogger(SourceAnnotations.class);
	public static class BadAnnotationException extends Exception {
		private static final long serialVersionUID = 1L;
		private BasicBlock block;
		public BasicBlock getBlock() {
			return this.block;
		}
		public BadAnnotationException(String reason, BasicBlock block, int codeLineStart, int codeLineEnd) {
			super(reason+" for " + block.getLastInstruction()+ 
			      " in class " + block.getClassInfo().clazz.getClassName()  + ":" + 
				  codeLineStart + "-" + codeLineEnd);
			this.block = block;
		}
		public BadAnnotationException(String msg) {
			super(msg);
		}
	}
	public static class LoopBound extends Pair<Integer,Integer> {
		private static final long serialVersionUID = 1L;
		public LoopBound(Integer lb, Integer ub) {
			super(lb, ub);
		}
		public int getLowerBound()  { return fst(); }
		public int getUpperBound() { return snd(); }
		public static LoopBound boundedAbove(int ub) {
			return new LoopBound(0,ub);
		}
	}

	private Config config;
	public SourceAnnotations(Config config) {
		this.config = config;
	}

	/**
	 * Calculate the loop bounds for one class
	 * 
	 * @return a [Source Line] -> [Loop bound] map containing loop bounds for annotated
	 * source lines
	 * @throws IOException 
	 * @throws BadAnnotationException 
	 * 
	 */
	public  SortedMap<Integer,LoopBound> calculateWCA(ClassInfo ci) 
		throws IOException, BadAnnotationException {
		
		SortedMap<Integer, LoopBound> wcaMap = new TreeMap<Integer, LoopBound>();
		File fileName = config.getSourceFile(ci);
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		int lineNr = 1;

		while ((line = reader.readLine()) != null) {
			LoopBound wca = SourceAnnotations.calculateWCA(line);	
			if (wca != null) {
				wcaMap.put(lineNr,wca);
			}
			lineNr++;
		}
		logger.debug("Computed wca annotations for "+fileName);
		return wcaMap;
	}

	/**
	 * Return the loop bound for a java source lines. 
	 * <p>Loop bounds are tagged with one of the following annotations: 
	 * <ul> 
	 *  <li/>{@code @WCA loop &lt;?= [0-9]+}
	 *  <li/>{@code @WCA [0-9]+ &lt;= loop &lt;= [0-9]+}
	 * </ul>
	 * e.g. {@code // @WCA loop = 100} or {@code \@WCA 50 &lt;= loop &lt;= 60}.
	 * 
	 * @param line a java source code line (possibly annotated)
	 * @return the loop bound limit or null if no annotation was found
	 * @throws BadAnnotationException if the loop bound annotation has syntax errors or is
	 * invalid
	 */
	public static LoopBound calculateWCA(String wcaA)
		throws BadAnnotationException {

		int ai = wcaA.indexOf("@WCA");
		if(ai != -1 ){

			String annotString = wcaA.substring(ai+"@WCA".length());
			if(annotString.indexOf("loop") < 0) return null;
			
			Pattern pattern1 = Pattern.compile(" *loop *(<?=) *([0-9]+) *");
			Pattern pattern2 = Pattern.compile(" *([0-9]+) *<= *loop *<= *([0-9]+) *");
			Matcher matcher1 = pattern1.matcher(annotString);
			if(matcher1.matches()) {				
				int ub = Integer.parseInt(matcher1.group(2));
				int lb = (matcher1.group(1).equals("=")) ? ub : 0;
				return new LoopBound(lb,ub);		
			}
			Matcher matcher2 = pattern2.matcher(annotString);
			if(matcher2.matches()) {
				int lb = Integer.parseInt(matcher2.group(1));
				int ub = Integer.parseInt(matcher2.group(2));
				return new LoopBound(lb,ub);		
			}
			throw new BadAnnotationException("Syntax error in loop bound annotation: "+annotString);
		}
		return null;
	}

}
