/**
 * 
 */
package wcet.framework.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import wcet.framework.interfaces.hierarchy.IHierarchy;
import wcet.framework.interfaces.hierarchy.IHierarchyConstructor;

/**
 * @author Elena Axamitova
 * @version 0.1 28.07.2007
 */
public class Hierarchy implements IHierarchyConstructor, IHierarchy {
    private HashMap<String, ClassInfo> classes;

    private HashMap<String, InterfaceInfo> interfaces;

    public Hierarchy() {
	this.classes = new HashMap<String, ClassInfo>();
	this.interfaces = new HashMap<String, InterfaceInfo>();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.hierarchy.IHierarchy#getAllMethodImpls(wcet.framework.hierarchy.MethodKey)
         */
    public HashSet<MethodKey> getAllMethodImpls(MethodKey key) {
	HashSet<MethodKey> result = new HashSet<MethodKey>();
	//get the method called when dynamic type equals the declared one 
	/*ClassInfo currClass = this.getClassInfo(key.getOwner());
	while ((currClass != null)
		&& (!currClass.hasMethod(key.getName(), key.getDecription()))) {
	    currClass = currClass.getSuperclass();
	}
	result.add(new MethodKey(currClass.getInternalName(), key.getName(),
		key.getDecription()));*/
	result.add(key);
	//get all method implementation in subtypes
	HashSet<String> types = this.getAllSubtypes(key.getOwner());
	for (Iterator<String> iterator = types.iterator(); iterator.hasNext();) {
	    String typeName = iterator.next();
	    ClassInfo currClass = this.classes.get(typeName);
	    if (currClass.hasMethod(key.getName(), key.getDecription()))
		result.add(new MethodKey(currClass.getInternalName(), key
			.getName(), key.getDecription()));
	}
	return result;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.hierarchy.IHierarchy#getAllSubtypes(java.lang.String)
         */
    public HashSet<String> getAllSubtypes(String itemName) {
	HashSet<String> result = new HashSet<String>();
	if (this.classes.containsKey(itemName)) {
	    ClassInfo currClass = this.classes.get(itemName);
	    result.add(currClass.getInternalName());
	    HashSet<ClassInfo> subclasses = currClass.getSubclasses();
	    for (Iterator<ClassInfo> iterator = subclasses.iterator(); iterator
		    .hasNext();) {
		currClass = iterator.next();
		result.addAll(this.getAllSubtypes(currClass.getInternalName()));
	    }
	} else if (this.interfaces.containsKey(itemName)) {
	    InterfaceInfo currInterface = this.interfaces.get(itemName);
	    HashSet<ClassInfo> implClasses = currInterface
		    .getImplemetingClasses();
	    for (Iterator<ClassInfo> iterator = implClasses.iterator(); iterator
		    .hasNext();) {
		ClassInfo currClass = iterator.next();
		result.addAll(this.getAllSubtypes(currClass.getInternalName()));
	    }
	    HashSet<InterfaceInfo> subinterfaces = currInterface
		    .getSubInterfaces();
	    for (Iterator<InterfaceInfo> iterator = subinterfaces.iterator(); iterator
		    .hasNext();) {
		currInterface = iterator.next();
		result.addAll(this.getAllSubtypes(currInterface
			.getInternalName()));
	    }
	}
	return result;
    }

    private ClassInfo getClassInfo(String className) {
	ClassInfo result = this.classes.get(className);
	if (result == null) {
	    result = new ClassInfo(className);
	    this.classes.put(className, result);
	}
	return result;
    }

    private InterfaceInfo getInterfaceInfo(String interfaceName) {
	InterfaceInfo result = this.interfaces.get(interfaceName);
	if (result == null) {
	    result = new InterfaceInfo(interfaceName);
	    this.interfaces.put(interfaceName, result);
	}
	return result;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.hierarchy.IHierarchyConstructor#addImplClass(java.lang.String,
         *      java.lang.String)
         */
    public void addImplClass(String interfaceName, String className) {
	InterfaceInfo intf = this.getInterfaceInfo(interfaceName);
	intf.addImplementingClass(this.getClassInfo(className));
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.hierarchy.IHierarchyConstructor#addMethod(wcet.framework.hierarchy.MethodKey)
         */
    public void addMethod(MethodKey key) {
	if (this.classes.containsKey(key.getOwner())) {
	    this.getClassInfo(key.getOwner()).addMethod(key.getName(),
		    key.getDecription());
	} else if (this.interfaces.containsKey(key.getOwner())) {
	    this.getInterfaceInfo(key.getOwner()).addMethod(key.getName(),
		    key.getDecription());
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.hierarchy.IHierarchyConstructor#addSubClass(java.lang.String,
         *      java.lang.String)
         */
    public void addSubClass(String superClassName, String subClassName) {
	ClassInfo superClass = this.getClassInfo(superClassName);
	ClassInfo subClass = this.getClassInfo(subClassName);
	superClass.addSubclass(subClass);
	subClass.setSuperclass(superClass);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.hierarchy.IHierarchyConstructor#addSubInterface(java.lang.String,
         *      java.lang.String)
         */
    public void addSubInterface(String superIntfName, String subIntfName) {
	InterfaceInfo superIntf = this.getInterfaceInfo(superIntfName);
	superIntf.addSubInterface(this.getInterfaceInfo(subIntfName));
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.hierarchy.IHierarchy#getSuperName(java.lang.String)
         */
    public String getSuperclassName(String itemName) {
	ClassInfo subClass = this.getClassInfo(itemName);
	return subClass.getSuperclass().getInternalName();
    }
}
