/**
 * 
 */
package wcet.framework.general;

import java.util.Hashtable;
import java.util.Vector;

import wcet.framework.interfaces.solver.IConstraint;


/**
 * @author Elena
 * @version 0.1
 */
public final class SimpleDataStore extends AbstractDataStore {
	
	public SimpleDataStore(){
		this.output = null;
		this.graph = null;
		this.constraints = new Vector<IConstraint>();
		this.otherObjects = new Hashtable<String, Object>();
	}
}
