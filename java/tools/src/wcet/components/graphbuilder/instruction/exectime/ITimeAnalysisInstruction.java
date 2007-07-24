/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import wcet.framework.interfaces.instruction.cache.ICacheAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 22.02.2007
 */
public interface ITimeAnalysisInstruction extends ICacheAnalysisInstruction{
    
    public final int CYCLES_UNKNOWN = -1;
    
    public final int CYCLES_OF_LAST_METHOD = 0;
	
    public static final int rws = 1;
    
    public static final int wws = 1;
    
    public static final int cws = 0;
    
    public int getCycles();
	
}
