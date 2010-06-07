/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common;

import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.logger.LoggerConfig;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.util.ClassPath;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * This class is a helper used for creating and setting up the AppInfo class as well as for common
 * configuration tasks.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AppSetup {

    private Config config;
    private LoggerConfig loggerConfig;
    private AppInfo appInfo;
    private boolean handleAppInfoInit;
    private String prgmName;
    private String usageDescription;
    private String optionSyntax;
    private String versionInfo;

    public AppSetup(boolean loadSystemProps) {
        this(null, loadSystemProps);
    }

    public AppSetup(Properties defaultProps, boolean loadSystemProps) {

        Properties def;
        if ( loadSystemProps ) {
            def = new Properties(defaultProps);
            def.putAll(System.getProperties());
        } else {
            def = defaultProps;
        }
        config = new Config(def);

        // using default configuration here
        appInfo = new AppInfo(new ClassPath(config.getOption(Config.CLASSPATH)));
        loggerConfig = new LoggerConfig();
    }

    public static Properties loadResourceProps(Class rsClass, String filename) throws IOException {
        return loadResourceProps(rsClass, filename, null);
    }

    public static Properties loadResourceProps(Class rsClass, String filename, Properties defaultProps)
            throws IOException
    {
        Properties p = new Properties(defaultProps);
        InputStream is = new BufferedInputStream(rsClass.getResourceAsStream(filename));
        p.load(is);
        return p;
    }

    public Config getConfig() {
        return config;
    }

    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }


    /**
     * Add some standard options to the config.
     *
     * @param stdOptions if true, add options defined in {@link Config#standardOptions}.
     * @param handleAppInfoInit if true, setupConfig will also handle common setup tasks for AppInfo.
     */
    public void addStandardOptions(boolean stdOptions, boolean handleAppInfoInit) {
        this.handleAppInfoInit = handleAppInfoInit;

        if (stdOptions) {
            config.addOptions(Config.standardOptions);
        }

        if (handleAppInfoInit) {
            config.addOption(Config.CLASSPATH);
            config.addOption(Config.ROOTS);
            config.addOption(Config.NATIVE_CLASSES);
            config.addOption(Config.MAIN_METHOD_NAME);
        }
    }

    public void setUsageInfo(String prgmName, String description) {
        setUsageInfo(prgmName, description, null);
    }

    public void setUsageInfo(String prgmName, String description, String optionSyntax) {
        this.prgmName = prgmName;
        usageDescription = description;
        this.optionSyntax = optionSyntax;
    }

    public void setVersionInfo(String version) {
        versionInfo = version;
    }

    public String[] setupConfig(String[] args) {
        return setupConfig(null, args);
    }

    /**
     * Load the config file, parse and check options, and if handleApInfoInit has been
     * set, also initialize AppInfo.
     *
     * @param configFile filename of an optional user configuration file, will be tried to be loaded before
     *                   arguments are parsed.
     * @param args cmdline arguments to parse
     * @return arguments not consumed.
     */
    public String[] setupConfig(String configFile, String[] args) {

        File file = findConfigFile(configFile);
        if ( file != null && file.exists() ) {
            try {
                InputStream is = new BufferedInputStream(new FileInputStream(file));
                config.addProperties(is);
            } catch (FileNotFoundException e) {
                // should never happen
                System.out.println("Configuration file '"+configFile+"' not found: "+e.getMessage());
            } catch (IOException e) {
                System.out.println("Could not read config file '"+file+"': "+e.getMessage());
                System.exit(3);
            }
        }

        String[] rest = null;
        try {
            rest = config.parseArguments(args);
            config.checkOptions();
        } catch (Config.BadConfigurationException e) {
            System.out.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.out.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        // handle standard options
        if ( Config.SHOW_HELP.isEnabled(config.getOptions()) && prgmName != null ) {
            printUsage();
            System.exit(0);
        }
        if ( Config.SHOW_VERSION.isEnabled(config.getOptions()) && versionInfo != null ) {
            System.out.println(versionInfo);
            System.exit(0);
        }

        return rest;
    }

    public void setupAppInfo(String[] args) {

        // check arguments
        if (args.length == 0) {
            System.out.println("You need to specify a main class or entry method.");
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.out.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        appInfo.setClassPath(new ClassPath(config.getOption(Config.CLASSPATH)));

        String[] natives = Config.splitStringList(config.getOption(Config.NATIVE_CLASSES));
        for (String n : natives) {
            appInfo.excludeNative(n.replaceAll("/","."));
        }

        // add system classes as roots
        String[] roots = Config.splitStringList(config.getOption(Config.ROOTS));
        for (String root : roots) {
            ClassInfo rootInfo = appInfo.loadClass(root.replaceAll("/","."));
            if ( rootInfo == null ) {
                System.out.println("Error loading root class '"+root+"'.");
                System.exit(4);
            }
            appInfo.addRoot(rootInfo);
        }

        // try to find main entry method
        try {
            MethodInfo main = getMainMethod(args[0].replaceAll("/","."));

            appInfo.setMainMethod(main);

        } catch (Config.BadConfigurationException e) {
            System.out.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.out.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        // load other root classes
        for (int i = 1; i < args.length; i++) {
            ClassInfo clsInfo = appInfo.loadClass(args[i].replaceAll("/","."));

            appInfo.addRoot(clsInfo);
        }
    }

    /**
     * Setup the logger. You may want to call {@link #setupConfig(String[])} first to
     * load commandline options.
     *
     * @see LoggerConfig#setupLogger(Config)
     */
    public void setupLogger() {
        loggerConfig.setupLogger(config);
    }

    public void printUsage() {
        String optionDesc;
        if ( optionSyntax != null ) {
            optionDesc = optionSyntax;
        } else {
            optionDesc = "<options>";
            if ( config.getOptions().availableCommands().size() > 0 ) {
                optionDesc += " <cmd> <cmd-options>";
            }
            if ( handleAppInfoInit ) {
                optionDesc += " [--] <main-method> [<additional-roots>]";
            }
        }

        System.out.print("Usage: "+prgmName);
        System.out.println(optionDesc);
        System.out.println();
        if ( usageDescription != null && !"".equals(usageDescription) ) {
            System.out.println(usageDescription);
            System.out.println();
        }

        System.out.println("Available options:");
        for (Option<?> option : config.getOptions().availableOptions() ) {
            System.out.println(option.toString(10));
        }

    }



    private File findConfigFile(String configFile) {
        if ( configFile == null || "".equals(configFile) ) {
            return null;
        }
        // look in different paths? load multiple files?
        return new File(configFile);
    }

    private MethodInfo getMainMethod(String signature) throws Config.BadConfigurationException {
        Signature sMain = new Signature(signature);

        String clsName = sMain.getClassName();
        if ( clsName == null ) {
            throw new Config.BadConfigurationException("You need to specify a classname for the main method.");
        }

        ClassInfo clsInfo = appInfo.loadClass(clsName);
        if ( clsInfo == null ) {
            throw new Config.BadConfigurationException("Class '"+clsName+"' for main method not found.");
        }

        if ( sMain.isMethodSignature() ) {
            MethodInfo method = clsInfo.getMethodInfo(sMain.getMemberSignature());
            if ( method == null ) {
                throw new Config.BadConfigurationException("Method '"+sMain.getMemberSignature()+"' not found in '"
                            +clsName+"'.");
            }
            return method;
        }

        // try to find main method
        String mainName = sMain.getMemberName();
        if ( mainName == null ) {
            mainName = config.getOption(Config.MAIN_METHOD_NAME);
        }
        MethodInfo[] methods = clsInfo.getMethodByName(mainName);

        if ( methods.length == 0 ) {
            throw new Config.BadConfigurationException("Method '"+mainName+"' not found in '"
                        +clsName+"'.");
        }
        if ( methods.length > 1 ) {
            // TODO maybe check if there is a single static method by that name?
            StringBuffer s = new StringBuffer(String.format(
                    "Multiple candidates for '%s' in '%s', please specify a signature: ", mainName, clsName) );
            for (MethodInfo m : methods) {
                s.append("\n");
                s.append(m.getSignature());
            }
            throw new Config.BadConfigurationException(s.toString());
        }

        return methods[0];
    }

}
