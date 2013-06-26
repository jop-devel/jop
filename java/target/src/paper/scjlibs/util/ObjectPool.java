package scjlibs.util;

public class ObjectPool<E> {

	private static final int DEFAULT_CAPACITY = 16;
	
	protected final int MAX_OBJECTS;
	protected PoolObject[] objects;
	protected int usedObjects;

	PoolObjectFactory factory;

	public ObjectPool(PoolObjectFactory factory) {
		this(DEFAULT_CAPACITY, factory);
	}

	public ObjectPool(int size, PoolObjectFactory factory) {
		this.MAX_OBJECTS = size;
		this.factory = factory;
		objects = new PoolObject[MAX_OBJECTS];

		for (int i = 0; i < MAX_OBJECTS; i++) {
			objects[i] = factory.createObject();
		}

	}

	public int usedObjects(){
		return usedObjects;
	}
	
	public int maxObjects(){
		return MAX_OBJECTS;
	}
	
	public synchronized E getPoolObject() {

		PoolObject obj = null;

		if (usedObjects >= MAX_OBJECTS) {
			obj = null;
		}

		// TODO: Change to a red-black tree to reduce lookup delay
		for (int i = 0; i < MAX_OBJECTS; i++) {
			if (objects[i].isFree()) {
				objects[i].initialize();
				obj = objects[i];
				usedObjects++;
				break;
			}
		}

		return (E) obj;

	}

	public synchronized void releasePoolObject(PoolObject object) {

		object.finalize();
		usedObjects--;

	}

}
