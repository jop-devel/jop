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
import com.jopdesign.jopui.helper.Color;
import com.jopdesign.jopui.helper.Color8Bit;

/**
 * Implements a button
 */
public class Button extends Label {

	/**
	 * If a button is released <p>
	 * Constant has the value 0
	 */
	public static final int UP = 0;
	
	/**
	 * If a button is pressed <p>
	 * Constant has the value 1
	 */
	public static final int DOWN = 1;
	
	private int state = UP;
	private int colorLight;
	private int colorDark;

	/**
	 * Creates a new Button with the given dimension and a text
	 * @param x x-coordinate of the position  
	 * @param y y-coordinate of the position
	 * @param width width of the button
	 * @param height height of the button
	 * @param text text on the button
	 */
	public Button(int x, int y, int width, int height, String text) {
		super(x, y, width, height, text);
		defaultValues();
	}
	
	/**
	 * Creates a new Button with the given dimension and a text
	 * @param bounds dimension of the button
	 * @param text text on the button
	 */
	public Button(Bounds bounds, String text) {
		super(bounds, text);
		defaultValues();
	}

	private void defaultValues(){
		colorLight = Theme.colorButtonLight;
		colorDark = Theme.colorButtonDark;
		colorBody = Theme.colorButtonBackground;
		colorBorder = Theme.colorKey;
		colorText = Theme.colorButtonText;
		leftMargin = Theme.marginButtonLeft;
		rightMargin = Theme.marginButtonRight;
		topMargin = Theme.marginButtonTop;
		bottomMargin = Theme.marginButtonBottom;
		halign = Theme.halignButton;
		valign = Theme.valignButton;
	}
	
	protected Image create() {
		img = super.create();
		Graphics g = img.getGraphics();
		int width = bounds.getWidth();
		int height = bounds.getHeight();
				
		if(state == UP) {
			g.setColor(colorDark);
			g.drawLine(0,height-1,width-1,height-1);
			g.drawLine(width-1,0,width-1,height-1);
			g.setColor(colorLight);
			g.drawLine(0,0,width-1,0);
			g.drawLine(0,0,0,height-1);
		} else {
			g.setColor(colorLight);
			g.drawLine(0,height-1,width-1,height-1);
			g.drawLine(width-1,0,width-1,height-1);
			g.setColor(colorDark);
			g.drawLine(0,0,width-1,0);
			g.drawLine(0,0,0,height-1);
		}
		
		if(isFocus()) { // underline on focus
			g.setColor(Theme.focusColor);
			g.drawLine(leftMargin, height-bottomMargin-2, width-rightMargin-1, height-bottomMargin-2);
		}
		
		return img;
	}
	
	/**
	 * Captures the Button related events and passes events to super method<p>
	 * @see com.jopdesign.jopui.core.Component#distribute(JopEvent)
	 */
	public boolean distribute(JopEvent ev) {
		boolean ret;
		if(ev.getEventType() == JopEvent.KEYBOARD_EVENT) { // distribute KEYBOARD_EVENTS in special way
			
			KeyboardEvent kbev = (KeyboardEvent) ev;			
			
			if(kbev.getCharacter() == 0x0d || kbev.getCharacter() == 0x0a || kbev.getCharacter() == ' ') {
				if(kbev.getAction() == KeyboardEvent.KEY_PRESSED)
					state = DOWN;
				else
					state = UP;
				changed = true;
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
				this.makeFocus();
				
				if(mev.getAction() == MouseEvent.MOUSE_UP) {
					this.state = UP;
				} else {
					this.state = DOWN;
				}
 			}
		}
		
		ret = super.distribute(ev); 
		return ret || changed;
	}
	
	/**
	 * Returns the type of component<p>
	 * @see com.jopdesign.jopui.core.Component#getComponentType()
	 */
	public int getComponentType() {
		return BUTTON;
	}
	
	/**
	 * Returns the current state of the button
	 * @return Returns UP or DOWN
	 */
	public int getState() {
		return state;
	}

	/**
	 * Set the current state of the button
	 * @param state has to be UP or DOWN
	 */
	public void setState(int state) {
		changed = true;
		this.state = state;
	}

	/**
	 * Returns the light color of the button
	 * @return light color
	 */
	public int getColorLight() {
		return colorLight;
	}

	/**
	 * Set the light color of the button
	 * @param c light color
	 */
	public void setColorLight(Color c) {
		changed = true;
		this.colorLight = c.getColor();
	}

	/** 
	 * Returns the dark color of the button
	 * @return dark color
	 */
	public int getColorDark() {
		return colorDark;
	}

	/**
	 * Set the dark color of the button
	 * @param c dark color
	 */
	public void setColorDark(Color c) {
		changed = true;
		this.colorDark = c.getColor();
	}
}
