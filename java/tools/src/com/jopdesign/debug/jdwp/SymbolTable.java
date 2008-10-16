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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.JOPizer;
import com.jopdesign.build.JopMethodInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.debug.jdwp.constants.ErrorConstants;
import com.jopdesign.debug.jdwp.constants.TypeTag;
import com.jopdesign.debug.jdwp.handler.JDWPException;
import com.jopdesign.debug.jdwp.model.GenericReferenceData;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.Line;
import com.jopdesign.debug.jdwp.model.LineTable;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.model.ReferenceTypeList;
import com.jopdesign.debug.jdwp.model.Variable;
import com.jopdesign.debug.jdwp.model.VariableTable;

/**
 * SymbolTable.java
 * 
 * A table to store all symbols needed for debugging.
 * 
 * @author Paulo Abadie Guedes
 * 28/05/2007 - 14:45:41
 * 
 */
public class SymbolTable implements Serializable
{
  // Class ID's:
  // Class ID's are the class index into the class array. In sequence.
  //
  // Method ID's:
  // Method ID's are the method locations inside memory after compilation.
  // ID's are not in sequence. So, many ID's point just to "null" methods.
  // Method ID's are unique among all methods and does not need class ID's
  // to uniquely identify a method.
  //
  // Field ID's: (class and instance)
  // Field ID's are the field index inside classes. This include static
  // and instance fields. 
  // Field ID's are not unique among all classes and need class ID's
  // to identify a field inside a class.
  // Local fields does not belong to a class and are not identified 
  // by this kind of index.
  //
  // Local field ID's:
  // Local field ID's are the field index inside stack frames. They are not
  // unique among frames and need a frame ID to identify a field inside a frame.
  //
  // Frame ID's:
  // Frame ID's are the frame index in the call stack for each Thread 
  // (currently only one). The first ID start at zero (frame of method "main").
  // Used with local field ID's to access local fields inside frames.
  //
  
  private static final long serialVersionUID = 1L;
  
  // a list of the possible types in the classpath  
  private ReferenceTypeList referenceTypeList;
  
  // TODO: make this more specific and independent from other classes
  private List classList;
  
  private MethodTable methodTable;
  
  // the ID for the "java.lang.Object" class
  private int objectId;
  
  public SymbolTable()
  {
    clear();
  }
  
  public void clear()
  {
    referenceTypeList = new ReferenceTypeList();
    classList = new Vector();
    methodTable = new MethodTable();
  }
  
  public ReferenceTypeList getReferenceTypeList(String signature)
  {
    ReferenceTypeList subList = referenceTypeList.getClassesMatching(signature);
    return subList;
  }
  
  public ReferenceTypeList getAllReferenceTypes()
  {
    ReferenceTypeList list = referenceTypeList.copy();
    return list;
  }

  public void setClassList(List classList)
  {
    this.classList = classList;
    
    fillReferenceTypeList();
  }

  
  // IMPROVE: methods below may create unnecessary dependencies. Refactor later.
  // maybe it's a good idea to use BCEL classes in other places... don't know yet.
  private void fillReferenceTypeList()
  {
    Iterator iterator = classList.iterator();
    
    // a counter to create ID's for all known types.
    // This approach will allow to get the reference 
    // object directly from the list using its own index.
    int referenceTypeID = 0;
    while (iterator.hasNext())
    {
      ClassInfo element = (ClassInfo) iterator.next();
      
//      System.out.println("Element: " + element);
      
      JavaClass javaClass;
      javaClass = element.clazz;
      
      // add all methods to the method table
      registerAllMethods(element);
      
      String typeName = javaClass.getClassName();
      if (JOPizer.objectClass.equals(typeName))
      {
        // this is the Object class. Keep its ID to be used later.
        objectId = referenceTypeID;
      }
      
      System.out.println("  Class: " + typeName);
      
      ReferenceType type = new ReferenceType(typeName);
      
      int modifiers = javaClass.getModifiers();
      type.setModifiers(modifiers);
      
      String signature = javaClass.getClassName();
      type.setTypeSignature(signature);
      
      setTypeTag(javaClass, type);
      
      type.setTypeID(referenceTypeID);
      referenceTypeID++;
      
      // tag constant does not belong to class description. Comment?
//      byte tagConstant = javaClass.is
//      type.setTagConstant(tagConstant);
      referenceTypeList.add(type);
    }
  }
  
