/**
 * 
 */
package wcet.components.graphbuilder.util;

import java.io.InputStream;
import java.util.HashSet;

/**
 * @author Elena Axamitova
 * @version 0.1 04.06.2007
 * 
 * Provides InputStreams for the content of files stored by names.
 */
public interface IFileList {
    /**
     * Get the InputStream of the file FileName 
     * @param fileName - name of the file that is needed
     * @return InputStream of the file fileName
     */
    public InputStream getFileInputStream(String fileName);
    
    public int getSize();
    
    public HashSet<String> getAllFiles();
}
