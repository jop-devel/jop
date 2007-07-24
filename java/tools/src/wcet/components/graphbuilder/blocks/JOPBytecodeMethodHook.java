/**
 * 
 */
package wcet.components.graphbuilder.blocks;

import wcet.components.graphbuilder.methodgb.MethodBlock;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 11.04.2007
 * 
 * Not needed now, maybe later.
 */
public class JOPBytecodeMethodHook extends MethodHook {
    private IAnalysisInstruction instruction;
    
    public JOPBytecodeMethodHook(int invBB, int retBB, MethodBlock mb, IAnalysisInstruction insn){
	super(invBB, retBB, mb);
	this.instruction = insn;
    }
    
    public IAnalysisInstruction getInstruction(){
	return this.instruction;
    }
}
