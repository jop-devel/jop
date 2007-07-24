/**
 * 
 */
package wcet.framework.general;

import wcet.framework.exceptions.SurplusComponentException;
import wcet.framework.exceptions.TaskExecutionException;
import wcet.framework.interfaces.general.IAnalyser;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.util.IComponentRunner;
import wcet.framework.util.ComponentRunner;

/**
 * @author Elena
 * @version 0.1
 */
public abstract class AbstractAnalyser implements IAnalyser {
	
	protected IComponentRunner componentRunner;
	
	protected IDataStore dataStore;
	
	protected AbstractAnalyser(){
		this.componentRunner = new ComponentRunner();
	}
	
	public void analyse() throws TaskExecutionException{
		this.componentRunner.startComponents();
	}
	
	protected void addComponent(IAnalyserComponent component) throws SurplusComponentException {
		if (component.getOrder()>0){
			this.componentRunner.registerComponent(component);
		}
	}

	protected boolean removeComponent(IAnalyserComponent component) {
		return this.componentRunner.removeComponent(component);
	}
	
}
