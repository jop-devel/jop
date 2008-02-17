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

import java.io.PrintStream;
import java.util.Properties;

/**
 * A simple argument option container class.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class ArgOption {

    private String name;
    private String description;
    private String prefix;
    private String optionName;
    private boolean visible;

    protected ArgOption(String prefix, String option, String description) {
        this.prefix = prefix;
        optionName = option;
        this.name = prefix != null ? prefix + "." + option : option;
        this.description = description;
        visible = true;
    }

    public String getFullName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getOptionName() {
        return optionName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Parse the current argument, update properties accordingly.
     *
     * @param option the name by which this option is invoked.
     * @param args an array of all arguments.
     * @param pos the position of the next argument.
     * @param props the properties to be updated from the arguments.
     * @return the number of additionally read arguments.
     */
    public abstract int parse(String option, String[] args, int pos, Properties props) throws ArgumentException;

    /**
     * Print help about this option.
     * @param prefix a prefix to print before the option text.
     * @param out the stream to print the help message to.
     */
    public abstract void printHelp(String prefix, PrintStream out);

    /**
     * Small helper to format a help text with option name and description.
     * @param prefix a text to print before the option name.
     * @param option the option name with format.
     * @param description the description of this option
     * @return a complete intended help text line.
     */
    public static String formatOption(String prefix, String option, String description) {
        StringBuffer out = new StringBuffer();

        // NOTICE support for short options? replace magic values.
        out.append(prefix);
        out.append(option);

        String newline = "\n                       ";
        for ( int i = 0; i < prefix.length(); i++ ) {
            newline += " ";
        }

        if ( option.length() < 22 ) {
            for (int i = option.length(); i < 23; i++ ) {
                out.append(" ");
            }
        } else {
            out.append(newline);
        }

        // wrap description
        String[] lines = description.split("\r?\n");
        for (int i = 0; i < lines.length; i++) {

            if ( i > 0 ) out.append(newline);

            String line = lines[i];

            while ( line.length() > 50 ) {
                int p = line.lastIndexOf(" ",50);
                if ( p > 1 ) {
                    out.append(line.substring(0, p));
                    out.append(newline);
                    line = line.substring(p+1);
                } else {
                    break;
                }
            }

            out.append(line);
        }

        return out.toString();
    }

    /**
     * Parse a string and replace occurences of ${..} with variable values.
     * The value is fetched from system properties, or if not set from the environment variables.
     * If the value is not set, the placeholder is left as it is.
     *
     * @param arg the argument to parse
     * @param config the current configuration (maybe used later to replace properties too).
     * @return the argument with all known variables replaced.
     */
    public static String replaceVariables(String arg, Properties config) {
        int start = arg.indexOf("${");
        while ( start >= 0 ) {
            int end = arg.indexOf("}", start+2);
            if ( end == -1 ) break;

            String var = arg.substring(start + 2, end);
            String value = System.getProperty(var,System.getenv(var));

            if ( value != null ) {
                arg = arg.substring(0, start) + value + arg.substring(end+1);
            } else {
                // var not set, skip this var in the next round
                start = end;
            }

            start = arg.indexOf("${", start);
        }
        return arg;
    }
}
