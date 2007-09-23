/**
 * 
 */
package wcet.components.graphbuilder.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.methodgb.MethodBlock;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.hierarchy.MethodKey;

/**
 * @author Elena Axamitova
 * @version 0.3 12.01.2007
 * 
 * Caches method blocks of previously encountred classes. If the required method block
 * not found in cache, it will be created and  stored. The cache cache uses 
 * least recently accessed strategy when removing the oldest entry.
 */
// TODO does not handle Interfaces and abstract classes (? here ?)
public class MethodBlockCache implements IMethodBlockCache{
    /**
     * load factor of the map storing method blocks.
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * FileList that provides InputStreams for new classes
     */
    private IFileList fileList = null;

    /**
     * the method map (all method blocks of a class) cache
     */
    private LinkedHashMap<String, MethodMap> classMap = null;
    
    private ClassVisitor filer;

    private ClassNode currClassNode;
    
    /**
     * Construct a new cache that uses the provided file list
     * @param fl - file list to get class input streams from
     */
    public MethodBlockCache(IFileList fl) {
	this.fileList = fl;
	this.classMap = new LinkedHashMap<String, MethodMap>(
		IGraphBuilderConstants.CAPACITY, MethodBlockCache.LOAD_FACTOR,
		//true means least recently accessed strategy
		true) {
	    protected boolean removeEldestEntry(
		    Map.Entry<String, MethodMap> eldest) {
		return this.size() > IGraphBuilderConstants.CAPACITY;
	    }
	};
    }
    
    /**
     * Get MethodBlock for the method key. If not in cache, read the class, store
     * all method blocks of the class in cache.
     * @param key
     * @return
     * @throws TaskInitException
     */
    public MethodBlock getMethodBlock(MethodKey key) throws TaskInitException {
	MethodMap classMethods = this.classMap.get(key.getOwner());
	if (classMethods == null) {
	    //not in cache
	    classMethods = new MethodMap(this
		    .constructClassNode(key.getOwner()));
	    this.classMap.put(key.getOwner(), classMethods);
	}
	return classMethods.getNode(key);
    }

    public ClassNode getCurrClassNode(){
	return this.currClassNode;
    }
    
    public void setFilter(ClassVisitor f){
	this.filer = f;
    }
    
    /**
     * Constructs a cache entry (map of all method blocks of this class) for
     * the given class name.
     * 
     * @param className - name of the class that contains the required method
     * @return
     * @throws TaskInitException - when problems reading class file
     */
    private ClassNode constructClassNode(String className)
	    throws TaskInitException {
	//read the class
	ClassReader classReader;
	try {
	    classReader = new ClassReader(this.fileList
		    .getFileInputStream(className));
	} catch (IOException e) {
	    throw new TaskInitException(e);
	}
//	works as a normal ClassNode, only instead of MethodNodes
	//creates MethodBlocks
	this.currClassNode = new MethodBlockClassNode();
	if(this.filer!=null)
	    classReader.accept(this.filer, ClassReader.SKIP_FRAMES);
	else
	    classReader.accept(this.currClassNode, ClassReader.SKIP_FRAMES);
	
	return this.currClassNode;
    }

    /**
     * Map that contains all method blocks of the class it is constructed for,
     * stored by method key.
     * 
     * @author Elena Axamitova
     * @version 0.1 04.06.2007
     */
    private class MethodMap {
	private HashMap<MethodKey, MethodBlock> methods;

	// TODO find the worst method implementation, not just the first.
	// or find all implementations, hang them on the graph and let the lp
	// solver
	// find the worst (cache).
	
	/**
	 * name of the superclass, needed when resolving inherited methods
	 */
	private String superName;

	// TODO handle interfaces (complex)
	/**
	 * all implemented interfaces
	 */
	private String[] interfaces = new String[3];

	@SuppressWarnings("unchecked")
	MethodMap(ClassNode classNode) {
	    this.superName = classNode.superName;
	    List<String> toArray = classNode.interfaces;
	    this.interfaces = toArray.toArray(this.interfaces);
	    this.methods = new HashMap<MethodKey, MethodBlock>();
	    List<MethodBlock> methodList = classNode.methods;
	    Iterator<MethodBlock> iterator = methodList.iterator();
	    while (iterator.hasNext()) {
		MethodBlock tempNode = iterator.next();
		methods.put(new MethodKey(classNode.name, tempNode.name,
			tempNode.desc), tempNode);
	    }
	}

	/**
	 * Get the method block for the key. If not found, search
	 * superclass.
	 * 
	 * @param key - key of the method needed
	 * @return
	 * @throws TaskInitException
	 */
	MethodBlock getNode(MethodKey key) throws TaskInitException {
	    // methods.containsKey(key);
	    MethodBlock result = methods.get(key);
	    if (result == null) {
		if (this.superName == null) {
		    throw new TaskInitException("Method name: " + key.getName()
			    + " description:" + key.getDecription()
			    + " not found.");
		} else {
		    MethodKey superKey = new MethodKey(this.superName, key
			    .getName(), key.getDecription());
		    result = getMethodBlock(superKey);
		}
	    }
	    return result;
	}
    }
}
