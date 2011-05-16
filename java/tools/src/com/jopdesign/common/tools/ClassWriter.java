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

    public static final Logger logger = Logger.getLogger(LogConfig.LOG_WRITING+".ClassWriter");

    public ClassWriter() {
    }


    public void write(String writeDir) throws IOException {
        AppInfo appInfo = AppInfo.getSingleton();

        if (logger.isInfoEnabled()) {
            logger.info("Start writing classes to '"+writeDir+"' ..");
        }

        File classDir = new File(writeDir);
        if ( classDir.exists() ) {
            if ( classDir.isFile() ) {
                throw new IOException("Output directory '"+classDir+"' is a file.");
            }
        } else if ( !classDir.mkdirs() ) {
            throw new IOException("Could not create output directory "+classDir);
        }

        for (ClassInfo cls : appInfo.getClassInfos() ) {
            if (logger.isDebugEnabled()) {
                logger.debug("Writing class: " + cls.getClassName());
            }
            
            JavaClass jc = cls.compile();

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
            DataOutputStream stream = new DataOutputStream(out);
            jc.dump(stream);
            stream.close();
        }

        if (logger.isInfoEnabled()) {
            logger.info(appInfo.getClassInfos().size() + " classes written.");
        }
    }

}
