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
public class LookupSwitchAnalysisInsn extends AnalysisInstruction implements ICacheAnalysisInstruction{
	private int size;
	public LookupSwitchAnalysisInsn(int opc, int size){
		super(opc);
		this.type = IAnalysisInstructionType.LOOKUPSWITCH_INSN;
	}

	public int get8BitLength() {
	  return size;
	}

}
