/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wcet;

import com.jopdesign.build.AppInfo;
import com.jopdesign.build.ClassInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Option;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.ProjectConfig;
import com.jopdesign.wcet.report.InvokeDot;
import com.jopdesign.wcet.report.ReportConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WcetAppInfoTest {
	/*
	 * DEMO
	 * ~~~~
	 */

	public static String USAGE = 
		"Usage: java [-Dconfig=file://<config.props>] "+ 
		AppInfo.class.getCanonicalName()+
		" [-outdir outdir] [-cp classpath] package.rootclass.rootmethod";

	/* small demo using the class loader */	
	public static void main(String[] argv) {
		ProjectConfig pConfig = null;
		try {
			Config config = Config.instance();
			Option<?> options[] = { ProjectConfig.APP_CLASS_NAME, ProjectConfig.TARGET_METHOD,
									ProjectConfig.OUT_DIR, ReportConfig.PROGRAM_DOT.mandatory() };			
			config.addOptions(options);
			String[] argvrest = Config.load(System.getProperty("config"),argv);
			pConfig = new ProjectConfig(config);
			if(argvrest.length == 1) {
				String target = argvrest[0];
				if(target.indexOf('(') > 0) target = target.substring(0,target.indexOf('('));
				config.setProperty(ProjectConfig.APP_CLASS_NAME.getKey(), target.substring(0,target.lastIndexOf('.')));
				config.setProperty(ProjectConfig.TARGET_METHOD.getKey(),argvrest[0]);
			}
			config.checkOptions();
		} catch(Config.BadConfigurationException e) {
			System.err.println(e);
			System.err.println(USAGE);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			Config config = Config.instance();
			System.out.println("Classloader Demo: "+pConfig.getAppClassName());
			String rootClass = pConfig.getAppClassName();
			String rootPkg = rootClass.substring(0,rootClass.lastIndexOf("."));
			config.setOption(ProjectConfig.PROJECT_NAME, "typegraph");
			Project p = new Project(new ProjectConfig(config));
			p.load();
			AppInfo wcetAi = p.getAppInfo();
			ClassInfo ci = wcetAi.getClassInfo(pConfig.getAppClassName());
			System.out.println("Source file: "+ci.clazz.getSourceFileName());
			System.out.println("Root class: "+ci.clazz.toString());
			{ 
				System.out.println("Writing type graph to "+p.getOutFile("typegraph.png"));
				File dotFile = p.getOutFile("typegraph.dot");
				FileWriter dotWriter = new FileWriter(dotFile);
				wcetAi.getTypeGraph().exportDOT(dotWriter,rootPkg);			
				dotWriter.close();
				InvokeDot.invokeDot(dotFile, p.getOutFile("typegraph.png"));
			}
			SuperGraph sg = new SuperGraph(wcetAi,wcetAi.getFlowGraph(p.getTargetMethod()),0);
			{
				System.out.println("Writing supergraph graph to "+p.getOutFile("supergraph.png"));
				File dotFile = p.getOutFile("callgraph.dot");
				FileWriter dotWriter = new FileWriter(dotFile);
				sg.exportDOT(dotWriter);			
				dotWriter.close();			
				InvokeDot.invokeDot(dotFile, p.getOutFile("supergraph.png"));				
			}
			CallGraph cg = CallGraph.buildCallGraph(wcetAi, pConfig.getTargetClass(), pConfig.getTargetMethod());
			{
				System.out.println("Writing call graph to "+p.getOutFile("callgraph.png"));
				File dotFile = p.getOutFile("callgraph.dot");
				FileWriter dotWriter = new FileWriter(dotFile);
				cg.exportDOT(dotWriter);			
				dotWriter.close();			
				InvokeDot.invokeDot(dotFile, p.getOutFile("callgraph.png"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MethodNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
