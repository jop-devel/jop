package wcet.components.graphbuilder;

public interface IGraphBuilderConstants {

    /**
     * internal order of this graph builder hierarchy constructor 
     */
    public static final int HIERARCHY_CONSTRUCTOR = 2;

    /**
         * internal order of this graph builder component method graph builder
         */
    public static final int METHOD_GRAPH_BUILDER = 1;

    /**
         * internal order of this graph builder component graph writer
         */
    public static final int GRAPH_WRITER = 5;

    // Key String for data store
    /**
         * the key of the last class visitor in the chain
         */
    public static final String LAST_BB_CLASS_VISITOR_KEY = "GraphBuilder.BasicBlock_CVisitor";

    /**
         * the key of the last method visitor in the chain
         */
    public static final String LAST_MB_CLASS_VISITOR_KEY = "GraphBuilder.MethodBlock_CVisitor";

    /**
         * the key of the instruction generator for the GraphWriter
         */
    public static final String INSTRUCTION_GENERATOR_KEY = "GraphBuilder.InsnGenerator";

    /**
         * the root element of the method block tree
         */
    public static final String METHOD_BLOCK_TREE_ROOT_KEY = "GraphBilder.MethodBlockTreeRoot";

    /**
         * path to the jop system class files, esp. JVM, JVMHelp, Native, ...
         */
    public static final String JOP_SYSTEM_CLASSPATH_KEY = "GraphBuilder.JOPSystemClassPath";

    /**
         * path to the jop jdk library class files
         */
    public static final String JOP_JDK_CLASSPATH_KEY = "GraphBuilder.JOPJDKClassPath";

    /**
         * path to the jop main directory
         */
    public static final String JOP_HOME_KEY = "GraphBuilder.JOPHome";

    /**
         * path to the config file for jop instructions
         */
    public static final String JOP_CONFIG_FILE_KEY = "GraphBuilder.ConfigFile";

    /**
         * file list key
         */
    public static final String FILE_LIST_KEY = "GraphBuilder.FileList";

    public static final String HIERARCHY_KEY = "GraphBuilder.Hierarchy";

    
    /**
         * jop system sources path, relative to jop main directory
         */
    public static final String JOP_SYSTEM_REL_SOURCEPATH = "java/target/src/common/com/jopdesign/sys";

    /**
         * jop jdk base library sources path, relative to jop main directory
         */
    public static final String JOP_JDK_BASE_REL_SOURCEPATH = "java/target/src/jdk_base";

    /**
         * jop jdk 1.4 library surces path, relative to jop main directory
         */
    public static final String JOP_JDK_14_REL_SOURCEPATH = "java/target/src/jdk14";

    /**
         * jop jdk 1.1 library surces path, relative to jop main directory
         */
    public static final String JOP_JDK_11_REL_SOURCEPATH = "java/target/src/jdk11";

    /**
         * internal name of the Native class
         */
    public static final String ZIP_CLASSES_DEFAULT_REL_PATH = "java/target/dist/lib/classes.zip";

    public static final String JOP_SYSTEM_PACKAGE_NAME = "com/jopdesign/sys/";

    /**
         * internal name of the JVM class
         */
    public static final String JVMCLASS_NAME = "JVM";

    /**
         * internal name of the Native class
         */
    public static final String NATIVECLASS_NAME = "Native";

    /**
         * loop annotation name for wca comment annotation
         */
    public static final String WCET_LOOP_ANNOTATION_STRING = "WCA loop";

    /**
         * the default value for the main method name
         */
    public static final String MAIN_METHOD_NAME_DEFAULT = "main";

    /**
         * the default value for the main method descriptor
         */
    public static final String MAIN_METHOD_DESCRIPTOR_DEFAULT = "([Ljava/lang/String;)V";

    /**
         * capacity of the class cache
         */
    public static final int CAPACITY = 20;

}
