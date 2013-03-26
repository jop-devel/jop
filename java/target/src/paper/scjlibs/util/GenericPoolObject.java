package scjlibs.util;

/**
 * Generic object implementing the PoolObject interface.
 * 
 * @author jrri
 *
 */

public abstract class GenericPoolObject implements PoolObject{

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isFree() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PoolObject getNext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PoolObject setNext(PoolObject object) {
		// TODO Auto-generated method stub
		return null;
	}

}
