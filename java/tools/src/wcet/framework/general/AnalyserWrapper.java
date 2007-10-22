/**
 * 
 */
package wcet.framework.general;

import java.util.Properties;

import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.TaskException;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.interfaces.general.IAnalyser;
import wcet.framework.interfaces.general.IDataStoreKeys;
import wcet.framework.util.ClassLoaderSingleton;

/**
 * @author Elena Axamitova
 * @version 0.2
 */
public final class AnalyserWrapper implements IAnalyser {
    private String analyserClass = null;

    private String libraryPath = null;

    private IAnalyser analyser = null;

    public AnalyserWrapper(String analyserClass) {
	this.analyserClass = analyserClass;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.general.IAnalyser#analyse()
         */
    public void analyse() throws TaskException {
	if (analyser != null) {
	    this.analyser.analyse();

	} else {
	    throw new TaskInitException("AnalyserWrapper: Not initialized yet!");
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.general.IAnalyser#init()
         */
    public void init(Properties arguments) throws InitException {
	this.libraryPath = arguments
		.getProperty(IDataStoreKeys.LIBRARYPATH_KEY);
	try {
	    this.loadJars();
	    Class clazz = ClassLoaderSingleton.getInstance().loadClass(
		    this.analyserClass);
	    this.analyser = (IAnalyser) clazz.newInstance();
	    this.analyser.init(arguments);
	} catch (Exception e) {
	    throw new InitException(e);
	}

    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.general.IAnalyser#setTask(String mainClass)
         */
    public void setTask(String mainClass) throws TaskInitException {
	this.analyser.setTask(mainClass);
    }

    private void loadJars() throws InitException {
	if (ClassLoaderSingleton.getInstance() == null) {
	    ClassLoaderSingleton.addLibrary(this.libraryPath);
	}
    }
}
