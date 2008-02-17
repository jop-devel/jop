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

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.MethodInvocation;
import com.jopdesign.libgraph.struct.TypeException;
import joptimizer.config.ConfigurationException;
import joptimizer.config.IntOption;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractAction;
import joptimizer.framework.actions.ActionException;
import joptimizer.framework.visit.EmptyStructVisitor;
import joptimizer.framework.visit.MethodInvocationTraverser;
import joptimizer.framework.visit.StatisticsVisitor;
import joptimizer.framework.visit.StructVisitorList;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CallGraphPrinter extends AbstractAction {

    public static final String ACTION_NAME = "printcallgraph";

    public static final String CONF_CTROOT = "start";
    public static final String CONF_CTDEPTH = "maxdepth";
    public static final String CONF_CTFORMAT = "format";
    public static final String CONF_CTOUTFILE = "outfile";
    public static final String CONF_CTIGNORE = "ignore";

    private static Logger logger = Logger.getLogger(CallGraphPrinter.class);

    /**
     * a text format writer implementation.
     */
    private class TxtFilePrinter extends EmptyStructVisitor {

        private PrintStream out;
        private Stack depth;

        public TxtFilePrinter(PrintStream out) {
            this.out = out;
            reset();
        }

        public void reset() {
            depth = new Stack();
        }

        public void start(ClassInfo classInfo, MethodInfo methodInfo) {
            printMethod(classInfo, methodInfo, null, "");
        }

        public void traverseDown(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
            depth.push(Boolean.valueOf(hasNext));
        }

        public void traverseUp(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
            depth.pop();
        }

        public void visitMethodInvocation(MethodInvocation invoke) {
            StringBuffer prefix = new StringBuffer();

            for (int i = 1; i < depth.size(); i++) {
                if ( ((Boolean)depth.get(i)).booleanValue() ) {
                    prefix.append("|   ");
                } else {
                    prefix.append("    ");
                }
            }

            printMethod(invoke.getInvokedClass(), invoke.getInvokedMethod(), invoke, prefix.toString());
        }

        private void printMethod(ClassInfo classInfo, MethodInfo methodInfo, MethodInvocation invoke, String prefix) {
            out.print(prefix);
            if ( invoke != null ) out.print("+ ");
            out.print(classInfo.getClassName());
            out.print(".");
            try {
                out.print(methodInfo.getMethodSignature().getFullName());
            } catch (TypeException e) {
                out.print(methodInfo.getName());
                out.print("()");
                logger.error("Invalid signature found.", e);
            }
            out.println();
        }
    }

    /**
     * a dot format writer implementation.
     */
    private class DotFilePrinter extends EmptyStructVisitor {

        private PrintStream out;
        private Set visitedMethods;
        private Set visitedInvokes;
        private Map methodMapping;
        private int depth;
        private int skipDepth;

        public DotFilePrinter(PrintStream out) {
            this.out = out;
            reset();
        }

        public void reset() {
            skipDepth = -1;
            depth = 0;
            visitedMethods = new HashSet();
            visitedInvokes = new HashSet();
            methodMapping = new HashMap();
        }

        public void start(ClassInfo classInfo, MethodInfo methodInfo) {
            out.println("digraph CT {");
            out.println("    rankdir=LR;");
        }

        public void traverseDown(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {

            // check if we haven't visited this one before, skip subtree if so
            if ( skipDepth == - 1) {
                String methodName = methodInfo.getFQMethodName();
                if ( visitedMethods.contains(methodName) ) {
                    skipDepth = depth;
                } else {
                    visitedMethods.add(methodName);
                }
            }
            
            depth++;
        }

        public void traverseUp(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
            depth--;
            
            // returned from skipped subtree
            if ( skipDepth == depth ) {
                skipDepth = -1;
            }
        }

        public void visitMethodInvocation(MethodInvocation invoke) {

            // ignore already visited invokers and subtree
            if ( skipDepth != -1 ) {
                return;
            }

            String invoker = getMappedName(invoke.getInvoker());
            String invoked = getMappedName(invoke.getInvokedClass(), invoke.getInvokedMethod());
            String realInvoked = getMappedName(invoke.getInvokedMethod());

            String invokeEdge = invoker + " -> " + invoked;

            // skip multiple invocations from one method
            if ( visitedInvokes.contains(invokeEdge) ) {
                return;
            }

            visitedInvokes.add(invokeEdge);

            out.print("    ");
            out.print(invokeEdge);
            out.println(";");

            // this is a call to a method of a superclass which is not overwritten
            if ( !invoked.equals(realInvoked) ) {
                out.print("    ");
                out.print(realInvoked);
                out.print(" -> ");
                out.print(invoked);
                out.println(" [style=dotted];");
            }
        }

        public void finish() {
            out.println("}");
        }

        private String getMappedName(MethodInfo methodInfo) {
            return getMappedName(null, methodInfo);
        }

        private String getMappedName(ClassInfo classInfo, MethodInfo methodInfo) {

            String name;
            if ( classInfo == null ) {
                name = methodInfo.getFQMethodName();
            } else {
                name = MethodInfo.createFQMethodName(classInfo.getClassName(), methodInfo.getName(), methodInfo.getSignature());
            }
            String mapped = (String) methodMapping.get(name);

            // not found, create a new mapping
            if ( mapped == null ) {
                mapped = "m" + methodMapping.size();
                methodMapping.put(name, mapped);

                // generate method node label
                out.print("    ");
                out.print(mapped);
                out.print(" [label=\"");
                out.print(name);
                out.println("\"];");
            }
            
            return mapped;
        }

    }

    private ClassInfo rootClass;
    private MethodInfo rootMethod;
    private boolean useStdout;
    private String outfile;
    private String format;
    private String[] ignore;
    private int maxDepth;

    public CallGraphPrinter(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    public void appendActionArguments(List options) {

        options.add(new IntOption(getActionId(), CONF_CTDEPTH,
                "Set the maximum depth of the calltree to print.", "depth"));
        options.add(new StringOption(getActionId(), CONF_CTROOT,
                "Set the root method for the calltree as 'class#method'.", "method"));
        options.add(new StringOption(getActionId(), CONF_CTFORMAT,
                "Set the format for the calltree output as comma-separated list (txt,dot).", "format"));
        options.add(new StringOption(getActionId(), CONF_CTOUTFILE,
                "Output file name without extension, default is stdout for txt and 'outputpath/calltree' for dot.", "outfile"));
        options.add(new StringOption(getActionId(), CONF_CTIGNORE,
                "Comma separated list of package-prefixes to ignore.", "packages"));
        
    }


    public String getActionDescription() {
        return "Print a calltree as text or dot-graph.";
    }

    public boolean doModifyClasses() {
        return false;
    }

    public boolean configure(JopConfig config) throws ConfigurationException {

        outfile = getActionOption(config, CONF_CTOUTFILE);
        if ( outfile == null ) {
            outfile = config.getDefaultOutputPath() + File.separator + "calltree";
            useStdout = true;
        } else {
            useStdout = false;
        }

        String root = getActionOption(config, CONF_CTROOT);
        if ( root != null ) {
            MethodInfo info = MethodInfo.parseFQMethodName(getJoptimizer().getAppStruct(), root);
            if ( info == null ) {
                throw new ConfigurationException("Could not find root method {"+root+"}.");
            }

            rootClass = info.getClassInfo();
            rootMethod = info;

        } else {
            rootClass = getJoptimizer().getAppStruct().getClassInfo(config.getMainClassName());
            if ( rootClass == null ) {
                throw new ConfigurationException("Could not find default root class {"+config.getMainClassName()+"}.");
            }
            rootMethod = rootClass.getMethodInfo(config.getMainMethodSignature());
            if ( rootMethod == null ) {
                throw new ConfigurationException("Could not find main method in root class {"+config.getMainClassName()+"}.");
            }
        }

        format = getActionOption(config, CONF_CTFORMAT, "txt");

        try {
            maxDepth = Integer.parseInt(getActionOption(config, CONF_CTDEPTH, "10"));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Could not parse maxdepth option.",e);
        }

        String ignore = getActionOption(config, CONF_CTIGNORE);
        if ( ignore != null ) {
            this.ignore = ignore.split(",");
        } else {
            this.ignore = new String[0];
        }

        return true;
    }

    public void execute() throws ActionException {

        StructVisitorList vList = new StructVisitorList();

        // init output streams and requested output format visitors
        PrintStream txtOut;
        if ( useStdout ) {
            txtOut = System.out;
        } else {
            try {
                OutputStream outStream = new FileOutputStream(outfile + ".txt");
                txtOut = new PrintStream(outStream);
            } catch (FileNotFoundException e) {
                throw new ActionException("Could not write to outfile: " + e.getMessage(), e);
            }
        }

        if ( format.indexOf("txt") != -1 ) {
            vList.addVisitor(new TxtFilePrinter(txtOut));
        }

        PrintStream dotOut = null;
        if ( format.indexOf("dot") != -1 ) {
            try {
                OutputStream outStream = new FileOutputStream(outfile + ".dot");
                dotOut = new PrintStream(outStream);
            } catch (FileNotFoundException e) {
                throw new ActionException("Could not write to outfile: " + e.getMessage(), e);
            }

            vList.addVisitor(new DotFilePrinter(dotOut));
        }

        // TODO make statistics optional..
        StatisticsVisitor statVisitor = new StatisticsVisitor();
        vList.addVisitor(statVisitor);

        MethodInvocationTraverser traverser = new MethodInvocationTraverser(vList);

        // TODO option to enable recursion check in traverser

        for (int i = 0; i < ignore.length; i++) {
            traverser.addIgnorePrefix(ignore[i]);            
        }
        traverser.setMaxDepth(maxDepth);

        traverser.traverse(rootClass, rootMethod);

        txtOut.println();
        txtOut.println("Max stack size: " + statVisitor.getMaxStackSize());
        txtOut.println("Max locals:     " + statVisitor.getMaxLocalSize());
        txtOut.println("Max code size:  " + statVisitor.getMaxCodeSize());

        if ( dotOut != null ) {
            dotOut.close();
        }
        if ( !useStdout && txtOut != null ) {
            txtOut.close();
        }
    }

}
