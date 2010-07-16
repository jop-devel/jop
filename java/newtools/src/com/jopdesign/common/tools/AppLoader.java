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
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantInfo;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

    private static final Logger logger = Logger.getLogger("common.tools.AppLoader");

    public AppLoader() {
        queue = new LinkedList<ClassInfo>();
        visited = new HashSet<String>();
        newClasses = new LinkedList<ClassInfo>();
    }

    public void loadApp() {
        loadApp(false);
    }
    
    public void loadApp(boolean startFromRootsOnly) {
        AppInfo appInfo = AppInfo.getSingleton();
        if ( startFromRootsOnly ) {
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
        return Collections.unmodifiableCollection(newClasses);
    }

    private void processQueue() {

        newClasses.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("Starting transitive hull loader");
        }

        while (!queue.isEmpty()) {
            ClassInfo next = queue.remove(0);

            if (logger.isTraceEnabled()) {
                logger.trace("Processing class: "+next.getClassName());
            }

            int found = processClass(next);

            if (logger.isDebugEnabled()) {
                logger.debug("Found "+found+" new classes in " +next.getClassName());
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("AppLoader loaded " + newClasses.size() + " new classes");
        }
    }

    private int processClass(ClassInfo classInfo) {
        AppInfo appInfo = AppInfo.getSingleton();
        int cnt = 0;
                
        // process constantpool/fields+methods for class references, load and enqueue them
        int size = classInfo.getConstantPoolSize();
        for (int i = 0; i < size; i++) {
            ClassRef ref = classInfo.getConstantInfo(i).getClassRef();
            if ( ref == null ) {
                continue;
            }

            ClassInfo cls = ref.getClassInfo();
            if ( cls == null ) {
                cls = appInfo.loadClass(ref.getClassName());
                newClasses.add(cls);
                cnt++;
            }
            
            if ( cls != null ) {
                enqueue(cls);
            }
        }

        return cnt;
    }

    private void enqueue(Collection<ClassInfo> roots) {
        for (ClassInfo cls : roots) {
            enqueue(cls);
        }
    }

    private void enqueue(ClassInfo classInfo) {
        if ( !visited.contains(classInfo.getClassName()) ) {
            queue.add(classInfo);
            visited.add(classInfo.getClassName());
        }
    }

}
