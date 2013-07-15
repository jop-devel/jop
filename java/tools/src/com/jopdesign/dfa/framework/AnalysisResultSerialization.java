package com.jopdesign.dfa.framework;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.CallString.CallStringSerialization;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.type.MemberID;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


/** Helper class to dump analysis results.
 * <p>Result Map: MethodInfo -> Instruction Offset -> Callstring -> R</p>
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 * @param <R> Type of the result (indexed by instruction handle and callstring)
 * @param <S> Type of the serialized result
 */
public class AnalysisResultSerialization<R> {

	/** Interface for custom result printers (e.g., for loop bounds) */
	public interface ResultFormatter<T> {
		public String format(String method, CallStringSerialization callString, Integer position,
				T value);
	}

	/** Interface for custom result type conversion(e.g., for FlowEdge) */
	public interface Serializer<T,ST> {
		public ST serializedRepresentation(T obj);
		public T fromSerializedRepresentation(ST obj, AppInfo appInfo)
			   throws ClassNotFoundException, MethodNotFoundException, IOException;
	}
		
	private Map<String, Map<CallStringSerialization, Map<Integer, R>>> serializedResults;

	public AnalysisResultSerialization() {
		this.serializedResults = new LinkedHashMap<String, Map<CallStringSerialization,Map<Integer,R>>>();
	}

	private AnalysisResultSerialization(Map<String, Map<CallStringSerialization, Map<Integer, R>>> serialized) {
		this.serializedResults = serialized;
	}

	public static<T,R> AnalysisResultSerialization<R> fromContextMapResult(
			Map<InstructionHandle, ContextMap<CallString, T>> result) {

		return fromContextMapResult(result, null);
	}
	
	/** 
	 * <p>Result Map: MethodInfo -> Instruction Offset -> Callstring -> R</p>
	 * <p>TODO: More efficient representations are possible</p>
	 * @param result the result of the DFA analysis
	 * @param serializer converter for the result domain (if not serializable), or null
	 *                   if the results of type T should be serialized directly
	 */
	public static<T,R> AnalysisResultSerialization<R> fromContextMapResult(
			Map<InstructionHandle, ContextMap<CallString, T>> result,
			Serializer<T, R> serializer) {
		
		AnalysisResultSerialization<R> analysisResult = new AnalysisResultSerialization<R>();
		/* sort instruction handle by: method, offset */
		for (InstructionHandle instr : result.keySet()) {
			ContextMap<CallString, T> r = result.get(instr);
			Context c = r.getContext();
                    InstructionList il = c.getMethodInfo().getCode().getInstructionList(true, false);
			for (CallString cs : r.keySet()) {
				Integer position = instr.getPosition();

                            // skip stuff that is not used anymore
                            if (position < 0) continue;
                            if (il.findHandle(position) != instr) continue;

				if(serializer != null) {
					T rValue = r.get(cs);
					R sValue = serializer.serializedRepresentation(rValue);
					analysisResult.addResult(c.getMethodInfo(), position, cs, sValue);
				} else {
					analysisResult.addResult(c.getMethodInfo(), position, cs, (R) r.get(cs));			
				}
			}
		}
		return analysisResult;
	}
	
	public<T> Map<InstructionHandle, ContextMap<CallString, T>> 
	    toContextMapResult(AppInfo appInfo, Serializer<T,R> serializer)
	    throws MethodNotFoundException, IOException, ClassNotFoundException {

		/* `context' is a really bad hack in the DFA. In the deserialization,
		 * we guarantee that context.getMethodInfo() and context.callstring are correct,
		 * but the rest of context is undefined.
		 */
		Context currentContext;
		Map<InstructionHandle, ContextMap<CallString, T>> resultMap =
			new LinkedHashMap<InstructionHandle, ContextMap<CallString,T>>();
		for(Entry<String, Map<CallStringSerialization, Map<Integer, R>>> miEntry : serializedResults.entrySet()) {
			
			MethodInfo mi = appInfo.getMethodInfo(MemberID.parse(miEntry.getKey()));
			
			for(Entry<CallStringSerialization, Map<Integer, R>> csEntry : miEntry.getValue().entrySet()) {
				
				CallString cs = csEntry.getKey().getCallString(appInfo);				
				currentContext = new Context();
				currentContext.setMethodInfo(mi);
				currentContext.callString = cs;
				
				for(Entry<Integer, R> posEntry : csEntry.getValue().entrySet()) {
					
					int pos = posEntry.getKey();
					R value = posEntry.getValue();
					InstructionHandle instr = mi.getCode().getInstructionList(false,false).findHandle(pos);
					
					ContextMap<CallString, T> ctxMap = resultMap.get(instr);	
					if(ctxMap == null) {
						ctxMap = new ContextMap<CallString, T>(currentContext, new LinkedHashMap<CallString, T>());
						resultMap.put(instr, ctxMap);
					}
					
					if(serializer == null) {
						ctxMap.put(cs, (T) value);
					} else {
						T origValue = serializer.fromSerializedRepresentation(value, appInfo);
						ctxMap.put(cs, origValue);
					}
				}
			}
		}
		return resultMap;
	}

