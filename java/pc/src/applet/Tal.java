/*
  This file is part of JOP, the Java Optimized Processor

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package applet;

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

