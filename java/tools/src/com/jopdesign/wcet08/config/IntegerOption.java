/**
 * 
 */
package com.jopdesign.wcet08.config;

public class IntegerOption extends Option<Long> {
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