package com.jopdesign.dfa.framework;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContextMap<K, V> extends LinkedHashMap<K, V> {
	
	private static final long serialVersionUID = 1L;
	
	private Context context;
	
	public ContextMap(Context context, Map<K, V> map) {
		super(map);
		this.context = context;
	}

	public ContextMap(ContextMap<K, V> map) {
		super(map);
		this.context = map.context;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void add(Object elem) {
		put((K)elem, (V)elem);
	}
}
