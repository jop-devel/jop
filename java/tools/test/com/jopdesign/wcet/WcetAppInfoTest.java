/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008-2010, Benedikt Huber (benedikt.huber@gmail.com)
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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.graphutils.InvokeDot;
import com.jopdesign.common.graphutils.TypeGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WcetAppInfoTest {
    /*
     * DEMO
     * ~~~~
     */

    /* small demo using the class loader */	
    public static void main(String[] argv) {

        AppSetup appSetup = new AppSetup();
    
        WCETTool wcetTool = new WCETTool();
        appSetup.registerTool("wcet", wcetTool);
    
        Config config = appSetup.getConfig();
        config.setOption(ProjectConfig.PROJECT_NAME, "typegraph");
    
        AppInfo appInfo = appSetup.initAndLoad(argv, false, false, false);
    
        ProjectConfig pConfig = wcetTool.getProjectConfig();

        try {
            System.out.println("Classloader Demo: "+pConfig.getAppClassName());
            String rootClass = pConfig.getAppClassName();
            String rootPkg = rootClass.substring(0, rootClass.lastIndexOf("."));
            ClassInfo ci = appInfo.getClassInfo(pConfig.getAppClassName());
            System.out.println("Source file: "+ci.getSourceFileName());
            System.out.println("Root class: "+ci.toString());
            { 
                System.out.println("Writing type graph to "+pConfig.getOutFile("typegraph.png"));
                File dotFile = pConfig.getOutFile("typegraph.dot");
                FileWriter dotWriter = new FileWriter(dotFile);
                // FIXME TypeGraph is not used anymore, export ClassInfo/.. graph
                TypeGraph typeGraph = new TypeGraph();
                typeGraph.exportDOT(dotWriter,rootPkg);
                dotWriter.close();
                InvokeDot.invokeDot(wcetTool.getConfig(), dotFile, pConfig.getOutFile("typegraph.png"));
            }
            SuperGraph sg = new SuperGraph(appInfo,pConfig.getTargetMethodInfo().getCode().getControlFlowGraph(false), 0);
            {
                System.out.println("Writing supergraph graph to "+pConfig.getOutFile("supergraph.png"));
                File dotFile = pConfig.getOutFile("callgraph.dot");
                sg.exportDOT(dotFile);			
                InvokeDot.invokeDot(wcetTool.getConfig(), dotFile, pConfig.getOutFile("supergraph.png"));
            }
            CallGraph cg = appInfo.buildCallGraph(false);
            {
                System.out.println("Writing call graph to "+pConfig.getOutFile("callgraph.png"));
                File dotFile = pConfig.getOutFile("callgraph.dot");
                FileWriter dotWriter = new FileWriter(dotFile);
                cg.exportDOT(dotWriter);			
                dotWriter.close();			
                InvokeDot.invokeDot(wcetTool.getConfig(), dotFile, pConfig.getOutFile("callgraph.png"));
            }
                    
        } catch (IOException e) {
                e.printStackTrace();
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

}
