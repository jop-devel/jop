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

import com.jopdesign.common.type.Signature;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class BaseInfo {

    private AppInfo appInfo;

    private Map<String,Object> customValues;
    private Object[] registeredValues;

    public BaseInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
        customValues = new HashMap<String, Object>();
        registeredValues = new Object[appInfo.getRegisteredKeyCount()];
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public abstract ClassInfo getClassInfo();

    public Signature getSignature() {
        return null;
    }

    public Object removeCustomValue(String key) {
        return setCustomValue(key, null);
    }

    /**
     * Sets a new custom info value for a key.
     * Setting null as value has the same effect as removing the key.
     *
     * @param key
     * @param customValue
     * @return
     */
    public Object setCustomValue(String key, Object customValue) {
        // We could use generics here, and even use customValue.class as key, but
        // 1) using class as key makes it impossible to attach the same CustomValue class
        //    with different values multiple times,
        // 2) using generics like 'public <T extends CustomClassInfo> T getCustomValue() .. ' does
        //    not work since Java removes the generics type-info at compile-time, its not possible
        //    to access T.class or do 'instanceof T' or even 'try { return (T) value; } catch (Exception e) ..',
        //    therefore a possible type conflict must always(!) be handled at the callsite, so we may as well make
        //    the cast explicit at the callsite.

        int regID = appInfo.getRegisteredKeyID(key);
        return setCustomValue(key, regID, customValue);
    }

    public Object setCustomValue(int keyID, Object customValue) {
        return setCustomValue(appInfo.getKeyByID(keyID), keyID, customValue);
    }

    public Object setCustomValue(String key, int keyID, Object customValue) {

        if ( keyID != -1 ) {
            registeredValues[keyID] = customValue;
        }

        if ( customValue == null ) {
            return customValues.remove(key);
        } else {
            return customValues.put(key, customValue);
        }
    }

    public Object getCustomValue(String key) {
        return customValues.get(key);
    }

    public Object getCustomValue(int keyID) {
        return registeredValues[keyID];
    }


}
