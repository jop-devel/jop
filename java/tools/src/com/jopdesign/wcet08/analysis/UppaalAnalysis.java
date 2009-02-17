package com.jopdesign.wcet08.analysis;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.uppaal.Translator;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.WcetSearch;
import com.jopdesign.wcet08.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet08.uppaal.model.XmlSerializationException;

public class UppaalAnalysis {

	private MethodInfo target;
	private Logger logger;
	private Project project;
	private File outDir;
	private double searchtime;
	private double solvertimemax;
	public UppaalAnalysis(Logger logger, Project project, File outDir) {
		this.logger = logger;
		this.project = project;
		this.outDir = outDir;
	}
	public WcetCost computeWCET(MethodInfo targetMethod, long upperBound) throws IOException, DuplicateKeyException, XmlSerializationException {
		if(project.getProjectConfig().hasUppaalComplexityTreshold()) {
			return computeWCETWithTreshold(targetMethod,upperBound,project.getProjectConfig().getUppaalComplexityTreshold());
		} else {
			return calculateWCET(targetMethod, upperBound);
		}
	}
	public WcetCost computeWCETWithTreshold(MethodInfo targetMethod, long upperBound, Long uppaalComplexityTreshold) {
		throw new Project.UnsupportedFeatureException("compute uppaal wcet with treshold");
	}
	public WcetCost calculateWCET(MethodInfo m) throws IOException, DuplicateKeyException, XmlSerializationException {
		return calculateWCET(m,-1);
	}
	public WcetCost calculateWCET(MethodInfo m, long ub) throws IOException, DuplicateKeyException, XmlSerializationException {
		searchtime = 0;
		Long upperBound = null;
		if(ub > 0) upperBound = ub;
		solvertimemax = 0;
		logger.info("Starting UppAal translation");
		Translator translator = new Translator(project, outDir);
		translator.translateProgram(m);
		translator.writeOutput();
		logger.info("model and query can be found in "+outDir);
		logger.info("model file: "+translator.getModelFile());
		if(UppAalConfig.hasVerifier(project.getConfig())) {
			logger.info("Starting verification");
			WcetSearch search = new WcetSearch(project.getConfig(),translator.getModelFile());
			long start = System.nanoTime();
			long wcet = search.searchWCET(upperBound);
			long end = System.nanoTime();		
			this.searchtime = ((double)(end-start))/1E9;
			this.solvertimemax = search.getMaxSolverTime();
			return WcetCost.totalCost(wcet);
		} else {
			throw new IOException("No verifier binary available. Skipping search");
		}
	}
	public double getSearchtime() {
		return searchtime;
	}
	public double getSolvertimemax() {
		return solvertimemax;
	}
}
