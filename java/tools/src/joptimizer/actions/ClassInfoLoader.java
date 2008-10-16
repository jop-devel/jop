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
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.TypeException;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractAction;
import joptimizer.framework.actions.ActionException;
import joptimizer.framework.actions.ClassAction;
import joptimizer.framework.actions.MethodAction;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ClassInfoLoader extends AbstractAction implements ClassAction, MethodAction {

    public static final String ACTION_NAME = "loadclassinfo";

    /**
     * set to true if reload should also be done for already initialized classes and methods.
     */
    private boolean forceReload;

    public ClassInfoLoader(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
        forceReload = false;
    }

    public void appendActionArguments(List options) {
    }


    public String getActionDescription() {
        return "Reload all class- and method infos.";
    }

    public boolean doModifyClasses() {
        return false;
    }

    public boolean configure(JopConfig config) {
        return true;
    }

    public boolean doForceReload() {
        return forceReload;
    }

    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }

    public void execute() throws ActionException {
        Iterator it = getJoptimizer().getAppStruct().getClassInfos().iterator();

        // reload all classinfos
        while (it.hasNext()) {
            ClassInfo classInfo = (ClassInfo) it.next();
            execute(classInfo);
        }

        // reload all methodinfos. Must (currently) be done after all classes have been initialized
        // because this needs the methodinfos of super- and sub-classes.
        it = getJoptimizer().getAppStruct().getClassInfos().iterator();
        while (it.hasNext()) {
            ClassInfo classInfo = (ClassInfo) it.next();
            Collection methods = classInfo.getMethodInfos();
            
            for (Iterator it2 = methods.iterator(); it2.hasNext();) {
                MethodInfo method = (MethodInfo) it2.next();

                execute(method);
            }
        }
    }


    public void execute(ClassInfo classInfo) throws ActionException {
        if ( forceReload || !classInfo.isInitialized() ) {
            try {
                classInfo.reload();
            } catch (TypeException e) {
                throw new ActionException("Could not reload class {"+classInfo.getClassName()+"}.", e);
            }
        }
    }

    public void execute(MethodInfo methodInfo) throws ActionException {
        if ( forceReload || !methodInfo.isInitialized() ) {
            try {
                methodInfo.reload();
            } catch (TypeException e) {
                throw new ActionException("Could not reload methodInfo for {"+methodInfo.getFQMethodName()+"}", e);
            }
        }
    }
}
