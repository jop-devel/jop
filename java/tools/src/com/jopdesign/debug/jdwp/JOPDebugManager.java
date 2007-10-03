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

import com.jopdesign.debug.jdwp.constants.NetworkConstants;
import com.jopdesign.debug.jdwp.jop.JopSymbolManager;
import com.jopdesign.debug.jdwp.util.ArgumentParser;
import com.jopdesign.debug.jdwp.util.StringList;

/**
 * This is the main class for this project.
 * The debug manager is responsible for coordinating all tasks
 * related to the debug process.
 * 
 * Main responsibilities:
 * - Get and process launch parameters
 * - Start and cooperate with the symbol manager
 *   - Require it to load the symbol table for the program
 * - Create the debug communication channel to the JOP machine
 * - Create and colaborate with the JDWP event queue manager
 * - Create and colaborate with the JOP event queue manager
 * - Dispatch debug events
 * - Shutdown
 * 
 * @author Paulo Abadie Guedes
 */

public class JOPDebugManager
{
  private static final int HELP = 1;
  private static final int SUCCESS = 0;
  public static final String HELP_PARAMENTER = "help";
  
  private static String SYMBOL_FILE = "helloworld.sym";
  
  // class to handle program arguments
  private ArgumentParser parser;
  private StringList settings;
  
  // class to provide services to the packet handlers
  private JOPDebugInterface debugInterface; 
  
  // the object responsible to handle requisitions related to symbolic
  // information from the debugger
  private SymbolManager symbolManager;
  
  private String symbolTable = SYMBOL_FILE; 
  
  // the object which allows communication to the real device.
  // But how actually this is done is up to the objects itself.
  private JOPDebugChannel jopDebugChannel;
  
  // the event manager, responsible for handling events from the
  // network and recting accordingly.
  private JDWPEventManager jdwpEventManager;
  
  private JOPDebugManager()
  {
    symbolManager = new JopSymbolManager();
    jopDebugChannel = new JOPDebugChannel();
    
    initializeDefaultParameters();
  }
  
  /**
   * 
   */
  private void initializeDefaultParameters()
  {
    settings = 
      new StringList(NetworkConstants.NETWORK_SETTINGS);
    settings.add(HELP_PARAMENTER);
    
    // set recognizable parameters
    parser = new ArgumentParser(settings);
    
    // set default values.
    parser.parseArguments(NetworkConstants.DEFAULT_SETTINGS_JOP_DEBUG_MANAGER);
  }
  
  private int processLaunchParameters(String args[])
  {
    parser.parseArguments(args);
    
    if(parser.isDefined(args, HELP_PARAMENTER))
    {
      return HELP;
    }
    return SUCCESS;
  }
  
  private void requestSymbolManagerToLoadTable(String file) throws IOException, ClassNotFoundException
  {
    symbolManager.loadSymbolTable(file);
  }
  
  private void connectToJopMachine() throws IOException
  {
    String host;
    int port;
    
    host = parser.getValue(NetworkConstants.TARGET_HOST_PREFIX);
    port = parser.getValueAsInt(NetworkConstants.OUTPUT_PORT_PREFIX);
    
    jopDebugChannel.connect(host, port);
  }
  
  private void createJDWPEventQueueManager()
  {
    int port = parser.getValueAsInt(NetworkConstants.INPUT_PORT_PREFIX);
    jdwpEventManager = new JDWPEventManager(debugInterface, port);
  }

  private void handleJDWPEvents() throws InterruptedException
  {
    jdwpEventManager.start();
    jdwpEventManager.join();
  }
  
  private void shutdown() throws IOException
  {
    jopDebugChannel.close();
  }
  
  /**
   * A quick and simple way to add tags around the code
   * which still need to be implemented.
   *
   */
/*  private void remindMe()
  {
    System.out.println("//TODO: implement this");
    Exception error;
    error = new Exception();
    error.printStackTrace();
  }
*/  
  /**
   * Print out information about how to use this class.
   */
  private void printUsage()
  {
    System.out.println("  Usage:");
    System.out.println("  1) Connect a JOP machine to the network (JopServer class)");
    System.out.println("  2) Launch this class with the proper host/number");
    System.out.println("  3) Launch a Java debugger (with the given input port)");
    System.out.println();
    System.out.println("  Parameters:");
    
    int size = settings.size();
    for(int index = 0; index < size; index++)
    {
      String data = settings.get(index);
      String defaultValue = parser.getValue(data);
        
      System.out.print("    ");
      System.out.print(data);
      if("".equals(defaultValue) == false)
      {
        System.out.print("<value>  ");
        System.out.print("Default value: " + defaultValue);
      }
      System.out.println();
    }
  }
  
  /**
   * Launch all tasks, coordinate the creation and
   * usage of all objects and events. 
   * @param symbol_file2 
   * @throws ClassNotFoundException 
   * @throws Exception 
   *
   */
  private void startJopDebugManager() throws Exception, ClassNotFoundException
  {
    // load the symbol table based on data calculated during compilation
    requestSymbolManagerToLoadTable(symbolTable);
    System.out.println("  Symbol table loaded...");
    
    connectToJopMachine();
    System.out.println("  Jop connected...");
    
    createDebugInterface();
    createJDWPEventQueueManager();
    
//    int port = parser.getValueAsInt(NetworkConstants.INPUT_PORT_PREFIX);
//    System.out.println("  Debug server up and running!");
//    System.out.print("  Listening for a new debug connection. Port: ");
//    System.out.println(port);
    
//  createJopEventQueueManager();
    
    handleJDWPEvents();
    
    shutdown();
  }
  
  private void createDebugInterface()
  {
    debugInterface = new JOPDebugInterface(symbolManager);
    debugInterface.setJopChannel(jopDebugChannel);
  }
  
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    JOPDebugManager manager = new JOPDebugManager();
    
    try
    {
      int x = manager.processLaunchParameters(args);
      if(x == HELP)
      {
        manager.printUsage();
      }
      else
      {
          manager.startJopDebugManager();
      }
    }
    catch(Exception exception)
    {
      System.out.print("Failure. ");
      System.out.println(exception.getMessage());
      System.out.println();
//      exception.printStackTrace();
      
      manager.printUsage();
    }
  }
}
