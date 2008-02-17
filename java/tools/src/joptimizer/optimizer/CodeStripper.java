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
package joptimizer.optimizer;

import com.jopdesign.libgraph.struct.ClassInfo;
import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractAction;
import joptimizer.framework.actions.ActionException;
import joptimizer.framework.actions.ClassAction;

import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CodeStripper extends AbstractAction implements ClassAction {

    public static final String ACTION_NAME = "stripcode";

    public CodeStripper(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }


    public String getActionDescription() {
        return "Remove all unused methods, constants and classes.";
    }

    public void appendActionArguments(List options) {
    }

    public boolean doModifyClasses() {
        return true;
    }

    public boolean configure(JopConfig config) throws ConfigurationException {
        return false;
    }

    public void execute() throws ActionException {
    }

    public void execute(ClassInfo classInfo) throws ActionException {
    }
}
