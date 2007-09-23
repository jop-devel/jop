/**
 * 
 */
package wcet.framework.interfaces.hierarchy;

import java.util.HashSet;

import wcet.framework.hierarchy.MethodKey;

/**
 * @author Elena Axamitova
 * @version 0.1 31.07.2007
 */
public interface IHierarchy {
    public HashSet<MethodKey> getAllMethodImpls(MethodKey key);

    public HashSet<String> getAllSubtypes(String itemName);
    
    public String getSuperclassName(String itemName);
}
