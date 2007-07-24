/**
 * 
 */
package wcet.framework.instruction.cache;

import wcet.framework.instruction.AnalysisInstruction;
import wcet.framework.interfaces.instruction.IAnalysisInstructionType;
import wcet.framework.interfaces.instruction.cache.ICacheAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 17.03.2007
 */
public class InsnAnalysisInstruction extends AnalysisInstruction implements ICacheAnalysisInstruction{

    protected InsnAnalysisInstruction(int opc) {
	super(opc);
	this.type = IAnalysisInstructionType.INSN;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.cfg.instructions.IAnalysisInstruction#get8BitLength()
     */
    public int get8BitLength() {
	return 1;
    }

}
