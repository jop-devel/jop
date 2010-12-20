/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet.uppaal.model;

import java.awt.Color;
import java.awt.Point;
import java.util.Formatter;

import org.w3c.dom.Element;

public class VisualAttribute {
	private Point coords = null;
	private Color color = null;
	public VisualAttribute(int x, int y) { 
		coords = new Point(x,y);
	}
	public VisualAttribute(int x, int y, Color c) { 
		this(x,y); 
		this.color = c; 
	}
	public VisualAttribute(Color c) {
		this.color = c;
	}
	public VisualAttribute() {
	}		
	public boolean hasCoords() {
		return this.coords != null;
	}
	public boolean hasColor() {
		return this.color != null;
	}
	public Color getColor() {
		return color;
	}
	public int getX() {
		return coords.x;
	}
	public int getY() {
		return coords.y;
	}
	public void setXmlAttributes(Element elem) {
		if(hasCoords()) {
			elem.setAttribute("x", ""+coords.x);
			elem.setAttribute("y", ""+coords.y);
		}
		if(hasColor()) {
			elem.setAttribute("color", formatColor(color));
		}		
	}
	public void setColor(Color c) {
		this.color = c;
	}
	public void setPos(int x, int y) {
		coords = new Point(x,y);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder("VisAttr [");
		if(this.hasCoords()) s.append(coords.toString());
		if(this.hasCoords() && hasColor()) s.append("; ");
		if(this.hasColor()) s.append(this.color.toString());
		s.append("]");
		return s.toString();
	}
	
	public static String formatColor(Color c) {
		return new Formatter().
			format("#%02x%02x%02x",c.getRed(),c.getGreen(),c.getBlue()).
			toString();		
	}
	public Point getCoords() {
		return this.coords;
	}
}
