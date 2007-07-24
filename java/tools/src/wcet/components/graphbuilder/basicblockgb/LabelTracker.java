/**
 * 
 */
package wcet.components.graphbuilder.basicblockgb;

import java.util.HashMap;
import java.util.HashSet;

import org.objectweb.asm.Label;

/**
 * @author Elena Axamitova
 * @version 0.1 11.01.2007
 * 
 * Stores encountred labels and node that start at those labels.
 * Registers requests for not yet encoutered labels.
 */
// TODO handle exceptions(complex) ???here
public class LabelTracker {
    /**
     * map of processed labels to node ids that start on these labels
     */
    private HashMap<Label, Integer> offerMap = null;

    /**
     * map of not yet encountered labels to node ids that wait for them
     */
    private HashMap<Label, HashSet<Integer>> demandMap = null;

    /**
     * map of labels to their positions (line numbers) in source 
     */
    private HashMap<Label, Integer> labelToLineNrMap = null;

    protected LabelTracker() {
	this.offerMap = new HashMap<Label, Integer>();
	this.demandMap = new HashMap<Label, HashSet<Integer>>();
	this.labelToLineNrMap = new HashMap<Label, Integer>();
    }

    /**
     * @param label - start node label
     * @return id of the block that starts at this label or -1 no such node exists
     */
    protected int getOffer(Label label) {
	Integer result = this.offerMap.get(label);
	if (result == null) {
	    return -1;
	} else {
	    return result.intValue();
	}
    }

    /**
     * @param label - new label
     * @return ids of all blocks that wait for this label
     */
    protected HashSet<Integer> getDemand(Label label) {
	return this.demandMap.get(label);
    }

    /**
     * @param label - method label
     * @return source linenumber of the label position
     */
    protected int getLineNrToLabel(Label label) {
	return this.labelToLineNrMap.get(label).intValue();
    }

    /**
     * Reset this LabelTracker
     */
    protected void clear() {
	this.offerMap.clear();
	this.demandMap.clear();
	this.labelToLineNrMap.clear();
    }

    /**
     * Add label to node mapping.
     * 
     * @param label - new label
     * @param id - id of the corresponding node
     */
    protected void addOffer(Label label, int id) {
	this.offerMap.put(label, id);
    }

    /**
     * Register demand for node starting at the given label.
     * 
     * @param id - id of the waiting node
     * @param label - label the node waits for
     */
    protected void addDemand(int id, Label label) {
	HashSet<Integer> labelDemand = this.demandMap.get(label);
	if (labelDemand != null) {
	    labelDemand.add(id);
	} else {
	    labelDemand = new HashSet<Integer>();
	    labelDemand.add(id);
	    this.demandMap.put(label, labelDemand);
	}
    }

    /**
     * Delete demand for this label.
     * 
     * @param label - label of this method
     */
    protected void removeDemand(Label label) {
	this.demandMap.remove(label);
    }

    /**
     * Save the position of this label in source file
     * @param label - label of this method
     * @param id - line number
     */
    protected void addLabelToLineNrMapping(Label label, int id) {
	this.labelToLineNrMap.put(label, id);
    }
}
