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
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph.CallgraphBuilder;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provide config options and callback methods to setup and build the callgraph.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DefaultCallgraphBuilder implements CallgraphBuilder {

    private final int callstringLength;
    private boolean skipNatives = false;
    private boolean useCallgraph = true;

    public DefaultCallgraphBuilder() {
        this.callstringLength = AppInfo.getSingleton().getCallstringLength();
    }

    public DefaultCallgraphBuilder(int callstringLength) {
        this.callstringLength = callstringLength;
    }

    public DefaultCallgraphBuilder(int callstringLength, boolean useCallgraph) {
        this.callstringLength = callstringLength;
        this.useCallgraph = useCallgraph;
    }

    public int getCallstringLength() {
        return callstringLength;
    }

    public boolean doSkipNatives() {
        return skipNatives;
    }

    public void setSkipNatives(boolean skipNatives) {
        this.skipNatives = skipNatives;
    }

    public boolean doUseCallgraph() {
        return useCallgraph;
    }

    public void setUseCallgraph(boolean useCallgraph) {
        this.useCallgraph = useCallgraph;
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

        Set<ExecutionContext> newContexts = new LinkedHashSet<ExecutionContext>();

        invokeLoop:
        for(InvokeSite invokeSite : findInvokeSites(method.getCode())) {

            Set<MethodInfo> methods = getInvokedMethods(context, invokeSite);

            if (skipNatives) {
                for (MethodInfo impl : methods) {
                    // we do not want to have native methods in the callgraph
                    // we do not return any method for an invokesite even if only some of them are native,
                    // to indicate that the candidates are not completely represented!
                    if (impl.isNative()) {
                        continue invokeLoop;
                    }
                }
            }

            for(MethodInfo impl : methods) {
                //System.out.println("Implemented Methods: "+impl+" from "+iNode.getBasicBlock().getMethodInfo().methodId+" in context "+callstring.toStringVerbose());

                CallString newCallString = callstring.push(invokeSite, callstringLength);

                newContexts.add(new ExecutionContext(impl, newCallString));
            }
        }

        return newContexts;
    }

    /**
     * @param code the method to process
     * @return a set of invokeSites in this method which should be included in the constructed callgraph.
     */
    protected Set<InvokeSite> findInvokeSites(MethodCode code) {
        return code.getInvokeSites();
    }

    /**
     * Get a list of a all invoke candidates for an invokesite
     * @param context the context, containing the callstring up to the invoke site
     * @param invokeSite the invokesite
     * @return a set of possible implementations
     */
    protected Set<MethodInfo> getInvokedMethods(ExecutionContext context, InvokeSite invokeSite) {
        if (useCallgraph) {
            // This uses either the existing default callgraph to construct a new one (which has
            // the advantage that the new callgraph is derived from an existing one), or looks up
            // in the type graph if no default callgraph exists
            return AppInfo.getSingleton().findImplementations(invokeSite, context.getCallString());
        } else {
            return AppInfo.getSingleton().findImplementations(invokeSite.getInvokeeRef());
        }
    }

}
