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
package com.jopdesign.wcet.report;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Hierarchical Report pages (HTML). <br/>
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ReportEntry {
	private String key = null;
	private String link = null;
	private ReportEntry parent = null;
	private int level = 0;	

	private HashMap<String,ReportEntry> entrySet = new HashMap<String,ReportEntry>();
	private LinkedList<ReportEntry> subreportEntries = new LinkedList<ReportEntry>();

	public static ReportEntry rootReportEntry(String link) { 
		return new ReportEntry("root",link); 
	}
	public ReportEntry(String name, String link) { 
		this.key = name;
		this.link = link; 
	}
	/**
	 * Retrieve the subentry with the given name, or create it if not present.
	 * @param key
	 * @return the subentry
	 */
	public ReportEntry getOrCreate(String key) {
		ReportEntry p = getSubReportEntry(key);
		if(p == null) {
			p = add(key,null,false);
		}
		return p;
	}
	/**
	 * Retrieve the subentry with the given name, or create it at the end if not present.
	 * @param key
	 * @return the subentry
	 */
	public ReportEntry getOrCreateStart(String key) {
		ReportEntry p = getSubReportEntry(key);
		if(p == null) {
			p = add(key,null,true);
		}
		return p;
	}
	/**
	 * Create a subreport entry at the beginning of the subreport list. 
	 * Note that no entry with the same name must be present.
	 * @param name the name of the entry
	 * @param slink the link to that entry
	 * @return
	 */
	public ReportEntry addStart(String name, String slink) {
		return add(name,slink,true);
	}
	/**
	 * Create a subreport entry. Note that no entry with the same name must be present.
	 * @param name the name of the entry
	 * @param slink the link to that entry
	 * @return
	 */
	public ReportEntry add(String name, String slink) {
		return add(name,slink,false);
	}
	private ReportEntry add(String name, String slink, boolean atStart) {
		if(this.entrySet.containsKey(name)) {
			throw new AssertionError("createSubReport: There is already a subreport named "+
									 name);
		}
		ReportEntry p = new ReportEntry(name,slink);
		p.parent = this;
		p.level = p.parent.level + 1;
		this.entrySet.put(name,p);
		if(atStart) subreportEntries.addFirst(p);
		else subreportEntries.addLast(p);
		return p;
	}
	private ReportEntry getSubReportEntry(String searchKey) {
		return entrySet.get(searchKey);
	}
	public void addPageStart(String name, String link) {
		addPage(name,link,true);
	}
	public void addPage(String name, String link) {
		addPage(name,link,false);
	}
	private void addPage(String name, String link, boolean atStart) {
		String[] path = name.split("/");
		String leaf = path[path.length - 1];
		ReportEntry p = this;
		for(int i = 0; i < path.length - 1; i++) {
			p = p.getOrCreate(path[i]);
		}
		if(p.hasSubPage(leaf)) {
			p = p.getOrCreate(leaf);
			if(p.getLink() == null) {
				p.link = link;
			} else {
				// Report.logger.error("Page "+leaf+" already linked. Won't overwrite.");				
			}
		} else {
			p.add(leaf,link,atStart);
		}			
	}
	private boolean hasSubPage(String leaf) {
		return this.entrySet.containsKey(leaf);
	}

	/* Getters */	
	public String getKey() {
		return key;
	}
	public String getName() {
		return key;
	}
	public String getLink() {
		return link;
	}
	public List<ReportEntry> getSubReportEntries() {
		return subreportEntries;
	}
	@Override
	public String toString() {			
		return this.key + " #" +this.subreportEntries.size();
	}
}