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
package com.jopdesign.wcet.report;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.graphutils.InvokeDot;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.timing.jop.WCETInstruction;
import com.jopdesign.wcet.WCETTool;
import org.apache.bcel.classfile.LineNumber;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Analysis reports, using HTML framesets.
 * <p/>
 * TODO: This is an ad-hoc implementation. Design a good report concept.
 * TODO: html resources should be bundled in this package
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class Report {
    static final Logger logger = Logger.getLogger(WCETTool.LOG_WCET_REPORT+".Report");

    private ReportConfig config;
    private InvokeDot dotInvoker = null;

    private WCETTool project;

    private HashMap<ClassInfo, ClassReport> classReports =
            new HashMap<ClassInfo, ClassReport>();
    private HashMap<MethodInfo, List<DetailedMethodReport>> detailedReports =
            new HashMap<MethodInfo, List<DetailedMethodReport>>();
    private HashMap<String, Object> stats = new HashMap<String, Object>();
    private ReportEntry rootReportEntry = ReportEntry.rootReportEntry("summary.html");
    private HashMap<File, File> dotJobs = new HashMap<File, File>();

    public Report(WCETTool p, LogConfig logConfig) throws BadConfigurationException {
        this.project = p;
        this.config = new ReportConfig(p, logConfig);
        if (config.doInvokeDot()) {
            this.dotInvoker = new InvokeDot(InvokeDot.getDotBinary(p.getConfig()), config.getReportDir());
        }
    }

    /**
     * Initialize the velocity engine
     *
     * @throws Exception thrown by the velocity engine on init
     */
    public void initVelocity() throws Exception {
        Properties ps = new Properties();
        ps.put("resource.loader", "class");
        ps.put("class.resource.loader.description", "velocity: wcet class resource loader");
        ps.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        String templatedir = config.getTemplatePath();
        if (templatedir != null) {
            ps.put("resource.loader", "file, class");
            ps.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            ps.put("file.resource.loader.path", templatedir);
            ps.put("file.resource.loader.cache", "true");
        }
        Velocity.init(ps);
    }

    public void addStat(String key, Object val) {
        this.stats.put(key, val);
    }

    /**
     * add a HTML page, with the given name, order number and link
     *
     * @param name the name of the page
     * @param link the relative path to the page (e.g. <code>details/m1.html<code>)
     */
    public void addPage(String name, String link) {
        this.rootReportEntry.addPage(name, link);
    }

    public void generateFile(String templateName, File outFile, Map<String, Object> ctxMap) throws Exception {
        generateFile(templateName, outFile, new VelocityContext(ctxMap));
    }

    /**
     * Write the reports to disk
     *
     * @throws Exception
     */
    public void writeReport() throws Exception {
        this.addPage("logs/error.log", config.getErrorLogFile().toString());
        this.addPage("logs/info.log", config.getInfoLogFile().toString());
        generateBytecodeTable();
        generateIndex();
        generateSummary();
        generateTOC();
        generateDOT();
    }

    private void generateDOT() throws IOException {
        if (config.doInvokeDot() && dotInvoker != null) {
            for (Entry<File, File> dotJob : this.dotJobs.entrySet()) {
                dotInvoker.runDot(dotJob.getKey(), dotJob.getValue());
            }
        } else {
            FileWriter fw = new FileWriter(config.getReportFile("Makefile"));
            fw.append("dot:\n");
            for (Entry<File, File> dotJob : this.dotJobs.entrySet()) {
                fw.append("\tdot -Tpng -o " + dotJob.getValue().getName() + " " +
                        dotJob.getKey().getName() + "\n");
            }
            fw.close();
        }
    }

    private void generateBytecodeTable() throws IOException {
        File file = config.getReportFile("Bytecode WCET Table.txt");
        FileWriter fw = new FileWriter(file);
        
        // FIXME: generate proper timing table
        JOPConfig jopConfig = new JOPConfig(project.getConfig());
        fw.append(new WCETInstruction(jopConfig.rws(), jopConfig.wws()).toWCAString());

        fw.close();
        this.addPage("input/bytecodetable", file.getName());
    }

    private void generateIndex() throws Exception {
        generateFile("index.vm", config.getReportFile("index.html"), new VelocityContext());
    }

    private void generateTOC() throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("tree", this.rootReportEntry);
        generateFile("toc.vm", config.getReportFile("toc.html"), context);
    }

    private void generateSummary() throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("errorlog", config.getErrorLogFile());
        context.put("infolog", config.getInfoLogFile());
        context.put("stats", stats);
        generateFile("summary.vm", config.getReportFile("summary.html"), context);
    }

    private void generateFile(String templateName, File outFile, VelocityContext ctx)
            throws Exception {
        Template template;
        try {
            template = Velocity.getTemplate(templateName);
        } catch (ResourceNotFoundException ignored) {
            template = Velocity.getTemplate("com/jopdesign/wcet/report/" + templateName);
        }
        FileWriter fw = new FileWriter(outFile);
        template.merge(ctx, fw);
        fw.close();
    }

    /**
     * Dump the project's input (callgraph,cfgs)
     *
     * @throws IOException
     */
    public void generateInfoPages() throws IOException {
        this.addStat("#classes", project.getCallGraph().getClassInfos().size());
        this.addStat("#methods", project.getCallGraph().getReachableImplementationsSet(project.getTargetMethod()).size());
        this.addStat("max call stack ", project.getCallGraph().getMaximalCallStack());
        this.addStat("largest method size (in bytes)", project.getCallGraph().getLargestMethod().getNumberOfBytes());
        this.addStat("largest method size (in words)", project.getCallGraph().getLargestMethod().getNumberOfWords());
        this.addStat("total size of task (in bytes)", project.getCallGraph().getTotalSizeInBytes());
        generateInputOverview();
        this.addPage("details", null);
        for (MethodInfo m : project.getCallGraph().getReachableImplementationsSet(project.getTargetMethod())) {
            for (LineNumber ln : m.getCode().getLineNumberTable().getLineNumberTable()) {
                getClassReport(m.getClassInfo()).addLinePropertyIfNull(ln.getLineNumber(), "color", "lightgreen");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Generating report for method: " + m);
            }
            ControlFlowGraph flowGraph = project.getFlowGraph(m);
            Map<String, Object> stats = new TreeMap<String, Object>();
            stats.put("#nodes", flowGraph.vertexSet().size() - 2 /* entry+exit */);
            stats.put("number of words", flowGraph.getNumberOfWords());
            this.addDetailedReport(m,
                    new DetailedMethodReport(config, project, m, "CFG", stats, null, null),
                    true);
            generateDetailedReport(m);
        }
        for (ClassInfo c : project.getCallGraph().getClassInfos()) {
            ClassReport cr = getClassReport(c);
            String page = pageOf(c);
            HashMap<String, Object> ctx = new HashMap<String, Object>();
            ctx.put("classreport", cr);
            try {
                this.generateFile("class.vm", config.getReportFile(page), ctx);
            } catch (Exception e) {
                logger.error(e);
            }
            addPage("details/" + c.getClassName(), page);
        }
    }

    public ClassReport getClassReport(ClassInfo cli) {
        ClassReport cr = this.classReports.get(cli);
        if (cr == null) {
            try {
                cr = new ClassReport(cli, project.getSourceFile(cli));
            } catch (FileNotFoundException e) {
                throw new AssertionError("Unexpected FileNotFoundException: " + e);
            }
            this.classReports.put(cli, cr);
        }
        return cr;
    }

    private void generateInputOverview() throws IOException {
        HashMap<String, Object> ctx = new HashMap<String, Object>();

        File cgdot = config.getReportFile("callgraph.dot");
        File cgimg = config.getReportFile("callgraph.png");
        FileWriter fw = new FileWriter(cgdot);
        project.getCallGraph().exportDOT(fw);
        fw.close();
        recordDot(cgdot, cgimg);
        ctx.put("callgraph", "callgraph.png");

        List<MethodReport> mrv = new ArrayList<MethodReport>();
        for (MethodInfo m : project.getCallGraph().getReachableImplementationsSet(project.getTargetMethod())) {
            mrv.add(new MethodReport(project, m, pageOf(m)));
        }
        ctx.put("methods", mrv);


        try {
            this.generateFile("input_overview.vm", config.getReportFile("input_overview.html"), ctx);
        } catch (Exception e) {
            logger.error(e);
        }
        this.addPage("input", "input_overview.html");
    }

    void recordDot(File cgdot, File cgimg) {
        this.dotJobs.put(cgdot, cgimg);
    }

    private static String pageOf(ClassInfo ci) {
        return MiscUtils.sanitizeFileName(ci.getClassName()) + ".html";
    }

    private static String pageOf(MethodInfo i) {
        return MiscUtils.sanitizeFileName(i.getFQMethodName()) + ".html";
    }

    protected void addDetailedReport(MethodInfo m, DetailedMethodReport e, boolean prepend) {
        List<DetailedMethodReport> reports = this.detailedReports.get(m);
        if (reports == null) {
            reports = new LinkedList<DetailedMethodReport>();
            this.detailedReports.put(m, reports);
        }
        if (prepend) reports.add(0, e);
        else reports.add(e);
    }

    public void addDetailedReport(MethodInfo m, String key, Map<String, Object> stats,
                                  Map<CFGNode, ?> nodeAnnots,
                                  Map<ControlFlowGraph.CFGEdge, ?> edgeAnnots) {
        DetailedMethodReport re = new DetailedMethodReport(config, project, m, key, stats, nodeAnnots, edgeAnnots);
        this.addDetailedReport(m, re, false);
    }

    private void generateDetailedReport(MethodInfo method) {
        String page = pageOf(method);
        HashMap<String, Object> ctx = new HashMap<String, Object>();
        ctx.put("m", method);
        ctx.put("dfaresults", project.doDataflowAnalysis() ? project.getDfaTool().dumpDFA(method) : "");
        ctx.put("reports", this.detailedReports.get(method));
        for (DetailedMethodReport m : this.detailedReports.get(method)) {
            m.getGraph();
        }
        try {
            this.generateFile("method.vm", config.getReportFile(page), ctx);
        } catch (Exception e) {
            logger.error(e);
        }
        this.addPage("details/" +
                method.getClassInfo().getClassName() + "/" +
                sanitizePageKey(method.getMemberID().getMethodSignature()),
                page);
    }

    /* page keys may not contain a slash - replace it by backslash */

    private String sanitizePageKey(String s) {
        return s.replace('/', '\\');
    }
}
