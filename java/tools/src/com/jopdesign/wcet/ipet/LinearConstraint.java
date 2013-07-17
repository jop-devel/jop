/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.wcet.ipet;

/**
 * Integral linear constraint of the form <code>a x <=> b</code>.
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 * @param <T> Type of the variables in the linear constraint
 */
public class LinearConstraint<T> {
	public enum ConstraintType { LessEqual, Equal, GreaterEqual; } 

	private ConstraintType op;
	private LinearVector<T> vec;
	private long inhom;
	
	/**
	 * Initialize the linear constraint to <code>0 op 0</code>
	 * @param op the comparison operator
	 */
	public LinearConstraint(ConstraintType op) {
		this.vec = new LinearVector<T>();
		this.inhom = 0;		
		this.op   = op;
	}

	/**
	 * Copy constructor
	 * @param proto prototype to clone from
	 */
	public LinearConstraint(LinearConstraint<T> proto) {
		this.vec   = proto.vec.clone();
		this.inhom = proto.inhom;		
		this.op    = proto.op;
	}

	/**
	 * Add <code>coeff * var</code> to the left-hand-side of the constraint
	 * @param var  
	 * @param coeff
	 */
	public void addLHS(T var, long coeff) { vec.add(var,coeff); }
	public void addLHS(T var) { vec.add(var,1); }
	public void addLHS(Iterable<T> vars) { for(T v : vars) addLHS(v); }

	/**
	 * Add the constant integer <code>d</code> to the left-hand-side of the constraint
	 * @param d
	 */
	public void addLHS(long d) { inhom-=d; }

	/**
	 * Add <code>coeff * var</code> to the right-hand-side of the constraint
	 * @param var  
	 * @param coeff
	 */
	public void addRHS(T var, long coeff) { vec.add(var,-coeff); }
	public void addRHS(T var) { vec.add(var,-1); }
	public void addRHS(Iterable<T> vars) { for(T v : vars) addRHS(v); }

	/**
	 * Add the constant integer <code>d</code> to the right-hand-side of the constraint
	 * @param d
	 */
	public void addRHS(long d) { inhom+=d; }

	/**
	 * Return <code>a x</code> of <code>a x op b</code>
	 * @return the vector on the left hand side 
	 */
	public LinearVector<T> getLinearVectorOnLHS() {
		return this.vec;
	}
	/**
	 * Return <code>b</code> of <code>a x op b</code>
	 * @return the constant on the right hand side
	 */
	public long getInhomogenousTermOnRHS() {
		return this.inhom;
	}
	/** 
	 * the the type of the constraint: &lt;=, = or &gt;=
	 * @return 
	 */
	public ConstraintType getConstraintType() {
		return op;
	}

	@Override
	public LinearConstraint<T> clone() {
		return new LinearConstraint<T>(this);		
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(vec.toString());
		switch(op) {
		case Equal : s.append(" = ");break;
		case LessEqual: s.append(" <= ");break;
		case GreaterEqual : s.append(" >= ");break;
		}
		s.append(inhom);
		return s.toString();
	}
}
