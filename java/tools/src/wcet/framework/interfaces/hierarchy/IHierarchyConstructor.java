/**
 * 
 */
package wcet.framework.interfaces.hierarchy;

import wcet.framework.hierarchy.MethodKey;

/**
 * @author Elena Axamitova
 * @version 0.1 31.07.2007
 */
public interface IHierarchyConstructor {
    public void addMethod(MethodKey key);

    public void addSubInterface(String superIntfName, String subIntfName);
    
    public void addSubClass(String superClassfName, String subClassName);
    
    public void addImplClass(String interfaceName, String className);
}
