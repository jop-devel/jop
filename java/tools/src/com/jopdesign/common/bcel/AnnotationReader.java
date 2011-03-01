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
 * @author Peter Hilber (peter@hilber.name)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnnotationReader implements AttributeReader {

    public static final String VISIBLE_ANNOTATION_NAME = "RuntimeVisibleAnnotations";
    public static final String INVISIBLE_ANNOTATION_NAME = "RuntimeInvisibleAnnotations";
    public static final String VISIBLE_PARAMETER_ANNOTATION_NAME = "RuntimeVisibleParameterAnnotations";
    public static final String INVISIBLE_PARAMETER_ANNOTATION_NAME = "RuntimeInvisibleParameterAnnotations";
    public static final String ANNOTATION_DEFAULT_NAME = "AnnotationDefault";

    public static final String[] ATTRIBUTE_NAMES =
            {VISIBLE_ANNOTATION_NAME, INVISIBLE_ANNOTATION_NAME,
             VISIBLE_PARAMETER_ANNOTATION_NAME, INVISIBLE_PARAMETER_ANNOTATION_NAME,
             ANNOTATION_DEFAULT_NAME};

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_LOADING+".AnnotationReader");

    private String attributeName;

    public AnnotationReader(String attributeName) {
        this.attributeName = attributeName;
    }

    public boolean isVisible() {
        return attributeName.equals(VISIBLE_ANNOTATION_NAME) ||
               attributeName.equals(VISIBLE_PARAMETER_ANNOTATION_NAME);
    }

    public Attribute createAttribute(int name_index, int length, DataInputStream in, ConstantPool cp) {
        try {

            if ( attributeName.equals(ANNOTATION_DEFAULT_NAME) ) {
                return new AnnotationDefault(name_index, length, cp, AnnotationElementValue.createValue(in, cp));
            } else if ( attributeName.equals(VISIBLE_ANNOTATION_NAME) ||
                        attributeName.equals(INVISIBLE_ANNOTATION_NAME) )
            {
                return createAnnotation(name_index, length, in, cp);
            } else {
                return createParameterAnnotation(name_index, length, in, cp);
            }

        } catch (IOException e) {
            logger.error("Error parsing annotation attribute", e);
            return null;
        }
    }

    private Attribute createAnnotation(int name_index, int length, DataInputStream in, ConstantPool cp) throws IOException {
        int numAnnotations = in.readUnsignedShort();

        AnnotationAttribute attribute = new AnnotationAttribute(name_index,
                length, cp, isVisible(), numAnnotations);

        for (int i = 0; i < numAnnotations; i++) {
            attribute.addAnnotation(Annotation.createAnnotation(in, cp));
        }

        return attribute;
    }

    private Attribute createParameterAnnotation(int name_index, int length, DataInputStream in, ConstantPool cp) throws IOException {
        int numParameters = in.readUnsignedByte();

        ParameterAnnotationAttribute attribute = new ParameterAnnotationAttribute(name_index, length, cp,
                isVisible(), numParameters);

        for (int i = 0; i < numParameters; i++) {
            int numAnnotations = in.readUnsignedShort();
            for (int j = 0; j < numAnnotations; i++) {
                attribute.addAnnotation(i, Annotation.createAnnotation(in, cp));
            }
        }

        return attribute;
    }
}
