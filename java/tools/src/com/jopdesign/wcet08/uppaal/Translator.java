/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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
package com.jopdesign.wcet08.uppaal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import org.apache.log4j.Logger;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.CallGraph;
import com.jopdesign.wcet08.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet08.uppaal.model.XmlBuilder;
import com.jopdesign.wcet08.uppaal.model.XmlSerializationException;
import com.jopdesign.wcet08.uppaal.translator.MethodBuilder;
import com.jopdesign.wcet08.uppaal.translator.SystemBuilder;

public class Translator {
	private static final Logger logger = Logger.getLogger(Translator.class);

	private Project project;
	private SystemBuilder sys;

	public Translator(Project p) {
		this.project = p;
	}
	
	public SystemBuilder translateProgram() throws DuplicateKeyException {
		/* Get callgraph */
		CallGraph callGraph = project.getCallGraph();
		// logger.info("Call stack depth: "+callGraph.getMaxHeight());
		List<MethodInfo> impls = callGraph.getImplementedMethods();
		/* Create system builder */
		sys = new SystemBuilder(
				project, 
				callGraph.getMaxHeight(),
				impls.size());
		/* Translate methods */
		boolean collapseLeafs = Config.instance().getOption(UppAalConfig.UPPAAL_COLLAPSE_LEAVES);
		for(MethodInfo mi : impls) {
			MethodBuilder transl = new MethodBuilder(project,mi);
			if(mi.equals(project.getMeasuredMethod())) {
				sys.addTemplate(0,transl.buildRootMethod());				
			} else if(! collapseLeafs || ! project.getCallGraph().isLeafNode(mi)){
				sys.addTemplate(transl.getId(),transl.buildMethod());
			}
		}
		/* build the system */
		sys.buildSystem();
		return sys;
	}
	public void writeOutput() throws 
		XmlSerializationException, FileNotFoundException {
		String xml = XmlBuilder.domToString(sys.toXML());
		PrintStream outStreamXML = System.out, outStreamQ = System.out;
		File fileTemplate = null;
		Config config = Config.instance();
		if(config.getOutDir() != null) {
			outStreamXML = new PrintStream(getModelFile());
			outStreamQ = new PrintStream(getQueryFile());
		}
		outStreamXML.println(xml);

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("/* WCET Query */\n");
		stringBuilder.append("A[] (M0.E imply t<=0)\n");
		stringBuilder.append("E<> (M0.E && t >= 0)\n");
		
		outStreamQ.println(stringBuilder.toString());
		if(fileTemplate != null) {
			logger.info("Wrote XML file to " + fileTemplate);
			outStreamXML.close(); outStreamQ.close();
		}
		outStreamXML.close();
		outStreamQ.close();
	}

	public File getModelFile() {
		return Config.instance().getOutFile(project.getTargetName()+".xml");
	}

	public File getQueryFile() {
		return Config.instance().getOutFile(project.getTargetName()+".q");
	}

}
