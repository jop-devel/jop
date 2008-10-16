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
import com.jopdesign.libgraph.struct.TransitiveHullLoader;
import com.jopdesign.libgraph.struct.TypeException;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractClassAction;
import joptimizer.framework.actions.ActionException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Go through all currently loaded classes and load all referenced classes. <br>
 * Already loaded classes are not reloaded. <br>
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class TransitiveHullGenerator extends AbstractClassAction {

    public static final String ACTION_NAME = "loadtransitivehull";

    private static final Logger logger = Logger.getLogger(TransitiveHullGenerator.class);

    public TransitiveHullGenerator(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    public void appendActionArguments(List options) {
    }

    public String getActionDescription() {
        return "Go through all classes and load all referenced classes.";
    }

    public boolean doModifyClasses() {
        return false;
    }

    public boolean configure(JopConfig config) {
        return true;
    }

    public void execute() throws ActionException {

        TransitiveHullLoader loader = new TransitiveHullLoader(getJoptimizer().getAppStruct());

        try {
            loader.extendTransitiveHull( getJoptimizer().getAppStruct().getClassInfos() );
        } catch (TypeException e) {
            throw new ActionException("Failed loading transitive hull.", e);
        }

        getJoptimizer().addClasses(loader.getNewClasses());
    }

    public void execute(ClassInfo classInfo) throws ActionException {
        TransitiveHullLoader loader = new TransitiveHullLoader(getJoptimizer().getAppStruct());

        try {
            loader.extendTransitiveHull( classInfo );
        } catch (TypeException e) {
            throw new ActionException("Failed loading transitive hull.", e);
        }

        getJoptimizer().addClasses(loader.getNewClasses());
    }


}
