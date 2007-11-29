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

import java.io.IOException;

import com.jopdesign.debug.jdwp.constants.ClassStatusConstants;
import com.jopdesign.debug.jdwp.constants.ErrorConstants;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.constants.TagConstants;
import com.jopdesign.debug.jdwp.constants.ThreadStatusConstants;
import com.jopdesign.debug.jdwp.constants.TypeTag;
import com.jopdesign.debug.jdwp.handler.JDWPException;
import com.jopdesign.debug.jdwp.model.FrameList;
import com.jopdesign.debug.jdwp.model.GenericReferenceData;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.LineTable;
import com.jopdesign.debug.jdwp.model.ObjectReference;
import com.jopdesign.debug.jdwp.model.ObjectReferenceList;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.model.ReferenceTypeList;
import com.jopdesign.debug.jdwp.model.VariableTable;

/**
 * JOPDebugInterface.java
 * 
 * This class provide to the debug framework, services which are related to the
 * JOP processor.
 * 
 * Its role is to send commands to JOP and return the respective answers.
 * 
 * It does that through an instance of JOPDebugInterface.
 * 
 * @author Paulo Abadie Guedes
 * 
 * 16/05/2007 - 19:32:38
 * 
 */
public class JOPDebugInterface
{
  // class to provide symbol management support
  private SymbolManager symbolManager;
  
  // class to request services directly to a JOP machine
  private JOPDebugChannel jopDebugChannel;
  
  // constants related to the thread objects
  // a name and ID to simulate the main threadgroup.
  private static final int DEFAULT_THREAD_GROUP_ID = 16;

  private static final String DEFAULT_THREADGROUP_NAME = "Main threadgroup";

  private static final long NULL_OBJECT_REFERENCE = 0;

  private static final String THREAD_SIGNATURE = "java.lang.Thread";

  private static final String THREAD_NAME = "Main thread";

  // an ID to simulate the main thread. Any value will do here.
  private static final int MAIN_THREAD_ID = 1;

  // an object to simulate the main thread
  private ObjectReference mainThread;
  
  private ReferenceType threadClass;
  
  // a counter to control thread suspension;
  private int threadSuspendCounter;
  
  public JOPDebugInterface(SymbolManager manager)
  {
    initialize(manager);
  }

  private void initialize(SymbolManager manager)
  {
    threadSuspendCounter = 0;
    
    threadClass = new ReferenceType(JOPDebugInterface.THREAD_NAME);
    threadClass.setTypeSignature(JOPDebugInterface.THREAD_SIGNATURE);
    threadClass.setTypeTag(TypeTag.CLASS);
    threadClass.setTagConstant(TagConstants.THREAD);
    threadClass.setTypeID(MAIN_THREAD_ID);
    threadClass.setStatus(ClassStatusConstants.INITIALIZED);

    mainThread = new ObjectReference(MAIN_THREAD_ID);
    mainThread.setType(threadClass);
    
    symbolManager = manager;
  }
  
  protected SymbolManager getSymbolManager()
  {
    return symbolManager;
  }
  
  /**
   * @param signature
   * @return
   */
  public ReferenceTypeList getReferenceTypeList(String signature)
  {
    return symbolManager.getReferenceTypeList(signature);
  }

  /**
   * @return
   */
  public ReferenceTypeList getAllReferenceTypes()
  {
    return symbolManager.getAllReferenceTypes();
  }
  
  public ObjectReferenceList getCurrentlyRunningThreads()
  {
    ObjectReferenceList list = new ObjectReferenceList();
    
    // IMPROVE: improve thread support. 
    // the processor should inform this to provide 
    // actual thread ID's. Here this is just simulating
    // one mock ID which will be used later to query the
    // machine.
    list.add(mainThread);

    return list;
  }

  public ObjectReferenceList getTopLevelThreadGroups()
  {
    ObjectReferenceList list = new ObjectReferenceList();
    
    // IMPROVE: when support for ThreadGroups is implemented, finish this method.
    // for now, an empty list is allright.
    ObjectReference ref = new ObjectReference(DEFAULT_THREAD_GROUP_ID);
    
    // not necessary now.
//    ReferenceType type = THREADGROUP_TYPE;
//    ref.setType(type)
    
    list.add(ref);
    
    return list;
  }
  
