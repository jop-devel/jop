/*
 * This file is a part of the Jop-UI 
 * Copyright (C) 2009, 	Stefan Resch (e0425306@student.tuwien.ac.at)
 * 						Stefan Rottensteiner (e0425058@student.tuwien.ac.at)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.jopui;

import java.util.Enumeration;
import java.util.Vector;

import com.jopdesign.jopui.core.Graphics;
import com.jopdesign.jopui.core.Image;
import com.jopdesign.jopui.core.Theme;
import com.jopdesign.jopui.event.KeyboardEvent;
import com.jopdesign.jopui.event.MouseEvent;
import com.jopdesign.jopui.helper.KeyBoard;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

/**
 * Start point for JopUi <p>
 * initializes and starts all JopUi applications 
 */

public final class JopUi {

	private static int mouseX;
	private static int mouseY;
	
	private static int button_left = 0;
	private static int button_right = 0;
	private static int button_middle = 0;	
	
	private static KeyBoard keyboard = new KeyBoard();
	
	private static Vector apps = new Vector();
	
	/**
	 * Start address of video memory <p>
	 * Constant has the value 0x78500
	 */
	public static final int MEM_START = 0x78500;
	
	/**
	 * Width of the screen in pixel <p>
	 * Constant has the value 320
	 */
	public static final int WIDTH = 320;
	
	/**
	 * Height of the screen in pixel <p>
	 * Constant has the value 240
	 */
	public static final int HEIGHT = 240;
	
	private static Image fb = Image.createImage(WIDTH,HEIGHT);
	
	/**
	 * Returns the framebuffer, where all images will be drwan to 
	 * @return framebuffer image
	 */
	public static Image getFrameBuffer() {
		return fb;
	}
	
	/**
	 * Adds an application to the execution list
	 * @param app JopUiApplication
	 */
	public static void register(JopUiApplication app) {
			apps.addElement(app);
	}
	
	/**
	 * Run the applications
	 */
	public static void run() {
		
		Enumeration e;
		JopUiApplication app;
		KeyboardEvent keyboardEvent = null;
		MouseEvent mouseEvent = null;
		fb.setColorKey(Theme.colorKey);
		Graphics g = fb.getGraphics();
		Graphics.enable(Graphics.COLOR_KEY);
		int oldMouseX = 0;
		int oldMouseY = 0;
		mouseX = fb.getWidth()>>1;
		mouseY = fb.getHeight()>>1;
				
		boolean redraw;
		
		e = apps.elements();
		while(e.hasMoreElements()) {
			app = (JopUiApplication) e.nextElement();
			if(app.init() == false);		// TODO Error Message and remove app from list
								// as soon as there is a scheduling algorithm 
		}
		
		
		g.setColor(Theme.colorBackground);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		e = apps.elements();				// initial draw
		while(e.hasMoreElements()) {
			app = (JopUiApplication) e.nextElement();
			app.paint(g);
		
		}
		
		while(true) {
			
			keyboardEvent = readKeyboard();
			mouseEvent = readMouse();
			
			redraw = false;
			
			e = apps.elements();
			while(e.hasMoreElements()) {
				app = (JopUiApplication) e.nextElement();
				if(keyboardEvent != null)
					redraw |= app.distribute(keyboardEvent);
				if(mouseEvent != null)
					redraw |= app.distribute(mouseEvent);
			}
			
			if(redraw) {	
				g.setColor(Theme.colorBackground);
				g.fillRect(0, 0, WIDTH, HEIGHT);
				
				e = apps.elements();
				while(e.hasMoreElements()) {
					((JopUiApplication) e.nextElement()).paint(g);
				}
			}
			
			if((oldMouseX != mouseX) || (oldMouseY != mouseY))
				redraw = true;
			
			if(redraw) {
				int [] data = fb.getData();
				int [] pdata = new int[3*7];
				
				
				int adr;
				int absolute_adr;
				int adroffset;

				for(int i = 0; i < 3; i++) {		// copy destination area for mouse
					for(int j = 0; j < 7; j++) {
						adr = (mouseX>>2) + i + (mouseY + j) * (WIDTH>>2);
						if(adr < data.length)
							pdata[i + j*3] = data[adr];
					}
				}
				
				
				
				for(int i = 0; i < 7; i++) {		// draw mouse
					for(int j = 0; j < 7; j++) {
				
						int col = -1;
						
						if(i == 0) {
							col = Theme.mouseColorLight;
						} else if (i == j) {					
							col = Theme.mouseColorLight;
						} else if (i < j) {
							col = Theme.mouseColorDark;
						}
 						
						if(col != -1) {
							
							adr = (mouseX & 0x03) + i + j*12;
							absolute_adr = (adr>>2);
							adroffset = 8*(adr & 0x03);
							pdata[absolute_adr] &= ~(0xFF << adroffset);
							pdata[absolute_adr] |= col << adroffset;
						}
					}
				}
				
				int mx = -((mouseX-1)>>2);
				int my = -mouseY-1;
				
				for(int i=0; i<(data.length-1); ++i) { // TODO remeber to change this value when changing Image class
					
					mx++;
					
					if((i % (WIDTH>>2)) == 0) {
						mx = -(mouseX>>2);
						my++;
					}
					
									// either draw from mouse field or whole
					if(mx >= 0 && mx <= 2 && my >= 0 && my <= 6) {
						Native.wr(pdata[mx + my*3],MEM_START + i);
					} else {
						Native.wr(data[i],MEM_START + i);
					}
				}
			}
			
			oldMouseX = mouseX;
			oldMouseY = mouseY;
			// TODO other tasks?
		}
		
	}
	
