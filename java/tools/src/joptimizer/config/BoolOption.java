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
 * A boolean argument option, option is set to TRUE when this argument is given.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BoolOption extends ArgOption {

    public BoolOption(String prefix, String option, String description) {
        super(prefix, option, description);
    }

    public int parse(String option, String[] args, int pos, Properties props) {
        props.setProperty( getFullName(), "TRUE");
        return 0;
    }

    public void printHelp(String prefix, PrintStream out) {
        out.println( formatOption(prefix, getFullName(), getDescription() ));
    }
}