  /**
   * @param javaClass
   */
  private void registerAllMethods(ClassInfo classInfo)
  {
    List methods = classInfo.getMethods();
    JopMethodInfo methodInfo;
    
    for(Iterator iter = methods.iterator(); iter.hasNext();)
    {
      methodInfo = (JopMethodInfo) iter.next();
      methodTable.addMethod(methodInfo);
    }
  }
  
  private JopMethodInfo getMethodInfo(int methodId)
  {
    return methodTable.getMethod(methodId);
  }
  
  /**
   * @param javaClass
   * @param type
   */
  private void setTypeTag(JavaClass javaClass, ReferenceType type)
  {
    byte typeTag = TypeTag.CLASS;
    
    if(javaClass.isClass())
    {
      typeTag = TypeTag.CLASS;
    }
    if(javaClass.isInterface())
    {
      typeTag = TypeTag.INTERFACE;
    }
    // is it possible to have an "ARRAY" type tag? 
    // JDWP spec says it's possible, but in JOP all
    // array manipulation seems to be using bytecodes directly,
    // so it's not possible to set a class as "ARRAY".  
    // If another Java machine uses a class to model arrays,
    // uncomment the code below and implement a way to check
    // if it's an array.
//      if(isArray(javaClass))
//      {
//        typeTag = TypeTag.ARRAY;
//      }
    type.setTypeTag(typeTag);
  }

  /**
   * @param referenceTypeId
   * @return
   */
  public ReferenceType getReferenceType(int referenceTypeId)
  {
    ReferenceType referenceType = null;
    
    if(isValidTypeId(referenceTypeId))
    {
      referenceType = referenceTypeList.get(referenceTypeId);
    }
    
    return referenceType;
  }

  /**
   * Check if a type ID is valid.
   * 
   * @param id
   * @return
   */
  public boolean isValidTypeId(int id)
  {
    // All ID's need to be are between 0 (included) 
   // and the list size (excluded).
    return (id >= 0) && (id < referenceTypeList.size());
  }
  
  private ClassInfo getClassInfo(String className)
  {
    int id = getClassId(className);
    return getClassInfo(id);
  }
  
  private ClassInfo getClassInfo(int id)
  {
    ClassInfo result = null;
    
    if(isValidTypeId(id))
    {
      result = (ClassInfo) classList.get(id);
    }
    
    return result;
  }
  
  public GenericReferenceDataList getMethodList(int classId)
  {
    GenericReferenceDataList list = new GenericReferenceDataList();
    
    ClassInfo classInfo = getClassInfo(classId);
    if(classInfo!= null)
    {
      List methods = classInfo.getMethods();
      addMethods(list, methods);
    }
    
    return list;
  }
  
  /**
   * @param list
   * @param methods
   */
  private void addMethods(GenericReferenceDataList list, List methods)
  {
    int methodId;
    JopMethodInfo methodInfo;
    
    for(Iterator iter = methods.iterator(); iter.hasNext();)
    {
      methodInfo = (JopMethodInfo) iter.next();
      
      //method ID will be the address into the method table 
      methodId = getMethodId(methodInfo);
      
      GenericReferenceData data = new GenericReferenceData(methodId);
      
      Method method = methodInfo.getMethod();
      
      String name = method.getName();
      String signature = method.getSignature();
      int accessFlags = method.getAccessFlags();
      
      data.setName(name);
      data.setSignature(signature);
      data.setModifiers(accessFlags);
      
      list.add(data);
    }
  }
  
  private int getMethodId(JopMethodInfo methodInfo)
  {
    return MethodTable.getMethodId(methodInfo);
  }
  
  public int getMethodStructPointer(String className, String methodSignature)
  {
    int methodStructPointer;
    JopMethodInfo methodInfo;
    
    methodInfo = getMethodInfo(className, methodSignature);
    methodStructPointer = methodInfo.getStructAddress();
    
    return methodStructPointer;
  }
  
  /**
   * Return the method size in words. On the Java Machine, 
   * each word has 4 bytes.
   * 
   * @param className
   * @param methodSignature
   * @return
   */
  public int getMethodSizeInWords(String className, String methodSignature)
  {
    int methodSize;
    JopMethodInfo methodInfo;
    
    methodInfo = getMethodInfo(className, methodSignature);
    methodSize = methodInfo.getLength();
    
    return methodSize;
  }
  
