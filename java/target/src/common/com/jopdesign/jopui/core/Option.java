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
 * Implement an option element
 */
public class Option extends Label {

	/**
	 * If option element is marked <p>
	 * Constant has the value 0
	 */
	public static final int MARKED = 0;
	
	/**
	 * If option element is unmarked <p>
	 * Constant has the value 1
	 */
	public static final int UNMARKED = 1;
	
	private int state = UNMARKED;
	private int colorLight;
	private int colorDark;
	private int colorFill;
	private int colorMark;
	private int markSize;
	private OptionGroup optGroup = null;

	/**
	 * Creates an option element with the given dimension and text
	 * @param x x-coordinate of the upper left corner in pixel
	 * @param y y-coordinate of the upper left corner in pixel
	 * @param width width of the option element in pixel
	 * @param height height of the option element in pixel
	 * @param text label for the option element
	 */
	public Option(int x, int y, int width, int height, String text) {
		super(x, y, width, height, text);
		defaultValues();
	}
	
	/**
	 * Creates an option element with the given dimension and text
	 * @param bounds boundaries for the option element
	 * @param text label for the option element
	 */
	public Option(Bounds bounds, String text) {
		super(bounds, text);
		defaultValues();
	}

	private void defaultValues() {
		
		colorLight = Theme.colorOptionLight;
		colorDark = Theme.colorOptionDark;
		colorFill = Theme.colorOptionFill;
		colorMark = Theme.colorOptionMark;
		markSize = Theme.colorOptionSize;
		
		setColorBody(new Color8Bit(Theme.colorOptionBackground));
		setColorBorder(new Color8Bit(Theme.colorKey));
		setColorText(new Color8Bit(Theme.colorOptionText));
		this.setLeftMargin(Theme.marginOptionLeft);
		setRightMargin(Theme.marginOptionRight);
		setTopMargin(Theme.marginOptionTop);
		setBottomMargin(Theme.marginOptionBottom);
		setHalign(Theme.halignOption);
		setValign(Theme.valignOption);
	}
	
	protected Image create() {
		img = super.create();
		Graphics g = img.getGraphics();
		int width = bounds.getWidth();
		int height = bounds.getHeight();
		int y = height>>1;
		int x = (markSize>>1) + 1;
		
		g.setColor(colorFill);
		g.fillArc(x, y, (markSize>>1) - 1, (markSize>>1) - 1);
		
		if(state == MARKED) {
			g.setColor(colorMark);
			g.fillArc(x, y , markSize>>2, markSize>>2);
		}

		
		if(isFocus()) {
			g.setColor(Theme.focusColor);
		} else {
			g.setColor(colorDark);
		}
		
		g.drawArc(x, y, markSize>>1, markSize>>1);
		
		return img;
	}
	
	/**
	 * Captures the Option element related events and passes events to super method<p>
	 * @see com.jopdesign.jopui.core.Component#distribute(JopEvent)
	 */
	public boolean distribute(JopEvent ev) {
		boolean ret;
		if(ev.getEventType() == JopEvent.KEYBOARD_EVENT) { // listen for KEYBOARD events
			
			KeyboardEvent kbev = (KeyboardEvent) ev;			
			
			if(kbev.getCharacter() == 0x0d || kbev.getCharacter() == 0x0a || kbev.getCharacter() == ' ') {
				if(kbev.getAction() == KeyboardEvent.KEY_PRESSED) {
					
					if(this.state == UNMARKED) {
						changed = true;				
						this.state = MARKED;
						if(optGroup != null)
							optGroup.sendMark(this);
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
				
				if(this.state == UNMARKED) {
					changed = true;
					this.makeFocus();
					this.state = MARKED;
					if(optGroup != null)
						optGroup.sendMark(this);
				}
 			}
		}
		
		ret = super.distribute(ev); 
		return ret || changed;
	}
	
	/**
	 * Returns the type of component
	 * @see com.jopdesign.jopui.core.Component#getComponentType()
	 */
	public int getComponentType() {
		return OPTION;
	}
	
	/**
	 * Returns the current state of the option element
	 * @return MARKED or UNMARKED
	 */
	public int getState() {
		return state;
	}

	/**
	 * Set the state of the option element <p>
	 * (Should be changed after adding the OptionGroup.)
	 *
	 * @param state state has to be MARKED or UNMARKED
	 */
	public void setState(int state) {
		changed = true;
		if(optGroup != null && state == MARKED)	
			optGroup.sendMark(this);
			
		this.state = state;
	}
	
	/**
	 * Returns the light color of the option element
	 * @return light color
	 */
	public int getColorLight() {
		return colorLight;
	}

	/**
	 * Set the light color of the option element
	 * @param c light color
	 */
	public void setColorLight(Color c) {
		changed = true;
		this.colorLight = c.getColor();
	}

	/**
	 * Returns the dark color of the option element
	 * @return dark color
	 */
	public int getColorDark() {
		return colorDark;
	}

	/**
	 * Set the dark color of the option element
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
		return this.leftMargin - markSize - 2;
	}
	
	/**
	 * Override the method from label class <p>
	 * @see com.jopdesign.jopui.core.Label#setLeftMargin(int)
	 */
	public void setLeftMargin(int leftMargin) {
		changed = true;
		this.leftMargin = leftMargin + markSize + 2;
	}
	
	/**
	 * Returns the fill color of the option element
	 * @return fill color
	 */
	public int getColorFill() {
		return colorFill;
	}

	/**
	 * Set the fill color for the the option element
	 * @param c fill color
	 */
	public void setColorFill(Color c) {
		changed = true;
		this.colorFill = c.getColor();
	}
	
	/**
	 * Returns the color for a marked option element
	 * @return color
	 */
	public int getColorMark() {
		return colorMark;
	}

	/**
	 * Set the color for a marked option element
	 * @param c color
	 */
	public void setColorMark(Color c) {
		changed = true;
		this.colorMark = c.getColor();
	}
	
	/**
	 * Returns the size of the marker 
	 * @return size
	 */
	public int getMarkSize() {
		return markSize;
	}

	/**
	 * Set the size of the marker
	 * @param markSize size
	 */
	public void setMarkSize(int markSize) {
		changed = true;
		this.markSize = markSize;
	}
	
	/**
	 * Returns the option group at which the option element is part of
	 * @return member of which option group
	 */
	public OptionGroup getOptionGroup() {
		return optGroup;
	}

	/**
	 * Set the option element to a specific option group
	 * @param g become member for this option group
	 */
	public void setOptionGroup(OptionGroup g) {
		this.optGroup = g;
	}	
}
