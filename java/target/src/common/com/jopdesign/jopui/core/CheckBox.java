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
 * Implements a checkbox
 */
public class CheckBox extends Label {

	/**
	 * If checkbox is marked <p>
	 * Constant has the value 0
	 */
	public static final int CHECKED = 0;
	
	/**
	 * If checkbox is unmarked <p>
	 * Constant has the value 1
	 */
	public static final int UNCHECKED = 1;
	
	private int state = UNCHECKED;
	private int colorLight;
	private int colorDark;
	private int colorFill;
	private int colorCheck;
	private int boxSize;

	/**
	 * Creates a new checkbox with the given dimension and a text
	 * @param x x-coordinate of the upper left corner 
	 * @param y y-coordinate of the upper left corner
	 * @param width width of the checkbox with text
	 * @param height height of the checkbox width text
	 * @param text label for the checkbox
	 */
	public CheckBox(int x, int y, int width, int height, String text) {
		super(x, y, width, height, text);
		defaultValues();
	}
	
	/**
	 * Creates a new checkbox with the given dimension and a text
	 * @param bounds boundaries of the checkbox
	 * @param text label for the checkbox
	 */
	public CheckBox(Bounds bounds, String text) {
		super(bounds, text);
		defaultValues();
	}

	private void defaultValues(){
		
		colorLight = Theme.colorCheckBoxLight;
		colorDark = Theme.colorCheckBoxDark;
		colorFill = Theme.colorCheckBoxFill;
		colorCheck = Theme.colorCheckBoxCheck;
		boxSize = Theme.colorCheckBoxSize;
		
		colorBody = Theme.colorCheckBoxBackground;
		colorBorder = Theme.colorKey;
		colorText = Theme.colorCheckBoxText;
		setLeftMargin(Theme.marginCheckBoxLeft);
		rightMargin = Theme.marginCheckBoxRight;
		topMargin = Theme.marginCheckBoxTop;
		bottomMargin = Theme.marginCheckBoxBottom;
		halign = Theme.halignCheckBox;
		valign = Theme.valignCheckBox;
	}
	
	protected Image create() {
		img = super.create();
		Graphics g = img.getGraphics();
		int width = bounds.getWidth();
		int height = bounds.getHeight();
		int y = (height-boxSize)>>1;
		int x = 1;
		
		g.fillRect(x + 1, y + 1, boxSize - 2, boxSize - 2);
		
		if(state == CHECKED) {
			g.setColor(colorCheck);
			g.drawLine(x + 2, y + boxSize - boxSize/3 - 2, x + boxSize/3, y + boxSize - 3);
			g.drawLine(x + boxSize/3, y + boxSize - 3, x + boxSize - boxSize/3, y + 3);
		}

		
		if(isFocus()) {
			g.setColor(Theme.focusColor);
			g.drawRect(x, y, boxSize, boxSize);
		} else {
			g.setColor(colorDark);
			g.drawLine(x, y, x + boxSize - 1, y);
			g.drawLine(x, y, x, y + boxSize - 1);
			g.setColor(colorLight);
			g.drawLine(x + boxSize - 1, y, x + boxSize - 1, y + boxSize - 1);
			g.drawLine(x, y + boxSize - 1, x + boxSize - 1, y + boxSize - 1);
			g.setColor(colorFill);
		}
		
		return img;
	}
	