	private static KeyboardEvent readKeyboard() {
		int ctrl = keyboard.getCtrlReg();
		KeyboardEvent kbev = null;
		
		if((ctrl & KeyBoard.MSK_ASCII_RDY) != 0 || (ctrl & KeyBoard.MSK_SCC_RDY) != 0) {
			if((ctrl & KeyBoard.MSK_KEY_REL) != 0) {
				kbev = new KeyboardEvent(KeyboardEvent.KEY_RELEASED,(char)keyboard.getAscii(),keyboard.getScanCode());
				System.out.print("event -> kb_up : ");
				System.out.println((char)keyboard.getAscii());
				System.out.print(" == 0x");				
				System.out.println(Integer.toHexString(kbev.getScanCode()));
			} else {
				kbev = new KeyboardEvent(KeyboardEvent.KEY_PRESSED,(char)keyboard.getAscii(),keyboard.getScanCode());
				System.out.print("event -> kb_down : ");
				System.out.print((char)keyboard.getAscii());
				System.out.print(" == 0x");				
				System.out.println(Integer.toHexString(kbev.getScanCode()));
			}
		}
		
		return kbev;
	}
	
	private static MouseEvent readMouse() {
		// set mouseX mouseY
		// generate event
		MouseEvent me = null;
		int flags = Native.rd(Const.MOUSE_FLAG);
		if((flags & Const.MSK_DTA_RDY)!= 0) {
			mouseX += Native.rd(Const.MOUSE_X_INC);
			mouseY -= Native.rd(Const.MOUSE_Y_INC);
		}
		
		
		if(mouseX < 0)
			mouseX = 0;
		
		if(mouseX >= WIDTH)
			mouseX = WIDTH - 1;
		
		if(mouseY < 0)
			mouseY = 0;
		
		if(mouseY >= HEIGHT)
			mouseY = HEIGHT - 1;
		
		if((flags & Const.MSK_BTN_LEFT)!= button_left) {
			button_left = (flags & Const.MSK_BTN_LEFT);
			if(button_left == 0) {
				me = new MouseEvent(mouseX, mouseY, MouseEvent.MOUSE_UP, MouseEvent.LEFT_BUTTON);
				System.out.println("event -> mouse up : left");
			} else {
				me = new MouseEvent(mouseX, mouseY, MouseEvent.MOUSE_DOWN, MouseEvent.LEFT_BUTTON);
				System.out.println("event -> mouse down : left");
			}
		}
		
		
		if((flags & Const.MSK_BTN_MIDDLE)!= button_middle && me == null) {
			button_middle = (flags & Const.MSK_BTN_MIDDLE);
			if(button_middle == 0) {
				me = new MouseEvent(mouseX, mouseY, MouseEvent.MOUSE_UP, MouseEvent.MIDDLE_BUTTON);
				System.out.println("event -> mouse up : middle");
			} else {
				me = new MouseEvent(mouseX, mouseY, MouseEvent.MOUSE_DOWN, MouseEvent.MIDDLE_BUTTON);
				System.out.println("event -> mouse down : middle");
			}
		}
		
		
		if((flags & Const.MSK_BTN_RIGHT)!= button_right && me == null) {
			button_right = (flags & Const.MSK_BTN_RIGHT);
			if(button_right == 0) {
				me = new MouseEvent(mouseX, mouseY, MouseEvent.MOUSE_UP, MouseEvent.RIGHT_BUTTON);
				System.out.println("event -> mouse up : right");
			} else {
				me = new MouseEvent(mouseX, mouseY, MouseEvent.MOUSE_DOWN, MouseEvent.RIGHT_BUTTON);
				System.out.println("event -> mouse down : right");
			}
		}
		
		return me;
	}
}
