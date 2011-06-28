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

package com.jopdesign.common.code;

import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface CallgraphFilter {

    /**
     * @param context the context to get all childs for
     * @return a list of childs to visit (the list must be a modifiable copy and will be consumed), or null to use all childs.
     */
    List<ExecutionContext> getChildren(ExecutionContext context);

    /**
     * @param context the context to get all parents for
     * @return a list of parents to visit (the list must be a modifiable copy and will be consumed), or null to use all parents.
     */
    List<ExecutionContext> getParents(ExecutionContext context);
}
