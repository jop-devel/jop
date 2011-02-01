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

package com.jopdesign.common.code;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Provide config options and callback methods to setup and build the callgraph.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DefaultCallgraphConfig implements CallGraph.CallgraphConfig {

    private final int callstringLength;

    public DefaultCallgraphConfig(int callstringLength) {
        this.callstringLength = callstringLength;
    }

    public int getCallstringLength() {
        return callstringLength;
    }

    @Override
    public List<ExecutionContext> getInvokedMethods(ExecutionContext context) {

        CallString callstring = context.getCallString();
        MethodInfo method = context.getMethodInfo();

        if (method.isAbstract()) {
            //noinspection unchecked
            return (List<ExecutionContext>) Collections.EMPTY_LIST;
        }

        // TODO use only callstring length 1, split nodes only if required using DFA/.. later on?

        List<ExecutionContext> newContexts = new LinkedList<ExecutionContext>();

        for(InvokeSite invokeSite : method.getCode().getInvokeSites()) {

            Set<MethodInfo> methods = getInvokedMethods(context, invokeSite);

            for(MethodInfo impl : methods) {
                //System.out.println("Implemented Methods: "+impl+" from "+iNode.getBasicBlock().getMethodInfo().methodId+" in context "+callstring.toStringVerbose());

                CallString newCallString = callstring.push(invokeSite, callstringLength);

                newContexts.add(new ExecutionContext(impl, newCallString));
            }
        }

        return newContexts;
    }

    protected Set<MethodInfo> getInvokedMethods(ExecutionContext context, InvokeSite invokeSite) {
        // TODO use context here somehow without falling back to callgraph?
        return AppInfo.getSingleton().findImplementations(invokeSite.getInvokeeRef());
    }

}
