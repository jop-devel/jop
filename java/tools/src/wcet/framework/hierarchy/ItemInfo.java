/**
 * 
 */
package wcet.framework.hierarchy;

import java.util.HashSet;

/**
 * @author Elena Axamitova
 * @version 0.1 27.07.2007
 */
public abstract class ItemInfo {
    private String internalName;

    private HashSet<MethodKey> methods;
    
    protected ItemInfo(String in){
	this.internalName = in;
	this.methods = new HashSet<MethodKey>();
    }
    
    protected String getInternalName(){
	return this.internalName;
    }
    
    protected void addMethod(String name, String desc){
	this.methods.add(new MethodKey(this.internalName, name, desc));
    }
    
    protected boolean hasMethod(String name, String desc){
	return this.methods.contains(new MethodKey(this.internalName, name, desc));
    }
}
