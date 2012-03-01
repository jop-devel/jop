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

package com.jopdesign.common.tools;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.graphutils.EmptyClassVisitor;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class UpdatePositions extends EmptyClassVisitor {
    @Override
    public boolean visitClass(ClassInfo classInfo) {
        for (MethodInfo method : classInfo.getMethods()) {
            if (!method.hasCode()) continue;

            method.getCode().getInstructionList(true,false).setPositions();
        }
        return true;
    }
}
