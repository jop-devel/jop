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

package com.jopdesign.debug.jdwp.sniffer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.jopdesign.debug.jdwp.constants.NetworkConstants;
import com.jopdesign.debug.jdwp.io.CountOutputStream;
import com.jopdesign.debug.jdwp.util.BasicWorker;

/**
 * A socket which can forward and log data to the disk.
 * Work like the 'tee' application, but for the network.
 * 
 * This class models a generic network sniffer.
 * It act like a man-in-the-middle, and may be used to record 
 * regular network traffic. 
 *  
 * The default behaviour is to listen and just wait until a connection
 * arrives. Then it tries to attach to the given host and port number.
 * 
 * Once attached to a client and serverSocket, this class will 
 * forward packets on both connection flow sides.
 * 
 * At the same time, all packets will be copied to one or two
 * output streams. Each stream may be a file, the standard output or 
 * another kind of stream such as the network itself.
 * 
 * The primary purpose of this sniffer is to study, understand
 * and test my implementation of JDWP - the Java Debug Wire Protocol.
 * However, it may be used for almost anything that needs a sniffer
 * as a network stream. 
 * Hence, this is NOT a "transparent" sniffer, since it needs
 * to be attached to an specific network port. 
 * But it acts as a pipe wich may log traffic going through it.
 * 
 * @author Paulo Abadie Guedes
 */
public class SocketSniffer extends BasicWorker 
{
  /**
   * The hostname to which this program will try to connect to. 
   * The default target host name is "localhost".
   */
  private String targetHostname = NetworkConstants.DEFAULT_HOST;
  
  /**
   * The input port at which this object will listen and wait for a connection.
   * 
   * The default input port number is 8000.
   */ 
  private int inputPortNumber = NetworkConstants.DEFAULT_INPUT_PORT_NUMBER;
  
  /**
   * The default output port number is 8001.
   */ 
  private int outputPortNumber = NetworkConstants.DEFAULT_OUTPUT_PORT_NUMBER;
  
  // variables for the serverSocket side
  private ServerSocket serverSocket;
  
  private Socket serverInputSocket;
  
  //variables for the client side
  private Socket clientSocket;
  
  // sniffer interfaces
  SnifferCommunicationInterface serverInterface;
  SnifferCommunicationInterface clientInterface;
  
  private OutputStream serverLogOutputStream;
  private OutputStream clientLogOutputStream;
  
  /**
   * The default constructor. It will listen to port 8000 and
   * will forward the packets to a program on the same host, listening
   * on port 8001.
   * 
   * The captured packets will be copied to the standard output stream.
   */
  public SocketSniffer()
  {
    this(NetworkConstants.DEFAULT_INPUT_PORT_NUMBER, 
         NetworkConstants.DEFAULT_HOST, 
         NetworkConstants.DEFAULT_OUTPUT_PORT_NUMBER);
  }
  
  /**
   * 
   * @param inputPort
   * @param host
   * @param outputPort
   */
  public SocketSniffer(int inputPort, String host, int outputPort)
  {
    this.inputPortNumber = inputPort;
    this.targetHostname = host;
    this.outputPortNumber = outputPort;
    
    serverLogOutputStream = new CountOutputStream("  Server log");
    clientLogOutputStream = new CountOutputStream("  Client log");
  }
  
  /**
   * Run the sniffer.
   */
  public void run()
  {
    try
    {
      startServer();
      System.out.println("  Sniffer up and running. Network packets will be logged.");
      
      sniffConnection();
      
      try
      {
        // now wait the two interfaces to finish execution
        serverInterface.join();
        clientInterface.join();
      }
      catch (InterruptedException e)
      {
      }
      finally
      {
        // ok, job done. After so many code changes, that was easy ;)
        // the call below is not strictly necessary, but let's do it.
        stopWorking();
      }
      
      shutdown();
    }
    catch (IOException e)
    {
      System.out.println();
      System.out.println("  Failure. Maybe there is already a server in the ");
      System.out.println("  same inpout port, or the target server is not running.");
      System.out.println();
      System.out.print("  Error message: \"");
      System.out.print(e.getMessage());
      System.out.println("\"");
//      e.printStackTrace();
    }
  }
  
