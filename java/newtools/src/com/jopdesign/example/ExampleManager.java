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

package com.jopdesign.example;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.CustomValueManager;
import com.jopdesign.common.MethodInfo;

/**
 * A demonstration of a manager which maintains a single integer field.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ExampleManager implements CustomValueManager {

    private AppInfo.CustomKey myFieldID;

    public ExampleManager() {
    }

    public void registerManager(AppInfo appInfo) {
        myFieldID = appInfo.registerKey("iExampleField");

        int cnt = 0;
        for (ClassInfo clsInfo : appInfo.getClassInfos()) {
            setMyField(clsInfo, cnt++);
        }
    }

    public void onLoadClass(ClassInfo classInfo) {
        classInfo.setCustomValue(myFieldID, classInfo.getAppInfo().getClassInfos().size());
    }

    public void onClassModified(ClassInfo classInfo) {
    }

    public void onMethodModified(MethodInfo methodInfo) {
    }

    public int setMyField(ClassInfo clsInfo, int value) {
        return (Integer) clsInfo.setCustomValue(myFieldID, value);
    }

    public int getMyField(ClassInfo clsInfo) {
        return (Integer) clsInfo.getCustomValue(myFieldID);
    }
}
