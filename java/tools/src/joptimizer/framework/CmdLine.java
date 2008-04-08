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
package joptimizer.framework;

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.FieldInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.config.ArgOption;
import joptimizer.config.ArgumentException;
import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.framework.actions.Action;
import joptimizer.framework.actions.ActionCollection;
import joptimizer.framework.actions.ActionException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * a simple commandline interpreter class.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CmdLine {

    private JOPtimizer joptimizer;

    public static Logger logger = Logger.getLogger(CmdLine.class);

    public CmdLine(JOPtimizer joptimizer) {
        this.joptimizer = joptimizer;
    }

    public void execCmdLine() {

        System.out.println();
        System.out.println("JOPtimizer Commandline Interface. Enter 'help' for a list of available commands.");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while ( true ) {

            System.out.print("> ");
            String cmd;

            try {
                String s = br.readLine();
                if ( s == null ) {
                    return;
                }
                cmd = s.trim();
            } catch (IOException e) {
                logger.error("Could not read from System.in.", e);
                return;
            }

            if ( "".equals(cmd) ) {
                continue;
            }

            // TODO support for ".." options with spaces
            
            String[] args = cmd.split("[ \t]+");

            if ( execCmd(args, System.out) ) {
                return;
            }
        }
    }

    public void printHelp(PrintStream out) {
        out.println("  help                  Show this help.");
        out.println("  quit                  Terminate the program.");
        out.println("  load <configfile>     Load all values from a property-file as options.");
        out.println("                        The argument must be a valid url (like file:<path>).");
        out.println("  list <type>           List one of the following:");
        out.println("                         actions          list of all actions.");
        out.println("                         execactions      list all actions executed by runall.");
        out.println("                         config           list all configured values.");
        out.println("                         options          list all generic options.");
        out.println("                         options <action> list all options for action.");
        out.println("                         classes          list all loaded classes.");
        out.println("  info <classname>      Print some infos about the structure of a class.");
        out.println("  get <var>             Get the value of an option.");
        out.println("  set <var> <val>       Set an option to a given value.");
        out.println("  unset <var>           Unset the given option.");
        out.println("  run <action>          Execute an action on all loaded classes.");
        out.println("  run <action> <class> [<method>] ");
        out.println("                        Run an action only on a single class or a single method.");
        out.println("  runall                Run all configured actions and optimizations depending ");
        out.println("                        on the current optimization level.");
        out.println("  classpath <path>      Set classpath to new path.");
        out.println("  mainclass <cls>       Set main classname.");
        out.println("  rootclasses <cls>     Set new list of root classes and reloads systemclasses");
        out.println("                        from the architecture configuration.");
        out.println("  reload                Reload root classes from the current classpath.");
    }

    public void printConfig(PrintStream out) {

        out.print("Classpath: ");
        out.println(joptimizer.getAppStruct().getClassPath());

        out.print("Main class: ");
        out.println(joptimizer.getJopConfig().getMainClassName());
        out.print("Main method: ");
        out.println(joptimizer.getJopConfig().getMainMethodSignature());

        out.println();
        out.println("Root classes: ");
        Set rootClasses = joptimizer.getJopConfig().getRootClasses();
        for (Iterator it = rootClasses.iterator(); it.hasNext();) {
            out.print("  ");
            out.println(it.next());
        }

        out.println();
        out.println("Options:");

        Map options = joptimizer.getJopConfig().getOptions();
        for (Iterator it = options.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();

            out.print("  ");
            out.print(entry.getKey());
            out.print(" = ");
            out.println(entry.getValue());
        }
    }

    /**
     * print a list of available actions.
     * @param out stream to print to.
     */
    public void printActions(PrintStream out) {
        Map names = joptimizer.getActionFactory().getActionNames();
        for (Iterator it = names.entrySet().iterator(); it.hasNext();) {
            Map.Entry action = (Map.Entry) it.next();

            out.println(ArgOption.formatOption("  ", action.getKey().toString(), action.getValue().toString()));
        }
    }

    /**
     * print a list of all options for an action with descriptions.
     * @param action action to show options for.
     * @param out printstream to print to.
     */
    public void printOptions(String action, PrintStream out) {

        List options;

        if ( action == null ) {
            options = new LinkedList();
            
            JopConfig.createOptions(options);

        } else {
            options = joptimizer.getActionFactory().createActionArguments(action);

            if ( options == null ) {
                out.println("Action {" + action + "} unknown!");
                return;
            }
        }

        for (int i = 0; i < options.size(); i++) {
            ArgOption option = (ArgOption) options.get(i);
            option.printHelp(" ", out);
        }
    }

    public void printExecActions(String prefix, Collection actions, PrintStream out) {

        for (Iterator it = actions.iterator(); it.hasNext();) {
            Action action = (Action) it.next();
            out.println( ArgOption.formatOption(prefix, action.getActionName(), action.getActionDescription()) );

            if ( action instanceof ActionCollection ) {
                printExecActions(prefix + "  ", ((ActionCollection)action).getActions(), out);
            }
        }
    }

    private void printClassInfo(String className, PrintStream out) {
        ClassInfo classInfo = joptimizer.getAppStruct().getClassInfo(className.replace('/','.'));
        if ( classInfo == null ) {
            out.println("Class '"+className+"' not loaded or not a valid classname.");
            return;
        }

        // print class definition
        out.print( classInfo.isInterface() ? "interface " : "class " );
        out.print(classInfo.getClassName());
        if ( classInfo.getSuperClassName() != null ) {
            out.print(" extends ");
            out.print(classInfo.getSuperClassName());
        }
        Set interfaces = classInfo.getInterfaces();
        boolean first = true;
        if ( !interfaces.isEmpty() ) {
            out.print(" implements ");
            for (Iterator it = interfaces.iterator(); it.hasNext();) {
                ClassInfo ifc = (ClassInfo) it.next();
                if ( !first ) out.print(",");
                ifc.getClassName();
                first = false;
            }
        }
        out.println(" {");

        if ( !classInfo.getSubClasses().isEmpty() ) {
            out.println();
            if ( classInfo.isInterface() ) {
                out.println("    // Known classes/interfaces implementing this interface:");
            } else {
                out.println("    // Known classes extending this class");
            }
            for (Iterator it = classInfo.getSubClasses().iterator(); it.hasNext();) {
                ClassInfo info = (ClassInfo) it.next();
                if ( !first ) out.print(",");
                out.print("    //   ");
                out.print(info.getClassName());
                if ( info.isInterface() ) {
                    out.print(" (interface)");
                }
                out.println();
            }
        }

        if ( !classInfo.getFieldInfos().isEmpty() ) {
            out.println();
            out.println("    // Fields ");
            for (Iterator it = classInfo.getFieldInfos().iterator(); it.hasNext();) {
                FieldInfo fieldInfo = (FieldInfo) it.next();
                out.print("    ");
                out.print(fieldInfo.getModifierString());
                out.print(fieldInfo.getType().getTypeName());
                out.print(" ");
                out.print(fieldInfo.getName());
                if ( fieldInfo.isConst() ) {
                    out.print(" = ");
                    out.print(fieldInfo.getConstantValue());
                }
                out.println();
            }
        }

        out.println();
        out.println("    // Methods");
        for (Iterator it = classInfo.getMethodInfos().iterator(); it.hasNext();) {
            MethodInfo methodInfo = (MethodInfo) it.next();
            out.print("    ");
            out.print(methodInfo.getModifierString());
            out.print(methodInfo.getName());
            out.print(methodInfo.getSignature());
            out.println();
        }

        out.println("}");
        out.println();
    }

    public void loadConfigFile(String filename, PrintStream out) {

        ConfigLoader configLoader = new ConfigLoader( ConfigLoader.getDefaultOptions(joptimizer) );
        try {
            configLoader.loadOptionFile(filename);
        } catch (ArgumentException e) {
            out.println("Could not load configuration file: " + e.getMessage());
            if (logger.isInfoEnabled()) {
                logger.info("Could not load configuration file {" + filename + "}.", e);
            }
        }

        try {
            configLoader.storeConfig(joptimizer);
        } catch (ConfigurationException e) {
            out.println("Could not load options: " + e.getMessage());
            logger.info("Could not load options.", e);
        }
    }

    public void runAction(Action action, String[] args, int firstArg, PrintStream out) {

        ClassInfo classInfo = null;
        MethodInfo methodInfo = null;

        if ( args.length > firstArg ) {
            classInfo = joptimizer.getAppStruct().getClassInfo(args[firstArg]);
            if ( classInfo == null ) {
                out.println("Class not found: " + args[firstArg]);
                return;
            }
            if ( args.length - 1 > firstArg ) {
                methodInfo = classInfo.getMethodInfo(args[firstArg+1]);
                if ( methodInfo == null ) {
                    out.println("Method not found: " + args[firstArg+1]);
                    return;
                }
            }
        }

        try {
            if ( methodInfo != null ) {
                joptimizer.executeAction(action,methodInfo);
            } else if ( classInfo != null ) {
                joptimizer.executeAction(action,classInfo);
            } else {
                joptimizer.executeAction(action);
            }
        } catch (Exception e) {
            out.print("Error executing action: ");
            out.println(e.getMessage());
            if (logger.isInfoEnabled()) logger.info("Error executing action", e);
        }
    }

    /**
     * exec a command.
     * @param args the command as args[0] and its options
     * @param out the printstream to print the output to.
     * @return true if this is an exit command, else false.
     */
    public boolean execCmd(String[] args, PrintStream out) {

        if ( "quit".equals(args[0]) ) {
            return true;

        } else if ( "help".equals(args[0]) ) {
            printHelp(out);

        } else if ( "list".equals(args[0]) ) {

            if ( args.length < 2 || "config".equals(args[1]) ) {
                printConfig(out);
            } else if ( "actions".equals(args[1]) ) {
                printActions(out);
            } else if ( "options".equals(args[1]) ) {
                printOptions( args.length < 3 ? null : args[2], out );
            } else if ( "execactions".equals(args[1]) ) {
                List actions = joptimizer.getActionFactory().createConfiguredActions();
                printExecActions("  ", actions, out);
            } else if ( "classes".equals(args[1]) ) {
                out.println("# " + joptimizer.getAppStruct().getClassInfos().size() + " classes loaded:");
                for (Iterator it = joptimizer.getAppStruct().getClassInfos().iterator(); it.hasNext();) {
                    ClassInfo classInfo = (ClassInfo) it.next();
                    out.println(classInfo.getClassName());
                }
            } else {
                out.println("Unknown action: "+args[1]);
                return false;
            }
        } else if ( "load".equals(args[0]) ) {
            if ( args.length < 2 ) {
                out.println("Missing filename.");
                return false;
            }
            loadConfigFile(args[1], out);
        } else if ( "info".equals(args[0]) ) {
            if ( args.length < 2 ) {
                out.println("Missing classname.");
                return false;
            }
            for (int i = 1; i < args.length; i++) {
                printClassInfo(args[i], out);
            }

        } else if ( "mainclass".equals(args[0]) ) {
            if ( args.length < 2 ) {
                out.println("Missing mainclass.");
                return false;
            }
            joptimizer.getJopConfig().setMainClassName(args[1]);

        } else if ( "classpath".equals(args[0]) ) {
            if ( args.length < 2 ) {
                out.println("Missing classpath.");
                return false;
            }
            joptimizer.getAppStruct().setClassPath(args[1]);

        } else if ( "rootclasses".equals(args[0]) ) {
            Set rootClasses = new HashSet(args.length + 2);
            JopConfig jopConfig = joptimizer.getJopConfig();

            rootClasses.add(jopConfig.getMainClassName());
            rootClasses.addAll(jopConfig.getArchConfig().getSystemClasses());

            rootClasses.addAll(Arrays.asList(args).subList(1, args.length));

            jopConfig.setRootClasses(rootClasses);

        } else if ( "set".equals(args[0]) ) {
            if ( args.length != 3 ) {
                out.println("Syntax is: set <option> <value>");
                return false;
            }
            try {
                joptimizer.getJopConfig().setOption(args[1], args[2]);
            } catch (ConfigurationException e) {
                out.println("Could not set option: " + e.getMessage());
                logger.info("Could not set option.", e);
            }

        } else if ( "get".equals(args[0]) ) {
            for (int i = 1; i < args.length; i++) {
                String value = joptimizer.getJopConfig().getOption(args[i]);
                if ( value != null ) {
                    out.println(ArgOption.formatOption("  ", args[i], value));
                } else {
                    out.println("Option not set: " + args[i]);
                }
            }

        } else if ( "unset".equals(args[0]) ) {
            if ( args.length != 2 ) {
                out.println("Syntax is: unset <option>");
                return false;
            }
            try {
                joptimizer.getJopConfig().setOption(args[1], null);
            } catch (ConfigurationException e) {
                out.println("Could not unset option: " + e.getMessage());
                logger.info("Could not unset option.", e);
            }

        } else if ( "run".equals(args[0]) ) {
            if ( args.length < 2 ) {
                out.println("Missing action name.");
                return false;
            }
            Action action = joptimizer.getActionFactory().createAction(args[1]);
            if ( action != null ) {
                runAction(action, args, 2, out);
            } else {
                out.println("Unknown action: "+args[1]);
            }
        } else if ( "runall".equals(args[0]) ) {
            try {
                joptimizer.executeActions();
            } catch (ActionException e) {
                out.print("Error running actions: ");
                out.println(e.getMessage());
            }
        } else if ( "reload".equals(args[0]) ) {
            try {
                joptimizer.loadTransitiveHull(joptimizer.getJopConfig().getRootClasses());
                joptimizer.reloadClassInfos();
            } catch (Exception e) {
                out.print("Error loading classinfos: ");
                out.println(e.getMessage());
                logger.info("Error loading classinfos.", e);
            }

        } else {
            out.println("Invalid command: " + args[0]);
        }

        return false;
    }

}
