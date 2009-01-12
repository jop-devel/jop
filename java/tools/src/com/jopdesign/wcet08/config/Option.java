/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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

package com.jopdesign.wcet08.config;


/**
 * Typed options for improved command line interface
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 * @param <T> java type of the option
 */
public abstract class Option<T> {
	
	protected String key;
    protected String descr;
    protected boolean optional;	
    protected Class<T> valClass;
    protected T defaultValue = null;	

    public T getDefaultValue() {
		return defaultValue;
	}
	public String getDescr() {
		return descr;
	}
	public String getKey() {
		return key;
	}
	public boolean isOptional() {
		return optional;
	}
	protected Option(String key, Class<T> optClass, String descr, boolean optional) {
		this.key=key;this.valClass=optClass;this.descr=descr;this.optional=optional;
	}
	@SuppressWarnings("unchecked")
	public Option(String key, String descr, T defaultVal) {
		this(key,(Class<T>) defaultVal.getClass(),descr,true);
		this.defaultValue = defaultVal;
	}
	
	public abstract void checkFormat(String s) throws IllegalArgumentException;
	public abstract T parse(String s) throws IllegalArgumentException;
	
	public String toString() {
		return toString(0);
	}
	public String toString(int ladjust) {
		StringBuffer s = new StringBuffer(key);
		for(int i = s.length(); i <= ladjust; i++) {
			s.append(' ');
		}
		s.append(" ... ");
		s.append(descrString());
		return s.toString();
	}
	public String descrString() {
		StringBuffer s = new StringBuffer(this.descr);
		s.append(" ");
		if(defaultValue != null) {
			s.append("[default: "+ defaultValue +"]");
		} else {
			s.append(this.optional ? "[optional]" : "[mandatory]");
		}
		return s.toString();
	}
}