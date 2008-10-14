/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package joptimizer;

import java.util.*;

import joptimizer.config.ArgumentException;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.ConfigLoader;

/**
 * Try a simple Hello World with libgraph
 * 
 * @author Martin Schoeberl
 *
 */
public class TestLib {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Test librgaph");
		
		JopConfig jopConfig = new JopConfig();

		// List options = ConfigLoader.getDefaultOptions(null);

		// need at least one argument for root class.
		if (args.length == 0) {
			System.out.println("not enough parmeters");
			return;
		}

		List optionList = new LinkedList();
		optionList.add(new StringOption(null, "cp",
                "Set the classpath, default is '.'.", "classpath"));
        ConfigLoader config = new ConfigLoader(optionList);
        Set rootClasses = new HashSet();

        try {
            for (int i = 0; i < args.length; i++) {

                // check if argument is a configuration option
                // TODO handling of quotes and '='
                if ( args[i].startsWith("-") ) {
                    String arg = args[i].substring(1);                
                    i+= config.loadOption(arg, args, i);
                    continue;
                }

                // if no option, assume this as a classname
                String className = args[i].replace("/", ".");
                rootClasses.add(className);
                jopConfig.setMainClassName(className);
            }
        } catch (ArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            System.err.println("Try '--help'.");
            System.exit(2);
        }
        jopConfig.setRootClasses(rootClasses);


	}

}
