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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A term of the form
 * <code>c_1 * v_1 + ... + c_n * v_n</code>, 
 * where v_i denotes a variable and c_i an (integral) coefficient.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 * @param <T> type of the variables
 */
public class LinearVector<T> {
	private HashMap<T,Long> coeffs;

	/**
	 * Initialize the linear vector, with all coefficient 0.
	 */
	public LinearVector() {
		coeffs = new HashMap<T,Long>();
	}
	
	/**
	 * Create a copy of the given linear vector
	 * @param vector 
	 */
	public LinearVector(LinearVector<T> vector) {
		this.coeffs = new HashMap<T,Long>(vector.getCoeffs());
	}
	
	/**
	 * the number of non-zero coefficients
	 */
	public int size() {
		return this.coeffs.size();
	}
	
	/**
	 * Add the product <code>coeff * var</code> to the linear vector
	 * @param var
	 * @param coeff
	 */
	public void add(T var, long coeff) {
		if(coeff == 0) return;
		Long oldCoeff = this.coeffs.get(var);
		if (oldCoeff == null) {
			this.coeffs.put(var,coeff);
		} else {
			long newCoeff = coeff+oldCoeff;
			if(newCoeff == 0) {
				this.coeffs.remove(var);
			} else {
				this.coeffs.put(var,newCoeff);
			}
		}
	}
	
	/**
	 * Multiply the vector with the given scalar
	 * @param c the number to multiply each coefficient with
	 */
	public void mul(long c) {
		for(Entry<T,Long> e : this.coeffs.entrySet()) {
			this.coeffs.put(e.getKey(), e.getValue() * c);
		}
	}
	/**
	 * Return the linear vector as a map
	 * @return map from variables to <emph>non-zero</emph> coefficients
	 */
	public Map<T, Long> getCoeffs() {
		return this.coeffs;
	}
	
	@Override
	public LinearVector<T> clone() {
		return new LinearVector<T>(this);
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		toStringWith(s,0);
		return s.toString();
	}
	/**
	 * Append the string representation of <code>a x + d</code>
	 * @param s the string buffer to append to
	 * @param d 
	 */
	public void toStringWith(StringBuffer s, long d) {
		boolean first = true;
		for(Entry<T,Long> e : coeffs.entrySet()) {
			if(e.getValue() == 0) continue;
			if(first) {
				first=false;
				if(e.getValue() < 0) s.append('-');
				appendAbsCoeff(s,e);
			} else {
				s.append(e.getValue() >= 0 ? " + " : " - ");
				appendAbsCoeff(s,e);
			}
		}
		if(d !=0 || first) {
			if(first) {
				if(d < 0) s.append("- ");
			} else {
				s.append(d >= 0 ? " + " : " - ");					
			}
			s.append(Math.abs(d));
		}
	}
	private void appendAbsCoeff(StringBuffer s, Entry<T, Long> e) {
		long c = Math.abs(e.getValue());
		if(c != 1) s.append(c+" ");
		s.append(e.getKey());
	}
}