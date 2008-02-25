/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Nelson Langkamp

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
