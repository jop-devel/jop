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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.jopdesign.debug.jdwp.constants.NetworkConstants;
import com.jopdesign.debug.jdwp.handler.BasicPacketHandler;
import com.jopdesign.debug.jdwp.handler.ComposedPacketHandler;
import com.jopdesign.debug.jdwp.handler.DescriptionPacketHandler;
import com.jopdesign.debug.jdwp.handler.MainPacketHandler;
import com.jopdesign.debug.jdwp.util.BasicWorker;
import com.jopdesign.debug.jdwp.util.Debug;
import com.jopdesign.debug.jdwp.util.PacketQueue;

public class JDWPEventManager  extends BasicWorker implements Runnable
{
  /**
   * The hostname to which this program will try to connect to. 
   * The default target host name is "localhost".
   */
//  private String targetHostname = CommandConstants.DEFAULT_HOST;

  /**
   * The input port at which this object will listen and wait for a connection.
   * 
   * The default input port number is 8000.
   */ 
  private int inputPortNumber;
  
  /**
   * The default output port number is 8001.
   */ 
//  private int outputPortNumber = CommandConstants.DEFAULT_OUTPUT_PORT_NUMBER;

  // the serverSocket to wait for JDWP connections
  private ServerSocket serverSocket;
  private Socket socket;

  /**
   * The threads responsible to handle the packet queues.
   * Those objects will concurrently grab data from the
   * input stream and build new objects, or the contrary:
   * grab objects and write to the output stream as data.
   */
  private PacketInputQueueManager inputQueueManager;
  private PacketOutputQueueManager outputQueueManager;
  
  private JOPDebugInterface debugInterface;
  /**
   * This class is responsible to handle all events from the network.
   * It reads JDWP packets from the server input stream and also write 
   * other JDWP packets back to the network.
   * 
   * @param debugInterface 
   * @param server
   */
  public JDWPEventManager(JOPDebugInterface debugInterface, int serverPort)
  {
    inputPortNumber = serverPort;
    this.debugInterface = debugInterface;
  }
  
  public JDWPEventManager(JOPDebugInterface debugInterface)
  {
    this(debugInterface, NetworkConstants.DEFAULT_INPUT_PORT_NUMBER);
  }
  
  public int getInputPortNumber()
  {
    return inputPortNumber;
  }
  
  public void run()
  {
    try
    {
      Debug.recordLocation("  JDWPEventManager -> startServer();");
      startServer();
      System.out.println("  Server up and running.");
            
      try
      {
        inputQueueManager.join();
        outputQueueManager.join();
      }
      catch (InterruptedException e)
      {
        System.out.println("Interrupted: " + e.getMessage());
        e.printStackTrace();
      }
      Debug.recordLocation("  JDWPEventManager -> shutDown();");
      shutdown();
    }
    catch (IOException e)
    {
      System.out.println("  Cannot start the serverSocket, sorry.");
      System.out.println("  Maybe there is another service on the same port?");
      e.printStackTrace();
    }
  }
  
  /**
   * Initialize this server.
   * - Create a server socket. Wait until a connection arrives.
   * - Get the corresponding socket.
   * - Create the queue managers.
   * - Start its Threads.
   * 
   * @throws IOException
   */
  private void startServer() throws IOException
  {
    acceptNewConnection();
    
    System.out.println("  Connected. Initializing queues to handle packets.");
    
    initializeQueueManagers();
    
    System.out.println("  Threads and queues initialized successfully.");
  }

  /**
   * Accept a new connection, create a socket and return it.
   * 
   * @throws IOException
   */
  public Socket acceptNewConnection() throws IOException
  {
    int port = getInputPortNumber();
    serverSocket = new ServerSocket(port);
    
    System.out.print("  Debug server running. Waiting for connection at port ");
    System.out.println(port);
    socket = serverSocket.accept();
    
    return socket;
  }

  /**
   * Launch the queue managers based on the new socket.
   * 
   * @throws IOException
   */
  private void initializeQueueManagers() throws IOException
  {
    // create the in/out handlers
    InputStream inputStream = socket.getInputStream();
    OutputStream outputStream = socket.getOutputStream();
    
    // create a pair of packet queues
    PacketQueue inputQueue = new PacketQueue();
    PacketQueue outputQueue = new PacketQueue();
    
    // create the in/out manager threads, with its respective queues
    String inputId = "InputQueueManager";
    String outputId = "OutputQueueManager";
    inputQueueManager = new PacketInputQueueManager(inputId, inputStream, inputQueue);
    outputQueueManager = new PacketOutputQueueManager(outputId, outputStream, outputQueue);
    
    // create the input packet handlers, assigning their respective queues
    DescriptionPacketHandler descriptionHandler = new DescriptionPacketHandler(
      "DescriptionPacketHandler", inputQueue, outputQueue);
    
    MainPacketHandler mainPacketHandler = new MainPacketHandler(
       "MainPacketHandler", inputQueue, outputQueue, debugInterface);
    
    // using this approach, it is possible to combine two or more
    // handlers. Here the first will print the packets, while
    // the second will take proper actions in response.
    ComposedPacketHandler composedHandler = new ComposedPacketHandler(
       descriptionHandler, mainPacketHandler);
    
    // build a handler based on the composition of the previous objects
    BasicPacketHandler mainInputHandler = new BasicPacketHandler(inputQueue, 
      composedHandler);
    
    // launch all threads at once and let them run at will,
    // until the moment we need to stop the application.
    inputQueueManager.start();
    outputQueueManager.start();
    
    // the call below is not strictly necessary, if no further thread
    // is needed. the best thing would be to just store this object 
    // and call its own "run" method inside the "run" method above.
    mainInputHandler.start();
  }
  
  private void shutdown()
  {
    stopWorking();
  }
  
  public synchronized void stopWorking()
  {
    if(isWorking())
    {
      super.stopWorking();
      inputQueueManager.stopWorking();
      outputQueueManager.stopWorking();
    }
  }
}
