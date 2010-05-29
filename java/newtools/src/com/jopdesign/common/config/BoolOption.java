/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.common.config;

/**
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp <stefan@stefant.org>
 */
public class BoolOption extends Option<Boolean> {

	public BoolOption(String key, String descr) {
		super(key, Boolean.class, descr, false);
	}

	public BoolOption(String key, String descr, boolean def) {
		super(key,descr,def);
	}

	public Boolean parse(String s) throws IllegalArgumentException {
		String sl = s.toLowerCase();
		if(sl.equals("true") || sl.equals("yes") || sl.equals("y")) return Boolean.TRUE;
		else if (sl.equals("false") || sl.equals("no") || sl.equals("n")) return Boolean.FALSE;
		else throw new IllegalArgumentException("Cannot parse boolean: "+sl);
	}

}