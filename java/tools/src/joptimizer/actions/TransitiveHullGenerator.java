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
import com.jopdesign.libgraph.struct.TypeException;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractClassAction;
import joptimizer.framework.actions.ActionException;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Go through all currently loaded classes and load all referenced classes. <br>
 * Already loaded classes are not reloaded. <br>
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class TransitiveHullGenerator extends AbstractClassAction {

    private class ClassFinder {

        private List queue;
        private Set visited;
        private List newClasses;
        private boolean error;

        public ClassFinder(List queue) {
            this.queue = queue;
            newClasses = new LinkedList();
            error = false;

            visited = new HashSet( queue.size() * 3 );
            for (Iterator it = queue.iterator(); it.hasNext();) {
                ClassInfo javaClass = (ClassInfo) it.next();
                visited.add(javaClass.getClassName());
            }
        }

        public boolean hasNext() {
            return !queue.isEmpty();
        }

        public ClassInfo getNext() {
            return (ClassInfo) queue.remove(0);
        }
        
        public boolean hasError() {
            return error;
        }

        public List getNewClasses() {
            return newClasses;
        }

        private void visitClass(ClassInfo classInfo) {
            Set newClasses = classInfo.getReferencedClassNames();
            for (Iterator it = newClasses.iterator(); it.hasNext();) {
                String name = (String) it.next();
                addClass(name);
            }
        }

        private void addClass(String className) {

            if ( visited.contains(className) ) {
                return;
            } else {
                visited.add(className);
            }

            if ( doEnqueueClass(className) ) {
                ClassInfo newClass = null;

                try {
                    newClass = loadClass(className);
                } catch (ActionException e) {
                    logger.error("Could not load class {"+className+"}.",e);
                    error = true;
                }

                if ( newClass != null ) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add class " + newClass.getClassName());
                    }
                    queue.add(newClass);
                    newClasses.add(newClass);
                }
            }
        }

        private boolean doEnqueueClass(String className) {

            // ignore array classes
            if ( className.startsWith("[") ) {
                if (logger.isInfoEnabled()) logger.info("Ingored array class {" + className + "}.");
                return false;
            }

            return !getJoptimizer().getAppStruct().contains(className);
        }

    }

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

        Collection classes = getJoptimizer().getAppStruct().getClassInfos();
        List queue = new LinkedList();

        for (Iterator it = classes.iterator(); it.hasNext();) {
            ClassInfo classInfo = (ClassInfo) it.next();

            if ( classInfo != null ) {
                queue.add(classInfo);
            }
        }

        loadTransitiveHull(queue);
    }

    public void execute(ClassInfo classInfo) throws ActionException {

        List queue = new LinkedList();

        if ( classInfo != null ) {
            queue.add(classInfo);
            loadTransitiveHull(queue);
        }
    }

    public void loadTransitiveHull(List queue) {

        ClassFinder classFinder = new ClassFinder(queue);

        if ( logger.isInfoEnabled() ) {
            logger.info("Starting transitive hull search..");
        }

        while ( classFinder.hasNext() ) {
            ClassInfo next = classFinder.getNext();

            if (logger.isInfoEnabled()) {
                logger.info("Processing class " + next.getClassName());
            }

            classFinder.visitClass(next);
        }

        if ( logger.isInfoEnabled() ) {
            logger.info("Transitive hull search done; creating classInfos..");
        }

        getJoptimizer().addClasses(classFinder.getNewClasses());
    }

    private ClassInfo loadClass(String className) throws ActionException {
        try {
            return getJoptimizer().getAppStruct().createClassInfo(className);
        } catch (TypeException e) {
            if ( getJopConfig().doAllowIncompleteCode() ) {
                logger.warn("Could not load class {"+className+"}, ignored.", e);
            } else {
                throw new ActionException("Could not load class {"+className+"}.", e);
            }
        }
        return null;
    }

}
