/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantPool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Attribute defined in JSR202
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class StackMapTable extends CustomAttribute {

    public static class VerificationTypeInfo {

        private int tag;
        private int cp_index;
        private int offset;

        public VerificationTypeInfo(DataInputStream file) throws IOException {
            tag = file.readUnsignedByte();
            if (isObject()) {
                cp_index = file.readUnsignedShort();
            }
            if (isUninitialized()) {
                offset = file.readUnsignedShort();
            }
        }

        public void dump(DataOutputStream file) throws IOException {
            file.writeByte(tag);
            if (isObject()) {
                file.writeShort(cp_index);
            }
            if (isUninitialized()) {
                file.writeShort(offset);
            }
        }

        public int getLength() {
            int length = 1;
            if (isObject() || isUninitialized()) {
                length += 2;
            }
            return length;
        }

        public int getCPIndex() { return cp_index; }

        public int getOffset() { return offset; }

        public boolean isTop() { return tag == 0; }

        public boolean isInteger() { return tag == 1; }

        public boolean isFloat() { return tag == 2; }

        public boolean isLong() { return tag == 4; }

        public boolean isDouble() { return tag == 3; }

        public boolean isNull() { return tag == 5; }

        public boolean isUninitializedThis() { return tag == 6; }

        public boolean isObject() { return tag == 7; }

        public boolean isUninitialized() { return tag == 8; }
    }

    public static class StackMapFrame {
        private int tag;
        private int offset_delta;
        private List<VerificationTypeInfo> stack, locals;

        public StackMapFrame(DataInputStream file, ConstantPool constant_pool) throws IOException {
            tag = file.readUnsignedByte();
            if (isSameLocalsOneStackItemFrame()) {
                stack = new ArrayList<VerificationTypeInfo>(1);
                stack.add(new VerificationTypeInfo(file));
            }
            if (isSameLocalsOneStackItemFrameExtended()) {
                offset_delta = file.readUnsignedShort();
                stack = new ArrayList<VerificationTypeInfo>(1);
                stack.add(new VerificationTypeInfo(file));
            }
            if (isChopFrame() || isSameFrameExtended()) {
                offset_delta = file.readUnsignedShort();
            }
            if (isAppendFrame()) {
                offset_delta = file.readUnsignedShort();
                int len = tag - 251;
                locals = new ArrayList<VerificationTypeInfo>(len);
                for (int i = 0; i < len; i++) {
                    locals.add(new VerificationTypeInfo(file));
                }
            }
            if (isFullFrame()) {
                offset_delta = file.readUnsignedShort();
                int len;
                len = file.readUnsignedShort();
                locals = new ArrayList<VerificationTypeInfo>(len);
                for (int i = 0; i < len; i++) {
                    locals.add(new VerificationTypeInfo(file));
                }
                len = file.readUnsignedShort();
                stack = new ArrayList<VerificationTypeInfo>(len);
                for (int i = 0; i < len; i++) {
                    stack.add(new VerificationTypeInfo(file));
                }
            }
        }

        public int getLength() {
            int length = 1;
            if (isSameLocalsOneStackItemFrame()) {
                length += stack.get(0).getLength();
            }
            if (isSameLocalsOneStackItemFrameExtended()) {
                length += 2;
                length += stack.get(0).getLength();
            }
            if (isChopFrame() || isSameFrameExtended()) {
                length += 2;
            }
            if (isAppendFrame()) {
                length += 2;
                for (VerificationTypeInfo t : locals) {
                    length += t.getLength();
                }
            }
            if (isFullFrame()) {
                length += 6;
                for (VerificationTypeInfo t : locals) {
                    length += t.getLength();
                }
                for (VerificationTypeInfo t : stack) {
                    length += t.getLength();
                }
            }
            return length;
        }

        public void dump(DataOutputStream file) throws IOException {
            file.writeByte(tag);
            if (isSameLocalsOneStackItemFrame()) {
                stack.get(0).dump(file);
            }
            if (isSameLocalsOneStackItemFrameExtended()) {
                file.writeShort(offset_delta);
                stack.get(0).dump(file);
            }
            if (isChopFrame() || isSameFrameExtended()) {
                file.writeShort(offset_delta);
            }
            if (isAppendFrame()) {
                file.writeShort(offset_delta);
                for (VerificationTypeInfo t : locals) {
                    t.dump(file);
                }
            }
            if (isFullFrame()) {
                file.writeShort(offset_delta);
                file.writeShort(locals.size());
                for (VerificationTypeInfo t : locals) {
                    t.dump(file);
                }
                file.writeShort(stack.size());
                for (VerificationTypeInfo t : stack) {
                    t.dump(file);
                }
            }
        }

        public int getOffsetDelta() {
            return offset_delta;
        }

        public List<VerificationTypeInfo> getStack() {
            return stack;
        }

        public List<VerificationTypeInfo> getLocals() {
            return locals;
        }

        public boolean isSameFrame() { return tag >= 0 && tag <= 63; }

        public boolean isSameLocalsOneStackItemFrame() { return tag >= 64 && tag <=127; }

        public boolean isSameLocalsOneStackItemFrameExtended() { return tag == 247; }

        public boolean isChopFrame() { return tag >= 248 && tag <= 250; }

        public boolean isSameFrameExtended() { return tag == 251; }

        public boolean isAppendFrame() { return tag >= 252 && tag <= 254; }

        public boolean isFullFrame() { return tag == 255; }
    }

    private final List<StackMapFrame> entries;

    public StackMapTable(int name_index, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
        super(Constants.ATTR_UNKNOWN, name_index, length, constant_pool);

        int num = file.readUnsignedShort();
        entries = new ArrayList<StackMapFrame>(num);
        for (int i = 0; i < num; i++) {
            entries.add(new StackMapFrame(file, constant_pool));
        }

    }

    public StackMapTable(int name_index, int length, List<StackMapFrame> entries, ConstantPool constant_pool) {
        super(Constants.ATTR_UNKNOWN, name_index, length, constant_pool);
        this.entries = new ArrayList<StackMapFrame>(entries);
    }

    public List<StackMapFrame> getEntries() {
        return entries;
    }

    @Override
    public void dump(DataOutputStream file) throws IOException {
        updateLength();
        super.dump(file);
        file.writeShort(entries.size());
        for (StackMapFrame f : entries) {
            f.dump(file);
        }
    }

    @Override
    public String toString() {
        return "StackMapTable";
    }

    @Override
    public Attribute copy(ConstantPool _constant_pool) {
        // TODO deep copy
        List<StackMapFrame> newEntries = new ArrayList<StackMapFrame>(entries);
        return new StackMapTable(name_index, length, newEntries, _constant_pool);
    }

    @Override
    public Collection<Integer> getConstantPoolIDs() {
        Set<Integer> ids = new HashSet<Integer>();
        for (StackMapFrame f : entries) {
            if (f.getLocals() != null) {
                for (VerificationTypeInfo t : f.getLocals()) {
                    if (t.isObject()) {
                        ids.add(t.getCPIndex());
                    }
                }
            }
            if (f.getStack() != null) {
                for (VerificationTypeInfo t : f.getStack()) {
                    if (t.isObject()) {
                        ids.add(t.getCPIndex());
                    }
                }
            }
        }
        return ids;
    }

    public void updateLength() {
        int length = 2;
        for (StackMapFrame f : entries) {
            length += f.getLength();
        }
        setLength(length);
    }
}
