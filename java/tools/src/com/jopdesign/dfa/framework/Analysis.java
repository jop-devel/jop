package com.jopdesign.dfa.framework;

import java.util.Map;

import org.apache.bcel.generic.InstructionHandle;

public interface Analysis<K, V> {

	public ContextMap<K, V>	bottom();
	public ContextMap<K, V>	initial(InstructionHandle stmt);
	
	public void				initialize(String sig, Context context);
	
	public ContextMap<K, V> transfer(InstructionHandle stmt,
									FlowEdge edge,
									ContextMap<K, V> input,
									Interpreter<K, V> interpreter,
									Map<InstructionHandle, ContextMap<K, V>> state);
	public boolean		 	compare(ContextMap<K, V> s1, ContextMap<K, V> s2);
	public ContextMap<K, V> join(ContextMap<K, V> s1, ContextMap<K, V> s2);
	
	public Map			 	getResult();
}
