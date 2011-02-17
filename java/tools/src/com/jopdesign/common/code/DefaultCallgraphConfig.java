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
import java.util.HashSet;
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
    public Set<ExecutionContext> getInvokedMethods(ExecutionContext context) {

        CallString callstring = context.getCallString();
        MethodInfo method = context.getMethodInfo();

        if (!method.hasCode()) {
            //noinspection unchecked
            return (Set<ExecutionContext>) Collections.EMPTY_SET;
        }

        // TODO use only callstring length 1, split nodes only if required using DFA/.. later on?

        Set<ExecutionContext> newContexts = new HashSet<ExecutionContext>();

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
        // This uses either the existing default callgraph to construct a new one (which has
        // the advantage that the new callgraph is derived from an existing one), or looks up
        // in the type graph if no default callgraph exists
        return AppInfo.getSingleton().findImplementations(invokeSite, context.getCallString());
    }

}
