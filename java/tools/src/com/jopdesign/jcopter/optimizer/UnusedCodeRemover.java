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

package com.jopdesign.jcopter.optimizer;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.common.tools.UsedCodeFinder;
import com.jopdesign.common.tools.UsedCodeFinder.Mark;
import com.jopdesign.jcopter.JCopter;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class UnusedCodeRemover {

    public static final BooleanOption REMOVE_CONST_FIELDS =
            new BooleanOption("remove-const-fields", "Remove unused static final fields, "+
                    "does not (yet) check if they are used by WCA-annotations", false);

    public static final Option[] optionList =
            { REMOVE_CONST_FIELDS };

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".UnusedCodeRemover");

    private final JCopter jcopter;
    private final OptionGroup options;

    private UsedCodeFinder ucf;
    private boolean removeConstFields;

    public UnusedCodeRemover(JCopter jcopter, OptionGroup options) {
        this.jcopter = jcopter;
        this.options = options;
        removeConstFields = options.getOption(REMOVE_CONST_FIELDS);
        ucf = new UsedCodeFinder();
    }

    public JCopter getJCopter() {
        return jcopter;
    }

    public void execute() {
        ucf.resetMarks();
        // This starts at all app roots and JVM roots, as well as all threads,
        // <clinit> methods are marked in all reached classes
        ucf.markUsedMembers();

        // We also need to mark everything else we do not want to remove ..
        AppInfo appInfo = AppInfo.getSingleton();
        ProcessorModel pm = appInfo.getProcessorModel();

        if (pm.keepJVMClasses()) {
            for (String clName : pm.getJVMClasses()) {
                ClassInfo cls = appInfo.getClassInfo(clName);
                if (cls != null) {
                    ucf.markUsedMembers(cls, true);
                }
            }
        }

        removeUnusedMembers();
    }

    /**
     * Remove all unused classes, methods and fields.
     */
    private void removeUnusedMembers() {
        AppInfo appInfo = AppInfo.getSingleton();

        // we cannot modify the lists while iterating through it
        List<ClassInfo> unusedClasses = new LinkedList<ClassInfo>();
        List<FieldInfo> unusedFields = new LinkedList<FieldInfo>();
        List<MethodInfo> unusedMethods = new LinkedList<MethodInfo>();

        int fields = 0;
        int methods = 0;

        for (ClassInfo cls : appInfo.getClassInfos()) {
            if (ucf.getMark(cls)==Mark.UNUSED) {
                unusedClasses.add(cls);
                logger.debug("Removing unused class " +cls);
                continue;
            }

            unusedFields.clear();
            unusedMethods.clear();

            if (appInfo.isHwObject(cls)) {
                // Do not remove fields from hardware objects, else the mapping gets broken and
                // chaos takes over!
                logger.debug("Skipping fields of used hardware object " +cls);
            } else {
                for (FieldInfo f : cls.getFields()) {
                    if (ucf.getMark(f)==Mark.UNUSED) {
                        unusedFields.add(f);
                    }
                }
            }
            for (MethodInfo m : cls.getMethods()) {
                Mark mark = ucf.getMark(m);
                if (mark == Mark.UNUSED) {
                    unusedMethods.add(m);
                }
                if (mark == Mark.MARKED && !m.isNative() && !m.isAbstract()) {
                    logger.info("Making unused method "+m+" abstract");
                    m.setAbstract(true);
                    m.getClassInfo().setAbstract(true);
                }
            }

            for (FieldInfo f : unusedFields) {
                fields += removeField(f);
            }
            for (MethodInfo m : unusedMethods) {
                methods += removeMethod(m);
            }
        }

        appInfo.removeClasses(unusedClasses);

        int classes = unusedClasses.size();
        logger.info("Removed " + classes + (classes == 1 ? " class, " : " classes, ") +
                                 fields + (fields == 1 ? " field, " : " fields, ") +
                                 methods + (methods == 1 ? " method" : " methods"));
    }

    private int removeField(FieldInfo f) {

        if (f.isStatic() && f.isFinal() && !removeConstFields) {
            logger.debug("Not removing unused static final "+f);

            // Instead, mark it as unused
            f.setUnusedAnnotation();

            return 0;
        } else {
            ClassInfo cls = f.getClassInfo();
            logger.debug("Removing unused field "+f);
            cls.removeField(f.getShortName());
            return 1;
        }
    }

    private int removeMethod(MethodInfo m) {
        ClassInfo cls = m.getClassInfo();
        logger.debug("Removing unused method "+m);
        cls.removeMethod(m.getMethodSignature());
        return 1;
    }
}
