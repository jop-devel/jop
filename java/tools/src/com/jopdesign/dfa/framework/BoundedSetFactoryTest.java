/**
 * 
 */
package com.jopdesign.dfa.framework;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;

/**
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class BoundedSetFactoryTest {
	public int[] TEST_BOUNDS = {1,2,3,4,5,6};
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.jopdesign.dfa.framework.BoundedSetFactory#empty()}.
	 */
	@Test
	public void testEmpty() {
		for(int i : TEST_BOUNDS) {
			BoundedSet<String> bs = new BoundedSetFactory<String>(i).empty();
			assert(bs.getSize() == 0);
			assert(bs.getSet().size() == 0);
		}
	}
	/**
	 * Test method for {@link com.jopdesign.dfa.framework.BoundedSetFactory#singleton(java.lang.Object)}.
	 */
	@Test
	public void testSingleton() {
		for(int i : TEST_BOUNDS) {
			BoundedSet<String> bs = new BoundedSetFactory<String>(i).singleton("foo");
			assert(bs.getSize() == 1);
			assert(bs.getSet().size() == 1);
		}
	}

	@Test
	public void testUnsaturatedAdd() {
		for(int i : TEST_BOUNDS) {
			BoundedSetFactory<String> bsf = new BoundedSetFactory<String>(i);
			BoundedSet<String> bs = boundedSet(bsf,0,i-1);
			String t = getElement(i-1);
			bs.add(t);
			assert(bs.isSaturated() == false);
			assert(bs.getSize() == i);
			assert(! bsf.top().equals(bs));						
		}		
	}


	@Test
	public void testUnsaturatedAddAll() {
		for(int i : TEST_BOUNDS) {
			BoundedSetFactory<String> bsf = new BoundedSetFactory<String>(i);
			BoundedSet<String> bs1 = boundedSet(bsf,0,i/2+1);
			BoundedSet<String> bs2 = boundedSet(bsf,i/2-1,i-1);
			bs1.addAll(bs2);
			assert(bs1.isSaturated() == false);
			assert(bs1.getSize() == i);
			assert(! bsf.top().equals(bs1));						
		}		
	}

	@Test
	public void testSaturatedAdd() {
		for(int i : TEST_BOUNDS) {
			BoundedSetFactory<String> bsf = new BoundedSetFactory<String>(i);
			BoundedSet<String> bs = boundedSet(bsf,0,i-1);
			String t = getElement(i);
			bs.add(t);
			assert(bs.isSaturated() == true);
			assert(bs.getSize() == i+1);
			assert(bsf.top().equals(bs));
			BoundedSet<String> top = bsf.top();
			String t2 = getElement(i+1);
			top.add(t2);			
			assert(top.equals(bs));
			assert(bs.equals(top));
		}		
	}

	@Test
	public void testSaturatedAddAll() {
		for(int i : TEST_BOUNDS) {
			BoundedSetFactory<String> bsf = new BoundedSetFactory<String>(i);
			BoundedSet<String> bs1 = boundedSet(bsf,0,i/2);
			BoundedSet<String> bs2 = boundedSet(bsf,i/2,i);
			BoundedSet<String> top = bsf.top();
			bs1.addAll(bs2);
			top.addAll(bs1);
			assert(bs1.isSaturated() == true);
			assert(bs1.getSize() == i+1);
			assert(top.equals(bs1));
			assert(bs1.equals(top));
		}		
	}
	
	@Test
	public void testJoin() {
		for(int i : TEST_BOUNDS) {
			BoundedSetFactory<String> bsf = new BoundedSetFactory<String>(i);
			BoundedSet<String> bs1 = boundedSet(bsf,0,i/2);
			BoundedSet<String> bs2 = boundedSet(bsf,i/2,i-1);			
			BoundedSet<String> bs3 = bs1.join(bs2);
			bs2.addAll(bs1);			
			assert(bs3.equals(bs2));
			assert(bs3.hashCode() == bs2.hashCode());
			assert(bs3 != bs2);
			bs2.add(getElement(i));
			bs3.add(getElement(i));
			assert(bs3.equals(bs2));
			assert(bs3.hashCode() == bs2.hashCode());
			assert(bs3 != bs2);			
		}
	}

	@Test
	public void testSubset() {
		for(int i : TEST_BOUNDS) {
			BoundedSetFactory<String> bsf = new BoundedSetFactory<String>(i);
			BoundedSet<String> bs1 = boundedSet(bsf,0,i/2);			
			BoundedSet<String> bs2 = boundedSet(bsf,0,i/2 + 1);
			BoundedSet<String> bs3 = boundedSet(bsf,1,i/2 + 1);
			BoundedSet<String> bsempty = bsf.empty();
			BoundedSet<String> bsSingle = bsf.singleton(getElement(0));
			BoundedSet<String> bstop = bsf.top();
			BoundedSet[] all = { bs1, bs2, bs3, bsempty, bsSingle, bstop };
			for(BoundedSet<String> bs : all) {
				assert(bs.isSubset(bs)); // reflexivity
				assert(bsempty.isSubset(bs)); // bottom element
				assert(bs.isSubset(bstop)); // top element
				if(bs != bsempty && bs != bs3) assert(bsSingle.isSubset(bs));
				if(bs != bstop) assert(bs.isSubset(bs2));				
			}
			assert(bs1.isSubset(bs2));
			assert(bs3.isSubset(bs2));
			assert(! bs1.isSubset(bs3) || bs1.isSaturated() || bs3.isSaturated());
			assert(! bs3.isSubset(bs2) || bs2.isSaturated() || bs3.isSaturated());			
		}
	}
	
	
	private String getElement(int i) {
		return "x"+i;
	}
	private void addElements(BoundedSet<String> bs, int first, int last) {
		for(int i = first; i <= last; i++) {
			bs.add(getElement(i));
		}
	}
	private BoundedSet<String> boundedSet(BoundedSetFactory<String> bsf, int first, int last) {
		BoundedSet<String> bs = bsf.empty();
		addElements(bs,first,last);
		return bs;
	}

	/**
	 * Test method for {@link com.jopdesign.dfa.framework.BoundedSetFactory#top()}.
	 */
	@Test
	public void testTop() {
		for(int i : TEST_BOUNDS) {
			BoundedSetFactory<String> bsf = new BoundedSetFactory<String>(i);
			assert(bsf.top().isSaturated());
			assert(bsf.top().getSize() == i+1);
			try {
				bsf.top().getSet();
				fail("Expected failure: top().getSet()");				
			} catch(Error e) {
				
			}
		}
	}
}
