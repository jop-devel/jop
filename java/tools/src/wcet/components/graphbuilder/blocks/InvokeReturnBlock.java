/**
 * 
 */
package wcet.components.graphbuilder.blocks;

import com.jopdesign.wcet.WCETInstruction;

//import wcet.components.graphbuilder.instruction.ICacheMemoryConstants;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 23.03.2007
 * 
 * Common features of invoke and return blocks in a control flow graph.
 */
public abstract class InvokeReturnBlock extends BasicBlock {
    
    /**
     * key of the caller(retrun)/callee(invoke)
     */
    protected MethodKey methKey =null;
    
    /**
     * number of hidden cyclen of this block's bytecode equivalent
     */
    protected int hiddenCycles;
    
    public InvokeReturnBlock(MethodKey key){
	this.methKey = key;
	this.hiddenCycles =0;
    }
    
    /**
     * Set the size of this block's method
     * @param s - size of the method
     */
    public void setSize(int s){
	this.size = s;
    }
    
    /**
     * Set the instruction of this block (for example invokevirtual, areturn)
     * @param myInsn - this block 
     */
    public abstract void setAnalyserInstruction(IAnalysisInstruction myInsn);
    
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.blocks.BasicBlock#getValue()
     */
    @Override
    public int getValue(){
	return this.getCacheMissExecTime();
    }
    
    
    /**
     * @return cycles needed to load this instruction in case of cache hit
     */
    public int getCacheHitExecTime(){
	int retVal =  WCETInstruction.calculateB(true, this.getCacheSize());
	return this.substractHiddenCycles(retVal);
	/*int retVal = 0;
	retVal = 4;
	return this.substractHiddenCycles(retVal);
	*/
    }
    
    /**
     * @return cycles needed to load this instruction in case of cache miss
     */
    public int getCacheMissExecTime(){
	int retVal =  WCETInstruction.calculateB(false, this.getCacheSize());
	return this.substractHiddenCycles(retVal);
	/*int retVal = 0;
	retVal = 6+ ((this.getCacheSize())+1)*(2+ICacheMemoryConstants.cws);
	return this.substractHiddenCycles(retVal);*/
    }
    
    /**
     * @return this block's method key
     */
    public MethodKey getMethodKey(){
	return this.methKey;
    }
    
    /**
     * @param retVal - load time of this method's instruction
     * @return load time minus hidden cycles of this method's instruction
     */
    private int substractHiddenCycles(int retVal){
	if(retVal>this.hiddenCycles){
	    return retVal - this.hiddenCycles;
	}else {
	    return 0;
	}
    }
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.blocks.BasicBlock#addInstruction(wcet.framework.interfaces.instruction.IAnalysisInstruction)
     */
    @Override
    public void addInstruction(IAnalysisInstruction insn) {
	//do nothing
    }
    
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.blocks.BasicBlock#toString()
     */
    @Override
    public abstract String toString();
    
 
    /**
     * Get the size in 32 bit words.
     * 
     * @return (size in bytes div 4)+1
     */
    private int getCacheSize(){
	double result = Math.floor(this.size/4);
	return (int)Math.round(result);
    }
}
