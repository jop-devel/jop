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


    private final AppInfo appInfo;
    private final Map<String, CustomKey> registeredKeys;
    private int maxStructKeyID;

    /**
     * A class for custom attribute key.
     * To create a new key, use {@link #registerKey(KeyType,String,AttributeManager)}.
     */
    public static final class CustomKey  {

        private final String keyname;
        private final KeyType type;
        private final AttributeManager manager;
        private boolean clearOnInvalidate;
        private final int id;

        private CustomKey(KeyType type, String keyname, AttributeManager manager, int id) {
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

        public AttributeManager getManager() {
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

    private static final KeyManager singleton;
    static {
        singleton = new KeyManager();
    }

    public static KeyManager getSingleton() {
        return singleton;
    }

    public KeyManager() {
        appInfo = AppInfo.getSingleton();
        registeredKeys = new HashMap<String, CustomKey>();
        maxStructKeyID = 0;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Key registration
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Register a new key which can be used to attach values to structure elements (i.e. all {@link MemberInfo}).
     *
     * @see #registerKey(KeyType, String, AttributeManager)
     * @param keyname a unique keyname for the key
     * @return the new key
     */
    public CustomKey registerStructKey(String keyname) {
        return registerKey(KeyType.STRUCT, keyname, null);
    }

    /**
     * Register a new key which can be used to attach values to instructions or code blocks).
     *
     * @see #registerKey(KeyType, String, AttributeManager)
     * @param keyname a unique keyname for the key
     * @return the new key
     */
    public CustomKey registerCodeKey(String keyname) {
        return registerKey(KeyType.CODE, keyname, null);
    }

    public CustomKey registerKey(KeyType type, String keyname) {
        return registerKey(type, keyname, null);
    }

    public CustomKey registerKey(KeyType type, String keyname, AttributeManager manager) {

        // check if exists
        CustomKey k = registeredKeys.get(keyname);
        if ( k != null ) {
            throw new IllegalArgumentException("CustomKey "+keyname+" already exists but has a different definition.");
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
    // Get and set CustomValues
    //////////////////////////////////////////////////////////////////////////////

    public Object setCustomValue(InstructionHandle ih, CustomKey key, Object value) {
        return setCustomValue(ih, key, value, KEY_INSTRUCTION_VALUE);
    }

    public Object setCustomBlockValue(InstructionHandle ih, CustomKey key, Object value) {
        return setCustomValue(ih, key, value, KEY_BLOCK_VALUE);
    }

    public Object getCustomValue(InstructionHandle ih, CustomKey key) {
        return getCustomValue(ih, key, KEY_INSTRUCTION_VALUE);
    }

    public Object getCustomBlockValue(InstructionHandle ih, CustomKey key) {
        return getCustomValue(ih, key, KEY_BLOCK_VALUE);
    }

    public Object clearCustomKey(InstructionHandle ih, CustomKey key) {
        return clearCustomKey(ih, key, KEY_INSTRUCTION_VALUE);
    }

    public Object clearCustomBlockKey(InstructionHandle ih, CustomKey key) {
        return clearCustomKey(ih, key, KEY_BLOCK_VALUE);
    }

    public Object setCustomValue(MemberInfo member, CustomKey key, Object value) {
        // TODO might be more memory-efficient to store all custom-values in a single map per CustomKey
        //      would make clean easier, but needs special handling on rename/copy/remove/.. ops in ClassInfo
        return member.setCustomValue(key, value);
    }

    public Object getCustomValue(MemberInfo member, CustomKey key) {
        return member.getCustomValue(key);
    }

    public Object clearCustomKey(MemberInfo member, CustomKey key) {
        return member.removeCustomValue(key);
    }

    public void copyCustomValues(MemberInfo from, MemberInfo to) {

    }

    public void copyCustomValues(InstructionHandle from, InstructionHandle to) {
        // TODO copy all instruction- and block-values
    }

    public void clearAllValues(CustomKey key) {
        // do we need a version of this method with more fine-grained control (clear only from methods,..)?

        boolean fromStruct = key.getType().isStruct();
        boolean fromCode = key.getType().isCode();

        for ( ClassInfo cls : appInfo.getClassInfos() ) {
            if ( fromStruct ) {
                cls.removeCustomValue(key);

                for ( FieldInfo field : cls.getFields() ) {
                    field.removeCustomValue(key);
                }
            }
            for ( MethodInfo method : cls.getMethods() ) {
                if ( fromStruct ) {
                    method.removeCustomValue(key);
                }
                if ( fromCode && !method.isAbstract() ) {
                    InstructionList il = method.getInstructionList();
                    if (il == null) {
                        continue;
                    }
                    for (InstructionHandle ih : il.getInstructionHandles()) {
                        clearCustomKey(ih, key, KEY_INSTRUCTION_VALUE);
                        clearCustomKey(ih, key, KEY_BLOCK_VALUE);
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // Key management
    //////////////////////////////////////////////////////////////////////////////

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
        

    private Object setCustomValue(InstructionHandle ih, CustomKey key, Object value, Object ihKey) {
        @SuppressWarnings({"unchecked"})
        Map<CustomKey,Object> map = (Map<CustomKey, Object>) ih.getAttribute(ihKey);
        if (map == null) {
            map = new HashMap<CustomKey, Object>(1);
            ih.addAttribute(ihKey, map);
        }
        return map.put(key, value);
    }

    private Object getCustomValue(InstructionHandle ih, CustomKey key, Object ihKey) {
        @SuppressWarnings({"unchecked"})
        Map<CustomKey,Object> map = (Map<CustomKey, Object>) ih.getAttribute(ihKey);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    private Object clearCustomKey(InstructionHandle ih, CustomKey key, Object ihKey) {
        @SuppressWarnings({"unchecked"})
        Map<CustomKey,Object> map = (Map<CustomKey, Object>) ih.getAttribute(ihKey);
        if (map == null) {
            return null;
        }
        Object value = map.remove(key);
        if (map.size() == 0) {
            ih.removeAttribute(ihKey);
        }
        return value;
    }

}