  public void dispose() throws IOException
  {
    // according to the specification, those are the actions that need to
    // be done.
    cancellAllEventRequests();
    resumeAllThreads();
    enableGarbageCollection();
  }

  private void cancellAllEventRequests()
  {
    //  TODO Auto-generated method stub
  }
  
  private void resumeAllThreads() throws IOException
  {
    if(isSuspended())
    {
      threadSuspendCounter = 0;
      jopDebugChannel.sendResumeCommand();
    }
  }
  
  /**
   * @return
   */
  private void increaseSuspendCounter()
  {
    threadSuspendCounter ++;
  }
  
  /**
   * @return
   */
  private void reduceSuspendCounter()
  {
    if (threadSuspendCounter > 0)
    {
      threadSuspendCounter --;
    }
  }
  
  /**
   * @return
   */
  private boolean isSuspended()
  {
    return (threadSuspendCounter > 0);
  }
  
  /**
   * 
   */
  private void enableGarbageCollection()
  {
    // TODO Auto-generated method stub
    
  }

  public void suspendJavaMachine() throws IOException
  {
    if(isSuspended() == false)
    {
      jopDebugChannel.suspendJavaMachine();
    }
    increaseSuspendCounter();
  }
  
  public void resumeJavaMachine() throws IOException
  {
    reduceSuspendCounter();
    if(isSuspended() == false)
    {
      jopDebugChannel.resumeJavaMachine();
    }
  }
  
  public void exitJavaMachine(int exitCode) throws IOException
  {
    
    jopDebugChannel.sendExitCommand(exitCode);
  }

  public int createString(String data)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public void holdEvents()
  {
    // TODO Auto-generated method stub

  }

  public void releaseEvents()
  {
    // TODO Auto-generated method stub

  }

  // methods for ReferenceType Command Set (2)
  public String getSignature(long referenceType)
  {
    return symbolManager.getReferenceTypeSignature((int)referenceType);
  }

  public long getClassLoaderID(long referenceType) throws JDWPException
  {
    // all classes in JOP are created directly by JOPizer.
    // no further classes are loaded after the system's image is built.
    // consider all classes as "loaded" by the default loader.

    // check if it's a valid reference. Will throw an exception
    // if it's invalid.
    getReferenceType(referenceType);
    
    // now there is only the system class loader. Change this
    // to support more loaders in the future.
    return JDWPConstants.SYSTEM_CLASS_LOADER;
  }

  public ReferenceType getReferenceType(long referenceType)
      throws JDWPException
  {
    ReferenceType reference = null;

    reference = queryReferenceType(referenceType);
    if (reference == null)
    {
      int errorCode = ErrorConstants.ERROR_INVALID_CLASS;
      JDWPException failure = new JDWPException(errorCode);
      throw failure;
    }

    return reference;
  }
  
  private ReferenceType queryReferenceType(long referenceType)
  {
    ReferenceType reference = null;
    
    reference = symbolManager.getReferenceType((int) referenceType);
    
    return reference;
  }

  public int getModifiers(long referenceType) throws JDWPException
  {
    ReferenceType reference = getReferenceType(referenceType);
    return reference.getModifiers();
  }

  public GenericReferenceDataList getFieldReferenceList(long referenceType)
      throws JDWPException
  {
    return symbolManager.getFieldReferenceList((int)referenceType);
  }

  public GenericReferenceDataList getMethodReferenceList(long referenceType)
      throws JDWPException
  {
    return symbolManager.getMethodReferenceList((int)referenceType);
  }

  public void getStaticValues(long referenceTypeId,
      GenericReferenceDataList referenceList) throws JDWPException
  {
    int index = 0;
    int size;
    GenericReferenceData reference;

    ReferenceType referenceType = getReferenceType(referenceTypeId);
    size = referenceList.size();

    for (index = 0; index < size; index++)
    {
      reference = referenceList.get(index);
      setReferenceValue(referenceType, reference);
    }
  }

  private void setReferenceValue(ReferenceType referenceType,
      GenericReferenceData reference)
  {
    // TODO Auto-generated method stub
    long id;
    id = reference.getFieldOrMethodId();

    // query JOP about the value
    // set the value
  }

