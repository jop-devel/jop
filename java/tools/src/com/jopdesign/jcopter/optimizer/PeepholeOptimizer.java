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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.jcopter.JCopter;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.util.InstructionFinder.CodeConstraint;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class PeepholeOptimizer extends AbstractOptimizer {

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".PeepholeOptimizer");

    private long matchBoolExpressions;

    public PeepholeOptimizer(JCopter jcopter) {
        super(jcopter);
    }

    @Override
    public void initialize() {
        matchBoolExpressions = 0;
    }

    @Override
    public void optimizeMethod(MethodInfo method) {
        InstructionList il = method.getCode().getInstructionList();

        // BCEL Example optimization
        optimizeBoolExpressions(il);
    }

    @Override
    public void printStatistics() {
        logger.info("Boolean Expressions optimized: "+matchBoolExpressions);
    }

    /**
     * Optimize some boolean expressions.
     * <p>
     * This code is taken directly from the BCEL manual, chapter 3.3.8.
     * </p>
     *
     * @param il the instruction list to optimize.
     */
    private void optimizeBoolExpressions(InstructionList il) {
        InstructionFinder f    = new InstructionFinder(il);
        String            pat = "IfInstruction ICONST_0 GOTO ICONST_1 NOP(IFEQ|IFNE)";

        CodeConstraint constraint = new CodeConstraint() {
            @Override
            public boolean checkCode(InstructionHandle[] match) {
                IfInstruction if1 = (IfInstruction)match[0].getInstruction();
                GOTO          g   = (GOTO)match[2].getInstruction();
                return (if1.getTarget() == match[3]) && (g.getTarget() == match[4]);
            }
        };

        for(Iterator e = f.search(pat, constraint); e.hasNext(); ) {
            InstructionHandle[] match = (InstructionHandle[])e.next();

            IfInstruction if1 = (IfInstruction)match[0].getInstruction();
            IfInstruction if2 = (IfInstruction)match[5].getInstruction();

            if1.setTarget(if2.getTarget()); // Update target

            try {
              il.delete(match[1], match[5]);
            } catch(TargetLostException ex) {
                for (InstructionHandle target : ex.getTargets()) {
                    for (InstructionTargeter t : target.getTargeters()) {
                        t.updateTarget(target, match[0]);
                    }
                }
            }

            matchBoolExpressions++;
        }

    }

}
