/**
 * 
 */
package wcet.framework.interfaces.solver;

import java.util.ArrayList;

/**
 * @author Elena Axamitova
 * @version 0.1 15.04.2007
 */
public interface IDoubleSidedConstraint extends IConstraint {
    public ArrayList<IConstraintTerm> getMiddleSide();
    
    public void addMiddleTerm(IConstraintTerm ct);
}
