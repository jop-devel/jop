/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
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

package com.jopdesign.common.misc;

/**
 * Raised if the given graph isn't reducible, i.e. there are cycles without back-edges,
 * or if there is more than one component.
 */
public class BadGraphException extends Exception {
    private static final long serialVersionUID = 1L;

    public BadGraphException() {
    }

    public BadGraphException(String message) {
        super(message);
    }

    public BadGraphException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadGraphException(Throwable cause) {
        super(cause);
    }
}
