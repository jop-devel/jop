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
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.tools.ClassWriter;
import com.jopdesign.common.tools.AppLoader;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.util.ClassPath;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class is a helper used for creating and setting up the AppInfo class as well as for common
 * configuration tasks.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AppSetup {

    /**
     * Helper to load a property file in the package of a given class.
     *
     * @param rsClass the class for which the property file should be loaded.
     * @param filename the filename of the property file.
     * @return the loaded property file.
     * @throws IOException on read errors.
     */
    public static Properties loadResourceProps(Class rsClass, String filename) throws IOException {
        return loadResourceProps(rsClass, filename, null);
    }

    /**
     * Helper to load a property file in the package of a given class.
     *
     * @param rsClass the class for which the property file should be loaded.
     * @param filename the filename of the property file.
     * @param defaultProps default properties to use for the new properties.
     * @return the loaded property file.
     * @throws IOException on read errors.
     */
    public static Properties loadResourceProps(Class rsClass, String filename, Properties defaultProps)
            throws IOException
    {
        Properties p = new Properties(defaultProps);
        InputStream is = rsClass.getResourceAsStream(filename);
        if ( is == null ) {
            throw new IOException("Unable to find resource '"+filename+"' for class '"+rsClass.getCanonicalName()+"'.");
        }
        p.load(new BufferedInputStream(is));
        return p;
    }


    private Config config;
    private LogConfig logConfig;
    private AppInfo appInfo;
    private boolean handleAppInfoInit;
    private String prgmName;
    private String usageDescription;
    private String optionSyntax;
    private String versionInfo;

    private Map<String,Module> modules;

    public AppSetup(boolean loadSystemProps) {
        this(new Properties(), loadSystemProps);
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
        appInfo = AppInfo.getSingleton();

        logConfig = new LogConfig();
        modules = new HashMap<String, Module>();
    }

    public Config getConfig() {
        return config;
    }

    public LogConfig getLoggerConfig() {
        return logConfig;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }


    public void registerModule(String name, Module module) {
        modules.put(name, module);

        // setup defaults and config
        Properties defaults = module.getDefaultProperties();
        if ( defaults != null ) {
            config.addDefaults(defaults);
        }

        // setup options
        module.registerOptions(config.getOptions());

        // register manager
        AttributeManager manager = module.getAttributeManager();
        if ( manager != null ) {
            appInfo.registerManager(name, manager);
        }
    }

    /**
     * Add some standard options to the config.
     *
     * @param stdOptions if true, add options defined in {@link Config#standardOptions}.
     * @param setupAppInfo if true, this will also add common setup options for AppInfo used by {@link #setupAppInfo}
     */
    public void addStandardOptions(boolean stdOptions, boolean setupAppInfo) {
        this.handleAppInfoInit = setupAppInfo;

        if (stdOptions) {
            config.addOptions(Config.standardOptions);
        }

        if (setupAppInfo) {
            config.addOption(Config.CLASSPATH);
            config.addOption(Config.ROOTS);
            config.addOption(Config.NATIVE_CLASSES);
            config.addOption(Config.MAIN_METHOD_NAME);
        }
    }

    /**
     * Add options to classify classes and packages and optionally exclude
     * them from the loader.
     *
     * @param addExcludeOptions if true, add options to exclude classes from loading.
     */
    public void addPackageOptions(boolean addExcludeOptions) {

        config.addOption(Config.LIBRARY_CLASSES);

        if ( addExcludeOptions ) {
            config.addOption(Config.IGNORE_CLASSES);            
            config.addOption(Config.EXCLUDE_LIBRARIES);
            config.addOption(Config.LOAD_NATIVES);
        }
    }

    public void addWritePathOption() {
        config.addOption(Config.WRITE_PATH);
    }

    public void setUsageInfo(String prgmName, String description) {
        setUsageInfo(prgmName, description, null);
    }

    public void setUsageInfo(String prgmName, String description, String optionSyntax) {
        this.prgmName = prgmName;
        usageDescription = description;
        this.optionSyntax = optionSyntax;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String[] setupConfig(String[] args) {
        return setupConfig(args, null);
    }

    /**
     * Load the config file, parse and check options, and if handleApInfoInit has been
     * set, also initialize AppInfo.
     *
     * @param args cmdline arguments to parse
     * @param configFile filename of an optional user configuration file, will be tried to be loaded before
     *                   arguments are parsed.
     * @return arguments not consumed.
     */
    public String[] setupConfig(String[] args, String configFile) {

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
        if ( config.getOption(Config.SHOW_HELP) && prgmName != null ) {
            printUsage();
            System.exit(0);
        }
        if ( config.getOption(Config.SHOW_VERSION) && versionInfo != null ) {
            printVersion();
            System.exit(0);
        }
        if ( config.getOption(Config.SHOW_CONFIG) ) {
            config.printConfiguration(config.getDefaultIndent());
            System.exit(0);
        }

        // let modules process their config options
        try {
            for (Module module : modules.values()) {
                module.onSetupConfig(this);
            }
        } catch (Config.BadConfigurationException e) {
            System.out.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.out.println("Use '--help' to show a usage message.");
            }
            System.exit(2);            
        }

        return rest;
    }

    public void setupAppInfo(String[] args, boolean loadTransitiveHull) {

        // check arguments
        if (args.length == 0 || "".equals(args[0])) {
            System.out.println("You need to specify a main class or entry method.");
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.out.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        appInfo.setClassPath(new ClassPath(config.getOption(Config.CLASSPATH)));

        String[] natives = Config.splitStringList(config.getOption(Config.NATIVE_CLASSES));
        for (String n : natives) {
            appInfo.addNative(n.replaceAll("/","."));
        }

        // handle class loading options if set
        if ( config.hasOption(Config.LIBRARY_CLASSES) ) {
            String[] libs = Config.splitStringList(config.getOption(Config.LIBRARY_CLASSES));
            for (String lib : libs) {
                appInfo.addLibrary(lib.replaceAll("/", "."));
            }
        }

        if ( config.hasOption(Config.IGNORE_CLASSES) ) {
            String[] ignore = Config.splitStringList(config.getOption(Config.IGNORE_CLASSES));
            for (String cls : ignore) {
                appInfo.addLibrary(cls.replaceAll("/", "."));
            }
        }
        if ( config.hasOption(Config.EXCLUDE_LIBRARIES) ) {
            appInfo.setLoadLibraries(!config.getOption(Config.EXCLUDE_LIBRARIES));
        }
        if ( config.hasOption(Config.LOAD_NATIVES) ) {
            appInfo.setLoadNatives(config.getOption(Config.LOAD_NATIVES));
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

        if (loadTransitiveHull) {
            new AppLoader().loadAll();
        }

    }

    /**
     * Setup the logger. You may want to call {@link #setupConfig(String[])} first to
     * load commandline options.
     *
     * @see LogConfig#setupLogger(Config)
     */
    public void setupLogger() {
        logConfig.setupLogger(config);
    }

    public void printUsage() {
        String optionDesc;
        if ( optionSyntax != null ) {
            optionDesc = " " + optionSyntax;
        } else {
            optionDesc = " <options>";
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
            System.out.println(option.toString(config.getDefaultIndent()));
        }

    }

    public void printVersion() {

        if (versionInfo != null && !"".equals(versionInfo)) {
            System.out.println(versionInfo);
        }
        for (String name : modules.keySet() ) {
            versionInfo += name + ": " + modules.get(name).getModuleVersion();
        }
    }

    public void writeClasses() {
        // TODO add+use options to support writing to .jar file?
        try {
            new ClassWriter().writeToDir(config.getOption(Config.WRITE_PATH));
        } catch (IOException e) {
            System.out.println("Failed to write classes: "+e.getMessage());
            System.exit(5);
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
        Signature sMain;
        sMain = Signature.parse(signature);

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
