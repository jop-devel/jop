package com.jopdesign.wcet.analysis.cache;

/**
 * Instances of this class represent low level control-flow edges.
 * They are only represented in the IPET problem, and usually model
 * a case distinction on a program flow edge
 * (Example: hit edge + miss edge = cfg edge).
 * The split ID is used to distinguish low-level edges which
 * have the same parent model.
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class LowLevelEdgeModel<T> {
	private T parent;
	private Object splitID;

	public LowLevelEdgeModel(T parent, Object splitID) {
		this.parent  = parent;
		this.splitID = splitID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (parent.hashCode());
		result = prime * result + (splitID.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LowLevelEdgeModel<?> other = (LowLevelEdgeModel<?>) obj;
		if (!parent.equals(other.parent)) return false;
		if (splitID.equals(other.splitID)) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LLEdge [parent=" + parent + ", " + splitID + "]";
	}			

}