  public String getSourceFile(long referenceType) throws JDWPException
  {
    String sourceFile = symbolManager.getSourceFile((int) referenceType);
    
    if(sourceFile == null)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_CLASS);
    }
    
    return sourceFile;
  }

  public GenericReferenceDataList getNestedTypesList(long typeId)
      throws JDWPException
  {
    return symbolManager.getNestedTypesList((int) typeId);
  }

  public int getStatus(long typeId) throws JDWPException
  {
    int status;

    // if there is no type, an exception will be raised here
    getReferenceType(typeId);
    status = ClassStatusConstants.INITIALIZED;

    return status;
  }

  public GenericReferenceDataList getDeclaredInterfacesList(long typeId)
      throws JDWPException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public long getClassObject(long typeId) throws JDWPException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getSourceDebugExtension(long typeId) throws JDWPException
  {
    // IMPROVE: when supported, implement this.
//    throw new JDWPException(ErrorConstants.ERROR_NOT_IMPLEMENTED);
    return "";
  }

  public long getSuperclass(long classId) throws JDWPException
  {
    long superclassId;

    if (isObjectClassId(classId))
    {
      superclassId = 0;
    } else
    {
      superclassId = querySuperClass(classId);
    }

    return superclassId;
  }

  private long querySuperClass(long classId)
  {
    return symbolManager.getSuperClass((int)classId);
  }

  private boolean isObjectClassId(long classId)
  {
    return symbolManager.isObjectClassId((int)classId);
  }

  public GenericReferenceData getGenericReferenceData(long fieldId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setStaticValues(long classId, GenericReferenceDataList list)
      throws JDWPException
  {
    checkValidClass(classId);

    int index, size;
    size = list.size();
    // check if all fields belong to the class. If not,
    // throw an exception.
    for (index = 0; index < size; index++)
    {
      GenericReferenceData data = list.get(index);
      checkStaticField(classId, data);
    }

    for (index = 0; index < size; index++)
    {
      GenericReferenceData data = list.get(index);
      setStaticField(classId, data);
    }
  }

  private void checkValidClass(long classId) throws JDWPException
  {
    boolean isValid = symbolManager.isValidTypeId((int) classId);
    if(isValid == false)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_CLASS);
    }
  }

  private void checkStaticField(long classId, GenericReferenceData data)
      throws JDWPException
  {
    int fieldId = data.getFieldOrMethodId();
    boolean isValid = symbolManager.isValidStaticFieldId((int) classId, fieldId);
    if(isValid == false)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_FIELDID);
    }
  }

  private void setStaticField(long classId, GenericReferenceData data)
  {
    // TODO Auto-generated method stub

  }

  public LineTable getLineTable(long typeId, long methodId) throws JDWPException
  {
    return symbolManager.getLineTable((int) typeId, (int) methodId);
  }

  public VariableTable getVariableTable(long typeId, long methodId)
      throws JDWPException
  {
    return symbolManager.getVariableTable((int) typeId, (int) methodId);
  }

  public byte[] getBytecodes(long typeId, long methodId) throws JDWPException
  {
    return symbolManager.getBytecodes((int) typeId, (int)methodId);
  }

  public boolean isObsolete(long typeId, long methodId) throws JDWPException
  {
    // currently it's not possible to replace methods on-the-fly,
    // so this call will always return false.
    return false;
  }

  public ObjectReference getObjectReference(long objectId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public GenericReferenceData getField(long objectId, long fieldId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setFieldValues(long objectId, GenericReferenceDataList list)
  {
    // TODO Auto-generated method stub

  }

  public GenericReferenceData getStaticFieldReferenceData(long classId,
      long fieldId)
  {
    // TODO Auto-generated method stub
//    GenericReferenceData field = symbolManager.getField((int)classId, (int)fieldId);
//    int fieldAddress = symbolManager.getFieldAddress((int)classId, (int)fieldId);
//    int fieldSize = symbolManager.getFieldSize((int) classId, (int)fieldId);
//    
    return null;
  }


//public int getStaticPrimitiveVariableValue(int classId, int fieldId) throws IOException
//{
//  int pointer;
//  , int variableIndex;
//  // check class ID
//  // get class info
//  // get field index
//  // check if it is actually static
//  // get constant pool index
//  // get constant pool content at field index: field address
//  // if (size == 1)
//  //   read 1 byte: address content
//  // else
//  //   read 2 bytes (address, address + 1
//  
////  checkConnection();
////  
////  int received;
////  //------------------------------------------------------------
////  // get a static primitive variable
////  output.writeByte(2);
////  output.writeByte(6);
////  
////  // frame 0 (main method call)
//////  output.writeInt(0);
////  output.writeInt(frameIndex);
////  
////  // local variable 1 (x variable)
//////  output.writeInt(1);
////  output.writeInt(variableIndex);
////
////  received = input.readInt();
////  System.out.println("  Variable value: " + received);
////  
////  return received;
//  // TODO: finish this
//  return 0;
//}

  
  public String getStringValue(long objectId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public String getThreadName(long objectId) throws JDWPException
  {
    checkValidThread(objectId);
    String name = queryThreadName(objectId);
    return name;
  }

  /**
   * Check if the reference is a valid thread. If it's null, not a valid
   * reference to a thread or has exited, throw a JDWPException with the correct
   * error code.
   * 
   * @param objectId
   * @throws JDWPException
   */
  private void checkValidThread(long objectId) throws JDWPException
  {
    boolean hasExited = true;
    boolean isValidThread = isValidThread(objectId);
    if (isValidThread)
    {
      hasExited = hasExited(objectId);
    }
    if ((objectId == 0) || (isValidThread == false) || hasExited)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_THREAD);
    }
  }

  private boolean hasExited(long objectId)
  {
    // when there is support for more than one execution line, improve this
    return false;
  }

  private boolean isValidThread(long objectId)
  {
    // currently there is only one valid thread
    return (objectId == MAIN_THREAD_ID);
  }

  private String queryThreadName(long objectId)
  {
    // currently there is only one valid thread
    return THREAD_NAME;
  }

  public void suspendThread(long objectId) throws JDWPException, IOException
  {
    checkValidThread(objectId);
    requestThreadSuspension(objectId);
  }

  private void requestThreadSuspension(long objectId) throws IOException
  {
    jopDebugChannel.suspendJavaMachine();
  }

  public void resumeThread(long objectId) throws JDWPException
  {
    checkValidThread(objectId);
    boolean isSuspended = isThreadSuspended(objectId);
    if (isSuspended)
    {
      try
      {
        requestThreadResume(objectId);
      }
      catch(IOException exception)
      {
        throw new JDWPException(ErrorConstants.ERROR_VM_DEAD);
      }
    }
  }

  public boolean isThreadSuspended(long objectId) throws JDWPException
  {
    checkValidThread(objectId);
    return checkSuspendedThread(objectId);
  }

  /**
   * @param objectId
   * @return
   */
  private boolean checkSuspendedThread(long objectId)
  {
    // IMPROVE when there is support for more than one thread. 
    return isSuspended();
  }

  private void requestThreadResume(long objectId) throws IOException
  {
    jopDebugChannel.sendResumeCommand();
  }

  public int getThreadStatus(long objectId) throws JDWPException
  {
    int status;
    
    checkValidThread(objectId);
    if(isSuspended())
    {
      status = ThreadStatusConstants.SLEEPING;
    }
    else
    {
      status = ThreadStatusConstants.RUNNING;
    }
    
    return status;
  }

  public int getThreadSuspendStatus(long objectId) throws JDWPException
  {
    return getThreadStatus(objectId);
  }

  public int getThreadGroup(long objectId) throws JDWPException
  {
    checkValidThread(objectId);
    return DEFAULT_THREAD_GROUP_ID;
  }

  /**
   * @param threadId
   * @param startFrame
   * @param length
   * @return
   * @throws JDWPException
   */
  public FrameList getFrames(long threadId, int startFrame, int length)
      throws JDWPException
  {
    boolean suspended;
    suspended = isThreadSuspended(threadId);
    if (suspended == false)
    {
      throw new JDWPException(ErrorConstants.ERROR_THREAD_NOT_SUSPENDED);
    }
    
    FrameList list;
    try
    {
      list = jopDebugChannel.getStackFrameList();
    }
    catch(IOException exception)
    {
      exception.printStackTrace();
      throw new JDWPException(ErrorConstants.ERROR_VM_DEAD);
    }
    
    return list;
  }

  /**
   * @param threadId
   * @return
   * @throws JDWPException 
   */
  public int getFrameCount(long threadId) throws JDWPException
  {
    int count;
    boolean suspended;
    suspended = isThreadSuspended(threadId);
    if (suspended == false)
    {
      throw new JDWPException(ErrorConstants.ERROR_THREAD_NOT_SUSPENDED);
    }
    
    try
    {
      count = jopDebugChannel.getStackDepth();
    }
    catch(IOException exception)
    {
      exception.printStackTrace();
      throw new JDWPException(ErrorConstants.ERROR_VM_DEAD);
    }
    
    return count;
  }

  /**
   * @param threadId
   * @return
   * @throws JDWPException
   */
  public ObjectReferenceList getOwnedMonitors(long threadId)
      throws JDWPException
  {
    throw new JDWPException(ErrorConstants.ERROR_NOT_IMPLEMENTED);
  }

  /**
   * @param threadId
   * @return
   * @throws JDWPException
   */
  public ObjectReference getCurrentContendedMonitor(long threadId)
      throws JDWPException
  {
    throw new JDWPException(ErrorConstants.ERROR_NOT_IMPLEMENTED);
  }

  /**
   * @param threadId
   * @param exceptionId
   * @throws JDWPException
   */
  public void stopThread(long threadId, long exceptionId) throws JDWPException
  {
    throw new JDWPException(ErrorConstants.ERROR_NOT_IMPLEMENTED);
  }

  /**
   * @param threadId
   * @throws JDWPException
   */
  public void interruptThread(long threadId) throws JDWPException
  {
    throw new JDWPException(ErrorConstants.ERROR_NOT_IMPLEMENTED);
  }

  /**
   * @param threadId
   * @return
   * @throws JDWPException
   */
  public int getSuspendCount(long threadId) throws JDWPException
  {
    return this.threadSuspendCounter;
  }
  
  /**
   * @param objectId
   * @return
   * @throws JDWPException
   */
  public String getThreadGroupName(long objectId) throws JDWPException
  {
    checkValidThreadGroup(objectId);
    return DEFAULT_THREADGROUP_NAME;
  }

  /**
   * @param objectId
   * @throws JDWPException
   */
  private void checkValidThreadGroup(long objectId) throws JDWPException
  {
    if (objectId != DEFAULT_THREAD_GROUP_ID)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_THREAD_GROUP);
    }
  }

  /**
   * @param objectId
   * @return
   * @throws JDWPException
   */
  public long getParentThreadGroupId(long objectId) throws JDWPException
  {
    checkValidThreadGroup(objectId);
    return NULL_OBJECT_REFERENCE;
  }

  /**
   * @param objectId
   * @return
   * @throws JDWPException
   */
  public ObjectReferenceList getChildrenThreads(long objectId)
      throws JDWPException
  {
    checkValidThreadGroup(objectId);
    ObjectReferenceList threads = new ObjectReferenceList();
    ObjectReference object = mainThread;
    threads.add(object);

    return threads;
  }

  /**
   * @param objectId
   * @return
   * @throws JDWPException
   */
  public ObjectReferenceList getChildrenGroups(long objectId)
      throws JDWPException
  {
    checkValidThreadGroup(objectId);
    // currently we don't support thread groups
    ObjectReferenceList groups = new ObjectReferenceList();
    return groups;
  }

  /**
   * @param objectId
   * @return
   * @throws JDWPException 
   */
  public int getArrayLength(long objectId) throws JDWPException
  {
    // TODO Auto-generated method stub
    throw new JDWPException(ErrorConstants.ERROR_NOT_IMPLEMENTED);
//    return 0;
  }

  /**
   * @param objectId
   * @return
   */
  public boolean isObjectArray(long objectId)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @param objectId
   * @param length 
   * @param firstIndex 
   * @return
   */
  public GenericReferenceDataList getArrayValues(long objectId, 
    int firstIndex, int length)
  {
    checkArrayBounds(objectId, firstIndex, length);
    GenericReferenceDataList list = new GenericReferenceDataList();
    
    // TODO Auto-generated method stub
//    queryElements(objectId)
    return list;
  }

  /**
   * @param objectId
   * @param firstIndex
   * @param length
   */
  private void checkArrayBounds(long objectId, int firstIndex, int length)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param objectId
   * @return
   */
  public ReferenceType getArrayType(long objectId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param firstIndex
   * @param list
   */
  public void setArrayValues(int firstIndex, GenericReferenceDataList list)
  {
    // TODO Auto-generated method stub
    
  }
  
  public ReferenceTypeList getVisibleClasses(long classId) throws JDWPException
  {
    // currently JOP does not have a classloader, so consider it the "default"
    // loader and return a list with all available classes in the system.
    
    // IMPROVE: when there are other loaders than the default, use them.
    // Check the ID and return the corresponding classes.
    if (classId != JDWPConstants.SYSTEM_CLASS_LOADER)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_CLASS_LOADER);
    }
    return getAllReferenceTypes();
  }

  /**
   * @param jopDebugChannel
   */
  public void setJopChannel(JOPDebugChannel jopDebugChannel)
  {
    this.jopDebugChannel = jopDebugChannel;
  }
  
  /**
   * @param threadId
   * @throws JDWPException 
   */
  private void checkThreadId(long threadId) throws JDWPException
  {
    // IMPROVE currently we have only one thread.
    // make generic later.
    //
    // updateThreadIdList();
    // if(threadIdList.contains(threadId) == false)
    // {
    //   throw new JDWPException(ErrorConstants.ERROR_INVALID_THREAD);
    // }
    //
    if (MAIN_THREAD_ID != threadId)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_THREAD);
    }
  }
  
  /**
   * 
   * @param threadId
   * @param frameId
   * @throws IOException
   * @throws JDWPException 
   */
  private void checkFrameId(long threadId, long frameId) throws IOException, JDWPException
  {
    int size = jopDebugChannel.getStackDepth();
    if (0 > frameId || frameId >= size)
    {
      throw new JDWPException(ErrorConstants.ERROR_INVALID_FRAMEID);
    }    
  }
  
  /**
   * @param threadId
   * @param frameId
   * @param list
   * @return
   * @throws IOException 
   * @throws JDWPException 
   */
  public GenericReferenceDataList getStackFrameValues(long threadId, 
    long frameId, GenericReferenceDataList list) throws IOException, JDWPException
  {
    int size, index;
    int frameIndex, variableIndex;
    int value;
    
    GenericReferenceDataList newList;
    GenericReferenceData data;
    
    // check if the thread and frame ID are valid or not
    checkThreadId(threadId);
    checkFrameId(threadId, frameId);
    
    // currently ignore the threadId.
    frameIndex = (int) frameId;
    
    newList = new GenericReferenceDataList();
    size = list.size();
    for(index = 0; index < size; index++)
    {
      data = list.get(index);
      variableIndex = data.getFieldOrMethodId();
      
      value = jopDebugChannel.getLocalVariableValue(frameIndex, variableIndex);
      
      data.setValue(value);
      newList.add(data);
    }
    
    return newList;
  }
  
  /**
   * 
   * @param threadId
   * @param frameId
   * @param list
   * @throws IOException
   * @throws JDWPException 
   */
  public void setStackFrameValues(long threadId, 
      long frameId, GenericReferenceDataList list) throws IOException, JDWPException
  {
    int size, index;
    int frameIndex, variableIndex;
    int value;
    
    GenericReferenceData data;
    
    // check if the thread and frame ID are valid or not
    checkThreadId(threadId);
    checkFrameId(threadId, frameId);
    
    // currently ignore the threadId.
    frameIndex = (int) frameId;
    
    size = list.size();
    for(index = 0; index < size; index++)
    {
      data = list.get(index);
      variableIndex = data.getFieldOrMethodId();
      value = data.getIntValue();
      jopDebugChannel.setLocalVariableValue(frameIndex, variableIndex, value);
    }
  }

  /**
   * @param frameId
   * @return
   * @throws JDWPException 
   * @throws IOException 
   */
  public GenericReferenceData getThisObject(long threadId, long frameId) throws JDWPException, IOException
  {
    int thisReference;
    int methodPointer;
    
    // check if the thread and frame ID are valid or not
    checkThreadId(threadId);
    checkFrameId(threadId, frameId);
    
    methodPointer = jopDebugChannel.getMethodPointer((int) frameId);
    
    // if this is an object method, the "this" reference will be at position 0
    int variableIndex = 0;
    GenericReferenceData data = new GenericReferenceData(variableIndex);
    
    if(symbolManager.isStaticOrNative(methodPointer))
    {
      thisReference = 0;
    }
    else
    {
      thisReference = jopDebugChannel.getLocalVariableValue((int)frameId,
        variableIndex);
    }
    
    data.setValue(thisReference);
    return data;
  }
}
