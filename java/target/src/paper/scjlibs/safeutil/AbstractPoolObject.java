package scjlibs.safeutil;

/**
 * Generic object implementing the PoolObject interface.
 * 
 * @author jrri
 *
 */

public abstract class AbstractPoolObject implements PoolObject{
	
	ObjectPool<?> objectPool = null;
	
	public void setPool(ObjectPool<?> objectPool){
		this.objectPool = objectPool;
	}
	
	public ObjectPool<?> getPool(){
		return objectPool;
	}


}
