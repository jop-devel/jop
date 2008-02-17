/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package joptimizer.actions;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.MethodSignature;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractMethodAction;
import joptimizer.framework.actions.ActionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ControlFlowGraphPrinter extends AbstractMethodAction {

    public static final String ACTION_NAME = "printcfg";

    public static final String CONF_FORMAT = "format";
    public static final String CONF_OUTFILE = "outfile";

    private String format;
    private String outfile;
    private boolean useStdout;
    private PrintStream outTxt, outDot;

    public ControlFlowGraphPrinter(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    public void appendActionArguments(List options) {
        options.add(new StringOption(getActionId(), CONF_FORMAT,
                "Set the format for the graph output as comma-separated list (txt,dot).", "format"));
        options.add(new StringOption(getActionId(), CONF_OUTFILE,
                "Output file name without extension, default is stdout for txt, else 'outputpath/graph'.",
                "outfile"));
    }


    public String getActionDescription() {
        return "Print a control flow graph for a given method.";
    }

    public boolean doModifyClasses() {
        return false;
    }

    public boolean configure(JopConfig config) throws ConfigurationException {

        format = getActionOption(config, CONF_FORMAT, "txt");
        outfile = getActionOption(config, CONF_OUTFILE, format.equals("txt") ? "-" :
                config.getDefaultOutputPath() + File.separator + "graph" );

        return true;
    }


    public void startAction() throws ActionException {

        if ( "-".equals(outfile) ) {
            if ( format.indexOf("txt") != -1 ) {
                outTxt = System.out;
            }
            if ( format.indexOf("dot") != -1 ) {
                outDot = System.out;
            }

            useStdout = true;
        } else {
            try {
                if ( format.indexOf("txt") != -1 ) {
                    OutputStream outStream = new FileOutputStream(outfile + ".txt");
                    outTxt = new PrintStream(outStream);
                }
                if ( format.indexOf("dot") != -1 ) {
                    OutputStream outStream = new FileOutputStream(outfile + ".dot");
                    outTxt = new PrintStream(outStream);
                }
            } catch (FileNotFoundException e) {
                throw new ActionException("Could not open output file.", e);    
            }
            useStdout = true;
        }
    }

    public void finishAction() throws ActionException {

        if ( ! useStdout ) {
            if ( outTxt != null ) {
                outTxt.close();
            }
            if ( outDot != null ) {
                outDot.close();
            }
        }
    }

    public void execute(MethodInfo methodInfo) throws ActionException {

        MethodCode code = methodInfo.getMethodCode();
        if ( code == null ) {
            return;
        }

        ControlFlowGraph graph;
        try {
            graph = code.getGraph();
        } catch (GraphException e) {
            throw new ActionException("Could not get graph from method {"+methodInfo.getFQMethodName()+"}.", e);
        }

        // TODO print graph features, .. 

        if ( outTxt != null ) {
            try {
                printTextGraph(methodInfo, graph, outTxt);
            } catch (TypeException e) {
                throw new ActionException("Could not print text for method{"+methodInfo.getFQMethodName()+"}.", e);
            }
        }
    }

    private void printTextGraph(MethodInfo methodInfo, ControlFlowGraph graph, PrintStream out) throws TypeException {

        // Print header
        out.print(getMethodStub(methodInfo));
        out.println(" {");

        // print variables
        List vars = graph.getVariableTable().getVariables();
        for (int i = 0; i < vars.size(); i++) {
            Variable var = (Variable) vars.get(i);
            if ( var == null ) {
                continue;
            }

            out.print("    ");
            if ( var.getType() != null ) {
                out.print(var.getType().getTypeName());
            } else {
                out.print("var");
            }
            out.println(" " + var.getName());
        }
        out.println();

        // Print body
        Iterator bi = graph.getBlocks().iterator();
        int i = 0;
        while (bi.hasNext()) {
            BasicBlock block = (BasicBlock) bi.next();

            printBasicBlock(out, "", block);
        }

        if ( graph.getExceptionTable().getExceptionHandlers().size() > 0 ) {
            out.println("  Exceptiontable:");
            for (Iterator it = graph.getExceptionTable().getExceptionHandlers().iterator(); it.hasNext();) {
                BasicBlock.ExceptionHandler handler = (BasicBlock.ExceptionHandler) it.next();
                out.print("    E" + handler.getHandlerIndex() + ": ");
                if ( handler.getExceptionClass() == null ) {
                    out.print("*");
                } else {
                    out.print(handler.getExceptionClass().getClassName());
                }
                out.print(" by B" + handler.getExceptionBlock().getBlockIndex());
                out.println(" // " + handler.getHandledBlocks().size() + " handled blocks");
            }
        }


        out.println("}");
        out.println();

    }

    private void printBasicBlock(PrintStream out, String prefix, BasicBlock block) {
        String newLine = System.getProperty("line.separator");

        out.println(prefix + "B" + block.getBlockIndex() + ":");

        // TODO print stack

        // print statements
        int linenr = -1;
        for (Iterator it = block.getCodeBlock().getStatements().iterator(); it.hasNext();) {
            Statement stmt = (Statement) it.next();
            if ( stmt.getLineNumber() != linenr ) {
                linenr = stmt.getLineNumber();
                out.println(prefix + "    Line(" + linenr + "):");
            }
            out.println(prefix + "    " + stmt.getCodeLine().replace("\n", newLine + prefix + "    "));
        }

        // print targets
        if ( block.getTargetCount() > 0 || block.getNextBlockEdge() != null ) {
            out.println(prefix + "  targets:");
            BasicBlock.Edge edge;
            for (int i = 0; i < block.getTargetCount(); i++) {
                edge =  block.getTargetEdge(i);
                if ( edge != null ) {
                    out.println(prefix + "    #" + i + ": B" + edge.getTargetBlock().getBlockIndex());
                }
            }
            edge = block.getNextBlockEdge();
            if ( edge != null ) {
                out.println(prefix + "    next: B" + edge.getTargetBlock().getBlockIndex());
            }
        }

        if ( block.getExceptionHandlers().size() > 0 ) {
            out.print(prefix + "  exceptiontargets:");
            for (Iterator it = block.getExceptionHandlers().iterator(); it.hasNext();) {
                BasicBlock.ExceptionHandler handler = (BasicBlock.ExceptionHandler) it.next();
                out.print(" E");
                out.print(handler.getHandlerIndex());
            }
            out.println();
        }
    }

    private String getMethodStub(MethodInfo method) throws TypeException {
        StringBuffer out = new StringBuffer();
        MethodSignature signature = method.getMethodSignature();
        TypeInfo[] params = signature.getParameterTypes();

        out.append(method.getModifierString());
        out.append(signature.getType().getTypeName());
        out.append(" ");
        out.append(method.getClassInfo().getClassName());
        out.append(".");
        out.append(method.getName());
        out.append("(");
        for (int i = 0; i < params.length; i++) {
            if ( i > 0 ) out.append(", ");
            out.append(params[i].getTypeName());
        }
        out.append(")");

        return out.toString();
    }
}
