package com.jopdesign.sys;

public class Config {

	/**
	 * Use either scoped memories or a GC.
	 * Combining scopes and the GC needs some extra work.
	 */
	final static boolean USE_SCOPES = false;
	final static boolean USE_SCOPECHECKS = false;
	final static boolean ADD_REF_INFO = false;
	/** Set to false for the WCET analysis, true for measurement */
	public final static boolean MEASURE = true;


}
