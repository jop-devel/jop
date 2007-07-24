/**
 * 
 */
package wcet.framework.instruction.cache;

import wcet.framework.instruction.AnalysisInstruction;
import wcet.framework.interfaces.instruction.IAnalysisInstructionType;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
public class IntAnalysisInsn extends AnalysisInstruction{
	
	public IntAnalysisInsn(int opc){
		super(opc);
		this.type = IAnalysisInstructionType.INT_INSN;
	}

	public int get8BitLength() {
	   if (this.opcode==OpCodes.SIPUSH){
	       return 3;
	   }else{//NEWARAY or BIPUSH
	    return 2;
	   }
	}


}
