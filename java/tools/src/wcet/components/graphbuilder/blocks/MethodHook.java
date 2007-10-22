/**
 * 
 */
package wcet.components.graphbuilder.blocks;

import wcet.components.graphbuilder.methodgb.MethodBlock;


/**
 * @author Elena Axamitova
 * @version 0.1 17.03.2007
 * 
 * Representation of an (yet) unprocesed method invocation in the control flow graph.
 */

public class MethodHook {
    
    /**
     * method block of the invoked method
     */
    private MethodBlock methB;
    
    /**
     * vertex id of the return block of this method
     */
    private int retBB;
    
    /**
     * vertex id of the invoke block of this method
     */
    private int invBB;
    
    //private MethodBlock methBlock;
    
    public MethodHook(int invBB, int retBB, MethodBlock mb){
	this.retBB = retBB;
	this.invBB = invBB;
	this.methB = mb;
    }
   
    /**
     * @return corresponding return block vertex id
     */
    public int getReturnBlock(){
	return this.retBB;
    }
    
    /**
     * @return corresponding invoke block vertex id
     */
    public int getInvokeBlock(){
	return this.invBB;
    }
    
    /**
     * @return corresponding method block 
     */
    public MethodBlock getMethodBlock(){
	return this.methB;
    }
}
