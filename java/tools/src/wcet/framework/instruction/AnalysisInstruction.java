/**
 * 
 */
package wcet.framework.instruction;

import wcet.framework.interfaces.instruction.IAnalysisInstruction;
/**
 * @author Elena Axamitova
 * @version 0.1 12.03.2007
 */
public class AnalysisInstruction implements IAnalysisInstruction{
	protected int opcode = -1;
	protected int type = -1;
	
	protected AnalysisInstruction(int opc){
		this.opcode = opc;
	}
	
	public int getOpcode(){
		return this.opcode;
	}
	
	public int getType(){
	    return this.type;
	}
}
