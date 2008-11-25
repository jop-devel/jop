package com.jopdesign.wcet08.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.wcet08.Config;

public class ClassReport {
	public static class JavaSource {
		File sourceFile;
		private Map<Integer,Line> lines = new TreeMap<Integer,Line>();
		public static class Line {
			public Line(int ix, String l) {
				this.ix= ix; this.text = l;
			}
			private int ix;
			private String text;
			private Properties props = new Properties();
			public int getIx() {
				return ix;
			}
			public String getInfo() {
				String s;
				if(this.props.containsKey("cost")) {
					Object[] args = { getIx(), "["+this.props.get("cost").toString() +"]"}; 
					s = String.format("%-4d %-8s", args);
				} else {
					Object[] args= { getIx()  };
					s = String.format("%-13d",args);
				}
				return s.replaceAll("\\s","&nbsp;");
			}
			public String getText() {
				return text;
			} 			
			public String getCode() {
				// TODO: Use velocity escaper
				return text.replace("\t","        ").replaceAll("\\s","&nbsp;").
					replace("<", "&lt;");				
			}
			/* add markers etc. */
			public void addProperty(String prop, Object val) {
				this.props.put(prop,val);
			}
			public String getBgColor() {
				return props.containsKey("color") ? props.getProperty("color") : "white";
			}
			public Object getProperty(String prop) {
				return props.get(prop);
			}
		}
		public JavaSource(File sourcefile) {
			this.sourceFile = sourcefile;
		}
		public void addLine(int ix, String l) {
			this.lines.put(ix,new Line(ix,l));
			if(l.matches("[<>\\[\\]\\w\\s]*[<>\\[\\]\\w]+\\([\\[\\]A-Za-z0-9_<> ]+ [\\[\\]A-Za-z0-9_<> ]+[,\\)][^;]*") ||
			   l.matches("[<>\\[\\]\\w\\s]+\\(\\s*\\)[^;]*")) {
				this.addLineProperty(ix, "color", "lightblue");
			}
		}
		public File getSourceFile() {
			return this.sourceFile;
		}
		public Collection<Line> getLines() {
			return this.lines.values();
		}
		public static JavaSource readSource(File sourcefile) throws IOException {
			JavaSource js = new JavaSource(sourcefile);
			BufferedReader r = new BufferedReader(new FileReader(sourcefile));
			String l;int i=1;
			while((l = r.readLine()) != null) {
				js.addLine(i++,l);
			}
			return js;
		}
		public void addLineProperty(int sourceLine, String prop, Object val) {
			this.lines.get(sourceLine).addProperty(prop,val);
		}
		public Object getLineProperty(int sourceLine, String prop) {
			return this.lines.get(sourceLine).getProperty(prop);
		}
		
	}
	private ClassInfo ci;
	private JavaSource source;
	
	public ClassInfo getClassInfo() {
		return ci;
	}

	public JavaSource getSource() {
		return source;
	}

	public ClassReport(ClassInfo ci) {
		this.ci = ci;
		try {
			this.source = JavaSource.readSource(Config.instance().getSourceFile(ci));
		} catch (IOException e) {
			this.source = null;
		}
	}

	public void addLineProperty(int sourceLine, String prop, Object val) {
		if(this.source == null) return;
		this.source.addLineProperty(sourceLine, prop,val);		
	}
	public void addLinePropertyIfNull(int sourceLine, String prop, Object val) {
		if(this.source == null) return;
		if(this.source.getLineProperty(sourceLine, prop) != null) return;
		this.source.addLineProperty(sourceLine, prop,val);		
	}
	public Object getLineProperty(int sourceLine, String prop) {
		return this.source.getLineProperty(sourceLine,prop);
	}


}
