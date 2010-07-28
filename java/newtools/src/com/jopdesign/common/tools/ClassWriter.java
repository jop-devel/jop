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

package com.jopdesign.common.tools;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.logger.LogConfig;
import org.apache.bcel.classfile.JavaClass;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Simple tool to write classInfos
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ClassWriter {

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_WRITING+".ClassWriter");

    public ClassWriter() {
    }

    public ClassWriter(OptionGroup options) {
        setup(options);
    }

    /**
     * Add options this Writer supports to the given option-group (excluding an output-path option).
     *
     * @param options the OptionGroup to set options to.
     */
    public static void addOptions(OptionGroup options) {
        // TODO options to write to .jar?, exclude native/.. classes from writing,.. ?
    }

    /**
     * Use options from the given OptionGroup to setup this ClassWriter.
     *
     * @param options the options to use to set the options of this ClassWriter.
     */
    public void setup(OptionGroup options) {
    }

    
    public void write(String writeDir) throws IOException {
        AppInfo appInfo = AppInfo.getSingleton();

        if (logger.isInfoEnabled()) {
            logger.info("Start writing classes to '"+writeDir+"' ..");
        }

        File classDir = new File(writeDir);
        if ( !classDir.mkdir() ) {
            throw new IOException("Could not create output directory "+classDir);
        }

        for (ClassInfo cls : appInfo.getClassInfos() ) {
            if (logger.isDebugEnabled()) {
                logger.debug("Writing class: " + cls.getClassName());
            }
            
            JavaClass jc = cls.compileJavaClass();

            String filename = classDir + File.separator +
                    cls.getClassName().replace(".", File.separator) + ".class";


            File file = new File(filename);
            String parent = file.getParent();

            if(parent != null) {
                File pDir = new File(parent);
                //noinspection ResultOfMethodCallIgnored
                pDir.mkdirs();
            }

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            jc.dump(new DataOutputStream(out));
        }

        if (logger.isInfoEnabled()) {
            logger.info(appInfo.getClassInfos().size() + " classes written.");
        }
    }

}
