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

package com.jopdesign.jopui.core;

import com.jopdesign.jopui.event.JopEvent;
import com.jopdesign.jopui.event.KeyboardEvent;
import com.jopdesign.jopui.event.MouseEvent;
import com.jopdesign.jopui.helper.Color8Bit;

/**
 * Implements a TextFiled
 */
public class TextField extends Label {

	private int colorLight;
	private int colorDark;
	
	private int cursorPos;
	private int visibleBegin;
	private String text;
	
	/**
	 * Create a new TextField with the given dimension and text
	 * @param bounds boundaries of the textfield
	 * @param text text shown by the textfield
	 */
	public TextField(Bounds bounds, String text) {
		super(bounds, text);
		this.setText(text);
		defaultValues();
	}
	
	/**
	 * Creates a new TextField with the given dimension and text
	 * @param x x-coordinate of the upper left corner in pixel
	 * @param y y-coordinate of the upper left corner in pixel
	 * @param width width of the textfield in pixel
	 * @param height height of the textfield in pixel
	 * @param text text shown by the textfield
	 */
	public TextField(int x, int y, int width, int height, String text) {
		super(x, y, width, height, text);
		this.setText(text);
		defaultValues();
	}
	
	private void defaultValues() {
		
		colorLight = Theme.colorTextFieldLight;
		colorDark = Theme.colorTextFieldDark;
		
		setColorBody(new Color8Bit(Theme.colorTextFieldBackground));
		setColorBorder(new Color8Bit(Theme.colorKey));
		setColorText(new Color8Bit(Theme.colorTextFieldText));
		setLeftMargin(Theme.marginTextFieldLeft);
		setRightMargin(Theme.marginTextFieldRight);
		setTopMargin(Theme.marginTextFieldTop);
		setBottomMargin(Theme.marginTextFieldBottom);
		setHalign(Theme.halignTextField);
		setValign(Theme.valignTextField);
	}
	
	protected Image create() {
		img = null;		
		super.setText(text.substring(visibleBegin));

		img = super.create();
		Graphics g = img.getGraphics();
		int width = bounds.getWidth();
		int height = bounds.getHeight();
		
		if(isFocus()) {
			g.setColor(Theme.focusColor);
			int x = leftMargin+getCharWidth()*(cursorPos-visibleBegin);
			g.drawLine(x, topMargin, x, height-bottomMargin);
		}
		
		g.setColor(colorLight);
		g.drawLine(0,height-1,width-1,height-1);
		g.drawLine(width-1,0,width-1,height-1);
		g.setColor(colorDark);
		g.drawLine(0,0,width-1,0);
		g.drawLine(0,0,0,height-1);
		return img;
	}
	
	/**
	 * Captures the TextField related events and passes events to super method<p>
	 * @see com.jopdesign.jopui.core.Component#distribute(JopEvent)
	 */
	public boolean distribute(JopEvent ev) {
		boolean ret;
		if(ev.getEventType() == JopEvent.KEYBOARD_EVENT) { // distribute KEYBOARD_EVENTS in special way
			
			KeyboardEvent kbev = (KeyboardEvent) ev;			
			char c = kbev.getCharacter();
			int sc = kbev.getScanCode();
			
			if(kbev.getAction() == KeyboardEvent.KEY_PRESSED) {
			
				//System.out.print("sc = 0x");
				//System.out.println(Integer.toHexString(sc));
			
				switch(sc) {
					case 0x08: // Backspace
					case 0x66:
						if(cursorPos == 0)
							return super.distribute(ev);
							
						text = text.substring(0, cursorPos - 1) + text.substring(cursorPos);
						cursorPos--;
						changed = true;
						break;
						
					case 0x25: // Left
					case 0xe06b:
						cursorPos--;
						changed = true;
						break;
						
					case 0x27: // Right
					case 0xe074:
						cursorPos++;
						changed = true;
						break;
						
					default:
					
						if(c < 0x20 || c > 0x7F)
							return super.distribute(ev);
						
						text = text.substring(0, cursorPos) + c + text.substring(cursorPos); ; //text + c;
						cursorPos++;
						changed = true;
					
				}
				
				if(cursorPos < 0)
					cursorPos = 0;
				
				if(cursorPos > text.length())
					cursorPos = text.length();
					
				if(cursorPos < visibleBegin)
					visibleBegin = cursorPos;
					
				if(cursorPos > visibleBegin + getVisibleCharCount())
					visibleBegin = cursorPos - getVisibleCharCount();			
				
				
				super.setText(text.substring(visibleBegin));
			}
		}
		
		if(ev.getEventType() == JopEvent.MOUSE_EVENT) { // distribute KEYBOARD_EVENTS in special way
			
			MouseEvent mev = (MouseEvent) ev;			
			
			if(!(mev.getX() >= bounds.getX() &&
			     mev.getX() <= bounds.getX() + bounds.getWidth() &&
			     mev.getY() >= bounds.getY() &&
			     mev.getY() <= bounds.getY() + bounds.getHeight()))
				return false; // mouse events are not distributed if they are out of scope
			
			if(mev.getButton() == MouseEvent.LEFT_BUTTON) {
			
				changed = true;
				
				if(mev.getAction() == MouseEvent.MOUSE_UP) {
					this.makeFocus();
				}
 			}
		}
		
		ret = super.distribute(ev); 
		return ret || changed;
	}
	
	/**
	 * Set the text, which will be shown inside the textbox
	 * @see com.jopdesign.jopui.core.Label#setText(java.lang.String)
	 */
	public void setText(String text) {
		changed = true;
		if(text == null)
			text = "";
			
		this.text = text;
		cursorPos = text.length();
		visibleBegin = 0;
	}
	
	/**
	 * Returns the type of component
	 * @see com.jopdesign.jopui.core.Label#getComponentType()
	 */
	public int getComponentType() {
		return TEXTFIELD;
	}
	
}
