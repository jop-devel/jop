package com.jopdesign.dfa.analyses;

public class Interval {

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
		// TODO: saturate
		if (lv) {
			lb += val;
		}
		if (uv) {
			ub += val;
		}
	}
	
	public void add(Interval val) {
		// TODO: saturate
		if (lv && val.lv) {
			lb += val.lb;
		} else {
			lb = BOT;
			lv = false;
		}
		if (uv && val.uv) {
			ub += val.ub;
		} else {
			ub = TOP;
			uv = false;
		}
	}
	
	public void sub(Interval val) {
		// TODO: saturate
		if (lv && val.lv) {
			lb -= val.ub;
		} else {
			lb = BOT;
			lv = false;
		}
		if (uv && val.uv) {
			ub -= val.lb;
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
		Interval i = (Interval) obj;
		if (lb != i.lb || ub != i.ub)
			return false;
		return true;
	}
	
	public String toString() {
		return "("+(lv?lb:"BOT")+","+(uv?ub:"TOP")+")";
	}

}
