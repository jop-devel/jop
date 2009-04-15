/**
 * 
 */
package com.jopdesign.wcet.uppaal.translator;
import com.jopdesign.wcet.uppaal.model.Location;

class SubAutomaton {
	private static final long serialVersionUID = 1L;
	private Location entry;
	private Location exit;
	public SubAutomaton(Location entry, Location exit) {
		this.entry = entry;
		this.exit = exit;
	}
	public Location getEntry() { return entry; }
	public Location getExit()  { return exit; }
	public static SubAutomaton singleton(Location exit) {
		return new SubAutomaton(exit,exit);
	}
	@Override
	public String toString() {
		return String.format("auto(%s,%s)",entry,exit);
	}
}