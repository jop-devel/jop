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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.WCETTool;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DetailedMethodReport {
    Map<String, Object> stats;
    Map<CFGNode, ?> nodeAnnotations;
    Map<ControlFlowGraph.CFGEdge, ?> edgeAnnotations;
    private String graphLink;
    private MethodInfo method;
    private String key;
    private WCETTool project;
    private ReportConfig config;

    private static final Logger logger = Logger.getLogger(WCETTool.LOG_WCET_REPORT+".DetailedMethodReport");

    public DetailedMethodReport(ReportConfig c,
                                WCETTool p, MethodInfo m,
                                String key, Map<String, Object> stats,
                                Map<CFGNode, ?> wcets, Map<ControlFlowGraph.CFGEdge, ?> flowMapOut) {
        this.config = c;
        this.project = p;
        this.method = m;
        this.key = key;
        this.stats = stats;
        this.nodeAnnotations = wcets;
        this.edgeAnnotations = flowMapOut;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public String getGraph() {
        if (graphLink == null) {
            File graphfile;
            try {
                graphfile = generateGraph(method, key, nodeAnnotations, edgeAnnotations);
                graphLink = graphfile.getName();
            } catch (IOException e) {
                logger.error("Failed to generate graph file for " + method, e);
            }
        }
        return graphLink;
    }

    public String getKey() {
        return key;
    }

    private File generateGraph(MethodInfo method, String key, Map<CFGNode, ?> nodeAnnotations,
                               Map<ControlFlowGraph.CFGEdge, ?> edgeAnnotations) throws IOException {
        File cgdot = config.getOutFile(method, key + ".dot");
        File cgimg = config.getOutFile(method, key + ".png");
        ControlFlowGraph flowGraph = project.getFlowGraph(method);
        if (nodeAnnotations != null || edgeAnnotations != null) {
            flowGraph.exportDOT(cgdot, nodeAnnotations, edgeAnnotations);
        } else {
            flowGraph.exportDOT(cgdot, new WCETNodeLabeller(project), null);
        }
        project.getReport().recordDot(cgdot, cgimg);
        return cgimg;
    }
}
