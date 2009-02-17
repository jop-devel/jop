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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.Vector;

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
import com.jopdesign.wcet08.config.LoggerConfig;
import com.jopdesign.wcet08.frontend.CallGraph;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.WcetAppInfo;
import com.jopdesign.wcet08.frontend.SourceAnnotations;
import com.jopdesign.wcet08.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet08.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.frontend.WcetAppInfo.MethodNotFoundException;
import com.jopdesign.wcet08.jop.JOPModel;
import com.jopdesign.wcet08.report.Report;

/** WCET 'project', information on which method in which class to analyse etc. */
public class Project {
	/* Hard errors */
	public static class UnsupportedFeatureException extends Error {
		private static final long serialVersionUID = 1L;
		public UnsupportedFeatureException(String msg) {
			super(msg);
		}
	}
	public static class AnalysisError extends Error {
		private static final long serialVersionUID = 1L;
		public AnalysisError(String msg, Exception inner) {
			super(msg,inner);
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
	public ProjectConfig getProjectConfig() {
		return projectConfig;
	}
	public Config getConfig() {
		return projectConfig.getConfigManager();
	}
	private String projectName;

	private WcetAppInfo wcetAppInfo;
	private CallGraph callGraph;
		
	private Map<ClassInfo, SortedMap<Integer, LoopBound>> annotationMap;

	private LoopBounds dfaLoopBounds;

	private boolean genWCETReport;
	private Report results;
	private ProcessorModel processor;
	private SourceAnnotations sourceAnnotations;

	public Project(ProjectConfig config) throws IOException {
		this.projectConfig =  config;
		this.projectName = projectConfig.getProjectName();
		{
			File outDir = projectConfig.getOutDir();
			Config.checkDir(outDir,true);
			File ilpDir = new File(outDir,"ilps");
			Config.checkDir(ilpDir, true);
		}
		if(projectConfig.doGenerateReport()) {
			this.results = new Report(projectConfig.getConfigManager(),
									  this,
									  projectConfig.getReportDir());
			this.genWCETReport = true;
		} else {
			this.genWCETReport = false;
		}
		this.processor = new JOPModel(this);
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
		return Config.sanitizeFileName(projectConfig.getAppClassName()+"_"+projectConfig.getTargetMethodName());		
	}
		
	public File getSourceFile(MethodInfo method) throws FileNotFoundException {
		return getSourceFile(method.getCli());
	}
	public File getClassFile(ClassInfo ci) throws FileNotFoundException {
		List<File> dirs = getSearchDirs(ci, projectConfig.getClassPath());
		for(File classDir : dirs) {
			String classname = ci.clazz.getClassName();
			classname = classname.substring(classname.lastIndexOf(".")+1);
			File classFile = new File(classDir, classname + ".class");
			if(classFile.exists()) return classFile;	
		}
		for(File classDir : dirs) {
			File classFile = new File(classDir, ci.clazz.getClassName()+".class");
			System.err.println("Class file not found: "+classFile);
		}
		throw new FileNotFoundException("Class file for "+ci.clazz.getClassName()+" not found.");
	}
	public File getSourceFile(ClassInfo ci) throws FileNotFoundException {
		List<File> dirs = getSearchDirs(ci, projectConfig.getSourcePath());
		for(File sourceDir : dirs) {
			File sourceFile = new File(sourceDir, ci.clazz.getSourceFileName());
			if(sourceFile.exists()) return sourceFile;	
		}
		throw new FileNotFoundException("Source for "+ci.clazz.getClassName()+" not found.");
	}
	private List<File> getSearchDirs(ClassInfo ci, String path) {
		List<File> dirs = new Vector<File>();
		StringTokenizer st = new StringTokenizer(path,File.pathSeparator);
		while (st.hasMoreTokens()) {
			String sourcePath = st.nextToken();
			String pkgPath = ci.clazz.getPackageName().replace('.', File.separatorChar);
			sourcePath += File.separator + pkgPath;
			dirs.add(new File(sourcePath));
		}
		return dirs;
	}
	public CallGraph getCallGraph() {
		return callGraph;
	}
	public ClassInfo getApplicationEntryClass() {
		return this.wcetAppInfo.getClassInfo(projectConfig.getAppClassName());
	}
	public MethodInfo getTargetMethod() {
		try {
			return wcetAppInfo.searchMethod(projectConfig.getTargetClass(),
					                        projectConfig.getTargetMethod());
		} catch (MethodNotFoundException e) {
			throw new AssertionError("Target method not found: "+e);
		}
	}
	public ClassInfo getTargetClass() {
		return wcetAppInfo.getClassInfo(projectConfig.getTargetClass());
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
		for(String klass : processor.getJVMClasses()) {
			appInfo.addClass(klass);			
		}
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
		wcetAppInfo = new WcetAppInfo(this,appInfo,processor);
		/* Initialize annotation map */
		annotationMap = new Hashtable<ClassInfo, SortedMap<Integer,LoopBound>>();
		sourceAnnotations = new SourceAnnotations(this);

		/* run dataflow analysis */
		if(projectConfig.doDataflowAnalysis()) {
			topLevelLogger.info("Starting DFA analysis");
			dataflowAnalysis();
			topLevelLogger.info("DFA analysis finished");
		}
		
		/* build callgraph */
		callGraph = CallGraph.buildCallGraph(wcetAppInfo,
											 projectConfig.getTargetClass(),
											 projectConfig.getTargetMethod());
	}

	public WcetAppInfo getWcetAppInfo() {
		return this.wcetAppInfo;
	}
	
	/**
	 * Get flow fact annotations for a class, lazily.
	 * @param cli
	 * @return
	 * @throws IOException
	 * @throws BadAnnotationException
	 */
	public SortedMap<Integer, LoopBound> getAnnotations(ClassInfo cli) throws IOException, BadAnnotationException {
		SortedMap<Integer, LoopBound> annots = this.annotationMap.get(cli);
		if(annots == null) {
			annots = sourceAnnotations.calculateWCA(cli);
			annotationMap.put(cli, annots);
		}
		return annots;		
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
	 */
	public LoopBounds getDfaLoopBounds() {
		return this.dfaLoopBounds;
	}
	/**
	 * Convenience delegator to get the flowgraph of the given method
	 */
	public ControlFlowGraph getFlowGraph(MethodInfo mi) {
		return wcetAppInfo.getFlowGraph(mi);
	}
	/**
	 * Convenience delegator to get the size of the given method
	 */
	public int getSizeInWords(MethodInfo mi) {
		return this.getFlowGraph(mi).getNumberOfWords();
	}
	
	public void writeReport() throws Exception {
		this.results.addStat( "classpath", projectConfig.getClassPath());
		this.results.addStat( "application", projectConfig.getAppClassName());
		this.results.addStat( "class", projectConfig.getTargetClass());
		this.results.addStat( "method", projectConfig.getTargetMethod());
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

	public int computeCyclomaticComplexity(MethodInfo m) {
		ControlFlowGraph g = getFlowGraph(m);
		int nLocal = g.getGraph().vertexSet().size();
		int eLocal = g.getGraph().edgeSet().size();
		int pLocal = g.getLoopBounds().size();
		int ccLocal = eLocal - nLocal + 2 * pLocal;
		int ccGlobal = 0;
		Iterator<CallGraphNode> iter = this.getCallGraph().getReferencedMethods(m);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			MethodInfo impl = n.getMethodImpl();
			ccGlobal += 2 + computeCyclomaticComplexity(impl);
		}
		return ccLocal + ccGlobal;
	}
	public ProcessorModel getProcessorModel() {
		return this.processor;
	}
}
