/**
 * 
 */
package wcet.components.graphbuilder.blocks;

import wcet.components.graphbuilder.instruction.exectime.MethodTimeAnalysisInsn;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 11.04.2007
 * 
 *  BasicBlock extension that stores all relevant information of a return block.
 */
public class InvokeBlock extends InvokeReturnBlock {
    public InvokeBlock(MethodKey key) {
	super(key);
	this.type = BasicBlock.INVOKE_BB;
    }

    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.blocks.InvokeReturnBlock#setAnalyserInstruction(wcet.framework.interfaces.instruction.IAnalysisInstruction)
     */
    @Override
    public void setAnalyserInstruction(IAnalysisInstruction myInsn) {
	this.hiddenCycles = ((MethodTimeAnalysisInsn) myInsn).getHiddenCycles();
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
	return "InvBB:" + keyString + "Size:" + this.size;
    }

}
