/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
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

package com.jopdesign.wcet;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.EmptyAppEventHandler;
import com.jopdesign.common.KeyManager.CustomKey;
import com.jopdesign.common.KeyManager.KeyType;
import com.jopdesign.wcet.annotations.BadAnnotationException;
import com.jopdesign.wcet.annotations.SourceAnnotationReader;
import com.jopdesign.wcet.annotations.SourceAnnotations;

import java.io.IOException;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCETEventHandler extends EmptyAppEventHandler {

    private CustomKey annotationKey;

    private WCETTool project;
    private SourceAnnotationReader annotationReader;

    public WCETEventHandler(WCETTool wcetTool) {
        this.project = wcetTool;
    }

    @Override
    public void onRegisterEventHandler(AppInfo appInfo) {
        // TODO attach annotations to CFG/blocks/instructions instead of classInfo
        annotationKey = appInfo.getKeyManager().registerKey(KeyType.STRUCT, "SourceAnnotations");
        annotationReader = new SourceAnnotationReader(project);
    }

    @Override
    public void onCreateClass(ClassInfo classInfo, boolean loaded) {
        // TODO maybe read annotations on class load, to avoid problems with modifications of code??
    }

    public SourceAnnotations getAnnotations(ClassInfo cli) throws BadAnnotationException, IOException {
        SourceAnnotations annots = (SourceAnnotations) cli.getCustomValue(annotationKey);
        if(annots == null) {
            annots = annotationReader.readAnnotations(cli);
            cli.setCustomValue(annotationKey, annots);
        }
        return annots;

    }
}
