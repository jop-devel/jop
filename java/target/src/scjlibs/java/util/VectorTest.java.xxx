package scjlibs.safeutil;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class VectorTest {

	class MyPoolObject extends AbstractPoolObject {

		private boolean isFree = true;
		public int number = 0; 

		@Override
		public void initialize() {
			this.isFree = false;

		}

		@Override
		public boolean isFree() {
			return this.isFree;
		}

		@Override
		public void terminate() {
			this.isFree = true;
		}

	}

	class MyFactory implements PoolObjectFactory {
		
		private int count = 0;

		@Override
		public AbstractPoolObject createObject() {
			MyPoolObject temp = new MyPoolObject();
			temp.number = count;
			count++;
			return temp;
		}

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEquals() {
		fail("Not yet implemented");
	}

	@Test
	public void testHashCode() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	@Test
	public void testSize() {

		Vector<MyPoolObject> fixture = new Vector<MyPoolObject>();
		ObjectPool<MyPoolObject> MyPool = new ObjectPool<MyPoolObject>(
				new MyFactory());

		assertEquals(fixture.size(), 0);

		fixture.add(MyPool.getPoolObject());

		assertEquals(MyPool.usedObjects(), 1, MyPool.usedObjects());
		assertEquals(fixture.size(), 1);

	}

	@Test
	public void testIsEmp() {

		/**
		 * No element has been added to the Vector, hence it should be empty
		 */
		Vector<MyPoolObject> fixture = new Vector<MyPoolObject>();
		assertTrue(fixture.isEmpty());

	}

	@Test
	public void testContains() {
		fail("Not yet implemented");
	}

	@Test
	public void testToArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testToArrayTArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddE() {
		
		Vector<MyPoolObject> fixture = new Vector<MyPoolObject>();
		ObjectPool<MyPoolObject> MyPool = new ObjectPool<MyPoolObject>(
				new MyFactory());
		
		/*
		 * Pool created with default capacity 
		 */
		assertEquals(16, ObjectPool.DEFAULT_CAPACITY);
		assertEquals(0, MyPool.usedObjects());
		
		for(int i = 0; i< fixture.capacity(); i++){
			fixture.add(MyPool.getPoolObject());
			assertEquals(i+1, MyPool.usedObjects());
			assertEquals(i+1, fixture.size());
		}
		
		assertEquals(10, MyPool.usedObjects());
		assertEquals(10, fixture.size());
		
		
	}

	@Test
	public void testRemoveObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testClear() {
		fail("Not yet implemented");
	}

	@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	@Test
	public void testSet() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddIntE() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testIndexOfObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testLastIndexOfObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveRange() {
		fail("Not yet implemented");
	}

	@Test
	public void testVectorInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testVector() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopyInto() {
		fail("Not yet implemented");
	}

	@Test
	public void testCapacity() {

		Vector<MyPoolObject> fixture = new Vector<MyPoolObject>();

		/**
		 * Vector created with default capacity
		 */
		assertEquals(fixture.capacity(), Vector.DEFAULT_CAPACITY);

	}

	@Test
	public void testElements() {
		fail("Not yet implemented");
	}

	@Test
	public void testIndexOfObjectInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testLastIndexOfObjectInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testElementAt() {
		fail("Not yet implemented");
	}

	@Test
	public void testFirstElement() {
		fail("Not yet implemented");
	}

	@Test
	public void testLastElement() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetElementAt() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveElementAt() {

		Vector<MyPoolObject> fixture = new Vector<MyPoolObject>();
		ObjectPool<MyPoolObject> objectPool = new ObjectPool<MyPoolObject>(
				new MyFactory());
		
		MyPoolObject[] objectArray = new MyPoolObject[10];
		for(int i = 0; i < objectArray.length; i++){
			objectArray[i] = objectPool.getPoolObject();
		}
		
		for(int i = 0; i< fixture.capacity(); i++){
			fixture.add(objectArray[i]);
		}
		
		assertEquals(10, objectPool.usedObjects());
		assertEquals(10, fixture.size());
		
		/**
		 * Deletes the component at the specified index. Each component in this
		 * vector with an index greater or equal to the specified {@code index}
		 * is shifted downward to have an index one smaller than the value it
		 * had previously. The size of this vector is decreased by {@code 1} and
		 * the removed element is returned to its object pool.
		 */
		fixture.removeElementAt(5);
		assertEquals(4,fixture.elementAt(4).number);
		assertEquals(6,fixture.elementAt(5).number);
		assertEquals(9, fixture.size());
		assertEquals(9, objectPool.usedObjects);

		fixture.removeElementAt(5);
		assertEquals(4,fixture.elementAt(4).number);
		assertEquals(7,fixture.elementAt(5).number);
		assertEquals(8, fixture.size());
		assertEquals(8, objectPool.usedObjects);
		
		/**
		 * The index must be a value greater than or equal to {@code 0} and less
		 * than the current size of the vector.
		 */
		int size = fixture.size();
		try {
			fixture.removeElementAt(size+1);
		} catch (ArrayIndexOutOfBoundsException e) {
			assertNotNull("ArrayIndexOutOfBoundsException expected", e);
		}
		
		try {
			fixture.removeElementAt(-1);
		} catch (ArrayIndexOutOfBoundsException e) {
			assertNotNull("ArrayIndexOutOfBoundsException expected", e);
		}
		

	}

	@Test
	public void testInsertElementAt() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddElement() {

		fail("Not yet implemented");
	}

	@Test
	public void testRemoveElement() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveAllElements() {
		fail("Not yet implemented");
	}

}
