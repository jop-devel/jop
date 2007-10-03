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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.util.BCELifier;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.verifier.Verifier;

import com.jopdesign.debug.jdwp.util.Debug;

/**
 * BytecodeHandler.java
 * 
 * This class transform other classes using the BCEL library.
 * 
 * Currently the main role is to insert NOP instructions in specific
 * positions inside the code in order to help during
 * breakpoint implementation.
 * 
 * This modification can be done both in the class files (change 
 * compiled code on disk) and in JavaClass objects 
 * (in this case, modifications are in memory only).
 * 
 * @author Paulo Abadie Guedes
 *
 * 04/07/2007 - 12:05:06
 * 
 */
public class BytecodeHandler
{
//  private Repository repository;
//  private static BytecodeHandler bytecodeHandler;
  
  private BytecodeHandler()
  {
    
  }

//  
//  public BytecodeHandler()
//  {
//    ClassPath path = new ClassPath(classpath);
//    repository = SyntheticRepository.getInstance(path); 
//  }
//  
//  public static void createRepository(String classpath)
//  {
//    
//  }
//  
  /**
   * Add NOP instructions before each linenumber position
   * in the bytecode.
   * 
   * @param method
   */
  public static void insertNopInstructionsAtLines(MethodGen methodGen)
  {
    InstructionList list;
    
    list = createNopInstructionList();
    insertInstructionsAtLines(methodGen, list);
  }
  
  /**
   * @return
   */
  private static InstructionList createNopInstructionList()
  {
    Instruction instruction;
    InstructionList list;
    instruction = InstructionConstants.NOP;
    list = new InstructionList();
    list.append(instruction);
    return list;
  }
  
  /**
   * Add a sequence of instructions right before the locations
   * specified by all LineNumebr objects inside the given method.
   * @param methodGen
   */
  public static void insertInstructionsAtLines(MethodGen methodGen, 
      InstructionList instructionList)
  {
    int index, size;
    LineNumberGen[] lines;
    LineNumberGen lineNumberGen;
//    Instruction instruction;
    InstructionHandle handle, insertedInstruction;
    InstructionList list;
    InstructionList copiedList;
//    InstructionTargeter[] targeters;
    
//    nopInstruction = new InstructionHandle(InstructionConstants.NOP);
//    nopInstruction = InstructionHandle.getInstructionHandle(InstructionConstants.NOP);
//    instruction = InstructionConstants.NOP;
    list = methodGen.getInstructionList();
    
    Debug.println("Original instruction list:");
    Debug.println(list);
    
    lines = methodGen.getLineNumbers();
//    methodGen.removeLineNumbers();
    size = lines.length;
    for(index = 0; index < size; index++)
    {
      lineNumberGen = lines[index];
      handle = lineNumberGen.getInstruction();
      
//    modifyInstructionList(list);
      
      // need to copy because the list.insert() call modify the original
      // list. Without a deep copy the list would be inserted only once.
      
      // this call does not work because all further references 
      // for the next line numbers will point to it
//      copiedList = instructionList.copy();
      copiedList = copyList(instructionList);
      
      insertedInstruction = list.insert(handle.getInstruction(), copiedList);
      
      list.redirectBranches(handle, insertedInstruction);
      list.redirectExceptionHandlers(methodGen.getExceptionHandlers(),
          handle, insertedInstruction);
      list.redirectLocalVariables(methodGen.getLocalVariables(), 
          handle, insertedInstruction);
      
      // now set the line number to point to the inserted instruction.
      
      // The line below is not working (actually it is not needed, leave commented). 
      // Using it actually set the line but, break it somehow.
      // it seems that the object reference which is used is not correct.  
      // If used, the side effect is this: calling this method 
      // (insertInstructionsAtLines) twice (or more) makes all new 
      // instructions to be inserted at the beginning of the method, 
      // instead of at the correct places inside the method.
//      lineNumberGen.setInstruction(insertedInstruction);
      
      // just to 
//      methodGen.addLineNumber(insertedInstruction, lineNumberGen.getSourceLine());
    }
    list.setPositions();
    list.update();
    
    methodGen.setMaxStack();
    methodGen.setMaxLocals();
    
    methodGen.update();
//    Debug.println("Transformed instruction list:");
//    Debug.println(list);
//    Debug.println();
//    
//    InstructionHandle[] array = list.getInstructionHandles();
//    Debug.println("Array:");
//    Debug.println(array);
  }
  
  private static InstructionList copyList(InstructionList list)
  {
    byte[] bytecode = list.getByteCode();
    InstructionList instructionList = new InstructionList(bytecode);
    return instructionList;
  }
  
//  private static void modifyInstructionList(InstructionList list)
//  {
//  targeters = handle.getTargeters();
    
    // does not work because the jump targets are not updated.
//    list.insert(handle.getInstruction(), instruction);
    
    // does not update correctly the jumps also
//    list.append(handle.getInstruction(), handle.getInstruction());
//    handle.setInstruction(instruction);
    
