package test.tcpip;

import java.net.*;
import java.io.*;


public class TestTcp{

public static void main(String[] args){
	
	System.out.println("TestTcp started");
    try{
    Socket socket = new Socket("129.168.0.123",44 );
         PrintStream os = new PrintStream(socket.getOutputStream());
       os.println("est MF");
       os.flush();
    // DataInputStream is = new DataInputStream(socket.getInputStream());
    InputStreamReader is = new InputStreamReader(socket.getInputStream());
   int fromServer;
   while ((fromServer = is.read()) != -1) {
    System.out.println("Server: " + (char)fromServer);
   }
  }catch (Exception e){}

}


}
