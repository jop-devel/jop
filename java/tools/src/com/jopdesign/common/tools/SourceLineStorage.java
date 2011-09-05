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

package com.jopdesign.common.tools;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.EmptyAppEventHandler;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.ClassInfoNotFoundException;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.type.MemberID;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can load and store source-line and -file attributes of methods to a separate file.
 * This is needed when the source file of an instruction is not the same as the source file as the class.
 *
 * TODO there is a lot which can be done here: optionally store in custom method attributes, support for
 * flowfacts like loopbounds, ..
 *
 * File format is:
 * [<fqmethodname>]
 * <first-index>-<last-index>: <linenr> <fqsourceclass>
 * ...
 * <empty line>
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class SourceLineStorage extends EmptyAppEventHandler {

    private static class SourceLineEntry {
        private int start;
        private int end;
        private int line;
        private String className;

        private SourceLineEntry(int start, int end, int line, String className) {
            this.start = start;
            this.end = end;
            this.line = line;
            this.className = className;
        }

        public static List<SourceLineEntry> findSourceEntries(MethodCode code) {
            List<SourceLineEntry> entries = new ArrayList<SourceLineEntry>();

            int start = -1;
            int line = 0;
            String className = null;

            InstructionHandle[] il = code.getInstructionList(true, false).getInstructionHandles();
            for (int i = 0; i < il.length; i++) {
                InstructionHandle ih = il[i];

                String src = code.getSourceClassAttribute(ih);

                if (start >= 0) {
                    // we are currently processing an entry, check if we reached the end
                    if (src != null || code.getLineNumberEntry(ih,false) != null) {
                        // a new line entry starts at this line
                        entries.add(new SourceLineEntry(start, i-1, line, className));
                        start = -1;
                    }
                }

                // we have a new entry here
                if (src != null) {
                    start = i;
                    line = code.getLineNumber(ih);
                    className = src;
                }

            }

            // need to process the leftovers
            if (start >= 0) {
                entries.add(new SourceLineEntry(start, il.length-1, line, className));
            }

            return entries;
        }

        public void applyEntry(MethodCode code, InstructionHandle[] il) {
            ClassInfo cls = null;
            try {
                cls = AppInfo.getSingleton().getClassInfo(className,false);
            } catch (ClassInfoNotFoundException e) {
                logger.error("Could not find class " + className + ", skipping source line entry.", e);
                return;
            }
            if (cls == null) {
                logger.info("Could not find class " + className + ", skipping. Maybe no longer used?");
                return;
            }
            code.setLineNumber(il[start], cls, line);
            for (int i = start+1; i <= end; i++) {
                code.clearLineNumber(il[i]);
            }
        }

        public static SourceLineEntry readEntry(String entry) {
            int p1 = entry.indexOf('-');
            int p2 = entry.indexOf(':', p1+1);
            int p3 = entry.indexOf(' ', p2+2);

            int start = Integer.parseInt(entry.substring(0, p1));
            int end = Integer.parseInt(entry.substring(p1+1, p2));
            int line = Integer.parseInt(entry.substring(p2+2, p3));
            String className = entry.substring(p3+1);

            return new SourceLineEntry(start, end, line, className);
        }

        public void writeEntry(PrintWriter writer) {
            writer.printf("%d-%d: %d %s", start, end, line, className);
            writer.println();
        }

    }

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_APPINFO+".SourceLineStorage");

    private final File storage;
    private Map<MemberID, List<SourceLineEntry>> sourceLineMap;

    /**
     * Create a new storage manager.
     * @param storage the file where sourcefile annotations are kept.
     */
    public SourceLineStorage(File storage) {
        // TODO we could optionally use a method attribute for storage too, but this would add constant-pool
        //      entries which we need to remove later, so we stay with files for now..
        this.storage = storage;
    }

    @Override
    public void onRegisterEventHandler(AppInfo appInfo) {
        try {
            readSourceInfos();
        } catch (IOException e) {
            logger.error("Error reading source line file: "+e.getMessage(), e);
        }
    }

    @Override
    public void onCreateClass(ClassInfo classInfo, boolean loaded) {
        if (!loaded) return;

        try {
            if (AppInfo.getSingleton().getClassFile(classInfo).getTime() > storage.lastModified()) {
                // TODO we should skip loading for all classes here, but what to do about classes we have already loaded?
                logger.error("Classfile of "+classInfo+" is newer than source line file "+storage+
                        ", not loading source lines for this class!");
                return;
            }

            for (MethodInfo method : classInfo.getMethods()) {
                if (!method.hasCode()) continue;

                List<SourceLineEntry> entries = sourceLineMap.get(method.getMemberID());
                if (entries == null) continue;

                applySourceInfos(method, entries);
            }

        } catch (FileNotFoundException e) {
            logger.warn("Could not load source class file for timestamp check", e);
        }
    }

    /**
     * Store all source file and -line annotations of all classes to the storage file.
     */
    public void storeSourceInfos() {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(storage, false)));
        } catch (IOException e) {
            logger.error("Error opening file "+storage+" for writing, not writing source line infos!", e);
            return;
        }

        for (ClassInfo cls : AppInfo.getSingleton().getClassInfos()) {
            for (MethodInfo method : cls.getMethods()) {
                if (!method.hasCode()) continue;

                writeSourceInfos(method.getCode(), writer);
            }
        }

        writer.close();
    }

    /**
     * Load all source file and -line annotations for all classes from the storage file.
     */
    public void loadSourceInfos() {
        if (sourceLineMap == null) {
            try {
                readSourceInfos();
            } catch (IOException e) {
                logger.error("Error reading sourceline file "+storage, e);
            }
        }

        Map<MethodInfo, List<SourceLineEntry>> methodMap = new HashMap<MethodInfo, List<SourceLineEntry>>(sourceLineMap.size());
        for (MemberID mID : sourceLineMap.keySet()) {
            try {
                MethodInfo method = AppInfo.getSingleton().getMethodInfo(mID);
                methodMap.put(method, sourceLineMap.get(mID));
            } catch (MethodNotFoundException ignored) {
                logger.warn("No method for entry "+mID+" in " +storage+" found!");
            }
        }

        for (MethodInfo method : methodMap.keySet()) {
            try {
                if (AppInfo.getSingleton().getClassFile(method.getClassInfo()).getTime() > storage.lastModified()) {
                    logger.error("One or more class files are newer than annotation file "+storage+
                                 ", not loading source line annotations!");
                    return;
                }
            } catch (FileNotFoundException e) {
                logger.error("Could not get class file for class "+method.getClassInfo()+", not loading source lines!", e);
                return;
            }
        }

        for (Map.Entry<MethodInfo,List<SourceLineEntry>> entry : methodMap.entrySet()) {
            MethodInfo method = entry.getKey();
            applySourceInfos(method, entry.getValue());
        }
    }

    private void applySourceInfos(MethodInfo method, List<SourceLineEntry> entries) {
        MethodCode code = method.getCode();
        InstructionHandle[] il = code.getInstructionList(true, false).getInstructionHandles();

        for (SourceLineEntry sle : entries) {
            sle.applyEntry(code, il);
        }
    }

    private void readSourceInfos() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(storage));

        sourceLineMap = new HashMap<MemberID, List<SourceLineEntry>>();

        String entry;
        List<SourceLineEntry> entries = null;

        //noinspection NestedAssignment
        while ((entry = reader.readLine()) != null ) {

            entry = entry.trim();

            if (entry.startsWith("[")) {
                int p1 = entry.indexOf(']');
                String methodID = entry.substring(1, p1);
                entries = new ArrayList<SourceLineEntry>();

                MemberID id = MemberID.parse(methodID);
                sourceLineMap.put(id, entries);
            } else if (!"".equals(entry)) {
                assert entries != null;
                entries.add(SourceLineEntry.readEntry(entry));
            }
        }

        reader.close();
    }

    private void writeSourceInfos(MethodCode code, PrintWriter writer) {
        List<SourceLineEntry> entries = SourceLineEntry.findSourceEntries(code);

        if (entries.isEmpty()) {
            return;
        }

        writer.println("["+code.getMethodInfo().getMemberID()+"]");

        for (SourceLineEntry entry : entries) {
            entry.writeEntry(writer);
        }

        writer.println();
    }

}
