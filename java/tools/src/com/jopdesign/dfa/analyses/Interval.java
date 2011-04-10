/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
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

package com.jopdesign.dfa.analyses;

import java.io.Serializable;
import java.util.Arrays;

@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
public class Interval implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int TOP = Integer.MAX_VALUE;
	public static final int BOT = Integer.MIN_VALUE;
	
	private int lb;
	private boolean lv;
	private int ub;
	private boolean uv;
	
    public Interval(Interval val) {
		lb = val.lb;
		lv = val.lv;
		ub = val.ub;
		uv = val.uv;
	}

	public Interval() {
		lb = BOT;
		lv = false;
		ub = TOP;
		uv = false;
	}

	public Interval(int lb, int ub) {
		this.lb = lb;
		this.lv = true;
		this.ub = ub;
		this.uv = true;
	}

	public int getLb() {
		return lb;
	}
	
	public void setLb(int lb) {
		this.lb = lb;
		this.lv = true;
	}
	
	public boolean hasLb() {
		return lv;
	}
	
	public int getUb() {
		return ub;
	}

	public void setUb(int ub) {
		this.ub = ub;
		this.uv = true;
	}
	
	public boolean hasUb() {
		return uv;
	}

	public void add(int val) {
		if (lv) {
			long l = lb+val;
			if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
				lb = BOT;
				lv = false;
			} else {
				lb += val;
			}
		}
		if (uv) {
			long l = ub+val;
			if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
				ub = TOP;
				uv = false;
			} else {			
				ub += val;
			}
		}
	}
	
	public void add(Interval val) {
		if (lv && val.lv) {
			long l = lb+val.lb;
			if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
				lb = BOT;
				lv = false;
			} else {
				lb += val.lb;
			}
		} else {
			lb = BOT;
			lv = false;
		}
		if (uv && val.uv) {
			long l = ub+val.ub;
			if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
				ub = TOP;
				uv = false;
			} else {			
				ub += val.ub;
			}
		} else {
			ub = TOP;
			uv = false;
		}
	}
	
	public void sub(Interval val) {
		if (lv && val.uv) {
			long l = lb-val.ub;
			if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
				lb = BOT;
				lv = false;
			} else {
				lb -= val.ub;
			}
		} else {
			lb = BOT;
			lv = false;
		}
		if (uv && val.lv) {
			long l = ub-val.lb;
			if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
				ub = TOP;
				uv = false;
			} else {			
				ub -= val.lb;
			}
		} else {
			ub = TOP;
			uv = false;
		}
	}
	
	public void ushr(Interval val) {
		if (lv && val.lv) {
			lb >>>= val.lb;
			ub >>>= val.lb;
		} else {
			lb = BOT;
			lv = false;
			ub = TOP;
			uv = false;
		}
	}

	public void shr(Interval val) {
		if (lv && val.lv) {
			lb >>= val.lb;
			ub >>= val.lb;
		} else {
			lb = BOT;
			lv = false;
			ub = TOP;
			uv = false;
		}
	}
	
	public void div(Interval val) {
		if (lv && val.lv) {
			if ((val.lb > 0 && val.ub > 0)  // val does not contain zero
				 || (val.lb < 0 && val.ub < 0)) { 

				long[] bounds = new long[4];
				bounds[0] = lb/val.lb;
				bounds[1] = lb/val.ub;
				bounds[2] = ub/val.lb;
				bounds[3] = ub/val.ub;
				Arrays.sort(bounds);
				long resL = bounds[0];
				long resU = bounds[3];
				if (resL == (long)(int)resL
						&& resU == (long)(int)resU) {
					lb = (int)resL;
					ub = (int)resU;
				}				
			} else {
				lb = BOT;
				lv = false;
				ub = TOP;
				uv = false;				
			}
		} else {
			lb = BOT;
			lv = false;
			ub = TOP;
			uv = false;
		}
	}
	
	public void mul(Interval val) {
		if (lv && val.lv) {
			long[] bounds = new long[4];
			bounds[0] = lb*val.lb;
			bounds[1] = lb*val.ub;
			bounds[2] = ub*val.lb;
			bounds[3] = ub*val.ub;
			Arrays.sort(bounds);
			long resL = bounds[0];
			long resU = bounds[3];
			if (resL == (long)(int)resL
					&& resU == (long)(int)resU) {
				lb = (int)resL;
				ub = (int)resU;
			} else {
				lb = BOT;
				lv = false;
				ub = TOP;
				uv = false;				
			}
		} else {
			lb = BOT;
			lv = false;
			ub = TOP;
			uv = false;
		}
	}

	public void neg() {
		boolean newLv;
		int newLb;
		boolean newUv;
		int newUb;
		
		newLv = uv;
		newLb = -ub; 
		newUv = lv;
		newUb = -lb;
		
		lv = newLv;
		lb = newLb;
		uv = newUv;
		ub = newUb;
	}
	
	public void join(Interval val) {
		lb = Math.min(lb, val.lb);
		lv = lv && val.lv;
		ub = Math.max(ub, val.ub);
		uv = uv && val.uv;
	}
	
	public void constrain(Interval val) {
		if (lv && val.lv) {
			lb = Math.max(lb, val.lb);
		} else if (val.lv) {
			lb = val.lb;
			lv = true;
		}
		if (uv && val.uv) {
			ub = Math.min(ub, val.ub);
		} else if (val.uv) {
			ub = val.ub;
			uv = true;
		}
	}
	
	public void widen(Interval val) {
		if (val.lv && lb > val.lb) {
			lb = val.lb;
		}
		if (val.uv && ub < val.ub) {
			ub = val.ub;
		}
	}
	
	public int hashCode() {
		return lb*31 + ub*31*31;
	}

	public boolean equals(Object obj) {
        if (!(obj instanceof Interval)) return false;

		Interval i = (Interval) obj;
		if (lb != i.lb || ub != i.ub)
			return false;
		return true;
	}
	
	public boolean compare(Object obj) {
		Interval i = (Interval) obj;
		if (lb < i.lb || ub > i.ub)
			return false;
		return true;
	}

	public String toString() {
		return "("+(lv?lb:"BOT")+","+(uv?ub:"TOP")+")";
	}

}
