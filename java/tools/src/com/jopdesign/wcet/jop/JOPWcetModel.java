/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet.jop;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.common.processormodel.JOPModel;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.timing.jop.JOPCmpTimingTable;
import com.jopdesign.timing.jop.JOPTimingTable;
import com.jopdesign.timing.jop.SingleCoreTiming;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JOPWcetModel implements WCETProcessorModel {

    private final String identifier;
    private MethodCache methodCache;
    private JOPTimingTable timing;
    private final JOPConfig config;
    private final ProcessorModel processorModel;
	private ObjectCache objectCache;

    /* TODO: add configuration stuff */
    public JOPWcetModel(WCETTool p) throws IOException {
    	
        StringBuffer key = new StringBuffer();
        this.processorModel = p.getProcessorModel();
        if (processorModel instanceof JOPModel) {
            this.config = ((JOPModel)processorModel).getConfig();
        } else {
            this.config = new JOPConfig(p.getConfig());
        }
        if(config.isCmp()) {
            this.timing = JOPCmpTimingTable.getCmpTimingTable(
            		config.getAsmFile(), config.rws(), config.wws(), config.getCpus(), config.getTimeslot());
        } else {
            this.timing = SingleCoreTiming.getTimingTable(config.getAsmFile());
            timing.configureWaitStates(config.rws(), config.wws());
        }
        
        this.methodCache = MethodCacheImplementation.getCacheModel(p, timing);
    	// FIXME: Hackish
        if(config.hasObjectCache()) {
        	this.objectCache = new ObjectCache(p, timing);
        	// set hit cycles for getfield
        	this.timing.setCustomTiming(Constants.GETFIELD, objectCache.getHitCycles());
        	this.timing.setCustomTiming(JopInstr.get("getfield_ref"), objectCache.getHitCycles());        	
        }
        key.append("jop");
        if(config.isCmp()) key.append("-cmp");
        key.append("-").append(config.getMethodCacheImpl());
        identifier = key.toString();

    }
    
    /** return true if we are not able to compute a WCET for the given bytecode */
    public boolean isUnboundedBytecode(Instruction ii) {
        // return (ii instanceof ATHROW || ii instanceof NEW ||
        //         ii instanceof NEWARRAY || ii instanceof ANEWARRAY);
		return ii instanceof ATHROW;
    }

    @Override
    public String getName() {
        return identifier;
    }

    /* get plain execution time, without global effects */
    public long getExecutionTime(ExecutionContext context, InstructionHandle ih) {

        Instruction i = ih.getInstruction();
        MethodInfo mctx = context.getMethodInfo();
        int jopcode = processorModel.getNativeOpCode(mctx, i);
        long cycles = timing.getLocalCycles(jopcode);
        if(isUnboundedBytecode(i)){
               WCETTool.logger.error("[FATAL] Unsupported (unbounded) bytecode: "+i.getName()+
                                            " in " + mctx.getFQMethodName()+
                                        ". Approximating with 2000 cycles, but result is not safe anymore!");
               return 2000L;
        } else if(cycles < 0) {
               throw new AssertionError("Requesting #cycles of non-implemented opcode: "+
                               i+"(opcode "+jopcode+") used in context: "+context);
        } else {
                return (int) cycles;
        }
    }

    public long basicBlockWCET(ExecutionContext context, BasicBlock bb) {
        int wcet = 0;
        for(InstructionHandle ih : bb.getInstructions()) {
            wcet += getExecutionTime(context, ih);
        }
        return wcet;
    }

	@Override
    public MethodCache getMethodCache() {
        return methodCache;
    }

	@Override
	public ObjectCache getObjectCache() {

		return objectCache;
	}

	@Override
	public Iterable<CacheModel> getCaches() {
		
		List<CacheModel> list = new ArrayList<CacheModel>(2);
		if(methodCache.getNumBlocks() > 0) list.add(methodCache);
		if(objectCache != null)            list.add(objectCache);
		return list;
	}

}
