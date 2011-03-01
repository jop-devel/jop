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

package com.jopdesign.common.bcel;

import com.jopdesign.common.logger.LogConfig;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.AttributeReader;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class EnclosingMethodReader implements AttributeReader {

    public static final String ATTRIBUTE_NAME = "EnclosingMethod";

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_LOADING+".EnclosingMethodReader");

    public Attribute createAttribute(int name_index, int length, DataInputStream file, ConstantPool constant_pool) {
        if ( length != 4 ) {
            logger.error("Length of EnclosingMethod attribute of "+length+" is not correct.");
            return null;
        }
        try {
            int classIndex = file.readUnsignedShort();
            int methodIndex = file.readUnsignedShort();
            return new EnclosingMethod(name_index, constant_pool, classIndex, methodIndex);
        } catch (IOException e) {
            logger.error("Error reading EnclosingMethod attribute: "+e.getMessage(), e);
            return null;
        }
    }
}
