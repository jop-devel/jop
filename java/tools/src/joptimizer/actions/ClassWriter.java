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
package joptimizer.actions;

import com.jopdesign.libgraph.struct.ClassInfo;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractClassAction;
import joptimizer.framework.actions.ActionException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Action to write classfiles into out directory.
 * Existing classes are replaced without warning, which allows to optimize classes 'in-place'.
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ClassWriter extends AbstractClassAction {

    public static final String ACTION_NAME = "writeclasses";

    public static final String CONF_OUTCLASSPATH = "out";

    private String outdir;

    private static Logger logger = Logger.getLogger(ClassWriter.class);

    public ClassWriter(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    public void appendActionArguments(List options) {

        options.add(new StringOption(getActionId(), CONF_OUTCLASSPATH,
                "Output-directory for generated class files, defaults to common outpath.", "path"));

        // TODO option to write to .jar file.

    }


    public String getActionDescription() {
        return "Write all classes to a directory.";
    }

    public boolean doModifyClasses() {
        return false;
    }

    public boolean configure(JopConfig config) {
        outdir = getActionOption(config, CONF_OUTCLASSPATH);
        if ( outdir == null ) {
            outdir = config.getDefaultOutputPath();
        }
        return true;
    }

    public void execute(ClassInfo classInfo) throws ActionException {

        String filename = outdir + File.separator + classInfo.getClassName().replace(".", File.separator) +
                ".class";

        try {
            if ( logger.isInfoEnabled() ) {
                logger.info("Writing class file {" + filename + "}.");
            }
            classInfo.writeClassFile(filename);
        } catch (IOException e) {
            throw new ActionException("Could not write class file for class {"+
                    classInfo.getClassName()+"}.", e);
        }
    }
}