    // don't know if it worked:(
//    try
//    {
//      insertedInstruction = list.append(handle.getInstruction(), instruction);
//      list.append(insertedInstruction.getInstruction(), handle.getInstruction());
//      list.delete(handle);
//    }
//    catch(TargetLostException e)
//    {
//      // get all instructions which are now inconsistent because their targets
//      // were removed in the try block
//      InstructionHandle[] targets = e.getTargets();
//      for(int i = 0; i < targets.length; i++)
//      {
//        // get all handlers whose targets used to be the handle at index i
//        targeters = targets[i].getTargeters();
//        for(int j = 0; j < targeters.length; j++)
//        {
//          // make them point now to the inserted instruction
//          targeters[j].updateTarget(targets[i], insertedInstruction);
//        }
//      }
//    }
//  }
  
  public static JavaClass loadClass(ClassPath classpath, String className) throws IOException
  {
    JavaClass javaClass;
    
//    if(classList.contains(className))
//    {
//      // already loaded. Return same instance.
//      javaClass = classList.getClass(className);
//    }
//    else
    {
      // not loaded yet. Load and add to the repository.
      InputStream is = classpath.getInputStream(className);
      ClassParser parser = new ClassParser(is, className);
      javaClass = parser.parse();
      
//      classList.add(javaClass);
//      Repository.addClass(javaClass);
    }
    
    return javaClass;
  }
  
  public static ClassPath createClasspath(String path)
  {
    ClassPath classpath = new org.apache.bcel.util.ClassPath(path);
    return classpath;
  }
  
  /**
   * Print information related to stack usage for each bytecode.
   */
  public static void printStackUsage()
  {
    int index, size;
    
    Debug.println("Consumed and produced stack for bytecodes:");
    Debug.println("------------------------------------------");
    size = Constants.CONSUME_STACK.length;
    for(index = 0; index < size; index++)
    {
      Debug.print("Bytecode: ");
      printAndAlign(index, 4);
      printAndAlign(Constants.OPCODE_NAMES[index], 18);
      printAndAlign(Constants.CONSUME_STACK[index], 4);
      printAndAlign(Constants.PRODUCE_STACK[index], 4);
      Debug.println();
    }
  }
  
  public static void printAndAlign(int data, int size)
  {
    printAndAlign("" + data, size);
  }
  
  public static void printAndAlign(String string, int size)
  {
    int index;
    Debug.print(string);
    for(index = string.length(); index < size; index++)
    {
      Debug.print(" ");
    }
  }
  
  /**
   * This method insert NOP instructions before the code of each LineNumber,
   * for all methods inside the given JavaClass.
   * 
   * Methods that are native or abstract are skipped. 
   * 
   * @param javaClass
   */
  public static void insertNopInstructions(ClassGen classGen)
  {
    int index, size;
    String className;
    Method[] methodArray;
    Method method = null;
    MethodGen methodGen;
    
    className = classGen.getClassName();
    methodArray = classGen.getMethods();
    
    Debug.println("Class: ");
    Debug.println(classGen);
    
//    printConstantPool(classGen);
    
    size = methodArray.length;
    for(index = 0; index < size; index++)
    {
      method = methodArray[index];
      
      Debug.print("Method: ");
      Debug.println(method);
      
      // if the method is not abstract nor native, process it. Ignore otherwise.
      if((method.isNative() == false) &&
         (method.isAbstract() == false))
      {
//        methodGen = createMethodGen(javaClass, method);
        methodGen = createMethodGen(className, method);
        insertNopInstructionsAtLines(methodGen);
        
        // just to test. The calls below should revert the actions done before.
//        methodGen.removeNOPs();
//        methodGen.getInstructionList().setPositions();
        
        // in order to properly support exceptions it may be necessary to
        // add try/catch blocks for the entire method code, as well as 
        // NOP instructions for breakpoints before each exception handler.
        // Will prove first that the NOP idea will work and then add this change.
        
        method = methodGen.getMethod();
        methodArray[index] = method;
      }
      
//      Debug.println();
//      Debug.println("Method code:");
//      Debug.println(method.getCode());
    }
    
    // now assign back the (possibly) modified code into the original class
//    javaClass.setMethods(methodArray);
    classGen.setMethods(methodArray);
    classGen.update();
    
//    printConstantPool(classGen);
  }

  /**
   * @param classGen
   */
  private static void printConstantPool(ClassGen classGen)
  {
    System.out.println("ConstantPool: ");
    System.out.println(classGen.getConstantPool());
    System.out.println();
  }
  
  /**
   * @param method
   * @return
   */
  public static MethodGen createMethodGen(String className, Method method)
  {
//    String className = javaClass.getClassName();
    
    ConstantPool constantPool = method.getConstantPool();
    ConstantPoolGen constantPoolGen = new ConstantPoolGen(constantPool);
    MethodGen methodGen = new MethodGen(method, className, constantPoolGen);
    
    return methodGen;
  }
  
