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

package com.jopdesign.debug.jdwp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.jopdesign.debug.jdwp.handler.JDWPException;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.LineTable;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.model.ReferenceTypeList;
import com.jopdesign.debug.jdwp.model.VariableTable;

/**
 * SymbolManager.java
 * 
 * A class to manage symbolic information related to all classes
 * available on the classpath during the machine execution.
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 14:14:58
 *
 */
public class SymbolManager implements Serializable
{
  // 
  private static final long serialVersionUID = 1L;

  public static final String SYMBOL_EXTENSION = ".sym";
  
  // a default name for the symbol table
  private String symbolFile = "symbolTable." + SYMBOL_EXTENSION;
  
  private SymbolTable table;
  
  public SymbolManager()
  {
    table = new SymbolTable();
  }
  
  // TODO: set protected
//  protected SymbolTable getSymbolTable()
  public SymbolTable getSymbolTable()
  {
    return table;
  }
  
  protected void clearSymbolTable()
  {
    table.clear();
  }
  
  // store the state of this symbol manager
  public static void storeSymbols(SymbolManager table, String filename) throws IOException
  {
    FileOutputStream fileOutputStream = new FileOutputStream(filename);
    ObjectOutputStream stream = new ObjectOutputStream(fileOutputStream);
    stream.writeObject(table);
    stream.flush();
    stream.close();
  }
  
  //reload the state of this symbol manager
  public static SymbolManager loadSymbols(String filename) throws IOException, ClassNotFoundException
  {
    SymbolManager table;
    
    FileInputStream file = new FileInputStream(filename);
    ObjectInputStream stream = new ObjectInputStream(file);
    table = (SymbolManager) stream.readObject();
    stream.close();
    
    return table;
  }
    
  public static void storeSymbolTable(SymbolTable table, String filename) throws IOException
  {
    FileOutputStream fileOutputStream = new FileOutputStream(filename);
    ObjectOutputStream stream = new ObjectOutputStream(fileOutputStream);
    stream.writeObject(table);
    stream.flush();
    stream.close();
  }
  
  //reload the state of this symbol manager
  public static SymbolTable loadSymbolTableFromFile(String filename) throws IOException, ClassNotFoundException
  {
    SymbolTable table;
    
    FileInputStream file = new FileInputStream(filename);
    ObjectInputStream stream = new ObjectInputStream(file);
    table = (SymbolTable) stream.readObject();
    stream.close();
    
    return table;
  }
  
  public void loadSymbolTable(String filename) throws IOException, ClassNotFoundException
  {
    this.table = loadSymbolTableFromFile(filename);
  }
  
  public void storeSymbolTable(String filename) throws IOException
  {
    storeSymbolTable(this.table, filename);
  }
  
  public ReferenceTypeList getReferenceTypeList(String signature)
  {
    return table.getReferenceTypeList(signature);
  }
  
  public ReferenceTypeList getAllReferenceTypes()
  {
    return table.getAllReferenceTypes();
  }
  
  public String getSymbolFile()
  {
    return symbolFile;
  }

  public void setSymbolFile(String symbolFile)
  {
    if(symbolFile != null)
    {
      this.symbolFile = symbolFile;
    }
  }

  /**
   * @param referenceType
   * @return
   */
  public ReferenceType getReferenceType(int referenceTypeId)
  {
    return table.getReferenceType(referenceTypeId);
  }
  
  public String getReferenceTypeSignature(int referenceTypeId)
  {
    ReferenceType type = table.getReferenceType(referenceTypeId);
    return type.getTypeSignature();
  }

  /**
   * @param referenceType
   * @return
   */
  public GenericReferenceDataList getMethodReferenceList(int classId)
  {
    return table.getMethodList(classId);
  }

  /**
   * 
   * @param typeId
   * @return
   */
  public String getSourceFile(int typeId)
  {
    return table.getSourceFile(typeId);
  }

  /**
   * 
   * @param typeId
   * @param methodId
   * @return
   * @throws JDWPException
   */
  public LineTable getLineTable(int typeId, int methodId) throws JDWPException
  {
    return table.getLineTable(typeId, methodId);
  }

  /**
   * 
   * @param typeId
   * @param methodId
   * @return
   * @throws JDWPException
   */
  public VariableTable getVariableTable(int typeId, int methodId) throws JDWPException
  {
    return table.getVariableTable(typeId, methodId);
  }

