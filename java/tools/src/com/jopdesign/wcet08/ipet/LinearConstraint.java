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
package com.jopdesign.wcet08.ipet;

import java.util.Map;
import java.util.Map.Entry;

public class LinearConstraint<T> {
	public enum ConstraintType { LessEqual, Equal, GreaterEqual; } 
	private ConstraintType cmp;
	private LinearVector<T> vec;
	private long inhom;
	public LinearConstraint(ConstraintType cmp) {
		this.vec = new LinearVector<T>();
		this.inhom = 0;		
		this.cmp   = cmp;
	}
	public void addLHS(T unit) { vec.add(unit,1); }
	public void addLHS(T unit, long coeff) { vec.add(unit,coeff); }
	public void addLHS(long d) { inhom-=d; }
	public void addRHS(T unit) { vec.add(unit,-1); }
	public void addRHS(T unit, long coeff) { vec.add(unit,-coeff); }
	public void addRHS(long d) { inhom+=d; }
	/**
	 * For a linear constraint <pre>a x + b op b y + d</pre>
	 * @return <pre>a x - b y</pre>
	 */
	public LinearVector<T> getLinearVectorOnLHS() {
		return this.vec;
	}
	/**
	 * For a linear constraint <pre>a x + b op b y + d</pre>
	 * @return <pre>d-b</pre>
	 */
	public long getInhomogenousTermOnRHS() {
		return this.inhom;
	}
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(vec.toString());
		switch(cmp) {
		case Equal : s.append(" = ");break;
		case LessEqual: s.append(" <= ");break;
		case GreaterEqual : s.append(" >= ");break;
		}
		s.append(inhom);
		return s.toString();
	}
	public ConstraintType getConstraintType() {
		return cmp;
	}
}
