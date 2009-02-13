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
package com.jopdesign.wcet08;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.log4j.Logger;
import com.jopdesign.build.AppInfo;
import com.jopdesign.build.AppVisitor;
import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.build.WcetPreprocess;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.analyses.ReceiverTypes;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.frontend.CallGraph;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.WcetAppInfo;
import com.jopdesign.wcet08.frontend.SourceAnnotations;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.report.Report;

/** WCET 'project', information on which method in which class to analyse etc. */
public class Project {
	public static class UnsupportedFeatureException extends Exception {
		private static final long serialVersionUID = 1L;
		public UnsupportedFeatureException(String msg) {
			super(msg);
		}
	}
	/**
	 * Remove NOPs in all reachable classes
	 */
	public static class RemoveNops extends AppVisitor {
		public RemoveNops(AppInfo ai) {
			super(ai);
		}
		@Override
		public void visitJavaClass(JavaClass clazz) {
			super.visitJavaClass(clazz);
			ClassInfo cli = super.getCli();
			for(MethodInfo m : cli.getMethods()) {
				m.getMethodGen().removeNOPs();
				m.updateMethodFromGen();
			}
		}		
	}
	/**
	 * Set {@link MethodGen} in all reachable classes 
	 */
	public static class CreateMethodGenerators extends AppVisitor {
		public CreateMethodGenerators(AppInfo ai) {
			super(ai);
		}
		public void visitJavaClass(JavaClass clazz) {
			super.visitJavaClass(clazz);
			ConstantPoolGen cpg = new ConstantPoolGen(clazz.getConstantPool());
			Method[] methods = clazz.getMethods();
			for(int i=0; i < methods.length; i++) {
				if(!(methods[i].isAbstract() || methods[i].isNative())) {
					Method m = methods[i];
			        MethodInfo mi = getCli().getMethodInfo(m.getName()+m.getSignature());
			        mi.setMethodGen(new MethodGen(m,
			        							  mi.getCli().clazz.getClassName(),
			        							  cpg));
				}
			}
		}
	}

	public static final Logger logger = Logger.getLogger(Project.class);
	private Logger topLevelLogger = Logger.getLogger(Project.class); /* special logger */
	public void setTopLevelLooger(Logger tlLogger) {
		this.topLevelLogger = tlLogger;		
	}

	private ProjectConfig projectConfig;
	private String projectName;

	private WcetAppInfo wcetAppInfo;
	private CallGraph callGraph;
		
	private Map<ClassInfo, SortedMap<Integer, LoopBound>> annotationMap;

	private LoopBounds dfaLoopBounds;

	private boolean genWCETReport;
	private Report results;

	public Project(Config config) throws IOException {
		this.projectConfig =  new ProjectConfig(config);
		this.projectName = projectConfig.getProjectName();
		{
			File outDir = projectConfig.getOutDir();
			Config.checkDir(outDir,true);
			File ilpDir = new File(outDir,"ilps");
			Config.checkDir(ilpDir, true);
		}
		if(projectConfig.doGenerateReport()) {
			this.results = new Report(config,this, projectConfig.getReportDir());
			this.genWCETReport = true;
		} else {
			this.genWCETReport = false;
		}
	}
	public String getProjectName() {
		return this.projectName;
	}
	public boolean reportGenerationActive() {
		return this.genWCETReport ;
	}
	public void setGenerateWCETReport(boolean generateReport) {
		this.genWCETReport = generateReport;
	}

	public String getTargetName() {
		return Config.sanitizeFileName(projectConfig.getAppClassName()+"_"+projectConfig.getMeasureTarget());		
	}
		
	public File getSourceFile(MethodInfo method) throws FileNotFoundException {
		return getSourceFile(method.getCli());
	}
	
	public File getSourceFile(ClassInfo ci) throws FileNotFoundException {
		StringTokenizer st = new StringTokenizer(projectConfig.getSourcePath(),File.pathSeparator);
		while (st.hasMoreTokens()) {
			String sourcePath = st.nextToken();
			String pkgPath = ci.clazz.getPackageName().replace('.', File.separatorChar);
			sourcePath += File.separator + pkgPath;
			File sourceDir = new File(sourcePath);
			File sourceFile = new File(sourceDir, ci.clazz.getSourceFileName());
			if(sourceFile.exists()) return sourceFile;	
		}
		throw new FileNotFoundException("Source for "+ci.clazz.getClassName()+" not found.");
	}
	public CallGraph getCallGraph() {
		return callGraph;
	}
	public ClassInfo getApplicationEntryClass() {
		return this.wcetAppInfo.getClassInfo(projectConfig.getAppClassName());
	}
	public ClassInfo getMeasuredClass() {
		return callGraph.getRootClass();
	}
	public MethodInfo getMeasuredMethod() {
		return callGraph.getRootMethod();
	}
	public Report getReport() { return results; }
	public boolean doWriteReport() {
		return projectConfig.getReportDir() != null;
	}
	
