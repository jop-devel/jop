/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.tools;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.logger.LogConfig;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AppLoader {

    private final List<ClassInfo> queue;
    private final Set<String> visited;
    private final List<ClassInfo> newClasses;
    private boolean followNatives;

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_LOADING + ".AppLoader");

    public AppLoader() {
        queue = new LinkedList<ClassInfo>();
        visited = new LinkedHashSet<String>();
        newClasses = new LinkedList<ClassInfo>();
        followNatives = true;
    }

    public AppLoader(boolean followNatives) {
        queue = new LinkedList<ClassInfo>();
        visited = new LinkedHashSet<String>();
        newClasses = new LinkedList<ClassInfo>();
        this.followNatives = followNatives;
    }

    public boolean doProcessNatives() {
        return followNatives;
    }

    public void setFollowNatives(boolean followNatives) {
        this.followNatives = followNatives;
    }

    public void reset() {
        queue.clear();
        visited.clear();
        newClasses.clear();
    }

    /**
     * Load the complete transitive hull of all classes currently in AppInfo.
     */
    public void loadAll() {
        loadAll(false);
    }
    
    public void loadAll(boolean startFromRootsOnly) {
        AppInfo appInfo = AppInfo.getSingleton();
        if ( startFromRootsOnly ) {
            // we only work on classes, not methods, so starting with root classes is sufficient here
            enqueue( appInfo.getRootClasses() );
        } else {
            enqueue( appInfo.getClassInfos() );
        }

        processQueue();
    }

    public void loadTransitiveHull(Collection<ClassInfo> roots) {
        enqueue(roots);
        processQueue();
    }

    public void loadTransitiveHull(ClassInfo root) {
        enqueue(root);
        processQueue();
    }

    public Collection<ClassInfo> getLoadedClasses() {
        return newClasses;
    }

    private void processQueue() {

        if (logger.isInfoEnabled()) {
            logger.info("Starting transitive hull loader");
        }

        while (!queue.isEmpty()) {
            ClassInfo next = queue.remove(0);

            if (logger.isDebugEnabled()) {
                logger.debug("Processing class: "+next.getClassName());
            }

            int found = 0;
            for (String name : ConstantPoolReferenceFinder.findReferencedClasses(next)) {
                found += processClassName(name);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Found "+found+" new classes in " +next.getClassName());
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("AppLoader loaded " + newClasses.size() + " new classes");
        }
    }

    private int processClassName(String className) {
        AppInfo appInfo = AppInfo.getSingleton();
        int cnt = 0;

        ClassInfo cls;
        if ( appInfo.hasClassInfo(className) ) {
            cls = appInfo.getClassInfo(className);
        } else {
            cls = appInfo.loadClass(className);
            if ( cls != null ) {
                newClasses.add(cls);
                cnt++;
            }
        }

        if ( cls != null ) {
            enqueue(cls);
        }
        return cnt;
    }

    private void enqueue(Collection<ClassInfo> roots) {
        for (ClassInfo cls : roots) {
            enqueue(cls);
        }
    }

    private void enqueue(ClassInfo classInfo) {
        if ( !followNatives && AppInfo.getSingleton().isNative(classInfo.getClassName()) ) {
            return;
        }
        if ( visited.contains(classInfo.getClassName()) ) {
            return;
        }
        queue.add(classInfo);
        visited.add(classInfo.getClassName());
    }    

}
