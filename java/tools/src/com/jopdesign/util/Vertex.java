package com.jopdesign.util;

import java.util.HashSet;

public class Vertex {
	HashSet succ, pred;
	
	Object userData;
	public Vertex(Object data) {
		userData = data;
		succ = new HashSet();
		pred = new HashSet();
	}
	
	public HashSet getSucc() {
		return succ;
	}
	
	public String toString() {
		
		return userData.toString();
	}
	
	public String toDotString() {
		return toString();
	}
}
