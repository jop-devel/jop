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

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class KeyManager {

    public enum KeyType {
        // If required, types 'Annotation' and 'Attribute' could be added too, keyname would
        // represent annotation-typename or attribute-name
        STRUCT, CODE, BOTH;

        public boolean isStruct() { return this == STRUCT || this == BOTH; }
        public boolean isCode()   { return this == CODE   || this == BOTH; }
    }

    private static final Object KEY_INSTRUCTION_VALUE = "KeyManager.InstructionValue";
    private static final Object KEY_BLOCK_VALUE = "KeyManager.BlockValue";

    // Clearing registeredKeys is not a good idea, since many keys are stored as static field!
    private final Map<String, CustomKey> registeredKeys;
    private int maxStructKeyID;

    /**
     * A class for custom attribute key.
     * To create a new key, use {@link #registerKey(KeyType,String, AppEventHandler)}.
     */
    public static final class CustomKey  {

        private final String keyname;
        private final KeyType type;
        private final AppEventHandler manager;
        private boolean clearOnInvalidate;
        private final int id;

        private CustomKey(KeyType type, String keyname, AppEventHandler manager, int id) {
            this.type = type;
            this.keyname = keyname;
            this.manager = manager;
            this.id = id;
            clearOnInvalidate = false;
        }

        public KeyType getType() {
            return type;
        }

        public String getKeyname() {
            return keyname;
        }

        public AppEventHandler getManager() {
            return manager;
        }

        /* not yet implemented
        public boolean doClearOnInvalidate() {
            return clearOnInvalidate;
        }

        public void setClearOnInvalidate(boolean clearOnInvalidate) {
            this.clearOnInvalidate = clearOnInvalidate;
        }
        */

        public int hashCode() {
            return keyname.hashCode();
        }

        /**
         * Two CustomKeys are equal, if their keynames are equal.
         * @param o the object to test.
         * @return true if equal.
         */
        public boolean equals(Object o) {
            if ( o instanceof CustomKey ) {
                return ((CustomKey)o).getKeyname().equals(keyname);
            }
            return false;
        }

        int getId() {
            return id;
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // Singleton
    //////////////////////////////////////////////////////////////////////////////

    private static final KeyManager singleton = new KeyManager();

    public static KeyManager getSingleton() {
        return singleton;
    }

    private KeyManager() {
        registeredKeys = new HashMap<String, CustomKey>();
        maxStructKeyID = 0;
    }

    public AppInfo getAppInfo() {
        return AppInfo.getSingleton();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Key registration
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Register a new key which can be used to attach values to structure elements (i.e. all {@link MemberInfo}).
     *
     * @see #registerKey(KeyType, String, AppEventHandler)
     * @param keyname a unique keyname for the key
     * @return the new key
     */
    public CustomKey registerStructKey(String keyname) {
        return registerKey(KeyType.STRUCT, keyname, null);
    }

    /**
     * Register a new key which can be used to attach values to instructions or code blocks).
     *
     * @see #registerKey(KeyType, String, AppEventHandler)
     * @param keyname a unique keyname for the key
     * @return the new key
     */
    public CustomKey registerCodeKey(String keyname) {
        return registerKey(KeyType.CODE, keyname, null);
    }

    /**
     * Register a new key which can be attached to memberInfos or code or both.
     * @param type the type of the key.
     * @param keyname the name of the key.
     * @return a key by that name.
     */
    public CustomKey registerKey(KeyType type, String keyname) {
        return registerKey(type, keyname, null);
    }

    public CustomKey registerKey(KeyType type, String keyname, AppEventHandler manager) {

        // check if exists
        CustomKey k = registeredKeys.get(keyname);
        if ( k != null ) {
            if (k.getType() != type || k.getManager() != manager) {
                throw new IllegalArgumentException("CustomKey "+keyname+" already exists but has a different definition!");
            }
            return k;
        }

        // currently there is no way to unregister a key and storing struct-keys might get replaced by maps for
        // memory-reasons, so a single ID-counter is fine; ID is only used for values attached to memberinfos
        int id = type.isStruct() ? maxStructKeyID++ : -1 ;

        k = new CustomKey(type, keyname, manager, id);
        registeredKeys.put(keyname, k);

        return k;
    }

    public CustomKey getRegisteredKey(String key) {
        return registeredKeys.get(key);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Key management
    //////////////////////////////////////////////////////////////////////////////

    public void clearAllValues(CustomKey key) {
        // do we need a version of this method with more fine-grained control (clear only from methods,..)?

        for ( ClassInfo cls : getAppInfo().getClassInfos() ) {
            clearAllValues(key, cls);
        }
    }

    public void clearAllValues(CustomKey key, ClassInfo classInfo) {
        boolean fromStruct = key.getType().isStruct();
        boolean fromCode = key.getType().isCode();

        if ( fromStruct ) {
            classInfo.removeCustomValue(key);

            for ( FieldInfo field : classInfo.getFields() ) {
                field.removeCustomValue(key);
            }
        }
        for ( MethodInfo method : classInfo.getMethods() ) {
            if ( fromStruct ) {
                method.removeCustomValue(key);
            }
            if ( fromCode && method.hasCode() ) {
                MethodCode code = method.getCode();
                InstructionList il = code.getInstructionList();
                for (InstructionHandle ih : il.getInstructionHandles()) {
                    code.clearCustomKey(ih, key);
                }
            }
        }
    }

    public void updateKeys(CustomKey[] keep) {
        // TODO invalidate all keys which have not been handled/kept
        // for each invalidated key: try manager first if set, invalidate
        // for each key not in keep: clear if requested by key
    }

    public void addedKeys(CustomKey[] added) {
        // TODO find better name
        // TODO add keys to list of up-to-date-keys (e.g. as a result of an analysis)
    }

    public boolean isSet(CustomKey keys) {
        // TODO check list of up-to-date-keys
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Internal Affairs
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Get number of registered struct keys. Should be accessed only by MemberInfo.
     *
     * @return number of currently registered keys which can be attached to a MemberInfo
     */
    int getNumStructKeys() {
        return maxStructKeyID;
    }

}
