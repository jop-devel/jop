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
package joptimizer.config;

import com.jopdesign.libgraph.struct.AppConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Main configuration container class.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 * @noinspection InconsistentJavaDoc
 */
public class JopConfig implements AppConfig {

    private Properties config;
    private Set rootClasses;
    private String mainClass;
    private ArchConfig archConfig;
    private String[] libraries;
    private String[] ignore;

    public static final String CONF_OUTPUTPATH = "o";

    public static final String CONF_ASSUME_DYNAMIC_LOADING = "assume-dynamic-loading";

    public static final String CONF_ASSUME_REFLECTION = "assume-reflection";

    public static final String CONF_ALLOW_INCOMPLETE_CODE = "allow-incomplete-code";

    public static final String CONF_ALLOW_LOADDEMAND = "allow-loaddemand";

    public static final String CONF_ARCH = "arch";

    public static final String CONF_ARCH_CONFIG = "arch-config";

    public static final String CONF_LIBRARY_PATH = "libraries";

    public static final String CONF_IGNORE_PATH = "skip";

    public static final String CONF_IGNORE_ACTION_ERRORS = "ignore-errors";

    public JopConfig() {
        initialize(null, null);
    }

    public JopConfig(Properties config) throws ConfigurationException {
        initialize(config, null);
        if ( config != null ) {
            setProperties(config);
        }
    }

    public JopConfig(Properties config, String mainClass) throws ConfigurationException {
        initialize(config, mainClass);
        if ( config != null ) {
            setProperties(config);
        }
    }

    private void initialize(Properties config, String mainClass) {
        this.config = new Properties();
        this.mainClass = mainClass;
        this.rootClasses = new HashSet();
        libraries = new String[0];
        ignore = new String[0];
        archConfig = new ArchConfig();
    }

    /**
     * Add all generic options for this configuration to a list.
     *
     * @param optionList a list where options as ArgOption will be added.
     */
    public static void createOptions(List optionList) {

        optionList.add(new StringOption(null, CONF_OUTPUTPATH,
                "Set default output path for all generated files.", "path"));
        optionList.add(new StringOption(null, CONF_ARCH,
                "Initialize options with default values for an architecture if not set, and load " +
                "internal architecture configfile. Currently supported: jop,jvm", "arch"));
        optionList.add(new StringOption(null, CONF_ARCH_CONFIG,
                "Load an architecture configuration from a config file.", "file"));
        optionList.add(new BoolOption(null, CONF_ASSUME_DYNAMIC_LOADING,
                "Assume that dynamic class loading is used (disables some optimizations)."));
        optionList.add(new BoolOption(null, CONF_ASSUME_REFLECTION,
                "Assume that reflection is used (disables some optimizations)."));
        optionList.add(new BoolOption(null, CONF_ALLOW_INCOMPLETE_CODE,
                "Ignore missing classes. Some features will not work when this option is set."));
        optionList.add( new BoolOption(null, CONF_ALLOW_LOADDEMAND,
                "Allow class loading on demand. Disables automatic transitive hull loading."));
        optionList.add( new StringOption(null, CONF_LIBRARY_PATH,
                "Comma-separated list of packages or classes which are part of libraries and should " +
                "not be loaded.", "pkg"));
        optionList.add( new StringOption(null, CONF_IGNORE_PATH,
                "Comma-separated list of packages or classes which will not be loaded. Ignored if " +
                CONF_ALLOW_INCOMPLETE_CODE + " is not set.", "pkg"));
        optionList.add( new BoolOption(null, CONF_IGNORE_ACTION_ERRORS,
                "Continue with next method, class or action if any action throws an error."));
    }

