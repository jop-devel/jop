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

import com.jopdesign.jopui.helper.Color;

/**
 * Implements a label
 */
public class Label extends Component{

	/**
	 * Align the text inside the label to the top <p>
	 * Constant has the value 0
	 */
	public static final int TOP = 0;
	
	/**
	 * Align the text inside the label to the middle <p>
	 * Constant has the value 1
	 */
	public static final int MIDDLE = 1;
	
	/**
	 * Align the text inside the label to the bottom <p>
	 * Constant has the value 2
	 */
	public static final int BOTTOM = 2;
	
	/**
	 * Align the text inside the label to the left <p>
	 * Constant has the value 0
	 */
	public static final int LEFT = 0;
	
	/**
	 * Align the text inside the label to the center <p>
	 * Constant has the value 1
	 */
	public static final int CENTER = 1;
	
	/**
	 * Align the text inside the label to the right <p>
	 * Constant has the value 2
	 */
	public static final int RIGHT = 2;
	
	protected int valign;
	protected int halign;
	
	protected int leftMargin;
	protected int rightMargin;
	protected int topMargin;
	protected int bottomMargin;
	
	protected int colorBody;
	protected int colorText;
	protected int colorBorder;

	private String text;
	private Font font = null;
	
	/**
	 * Creates a new label with the given dimension and a text
	 * @param x x-coordinate of the upper left corner in pixel 
	 * @param y y-coordinate of the upper left corner in pixel
	 * @param width width of the label in pixel 
	 * @param height height of the label in pixel
	 * @param text the text to be drawn
	 */
	public Label(int x, int y, int width, int height, String text) {
		super.bounds = new Bounds(x, y, width, height);
		
		if(text == null)
			text = "";
		this.text = text;
		
		defaultValues();
	}
	
	/**
	 * Creates a new label with the given dimension and a text
	 * @param bounds the dimension of the label
	 * @param text the text to be drawn
	 */
	public Label(Bounds bounds, String text) {
		super.bounds = bounds;
		
		if(text == null)
			text = "";
		this.text = text;
		
		defaultValues();
	}
	
	/**
	 * Returns the number of visible characters
	 * @return number of characters
	 */
	public int getVisibleCharCount() {
		Graphics g = img.getGraphics();
		Font drawFont;
		if(font == null) 
			drawFont = g.getFont();
		else 
			drawFont = font;
		
		return bounds.getWidth()/drawFont.getWidth();
	}
	
	/**
	 * Returns the width of a character for the current font
	 * @return character width
	 */
	public int getCharWidth() {
		Graphics g = img.getGraphics();
		Font drawFont;
		if(font == null) 
			drawFont = g.getFont();
		else 
			drawFont = font;
		
		return drawFont.getWidth();
	}
	
	private void defaultValues() {
		colorBody  = Theme.colorLabelBackground;
		colorBorder = Theme.colorLabelBorder;
		colorText = Theme.colorLabelText;
		leftMargin = Theme.marginLabelLeft;
		rightMargin = Theme.marginLabelRight;
		topMargin = Theme.marginLabelTop;
		bottomMargin = Theme.marginLabelBottom;
		halign = Theme.halignLabel;
		valign = Theme.halignLabel;
	}
	
	protected Image create() {
		img = null;
		super.create();
		
		Graphics.disable(Graphics.COLOR_KEY);
		int width = bounds.getWidth();
		int height = bounds.getHeight();
		int x = 0;
		int y = 0;
		
		Graphics g = img.getGraphics();
		
		Font drawFont;
		Font saveFont = null;
		if(font == null) {
			drawFont = g.getFont();
		} else {
			drawFont = font;
			saveFont = g.getFont();
			g.setFont(font);
		}
		
		
		g.setColor(colorBody);
		g.fillRect(x,y,width,height);
		g.setColor(colorBorder);
		g.drawRect(x,y,width,height);
		g.setColor(colorText);
		
		int fontWidth = drawFont.getWidth();
		int maxChars = width / fontWidth;
		//int maxLines = bounds.getHeight() / drawFont.getHeight();
		
		int chars = text.length();
		
		if(chars > maxChars) {
			chars = maxChars;
		}
		
		switch(valign) {
			case TOP:
				y += topMargin;
				break;
			case BOTTOM:
				y += bounds.getHeight() - drawFont.getHeight() - bottomMargin;
				break;
			case MIDDLE:
			default:
				y += (bounds.getHeight() - drawFont.getHeight())/2;
		}
		 
		switch(halign) {
			case CENTER:
				x += (width - chars * fontWidth)/2;
				break;
			case RIGHT:
				x += width - chars * fontWidth - rightMargin;
				break;
			case LEFT:
				x += leftMargin;
			default:
		
		}
		
		g.drawSubstring(text, 0, chars, x, y);

		if(font != null) {
			g.setFont(saveFont);
		}
		Graphics.enable(Graphics.COLOR_KEY);
		
		return img;
	} 

