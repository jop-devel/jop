/**
 * 
 */
package wcet.components.graphbuilder.blocks;

import wcet.components.graphbuilder.instruction.exectime.InsnTimeAnalysisInsn;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 11.04.2007
 * 
 * BasicBlock extension that stores all relevant information of a return block.
 */
public class ReturnBlock extends InvokeReturnBlock {
    public ReturnBlock(MethodKey key){
	super(key);
	this.type = BasicBlock.RETURN_BB;
    }
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.blocks.InvokeReturnBlock#setAnalyserInstruction(wcet.framework.interfaces.instruction.IAnalysisInstruction)
     */
    @Override
    public void setAnalyserInstruction(IAnalysisInstruction myInsn) {
	this.hiddenCycles = ((InsnTimeAnalysisInsn)myInsn).getHiddenCycles();
    }
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.blocks.InvokeReturnBlock#toString()
     */
    @Override
    public String toString() {
	String keyString;
	if (this.methKey == null) {
	    keyString = "null";
	} else {
	    keyString = this.methKey.toString();
	}
	return "RetBB:" + keyString + "Size:" + this.size;
    }
}
