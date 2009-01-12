/**
 * 
 */
package com.jopdesign.wcet08.config;

public class BooleanOption extends Option<Boolean> {
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