	/**
	 * Captures the CheckBox related events and passes events to super method<p>
	 * @see com.jopdesign.jopui.core.Component#distribute(JopEvent)
	 */
	public boolean distribute(JopEvent ev) {
		boolean ret;
		if(ev.getEventType() == JopEvent.KEYBOARD_EVENT) { // listen for KEYBOARD events
			
			KeyboardEvent kbev = (KeyboardEvent) ev;			
			
			if(kbev.getCharacter() == 0x0d || kbev.getCharacter() == 0x0a || kbev.getCharacter() == ' ') {
				if(kbev.getAction() == KeyboardEvent.KEY_PRESSED) {
					
					changed = true;
					if(this.state == CHECKED) {
						this.state = UNCHECKED;
					} else {
						this.state = CHECKED;
					}
				}
			}
		}
		
		if(ev.getEventType() == JopEvent.MOUSE_EVENT) { // listen for MOUSE events
			
			MouseEvent mev = (MouseEvent) ev;			
			
			if(!(mev.getX() >= bounds.getX() &&
			     mev.getX() <= bounds.getX() + bounds.getWidth() &&
			     mev.getY() >= bounds.getY() &&
			     mev.getY() <= bounds.getY() + bounds.getHeight()))
				return false; // mouse events are not distributed if they are out of scope
			
			if(mev.getButton() == MouseEvent.LEFT_BUTTON &&
			   mev.getAction() == MouseEvent.MOUSE_UP) {
				
				changed = true;
				this.makeFocus();
				
				if(this.state == CHECKED) {
					this.state = UNCHECKED;
				} else {
					this.state = CHECKED;
				}
 			}
		}
		
		ret = super.distribute(ev); 
		return ret || changed;
	}
	
	/**
	 * Returns the type of component <p>
	 * @see com.jopdesign.jopui.core.Component#getComponentType()
	 */
	public int getComponentType() {
		return CHECKBOX;
	}
	
	/**
	 * Returns the current state of the checkbox
	 * @return CHECKED or UNCHECKED
	 */
	public int getState() {
		return state;
	}

	/**
	 * Set the current state of the checkbox
	 * @param state state has to be CHECKED or UNCHECKED
	 */
	public void setState(int state) {
		changed = true;
		this.state = state;
	}
	
	/**
	 * Returns the light color of the checkbox
	 * @return light color
	 */
	public int getColorLight() {
		return colorLight;
	}

	/**
	 * Set the light color for the checkbox
	 * @param c light color
	 */
	public void setColorLight(Color c) {
		changed = true;
		this.colorLight = c.getColor();
	}

	/**
	 * Returns the dark color for the checkbox
	 * @return dark color
	 */
	public int getColorDark() {
		return colorDark;
	}

	/**
	 * Set the dark color for the checkbox
	 * @param c dark color
	 */
	public void setColorDark(Color c) {
		changed = true;
		this.colorDark = c.getColor();
	}
	
	/**
	 * Override the method from label class <p>
	 * @see com.jopdesign.jopui.core.Label#getLeftMargin()
	 */
	public int getLeftMargin() {
		return this.leftMargin - boxSize - 1;
	}
	
	/**
	 * Override the method from label class <p>
	 * @see com.jopdesign.jopui.core.Label#setLeftMargin(int)
	 */
	public void setLeftMargin(int leftMargin) {
		changed = true;
		this.leftMargin = leftMargin + boxSize + 1;
	}
	
	/**
	 * Returns the fill color of the checkbox
	 * @return fill color
	 */
	public int getColorFill() {
		return colorFill;
	}

	/**
	 * Set the fill color of the checkbox
	 * @param c fill color
	 */
	public void setColorFill(Color c) {
		changed = true;
		this.colorFill = c.getColor();
	}
	
	/**
	 * Returns the color for a marked checkbox
	 * @return color
	 */
	public int getColorCheck() {
		return colorCheck;
	}

	/**
	 * Set the color for a marked checkbox
	 * @param c color
	 */
	public void setColorCheck(Color c) {
		changed = true;
		this.colorCheck = c.getColor();
	}
	
	/**
	 * Returns the size of the box
	 * @return size
	 */
	public int getBoxSize() {
		return boxSize;
	}

	/**
	 * Set the size of the box
	 * @param boxSize size
	 */
	public void setBoxSize(int boxSize) {
		changed = true;
		this.boxSize = boxSize;
	}
}
