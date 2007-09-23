/**
 * 
 */
package wcet.components.graphbuilder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import wcet.framework.exceptions.InitException;

/**
 * @author Elena Axamitova
 * @version 0.3 09.01.2007
 * 
 * Provides InputStream for files stored in a file system.
 */
public class FileList implements IFileList {

    /**
         * map containing files stored by file names
         */
    private HashMap<String, String> files = null;

    /**
         * path to look for files
         */
    private String path = null;

    /**
         * extension of the files to look for
         */
    private String extension = null;

    /**
         * Construct a new FileList.
         * 
         * @param p -
         *                path to look for files
         * @param e -
         *                extension of the files needed
         * @throws InitException
         */
    public FileList(String p, String e) throws InitException {
	if ((p != null) && (e != null)) {
	    this.path = p;
	    this.extension = e;
	    this.files = new HashMap<String, String>();
	} else {
	    throw new InitException(
		    "GB:FileList: Path or file extension to search for not specified.");
	}
    }

    /**
         * Find all files with the provided extension in the path given.
         */
    public void findAllFiles() {
	StringTokenizer stClass = new StringTokenizer(this.path,
		File.pathSeparator);
	while (stClass.hasMoreTokens()) {// "java/target/src/common";
	    this.findAllFiles(new File(stClass.nextToken()));
	}
    }

    /**
         * Get whole path of the file fileName
         * 
         * @param fileName -
         *                name of the file
         * @return whole path to the file fileName
         */
    public String getFilePath(String fileName) {
	int idx = fileName.lastIndexOf('/');
	// if(idx==-1){
	// idx=0;// fileName.length();
	// }
	String className = fileName.substring(idx + 1, fileName.length());
	String paths = this.files.get(className);
	if (paths == null)
	    return null;
	StringTokenizer tokenizer = new StringTokenizer(paths,
		File.pathSeparator);
	while (tokenizer.hasMoreTokens()) {
	    String path = tokenizer.nextToken();
	    if (this.getNameWithoutSuffix(path).endsWith(fileName))
		return path;
	}
	return null;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.util.IFileList#getFileInputStream(java.lang.String)
         */
    public InputStream getFileInputStream(String fileName) {
	String path = this.getFilePath(fileName);
	if (path == null)
	    return null;
	else
	    try {
		return new FileInputStream(path);
	    } catch (FileNotFoundException e) {
		return null;
	    }
    }

    /* P R I V A T E M E T H O D S */
    /**
         * Find all files in the parent file given
         * 
         * @param item -
         *                File(directory) to search in
         */
    private void findAllFiles(File item) {
	if (item.isDirectory()) {
	    String[] children = item.list();
	    for (int i = 0; i < children.length; i++) {
		this.findAllFiles(new File(item, children[i]));
	    }
	} else {
	    String filePath = item.getAbsolutePath();
	    String fileName = item.getName();
	    if (fileName.endsWith(this.extension)) {
		String oldPath = this.files.get(fileName);
		if (oldPath != null) {
		    filePath = oldPath + File.pathSeparator + filePath;
		}
		fileName = this.getNameWithoutSuffix(fileName);
		// System.out.println(this.extension+" files: Key " + fileName +
                // " Path:"
		// + filePath);
		files.put(fileName, filePath);
	    }
	}
    }

    /**
         * Remove file name suffix
         * 
         * @param fileName
         * @return
         */
    private String getNameWithoutSuffix(String fileName) {
	fileName = fileName.replace('\\', '/');
	return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.util.IFileList#getAllFiles()
     */
    public HashSet<String> getAllFiles() {
	return new HashSet<String>(this.files.keySet());
    }

    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.util.IFileList#getSize()
     */
    public int getSize() {
	return this.files.size();
    }

}
