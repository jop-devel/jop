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

package com.jopdesign.jcopter;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.EmptyTool;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.wcet.WCETTool;

/**
 * User: Stefan Hepp (stefan@stefant.org)
 * Date: 18.05.2010
 */
public class JCopter extends EmptyTool<JCopterManager> {

    public static final String VERSION = "0.1";

    public static final StringOption LIBRARY_CLASSES =
            new StringOption("libraries", "comma-separated list of library classes and packages", "");

    public static final StringOption IGNORE_CLASSES =
            new StringOption("ignore", "comma-separated list of classes and packages to ignore", "");

    public static final BooleanOption ALLOW_INCOMPLETE_APP =
            new BooleanOption("allow-incomplete", "Ignore missing classes", false);


    private final JCopterManager manager;
    private DFATool dfaTool;
    private WCETTool wcetTool;

    public JCopter() {
        super(VERSION);
        manager = new JCopterManager();
    }

    public JCopterManager getEventHandler() {
        return manager;
    }

    public void registerOptions(OptionGroup options) {
        options.addOption( ALLOW_INCOMPLETE_APP );
    }

    @Override
    public void onSetupConfig(AppSetup setup) throws Config.BadConfigurationException {
        Config config = setup.getConfig();
        AppInfo appInfo = AppInfo.getSingleton();

        if ( config.getOption(ALLOW_INCOMPLETE_APP) ) {
            appInfo.setIgnoreMissingClasses(true);
        }

    }

    public DFATool getDfaTool() {
        return dfaTool;
    }

    public void setDfaTool(DFATool dfaTool) {
        this.dfaTool = dfaTool;
    }

    public WCETTool getWcetTool() {
        return wcetTool;
    }

    public void setWcetTool(WCETTool wcetTool) {
        this.wcetTool = wcetTool;
    }

    public boolean useDFA() {
        return dfaTool != null;
    }

    public boolean useWCET() {
        return wcetTool != null;
    }

    public void optimize(Config config) {


    }


    public static void main(String[] args) {

        // setup some defaults
        AppSetup setup = new AppSetup();
        setup.setUsageInfo("jcopter", "A WCET driven Java bytecode optimizer.");
        setup.setVersionInfo(VERSION);
        setup.setConfigFilename("jcopter.properties");

        DFATool dfaTool = new DFATool();
        WCETTool wcetTool = new WCETTool();
        JCopter jcopter = new JCopter();

        setup.registerTool("dfa", dfaTool, true, false);
        setup.registerTool("wcet", wcetTool, true, true);
        setup.registerTool("jcopter", jcopter);

        AppInfo appInfo = setup.initAndLoad(args, true, true, true);

        if (setup.useTool("dfa")) {
            wcetTool.setDfaTool(dfaTool);
            jcopter.setDfaTool(dfaTool);
        }
        if (setup.useTool("wcet")) {
            jcopter.setWcetTool(wcetTool);
        }

        // parse options and config, setup everything, load application classes
        String[] rest = setup.setupConfig(args);

        setup.setupLogger(true);
        setup.setupAppInfo(rest, true);

        // run optimizations
        jcopter.optimize(setup.getConfig());

        // write results
        setup.writeClasses();

    }

}
