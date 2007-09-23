/**
 * 
 */
package wcet.components.graphbuilder.util;

import wcet.components.graphbuilder.methodgb.MethodBlock;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.hierarchy.MethodKey;

/**
 * @author Elena Axamitova
 * @version 0.1 23.07.2007
 */
public interface IMethodBlockCache {
    public MethodBlock getMethodBlock(MethodKey key) throws TaskInitException;
}
