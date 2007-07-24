/**
 * 
 */
package wcet.framework.constraints;

import wcet.framework.interfaces.constraints.IConstraintTerm;

/**
 * @author Elena Axamitova
 * @version 0.1 15.04.2007
 */
public class BasicConstraintTerm implements IConstraintTerm {
    private int coeff;

    private String var;

    public BasicConstraintTerm(String v) {
	this.coeff = 1;
	this.var = v;
    }

    public BasicConstraintTerm(int c, String v) {
	this.coeff = c;
	this.var = v;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.constraints.IConstraintTerm#getCoefficient()
         */
    public int getCoefficient() {
	return this.coeff;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.constraints.IConstraintTerm#getValiable()
         */
    public String getVariable() {
	return this.var;
    }

    public String toString() {
	String result="";
	
	if (this.var == null)
	    result += Integer.valueOf(this.coeff).toString();
	else{
	    if(this.coeff>0){
		result+="+";
	    }
	    result += this.coeff + "*" + this.var;
	}
	return result;
    }

}
