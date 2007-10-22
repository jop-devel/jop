/**
 * 
 */
package wcet.framework.interfaces.solver;

/**
 * @author Elena Axamitova
 * @version 0.1 15.04.2007
 */
public interface IConstraintTerm {
    public int getCoefficient();
    public String toString();
    public String getVariable();
}
