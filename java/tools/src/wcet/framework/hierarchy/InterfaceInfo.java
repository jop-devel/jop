/**
 * 
 */
package wcet.framework.hierarchy;

import java.util.HashSet;

/**
 * @author Elena Axamitova
 * @version 0.1 27.07.2007
 */
public class InterfaceInfo extends ItemInfo{

    private HashSet<InterfaceInfo> subInterfaces;
    
    private HashSet<ClassInfo> implemetingClasses;
    /**
     * @param in
     */
    protected InterfaceInfo(String in) {
	super(in);
	this.subInterfaces = new HashSet<InterfaceInfo>();
	this.implemetingClasses = new HashSet<ClassInfo>();
    }

    protected void addSubInterface(InterfaceInfo ii){
	this.subInterfaces.add(ii);
    }
    
    protected void addImplementingClass(ClassInfo ci){
	this.implemetingClasses.add(ci);
    }
    
    /**
     * @return all subinterfaces
     */
    protected HashSet<InterfaceInfo> getSubInterfaces(){
	return this.subInterfaces;
    }

    /**
     * @return all implemeting classes
     */
    protected HashSet<ClassInfo> getImplemetingClasses() {
        return implemetingClasses;
    }
}
