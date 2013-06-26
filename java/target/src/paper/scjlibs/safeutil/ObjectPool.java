package scjlibs.safeutil;

public class ObjectPool<E> {

	/**
	 * Default capacity when the pool is constructed without indicating an
	 * explicit size.
	 */
	protected static final int DEFAULT_CAPACITY = 16;

	/**
	 * Maximum number of elements in the pool.
	 */
	protected final int MAX_OBJECTS;
	/**
	 * Array containing the pool elements.
	 */
	protected AbstractPoolObject[] objects;
	/**
	 * Number of elements used in the pool.
	 */
	protected int usedObjects;

	PoolObjectFactory factory;

	/**
	 * Construct a pool to hold objects of type {@code E}. With this
	 * constructor, the default capacity of 16 is used. Details on how the pool
	 * elements are created is left to the particular implementation of the
	 * factory interface passed as argument.
	 * 
	 * @param factory
	 *            a PoolObjectFactory object that has the details on how pool
	 *            elements are created
	 */
	public ObjectPool(PoolObjectFactory factory) {
		this(DEFAULT_CAPACITY, factory);
	}

	/**
	 * Construct a pool to hold objects of type {@code E}. With this
	 * constructor, the default capacity is the value of the {@code size}
	 * argument. Details on how the pool elements are created is left to the
	 * particular implementation of the factory interface passed as argument.
	 * 
	 * @param size
	 *            number of elements in the pool
	 * @param factory
	 *            a PoolObjectFactory object that has the details on how pool
	 *            elements are created
	 */
	public ObjectPool(int size, PoolObjectFactory factory) {
		this.MAX_OBJECTS = size;
		this.factory = factory;
		objects = new AbstractPoolObject[MAX_OBJECTS];

		for (int i = 0; i < MAX_OBJECTS; i++) {
			objects[i] = factory.createObject();
			objects[i].objectPool = this;
		}

	}

	/**
	 * Return the number of used objects
	 * 
	 * @return the number of used objects
	 */
	public int usedObjects() {
		return usedObjects;
	}

	/**
	 * Return the maximum number of objects in the pool
	 * 
	 * @return the maximum number of objects in the pool
	 */
	public int maxObjects() {
		return MAX_OBJECTS;
	}

	/**
	 * Return a reference to a free object in the pool.
	 * 
	 * @return a reference to a free object in the pool.
	 */
	public synchronized E getPoolObject() {

		PoolObject obj = null;

		if (usedObjects >= MAX_OBJECTS) {
			obj = null;
		}

		// TODO: Change to a different structure to reduce lookup delay
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

	/**
	 * Return to the pool the object specified in the argument.
	 * 
	 * @param object
	 *            The object to be returned into the pool.
	 */
	public synchronized void releasePoolObject(PoolObject object) {

		object.terminate();
		usedObjects--;

	}

}