	public AppInfo loadApp() throws IOException {
		AppInfo appInfo;
		if(projectConfig.doDataflowAnalysis()) {
			appInfo = new com.jopdesign.dfa.framework.DFAAppInfo(
							new com.jopdesign.dfa.framework.DFAClassInfo());
		} else {
			appInfo = new AppInfo(ClassInfo.getTemplate());
		}
		appInfo.configure(projectConfig.getClassPath(),
						  projectConfig.getSourcePath(),
						  projectConfig.getAppClassName());
		appInfo.addClass(WcetAppInfo.JVM_CLASS);
		if(projectConfig.doDataflowAnalysis()) {			
			appInfo.load();
			appInfo.iterate(new RemoveNops(appInfo));
		} else {
			appInfo.load();
			WcetPreprocess.preprocess(appInfo);
			appInfo.iterate(new CreateMethodGenerators(appInfo));			
		}
		return appInfo;
	}
	
	public void load() throws Exception  {
		AppInfo appInfo = loadApp();
		wcetAppInfo = new WcetAppInfo(appInfo);

		/* run dataflow analysis */
		if(projectConfig.doDataflowAnalysis()) {
			topLevelLogger.info("Starting DFA analysis");
			dataflowAnalysis();
			topLevelLogger.info("DFA analysis finished");
		}
		
		/* build callgraph */
		callGraph = CallGraph.buildCallGraph(wcetAppInfo,
											 projectConfig.getMeasuredClass(),
											 projectConfig.getMeasuredMethod());

		/* Load source code annotations */
		annotationMap = new Hashtable<ClassInfo, SortedMap<Integer,LoopBound>>();
		SourceAnnotations sourceAnnotations = new SourceAnnotations(this);
		for(ClassInfo ci : callGraph.getClassInfos()) {
			annotationMap.put(ci,sourceAnnotations.calculateWCA(ci));
		}
		/* Analyse control flow graphs */
		wcetAppInfo.analyseFlowGraphs(this, this.callGraph.getImplementedMethods());
	}

	public WcetAppInfo getWcetAppInfo() {
		return this.wcetAppInfo;
	}
	public SortedMap<Integer, LoopBound> getAnnotations(ClassInfo cli) {
		return this.annotationMap.get(cli);
	}
	
	/* Data flow analysis
	 * ------------------
	 */
	public boolean doDataflowAnalysis() {
		return projectConfig.doDataflowAnalysis();
	}
	
	public com.jopdesign.dfa.framework.DFAAppInfo getDfaProgram() {
		return (com.jopdesign.dfa.framework.DFAAppInfo) this.wcetAppInfo.getAppInfo();
	}
	
	@SuppressWarnings("unchecked")
	public void dataflowAnalysis() {
		com.jopdesign.dfa.framework.DFAAppInfo program = getDfaProgram();
		topLevelLogger.info("Receiver analysis");
		ReceiverTypes recTys = new ReceiverTypes();
		Map<InstructionHandle, ContextMap<String, String>> receiverResults = 
			program.runAnalysis(recTys);
		program.setReceivers(receiverResults);
		wcetAppInfo.setReceivers(receiverResults);
		topLevelLogger.info("Loop bound analysis");
		dfaLoopBounds = new LoopBounds();
		program.runAnalysis(dfaLoopBounds);
	}
	/**
	 * Get the loop bounds found by dataflow analysis
	 * @return
	 */
	public LoopBounds getDfaLoopBounds() {
		return this.dfaLoopBounds;
	}
	/**
	 * Convenience delegator to get the flowgraph of the given method
	 * @param mi
	 * @return
	 */
	public ControlFlowGraph getFlowGraph(MethodInfo mi) {
		return wcetAppInfo.getFlowGraph(mi);
	}
	
	public void writeReport() throws Exception {
		this.results.addStat( "classpath", projectConfig.getClassPath());
		this.results.addStat( "application", projectConfig.getAppClassName());
		this.results.addStat( "class", projectConfig.getMeasuredClass());
		this.results.addStat( "method", projectConfig.getMeasuredMethod());
		this.results.writeReport();
	}
	
	public File getOutDir(String sub) {
		File outDir = projectConfig.getOutDir();
		File subDir = new File(outDir,sub);
		if(! subDir.exists()) subDir.mkdir();
		return subDir;
	}
	public File getOutFile(String file) {
		return new File(projectConfig.getOutDir(),file);
	}
}