  /**
   * Sniff the network connection by dumping the information received
   * from both sides of the streams. Spawn two child threads and control them
   * so as to allow an independent flow of information in both sides. 
   */
  private void sniffConnection()
  {
    serverInterface.start();
    clientInterface.start();
  }

  /**
   * Shutdown the network connections closing the streams. 
   * 
   * @throws IOException
   */
  private void shutdown() throws IOException
  {
    try
    {
      if(serverInterface != null)
      {
        serverInterface.close();
      }
    }
    finally
    {
      if(clientInterface != null)
      {
        clientInterface.close();
      }
    }
  }

  /**
   * @return the inputPortNumber
   */
  public int getInputPortNumber()
  {
    return inputPortNumber;
  }

  /**
   * @return the outputPortNumber
   */
  public int getOutputPortNumber()
  {
    return outputPortNumber;
  }

  /**
   * @return the serverSocket
   */
  public ServerSocket getServerSocket()
  {
    return serverSocket;
  }

  /**
   * This method enable the serverSocket, which will listen until a connection
   * arrives. When this happens it tries to connect to the target server.
   * If it succeeds, it create the two communication interfaces to handle 
   * data forwarding on both sides of the flow.
   * 
   * After this method completion, the sniffer will be ready to 
   * fork threads to manage the connections. It should then sit
   * and wait until all activities are done. Then it should shutdown.
   * 
   * @throws IOException 
   */
  public void startServer() throws IOException
  {
    if (serverSocket == null)
    {
      int port = getInputPortNumber();
      serverSocket = new ServerSocket(port);
      
      System.out.println("  Server running: network data will be logged.");
      System.out.print("  Waiting for a new connection. Port: ");
      System.out.println(port);
      
      serverInputSocket = serverSocket.accept();
      
      System.out.println("  Connected. Attaching to the second server...");
      this.clientSocket = new Socket(getTargetHostname(), getOutputPortNumber());
      
      System.out.println("  Successfully connected to second server.");
      
      serverInterface = new SnifferCommunicationInterface(this, serverInputSocket);
      clientInterface = new SnifferCommunicationInterface(this, clientSocket);
      
      OutputStream serverLog = getServerLogOutputStream();
      OutputStream clientLog = getClientLogOutputStream();
      
      serverInterface.initialize(clientSocket.getOutputStream(), serverLog);
      clientInterface.initialize(serverInputSocket.getOutputStream(), clientLog);
      
      System.out.println("  Done. Will start logging now.");
    }
  }
  
  private OutputStream getServerLogOutputStream()
  {
    return serverLogOutputStream;
  }
  
  private OutputStream getClientLogOutputStream()
  {
    return clientLogOutputStream;
  }

  /**
   * Spawn a new serverSocket thread.
   * 
   * @return
   */
  public Thread spawnServerThread()
  {
    Thread task = new Thread(this, "Sniffer Thread");
    task.start();
    
    return task;
  }
  
  /**
   * @return the clientSocket
   */
  public Socket getClientSocket()
  {
    return clientSocket;
  }

  /**
   * @return the targetHostname
   */
  public String getTargetHostname()
  {
    return targetHostname;
  }
    
  private static SocketSniffer parseArguments(String args[])
  {
    int i = 0;
    SocketSniffer sniffer;

    if(args.length <= 0)
    {
      System.out.println("  Using default values for serverSocket and ports.");
      System.out.println();
      sniffer = new SocketSniffer();
    }
    else
    {
      sniffer = new SocketSniffer();
      for(i = 0; i < args.length; i++)
      {
        String parameter = args[i];
        handleParameter(sniffer, parameter);
      }
    }
    return sniffer;
  }
  
