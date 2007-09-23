/**
 * 
 */
package wcet.components.graphbuilder.util;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Elena Axamitova
 * @version 0.1 22.07.2007
 */
public class FileListPool implements IFileList {
    HashSet<IFileList> fileList;
    
    public FileListPool(){
	this.fileList = new HashSet<IFileList>();
    }
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.util.IFileList#getFileInputStream(java.lang.String)
     */
    public InputStream getFileInputStream(String fileName) {
	Iterator<IFileList> iterator = this.fileList.iterator();
	while(iterator.hasNext()){
	    IFileList currFileList = iterator.next();
	    InputStream fileInpStream = currFileList.getFileInputStream(fileName);
	    if(fileInpStream!=null)
		return fileInpStream;
	}
	return null;
    }

    public void addFileList(IFileList fl){
	this.fileList.add(fl);
    }
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.util.IFileList#getAllFiles()
     */
    public HashSet<String> getAllFiles() {
	HashSet<String> result = new HashSet<String>();
	for(Iterator<IFileList> iterator=this.fileList.iterator();iterator.hasNext();){
	    result.addAll(iterator.next().getAllFiles());
	}
	return result;
    }
    /* (non-Javadoc)
     * @see wcet.components.graphbuilder.util.IFileList#getSize()
     */
    public int getSize() {
	int result = 0;
	for(Iterator<IFileList> iterator=this.fileList.iterator();iterator.hasNext();){
	    result += iterator.next().getSize();
	}
	return result;
    }
}
