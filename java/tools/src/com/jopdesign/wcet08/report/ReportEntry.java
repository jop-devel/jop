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
package com.jopdesign.wcet08.report;

import java.util.Vector;

/**
 * Hierarchical Report pages (HTML)
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ReportEntry {
	private String key = null;
	private String link = null;
	private Vector<ReportEntry> subreportEntries = new Vector<ReportEntry>();
	public String toString() {			
		return this.key + " #" +this.subreportEntries.size();
	}
	private ReportEntry parent = null;
	private int level = 0;

	public static ReportEntry rootReportEntry(String link) { return new ReportEntry("root",link); }
	public ReportEntry(String key, String link) { this.key = key; this.link = link; }

	public ReportEntry getOrCreateSubReportEntry(String key2) {
		ReportEntry p = getSubReportEntry(key2);
		if(p == null) {
			p = createSubReportEntry(key2,null);
		}
		return p;
	}
	public ReportEntry createSubReportEntry(String key, String slink) {
		ReportEntry p = new ReportEntry(key,slink);
		p.parent = this;
		p.level = p.parent.level + 1;
		subreportEntries.add(p);
		return p;
	}
	private ReportEntry getSubReportEntry(String searchKey) {
		for(ReportEntry p: subreportEntries) {
			if(p.key.equals(searchKey)) return p;
		}
		return null;
	}
	public boolean hasSubPage(String searchKey) {
		return(getSubReportEntry(searchKey) != null);
	}
	public void addPage(String key2, String link2) {
		String[] path = key2.split("/");
		String leaf = path[path.length - 1];
		ReportEntry p = this;
		for(int i = 0; i < path.length - 1; i++) {
			p = p.getOrCreateSubReportEntry(path[i]);
		}
		if(p.hasSubPage(leaf)) {
			Report.logger.error("Page "+key+" already exists. Won't overwrite.");
		} else {
			p.createSubReportEntry(leaf,link2);
		}			
	}
	public String getKey() {
		return key;
	}
	public String getLink() {
		return link;
	}
	public Vector<ReportEntry> getSubReportEntries() {
		return subreportEntries;
	}
}