  // IMPROVE: use the ArgumentParser class here
  private static void handleParameter(SocketSniffer sniffer, String parameter)
  {
    if(parameter.startsWith(NetworkConstants.SERVER_FILE_PREFIX))
    {
      // log messages from server to the specified file
      String filename=parameter.substring(NetworkConstants.SERVER_FILE_PREFIX.length());
      OutputStream serverOut = createOutputFile(filename);
      sniffer.setServerLogOutputStream(serverOut);
    }
    
    if(parameter.startsWith(NetworkConstants.CLIENT_FILE_PREFIX))
    {
      // log messages from client to the specified file
      String filename=parameter.substring(NetworkConstants.CLIENT_FILE_PREFIX.length());
      OutputStream serverOut = createOutputFile(filename);
      sniffer.setClientLogOutputStream(serverOut);
    }
    
    if(parameter.startsWith(NetworkConstants.INPUT_PORT_PREFIX))
    {
      // define input port
      String data = parameter.substring(NetworkConstants.INPUT_PORT_PREFIX.length());
      int port;
      try
      {
        port = Integer.parseInt(data);
      }
      catch(NumberFormatException e)
      {
        // use default
        port = NetworkConstants.DEFAULT_INPUT_PORT_NUMBER;
      }
      sniffer.setInputPortNumber(port);
    }

    if(parameter.startsWith(NetworkConstants.OUTPUT_PORT_PREFIX))
    {
      // define input port
      String data = parameter.substring(NetworkConstants.OUTPUT_PORT_PREFIX.length());
      int port;
      try
      {
        port = Integer.parseInt(data);
      }
      catch(NumberFormatException e)
      {
        // use default
        port = NetworkConstants.DEFAULT_OUTPUT_PORT_NUMBER;
      }
      sniffer.setOutputPortNumber(port);
    }
    
    if(parameter.startsWith(NetworkConstants.TARGET_HOST_PREFIX))
    {
      // define input port
      String host = parameter.substring(NetworkConstants.TARGET_HOST_PREFIX.length());
      sniffer.setTargetHostname(host);
    }
  }

  private static OutputStream createOutputFile(String filename)
  {
    OutputStream serverOut;
    try
    {
      serverOut = new BufferedOutputStream(new FileOutputStream(filename));
      System.out.println("  Output log file: <" + filename + ">");
    }
    catch (FileNotFoundException e)
    {
      System.out.print("  Failure creating output log file: <");
      System.out.print(filename);
      System.out.println(">.");
      System.out.println("  Using a simple Counter instead.");
      serverOut = new CountOutputStream();
      
      e.printStackTrace();
    }
    return serverOut;
  }

  public static void main(String args[]) throws IOException, InterruptedException
  {
    SocketSniffer sniffer;

//    System.out.println();
//    System.out.println("  Reading input data...");
//    System.out.println("  Launching network logger...");
//    
    sniffer = parseArguments(args);
    
    System.out.println("  Input port:" + sniffer.getInputPortNumber());
    System.out.println("  Target serverSocket:" + sniffer.getTargetHostname());
    System.out.println("  Output port:" + sniffer.getOutputPortNumber());
    System.out.println();
    
    Thread thread = sniffer.spawnServerThread();
    
//    System.out.println("  Sniffer up and running. Network packets will be logged.");
    
    // the solution below works but it's not efficient. Use joint() instead.
//    while(sniffer.isWorking())
//    {
//      Thread.sleep(1000);
//    }
    try
    {
//      sniffer.join();
      thread.join();
    }
    catch(InterruptedException exception)
    {
      System.out.println("  Interrupted.");
    }
    
//    System.out.println("  Closing logger...");
    
    System.out.println("Done.");
    System.out.println();
  }

  /**
   * @param clientLogOutputStream the clientLogOutputStream to set
   */
  private void setClientLogOutputStream(OutputStream clientLogOutputStream)
  {
    this.clientLogOutputStream = clientLogOutputStream;
  }

  /**
   * @param serverLogOutputStream the serverLogOutputStream to set
   */
  private void setServerLogOutputStream(OutputStream serverLogOutputStream)
  {
    this.serverLogOutputStream = serverLogOutputStream;
  }

  private void setInputPortNumber(int inputPortNumber)
  {
    this.inputPortNumber = inputPortNumber;
  }

  private void setOutputPortNumber(int outputPortNumber)
  {
    this.outputPortNumber = outputPortNumber;
  }

  private void setTargetHostname(String targetHostname)
  {
    this.targetHostname = targetHostname;
  }
}
