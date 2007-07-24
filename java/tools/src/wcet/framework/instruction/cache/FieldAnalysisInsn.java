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
public class FieldAnalysisInsn extends AnalysisInstruction implements ICacheAnalysisInstruction{
	
	public FieldAnalysisInsn(int opc){
		super(opc);
		this.type = IAnalysisInstructionType.FIELD_INSN;
	}

	public int get8BitLength() {
	    return 3;
	}

}
