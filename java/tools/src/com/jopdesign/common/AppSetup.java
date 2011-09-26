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

import com.jopdesign.common.bcel.CustomAttribute;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationError;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.ClassInfoNotFoundException;
import com.jopdesign.common.processormodel.AllocationModel;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.common.processormodel.JOPModel;
import com.jopdesign.common.processormodel.JVMModel;
import com.jopdesign.common.processormodel.JamuthModel;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.common.processormodel.ProcessorModel.Model;
import com.jopdesign.common.tools.AppLoader;
import com.jopdesign.common.tools.ClassWriter;
import com.jopdesign.common.tools.SourceLineStorage;
import com.jopdesign.common.type.MemberID;
import org.apache.bcel.util.ClassPath;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
        is.close();
        return p;
    }

    private Config config;
    private LogConfig logConfig;
    private AppInfo appInfo;
    private boolean handleAppInfoInit;
    private boolean loadSystemProps;
    private String programName;
    private String usageDescription;
    private String optionSyntax;
    private String versionInfo;
    private String configFilename;

    private MemberID mainMethodID;

    private Map<String, JopTool> tools;
    private Map<String, BooleanOption> optionalTools;

    /**
     * Initialize a new AppSetup with no default properties.
     * <p>
     * Tools however can add their own default config (see {@link JopTool#getDefaultProperties()}.
     * </p>
     * @see #AppSetup(Properties, boolean)
     */
    public AppSetup() {
        // we do not want to load system props per default!
        // This ensures that the defaults are only defined by the tools and
        // execution on different machines does not differ due to different 'hidden' configurations.
        this(null, false);
    }

    /**
     * Initialize a new AppSetup and set the given (application specific) default properties.
     *
     * @param defaultProps defaults for the application, can overwrite tool defaults.
     * @param loadSystemProps if true, add all JVM system properties to the default properties.  
     */
    public AppSetup(Properties defaultProps, boolean loadSystemProps) {
        this.loadSystemProps = loadSystemProps;

        Properties def = new Properties();
        if ( defaultProps != null ) {
            def.putAll(defaultProps);
        }
        if ( loadSystemProps ) {
            def.putAll(System.getProperties());
        }

        config = new Config(def);

        // using default configuration here
        appInfo = AppInfo.getSingleton();

        logConfig = new LogConfig();
        tools = new TreeMap<String, JopTool>();
        optionalTools = new LinkedHashMap<String,BooleanOption>();
    }

    /**
     * Initialize this AppSetup, and load the classes into AppInfo.
     * <p>
     * Use {@link #registerTool(String, JopTool)} first to add all tools you want to use, as well as
     * {@link #setUsageInfo(String, String)}, {@link #setConfigFilename(String)} and {@link #setVersionInfo(String)}
     * if required.
     * </p>
     * @param args the commandline arguments
     * @param initReports if true, add options and initialize html report loggers.
     * @param allowExcludes if true, add options to allow to exclude packages or classes from loading.
     * @param writeClassOption if true, add options to write classes using {@link #writeClasses()}
     * @return a new AppSetup which has initialized AppInfo.
     */
    public AppInfo initAndLoad(String[] args,
                               boolean initReports, boolean allowExcludes, boolean writeClassOption)
    {
        addStandardOptions(true, true, initReports);
        addPackageOptions(allowExcludes);
        addWriteOptions(writeClassOption);

        String[] remain = setupConfig(args);
        setupLogger(initReports);
        setupAppInfo(remain, true);

        return appInfo;
    }

    public Config getConfig() {
        return config;
    }

    public OptionGroup getDebugGroup() {
        return config.getDebugGroup();
    }

    public LogConfig getLoggerConfig() {
        return logConfig;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    /**
     * Get the main method signature, recognized by parsing commandline and config options.
     * This is available after {@link #setupConfig(String[])}, but does not check if the main
     * class exists.
     *
     * @return the signature of the main method, as set by the options.
     */
    public MemberID getMainMethodID() {
        return mainMethodID;
    }

    /**
     * Register a tool and its AppEventHandler to AppInfo and AppSetup.
     * <p>
     * Event handlers will be called in the same order the tools are registered, so
     * the order of the registrations might be important.
     * </p>
     * @param name the unique name of the tool
     * @param jopTool the tool to register
     */
    public void registerTool(String name, JopTool jopTool) {
        registerTool(name, jopTool, false, false);
    }

    /**
     * Register a tool and its AppEventHandler to AppInfo and AppSetup.
     * <p>
     * Event handlers will be called in the same order the tools are registered, so
     * the order of the registrations might be important.
     * </p>
     * If the tool is registered as optional, an additional option {@code use-<toolname>} will be
     * added, and the tool will only be initialized and used if the option is set.
     *
     * @param name the unique name of the tool
     * @param jopTool the tool to register
     * @param optional make the tool optional and add an option to config.
     * @param useDefault default value for the 'use' option if the tool is optional
     */
    public void registerTool(String name, JopTool jopTool, boolean optional, boolean useDefault) {
        tools.put(name, jopTool);

        if (optional) {
            BooleanOption option = new BooleanOption("use-"+name, "Use the "+name+" tool", useDefault);
            config.addOption(option);
            optionalTools.put(name, option);

            config.setEnableOption(option.getKey());
        } else {
            config.unsetEnableOption();
        }

        // setup defaults and config
        try {
            Properties defaults = jopTool.getDefaultProperties();
            if ( defaults != null ) {
                config.addDefaults(defaults, false);
            }
        } catch (IOException e) {
            System.err.println("Error loading default configuration file: "+e.getMessage());
            System.exit(1);
        }

        // setup options
        jopTool.registerOptions(config);

        config.unsetEnableOption();
    }

    /**
     * Check if a given tool is enabled.
     * @param name the name of the tool used to register it.
     * @return true if the tool is enabled.
     */
    public boolean useTool(String name) {
        if (!tools.containsKey(name)) return false;
        if (!optionalTools.containsKey(name)) return true;
        return config.getOption(optionalTools.get(name));
    }

    /**
     * Add some standard options to the config.
     *
     * @param stdOptions if true, add options defined in {@link Config#standardOptions}.
     * @param setupAppInfo if true, this will also add common setup options for AppInfo used by {@link #setupAppInfo}
     * @param setupReports if true, add options to setup reports
     */
    public void addStandardOptions(boolean stdOptions, boolean setupAppInfo, boolean setupReports) {
        this.handleAppInfoInit = setupAppInfo;

        if (stdOptions) {
            config.addOptions(Config.standardOptions);
        }

        if (setupAppInfo) {
            config.addOption(Config.CLASSPATH);
            config.addOption(Config.ROOTS);
            config.addOption(Config.CALLSTRING_LENGTH);
            config.addOption(Config.MAIN_METHOD_NAME);
            config.addOption(Config.HW_OBJECTS);

            addProcessorModelOptions();

            getDebugGroup().addOptions(Config.debugOptions);
        }

        if (setupReports) {
            config.addOption(Config.REPORTDIR);
            config.addOption(Config.ERROR_LOG_FILE);
            config.addOption(Config.INFO_LOG_FILE);
        }
    }

    private void addProcessorModelOptions() {
        config.addOption(Config.PROCESSOR_MODEL);

        JOPConfig.registerOptions(config);
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
            config.addOption(Config.EXCLUDE_NATIVES);
        }
    }

    /**
     * Add option {@link Config#WRITE_PATH} for the default output path
     * and options for the ClassWriter used in {@link #writeClasses()}.
     *
     * @param writeClasses if true, add options for writing classfiles.
     */
    public void addWriteOptions(boolean writeClasses) {
        config.addOption(Config.WRITE_PATH);
        if ( writeClasses ) {
            config.addOption(Config.WRITE_CLASSPATH);
        }
    }

    /**
     * Add options to load and store instruction source line infos which have a different source file
     * than the class to a file. Loading and writing classes using AppSetup will then load the configured
     * file automatically.
     *
     * @param writeOption add option for writing too, else only a load option is added.
     */
    public void addSourceLineOptions(boolean writeOption) {
        config.addOption(Config.LOAD_SOURCELINES);
        if (writeOption) {
            config.addOption(Config.WRITE_SOURCELINES);
        }
    }

    /**
     * Set some usage infos.
     *
     * @param programName the executable program name
     * @param description a short additional usage description text
     */
    public void setUsageInfo(String programName, String description) {
        setUsageInfo(programName, description, null);
    }

    /**
     * Set some usage infos.
     *
     * @param programName the executable program name
     * @param description a short additional usage description text
     * @param optionSyntax overwrite the generated program options syntax string.
     */
    public void setUsageInfo(String programName, String description, String optionSyntax) {
        this.programName = programName;
        usageDescription = description;
        this.optionSyntax = optionSyntax;
    }

    /**
     * Add some text written out before the tool versions for --version.
     * @param versionInfo some version info text for the application.
     */
    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    /**
     * Get the base filename of the user-provided application configuration file.
     *
     * @return the configuration file without directory prefix.
     */
    public String getConfigFilename() {
        return configFilename;
    }

    /**
     * Set the filename of the user-provided configuration file to load if it exists.
     * <p>
     * There is only one user-configfile for the whole application, not for each tool.
     * </p>
     *
     * @param configFilename the name of the config file for this app without path prefix
     */
    public void setConfigFilename(String configFilename) {
        this.configFilename = configFilename;
    }

    /**
     * Load the config file, parse and check options.
     *
     * @param args cmdline arguments to parse
     * @return arguments not consumed.
     */
    public String[] setupConfig(String[] args) {

        if ( configFilename != null ) {
            File file = findConfigFile(configFilename);
            if ( file != null && file.exists() ) {
                try {
                    InputStream is = new BufferedInputStream(new FileInputStream(file));
                    config.addProperties(is);
                    is.close();
                } catch (FileNotFoundException e) {
                    // should never happen
                    System.out.flush();
                    System.err.println("Configuration file '"+configFilename+"' not found: "+e.getMessage());
                } catch (IOException e) {
                    System.out.flush();
                    System.err.println("Could not read config file '"+file+"': "+e.getMessage());
                    System.exit(3);
                }
            }
        }

        String[] rest = null;
        try {
            rest = config.parseArguments(args);
            // TODO options of non-enabled tools should not be required (but checked if present?)
            config.checkOptions();

            // we parse the main method signature here, so it is available to the tools before
            // AppInfo is initialized
            mainMethodID = getMainSignature(rest.length > 0 ? rest[0] : null);
        } catch (Config.BadConfigurationException e) {
            System.out.flush();
            System.err.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.err.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        // handle standard options
        if ( config.getOption(Config.SHOW_HELP) ) {
            printUsage();
            System.exit(0);
        }
        if ( config.getOption(Config.SHOW_VERSION) ) {
            printVersion();
            System.exit(0);
        }

        // let modules process their config options
        try {
            for (String tool : tools.keySet()) {
                if (useTool(tool)) {
                    tools.get(tool).onSetupConfig(this);
                }
            }
        } catch (Config.BadConfigurationException e) {
            System.out.flush();
            System.err.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.err.println("Use '--help' to show a usage message.");
            }
            System.exit(2);            
        }

        // Dump the config only after we let the tools initialize the config first
        if ( config.getOption(Config.SHOW_CONFIG) ) {
            config.printConfiguration(config.getDefaultIndent());
            System.exit(0);
        }

        return rest;
    }

    /**
     * Setup AppInfo using the config previously initialized with {@link #setupConfig(String[])}.
     * 
     * @param args the arguments containing the name of the main method and additional roots without config options.
     * @param loadTransitiveHull if true, load the transitive hull of the root classes too.
     */
    public void setupAppInfo(String[] args, boolean loadTransitiveHull) {

        CustomAttribute.registerDefaultReader();

        appInfo.setClassPath(new ClassPath(config.getOption(Config.CLASSPATH)));
        appInfo.setExitOnMissingClass(!config.getOption(Config.VERBOSE));

        // handle class loading options if set
        if ( config.hasOption(Config.LIBRARY_CLASSES) ) {
            List<String> libs = Config.splitStringList(config.getOption(Config.LIBRARY_CLASSES));
            for (String lib : libs) {
                appInfo.addLibrary(lib.replaceAll("/", "."));
            }
        }

        if ( config.hasOption(Config.IGNORE_CLASSES) ) {
            List<String> ignore = Config.splitStringList(config.getOption(Config.IGNORE_CLASSES));
            for (String cls : ignore) {
                appInfo.addLibrary(cls.replaceAll("/", "."));
            }
        }
        if ( config.hasOption(Config.EXCLUDE_LIBRARIES) ) {
            appInfo.setLoadLibraries(!config.getOption(Config.EXCLUDE_LIBRARIES));
        }
        if ( config.hasOption(Config.EXCLUDE_NATIVES) ) {
            appInfo.setLoadNatives(!config.getOption(Config.EXCLUDE_NATIVES));
        }        

        appInfo.setCallstringLength(config.getOption(Config.CALLSTRING_LENGTH).intValue());

        for (String hwObject : Config.splitStringList(config.getOption(Config.HW_OBJECTS))) {
            appInfo.addHwObjectName(hwObject);
        }

        if (getDebugGroup().isSet(Config.DUMP_CACHEKEY)) {
            appInfo.setDumpCacheKeyFile(getDebugGroup().getOption(Config.DUMP_CACHEKEY));
        }

        // register handler
        for (String toolName : tools.keySet()) {
            if (useTool(toolName)) {
                AppEventHandler handler = tools.get(toolName).getEventHandler();
                if ( handler != null ) {
                    appInfo.registerEventHandler(handler);
                }
            }
        }

        if ( config.hasOption(Config.PROCESSOR_MODEL) ) {
            initProcessorModel(config.getOption(Config.PROCESSOR_MODEL));
        }

        // add system classes as roots
        List<String> roots = Config.splitStringList(config.getOption(Config.ROOTS));
        for (String root : roots) {
            ClassInfo rootInfo = appInfo.loadClass(root.replaceAll("/","."));
            if ( rootInfo == null ) {
                System.out.flush();
                System.err.println("Error loading root class '"+root+"'.");
                System.exit(4);
            }
            appInfo.addRoot(rootInfo);
        }
        // check arguments
        String mainClassName = null;        
        if (args.length > 0 && ! "".equals(args[0])) {
        	mainClassName = args[0];
        } else if(config.hasOption(Config.MAIN_METHOD_NAME)){
        	mainClassName = MemberID.parse(config.getOption(Config.MAIN_METHOD_NAME)).getClassName();
        } else {
            System.out.flush();
            System.err.println("You need to specify a main class or entry method.");
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.err.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        // try to find main entry method
        try {
            MethodInfo main = getMainMethod(mainClassName.replaceAll("/","."));

            appInfo.setMainMethod(main);

        } catch (Config.BadConfigurationException e) {
            System.out.flush();
            System.err.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.err.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        // load other root classes
        for (int i = 1; i < args.length; i++) {
            ClassInfo clsInfo = appInfo.loadClass(args[i].replaceAll("/","."));
            appInfo.addRoot(clsInfo);
        }

        // notify the tools about the root classes
        try {
            for (String tool : tools.keySet()) {
                if (useTool(tool)) {
                    tools.get(tool).onSetupRoots(this, appInfo);
                }
            }
        } catch (Config.BadConfigurationException e) {
            System.out.flush();
            System.err.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.err.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }

        // load and initialize all app classes
        if (loadTransitiveHull) {
            loadClassInfos();

        // register source line loader before other event handlers
        if ( config.hasOption(Config.LOAD_SOURCELINES) ) {
            String filename = config.getOption(Config.LOAD_SOURCELINES);
            if (filename != null && !"".equals(filename.trim())) {
                File storage = new File(filename);
                if (storage.exists()) {
                    new SourceLineStorage(storage).loadSourceInfos();
                }
            }
        }


        }

        // let modules process their config options
        try {
            for (String tool : tools.keySet()) {
                if (useTool(tool)) {
                    tools.get(tool).onSetupAppInfo(this, appInfo);
                }
            }
        } catch (Config.BadConfigurationException e) {
            System.out.flush();
            System.err.println(e.getMessage());
            if ( config.getOptions().containsOption(Config.SHOW_HELP) ) {
                System.err.println("Use '--help' to show a usage message.");
            }
            System.exit(2);
        }
    }

    /**
     * Setup the logger. You may want to call {@link #setupConfig(String[])} first to
     * load commandline options.
     *
     * @see LogConfig#setupLogger(Config)
     * @param addReportLoggers if true, add html-report loggers writing to {@link Config#WRITE_PATH}.
     */
    public void setupLogger(boolean addReportLoggers) {
        logConfig.setupLogger(config);

        if ( addReportLoggers ) {
            String outDir = config.getOption(Config.REPORTDIR) + File.separator;
            try {
                String errorFile = outDir + config.getOption(Config.ERROR_LOG_FILE);
                String infoFile  = outDir + config.getOption(Config.INFO_LOG_FILE);
                logConfig.setReportLoggers(new File(errorFile), new File(infoFile));
            } catch (IOException e) {
                System.out.flush();
                System.err.println("Error creating log files: "+e.getMessage());
                System.exit(4);
            }
        }
    }

    public void printUsage() {
        String optionDesc;
        OptionGroup options = config.getOptions();

        if ( optionSyntax != null ) {
            optionDesc = " " + optionSyntax;
        } else {
            optionDesc = " [@<propertyfile>] <options>";
            if ( options.hasCommands() ) {
                optionDesc += " <cmd> <cmd-options>";
            }
            if ( handleAppInfoInit ) {
                optionDesc += " [--] [<main-method> [<additional-roots>]]";
            }
        }

        System.out.print("Usage: "+ (programName != null ? programName : ""));
        System.out.println(optionDesc);
        System.out.println();
        if ( usageDescription != null && !"".equals(usageDescription) ) {
            System.out.println(usageDescription);
            System.out.println();
        }

        System.out.println("Available options:");
        for (Option<?> option : options.availableOptions() ) {
            System.out.println(option.toString(config.getDefaultIndent(), options));
        }

        System.out.println();

        // TODO this does not handle sub-subgroups
        for (String name : options.availableSubgroups()) {
            System.out.println("Options in group " + name + ":");
            OptionGroup group = options.getGroup(name);

            for (Option<?> option : group.availableOptions()) {
                System.out.println(option.toString(config.getDefaultIndent(), group));
            }
            System.out.println();
        }

        System.out.println("The @<filename> syntax can be used multiple times. Entries in the property-file");
        System.out.println("overwrite previous options and can be overwritten by successive options.");
        System.out.println("Every property-file can contain additional log4j configuration options.");
        System.out.println();
        if (config.hasOption(Config.MAIN_METHOD_NAME)) {
            System.out.println("If '--"+Config.MAIN_METHOD_NAME.getKey()+
                    "' specifies a fully-qualified method name, <main-method> is optional.");
        }
        if ( loadSystemProps && configFilename != null ) {
            System.out.println("Config values can be set in the JVM system properties and in '" + configFilename + "'");
            System.out.println("in the working directory.");
        } else if ( configFilename != null ) {
            System.out.println("Config values can be set in '" + configFilename + "' in the working directory.");
        } else if ( loadSystemProps ) {
            System.out.println("Config values can be set in the JVM system properties.");
        }
        System.out.println();
    }

    public void printVersion() {

        if (versionInfo != null && !"".equals(versionInfo)) {
            System.out.println(versionInfo);
        }
        for (String name : tools.keySet() ) {
            System.out.println(name + ": " + tools.get(name).getToolVersion());
        }
    }

    /**
     * Write the AppInfo classes to the directory specified by the {@link Config#WRITE_CLASSPATH} option.
     */
    public void writeClasses() {
        writeClasses(Config.WRITE_CLASSPATH);

        // writing the source line infos *after* all other classes have been written so that timestamp checks works
        if (config.hasOption(Config.WRITE_SOURCELINES)) {
            String filename = config.getOption(Config.WRITE_SOURCELINES);
            if (filename != null && !"".equals(filename.trim())) {
                File storage = new File(filename);
                new SourceLineStorage(storage).storeSourceInfos();
            }
        }
    }

    /**
     * Write the AppInfo classes to the directory specified by the outDir option.
     *
     * @param outDir the option for the classfiles output directory.
     */
    public void writeClasses(Option<String> outDir) {
        try {
            ClassWriter writer = new ClassWriter();
            writer.write(config.getOption(outDir));
        } catch (IOException e) {
            ClassWriter.logger.error("Failed to write classes: "+e.getMessage(), e);
            System.exit(5);
        }
    }


    private void loadClassInfos() {
        // We could use UsedCodeFinder here to load only reachable code, once it supports loading classes on the fly
        new AppLoader().loadAll(false);
        appInfo.reloadClassHierarchy();
    }

    private void initProcessorModel(Model model) {
        ProcessorModel pm;
        switch (model) {
            case JOP:
                pm = new JOPModel(config);
                break;
            case jamuth:
                pm = new JamuthModel(config);
                break;
            case allocation:
                pm = new AllocationModel(config);
                break;
            case JVM:
                pm = new JVMModel();
                break;
            default:
                throw new BadConfigurationError("Unknown processor model " + model);
        }

        appInfo.setProcessorModel(pm);

        // load referenced classes as roots
        for (String jvmClass : pm.getJVMClasses()) {
            ClassInfo rootInfo = appInfo.loadClass(jvmClass.replaceAll("/","."));
            if ( rootInfo == null ) {
                System.err.println("Error loading JVM class '"+jvmClass+"'.");
                System.exit(4);
            }
        }
        if (appInfo.doLoadNatives()) {
            for (String nativeClass : pm.getNativeClasses()) {
                ClassInfo rootInfo = appInfo.loadClass(nativeClass.replaceAll("/","."));
                if ( rootInfo == null ) {
                    System.err.println("Error loading Native class '"+nativeClass+"'.");
                    System.exit(4);
                }
            }
        }

        // we do not set the JVM and native classes as root anymore, instead we let the PM decide which roots we need
        for (String root : pm.getJVMRoots()) {
            MemberID mID = MemberID.parse(root);
            // make sure the class exists..
            ClassInfo cls = appInfo.loadClass(mID.getClassName());
            // Get the member and add it as root
            if (mID.hasMemberName()) {
                MethodInfo methodInfo = cls.getMethodInfo(mID);
                if (methodInfo == null) {
                    System.err.println("Could not find JVM root "+root);
                    System.exit(5);
                }
                appInfo.addRoot(methodInfo);
            } else {
                appInfo.addRoot(cls);
            }
        }

    }

    private File findConfigFile(String configFile) {
        if ( configFile == null || "".equals(configFile) ) {
            return null;
        }
        // look in different paths? load multiple files?
        return new File(configFile);
    }

    private MemberID getMainSignature(String signature) throws BadConfigurationException {
        MemberID sMain;

        ClassPath path = new ClassPath(config.getOption(Config.CLASSPATH));

        MemberID sMainMethod = MemberID.parse(config.getOption(Config.MAIN_METHOD_NAME), path);

        if (signature == null || "".equals(signature)) {
            sMain = sMainMethod;
        } else {
            // try to parse the signature
            sMain = MemberID.parse(signature, path);

            // use --mm if only main class has been given
            if (!sMain.hasMemberName()) {
                if (!sMainMethod.hasMemberName()) {
                    throw new BadConfigurationException("Option '"+Config.MAIN_METHOD_NAME.getKey()
                            +"' needs to specify a method name.");
                }

                sMain = new MemberID(sMain.getClassName(), sMainMethod.getMemberName(),
                                                            sMainMethod.getDescriptor());
            }
        }

        return sMain;
    }

    private MethodInfo getMainMethod(String signature) throws Config.BadConfigurationException {

        ClassInfo clsInfo;
        String clsName;

        MemberID sMain = getMainSignature(signature);

        clsName = sMain.getClassName();
        if ( clsName == null ) {
            throw new BadConfigurationException("You need to specify a classname for the main method.");
        }

        try {
            clsInfo = appInfo.loadClass(clsName, true, false);
        } catch (ClassInfoNotFoundException e) {
            throw new BadConfigurationException("Class for '"+signature+"' could not be loaded: "
                    + e.getMessage(), e);
        }

        // check if we have a full signature
        if (sMain.hasMethodSignature()) {
            MethodInfo method = clsInfo.getMethodInfo(sMain.getMethodSignature());
            if (method == null) {
                throw new BadConfigurationException("Method '"+sMain.getMethodSignature()+"' not found in '"
                            +clsName+"'.");
            }
            return method;
        }

        // try to find main method
        String mainName = sMain.getMemberName();
        if ( mainName == null ) {
            mainName = config.getOption(Config.MAIN_METHOD_NAME);
            if(mainName != null) {
            	mainName = MemberID.parse(mainName).getMethodSignature();
            }
        }

        Collection<MethodInfo> methods = clsInfo.getMethodByName(mainName);

        if ( methods.isEmpty() ) {
            throw new BadConfigurationException("'No method '"+mainName+"' found in '"+clsName+"'.");
        }
        if ( methods.size() > 1 ) {
            StringBuilder s = new StringBuilder(String.format(
                    "Multiple candidates for '%s' in '%s', please specify with a signature: ", mainName, clsName));
            for (MethodInfo m : methods) {
                s.append("\n");
                s.append(m.getMemberID());
            }
            throw new BadConfigurationException(s.toString());
        }

        return methods.iterator().next();
    }

}
