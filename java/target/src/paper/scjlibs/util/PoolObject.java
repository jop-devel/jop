package scjlibs.util;

public interface PoolObject {
	
	public void initialize();
	
	public boolean isFree();
	
	public void finalize();
	
	public PoolObject getNext();
	
	public PoolObject setNext(PoolObject object);

}
