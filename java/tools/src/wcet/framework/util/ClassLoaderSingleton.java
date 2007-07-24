/**
 * 
 */
package wcet.framework.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

/**
 * @author Elena Axamitova
 * @version 0.3 04.02.2007
 * 
 * Provides the class loader instance for init of analyser components
 */
public class ClassLoaderSingleton {
    static ClassLoader instance = null;

    private ClassLoaderSingleton() {

    }

    public static ClassLoader getInstance() {
	if(instance==null)
		instance = ClassLoader.getSystemClassLoader();
	return instance;
    }

    public static void addLibrary(String path) {
	if(instance==null)
		instance = ClassLoader.getSystemClassLoader();
	if (path != null) {
	    StringTokenizer tokenizer = new StringTokenizer(path,
		    File.pathSeparator);
	    URL[] libraryURLs = new URL[tokenizer.countTokens()];
	    for (int i = 0; tokenizer.hasMoreTokens(); i++) {
		try {
		    String libraryURL = "file:///" + tokenizer.nextToken();
		    libraryURLs[i] = new URL(libraryURL);
		} catch (MalformedURLException e) {
		    // ignore
		}
	    }
	    instance = new URLClassLoader(libraryURLs, instance);
	}
    }
}
