/**
 * 
 */
package wcet.components.graphbuilder.instruction.dataflow;

import wcet.framework.instruction.cache.IincAnalysisInsn;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 12.03.2007
 */
public class IincDataAnalysisInsn extends IincAnalysisInsn
		implements IDataflowAnalysisInstruction {

	private int variable;

	private int increment;

	public IincDataAnalysisInsn(int var, int incr) {
		super(OpCodes.IINC);
		this.variable = var;
		this.increment = incr;
	}

	public int getVariable() {
		return this.variable;
	}

	public int getIncrement() {
		return this.increment;
	}
}
