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

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

public class LinearVector<T> {
	private Hashtable<T,Long> coeffs;
	public LinearVector() {
		coeffs = new Hashtable<T,Long>();
	}
	public LinearVector(LinearVector<T> vecL) {
		this.coeffs = new Hashtable<T,Long>(vecL.getCoeffs());
	}
	public int size() {
		return this.coeffs.size();
	}
	public void add(T elem, long offs) {
		if(offs == 0) return;
		Long oldCoeff = this.coeffs.get(elem);
		if (oldCoeff == null) {
			this.coeffs.put(elem,offs);
		} else {
			long newCoeff = offs+oldCoeff;
			if(newCoeff == 0) {
				this.coeffs.remove(elem);
			} else {
				this.coeffs.put(elem,newCoeff);
			}
		}
	}
	public void mul(long c) {
		for(Entry<T,Long> e : this.coeffs.entrySet()) {
			this.coeffs.put(e.getKey(), e.getValue() * c);
		}
	}
	public Map<T, Long> getCoeffs() {
		return this.coeffs;
	}
	public String toString() {
		StringBuffer s = new StringBuffer();
		toStringWith(s,0);
		return s.toString();
	}
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