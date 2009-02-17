/**
 * 
 */
package com.jopdesign.wcet08.config;

public class StringOption extends Option<String> {
	public StringOption(String key, String descr, boolean optional) {
		super(key, String.class, descr, optional);
	}
	public StringOption(String key, String descr, String def) {
		super(key,descr,def);
	}
	@Override
	public void checkFormat(String s) { return; }
	public String parse(String s) { return s.trim(); }
	public StringOption mandatory() {
		return new StringOption(key,descr,false);
	}
}