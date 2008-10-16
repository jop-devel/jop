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
package com.jopdesign.libgraph.struct;

import com.jopdesign.libgraph.struct.type.TypeHelper;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class TransitiveHullLoader {

    private AppStruct appStruct;

    private List queue;
    private Set visited;
    private List newClasses;

    private boolean postLoadPhase;
    private boolean initNewClasses;

    private static Logger logger = Logger.getLogger(TransitiveHullLoader.class);

    public TransitiveHullLoader(AppStruct appStruct) {
        this.appStruct = appStruct;
        queue = new LinkedList();
        newClasses = new LinkedList();
        visited = new HashSet();
        postLoadPhase = false;
        initNewClasses = false;
    }

    public void reset() {
        queue.clear();
        visited.clear();
        newClasses.clear();
    }

    public boolean isPostLoadPhase() {
        return postLoadPhase;
    }

    public void setPostLoadPhase(boolean postLoadPhase) {
        this.postLoadPhase = postLoadPhase;
    }

    public boolean doInitNewClasses() {
        return initNewClasses;
    }

    public void setInitNewClasses(boolean initNewClasses) {
        this.initNewClasses = initNewClasses;
    }

    public void extendTransitiveHull(Collection rootClasses) throws TypeException {

        for (Iterator it = rootClasses.iterator(); it.hasNext();) {
            ClassInfo classInfo = (ClassInfo) it.next();
            addRootClass(classInfo);
        }

        processQueue();
    }

    public void extendTransitiveHull(ClassInfo classInfo) throws TypeException {

        addRootClass(classInfo);
        
        processQueue();
    }

    public Set getVisitedClassNames() {
        return Collections.unmodifiableSet(visited);
    }

    public Collection getNewClasses() {
        return newClasses;
    }


    private void addRootClass(ClassInfo classInfo) {
        String className = classInfo.getClassName();

        if ( !visited.contains(className) ) {
            queue.add(classInfo);
            visited.add(className);
        }
    }
    
    private void processQueue() throws TypeException {

        if ( logger.isInfoEnabled() ) {
            logger.info("Starting transitive hull search..");
        }

        while ( !queue.isEmpty() ) {
            ClassInfo next = (ClassInfo) queue.remove(0);

            if (logger.isInfoEnabled()) {
                logger.info("Processing class " + next.getClassName());
            }

            visitClass(next);
        }

        if ( logger.isInfoEnabled() ) {
            logger.info("Transitive hull search done; creating classInfos..");
        }

    }

    private void visitClass(ClassInfo classInfo) throws TypeException {

        Set newClasses = classInfo.getReferencedClassNames();
        
        for (Iterator it = newClasses.iterator(); it.hasNext();) {
            String name = (String) it.next();
            addClass(name);
        }
    }

    private void addClass(String className) throws TypeException {

        // get class from array classes
        if ( className.startsWith("[") ) {
            className = TypeHelper.getClassName(className);
            if ( className == null ) {
                return;
            }
        }

        if ( visited.contains(className) ) {
            return;
        } else {
            visited.add(className);
        }

        if ( doEnqueueClass(className) ) {

            ClassInfo newClass = appStruct.tryLoadClass(className, postLoadPhase, initNewClasses);

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

        String reason = appStruct.doIgnoreClassName(className);
        if ( reason != null ) {
            if ( logger.isInfoEnabled() ) {
                logger.info(reason);
            }
            return false;
        }

        return !appStruct.contains(className);
    }

}
