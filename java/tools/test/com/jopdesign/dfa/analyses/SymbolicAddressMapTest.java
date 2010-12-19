/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.dfa.analyses;

import com.jopdesign.dfa.framework.BoundedSetFactory.BoundedSet;
import com.jopdesign.dfa.framework.BoundedSetFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SymbolicAddressMapTest {


	private BoundedSetFactory<SymbolicAddress> bsf2;

	@Before
	public void setUp() throws Exception {
		bsf2 = new BoundedSetFactory<SymbolicAddress>(2);
	}
	private BoundedSet<SymbolicAddress> boundedSet(Object... names) {
		BoundedSet<SymbolicAddress> bs = bsf2.empty();
		for(Object s : names) {
			SymbolicAddress addr = SymbolicAddress.rootAddress(s.toString());
			bs.add(addr);
		}
		return bs;
	}

	@After
	public void tearDown() throws Exception {
	}

	// 0 -> { a,b }
	// 1 -> { c }
	// 2 -> { b,c }
	// f1 -> { a,b }
	// f2 -> { top }
	private SymbolicAddressMap map1() {
		SymbolicAddressMap map1 = new SymbolicAddressMap(bsf2);
		map1.putStack(0, boundedSet("a","b"));
		map1.putStack(1, boundedSet("c"));
		map1.putStack(2, boundedSet("b","c"));
		map1.put(new Location("f1"), boundedSet("a"));
		map1.put(new Location("f2"), bsf2.top());
		return map1;
	}

	// 0 -> { b,c }
	// 1 -> { a }
	// f1 -> { a,b }
	// f3 -> { top }
	// f4 -> { a,b }
	private SymbolicAddressMap map2() {
		SymbolicAddressMap map2 = new SymbolicAddressMap(bsf2);
		map2.putStack(0, boundedSet("b","c"));
		map2.putStack(1, boundedSet("c"));
		map2.put(new Location("f1"), boundedSet("a","b"));
		map2.put(new Location("f3"), bsf2.top());
		map2.put(new Location("f4"), boundedSet("a","b"));
		return map2;
	}

	@Test
	public void testHashCode() {
		assert(map1().hashCode() == map1().hashCode());
		assert(map1().hashCode() != map2().hashCode());
		assert(map1().hashCode() != SymbolicAddressMap.top().hashCode());
		assert(map2().hashCode() == map2().hashCode());
		assert(map2().hashCode() != SymbolicAddressMap.top().hashCode());
		assert(SymbolicAddressMap.top().hashCode() == SymbolicAddressMap.top().hashCode());
	}

	@Test
	public void testIsTop() {
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		assert(! m1.isTop());
		assert(! m2.isTop());
		assert(SymbolicAddressMap.top().isTop());
		m1.join(SymbolicAddressMap.top());
		assert(m1.isTop());
	}

	@Test
	public void testEqualsObject() {
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		assert(m1.equals(m1));
		assert(m1.equals(map1()));
		assert(m2.equals(map2()));
		assert(! m1.equals(m2));
		m1.join(SymbolicAddressMap.top());
		assert(m1.equals(SymbolicAddressMap.top()));
		m2.join(SymbolicAddressMap.top());
		assert(m1.equals(m2));
	}

	@Test
	public void testIsSubset() {
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		SymbolicAddressMap top = SymbolicAddressMap.top();
		assert(! m1.isSubset(m2));
		assert(! m2.isSubset(m1));
		assert(m1.isSubset(map1()));
		assert(m2.isSubset(map2()));
		assert(m1.isSubset(top));
		assert(m2.isSubset(top));
		assert(! top.isSubset(m1));
		assert(! top.isSubset(m2));
		SymbolicAddressMap m12 = m1.clone(); m12.join(m2);
		SymbolicAddressMap m21 = m2.clone(); m21.join(m1);
		assert(m12.equals(m21));
		assert(m1.isSubset(m12));
		assert(m1.isSubset(m12));
		assert(m2.isSubset(m21));
		assert(m2.isSubset(m21));
	}

	@Test
	public void testClone() {
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		SymbolicAddressMap top = SymbolicAddressMap.top().clone();
		assert(m1.equals(m1.clone()));
		assert(m1.equals(m2.clone()));
		assert(m1.equals(top.clone()));
	}

	@Test
	public void testCloneFilterStack() {
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		SymbolicAddressMap top = SymbolicAddressMap.top();

		SymbolicAddressMap m1a = m1.cloneFilterStack(2);
		assert(! m1a.equals(m1));
		assert(m1a.isSubset(m1));
		assert(m1a.getStack(0).equals(m1.getStack(0)));
		assert(m1a.getStack(1).equals(m1.getStack(1)));
		assert(!m1a.getStack(2).equals(m1.getStack(2)));
		m1a.putStack(2, m1.getStack(2));
		assert(m1a.equals(m1));
		
		SymbolicAddressMap m2a = m2.cloneFilterStack(2);
		assert(m2a.equals(m2));

		SymbolicAddressMap topa = top.cloneFilterStack(1);
		assert(topa.equals(top));
	}

	@Test
	public void testCloneInvoke() {
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		SymbolicAddressMap top = SymbolicAddressMap.top();

		// Trivial
		assertEquals(m1, m1.cloneInvoke(0));

		int framePtr = 2;
		// Stack[2] -> Stack[0]
		SymbolicAddressMap m1i = m1.cloneInvoke(framePtr);

		SymbolicAddressMap m1a = new SymbolicAddressMap(bsf2);
		m1a.addStackUpto(m1, framePtr);
		
		// m1i: Stack[0] -> Stack[2] and merge
		m1a.joinReturned(m1i, framePtr);
		assertEquals(m1,m1a);

		framePtr = 1;
		SymbolicAddressMap m2i = m2.cloneInvoke(framePtr);
		SymbolicAddressMap m2a = new SymbolicAddressMap(bsf2);
		m2a.addStackUpto(m2, framePtr);
		m2a.joinReturned(m2i, framePtr);
		assertEquals(m2,m2a);
		
		try {
			m1a.joinReturned(null, 0);
			fail("joinReturned(null) should throw AssertionError");
		} catch (AssertionError _) {}

		assertEquals(m1,m1a);
		m1a.joinReturned(SymbolicAddressMap.top(), 1);
		assertEquals(m1a,top);
		top.joinReturned(m1i, 1);
		assertEquals(SymbolicAddressMap.top(),top);		
	}

	@Test
	public void testAddStackUpto() {
		// most important functionality has been covered above
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		SymbolicAddressMap top = SymbolicAddressMap.top();
		// trivial (bound 0)
		SymbolicAddressMap m1a = m1.clone();
		m1a.addStackUpto(m2, 0);
		assertEquals(m1,m1a);
		// add undefined
		try {
			m1.addStackUpto(null, 0);
			fail("addStackUpto(undefined,..) should fail");
		} catch(AssertionError _) {}
		try {
			top.addStackUpto(null, 0);						
			fail("addStackUpto(undefined,..) should fail");
		} catch(AssertionError _) {}
		// top is handled conservatively
		m1.addStackUpto(top, 1);
		assertEquals(m1,top);
	}

	@Test
	public void testJoin() {
		SymbolicAddressMap m1 = map1();
		SymbolicAddressMap m2 = map2();
		SymbolicAddressMap top = SymbolicAddressMap.top();
		// join with undefined
		assertEquals(join(m1,null),m1);
		assertEquals(join(m2,null),m2);
		assertEquals(join(top,null),top);
		// join reflexivity
		assertEquals(join(m1,m2),join(m2,m1));
		// join top
		assertEquals(join(m1,top),top);
		assertEquals(join(top,m1),top);
		assertEquals(join(top,top),top);
		// join subset
		assert(m1.isSubset(join(m1,m2)));
		assert(m1.isSubset(join(m2,m1)));
		assert(m2.isSubset(join(m1,m2)));
		assert(m2.isSubset(join(m2,m1)));
		assert(join(m1,m2).isSubset(top));		
	}
	private SymbolicAddressMap join(SymbolicAddressMap m1, SymbolicAddressMap m2) {
		SymbolicAddressMap m12 = m1.clone();
		m12.join(m2);
		return m12;
	}
	@Test
	public void testJoinReturned() {
		// partly covered above
	}

	@Test
	public void testGetStack() {
		SymbolicAddressMap m1 = map1();
		assertEquals(m1.getStack(0),boundedSet("a","b"));
		m1.putStack(3,bsf2.top());
		assertEquals(m1.getStack(3),SymbolicAddressMap.top().getStack(3));
	}


	@Test
	public void testPut() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testPutStack() {
		fail("Not yet implemented"); // TODO
	}

}
