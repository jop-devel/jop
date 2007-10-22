/**
 * 
 */
package wcet.components.graphbuilder.blocks;

// import java.util.ArrayList;
// import java.util.Iterator;

import wcet.components.graphbuilder.instruction.exectime.ITimeAnalysisInstruction;
import wcet.framework.interfaces.cfg.IVertexData;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.2 12.03.2007
 * 
 * Basic Block object. Represents a sequence of bytecodes without any jumps or jump
 * targets in the sequence.
 */
// TODO Instructions implemented in Java are not yet replaced with methods.
// TODO maybe I should create some abstract  ...Block classes 
public class BasicBlock implements IVertexData {
    // private ArrayList<ITimeAnalysisInstruction> instructions;
    /**
     * Basic block representing a sequence of bytecodes
     */
    public static final int SIMPLE_BB = 0;
    
    /**
     * BasicBlock that stores information of a method invoke
     */
    public static final int INVOKE_BB = 1;
    
    /**
     * BasicBlock that stores information of a method return
     */
    public static final int RETURN_BB = 2;
   
    /**
     * worst case execution time of this blck in cycles
     */
    protected int value;
    
    /**
     * this block type
     */
    protected int type;
    
    /**
     * this block bytecode size in bytes
     */
    protected int size;
    
    protected StringBuffer toString;

    public BasicBlock() {
	// this.instructions = new ArrayList<ITimeAnalysisInstruction>();
	this.value = 0;
	this.type = BasicBlock.SIMPLE_BB;
	this.size = 0;
	this.toString = new StringBuffer();
    }

    /**
     * Adds an analysis instruction to this block.
     * 
     * @param insn - Instruction to be added
     */
    public void addInstruction(IAnalysisInstruction insn) {
	ITimeAnalysisInstruction timeInsn = (ITimeAnalysisInstruction)insn;
	if (this.value != ITimeAnalysisInstruction.CYCLES_UNKNOWN) {
	    int maxCycles = timeInsn.getCycles();
	    if (maxCycles != ITimeAnalysisInstruction.CYCLES_UNKNOWN) {
		this.value += maxCycles;
	    } else {
		value = ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	    }
	}
	this.size += timeInsn.get8BitLength();
	this.toString.append(OpCodes.OPCODE_NAMES[insn.getOpcode()]);
	this.toString.append(" ");
    }

   /**
 * @return this blck type
 */
public int getType(){
	return this.type;
    }
    
    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.cfg.IVertexData#getValue()
         */
    public int getValue() {
	return this.value;
    }
    
    /**
     * @return this block bytecode size in bytes
     */
    public int getSize(){
	return this.size;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(){
	return "SimpleBB:"+this.size+", "+this.value+this.toString.toString();
    }
    
    
}
