/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.jcopter.optimizer;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.JCopterConfig;

import java.util.Collection;
import java.util.TreeMap;

/**
 * This is the root class for simple intraprocedural optimizations, which provides some basic checks
 * if classes and methods should be optimized and iterates over all non-abstract methods.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class AbstractOptimizer implements ClassVisitor {

    private final JCopter jcopter;
    private final boolean iterateSorted;
    private final AppInfo appInfo;

    public AbstractOptimizer(JCopter jcopter) {
        this(jcopter, false);
    }

    public AbstractOptimizer(JCopter jcopter, boolean iterateSorted) {
        this.jcopter = jcopter;
        this.iterateSorted = iterateSorted;
        this.appInfo = AppInfo.getSingleton();
    }

    public JCopter getJCopter() {
        return jcopter;
    }

    public JCopterConfig getJConfig() {
        return jcopter.getJConfig();
    }

    public void optimize() {
        initialize();

        if (appInfo.hasCallGraph()) {
            Collection<MethodInfo> methods = appInfo.getCallGraph().getMethodInfos();
            if (iterateSorted) {
                // little hack to make the DFA cache hack more deterministic
                TreeMap<String, MethodInfo> temp = new TreeMap<String, MethodInfo>();
                for (MethodInfo method : methods) {
                    temp.put(method.getFQMethodName(), method);
                }
                methods = temp.values();
            }
            for (MethodInfo method : methods) {
                if (appInfo.isHwObject(method.getClassInfo())) {
                    // Do not optimize Hardware Objects, leave them alone!
                    continue;
                }
                if (method.hasCode()) {
                    optimizeMethod(method);
                }
            }
        } else {
            if (iterateSorted) {
                TreeMap<String, ClassInfo> temp = new TreeMap<String, ClassInfo>();
                for (ClassInfo cls : appInfo.getClassInfos()) {
                    temp.put(cls.getClassName(), cls);
                }
                for (ClassInfo cls: temp.values()) {
                    visitClass(cls);
                }
            } else {
                appInfo.iterate(this);
            }
        }
        printStatistics();
    }

    @Override
    public final boolean visitClass(ClassInfo classInfo) {
        if (appInfo.isHwObject(classInfo)) {
            // Do not optimize Hardware Objects, leave them alone!
            return false;
        }

        Collection<MethodInfo> methods = classInfo.getMethods();
        if (iterateSorted) {
            // little hack to make the DFA cache hack more deterministic
            TreeMap<String, MethodInfo> temp = new TreeMap<String, MethodInfo>();
            for (MethodInfo method : methods) {
                temp.put(method.getMethodSignature(), method);
            }
            methods = temp.values();
        }
        for (MethodInfo method : methods) {
            if (method.hasCode()) {
                optimizeMethod(method);
            }
        }
        return true;
    }

    @Override
    public final void finishClass(ClassInfo classInfo) {
    }

    public abstract void initialize();

    public abstract void optimizeMethod(MethodInfo method);

    public abstract void printStatistics();
}
