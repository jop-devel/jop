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
package com.jopdesign.libgraph.cfg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class Features {

    /**
     * Class for graph features.
     * NOTICE add description, dependencies, 'transform-invariant',..
     */
    public static class Feature {

        private final String name;

        public Feature(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int hashCode() {
            return name.hashCode();
        }
    }

    /**
     * Feature for graphs where the positions of variables in the variable-table corresponds
     * to bytecode-locals without collisions by category-2-variables. 
     */
    public static final Feature FEATURE_VAR_ALLOC = new Feature("var_alloc");

    /**
     * Feature for graphs where blocks have correct stack-depth and -types set for
     * begin and end of their codeblocks.
     */
    public static final Feature FEATURE_STACK_INFO = new Feature("stackinfo");

    /**
     * Feature for graphs in single-static-assignment form.
     */
    public static final Feature FEATURE_SSA = new Feature("ssa");

    private Set features;

    public Features() {
        features = new HashSet();
    }

    public void addFeature(Feature feature) {
        features.add(feature);
    }

    public boolean hasFeature(Feature feature) {
        return features.contains(feature);
    }

    public boolean removeFeature(Feature feature) {
        return features.remove(feature);
    }

    public Set getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    public void clearFeatures() {
        features.clear();
    }

}
