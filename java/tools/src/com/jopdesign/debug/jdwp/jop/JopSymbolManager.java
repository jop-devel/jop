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

package com.jopdesign.debug.jdwp.jop;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.jopdesign.build.AppInfo;
import com.jopdesign.build.JOPizer;
import com.jopdesign.debug.jdwp.SymbolManager;
import com.jopdesign.debug.jdwp.SymbolTable;

/**
 * SymbolManager.java
 * 
 * A class to manage symbolic information related to all classes packed into
 * a JOP build during the execution of the JOPizer class.
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 14:14:58
 *
 */
public class JopSymbolManager extends SymbolManager implements Serializable
{
  //
  private static final long serialVersionUID = 1L;

  private static String JOP_EXTENSION = ".jop";
  private static String SYMBOL_EXTENSION = ".sym";
  
  private List classList;
  
  private AppInfo jopizer;
  
  public JopSymbolManager()
  {
    
  }
    
  public static String createSymbolTableName(String filename)
  {
    
    int index = filename.indexOf(JOP_EXTENSION);
    
    // remove the extension
    if(index > -1)
    {
      filename = filename.substring(0, index);
    }
    
    filename = filename + SYMBOL_EXTENSION;
    return filename;
  }
  
  public static String getOutputFilename(String args[])
  {
    String outFile = "output.jop";
    
    for(int  i=0; i < args.length; i++) {
      if(args[i].equals("-o")) {
        i++;
        outFile = args[i];
        break;
      }
    }
    
    outFile = createSymbolTableName(outFile);
    return outFile;
  }
  
  /**
   * Build the symbol table.
   * 
   * @param jz
   */
  private void buildSymbolTable(AppInfo jz)
  {
    jopizer = jz;
    
    // just in case, remove any remaining data from the table
    clearSymbolTable();
    
    addClassReferences();
  }
  
  /**
   * Add the list of all reachable classes to the table.
   */
  private void addClassReferences()
  {
	  // list of JopClassInfo
    classList = new LinkedList(jopizer.cliMap.values());
    SymbolTable table = getSymbolTable();
    table.setClassList(classList);
  }
  
  /**
   * This application launch JOPizer. After JOPizer run and all symbolic data
   * is available, get it, process to build the symbol table and store to a 
   * file in the disk.
   * 
   * The symbol file may later be used by the debug module. 
   * 
   * @param args
   */  
  public static void main(String[] args) {
    String outFile;
    outFile = getOutputFilename(args);
    
    // run JOPizer
    JOPizer.main(args);
    
    // store it into a file for later usage during debugging ;)
    try
    {
      JopSymbolManager symbolManager = new JopSymbolManager();
      symbolManager.buildSymbolTable(JOPizer.jz);
      symbolManager.storeSymbolTable(outFile);
      System.out.println(" Successfully stored symbol data: " + outFile);
    }
    catch (IOException e)
    {
      System.out.println(" Failure during symbol table storage in file " + outFile);
      e.printStackTrace();
    }
  }
}
