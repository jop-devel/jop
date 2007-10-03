/*******************************************************************************

    An implementation of the Java Debug Wire Protocol (JDWP) for JOP
    Copyright (C) 2007 Paulo Abadie Guedes

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
    
*******************************************************************************/

package com.jopdesign.debug.jdwp.bcel;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * FileList.java
 * 
 * A list of File ojects.
 * 
 * @author Paulo Abadie Guedes
 *
 * 11/07/2007 - 13:51:38
 * 
 */
public class FileList
{
  private List list;
  
  public FileList()
  {
    list = new Vector();
  }
  
  public FileList(File[] elements)
  {
    this();
    add(elements);
  }
  
  public void add(File[] elements)
  {
    int index;
    
    if(elements != null)
    {
      int size = elements.length;
      for (index = 0; index < size; index++)
      {
        add(elements[index]);
      }
    }
  }
  
  public void add(File object)
  {
    if(object != null)
    {
      list.add(object);
    }
  }
  
  public File get(int index)
  {
    return (File) list.get(index);
  }
  
  public int size()
  {
    return list.size();
  }
  
  public void removeAll(File[] list)
  {
    for(int i = 0; i < list.length; i++)
    {
      remove(list[i]);
    }
  }

  public void removeAll(FileList list)
  {
    int size = list.size();
    for(int i = 0; i < size; i++)
    {
      remove(list.get(i));
    }
  }
  
  public File[] toFileArray()
  {
    int index;
    int size = size();
    File[] result = new File[size];
    
    for (index = 0; index < size; index++)
    {
      result[index] = get(index);
    }
    
    return result;
  }
  
  public void remove(int index)
  {
    if((index >= 0) && (index < size()))
    {
      list.remove(index);
    }
  }
  
  public void remove(File object)
  {
    list.remove(object);
  }
  
  /**
   * Create a list of File objects adding only those which are 
   * not folders and that are inside the given folder.
   *  
   * @param rootFolder
   * @return
   */
  public static FileList createFileList(File rootFolder)
  {
    FileList list = new FileList();
    list.addAllSubFiles(rootFolder);
    return list;
  }
  
  public static FileList createClassFileList(String rootFolder)
  {
    File root = new File(rootFolder);
    return createClassFileList(root);
  }
  
  public static FileList createClassFileList(File rootFolder)
  {
    FileList list = new FileList();
    list.addSubFiles(rootFolder, ".*.class");
    return list;
  }
  
  public void addAllSubFiles(File file)
  {
    addSubFiles(file, ".*");
  }
  
  public void addSubFiles(File file, String filter)
  {
    int index, length;
    File[] fileArray;
    File currentFile;
    
    if(file.isDirectory())
    {
      // get the current folder, list all files and modify them
      fileArray = file.listFiles();
      length = fileArray.length;
      
      for(index = 0; index < length; index++)
      {
        currentFile = fileArray[index];
        addSubFiles(currentFile, filter);
      }
    }
    else
    {
      if(file.getName().matches(filter))
      {
        add(file);
      }
    }
  }
  
  /**
   * Create a new list with only the objects that end with the
   * given String.
   * 
   * @param extension
   * @return
   */
  public FileList filterFilesByExtension(String extension)
  {
    return filterList(".*\\." + extension);
  }
  
  public FileList filterList(String regularExpression)
  {
    
    int index, length;
    FileList fileList;
    File file;
    
    length = size();
    fileList = new FileList();
    for(index = 0; index < length; index++)
    {
      file = get(index);
      if(file.getName().matches(regularExpression))
      {
        fileList.add(file);
      }
    }
    
    return fileList;
  }
  
  public String toString()
  {
    return list.toString();
  }
}