  /**
   * Return the method size in words. On the Java Machine, 
   * each word has 4 bytes.
   * 
   * @param methodPointer
   * @return
   */
  public int getMethodSizeInWords(int methodPointer)
  {
    int methodSize;
    JopMethodInfo methodInfo;
    
    methodInfo = getMethodInfo(methodPointer);
    methodSize = methodInfo.getLength();
    
    return methodSize;
  }
  
  public String getSourceFile(int typeId)
  {
    String sourceFile = null;
    
    ClassInfo classInfo = getClassInfo(typeId);
    if(classInfo!= null)
    {
      sourceFile = classInfo.clazz.getFileName();
    }
    
    return sourceFile;
  }

  /**
   * @param typeId
   * @param methodId
   * @return
   * @throws JDWPException 
   */
  public LineTable getLineTable(int typeId, int methodId) throws JDWPException
  {
    LineTable table = null;
    
    MethodInfo methodInfo = getMethodInfo(typeId, methodId);
    table = getLineTable(methodInfo);
    
    return table;
  }
  
  public LineTable getLineTable(String className, String methodSignature)
  {
    MethodInfo methodInfo;
    
    methodInfo = getMethodInfo(className, methodSignature);
    return getLineTable(methodInfo);
  }
  
  /**
   * @param className
   * @param methodSignature
   * @return
   */
  private JopMethodInfo getMethodInfo(String className, String methodSignature)
  {
    ClassInfo info;
    JopMethodInfo methodInfo;
    
    info = getClassInfo(className);
    methodInfo = (JopMethodInfo) info.getMethodInfo(methodSignature);
    
    return methodInfo;
  }

  private MethodInfo getMethodInfo(int typeId, int methodId) throws JDWPException
  {
    MethodInfo methodInfo = null;
    ClassInfo classInfo = getClassInfo(typeId);
    if(classInfo!= null)
    {
      // ok, it's a valid class. Let's get the method.
      methodInfo = getMethodInfo(methodId);
      
      // check if it's an invalid method
      if(methodInfo == null)
      {
        // invalid method ID. Throw a new exception here.
        throw new JDWPException(ErrorConstants.ERROR_INVALID_METHODID);
      }
    }
    else
    {
      // invalid class ID.
      throw new JDWPException(ErrorConstants.ERROR_INVALID_CLASS);
    }
    
    return methodInfo;
  }


  /**
   * @param methodInfo
   * @return
   */
  private LineTable getLineTable(MethodInfo methodInfo)
  {
    // well... now I realized that classes LineTable and Line 
    // are not essential :( BCEL classes could be used to do the same job.
    // Anyway, this may be improved later.
    
    int index, size;
    LineTable lineTable = new LineTable();
    
    Method method = methodInfo.getMethod();
    LineNumberTable lineNumberTable;
    LineNumber[] lineNumberArray;
    LineNumber lineNumber;
    
    lineNumberTable = method.getLineNumberTable();
    if(lineNumberTable != null)
    {
      lineNumberArray = lineNumberTable.getLineNumberTable();
      
      int start = 0;
      int end = 0;
      
      size = lineNumberArray.length;
      for(index = 0; index < size; index++)
      {
        lineNumber = lineNumberArray[index];
        Line line = new Line(lineNumber.getStartPC(), lineNumber.getLineNumber());
        
        lineTable.addLine(line);
        
        if(index == 0)
        {
          start = lineNumber.getStartPC();
        }
        if(index == (size - 1))
        {
          end = lineNumber.getStartPC();
        }
      }
      
      lineTable.setStart(start);
      lineTable.setEnd(end);
    }
    
    return lineTable;
  }

  /**
   * @param typeId
   * @param methodId
   * @return
   * @throws JDWPException 
   */
  public VariableTable getVariableTable(int typeId, int methodId) throws JDWPException
  {
    VariableTable table = null;
    
    MethodInfo methodInfo = getMethodInfo(typeId, methodId);
    table = getVariableTable(methodInfo);
    
    return table;
  }

