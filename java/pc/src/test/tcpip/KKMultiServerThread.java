package test.tcpip;

import java.net.*;
import java.io.*;

public class KKMultiServerThread extends Thread {
    private Socket socket = null;

    public KKMultiServerThread(Socket socket) {
	super("KKMultiServerThread");
	this.socket = socket;
    }

    public void run() {

	try {
	    // PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
				    new InputStreamReader(
				    socket.getInputStream()));
	    


	    System.out.println("Server Thread runs!");

 
	    //  out.write("testmf");
	    //   out.flush();

	
	    int input = 0;
	    	    while((input = in.read())!= -1){
	    	    System.out.println("input: "+ (char)input);
	    	}
	  

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
