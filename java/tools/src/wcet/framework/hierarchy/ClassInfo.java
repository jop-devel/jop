/**
 * 
 */
package wcet.framework.hierarchy;

import java.util.HashSet;

/**
 * @author Elena Axamitova
 * @version 0.1 27.07.2007
 */
public class ClassInfo extends ItemInfo{
    private ClassInfo superclass;
    
    private HashSet<ClassInfo> subclasses;

    protected ClassInfo(String in){
	super(in);
	this.subclasses = new HashSet<ClassInfo>();
    }
    
    protected void addSubclass(ClassInfo ci){
	this.subclasses.add(ci);
    }

    /**
     * @return the subclasses
     */
    protected HashSet<ClassInfo> getSubclasses() {
        return subclasses;
    }

    /**
     * @return the superclass
     */
    protected ClassInfo getSuperclass() {
        return superclass;
    }

    /**
     * @param superclass the superclass to set
     */
    protected void setSuperclass(ClassInfo superclass) {
        this.superclass = superclass;
    }
}
