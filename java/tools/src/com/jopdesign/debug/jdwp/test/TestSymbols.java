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

package com.jopdesign.debug.jdwp.test;

import java.io.IOException;

import com.jopdesign.debug.jdwp.SymbolTable;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.handler.JDWPException;
import com.jopdesign.debug.jdwp.jop.JopSymbolManager;
import com.jopdesign.debug.jdwp.model.GenericReferenceData;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.LineTable;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.model.ReferenceTypeList;

/**
 * TestSymbols.java
 * 
 * 
 * @author Paulo Guedes
 * 28/05/2007 - 17:19:21
 * 
 */
public class TestSymbols
{
  public static JopSymbolManager createManager() throws IOException, ClassNotFoundException
  {
    System.out.println("  Test symbol file");
    
    String filename;
    filename = JDWPConstants.DEFAULT_SYMBOL_FILE;
    
    JopSymbolManager manager = new JopSymbolManager();
    manager.loadSymbolTable(filename);
    
    System.out.println("  Table loaded.");
    
    SymbolTable symbolTable = manager.getSymbolTable();
    
    ReferenceTypeList list = symbolTable.getAllReferenceTypes();
    System.out.print("  Number of classes available: ");
    System.out.println(list.size());
    
    testCountLines(manager);
    
    return manager;
  }
  
  /**
   * Count the number of lines inside all line tables for all methods
   * inside all classes which may be used by the application.
   * 
   * @param manager
   */
  private static void testCountLines(JopSymbolManager manager)
  {
    ReferenceTypeList list = manager.getAllReferenceTypes();
    int total = 0;
    int typeCounter = 0;
    for(int index = 0; index < list.size(); index++)
    {
      int typeId, methodId;
      
      ReferenceType referenceType = list.get(index);
      typeId = referenceType.getTypeID();
      
      GenericReferenceDataList methods = manager.getMethodReferenceList(typeId);
      String sourceFile = manager.getSourceFile(typeId);
      typeCounter = 0;
      
      for(int methodIndex = 0; methodIndex < methods.size(); methodIndex++)
      {
        GenericReferenceData data = methods.get(methodIndex);
        methodId = data.getFieldOrMethodId();
        
        LineTable lineTable;
        try
        {
          lineTable = manager.getLineTable(typeId, methodId);
          typeCounter = typeCounter + lineTable.numLines();
          
//          System.out.println("");
        }
        catch(JDWPException exception)
        {
          System.out.println("Failure: " + exception.getMessage());
          exception.printStackTrace();
        }
      }
      System.out.print("Source file: ");
      System.out.print(sourceFile);
      System.out.print("  ");
      System.out.print(typeCounter);
      System.out.println(" lines");
      
      total = total + typeCounter;
    }
    System.out.println();
    System.out.println("----------------------------------------");
    System.out.println("Lines: " + total);
  }
  
  public static void main(String[] args) throws IOException, ClassNotFoundException
  {
    JopSymbolManager manager = createManager();
    System.out.println("  Symbols loaded successfully.");
    System.out.println(manager.toString());
  }
}
