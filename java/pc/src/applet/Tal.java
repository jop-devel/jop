import java.applet.Applet;
import java.awt.Graphics;

import java.net.*;
import java.io.*;

/**
*	an Applet to query actual data (data.txt) from TAL.
*/

public class Tal extends Applet implements Runnable {

	String inputLine;

	public void start() {

		new Thread(this).start();
	}

	public void run() {

	    while (true) {
			try {
				BufferedReader in = new BufferedReader(
					new InputStreamReader(
					(new URL(getCodeBase().toString()+"data.txt")).openStream()
				));
				inputLine = in.readLine();
				in.close();
			} catch (Exception e) {
			}
	        repaint();
	        try {
	            Thread.sleep(1000);
	        } catch (InterruptedException e) {
	            break;
	        }
	    }
	}

	public void paint(Graphics g) {

			g.drawString("Data: " + inputLine, 25, 25);
	}
}