  /**
   * @param methodInfo
   * @return
   */
  private VariableTable getVariableTable(MethodInfo methodInfo)
  {
    int index, size;
    VariableTable variableTable = new VariableTable();
    
    Method method = methodInfo.getMethod();
    LocalVariableTable localVariableTable;
    LocalVariable[] localVariableArray;
    LocalVariable localVariable;
    
    localVariableTable = method.getLocalVariableTable();
    if(localVariableTable != null)
    {
      localVariableArray = localVariableTable.getLocalVariableTable();
      
      // calculate the number of words needed by the method arguments
      int argCnt = 0;
      Type[] typeArray;
      Type type;
      
      typeArray = method.getArgumentTypes();
      size = typeArray.length;
      for(index = 0; index < typeArray.length; index++)
      {
        type = typeArray[index];
        argCnt = argCnt + type.getSize();
      }
      variableTable.setArgCnt(argCnt);
      
      // calculate the number of local variables inside a frame
      size = localVariableArray .length;
      variableTable.setSlots(size);
      
      // calculate information about each local variable
      for(index = 0; index < size; index++)
      {
        localVariable = localVariableArray[index];
        Variable variable = new Variable();
        
        variable.setCodeIndex(localVariable.getStartPC());
        variable.setName(localVariable.getName());
        variable.setSignature(localVariable.getSignature());
        variable.setLength(localVariable.getLength());
        variable.setSlot(localVariable.getIndex());
        
        variableTable.addVariable(variable);
      }
    }
    
    return variableTable;
  }

  /**
   * @param classId
   * @return
   */
  public GenericReferenceDataList getFieldList(int classId)
  {
    int index, size;
    GenericReferenceDataList list = new GenericReferenceDataList();
    
    ClassInfo classInfo = getClassInfo(classId);
    if(classInfo!= null)
    {
      Field[] fieldArray = classInfo.clazz.getFields();
      
      size = fieldArray.length;
      for(index = 0; index < size; index++)
      {
//      Field field;
//        field = fieldArray[index];
//        // use the field index inside the class as the ID
//        GenericReferenceData data = createFieldReferenceData(index, field);
        
        // create this type of object in one place only. 
        // easier to change if needed.
        GenericReferenceData data = getField(classId, index);
        list.add(data);
      }
    }
    
    return list;
  }
  
  /**
   * @param index
   * @param field
   * @return
   */
  private GenericReferenceData createFieldReferenceData(int fieldId, Field field)
  {
    String name;
    String signature;
    int accessFlags;
    
    GenericReferenceData data = new GenericReferenceData(fieldId);
    
    name = field.getName();
    data.setName(name);
    
    signature = field.getSignature();
    data.setSignature(signature);
    
    accessFlags = field.getAccessFlags();
    data.setModifiers(accessFlags);
    
    return data;
  }
  
  /**
   * @param classId
   * @return
   */
  public GenericReferenceData getField(int classId, int fieldId)
  {
    int size;
    GenericReferenceData data = null;
    
    ClassInfo classInfo;
    
    data = null;
    classInfo = getClassInfo(classId);
    if(classInfo!= null)
    {
      Field[] fieldArray = classInfo.clazz.getFields();
      Field field;
      
      size = fieldArray.length;
      if((fieldId >= 0) && (fieldId < size))
      {
        field = fieldArray[fieldId];
        data = createFieldReferenceData(fieldId, field);
      }
    }
    
    return data;
  }
  
  public int getFieldAddress(int classId, int fieldId)
  {
    int address = 0;
    
    ClassInfo classInfo = getClassInfo(classId);
    if(classInfo!= null)
    {
      
    }
    
    return address;
  }
  
  /**
   * @param classId
   * @return
   */
  public int getSuperClass(int classId)
  {
    ClassInfo classInfo = getClassInfo(classId);
    classInfo = (ClassInfo) classInfo.superClass;
    
    return getClassId(classInfo);
  }

  /**
   * @param classInfo
   * @return
   */
  private int getClassId(ClassInfo classInfo)
  {
    int index, id, size;
    
    id = 0;
    size = getNumClasses();
    for(index = 0; index < size; index++)
    {
      ClassInfo info = getClassInfo(index);
      if(classInfo == info)
      {
        id = index;
        break;
      }
    }
    
    return id;
  }
  
  public int getClassId(String className)
  {
    int index, id, size;
    
    id = 0;
    size = getNumClasses();
    for(index = 0; index < size; index++)
    {
      ClassInfo info = getClassInfo(index);
      if(className.equals(info.clazz.getClassName()))
      {
        id = index;
        break;
      }
    }
    
    return id;
  }
  /**
   * @return
   */
  private int getNumClasses()
  {
    return classList.size();
  }

  /**
   * @param classId
   * @return
   */
  public boolean isObjectClassId(int classId)
  {
    boolean result = false;
    
    if(isValidTypeId(classId))
    {
      result = (classId == objectId);
    }
    
    return result;
  }

