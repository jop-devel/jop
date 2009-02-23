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
package com.jopdesign.wcet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
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
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.frontend.CallGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.SourceAnnotations;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.frontend.WcetAppInfo.MethodNotFoundException;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.jop.CacheConfig;
import com.jopdesign.wcet.jop.JOPModel;
import com.jopdesign.wcet.report.Report;
import com.jopdesign.wcet.uppaal.UppAalConfig;

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
	private File resultRecord;

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
		if(projectConfig.saveResults()) {
			this.resultRecord = new File(config.getConfigManager().getOption(ProjectConfig.RESULT_FILE));
			if(! projectConfig.appendResults()) { resultRecord.delete(); resultRecord.createNewFile(); }
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
		for(CallGraphNode n: this.getCallGraph().getReferencedMethods(m)) {
			MethodInfo impl = n.getMethodImpl();
			ccGlobal += 2 + computeCyclomaticComplexity(impl);
		}
		return ccLocal + ccGlobal;
	}
	public ProcessorModel getProcessorModel() {
		return this.processor;
	}
	/* recording for scripted evaluatino */
	public void recordResult(WcetCost wcet, double timeDiff, double solverTime) {
		if(resultRecord == null) return;
		Config c = projectConfig.getConfigManager();		
		recordCVS("wcet","ipet",wcet,timeDiff,solverTime,
					c.getOption(CacheConfig.CACHE_IMPL),
					c.getOption(CacheConfig.CACHE_SIZE_WORDS),
					c.getOption(CacheConfig.CACHE_BLOCKS),
					c.getOption(CacheConfig.STATIC_CACHE_APPROX),
					c.getOption(CacheConfig.ASSUME_MISS_ONCE_ON_INVOKE));
		
	}
	public void recordResultUppaal(WcetCost wcet,
			                       double timeDiff, double searchtime,double solvertimemax) {
		if(resultRecord == null) return;
		Config c = projectConfig.getConfigManager();		
		recordCVS("wcet","uppaal",wcet,timeDiff,searchtime,solvertimemax,
				c.getOption(CacheConfig.CACHE_IMPL),
				c.getOption(CacheConfig.CACHE_SIZE_WORDS),
				c.getOption(CacheConfig.CACHE_BLOCKS),
				c.getOption(CacheConfig.DYNAMIC_CACHE_APPROX),
				projectConfig.getUppaalComplexityTreshold(),
				c.getOption(UppAalConfig.UPPAAL_COLLAPSE_LEAVES),
				c.getOption(UppAalConfig.UPPAAL_CONVEX_HULL),
				c.getOption(UppAalConfig.UPPAAL_TIGHT_BOUNDS));
	}
	public void recordSpecialResult(String metric, WcetCost cost) {
		if(resultRecord == null) return;
		if(projectConfig.appendResults()) return;
		recordCVS("metric",metric,cost);
	}
	public void recordMetric(String metric, Object... params) {
		if(resultRecord == null) return;
		if(projectConfig.appendResults()) return;
		recordCVS("metric",metric,null,params);
	}
	private void recordCVS(String key, String subkey, WcetCost cost, Object... params) {
		Object fixedCols[] = { key, subkey };
		try {			
			FileWriter fw = new FileWriter(resultRecord,true);
			fw.write(MiscUtils.joinStrings(fixedCols, ";"));
			if(cost != null) {
				Object costCols[] = { cost.getCost(), cost.getNonCacheCost(),cost.getCacheCost() }; 
				fw.write(";");
				fw.write(MiscUtils.joinStrings(costCols, ";"));				
			}
			if(params.length > 0) {
				fw.write(";");
				fw.write(MiscUtils.joinStrings(params, ";"));
			}
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
