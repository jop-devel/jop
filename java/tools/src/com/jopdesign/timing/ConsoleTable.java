/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)
  
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
package com.jopdesign.timing;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/** 
 *  Class for drawing tables on the console
 *  FIXME: If available, replace by a proper library implementation.
 */
public class ConsoleTable {
	
	/** DEMO App */
	public static void main(String argv[]) {
		System.out.println(demoTable().render());
	}
	/** DEMO Table */
	public static ConsoleTable demoTable() {
		ConsoleTable table = new ConsoleTable();
		table.addColumn("Problem",Alignment.ALIGN_LEFT)
			 .addColumn("Cost",Alignment.ALIGN_RIGHT)
			 .addColumn("Gain",Alignment.ALIGN_RIGHT);
		table.addRow().addCell("Foo")
			          .addCell(3)
					  .addCell(4);
		table.addRow().addCell("Undef")
		              .addCell("undefined",2,Alignment.ALIGN_LEFT);
		table.addRow().addCell("Waldemar Bankhofer")
		              .addCell(32324);
		table.addRow().addCell("This is a long text, width 'span 3'",3,Alignment.ALIGN_LEFT);
		table.addRow().addCell("Too many columns (4)",3,Alignment.ALIGN_CENTER)
        			  .addCell("I'm at col 4");
		table.addRow().addCell("Too many columns (5)",2,Alignment.ALIGN_CENTER)
		  			  .addCell("Spanning to col 5",3,Alignment.ALIGN_RIGHT);
		return table;
	}
	
	static final String lineSeparator = System.getProperty ( "line.separator" );
	public enum Alignment { ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT };

	/* cells */
	private class TableCell {
		Alignment align;
		Object data;
		int pos;
		int span;

		TableCell(Object data, int pos, int span, Alignment align) {
			this.data = data;
			this.pos = pos;
			this.span = span;
			this.align = align;
		}

		public StringBuffer getAlignedString(int cwidth) {
			StringBuffer s = new StringBuffer();
			s.append(' ');
			s.append(data.toString());
			s.append(' ');
			int pad = cwidth - s.length();
			int padLeft = 0, padRight = 0;
			switch(align) {
			case ALIGN_LEFT: padRight = cwidth - s.length();break;
			case ALIGN_RIGHT: padLeft = cwidth - s.length();break;
			case ALIGN_CENTER:
				padLeft = pad / 2;
				padRight = (pad+1) / 2;
				break;
			}
			for(int i = 0; i < padLeft; i++) s.insert(0,' ');
			for(int i = 0; i < padRight; i++) s.append(' ');
			return s;
		}
	}
	/** rows in the table, managing the corresponding cells */
	public class TableRow {
		private int nextCol = 0;
		private ArrayList<TableCell> entries = new ArrayList<TableCell>();
		public TableRow addCell(Object o) { return addCell(o,1, getColumn(nextCol).align); }
		public TableRow addCell(Object o, int colspan, Alignment align) {
			ensureColumn(nextCol + colspan - 1);
			entries.add(new TableCell(o, nextCol, colspan,align));
			nextCol += colspan;
			return this;
		}
	}
	/* columns (specification, header) */
	private class TableColumn { 
		String name; String label; Alignment align;
		TableColumn(String name,String label, Alignment align) { 
			this.name = name;
			this.label = label;
			this.align = align; 
		}
	}

	private List<TableColumn> columns;
	private List<TableRow> rows;
	private List<String> topLegend    = new ArrayList<String>(),
				    	 bottomLegend = new ArrayList<String>();
	private Vector<Integer> width;
	private int totalWidth;
	
	public ConsoleTable() {
		this.rows    = new ArrayList<TableRow>();
		this.columns = new ArrayList<TableColumn>();
	}
	
	public ConsoleTable addColumn(String name, Alignment align) {
		this.columns.add(new TableColumn(name,name,align));
		return this;
	}
	
	private TableColumn getColumn(int index) {
		ensureColumn(index);
		return columns.get(index);				
	}
	
	private void ensureColumn(int index) {
		while(columns.size() <= index) {
			addColumn("Column "+(columns.size()+1),Alignment.ALIGN_LEFT);
		}		
	}
	public TableRow addRow() {
		TableRow r = new TableRow();
		rows.add(r);
		return r;
	}
	
	public void addLegendTop(String str) {
		this.topLegend.add(str);
	}
	
	public void addLegendBottom(String str) {
		this.bottomLegend.add(str);
	}

	public String render() {
		StringBuffer sb = new StringBuffer();
		render(sb);
		return sb.toString();
	}
	
	public void render(StringBuffer sb) {
		calculateWidth();
		renderSep('=',sb);
		TableRow header = new TableRow();
		for(TableColumn col : columns) { header.addCell(col.label,1,Alignment.ALIGN_CENTER); }
		renderRow(header,sb);
		renderSep('-',sb);

		if(this.topLegend.size() > 0) {
			for(String legend : this.topLegend) {
				renderRow(new TableRow().addCell(legend,columns.size(),Alignment.ALIGN_LEFT),sb);
			}
			renderSep('-',sb);
		}
		
		for(TableRow row : rows) {
			renderRow(row,sb); 
		}
		
		if(this.bottomLegend.size() > 0) {
			renderSep('-',sb);
			for(String legend : this.bottomLegend) {
				renderRow(new TableRow().addCell(legend,columns.size(),Alignment.ALIGN_LEFT),sb);
			}
		}
		renderSep('=',sb);	
	}

	private void renderRow(TableRow r, StringBuffer sb) {
		int i = 0;
		sb.append("|");
		for(TableCell c : r.entries) {
			int cwidth = width.get(i++);
			for(int j = 0; j < c.span - 1; j++) { cwidth += width.get(i++) + 1; }
			sb.append(c.getAlignedString(cwidth));
			sb.append("|");
		}
		for(; i < columns.size(); i++) {
			renderChar(' ',width.get(i),sb);sb.append("|");
		}
		sb.append('\n');
	}
	
	private void renderSep(char sep, StringBuffer sb) {
		renderChar(sep,totalWidth,sb);
		sb.append('\n');
	}

	private static void renderChar(char s, int count, StringBuffer sb) {
		for(int i = 0 ; i < count; i++) sb.append(s);		
	}
	
	private void calculateWidth() {
		this.width = new Vector<Integer>();
		for(TableColumn col : columns) {
			width.add(col.label.length()+2);
		}
		for(TableRow row : rows) {
			int i = 0;
			for(TableCell cell : row.entries) {
				int minwidth = cell.data.toString().length() + 2;
				// heuristic
				for(int j = 1; j < cell.span; j++) {
					minwidth -= width.get(i+j) + 1;
				}
				width.set(i, Math.max(width.get(i),minwidth));
				i+=cell.span;
			}
		}
		this.totalWidth = 0;
		for(int w : width) { totalWidth += w; }
		totalWidth += width.size() + 1;
	}
	
	public static String getSepLine(char sep, int l) {
		StringBuffer sb = new StringBuffer();
		renderChar(sep, l, sb);
		return sb.toString();
	}
}
