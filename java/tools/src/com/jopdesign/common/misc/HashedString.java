/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Wolfgang Puffitsch
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

package com.jopdesign.common.misc;

import java.io.Serializable;

/**
 * A class which stores a string and its hash value.
 */
public class HashedString implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String value;
    private final int hash;

    public HashedString(String value) {
        this.value = value;
        this.hash = value.hashCode();
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        return value;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        try {
        	HashedString oHashedString = (HashedString)o;
        	if(hash != oHashedString.hash) 
        		return false;
            return value.equals(oHashedString.value);
        } catch(ClassCastException e) {
        	return false;
        }
    }
}
