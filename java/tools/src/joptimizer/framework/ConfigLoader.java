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

import joptimizer.config.ArgOption;
import joptimizer.config.ArgumentException;
import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ConfigLoader {

    public static final String CONF_CLASSPATH = "cp";

    private Map options;
    private Properties config;

    private static final Logger logger = Logger.getLogger(ConfigLoader.class);

    /**
     * Create a new configuration loader with an empty configuration.
     * @param optionList a list of {@link joptimizer.config.ArgOption}s which can be loaded.
     */
    public ConfigLoader(Collection optionList) {
        config = new Properties();
        initOptions(optionList);
    }

    private void initOptions(Collection optionList) {

        options = new LinkedHashMap();
        for (Iterator it = optionList.iterator(); it.hasNext();) {
            ArgOption option = (ArgOption) it.next();
            options.put(option.getFullName(), option);
        }
    }

    public static List getDefaultOptions(JOPtimizer joptimizer) {
        List optionList = new LinkedList();

        optionList.add(new StringOption(null, CONF_CLASSPATH,
                "Set the classpath, default is '.'.", "classpath"));

        JopConfig.createOptions(optionList);

        optionList.addAll(joptimizer.getActionFactory().createActionArguments());

        return optionList;
    }

    public Properties getConfig() {
        return config;
    }

    public void storeConfig(JOPtimizer joptimizer) throws ConfigurationException {

        String classPath = config.getProperty(CONF_CLASSPATH, ".");
        joptimizer.getAppStruct().setClassPath(classPath);

        joptimizer.getJopConfig().setProperties(config);        
    }

    public void loadOptionFile(String filename) throws ArgumentException {

        Properties propfile = new Properties();

        try {
            if ( logger.isInfoEnabled() ) {
                logger.info("Reading configuration file {"+filename+"}.");
            }

            // not using class.getResource() here as the config file is usually outside the classpath.
            URL file = new URL(filename);
            InputStreamReader fileStream = new InputStreamReader(file.openStream());
            Reader reader = new BufferedReader(fileStream);
            propfile.load(reader);
            reader.close();

        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error loading configfile {" + filename + "}.", e);
            }
            throw new ArgumentException("Could not load configfile {"+filename+"}: " + e.getMessage());
        }

        // Quick hack to allow usage of environment variables in config.
        for (Iterator it = propfile.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String arg = entry.getKey().toString();

            if (logger.isInfoEnabled() ) {
                logger.info("Found option {"+entry.getKey()+"} with value {"+entry.getValue()+"}");
            }

            loadOption(arg, new String[] {arg, entry.getValue().toString()}, 0);
        }

    }

    public int loadOption(String arg, String[] args, int pos) throws ArgumentException {

        int ret;
        ArgOption option = (ArgOption) options.get(arg);

        if ( option != null ) {

            ret = option.parse(arg, args, pos, config);

        } else {
            throw new ArgumentException("Unrecognized option '" + arg + "'.");
        }
        return ret;
    }

}
