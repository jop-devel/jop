/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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

import java.util.HashSet;
import java.util.Set;


/**
 * Typed options for improved command line interface
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp <stefan@stefant.org>
 *
 * @param <T> java type of the option
 */
public abstract class Option<T> {

    public static final char SHORT_NONE = ' ';

	protected String  key;
    protected char    shortKey = SHORT_NONE;
    protected String  description;
    protected boolean optional;

    protected boolean replaceOptions;

    /**
     * If an option with this flag is set, OptionChecker is not executed (for flags like 'help' or 'version').
     */
    protected boolean skipChecks = false;

    protected Class<T> valClass;
    protected T defaultValue = null;

    @SuppressWarnings("unchecked")
    public Option(String key, String descr, T defaultVal) {
        // Class<T> cast is always safe, shortcoming of Java Generics
        this(key, (Class<T>) defaultVal.getClass(), descr, true);
        this.defaultValue = defaultVal;
        replaceOptions = true;
    }

    protected Option(String key, Class<T> optClass, String descr, boolean optional) {
        this.key = key;
        this.valClass = optClass;
        this.description = descr;
        this.optional = optional;
        replaceOptions = true;
    }

    /**
     * Get the default value of this option, or null if not set.
     * @param options the optiongroup of this option used to get values for keyword replacements.
     * @return the default value or null if none.
     */
    public T getDefaultValue(OptionGroup options) {
        // keyword replacement only used for string option, implemented there.
		return defaultValue;
	}

    /**
     * Get the default value without keyword replacements.
     * @return the original default value, or null if not set.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

	public String getDescription() {
		return description;
	}

	public String getKey() {
		return key;
	}

    public char getShortKey() {
        return shortKey;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean doSkipChecks() {
        return skipChecks;
    }

    /**
     * Set to true to disable correctness checks of arguments, i.e. for 'help' and 'version' options.
     *
     * @param skipChecks set skipCheck flag.
     * @return a reference to this object for chaining.
     */
    public Option<T> setSkipChecks(boolean skipChecks) {
        this.skipChecks = skipChecks;
        return this;
    }

    public Option<T> setShortKey(char shortKey) {
        this.shortKey = shortKey;
        return this;
    }

    public boolean doReplaceOptions() {
        return replaceOptions;
    }

    /**
     * If true, references to other options of the form '<option>' are replaced before parsing.
     *
     * @param replaceOptions replace placeholder or leave untouched.
     * @return a reference to this for chaining.
     */
    public Option<T> setReplaceOptions(boolean replaceOptions) {
        this.replaceOptions = replaceOptions;
        return this;
    }
    
	public T parse(OptionGroup options, String s) throws IllegalArgumentException
    {
        if ( replaceOptions ) {
            HashSet<String> stack = new HashSet<String>();
            stack.add(options.getConfigKey(this));
            return parse( replacePlaceholders(options.getConfig(), s, stack) );
        } else {
            return parse(s);
        }
    }

    protected abstract T parse(String s) throws IllegalArgumentException;

    /**
     * Check if the given argument is a possible argument value or the next option.
     *
     * @param arg the argument to check.
     * @return true if it should be consumed as value, or false if it should be parsed as the next option.
     */
    public boolean isValue(String arg) {
        // if it starts with '-', assume it is an option, else a value
        // this prevents options being consumed as values.
        // specific option types may overwrite this check.
        return !arg.startsWith("-");
    }

    /**
     * Check if this option is enabled (i.e. has been set to a value not equal to null or 'false',
     * depending on the type of the option).
     *
     * @param options a reference to the OptionGroup containing this option.
     * @return true if this option has been set with a non-zero value.
     */
    public boolean isEnabled(OptionGroup options) {
        return options.hasValue(this);
    }

	public String toString() {
		return toString(0, null);
	}

	public String toString(int lAdjust, OptionGroup options) {
		StringBuffer s = new StringBuffer("  ");
        if ( shortKey != SHORT_NONE ) {
            s.append('-');
            s.append(shortKey);
            s.append(",--");
        } else {
            s.append("   --");
        }        
        s.append(key);
        
		for(int i = key.length(); i < lAdjust; i++) {
			s.append(' ');
		}
		s.append("  ");
		s.append(descrString(options));
		return s.toString();
	}

	public String descrString(OptionGroup options) {

        String defaultValue = options.getDefaultValueText(this);

		StringBuffer s = new StringBuffer(this.description);
		s.append(" ");
        s.append(getDefaultsText(defaultValue));
		return s.toString();
	}

    protected String getDefaultsText(String defaultValue) {
        StringBuffer s = new StringBuffer();
        if(defaultValue != null) {
            s.append("[default: ").append(defaultValue).append("]");
        } else {
            s.append(this.optional ? "[optional]" : "[mandatory]");
        }
        return s.toString();
    }

    protected String replacePlaceholders(Config config, String s, Set<String> stack) {
        int p1 = s.indexOf("${");
        if ( p1 == -1 ) {
            return s;
        }

        StringBuffer buf = new StringBuffer();
        int p2 = -1;
        while (p1 > -1) {
            buf.append(s.substring(p2+1, p1));

            p2 = s.indexOf('}',p1);
            if ( p2 == -1 ) {
                // if no closing } found, stop and add rest including '$['
                p2 = p1-1;
                break;
            }

            // replace placeholder with value
            String key = s.substring(p1+2, p2);

            // try to use the option if available to get default value from option.
            Option<?> opt = config.getOptions().getOptionSpec(key);
            String val = config.getValue(key);
            if ( val == null && opt != null ) {
                Object o = opt.getDefaultValue();
                val = o != null ? o.toString() : null;
            }

            if ( val == null || stack.contains(key) ) {
                buf.append("");
            } else {
                stack.add(key);
                buf.append(replacePlaceholders(config, val, stack));
                stack.remove(key);
            }

            p1 = s.indexOf("${", p2);
        }
        buf.append(s.substring(p2+1));

        return buf.toString();
    }
}