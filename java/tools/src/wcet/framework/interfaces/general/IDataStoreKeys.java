/*
 * IDataStoreKeys.java, WCETA tool
 */
package wcet.framework.interfaces.general;

/**
 * The interface defining basic keys for object storage and retrieval used by a
 * IDataStore object.
 * 
 * @author Elena Axamitova
 * @version 0.2
 */
public interface IDataStoreKeys {
    /**
         * the classpath (path to the .class files of the application) key
         */
    public static final String CLASSPATH_KEY = "General.Classpath";

    /**
         * the output (name of the file where the general output - debug and
         * progress messages are to be printed) key. If not specified, standard
         * output is used.
         */
    public static final String OUTPUT_KEY = "General.Output";

    /**
         * the graph key
         */
    public static final String GRAPH_KEY = "General.Graph";

    /**
         * the constraints key
         */
    public static final String CONSTRAINTS_KEY = "General.Constraints";

    /**
         * the task key - stores the currently processed task.
         */
    public static final String TASK_KEY = "General.Task";

    /**
         * the sourcepath (path to the .java files of the application) key
         */
    public static final String SOURCEPATH_KEY = "General.Sourcepath";

    /**
         * the main method name key. If not specified 'main' is used.
         */
    public static final String MAINMETHOD_NAME_KEY = "General.MainMethodName";

    /**
         * the main method descriptor key. If not specified
         * '([Ljava/lang/String;)V' - the main method descriptor is used.
         */
    public static final String MAINMETHOD_DESCRIPTOR_KEY = "General.MainMethodDescriptor";

    /**
         * the librarypath (path to the .jar files containing components) key
         */
    public static final String LIBRARYPATH_KEY = "General.LibraryPath";
 
    /**
     * the key of the path to the zip
     */
    public static final String ZIP_CLASSFILE_KEY = "General.ZipClassFile";
    
    /**
         * the keys of object with direct access, the set/get operations in the
         * general area should be ignored.
         */
    public static String[] ignoredKeys = { OUTPUT_KEY, GRAPH_KEY,
	    CONSTRAINTS_KEY };
}
