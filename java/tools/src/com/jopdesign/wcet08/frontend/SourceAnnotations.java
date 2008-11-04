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
		private String msg;

		public BadAnnotationException(String reason, BasicBlock block, int codeLineStart, int codeLineEnd) {
			this.block = block;
			this.msg = reason+" for " + block.getLastInstruction()+ 
					          " in class " + block.getClassInfo().clazz.toString()  + ":" + 
					          codeLineStart + "-" + codeLineEnd;
		}
		@Override
		public String getMessage() {
			return msg;
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
	 * 
	 */
	public  SortedMap<Integer,Integer> calculateWCA(ClassInfo ci) throws IOException {
		SortedMap<Integer, Integer> wcaMap = new TreeMap<Integer, Integer>();
		File fileName = config.getSourceFile(ci);
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		int lineNr = 1;

		while ((line = reader.readLine()) != null) {
			int wca = SourceAnnotations.calculateWCA(line);	
			if (wca != -1) {

				wcaMap.put(lineNr,wca);
			}
			lineNr++;
		}
		logger.debug("Computed wca annotations for "+fileName);
		return wcaMap;
	}

	/**
	 * Return the loop bound for a java source lines. Loop bounds are tagged with
	 * the following expression: // *@WCA *loop *<?= *[0-9]+ *
	 * e.g. // @WCA loop = 100
	 * 
	 * @param line a java source code line (possibly annotated)
	 * @return the loop bound limit or -1 if no annotation was found or the annotation
	 * was erroneous
	 */
	public static int calculateWCA(String wcaA){

		int ai = wcaA.indexOf("@WCA");
		if(ai != -1){

			String c = wcaA.substring(ai+"@WCA".length());

			Pattern pattern = Pattern.compile(" *loop *<?= *([0-9]+) *");
			Matcher matcher = pattern.matcher(c);

			if (! matcher.matches()) {
				//logger.error("Invalid WCA string: @WCA" + c + ". It must be of the form \" *loop *<?= *[0-9]+ *\"");
				return -1;
			}

			int val = Integer.parseInt(matcher.group(1));

			return val;

		}
		return -1;
	}

}
