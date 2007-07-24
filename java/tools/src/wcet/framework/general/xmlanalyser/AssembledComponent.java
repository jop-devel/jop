/**
 * 
 */
package wcet.framework.general.xmlanalyser;

import java.util.ArrayList;
import java.util.Iterator;

import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.util.IComponentRunner;
import wcet.framework.util.ComponentRunner;

/**
 * @author Elena Axamitova
 * @version 0.3
 */
public class AssembledComponent implements IAnalyserComponent {
	private ArrayList<IAnalyserComponent> components;

	private boolean onlyOne = true;

	private int order = IGlobalComponentOrder.UNKNOWN_ORDER;
	
	public AssembledComponent(int order) {
		this(true, order);
	}
	
	
	public AssembledComponent(boolean onlyOne, int order) {
		this.onlyOne = onlyOne;
		this.order = order;
		this.components = new ArrayList<IAnalyserComponent>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wcet.interfaces.general.IAnalyserComponent#addComponent(wcet.interfaces.general.IAnalyserComponent)
	 */
	public void addComponent(IAnalyserComponent component) {
		this.components.add(component);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wcet.interfaces.general.IAnalyserComponent#getOnlyOne()
	 */
	public boolean getOnlyOne() {
		return onlyOne;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wcet.interfaces.general.IAnalyserComponent#getOrder()
	 */
	public int getOrder() {
		return this.order;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see wcet.interfaces.general.IAnalyserComponent#init()
	 */
	public void init() throws InitException {
		for (Iterator<IAnalyserComponent> iterator = this.components.iterator(); iterator
				.hasNext();) {
			iterator.next().init();
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	public String call() throws Exception {
		IComponentRunner runner = new ComponentRunner();
		Iterator<IAnalyserComponent> iterator = this.components.iterator();
		while (iterator.hasNext()) {
			IAnalyserComponent temp = iterator.next();
			runner.registerComponent(temp);
		}
		runner.startComponents();
		return "AssembledComponent of order " +this.getOrder() + " executed all its subcomponents successfully.\n";
	}

}
