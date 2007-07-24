/**
 * 
 */
package wcet.framework.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import wcet.framework.exceptions.SurplusComponentException;
import wcet.framework.exceptions.TaskExecutionException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.util.IComponentRunner;

/**
 * A simple implementation of the IComponentRunner interface.
 * 
 * @author Elena Axamitova
 * @version 0.3
 */
public final class ComponentRunner implements IComponentRunner {

    private PrintStream output;

    /**
         * Component storage
         */
    private Hashtable<Integer, LinkedList<IAnalyserComponent>> components;

    /**
         * Creates a ComponentRunner object
         */
    public ComponentRunner() {
	this.components = new Hashtable<Integer, LinkedList<IAnalyserComponent>>(
		10);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.util.IComponentRunner#setOutput(java.io.PrintStream)
     */
    public void setOutput(PrintStream ps) {
	this.output = ps;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.util.IComponentRunner#registerComponent(wcet.interfaces.general.IAnalyserComponent)
         */
    public void registerComponent(IAnalyserComponent component)
	    throws SurplusComponentException {
	Integer order = Integer.valueOf(component.getOrder());
	LinkedList<IAnalyserComponent> concurrentComponents = this.components
		.get(order);
	if (concurrentComponents == null) {
	    concurrentComponents = new LinkedList<IAnalyserComponent>();
	    concurrentComponents.add(component);
	} else if (component.getOnlyOne() == true) {
	    throw new SurplusComponentException(
		    "There is a previously registred component of the same order and the new component of type "
			    + component.getClass().getCanonicalName()
			    + " does not allow concurrency.");
	} else {
	    IAnalyserComponent present = concurrentComponents.getFirst();
	    if (present.getOnlyOne()) {
		throw new SurplusComponentException(
			"Previously registred component of type "
				+ present.getClass().getCanonicalName()
				+ " does not allow concurrency.");
	    } else {
		concurrentComponents.add(component);
	    }
	}
	this.components.put(order, concurrentComponents);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.util.IComponentRunner#removeComponent(wcet.interfaces.general.IAnalyserComponent)
         */
    public boolean removeComponent(IAnalyserComponent component) {
	Integer order = Integer.valueOf(component.getOrder());
	LinkedList<IAnalyserComponent> concurrentComponents = this.components
		.get(order);
	if (concurrentComponents != null) {
	    return concurrentComponents.remove(component);
	}
	return false;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.util.IComponentRunner#startComponents()
         */
    public void startComponents() throws TaskExecutionException {
	//component are executed, sorted by their order
	Vector<Integer> orderNr = new Vector<Integer>(this.components.keySet());
	Collections.sort(orderNr);
	int firstOrderNr = orderNr.firstElement().intValue();
	int lastOrderNr = orderNr.lastElement().intValue();
	for (int i = firstOrderNr; i <= lastOrderNr; i++) {
	    Integer currPriority = Integer.valueOf(i);
	    if (components.containsKey(currPriority)) {
		//components of the same order are executed concurrently
		this.callComponents(currPriority);
	    }
	}
    }

    /*
         * Executes all regisred components with the given priority one after
         * one
         * 
         * @param priority - the priority of the components to be executed
         * @throws TaskExecutionException - when problems occur
         */
    /*
         * private void executeComponents(Integer priority) throws
         * TaskExecutionException { Queue<IAnalyserComponent> sortedComponents =
         * components.get(priority); String messages = null; while
         * (!sortedComponents.isEmpty()) { try { messages =
         * sortedComponents.poll().call(); if (messages != null) {
         * System.out.println(messages); } } catch (Exception e) { throw new
         * TaskExecutionException(e); } }
         *  }
         */

    /**
         * Runs all regisred components with the given priority concurrently.
         * 
         * @param priority -
         *                the priority of the components to be executed
         * @throws TaskExecutionException -
         *                 when problems occur
         */
    private void callComponents(Integer priority) throws TaskExecutionException {
	Queue<IAnalyserComponent> concurrentComponents = components
		.get(priority);
	ExecutorService myExecService = Executors.newFixedThreadPool(10);
	List<Future<String>> results = new ArrayList<Future<String>>();
	while (!concurrentComponents.isEmpty()) {
	    results.add(myExecService.submit(concurrentComponents.poll()));
	}
	try {
	    myExecService.shutdown();
	    myExecService.awaitTermination(3000, TimeUnit.SECONDS);
	    for (Iterator<Future<String>> iterator = results.iterator(); iterator
		    .hasNext();) {
		Future<String> currResult = iterator.next();
		try {
		    String message = currResult.get();
		    if ((this.output != null) && (message != null))
			this.output.println(message);
		} catch (ExecutionException e) {
		    throw new TaskExecutionException(e);
		}
	    }
	} catch (InterruptedException e) {
	    throw new TaskExecutionException(e);
	}
    }

}