	public void addResult(MethodInfo method,
			              Integer pos,
			              CallString cs,
			              R result) {
		Map<CallStringSerialization, Map<Integer, R>> csMap =
			getOrCreateMapEntry (serializedResults, method.getFQMethodName(), LinkedHashMap.class);
		Map<Integer, R> posMap =
			getOrCreateMapEntry(csMap, new CallStringSerialization(cs), TreeMap.class);
		if(posMap.containsKey(pos)) {
			throw new AssertionError("Duplicate Key in DFA result set");
		}
		posMap.put(pos, result);
	}

	/* Type hackery: did not manage to implement this in a fully checked way */
	@SuppressWarnings("unchecked")
	private static <K,V>
	V getOrCreateMapEntry(Map<K,V> map, K key, Class instantiable) {
		V r = map.get(key);	
		if(r == null) {
			try {
				r = (V) instantiable.newInstance();
				map.put(key, r);
			} catch (ClassCastException e) {
				throw new RuntimeException("AnalysisResultSerialization: Internal Error",e);				
			} catch (Exception e) {
				throw new RuntimeException("AnalysisResultSerialization: Internal Error",e);
			} 
		}
		return r;
	}
	
	/** Dump the results in human readable into a string */
	public String dump() { 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		dump(ps);
		return baos.toString();
	}

	/** Dump the results in human readable form to the given stream */
	public void dump(PrintStream os) { 
		
		dump(os,null);
	}

	/** Dump the results in human readable form to the given stream */
	public void dump(PrintStream os,ResultFormatter<R> formatter) {

		for(Entry<String, Map<CallStringSerialization, Map<Integer, R>>> miEntry : serializedResults.entrySet()) {
			os.println(miEntry.getKey());
			for(Entry<CallStringSerialization, Map<Integer, R>> csEntry : miEntry.getValue().entrySet()) {
				os.println("  "+csEntry.getKey().toString());
				for(Entry<Integer, R> posEntry : csEntry.getValue().entrySet()) {
					String rStr;
					if(formatter != null) {
						rStr = formatter.format(miEntry.getKey(), csEntry.getKey(),
								                posEntry.getKey(), posEntry.getValue());
					} else {
						rStr = ""+posEntry.getValue();
					}
					os.println(String.format("  %-6d: %s",posEntry.getKey(), rStr));						
				}
			}
		}
	}
	
	public void serialize(File f) throws IOException {
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		serialize(oos);
		oos.close();
	}

	public void serialize(ObjectOutputStream oos) throws IOException {
		
		oos.writeObject(this.serializedResults);		
	}
	

	public static<R> AnalysisResultSerialization<R>
    fromSerialization(File cacheFile)
    throws IOException, ClassNotFoundException, MethodNotFoundException {
			
		FileInputStream fis = new FileInputStream(cacheFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		AnalysisResultSerialization<R> result = fromSerialization(ois);
		ois.close();
		fis.close();
		return result;
	} 

	@SuppressWarnings("unchecked")
	public static<R> AnalysisResultSerialization<R>
	    fromSerialization(ObjectInputStream ois)
	    throws IOException, ClassNotFoundException, MethodNotFoundException {
		

		Map<String, Map<CallStringSerialization, Map<Integer, R>>> serializedResults =
			(Map<String, Map<CallStringSerialization, Map<Integer, R>>>) ois.readObject();
		return new AnalysisResultSerialization<R>(serializedResults);
	}

	public static<R, T> Map<InstructionHandle, ContextMap<CallString, T>>
			deserializeContextMap(AppInfo appInfo, ObjectInputStream ois,
					    Serializer<T, R> serializer)
			throws IOException, ClassNotFoundException, MethodNotFoundException {

		AnalysisResultSerialization<R> r = fromSerialization(ois);
		return r.toContextMapResult(appInfo, serializer);
	}

}
