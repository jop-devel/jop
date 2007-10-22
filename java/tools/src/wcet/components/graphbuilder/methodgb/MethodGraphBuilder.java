/**
 * 
 */
package wcet.components.graphbuilder.methodgb;

import java.io.File;

import org.objectweb.asm.ClassVisitor;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.util.FileList;
import wcet.components.graphbuilder.util.FileListPool;
import wcet.components.graphbuilder.util.IFileList;
//import wcet.components.graphbuilder.util.MethodBlockCache;
import wcet.components.graphbuilder.util.ZipFileList;
import wcet.framework.exceptions.InitException;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IDataStoreKeys;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.hierarchy.IHierarchy;

/**
 * @author Elena Axamitova
 * @version 0.1 20.01.2007
 * 
 * Creates graph consisting of method blocks of the methods called in the 
 * analysed application. Calls to methods of the Native class are replaced
 * by the corresponding bytecode instruction. Java methods for bytecodes implemented
 * in java are not included yet.
 */
public class MethodGraphBuilder implements IAnalyserComponent {

    /**
     * Shared data store
     */
    private IDataStore dataStore = null;

    /**
     * file list containing all class files needed
     */
    private IFileList classFileList = null;

    private MBCacheVisitor methodBlockCacheVisitor;

    public MethodGraphBuilder(IDataStore dataStore) {
	this.dataStore = dataStore;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.general.IAnalyserComponent#getOnlyOne()
         */
    public boolean getOnlyOne() {
	return true;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.general.IAnalyserComponent#getOrder()
         */
    public int getOrder() {
	return IGlobalComponentOrder.GRAPH_BUILDER
		+ IGraphBuilderConstants.METHOD_GRAPH_BUILDER;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.general.IAnalyserComponent#init()
         */
    public void init() throws InitException {
	this.initClassFileList();
	this.methodBlockCacheVisitor = new MBCacheVisitor(this.classFileList);
	this.dataStore.storeObject(IGraphBuilderConstants.FILE_LIST_KEY, this.classFileList);
	this.dataStore.storeObject(IGraphBuilderConstants.LAST_MB_CLASS_VISITOR_KEY, this.methodBlockCacheVisitor);
	MethodBlock.setMBCache(this.methodBlockCacheVisitor);
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    public String call() throws Exception {
//	close the method visitor chain
	ClassVisitor filter = (ClassVisitor)this.dataStore.getObject(IGraphBuilderConstants.LAST_MB_CLASS_VISITOR_KEY);
	this.methodBlockCacheVisitor.setFilter(filter);
	//change task name to java internal name conform
	String className = this.dataStore.getTask().replace('.', '/');
	String mainMethodName = this.dataStore.getMainMethodName();
	String mainMethodDescriptor = this.dataStore.getMainMethodDescriptor();
	//create the root method block - read in from class file and store keys (only keys)
	//of all methods called in it - its children
	IHierarchy hierarchy = (IHierarchy) this.dataStore.getObject(IGraphBuilderConstants.HIERARCHY_KEY);
	if(hierarchy != null)
	    MethodBlock.setHierarchy(hierarchy);
	MethodKey rootKey = new MethodKey(
		className,
		//use default values if not else provided
		mainMethodName == null ? IGraphBuilderConstants.MAIN_METHOD_NAME_DEFAULT
			: mainMethodName,
		mainMethodDescriptor == null ? IGraphBuilderConstants.MAIN_METHOD_DESCRIPTOR_DEFAULT
			: mainMethodDescriptor);
	MethodBlock rootBlock = this.methodBlockCacheVisitor.getMethodBlock(rootKey);
	//construct children - create method blocks for stored method keys
	rootBlock.resolve();
	this.dataStore.storeObject(
		IGraphBuilderConstants.METHOD_BLOCK_TREE_ROOT_KEY,
		rootBlock);
	return "+++ The MethodBlockBuilder component ended successfully.+++\n";
    }
    
    private void initClassFileList()throws InitException{
//	if classpath given, construct file list that searches for 
	//class files in the directories on the classpath 
	if (this.dataStore.getClasspath() != null) {
	    //init application classes file lisr
	    FileListPool flPool = new FileListPool();
	    String classPath = this.dataStore.getClasspath()
		    + File.pathSeparator
		    + this.dataStore
			    .getObject(IGraphBuilderConstants.JOP_JDK_CLASSPATH_KEY);
	    FileList classFileList = new FileList(classPath, ".class");
	    classFileList.findAllFiles();
	    flPool.addFileList(classFileList);
	    
	    //init jop system classes file list
	    String jopSystemPath = (String)this.dataStore.getObject(IGraphBuilderConstants.JOP_SYSTEM_CLASSPATH_KEY);
	    FileList jopSystemFileList = new FileList(jopSystemPath, ".class");
	    jopSystemFileList.findAllFiles();
	    flPool.addFileList(jopSystemFileList);
	    this.classFileList = flPool;
	} else {
	    //else if zip file path given, create ZipFileList for the zip file path
	    String zipPath = (String) this.dataStore
		    .getObject(IDataStoreKeys.ZIP_CLASSFILE_KEY);
	    if (zipPath != null) {
		this.classFileList = new ZipFileList(zipPath, ".class");
	    } else {
		//as a last resort try to find the zip file in the location where
		//jop design flow saves the input for JOPizer - default
		String jopHome = (String)this.dataStore.getObject(IGraphBuilderConstants.JOP_HOME_KEY);
		if (!jopHome.endsWith("/"))
		    jopHome+="/";
		String zipPathDefault = jopHome + IGraphBuilderConstants.ZIP_CLASSES_DEFAULT_REL_PATH;
		zipPathDefault.replace('\\', '/');
		this.classFileList = new ZipFileList(zipPathDefault, ".class");
	    }
	}
    }

}
