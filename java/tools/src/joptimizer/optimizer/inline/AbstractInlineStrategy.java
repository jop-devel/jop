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
package joptimizer.optimizer.inline;

import com.jopdesign.libgraph.struct.AppStruct;
import joptimizer.config.JopConfig;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class AbstractInlineStrategy implements InlineStrategy {

    private InlineHelper helper;
    private AppStruct appStruct;
    private JopConfig config;

    protected AbstractInlineStrategy() {
    }

    public void setup(InlineHelper helper, AppStruct appStruct, JopConfig config) {
        this.helper = helper;
        this.appStruct = appStruct;
        this.config = config;
    }

    public InlineHelper getInlineHelper() {
        return helper;
    }

    public AppStruct getAppStruct() {
        return appStruct;
    }

    public JopConfig getJopConfig() {
        return config;
    }
}
