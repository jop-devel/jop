/**
 * 
 */
package wcet.components.graphbuilder.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import wcet.framework.exceptions.InitException;

/**
 * @author Elena Axamitova
 * @version 0.1 03.06.2007
 * 
 * Provides InputStream for files stored in a zip file.
 */
public class ZipFileList implements IFileList {
    /**
         * zip file containing files of the application
         */
    private ZipFile zipFile;

    /**
         * file extension of the files
         */
    private String extension = null;

    public ZipFileList(String p, String e) throws InitException {
	if ((p != null) && (e != null)) {
	    try {
		this.zipFile = new ZipFile(p);
	    } catch (IOException e1) {
		throw new InitException(e1);
	    }
	    this.extension = e;
	} else {
	    throw new InitException(
		    "ZipFileList: Path or file extension to search for not specified.");
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.util.IFileList#getFileInputStream(java.lang.String)
         */
    public InputStream getFileInputStream(String fileName) {
	String namePlusExt = fileName + this.extension;
	// complete file name provided
	ZipEntry zipEntry = this.zipFile.getEntry(namePlusExt);
	if (zipEntry == null)
	    // only part of the fileName given, find
	    // zip entry that matches
	    zipEntry = this.searchFor(fileName);
	if (zipEntry == null)
	    return null;
	else
	    try {
		return this.zipFile.getInputStream(zipEntry);
	    } catch (IOException e) {
		return null;
	    }
    }

    /* P R I V A T E M E T H O D S */
    /**
         * Search for a file whose name ends with the given fileName
         * 
         * @param fileName -
         *                name of the file to search for
         * @return - the corresponding zip entry, null when not found
         */
    private ZipEntry searchFor(String fileName) {
	Enumeration<? extends ZipEntry> entries = zipFile.entries();
	while (entries.hasMoreElements()) {
	    ZipEntry entry = entries.nextElement();
	    if (entry.getName().endsWith(fileName + this.extension))
		return entry;
	}
	return null;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.util.IFileList#getAllFiles()
         */
    public HashSet<String> getAllFiles() {
	HashSet<String> result = new HashSet<String>();
	for (Enumeration<? extends ZipEntry> enumeration = this.zipFile
		.entries(); enumeration.hasMoreElements();) {
	    result.add(enumeration.nextElement().getName());
	}
	return result;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.util.IFileList#getSize()
         */
    public int getSize() {
	return this.zipFile.size();
    }
}
