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

/**
 * This is the root class for simple intraprocedural optimizations, which provides some basic checks
 * if classes and methods should be optimized and iterates over all non-abstract methods.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class AbstractOptimizer implements ClassVisitor {

    private final JCopter jcopter;
    private final AppInfo appInfo;

    public AbstractOptimizer(JCopter jcopter) {
        this.jcopter = jcopter;
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
            for (MethodInfo method : appInfo.getCallGraph().getMethodInfos()) {
                if (appInfo.isHwObject(method.getClassInfo())) {
                    // Do not optimize Hardware Objects, leave them alone!
                    continue;
                }
                if (method.hasCode()) {
                    optimizeMethod(method);
                }
            }
        } else {
            appInfo.iterate(this);
        }
        printStatistics();
    }

    @Override
    public final boolean visitClass(ClassInfo classInfo) {
        if (appInfo.isHwObject(classInfo)) {
            // Do not optimize Hardware Objects, leave them alone!
            return false;
        }
        for (MethodInfo method : classInfo.getMethods()) {
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
