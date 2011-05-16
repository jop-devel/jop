/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2010, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet;

import com.jopdesign.common.config.Config;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.WcetSearch;
import lpsolve.LpSolve;
import lpsolve.VersionInfo;
import org.apache.log4j.Logger;

import java.io.PrintStream;

/**
 * Helper class for command line executables.
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ExecHelper {

    /* Idea adopted from the Java Cookbook; warning: did not override all methods */

    public static class TeePrintStream extends PrintStream {

        private PrintStream p2;

        public TeePrintStream(PrintStream p1, PrintStream p2) {
            super(p1);
            this.p2 = p2;
        }

        @Override
        public void print(String s) {
            for (int i = 0; i < s.length(); i++) {
                super.write(s.charAt(i));
                p2.write(s.charAt(i));
            }
        }

        @Override
        public void println(String s) {
            print(s + "\n");
        }

    }

    private Logger summaryLogger;
    private Config config;

    public ExecHelper(Config config, Logger topLevelLogger) {
        this.config = config;
        this.summaryLogger = topLevelLogger;
    }

    /**
     * Print configuration to summaryLogger
     */
    public void dumpConfig() {
        summaryLogger.info("Configuration:\n" + config.dumpConfiguration(4));
        summaryLogger.info("java.library.path: " + System.getProperty("java.library.path"));
    }

    public void checkLibs() {
        try {
            VersionInfo v = LpSolve.lpSolveVersion();
            info("Using lp_solve for Java, v"+
                    v.getMajorversion()+"."+v.getMinorversion()+
                    " build "+v.getBuild()+" release "+v.getRelease());
        } catch(UnsatisfiedLinkError ule) {
            bail("Failed to load the lp_solve Java library: "+ule);
        }
        if(config.getOption(ProjectConfig.USE_UPPAAL)) {
            String vbinary = config.getOption(UppAalConfig.UPPAAL_VERIFYTA_BINARY);
            try {
                String version = WcetSearch.getVerifytaVersion(vbinary);
                info("Using uppaal/verifyta: "+vbinary+" version "+version);
            } catch(Exception fne) {
                bail("Failed to run uppaal verifier: "+fne);
            }
        }
    }

    public static double timeDiff(long nanoStart, long nanoStop) {
        return (((double) nanoStop - nanoStart) / 1.0E9);
    }

    public Logger getExecLogger() {
        return summaryLogger;
    }

    public void info(String string) {
        summaryLogger.info(string);
    }

    public void logException(String ctx, Throwable e) {
        e.printStackTrace();
        summaryLogger.error("Exception occured when " + ctx + ": " + e);
    }

    public void bail(String msg) {
        printSep();
        System.err.println("[ERROR] " + msg);
        printSep();
        System.exit(1);
    }

    public void bail(Exception e) {
        printSep();
        e.printStackTrace();
        bail(e.getMessage());
    }

    private void printSep() {
        System.err.println("---------------------------------------------------------------");
    }


}
