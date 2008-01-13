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
package joptimizer;

import joptimizer.config.ArgOption;
import joptimizer.config.ArgumentException;
import joptimizer.config.CmdLine;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.ActionException;
import org.apache.bcel.util.ClassPath;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A commandline wrapper for the optimizer.
 *
 * TODO extend AppInfo class
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class JOPtimizerRunner {

    public static Logger logger = Logger.getLogger(JOPtimizerRunner.class);

    private static Map options;
    private static String mainClass;

    private static boolean doCmdLine;
    private static boolean doSkipLoad;

    public static final String CONF_LOGCONF_FILE = "logconf";
    public static final String CONF_CLASSPATH = "cp";

    private static void initArguments(JOPtimizer joptimizer) {

        List optionList = new LinkedList();

        optionList.add(new StringOption(CONF_CLASSPATH,
                "Set the classpath, default is '.'.", "classpath"));

        JopConfig.createOptions(optionList);

        optionList.addAll(joptimizer.getActionFactory().createActionArguments());

        // hash options by name
        options = new LinkedHashMap();
        for (int i = 0; i < optionList.size(); i++) {
            ArgOption option = (ArgOption) optionList.get(i);
            options.put(option.getName(), option);
        }

    }

    /**
     * Print out a usage text about this tool.
     * 
     * @param out the stream to print the text to.
     */
    public static void printHelp(PrintStream out) {
        
        out.println("Usage: JOPtimizerRunner [<options>] <class> [<class2> ...] ");
        out.println();
        out.println("  The last given class must contain the 'main()' method.");
        out.println("  To set a different log4j configuration, use ");
        out.println("  'java -Dlog4j.configuration=file://myconf.props'.");
        out.println();
		out.println("  Options are:");
		out.println("    -h,--help               Print this help");
        out.println("    -cmdline                Run in interactive cmdline mode. No default ");
        out.println("                            actions will be executed.");
        out.println("    -skipload               Do not load classes on startup.");
        out.println("    -config <configfile>    A properties-file which may contain any of the ");
        out.println("                            following options.");

        for (Iterator it = options.values().iterator(); it.hasNext();) {
            ArgOption option = (ArgOption) it.next();
            option.printHelp("    -", out);
        }
        
        out.println();
	}

    /**
     * read all arguments into a configuration.
     * The last classname will be set in mainClass.
     *
     * @param args the arguments to read.
     * @param config the configuration which will be updated with the parsed options.
     * @param startClasses a set of classnames which will be filled with the given start class names.
     * @throws ArgumentException
     * @return false, if the program should terminate without executing anything, else true.
     */
    public static boolean parseArguments(String[] args, Properties config, Set startClasses) throws ArgumentException {

        for (int i = 0; i < args.length; i++) {

            // check common options
            if ( "-h".equals(args[i]) || "--help".equals(args[i]) ) {
                printHelp(System.out);
                return false;
            } else if ( "-cmdline".equals(args[i])) {
                doCmdLine = true;
                continue;
            } else if ( "-skipload".equals(args[i])) {
                doSkipLoad = true;
                continue;
            } else if ( "-config".equals(args[i]) ) {
                if ( args.length <= i + 1 ) {
                    throw new ArgumentException("Missing configfile argument for '-config'.");
                }

                Properties propfile = new Properties();
                String filename = args[++i];
                try {
                    if ( logger.isInfoEnabled() ) {
                        logger.info("Reading configuration file {"+filename+"}.");
                    }

                    // not using class.getResource() here as the config file is usually outside the classpath.
                    InputStream reader = new BufferedInputStream(new FileInputStream(filename));
                    propfile.load(reader);

                } catch (IOException e) {
                    logger.error("Could not load configfile {"+filename+"}.", e);
                }

                // Quick hack to allow usage of environment variables in config.
                for (Iterator it = propfile.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String arg = entry.getKey().toString();

                    if (logger.isInfoEnabled() ) {
                        logger.info("Found option {"+entry.getKey()+"} with value {"+entry.getValue()+"}");
                    }

                    loadOption(arg, new String[] {arg, entry.getValue().toString()}, 0, config);
                }

                continue;
            }

            // check if argument is a configuration option
            if ( args[i].startsWith("-") ) {
                String arg = args[i].substring(1);                
                i+= loadOption(arg, args, i, config);
                continue;
            }

            // if no option, assume this as a classname
            String className = args[i].replace("/", ".");
            startClasses.add(className);
            mainClass = className;
        }

        return true;
    }

    private static int loadOption(String arg, String[] args, int pos, Properties config) throws ArgumentException {

        int ret;
        ArgOption option = (ArgOption) options.get(arg);

        if ( option != null ) {

            ret = option.parse(arg, args, pos, config);

        } else {
            throw new ArgumentException("Unrecognized option '" + arg + "'.");
        }
        return ret;
    }

    /**
     * initialize log4j from a configfile.
     * @param configFile
     */
    private static void setupLogger(String configFile) throws ArgumentException {
        URL configUrl = JOPtimizerRunner.class.getResource(configFile);
        if ( configUrl != null ) {
            PropertyConfigurator.configure(configUrl);
        } else {
            throw new ArgumentException("Could not find log4j configuration file {"+configFile+"}.");
        }
    }

    /**
	 * @param args cmdline arguments.
	 */
	public static void main(String[] args) {

        // initialize JOPtimizer
        JopConfig jopConfig = new JopConfig();
        JOPtimizer joptimizer = new JOPtimizer(jopConfig);

        initArguments(joptimizer);

        // need at least one argument for root class.
		if ( args.length == 0 ) {
			printHelp(System.err);
			return;
		}

        Properties config = new Properties();
        Set rootClasses = new HashSet();

        // parse args into properties, set rootclasses
        try {
            if ( !parseArguments(args, config, rootClasses) ) {
                return;
            }
        } catch (ArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            System.err.println("Try '--help'.");
            System.exit(2);
        }

        if ( mainClass == null && !doCmdLine ) {
            System.err.println("Main class not specified. Please provide at least one class name.");
            System.exit(2);
        }

        ClassPath classPath = new ClassPath(config.getProperty(CONF_CLASSPATH, "."));

        // setup config
        rootClasses.addAll(jopConfig.getArchConfig().getSystemClasses());

        jopConfig.setProperties(config);
        jopConfig.setClassPath(classPath);
        jopConfig.setMainClassName(mainClass);
        jopConfig.setRootClasses(rootClasses);

        // generate transitive hull and parse static class infos
        if ( mainClass != null && !doSkipLoad ) {
            try {
                joptimizer.loadTransitiveHull(rootClasses);
                joptimizer.loadClassInfos();
            } catch (IOException e) {
                logger.error("Error initializing classinfos.", e);
                System.exit(1);
            } catch (ActionException e) {
                logger.error("Error loading classinfos.", e);
                System.exit(1);
            }
        }

        if ( doCmdLine ) {
            CmdLine cmdLine = new CmdLine(joptimizer);
            cmdLine.execCmdLine();
        } else {
            try {
                joptimizer.executeActions();
            } catch (ActionException e) {
                logger.error("Could not execute actions.", e);
                System.exit(1);
            }
        }

    }

}