  /**
   * @param typeId
   * @param methodId
   * @return
   * @throws JDWPException 
   */
  public byte[] getBytecodes(int typeId, int methodId) throws JDWPException
  {
    byte[] bytecodes = null;
    
    MethodInfo methodInfo = getMethodInfo(typeId, methodId);
    Code code = methodInfo.getCode();
    bytecodes = code.getCode();
    
    return bytecodes;
  }
  
  public String toString()
  {
    int index, size;
    StringBuffer buffer = new StringBuffer();
    
    buffer.append("[");
    size = getNumClasses();
    for(index = 0; index < size; index++)
    {
      ClassInfo classInfo = getClassInfo(index);
      buffer.append(classInfo.clazz.getClassName());
      if(index < (size - 1))
      {
        buffer.append(", ");
      }
    }
    buffer.append("]");
    
    return buffer.toString();
  }

  /**
   * @param methodPointer
   * @return
   */
  public boolean isStaticOrNative(int methodPointer)
  {
    MethodInfo methodInfo = getMethodInfo(methodPointer);
    Method method = methodInfo.getMethod();
    
    return (method.isStatic() || method.isNative());
  }
  
  /**
   * @param classId
   * @return
   */
  public boolean isValidFieldId(int classId, int fieldId)
  {
    boolean result = false;
    Field field;
    
    field = getBCELField(classId, fieldId);
    result = (field != null);
    
    return result;
  }
  
  /**
   * @param classId
   * @return
   */
  public boolean isValidStaticFieldId(int classId, int fieldId)
  {
    boolean result = false;
    Field field = getBCELField(classId, fieldId);
    
    if(field != null)
    {
      result = field.isStatic();
    }
    
    return result;
  }
  
  /**
   * Return a Field object from a class.
   * @param classId
   * @param fieldId
   * @return
   */
  private Field getBCELField(int classId, int fieldId)
  {
    Field field = null;
    
    ClassInfo classInfo = getClassInfo(classId);
    if(classInfo!= null)
    {
      Field[] fieldArray = classInfo.clazz.getFields();
      if((0 >= fieldId) && (fieldId < fieldArray.length))
      {
        field = fieldArray[fieldId];
      }
    }
    
    return field;
  }

  /**
   * @param typeId
   * @return
   */
  public GenericReferenceDataList getNestedTypesList(int classId)
  {
    // TODO: need to be tested
    int index, size;
    ClassInfo classInfo = getClassInfo(classId);
    ConstantPool constantPool = classInfo.clazz.getConstantPool();
    GenericReferenceDataList list = new GenericReferenceDataList();
    GenericReferenceData data;
    
    InnerClass[] classes = getInnerClassArray(classId);
    InnerClass innerClass;
    
    size = classes.length;
    for(index = 0; index < size; index++)
    {
      innerClass = classes[index];
      int nameIndex = innerClass.getInnerNameIndex();
      byte tag = Constants.CONSTANT_Class;
      
      String className = constantPool.getConstantString(nameIndex, tag);
      
      int innerClassId = getClassId(className);
      ClassInfo innerClassInfo = getClassInfo(innerClassId);
      
      data = new GenericReferenceData(innerClassId);
      
      // TODO: check if there is any other data which is needed to fill here.
      data.setName(className);
//      data.setSignature(innerClassInfo.clazz.get)
      data.setModifiers(innerClassInfo.clazz.getModifiers());
//      data.setTag(innerClassInfo.clazz.is);
      
      list.add(data);
    }
    
    return list;
  }
  private InnerClass[] getInnerClassArray(int classId)
  {
    int index, size;
    InnerClass[] classes;
    
    classes = new InnerClass[0]; 
    ClassInfo classInfo = getClassInfo(classId);
    
    Attribute[] attributeArray = classInfo.clazz.getAttributes();
    Attribute attribute;
    
    size = attributeArray.length;
    for(index = 0; index < size; index++)
    {
      attribute = attributeArray[index];
      if(attribute instanceof InnerClasses)
      {
        InnerClasses innerClasses = (InnerClasses) attribute;
        classes = innerClasses.getInnerClasses();
        break;
      }
    }
    return classes;
  }

  /**
   * Check if a method pointer is valid or not.
   * 
   * @param methodPointer
   * @return
   */
  public boolean isValidMethodStructurePointer(int methodPointer)
  {
    boolean isValid;
    MethodInfo info;
    
    // check if there is any method registered with this pointer.
    // if it's not, then this is an invalid method pointer.
    info = getMethodInfo(methodPointer);
    isValid = (info != null);
    
    return isValid;
  }
}
