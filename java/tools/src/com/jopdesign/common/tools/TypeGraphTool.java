/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Wolfgang Puffitsch
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

import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.EmptyTool;
import com.jopdesign.common.EmptyAppEventHandler;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.misc.NamingConflictException;
import com.jopdesign.common.graphutils.TypeGraph;
import org.apache.log4j.Logger;

/**
 * A small tool for examining the type graph
 * 
 * @author Wolfgang Puffitsch
 */
public class TypeGraphTool extends EmptyTool<EmptyAppEventHandler> {

    public static final String VERSION = "0.1";

	public static final BooleanOption MAX_LEVEL =
		new BooleanOption("max-level", "print depth of type graph", false);
	public static final BooleanOption DUMP_LEVELS =
		new BooleanOption("dump-levels", "print levels of individual classes in type graph", false);
	
	public static final Logger logger = Logger.getLogger("TypeGraphTool");

	private Map<ClassInfo, Integer> levels;
	private int maxLevel = 0;

    public TypeGraphTool() {
        super(VERSION);
    }

    @Override
    public void registerOptions(Config config) {
        config.addOption(MAX_LEVEL);
        config.addOption(DUMP_LEVELS);
    }

    @Override
    public void onSetupConfig(AppSetup setup) throws Config.BadConfigurationException {
    }

	public Map<ClassInfo, Integer> getLevels() {
		return levels;
	}

	public int getMaxLevel() {
		return maxLevel;
	}	

    public void run(Config conf) {

		TypeGraph T = new TypeGraph();

		levels = T.getLevels();

		if (conf.getOption(DUMP_LEVELS)) {
			logger.info("LEVELS: " + levels);
		}

		int max = 0;
		Object [] values = T.getLevels().values().toArray();
		for (Object v : values) {
			Integer val = (Integer)v;
			max = val > max ? val : max;
		}
		maxLevel = max;

		if (conf.getOption(MAX_LEVEL)) {
			logger.info("MAX_LEVEL: "+maxLevel);
		}
    }

    public static void main(String[] args) {

        // setup some defaults, initialize without any per-program defaults
        AppSetup setup = new AppSetup();
        setup.setUsageInfo("typegraph", "This is tool to examine the type graph.");
        setup.setVersionInfo("The version of this whole application is 0.1");
        // set the name of the (optional) user-provided config file
        setup.setConfigFilename("typegraph.properties");

        TypeGraphTool typeGraph = new TypeGraphTool();
        setup.registerTool("typegraph", typeGraph);

        AppInfo appInfo = setup.initAndLoad(args, false, true, true);

		typeGraph.run(setup.getConfig());
    }
}
