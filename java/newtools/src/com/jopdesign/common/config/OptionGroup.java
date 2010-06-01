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

package com.jopdesign.common.config;

import java.io.*;
import java.util.*;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class OptionGroup {

    public static final String CMD_KEY = "cmd";

    private OptionChecker checker;
    private Config config;
    private String prefix;

    /**
     * List of all options in this group.
     */
    private List<Option<?>> optionList;

    /**
     * Map of options to their respective keys (excluding group prefix).
     */
    private Map<String, Option<?>> optionSet;

    /**
     * Map of commands and their OptionGroups.
     */
    private Map<String, OptionGroup> cmds;

    public OptionGroup(Config config) {
        this(config, null);
    }

    public OptionGroup(Config config, String prefix) {
        this.config = config;
        this.prefix = prefix;
        optionList = new LinkedList<Option<?>>();
        optionSet  = new HashMap<String, Option<?>>();
        cmds = new HashMap<String, OptionGroup>();
    }

    public Config getConfig() {
        return config;
    }

    public OptionChecker getChecker() {
        return checker;
    }

    public void setChecker(OptionChecker checker) {
        this.checker = checker;
    }


    public String getPrefix() {
        return prefix;
    }

    public Set<String> availableCommands() {
        return cmds.keySet();
    }

    public OptionGroup addCommand(String cmd) {
        OptionGroup grp = new OptionGroup(config, cmd);
        cmds.put(cmd, grp);
        return grp;
    }

    public OptionGroup getCommandOptions(String cmd) {
        return cmds.get(cmd);
    }

    public String selectedCommand() {
        return config.getValue(getConfigKey(CMD_KEY));
    }
        
    /*
     * Setup Options
     * ~~~~~~~~~~~~~
     */

	public List<Option<?>> availableOptions() {
		return optionList;
	}

    public void addOption(Option<?> option) {
        optionSet.put(option.getKey(), option);
    }

    public void addOptions(Option<?>[] options) {
        for (Option<?> opt : options) {
            addOption(opt);
        }
    }

    public Option<?> getOptionSpec(String key) {
        return optionSet.get(key);
    }

    public boolean containsOption(Option<?> option) {
        return optionSet.containsKey(option.getKey());
    }

    public boolean containsOption(String key) {
        return optionSet.containsKey(key);
    }

    public String getConfigKey(Option<?> option) {
        return getConfigKey(option.getKey());
    }

    public String getConfigKey(String key) {
        return prefix == null ? key : prefix+'.'+key;
    }

    /*
     * Access Option Values
     * ~~~~~~~~~~~~~~~~~~~~ 
     */

    /**
     * Check if option has been set.
     * @param option the option to check.
     * @return true if option has been explicitly set.
     */
    public boolean isSet(Option<?> option) {
        return config.isSet(getConfigKey(option));
    }

    /**
     * Check if we have any value for this option (either set explicitly or some default value).
     * Does not check if the value can be parsed.
     *
     * @param option the option to check.
     * @return true if there is some value available for this option.
     */
    public boolean hasValue(Option<?> option) {
        String val = config.getValue(getConfigKey(option));
        return val != null || option.getDefaultValue() != null;
    }

    /**
     * Try to get the value of an option, or its default value.
     * If no value and no default value is available, this returns null, even
     * if the option is not optional.
     *
     * Note that after {@link #checkOptions()} has been called, this method
     * should not throw any errors.
     *
     * @param option the option to query.
     * @param <T> Type of the option
     * @return The value, the default value, or null if no value is available.
     * @throws IllegalArgumentException if the config-value cannot be parsed or is not valid.
     */
    public <T> T tryGetOption(Option<T> option) throws IllegalArgumentException {
        String val = config.getValue(getConfigKey(option));
        if ( val == null ) {
            return option.getDefaultValue();
        } else {
            // TODO we could cache the result of this parse call here in a map
            //      but then we need a way to clear the cache if some value changes.
            return option.parse(val);
        }
    }

    public <T> T getOption(Option<T> option, T defaultVal) throws IllegalArgumentException {
        T val = tryGetOption(option);
        return val != null ? val : defaultVal;
    }

    /**
     * Try to get the value of an option, or return null for optional options with no default value.
     * An exception is thrown if no value (and no default value) is given and the option is not optional,
     * or if the config-value cannot be parsed.
     *
     * Note that after {@link #checkOptions()} has been called, this method
     * should not throw any errors.
     *
     * @param option the option to get the value for.
     * @param <T> type of the option.
     * @return the option value or null if
     * @throws Config.BadConfigurationError if the config-value cannot be parsed or if the option is null but required.
     */
    public <T> T getOption(Option<T> option) throws Config.BadConfigurationError {
        T opt;
        try {
            opt = tryGetOption(option);
        } catch (IllegalArgumentException e) {
            throw new Config.BadConfigurationError("Error parsing option '"+getConfigKey(option)+"': "+e.getMessage(), e);
        }
        if(opt == null && ! option.isOptional()) {
            throw new Config.BadConfigurationError("Missing required option: "+getConfigKey(option));
        }
		return opt;
	}

	public <T> void checkPresent(Option<T> option) throws Config.BadConfigurationException {
		if(getOption(option) == null) {
			throw new Config.BadConfigurationException("Missing option: "+getConfigKey(option));
		}
	}

	public <T> void setOption(Option<T> option, T value) {
		config.setProperty(getConfigKey(option), value.toString());
	}

    /*
     * Parse, check and dump options
     * ~~~~~~~~~~~~~~~~~~~~~~
     */

	/**
	 * Consume all command line options and turn them into properties.<br/>
	 *
	 * <p>The arguments are processed as follows: If an argument is of the form
	 * "-option" or "--option", it is considered to be an option.
	 * If an argument is an option, the next argument is considered to be the parameter,
	 * unless the option is boolean and the next argument is missing or an option as well.
	 * We add the pair to our properties, consuming both arguments.
     * </p><p>
     * If an argument starts with @, the rest of it is considered as a property file name,
     * which is then loaded and added to the configuration. 
	 * The first non-option or the argument string {@code --} terminates the option list.
     * </p>
     *
	 * @param args The argument list
	 * @return An array of unconsumed arguments
     * @throws Config.BadConfigurationException if an argument is malformed.
	 */
	public String[] consumeOptions(String[] args) throws Config.BadConfigurationException {
		int i = 0;

		while (i < args.length) {

            if ( cmds.containsKey(args[i]) ) {
                config.setProperty(getConfigKey(CMD_KEY), args[i]);
                OptionGroup cmdGroup = cmds.get(args[i]);

                return cmdGroup.consumeOptions(Arrays.copyOfRange(args, i+1, args.length));
            }

            // handle custom config files
            if ( args[i].startsWith("@") ) {
                String filename = args[i].substring(1);
                try {
                    InputStream is = new BufferedInputStream(new FileInputStream(filename));
                    config.loadConfig(is, prefix);
                } catch (FileNotFoundException e) {
                    throw new Config.BadConfigurationException("Configuration file '"+filename+"' not found!", e);
                } catch (IOException e) {
                    throw new Config.BadConfigurationException("Error reading file '"+filename+"': "+e.getMessage(), e);
                }
                i++;
                continue;
            }

            // break if this is not an option argument, return rest
            if ( !args[i].startsWith("-") ) break;
            if ("-".equals(args[i]) || "--".equals(args[i])) {
                i++;
                break;
            }

			String key;
			if(args[i].charAt(1) == '-') key = args[i].substring(2);
			else key = args[i].substring(1);

            Option<?> spec = getOptionSpec(key);

            if (spec != null) {
				String val = null;
				if (i+1 < args.length) {
					try {
						spec.parse(args[i+1]);
						val = args[i+1];
					} catch(IllegalArgumentException ignored) {
					}
				}
				if (spec instanceof BoolOption && val == null) {
					val = "true";
				} else if (val == null){
					throw new Config.BadConfigurationException("Missing argument for option: "+spec);
				} else {
					i++;
				}
				config.setProperty(getConfigKey(spec), val);

            } else if (spec == null) {

                // maybe a boolean option, check for --no-<key>
                if ( key.startsWith("no-") ) {
                    spec = getOptionSpec(key.substring(3));
                    if ( spec != null && spec instanceof BoolOption ) {
                        config.setProperty(getConfigKey(spec), "false");
                    }
                }
			}
            if ( spec == null ) {
				throw new Config.BadConfigurationException("Not in option set: "+key+" ("+optionSet.keySet().toString()+")");
			}
			i++;
		}
		return Arrays.copyOfRange(args, i, args.length);
	}

    public void checkOptions() throws Config.BadConfigurationException {

        boolean skipCheck = false;

        // first, check if we can parse all options and if we need to call the OptionChecker
        for (Option<?> option : optionList) {

            // check if we can parse
            try {
                tryGetOption(option);
            } catch (IllegalArgumentException e) {
                throw new Config.BadConfigurationException("Error parsing option '"+getConfigKey(option)+"': "+e.getMessage(), e);
            }

            if ( option.doSkipChecks() && option.isEnabled(this) ) {
                skipCheck = true;
            }
        }

        // run the OptionChecker if required
        if (!skipCheck) {

            // check for required options
            for (Option<?> option : optionList) {
                if ( !option.isOptional() && !hasValue(option) ) {
                    throw new Config.BadConfigurationException("Missing required option '"+getConfigKey(option)+'"');
                }
            }

            // run OptionChecker
            if (checker != null) checker.check(this);
        }
    }

    /**
     * Dump configuration of all options for debugging purposes
     * @param indent indent used for keys
     * @return a dump of all options with their respective values.
     */
    public String dumpConfiguration(int indent) {
        StringBuilder sb = new StringBuilder();
        for(Option<?> o : availableOptions()) {
            Object val = tryGetOption(o);
            sb.append(String.format("%"+indent+"s%-20s ==> %s\n", "",o.getKey(),val == null ? "<not set>": val));
        }
        return sb.toString();
    }
}
