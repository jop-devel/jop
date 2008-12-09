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

package com.jopdesign.wcet08;

import java.util.Arrays;

/**
 * Typed options for improved command line interface
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 * @param <T> java type of the option
 */
public abstract class Option<T> {
	
	/*
	 * Standard Option Implementations
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	public static class StringOption extends Option<String> {
		public StringOption(String key, String descr, boolean optional) {
			super(key, String.class, descr, optional);
		}
		public StringOption(String key, String descr, String def) {
			super(key,descr,def);
		}
		@Override
		public void checkFormat(String s) { return; }
		public String parse(String s) { return s; }
	}
	public static class BooleanOption extends Option<Boolean> {
		public BooleanOption(String key, String descr) {
			super(key, Boolean.class, descr, false);
		}
		public BooleanOption(String key, String descr, boolean def) {
			super(key,descr,def);
		}
		@Override
		public void checkFormat(String s) throws IllegalArgumentException {
			parse(s);			
		}
		public Boolean parse(String s) throws IllegalArgumentException {
			String sl = s.toLowerCase();
			if(sl.equals("true") || sl.equals("yes") || sl.equals("y")) return Boolean.TRUE;
			else if (sl.equals("false") || sl.equals("no") || sl.equals("n")) return Boolean.FALSE;
			else throw new IllegalArgumentException("Cannot parse boolean: "+sl);
		}		
	}
	public static class IntegerOption extends Option<Long> {
		public IntegerOption(String key, String descr, boolean optional) {
			super(key, Long.class, descr, optional);
		}
		public IntegerOption(String key, String descr, long i) {
			super(key, descr, new Long(i));
		}
		@Override
		public void checkFormat(String s) throws NumberFormatException {
			parse(s);
		}
		@Override
		public Long parse(String s) throws IllegalArgumentException {
			return Long.parseLong(s);
		}
	}
	public static class EnumOption<T extends Enum<T>> extends Option<T> {
		public EnumOption(String key, String descr, T def) {
			super(key,descr + " " + enumDescr(def), def);
		}
		private static<U extends Enum<U>> String enumDescr(U v) {
			return Arrays.toString(v.getClass().getEnumConstants());
		}
		private static String enumDescr(Class<?> v) {
			return Arrays.toString(v.getEnumConstants());
		}
		@Override
		public void checkFormat(String s) throws IllegalArgumentException {
			parse(s);
		}
		public T parse(String s) throws IllegalArgumentException {
			try {
				return (T)Enum.valueOf(this.valClass,s);
			} catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("'"+s+"' failed to parse: not one of "+enumDescr(this.valClass));
			}
		}		
	}
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