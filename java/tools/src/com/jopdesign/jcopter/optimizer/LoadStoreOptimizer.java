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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.jcopter.JCopter;
import org.apache.log4j.Logger;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class LoadStoreOptimizer extends AbstractOptimizer {

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".LoadStoreOptimizer");

    public LoadStoreOptimizer(JCopter jcopter) {
        super(jcopter);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void optimizeMethod(MethodInfo method) {
        // TODO eliminate ..,store,load,.. patterns if stored variable is only used in next load
        //      eliminate some store,load,...,load,.. patterns by using ..,dup,.. instead
    }

    @Override
    public void printStatistics() {
    }
}
