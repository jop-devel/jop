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

package com.jopdesign.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ClassInfo extends BaseInfo {

    private Map<String,CustomClassInfo> customMap;
    private String className;

    public ClassInfo(AppInfo appInfo) {
        super(appInfo);
        // TODO maybe store in a single hashmap in AppInfo, more memory-efficient?
        customMap = new HashMap<String, CustomClassInfo>();
    }

    public CustomClassInfo clearCustomInfo(String key) {
        return customMap.remove(key);
    }

    public CustomClassInfo setCustomInfo(String key, CustomClassInfo customInfo) {
        // We could use generics here, and even use customInfo.class as key, but
        // 1) using class as key makes it impossible to attach the same CustomInfo class
        //    with different values multiple times,
        // 2) using generics like 'public <T extends CustomClassInfo> T getCustomInfo() .. ' does
        //    not work since Java removes the generics type-info at compile-time, its not possible
        //    to access T.class or do 'instanceof T' or even 'try { return (T) value; } catch (Exception e) ..',
        //    therefore a possible type conflict must always(!) be handled at the callsite, so we may as well make
        //    the cast explicit at the callsite.

        return customMap.put(key, customInfo);
    }

    public CustomClassInfo getCustomInfo(String key) {
        return customMap.get(key);
    }

    public FieldInfo getFieldInfo(String name) {
        return null;
    }

    public MethodInfo getMethodInfo(String signature) {
        return null;
    }

    public MethodInfo[] getMethodByName(String name) {
        return new MethodInfo[]{};
    }

    public String getClassName() {
        return className;
    }
}
