/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet.uppaal.translator.cache;

import com.jopdesign.wcet.uppaal.model.Transition;

public abstract class DynamicCacheBuilder extends CacheSimBuilder {
	public boolean isDynamic() {
		return true;
	}
	public boolean isAlwaysMiss() {
		return false;
	}
	public void onHit(Transition trans) {
		trans.getAttrs().appendGuard("lastHit");
	}

	public void onMiss(Transition trans) {
		trans.getAttrs().appendGuard("! lastHit");
	}

}