	/**
	 * Returns the type of component <p>
	 * @see com.jopdesign.jopui.core.Component#getComponentType()
	 */
	public int getComponentType() {
		return LABEL;
	}
	
	/**
	 * Returns the vertical alignment of the text
	 * @return vertical alignment
	 */
	public int getValign() {
		return valign;
	}

	/**
	 * Set the vertical alignment of the text
	 * @param valign has to be TOP or MIDDLE or BOTTOM 
	 */
	public void setValign(int valign) {
		changed = true;
		this.valign = valign;
	}

	/**
	 * Returns the horizontal alignment of the text
	 * @return horizontal alignment
	 */
	public int getHalign() {
		return halign;
	}

	/**
	 * Set the horizontal alignment of the text
	 * @param halign has to be LEFT or CENTER or RIGHT
	 */
	public void setHalign(int halign) {
		changed = true;
		this.halign = halign;
	}

	/**
	 * Returns the left margin
	 * @return space between text and border in pixel 
	 */
	public int getLeftMargin() {
		return leftMargin;
	}

	/**
	 * Set the left margin
	 * @param leftMargin space between text and border in pixel
	 */
	public void setLeftMargin(int leftMargin) {
		changed = true;
		this.leftMargin = leftMargin;
	}

	/**
	 * Returns the right margin
	 * @return space between text and border in pixel
	 */
	public int getRightMargin() {
		return rightMargin;
	}

	/**
	 * Set the right margin
	 * @param rightMargin space between text and border in pixel
	 */
	public void setRightMargin(int rightMargin) {
		changed = true;
		this.rightMargin = rightMargin;
	}

	/**
	 * Returns the top margin
	 * @return space between text and border in pixel
	 */
	public int getTopMargin() {
		return topMargin;
	}

	/**
	 * Set the top margin
	 * @param topMargin space between text and border in pixel
	 */
	public void setTopMargin(int topMargin) {
		changed = true;
		this.topMargin = topMargin;
	}

	/**
	 * Returns the bottom margin 
	 * @return space between text and border in pixel
	 */
	public int getBottomMargin() {
		return bottomMargin;
	}

	/**
	 * Set the bottom margin
	 * @param bottomMargin space between text and border in pixel
	 */
	public void setBottomMargin(int bottomMargin) {
		changed = true;
		this.bottomMargin = bottomMargin;
	}

	/**
	 * Returns the color of the label
	 * @return color of the label
	 */
	public int getColorBody() {
		return colorBody;
	}

	/**
	 * Set the color of the label
	 * @param c color of the label
	 */
	public void setColorBody(Color c) {
		changed = true;
		this.colorBody = c.getColor();
	}

	/**
	 * Returns the color of the text
	 * @return color of the text
	 */
	public int getColorText() {
		return colorText;
	}

	/**
	 * Set the color of the text
	 * @param c color of the text
	 */
	public void setColorText(Color c) {
		changed = true;
		this.colorText = c.getColor();
	}

	/**
	 * Returns the color of the border
	 * @return color of the border
	 */
	public int getColorBorder() {
		return colorBorder;
	}

	/**
	 * Set the color of the border
	 * @param c color of the border
	 */
	public void setColorBorder(Color c) {
		changed = true;
		this.colorBorder = c.getColor();
	}

	/**
	 * Returns the text of the label
	 * @return the stored string
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set the text of the label
	 * @param text text to be stored
	 */
	public void setText(String text) {
		changed = true;
		if(text == null)
			text = "";
		this.text = text;
	}

	/**
	 * Returns the used font
	 * @return used font
	 */
	public Font getFont() {
		return font;
	}
	
	/**
	 * Set the font to be used
	 * @param font font to be used
	 */
	public void setFont(Font font) {
		changed = true;
		this.font = font;
	}

}
