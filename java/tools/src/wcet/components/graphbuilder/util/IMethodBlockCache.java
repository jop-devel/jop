/**
 * 
 */
package wcet.components.graphbuilder.util;

import wcet.components.graphbuilder.methodgb.MethodBlock;
import wcet.components.graphbuilder.methodgb.MethodKey;
import wcet.framework.exceptions.TaskInitException;

/**
 * @author Elena Axamitova
 * @version 0.1 23.07.2007
 */
public interface IMethodBlockCache {
    public MethodBlock getMethodBlock(MethodKey key) throws TaskInitException;
}