  /**
   * @param i
   * @return
   */
  public GenericReferenceDataList getFieldReferenceList(int classId)
  {
    return table.getFieldList(classId);
  }

  /**
   * @param classId
   * @return
   */
  public int getSuperClass(int classId)
  {
    return table.getSuperClass(classId);
  }

  /**
   * @param i
   * @return
   */
  public boolean isObjectClassId(int classId)
  {
    return table.isObjectClassId(classId);
  }

  /**
   * 
   * @param typeId
   * @param methodId
   * @return
   * @throws JDWPException
   */
  public byte[] getBytecodes(int typeId, int methodId) throws JDWPException
  {
    return table.getBytecodes(typeId, methodId);
  }

  /**
   * @param methodPointer
   * @return
   */
  public boolean isStaticOrNative(int methodPointer)
  {
    return table.isStaticOrNative(methodPointer);
  }

  /**
   * @param i
   * @return
   */
  public boolean isValidTypeId(int classId)
  {
    return table.isValidTypeId(classId);
  }
  
  /**
   * 
   * @param classId
   * @param fieldId
   * @return
   */
  public boolean isValidFieldId(int classId, int fieldId)
  {
    return table.isValidFieldId(classId, fieldId);
  }
  
  /**
   * 
   * @param classId
   * @param fieldId
   * @return
   */
  public boolean isValidStaticFieldId(int classId, int fieldId)
  {
    return table.isValidStaticFieldId(classId, fieldId);
  }
  
  public boolean isValidMethodStructurePointer(int methodPointer)
  {
    return table.isValidMethodStructurePointer(methodPointer);
  }
  
  /**
   * 
   * @param typeId
   * @return
   */
  public GenericReferenceDataList getNestedTypesList(int typeId)
  {
    return table.getNestedTypesList(typeId);
  }
  
  /**
   * Get the method address. 
   * 
   * @param className
   * @param methodSignature
   * @return
   */
  public int getMethodStructPointer(String className, String methodSignature)
  {
    SymbolTable table = getSymbolTable();
    int methodId = table.getMethodStructPointer(className, methodSignature);
    return methodId;
  }
  
  /**
   * Get the line table for the method. 
   * 
   * @param className
   * @param methodSignature
   * @return
   */
  public LineTable getLineTable(String className, String methodSignature)
  {
    SymbolTable table = getSymbolTable();
    LineTable lineTable = table.getLineTable(className, methodSignature);
    return lineTable;
  }
  
  /**
   * Get the method size in words. 
   * 
   * @param className
   * @param methodSignature
   * @return
   */
  public int getMethodSizeInWords(String className, String methodSignature)
  {
    SymbolTable table = getSymbolTable();
    int methodId = table.getMethodSizeInWords(className, methodSignature);
    return methodId;
  }
  
  /**
   * Get the method size in words. If the method pointer is not valid,
   * return -1.
   * 
   * @param methodPointer
   * @return
   */
  public int getMethodSizeInWords(int methodPointer)
  {
    if(isValidMethodStructurePointer(methodPointer))
    {
      SymbolTable table = getSymbolTable();
      int methodId = table.getMethodSizeInWords(methodPointer);
      return methodId;
    }
    else
    {
      return -1;
    }
  }
  
  /**
   * Get the method size in bytes.
   * 
   * @param className
   * @param methodSignature
   * @return
   */
  public int getMethodSizeInBytes(String className, String methodSignature)
  {
    return 4 * getMethodSizeInWords(className, methodSignature);
  }
  
  /**
   * Get the method size in bytes.
   * 
   * @param methodPointer
   * @return
   */
  public int getMethodSizeInBytes(int methodPointer)
  {
    return 4 * getMethodSizeInWords(methodPointer);
  }

  /**
   * Check if a given offset is valid for a method pointer.
   * 
   * @param methodStructPointer
   * @param offset
   * @return
   */
  public boolean isValidInstructionOffset(int methodStructPointer, int offset)
  {
    boolean result = false;
    
    if(isValidMethodStructurePointer(methodStructPointer))
    {
      int size = getMethodSizeInBytes(methodStructPointer);
      if(0 <= offset && offset < size)
      {
        result = true;
      }
    }
    
    return result;
  }
}
