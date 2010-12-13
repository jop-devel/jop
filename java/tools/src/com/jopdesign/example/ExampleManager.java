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
import com.jopdesign.common.KeyManager;
import com.jopdesign.common.EmptyAppEventHandler;

/**
 * A demonstration of a manager which maintains a single integer field.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ExampleManager extends EmptyAppEventHandler {

    private KeyManager.CustomKey myFieldID;

    public ExampleManager() {
    }

    public void onRegisterEventHandler(AppInfo appInfo) {

        // register a new custom attribute and set it to every existing class
        myFieldID = KeyManager.getSingleton().registerKey(KeyManager.KeyType.STRUCT, "iExampleField");

        int cnt = 0;
        for (ClassInfo clsInfo : appInfo.getClassInfos()) {
            setMyField(clsInfo, cnt++);
        }
    }

    @Override
    public void onCreateClass(ClassInfo classInfo, boolean loaded) {
        // set our custom attribute to every new class
        classInfo.setCustomValue(myFieldID, classInfo.getAppInfo().getClassInfos().size());
    }

    /////// Provide access methods to the custom attribute ///////

    public int setMyField(ClassInfo clsInfo, int value) {
        return (Integer) clsInfo.setCustomValue(myFieldID, value);
    }

    public int getMyField(ClassInfo clsInfo) {
        Object value = clsInfo.getCustomValue(myFieldID);
        // just for demo, do some null-pointer handling
        return value != null ? (Integer) value : -1;
    }
}
