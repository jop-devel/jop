/**
 * 
 */
package wcet.framework.constraints;

import java.util.ArrayList;
import java.util.Iterator;

import wcet.framework.interfaces.constraints.IConstraint;
import wcet.framework.interfaces.constraints.IConstraintTerm;

/**
 * @author Elena Axamitova
 * @version 0.1 17.04.2007
 */
public class ObjectiveFunction extends BasicConstraint{
    
    public ObjectiveFunction(int type){
	super(type);
    }
    
    public ArrayList<IConstraintTerm> getLeftHandSide(){
	return null;
    }
    
    public void addLeftHandTerm(IConstraintTerm ct) {
	//left hand side empty
    }
    
    @Override
    public String toString(){
	StringBuffer result = new StringBuffer();
	if (this.getType()==IConstraint.MAXIMIZE){
	    result.append("max: ");
	}else if(this.getType()==IConstraint.MINIMIZE){
	    result.append("min: ");
	}
	for(Iterator<IConstraintTerm> iterator = this.getRightHandSide().iterator(); iterator.hasNext();){
	    IConstraintTerm currTerm = iterator.next();
	    result.append(currTerm.toString()+" ");
	}
	return result.toString();
    }
}

