/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)
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


/**
 * 
 */
package com.jopdesign.wcet;

import com.jopdesign.build.ReplaceIinc;
import com.jopdesign.build.InsertSynchronized;
import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;

/**
 * Perform the JOPtimizer transformations on the class files
 * and write class fils to the output directory for the WCET
 * analysis
 *
 * TODO moved to WCET package to avoid import clashes, maybe move back to build package?
 *      The main method does not need to be used anymore, since WCETTool calls preprocess() anyway.
 * FIXME [bh] should really be moved to build, and not called WCETPreprocess - it is vital that both JOPizer
 *       and WCA use the same code, and so both should call the same 'PreLinker' transformations
 * @author Martin Schoeberl
 * @author Stefan Hepp
 */
public class WCETPreprocess {

    public WCETPreprocess() {
    }

    public static void preprocess(AppInfo ai) {
        ai.iterate(new ReplaceIinc());
        ai.iterate(new InsertSynchronized());
    }

    public static void main(String[] args) {

        AppSetup setup = new AppSetup();
        AppInfo ai = setup.initAndLoad(args, false, false, true);

        preprocess(ai);

        // dump the methods
//	try {
//		ai.iterate(new Dump(ai, new PrintWriter(new FileOutputStream(ai.outFile+"/dump.txt"))));
//	} catch (FileNotFoundException e) {
//		e.printStackTrace();
//	}

        // write the class files
        setup.writeClasses();
    }

}
