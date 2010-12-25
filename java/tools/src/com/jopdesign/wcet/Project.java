/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet;

import com.jopdesign.build.WcetPreprocess;
import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.JamuthModel;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.wcet.allocation.BlockAllocationModel;
import com.jopdesign.wcet.allocation.HandleAllocationModel;
import com.jopdesign.wcet.allocation.HeaderAllocationModel;
import com.jopdesign.wcet.allocation.ObjectAllocationModel;
import com.jopdesign.wcet.annotations.BadAnnotationException;
import com.jopdesign.wcet.annotations.SourceAnnotationReader;
import com.jopdesign.wcet.annotations.SourceAnnotations;
import com.jopdesign.wcet.jop.JOPWcetModel;
import com.jopdesign.wcet.jop.LinkerInfo;
import com.jopdesign.wcet.report.Report;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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
	public static class RemoveNops implements ClassVisitor {
        @Override
        public boolean visitClass(ClassInfo classInfo) {
            for(MethodInfo m : classInfo.getMethods()) {
                if (m.isAbstract()) continue;
                m.getCode().removeNOPs();
            }
            return true;
        }

        @Override
        public void finishClass(ClassInfo classInfo) {
        }
	}

	public void setTopLevelLogger(Logger tlLogger) {
		this.topLevelLogger = tlLogger;
	}


	public AppInfo loadApp() throws IOException {
		AppInfo appInfo;
		if(projectConfig.doDataflowAnalysis()) {
			appInfo = new DFATool(
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
		try {
			if(projectConfig.doDataflowAnalysis()) {
					appInfo.load();
				appInfo.iterate(new RemoveNops(appInfo));
			} else {
				appInfo.load();
				WcetPreprocess.preprocess(appInfo);
				appInfo.iterate(new CreateMethodGenerators(appInfo));
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new AssertionError(e.getMessage());
		}
		return appInfo;
	}

	public void load() throws Exception  {
		AppInfo appInfo = loadApp();
		this.appInfo = new WcetAppInfo(this,appInfo,processor);
		/* Initialize annotation map */
		annotationMap = new HashMap<ClassInfo, SourceAnnotations>();
		sourceAnnotations = new SourceAnnotationReader(this);
	}

	/**
	 * Get flow fact annotations for a class, lazily.
	 * @param cli
	 * @return
	 * @throws IOException
	 * @throws BadAnnotationException
	 */
	public SourceAnnotations getAnnotations(ClassInfo cli) throws IOException, BadAnnotationException {
		SourceAnnotations annots = this.annotationMap.get(cli);
		if(annots == null) {
			annots = sourceAnnotations.readAnnotations(cli);
			annotationMap.put(cli, annots);
		}
		return annots;
	}


}
