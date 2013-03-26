package scjlibs.util;


public @interface MemSafe {
	
	public MemoryRisk[] risk(); 

}

enum MemoryRisk {
	NONE,
	LAZY,
	OBJ_REF_TO_NULL,
	UNREFERENCED_OBJ,
	TEMP_OBJECTS,
	MIXED_CONTEXT,
	RESIZE,
	EXCEPTION
}
