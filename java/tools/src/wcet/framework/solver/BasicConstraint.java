/**
 * 
 */
package wcet.framework.solver;

import java.util.ArrayList;
import java.util.Iterator;

import wcet.framework.interfaces.solver.IConstraint;
import wcet.framework.interfaces.solver.IConstraintTerm;

/**
 * @author Elena Axamitova
 * @version 0.1 15.04.2007
 */
public class BasicConstraint implements IConstraint {
    private ArrayList<IConstraintTerm> leftHandSide;
    private ArrayList<IConstraintTerm> rightHandSide;
    
    private int type;
    
    private String name;
    
    public BasicConstraint(int type){
	this.type = type;
	this.leftHandSide = new ArrayList<IConstraintTerm>();
	this.rightHandSide = new ArrayList<IConstraintTerm>();
	this.name = null;
    }
    
    public BasicConstraint(int type, String name){
	this.type = type;
	this.leftHandSide = new ArrayList<IConstraintTerm>();
	this.rightHandSide = new ArrayList<IConstraintTerm>();
	this.name = name;
    }
    /* (non-Javadoc)
     * @see wcet.framework.interfaces.constraints.IConstraint#addLeftHandTerm(wcet.framework.interfaces.constraints.IConstraintTerm)
     */
    public void addLeftHandTerm(IConstraintTerm ct) {
	this.leftHandSide.add(ct);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.constraints.IConstraint#addRightHandTerm(wcet.framework.interfaces.constraints.IConstraintTerm)
     */
    public void addRightHandTerm(IConstraintTerm ct) {
	this.rightHandSide.add(ct);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.constraints.IConstraint#getLeftHandSide()
     */
    public ArrayList<IConstraintTerm> getLeftHandSide() {
	return this.leftHandSide;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.constraints.IConstraint#getRghtHandSide()
     */
    public ArrayList<IConstraintTerm> getRightHandSide() {
	return this.rightHandSide;
    }
    
    /* (non-Javadoc)
     * @see wcet.framework.interfaces.constraints.IConstraint#getType()
     */
    public int getType() {
	return this.type;
    }
    
    @Override
    public String toString(){
	StringBuffer result = new StringBuffer();
	IConstraintTerm currTerm;
	
	if (this.name!=null)
	    result.append(this.name+": ");
	for(Iterator<IConstraintTerm> iterator = this.leftHandSide.iterator(); iterator.hasNext();){
	    currTerm = iterator.next();
	    result.append(currTerm.toString());
	}
	result.append(IConstraint.SIGN_STRINGS[this.type]);
	for(Iterator<IConstraintTerm> iterator = this.rightHandSide.iterator(); iterator.hasNext();){
	    currTerm = iterator.next();
	    result.append(currTerm.toString());
	}
	return result.toString();
    }
    public String getName() {
	return this.name;
    }
}
