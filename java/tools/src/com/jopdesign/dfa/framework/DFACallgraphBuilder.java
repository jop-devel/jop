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

package com.jopdesign.dfa.framework;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.DefaultCallgraphBuilder;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.dfa.DFATool;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DFACallgraphBuilder extends DefaultCallgraphBuilder {

    private final AppInfo appInfo;
    private final DFATool dfaTool;

    private static final Logger logger = Logger.getLogger(DFATool.LOG_DFA_FRAMEWORK+".DFACallgraphBuilder");

    public DFACallgraphBuilder(DFATool dfaTool, int callgraphLength) {
        super(callgraphLength);
        this.dfaTool = dfaTool;
        this.appInfo = AppInfo.getSingleton();
    }

    @Override
    protected Set<MethodInfo> getInvokedMethods(ExecutionContext context, InvokeSite invokeSite) {
        // Receivers of instructions which are JVM calls are not the JVM class implementing the call, so
        // to avoid problems we only use the DFA for virtual calls, all other can be uniquely resolved anyway.
        if (!invokeSite.isVirtual()) {
            return Collections.singleton(invokeSite.getInvokeeRef().getMethodInfo());
        }
        Set<String> receivers = dfaTool.getReceivers(invokeSite.getInstructionHandle(), context.getCallString());
        if (receivers == null) {
            // This can happen e.g. because we have all Runnable.run() methods as roots, regardless if they are used
            logger.debug("No receivers for " + invokeSite.getInvokeeRef() + " at " + invokeSite + " in call context " +
                         context.getCallString().toStringVerbose(false));
            return appInfo.findImplementations(invokeSite, context.getCallString());
        }

        if (receivers.size() == 0) {
            // This can happen if a method is analyzed for some contexts but not for this context
            logger.debug("No receivers for " + invokeSite.getInvokeeRef() + " at " + invokeSite + " in call context " +
                         context.getCallString().toStringVerbose(false));
            return appInfo.findImplementations(invokeSite, context.getCallString());
        }

        Set<MethodInfo> methods = new LinkedHashSet<MethodInfo>(receivers.size());
        for (String rcv: receivers) {
            MemberID mId = MemberID.parse(rcv);
            methods.add(appInfo.getMethodRef(mId).getMethodInfo());
        }
        return methods;
    }
}
