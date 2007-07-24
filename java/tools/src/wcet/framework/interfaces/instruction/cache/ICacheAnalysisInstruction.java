/**
 * 
 */
package wcet.framework.interfaces.instruction.cache;

import wcet.framework.interfaces.instruction.IAnalysisInstruction;

/**
 * @author Elena Axamitova
 * @version 0.1 18.03.2007
 */
public interface ICacheAnalysisInstruction extends IAnalysisInstruction{
    public int get8BitLength();
}
