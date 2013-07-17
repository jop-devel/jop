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

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.processormodel.JOPConfig.CacheImplementation;

public class NoMethodCache implements MethodCache {

	public NoMethodCache() {
	}


	@Override
	public CacheImplementation getName() {
		return CacheImplementation.NO_METHOD_CACHE;
	}

    @Override
    public long getSizeInWords() {    	
    	return 0;
    }

    @Override
    public boolean allFit(long blocks) {
        return false;
    }

    @Override
	public boolean fitsInCache(int sizeInWords) {
		return true;
	}
    
    @Override
    public int getNumBlocks() { 
    	return 0;
    }

	@Override
	public boolean isLRU() {
		return false;
	}

	@Override
	public int requiredNumberOfBlocks(int sizeInWords) {
		return 0;
	}

	@Override
	public long getMissPenalty(int words, boolean isInvokeInstruction) {

		return 0;
	}

	@Override
	public long getMissPenaltyOnInvoke(int invokeeWords,
			Instruction invokeInstruction) {
		
		return 0;
	}

	@Override
	public long getMissPenaltyOnReturn(int invokerWords, Type returnType) {

		return 0;
	}

	@Override
	public int requiredNumberOfBlocks(MethodInfo mi) {

		return 0;
	}

}