    public void setProperties(Map config) throws ConfigurationException {
        for (Iterator it = config.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            setOption(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
    }

    public String getMainClassName() {
        return mainClass;
    }

    public void setMainClassName(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * Get a set of all rootclass classnames as collection of strings.
     * @return a set of all rootclass classnames.
     */
    public Set getRootClasses() {
        return rootClasses;
    }

    /**
     * set the rootclass classnames as set of strings.
     * @param rootClasses all root classnames as strings.
     */
    public void setRootClasses(Set rootClasses) {
        this.rootClasses = rootClasses;
    }

    public ArchConfig getArchConfig() {
        return archConfig;
    }

    public String getArchConfigFileName() {
        return getOption(CONF_ARCH_CONFIG);
    }

    public void setArchConfig(URL config) throws ConfigurationException {
        if ( config != null ) {
            this.archConfig = new ArchConfig(config);
            this.config.put(CONF_ARCH_CONFIG, config.toString());
        } else {
            this.archConfig = new ArchConfig();
            this.config.remove(CONF_ARCH_CONFIG);
        }
    }

    public void initArchitecture(String arch) throws ConfigurationException {
        if ( "jop".equals(arch) ) {
            URL config = getClass().getResource("jop-arch.properties");
            if ( config == null ) {
                throw new ConfigurationException("Could not find resource {jop-arch.properties}.");
            }
            setArchConfig(config);

        } else if ( "jvm".equals(arch) ) {

            URL config = getClass().getResource("jvm-arch.properties");
            if ( config == null ) {
                throw new ConfigurationException("Could not find resource {jwm-arch.properties}.");
            }
            setArchConfig(config);

            if ( !isSet(CONF_LIBRARY_PATH) ) {
                setOption(CONF_LIBRARY_PATH, "java,sun");
            }

        } else {
            throw new ConfigurationException("Unknown architecture {"+arch+"}.");
        }
    }

    public String getMainMethodSignature() {
        return "main([Ljava/lang/String;)V";
    }

    /**
     * Check if the given option is set. This also returns true, if the option
     * has the value of FALSE.
     *
     * @see #isEnabled(String)
     * @param option the name of the option.
     * @return true, if the option has been set.
     */
    public boolean isSet(String option) {
        return config.containsKey(option);
    }

    public boolean isEnabled(String option) {
        return Boolean.valueOf(config.getProperty(option)).booleanValue();
    }

    public boolean isEnabled(String name, String id, String option) {
        String on = getOptionName(id, option);
        if ( isSet(on) ) {
            return isEnabled(on);
        }
        return isEnabled(getOptionName(name, option));
    }

    public void setOption(String option, String value) throws ConfigurationException {
        if ( CONF_ARCH_CONFIG.equals(option) ) {
            try {
                setArchConfig(new URL(value));
            } catch (MalformedURLException e) {
                throw new ConfigurationException("Invalid configuration file url {"+value+"}.", e);
            }
        } else if ( CONF_ARCH.equals(option) ) {
            initArchitecture(value);
        } else {
            if ( value != null ) {
                config.setProperty(option, value);
            } else {
                config.remove(option);
            }
        }

        if ( CONF_IGNORE_PATH.equals(option) ) {

            if ( value != null && !"".equals(value) ) {
                ignore = value.split(",");
            } else {
                ignore = new String[0];
            }

        } else if ( CONF_LIBRARY_PATH.equals(option) ) {

            if ( value != null && !"".equals(value) ) {
                libraries = value.split(",");
            } else {
                libraries = new String[0];
            }

        }
    }

    public String getOption(String option) {
        return config.getProperty(option);
    }

    public String getOption(String option, String defaultvalue) {
        return config.getProperty(option, defaultvalue);
    }

    public String getOptionName(String prefix, String option) {
        return prefix + "." + option;
    }

    public String getActionOption(String name, String id, String option) {
        return getActionOption(name, id, option, null);
    }

    public String getActionOption(String name, String id, String option, String defaultvalue) {
        String value = getOption(getOptionName(id, option), null);
        if ( value != null ) {
            return value;
        }
        return getOption(getOptionName(name, option), defaultvalue);
    }

    /**
     * Get a map of all currently set options. <br>
     * Do not modify this map.
     * 
     * @return a map with the full option name as key and its value as string.
     */
    public Map getOptions() {
        return config;
    }

    public String getDefaultOutputPath() {
        return getOption(CONF_OUTPUTPATH, ".");
    }

    public boolean doAssumeDynamicLoading() {
        return isEnabled(CONF_ASSUME_DYNAMIC_LOADING);
    }

    public boolean doAssumeReflection() {
        return isEnabled(CONF_ASSUME_REFLECTION);
    }

    public boolean doLoadOnDemand() {
        return isEnabled(CONF_ALLOW_LOADDEMAND);
    }

    public boolean doIgnoreActionErrors() {
        return isEnabled(CONF_IGNORE_ACTION_ERRORS);
    }

    /**
     * Allow for missing classes. Some features will be disabled
     * if this option is set.
     *
     * @return true if missing classes should be ignored.
     */
    public boolean doAllowIncompleteCode() {
        return isEnabled(CONF_ALLOW_INCOMPLETE_CODE);
    }

    public boolean isNativeClassName(String className) {
        return archConfig.getNativeClassName().equals(className) ||
                "java.lang.Class".equals(className);
    }

    public boolean isLibraryClassName(String className) {
        for (int i = 0; i < libraries.length; i++) {
            if ( className.startsWith(libraries[i]) ) {
                return true;
            }
        }
        return false;
    }

    public boolean doExcludeClassName(String className) {

        for (int i = 0; i < ignore.length; i++) {
            if ( className.startsWith(ignore[i]) ) {
                return true;
            }
        }

        return false;
    }

}
