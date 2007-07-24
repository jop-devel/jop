/**
 * 
 */
package wcet.components.graphbuilder.methodgb;

/**
 * @author Elena Axamitova
 * @version 0.1 22.01.2007
 * 
 * Method identifier.
 */
public class MethodKey {
    /**
     * enclosing class name
     */
    private String owner;

    /**
     * name of the method
     */
    private String name;

    /**
     * desriptor of the method (java class file method descriptor format)
     */
    private String description;

    /**
     * Construct new method key for the data. Cannot be changed later.
     * @param o - method owner class
     * @param n - method name
     * @param d - method deriptor
     */
    public MethodKey(String o, String n, String d) {
	this.owner = o.replace('.', '/');
	this.name = n;
	this.description = d;
    }

    /**
     * @return method class 
     */
    public String getOwner() {
	return this.owner;
    }

    /**
     * @return method name
     */
    public String getName() {
	return this.name;
    }

    /**
     * @return method descriptor
     */
    public String getDecription() {
	return this.description;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	MethodKey obj = (MethodKey) o;
	boolean result = ((obj.owner.equals(this.owner))
		&& (obj.description.equals(this.description)) && (obj.name
		.equals(this.name)));

	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
//	needed, since MethodKey is used as a key in HashMaps
	//must be conform to equals(.)
	return (new String(this.getOwner() + "$" + this.getName() + "$"
		+ this.getDecription())).hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return this.getOwner() + "||" + this.getName() + "||"
		+ this.getDecription();
    }
}
