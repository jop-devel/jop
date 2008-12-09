package com.jopdesign.wcet08.uppaal.translator;

import java.util.Vector;

import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.model.NTASystem;

public class FIFOCacheBuilder extends CacheSimBuilder {
	private int numBlocks;
	public FIFOCacheBuilder() {
		this.numBlocks = Config.instance().getOption(UppAalConfig.UPPAAL_CACHE_BLOCKS).intValue();
		// logger.info("FIFO cache simulation with "+numBlocks+ " blocks assuming empty cache");
	}
	@Override
	public void appendDeclarations(NTASystem system,String NUM_METHODS) {
		appendDeclsN(system,NUM_METHODS);
	}
	public void appendDeclsN(NTASystem system,String NUM_METHODS) {
		super.appendDeclarations(system,NUM_METHODS);
		system.appendDeclaration(String.format("int[0,%s] cache[%d] = %s;",
				NUM_METHODS,this.numBlocks,initCache(NUM_METHODS)));
		system.appendDeclaration(String.format("bool lastHit;"));
		system.appendDeclaration(
				"void access_cache(int mid) {\n"+
				"  int i = 0;\n"+
				"  lastHit = false;\n"+
				"  for(i = 0; i < "+numBlocks+"; i++) {\n"+
				"      if(cache[i] == mid) {\n"+
				"        lastHit = true;\n"+
				"        return;\n"+
				"      }\n"+
				"  }\n"+
				"  for(i = "+(numBlocks-1)+"; i > 0; i--) {\n"+
				"     cache[i]=cache[i-1];\n"+
				"  }\n"+
				"  cache[0] = mid;\n"+
				"}\n");
	}
	private StringBuilder initCache(String NUM_METHODS) {
		Vector<Object> cacheElems = new Vector<Object>();
		cacheElems.add(0);
		for(int i = 1; i < numBlocks; i++) cacheElems.add(NUM_METHODS);
		return CacheSimBuilder.constArray(cacheElems);
	}
}
