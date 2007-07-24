/**
 * 
 */
package wcet.framework.instruction.cache;

import wcet.framework.instruction.AnalysisInstruction;
import wcet.framework.interfaces.instruction.IAnalysisInstructionType;
import wcet.framework.interfaces.instruction.cache.ICacheAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
public class TableSwitchAnalysisInsn extends AnalysisInstruction implements ICacheAnalysisInstruction{
	private int size;
	public TableSwitchAnalysisInsn(int opc, int size){
		super(opc);
		this.type = IAnalysisInstructionType.TABLESWITCH_INSN;
	}

	public int get8BitLength() {
	    return size;
	}


}
