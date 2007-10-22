/**
 * 
 */
package wcet.framework.interfaces.solver;

import java.util.ArrayList;

/**
 * @author Elena Axamitova
 * @version 0.1 15.04.2007
 */
public interface IConstraint {
    public static final int EQUAL = 0;
    public static final int GREATEREQUAL = 1;
    public static final int GREATER = 2;
    public static final int LESSEQUAL = 3;
    public static final int LESS = 4;
    
    public static final int MAXIMIZE = 10;
    
    public static final int MINIMIZE = 11;
    
    public static final String[] SIGN_STRINGS = {"=",">=",">", "<=","<"};
    
    public String toString();
    
    public ArrayList<IConstraintTerm> getLeftHandSide();
    public ArrayList<IConstraintTerm> getRightHandSide();
    
    public void addRightHandTerm(IConstraintTerm ct);
    public void addLeftHandTerm(IConstraintTerm ct);
    
    public int getType();
    
    public String getName();
    
}
