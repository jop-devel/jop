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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.constants.NetworkConstants;
import com.jopdesign.debug.jdwp.io.FilterPrintStream;
import com.jopdesign.debug.jdwp.util.ArgumentParser;
import com.jopdesign.debug.jdwp.util.StringList;

/**
 * JopServer.java
 * 
 * This is a very simple class to provide a network connection
 * to the JopSim debugger without interfering on the simulator
 * code.
 * 
 * This class override the default input and output streams
 * and launch the simulator. Inside the simulator, one class
 * can communicate with the "outside world" implementing the
 * protocol.  
 * 
 * It uses the standard output and input streams to implement
 * a  very simple (JDWP inspired) protocol which can be used 
 * to transport two or more streams over one single channel.
 * 
 * @author Paulo Abadie Guedes
 * 29/05/2007 - 15:08:42
 * 
 */
public class JopServer implements Runnable
{
  private StringList settings;
  
  private ArgumentParser parser;
  
  private ServerSocket serverSocket;
  private Socket socket;
  
  private FilterPrintStream filterPrintStream;
  
  // variables to hold the standard input and output streams
  private InputStream standardInput;
  private PrintStream standardOutput;
  
  // the launcher responsible to setup an instance of a JOP machine
  private JopLauncher launcher;
  
  private String arguments[];
  private boolean finished;
  
  public JopServer(JopLauncher jopLauncher)
  {
    this();
    launcher = jopLauncher;
  }
  
  public JopServer()
  {
    settings = 
      new StringList(NetworkConstants.NETWORK_SETTINGS);
    settings.add(JopLauncher.JOP_LAUNCHER_PREFIX);
    
    // set recognizable parameters and its default values
    parser = new ArgumentParser(settings);
    parser.parseArguments(NetworkConstants.DEFAULT_SETTINGS_JOP_SERVER);
    parser.parse(JopLauncher.DEFAULT_SETTINGS_JOP_LAUNCHER);
    
    // keep it just in case.
    standardInput = System.in;
    standardOutput = System.out;
    
    // as a default, use the simulator to run the program.
    launcher = new JopSimLauncher();
  }
  
  /**
   * @throws IOException 
   *
   */
  private void replaceStreams() throws IOException
  {
    OutputStream outputStream = socket.getOutputStream();
    PrintStream socketPrintStream = new PrintStream(outputStream);
    
    filterPrintStream = new FilterPrintStream(standardOutput,
        socketPrintStream);
    
    System.setOut(filterPrintStream);
    
    // replace the input stream with the socket input stream.
    // Now an external tool can send commands directly to the
    // program running inside JOP.
    InputStream input = socket.getInputStream();
    System.setIn(input);
  }
  
  private void restoreStreams() throws IOException
  {
    //restore the streams back to normal. It's always good to cleanup.
    System.setOut(standardOutput);
    System.setIn(standardInput);
    
    // close the filterStream.
    filterPrintStream.close();
  }
  /**
   * @param args
   * @throws IOException 
   */
  private void launchServer(String[] args) throws IOException
  {
    parser.parseArguments(args);
    
    int port = parser.getValueAsInt(NetworkConstants.INPUT_PORT_PREFIX);
    
    System.out.print(" Server launched at port: ");
    System.out.println(port);
    
    serverSocket = new ServerSocket(port);
    socket = serverSocket.accept();
  }
  
  private String[] removeSettings(String[] args)
  {
    return StringList.removeElements(args, settings);
  }
  
  /**
   * Handshake here, just to avoid that a faster client start sending data
   * before the streams are ready, and the machine is up and running.
   * 
   * @throws IOException 
   * 
   */
  private void handshake() throws IOException
  {
    int read = 0;
    byte[] handshake;
    int length;
    byte[] data;
                           
    handshake = JDWPConstants.JDWP_HANDSHAKE_BYTES;
    length = handshake.length;
    data = new byte[length];
    
    // send a handshake packet. Could be anything here, but
    // let's reuse the JDWP handshake constant.
    OutputStream outputStream = socket.getOutputStream();
    outputStream.write(JDWPConstants.JDWP_HANDSHAKE_BYTES);
    
    InputStream inputStream = socket.getInputStream();
    while(read < length)
    {
      read = inputStream.read(data, read, length - read);
    }
    
    if(JopDebuggerUtil.arrayEquals(data, handshake) == false)
    {
      throw new IOException("  Failure during handshake. Expected: " + 
        (new String(handshake)) + " Received: " + (new String(data)));
    }
  }

  /**
   * @param args 
   * @return 
   * @throws ClassNotFoundException 
   * 
   */
  private Thread launchJop(String[] args) throws Exception
  {
    arguments = args;
    
    Thread thread = new Thread(this);
    thread.start();
    
    return thread;
  }
  
  public void run()
  {
    finished = false;
    
//  // load a class that implements JopLauncher using reflection
//  String launcherName = parser.getValue(JopLauncher.JOP_LAUNCHER_PREFIX);
//  
//  // I don't like to use reflection (this is the only place until now),
//  // but it was a simple way to solve the issue. Maybe fix it later.
//  Class launcherClass = Class.forName(launcherName);
//  JopLauncher launcher = (JopLauncher) launcherClass.newInstance();
  
    // launch an instance of JOP somehow. The default uses the simulator, 
    // but nothing prevents it to be a class that run the real machine.
    launcher.launchJop(arguments);
    
    // after the launcher return, set the "finished" flag 
    finished = true;
  }
  
  public boolean hasFinished()
  {
    return finished;
  }
  
  /**
   * 
   */
  private void checkConnection()
  {
    if(socket.isClosed())
    {
      finished = true;
    }
  }
  
  public void execute(String args[])
  {
    // launch the server and wait for a connection
    try
    {
      launchServer(args);
      // remove the network parameters, to avoid the risk of 
      // confusing other tools
      args = removeSettings(args);
      
      // replace the current input/output streams to talk to the Jop machine
      replaceStreams();
      
      // now that the setup is done, handshake to start the process
      handshake();
//    
//    // now run Jop
      //JopSim.main(args);
      Thread launcher = launchJop(args);
      
      while(hasFinished() == false)
      {
        Thread.sleep(2000);
        checkConnection();
      }
//      launcher.join();
      launcher.interrupt();
      
      restoreStreams();
      
      // force the machine to exit.
      System.exit(0);
    }
    catch (IOException exception)
    {
      System.err.println("  Failure.");
      System.err.println(exception.getLocalizedMessage());
      exception.printStackTrace();
    }
    catch(Exception exception)
    {
      System.err.println("  Failure loading class for JopLauncher.");
      System.err.println(exception.getLocalizedMessage());
      exception.printStackTrace();
    }
  }
  
  /**
   * Create a new server and call "execute(args)" with the 
   * same parameters provided to the "main" method.
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    JopServer server = new JopServer();
    server.execute(args);
  }
}