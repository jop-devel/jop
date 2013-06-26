package scjlibs;

import scjlibs.util.GenericPoolObject;
import scjlibs.util.PoolObject;
//import scjlibs.util.PoolObject;

public class GenericEntry extends  GenericPoolObject {

	private boolean isFree = true;
	private StringBuffer name;
	private PoolObject next;

	GenericEntry() {
		this("");
	}

	GenericEntry(String name) {
		this.name = new StringBuffer(name);
	}

	public String getName() {
		return name.toString();
	}

	public void setName(String name) {
		this.name.delete(0, name.length());
		this.name.append(name);
	}

	@Override
	public void finalize() {
		this.name.delete(0, name.length());
		this.isFree = true;
	}

	@Override
	public void initialize() {
		this.isFree = false;

	}

	@Override
	public boolean isFree() {
		return this.isFree;
	}
	
	@Override
	public PoolObject getNext(){
		return next;
	}

}
