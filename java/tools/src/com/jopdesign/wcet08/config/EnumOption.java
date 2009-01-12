/**
 * 
 */
package com.jopdesign.wcet08.config;

import java.util.Arrays;

public class EnumOption<T extends Enum<T>> extends Option<T> {
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