  public static void main(String[] args) throws NoSuchMethodException,
      IOException, ClassNotFoundException
  {
    String folder;
    boolean debug;
    
    debug = false;
//    debug = true;
    
    Debug.setDebugging(debug);
    
    folder = ".";
    
    if(args.length > 0)
    {
      folder = args[0];
    }
    File file = new File(folder);
    if(file.isDirectory() == false)
    {
      System.out.println();
      System.out.println("  Failure: folder does not exist.");
      System.out.println("  Please provide a valid folder. If no parameter is provided,");
      System.out.println("  the current folder will be used.");
      System.out.println();
    }
    else
    {
      // create a list with all class files inside the current folder
      FileList list = FileList.createClassFileList(folder);
      
      if(debug)
      {
        test(list);
      }
      else
      {
        processFileList(list);
      }
    }
  }
  
  private static void test(FileList list) throws IOException, NoSuchMethodException, ClassNotFoundException
  {
//  Debug.setDebugging(false);
    
    // printStackUsage();
    // processMethod(1);
    
//    processFileList(list);
    
    // for debug purposes only
    testBytecodeProcessor();
//    
    File file = list.get(0);
    list = new FileList();
    list.add(file);
    
//    verifyFileList(list);
//
//  processFileList(list);
//  processFileList(list);

  }
  
  public static void processFileList(FileList fileList) throws IOException
  {
    int index, length;
    File currentFile;
    
    length = fileList.size();
    
    for(index = 0; index < length; index++)
    {
      currentFile = fileList.get(index);
      processFile(currentFile);
    }
  }
  
//  public static void verifyFileList(FileList fileList) throws IOException
//  {
//    int index, length;
//    File currentFile;
//    String[] args = new String[1];
//    
//    length = fileList.size();
//    
//    for(index = 0; index < length; index++)
//    {
//      currentFile = fileList.get(index);
//      args[0] = currentFile.toString();
//      if(args[0].startsWith(".\\"))
//      {
//        args[0] = args[0].substring(2);
//      }
//      Verifier.main(args);
//    }
//  }
  
  /**
   * @param file
   * @throws IOException 
   */
  private static void processFile(File file) throws IOException
  {
    String className;
    InputStream is;
    ClassParser parser;
    JavaClass javaClass;
    ClassGen classGen;
    
    className = file.getName();
    
    // process only files that finish with ".class" extension
    if(className.endsWith(".class"))
    {
      // show which class is being processed
      System.out.print("Processing ");
      System.out.print(className);
      System.out.print("...  ");
      
      is = new FileInputStream(file);
      parser = new ClassParser(is, className);
      javaClass = parser.parse();
      
      // insert NOP instructions before each LineNumber into the java classes
      classGen = new ClassGen(javaClass);
      insertNopInstructions(classGen);
      
      // write back the class into its original file.
      javaClass = classGen.getJavaClass();
      javaClass.dump(file);
      
      System.out.println("done.");
    }
  }
  
  public static void testBytecodeProcessor() throws NoSuchMethodException,
      IOException, ClassNotFoundException
  {
    boolean isDebugging;
    ClassPath classpath;
    JavaClass javaClass;
    String classpathString, className;
    File file;
    ClassGen classGen;
     
    isDebugging = true;
//    isDebugging = false;
    Debug.setDebugging(isDebugging);
    
    classpathString = "./";
    classpath = createClasspath(classpathString);
    
    className = "com.jopdesign.debug.jdwp.util.HelloWorld";
//    className = "com.jopdesign.debug.jdwp.util.Util";
    file = new File(className.replace('.', '/') + ".class");
    
    javaClass = loadClass(classpath, className);
    
    BCELifier helper;
    
    helper = new BCELifier(javaClass, System.out);
    helper.start();
    
    classGen = new ClassGen(javaClass);
    insertNopInstructions(classGen);

    // write back the class into its original file.
    javaClass = classGen.getJavaClass();
    javaClass.dump(file);

    Debug.println("Done.");
    Debug.println(".");
    Debug.println(".");
    
    helper = new BCELifier(javaClass, System.out);
    helper.start();
  
  }
  

//  public static void processMethod(int index) throws IOException,
//      ClassNotFoundException
//  {
//    String classpathString, className;
//    // ClassPath classpath;
//    JavaClass javaClass;
//    Method[] methodArray;
//    Method method;
//
////    classList = new ClassList();
//
//    // className = "helloworld.TestJopDebugKernel";
//    className = "analysis.example.Test";
////    classpathString = "./;classes.zip";
//    classpathString = "./";
//    classpath = loadClasspath(classpathString);
//    javaClass = loadClass(classpath, className);
//
//    methodArray = javaClass.getMethods();
//    method = methodArray[index];
//
////    analyzeTypes(javaClass, method);
//
//    Debug.println("Class: " + javaClass.toString());
//
//    Debug.print("Method: ");
//    Debug.println(method);
//
//    Debug.println();
//    Debug.println("Method code:");
//    Debug.println(method.getCode());
//    // Pseudograph<Node, E> graph;
//  }
}
