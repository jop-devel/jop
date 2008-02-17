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
 * A integer argument option, requires an additional option.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class IntOption extends ArgOption {

    private String argName;

    public IntOption(String prefix, String option, String description, String argName) {
        super(prefix, option, description);
        this.argName = argName;
    }

    public int parse(String option, String[] args, int pos, Properties props) throws ArgumentException {
        if ( pos + 1 >= args.length ) {
            throw new ArgumentException("Missing text for argument '-" + option + "'.");
        }
        String value = replaceVariables(args[pos + 1], props);

        try {
            props.setProperty( getFullName(), Integer.valueOf(value).toString());
        } catch (NumberFormatException e) {
            throw new ArgumentException("Not a valid integer argument: " + value);
        }
        
        return 1;
    }

    public void printHelp(String prefix, PrintStream out) {
        out.println( formatOption(prefix, getFullName() + " #" + argName, getDescription() ) );
    }
}

