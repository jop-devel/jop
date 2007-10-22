/**
 * 
 */
package wcet.components.graphbuilder.basicblockgb;

// import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.blocks.BasicBlock;
import wcet.components.graphbuilder.blocks.InvokeBlock;
import wcet.components.graphbuilder.blocks.MethodHook;
import wcet.components.graphbuilder.blocks.ReturnBlock;
import wcet.components.graphbuilder.methodgb.MethodBlock;
import wcet.framework.cfg.BasicControlFlowGraph;
import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.TaskExecutionException;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.cfg.IEdge;
import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.hierarchy.IHierarchy;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;
import wcet.framework.interfaces.instruction.IInstructionGenerator;
import wcet.framework.interfaces.instruction.IJOPMethodVisitor;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.6 25.02.2007
 */

public class GraphWriter implements IAnalyserComponent, ClassVisitor {

    /**
         * Instruction generator for the currently used instruction set
         */
    protected IInstructionGenerator instructionGenerator;

    /**
         * Next visitor in chain
         */
    protected ClassVisitor lastVisitor;

    protected MethodBlock currMethBlock;

    protected MethodHook currMethHook;

    protected Queue<MethodHook> methodHookQueue;

    /**
         * Control flow graph
         */
    protected BasicControlFlowGraph cfg;

    /**
         * Shared data store
         */
    protected IDataStore dataStore;

    /**
         * Error messages submitted during the execution
         */
    protected String errorMessages = "";

    private IHierarchy hierarchy;

    public GraphWriter(IDataStore ds) {
	this.dataStore = ds;
	this.methodHookQueue = new LinkedList<MethodHook>();
	this.dataStore.storeObject(
		IGraphBuilderConstants.LAST_BB_CLASS_VISITOR_KEY, this);
    }

    public boolean getOnlyOne() {
	return true;
    }

    public int getOrder() {
	return IGlobalComponentOrder.GRAPH_BUILDER
		+ IGraphBuilderConstants.GRAPH_WRITER;
    }

    public void init() throws InitException {
	this.lastVisitor = (ClassVisitor) this.dataStore
		.getObject(IGraphBuilderConstants.LAST_BB_CLASS_VISITOR_KEY);
	if (this.lastVisitor == null) {
	    throw new InitException(
		    "GraphWriter: no last visitor in the chain specified.");
	}
	this.instructionGenerator = (IInstructionGenerator) this.dataStore
		.getObject(IGraphBuilderConstants.INSTRUCTION_GENERATOR_KEY);
	if (this.instructionGenerator == null) {
	    throw new InitException(
		    "GraphWriter: no instruction generator specified.");
	}
	this.hierarchy = (IHierarchy) this.dataStore
		.getObject(IGraphBuilderConstants.HIERARCHY_KEY);
    }

    public String call() throws Exception {
	this.startGraph();
	while (!this.methodHookQueue.isEmpty()) {
	    this.currMethHook = this.methodHookQueue.poll();
	    this.currMethBlock = this.currMethHook.getMethodBlock();
	    lastVisitor.visitSource(this.currMethBlock.getSourceFile(), null);
	    this.currMethBlock.accept(lastVisitor);
	}

	/*if (!this.errorMessages.equals("")) {
	    throw new TaskExecutionException(this.errorMessages);
	} else {*/
	    this.dataStore.setGraph(this.cfg);
	//}
	return "+++Graph writer comleted successfully.+++\n";
    }

    public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
	    String arg3, String[] arg4) {
	return new GraphWriterVisitor();
    }

    protected void startGraph() {
	this.cfg = new BasicControlFlowGraph();
	// construct a method hook for the root method block
	MethodBlock currMB = (MethodBlock) this.dataStore
		.getObject(IGraphBuilderConstants.METHOD_BLOCK_TREE_ROOT_KEY);
	InvokeBlock invBB = new InvokeBlock(new MethodKey(currMB.getOwner(),
		currMB.name, currMB.desc));

	// select the correct invoke instruction - used for cache load time
	int oc = 0;
	if ((currMB.access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0)
	    oc = OpCodes.INVOKEINTERFACE;
	else if ((currMB.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0)
	    oc = OpCodes.INVOKESTATIC;
	else if (((currMB.access & org.objectweb.asm.Opcodes.ACC_PRIVATE) != 0)
		|| (currMB.name.equals("<init>"))
		|| (currMB.name.equals("<clinit>")))
	    // TODO more ???
	    oc = OpCodes.INVOKESPECIAL;
	else
	    oc = OpCodes.INVOKEVIRTUAL;
	invBB.setAnalyserInstruction(instructionGenerator.getMethodInsn(oc,
		currMB.getOwner(), currMB.name, currMB.desc));
	ReturnBlock retBB = new ReturnBlock(null);
	int invBBId = this.cfg.addVertex(invBB);
	int retBBId = this.cfg.addVertex(retBB);
	MethodHook currMethHook = new MethodHook(invBBId, retBBId, currMB);
	this.methodHookQueue.add(currMethHook);
    }

    protected class GraphWriterVisitor implements IJOPMethodVisitor {

	protected int lastVertexId;

	protected IAnalysisInstruction lastInstruction;

	protected BasicBlock currBB;

	protected Label currLabel;

	protected int currLineNr;

	protected boolean currBlockInRow = true;

	protected ArrayList<ReturnBlock> childrensReturnBlocks;

	protected LabelTracker labelTracker;

	protected HashMap<Integer, Label> lccIdToLabelMap;// lcc - loop

	// controler candidate

	protected TreeMap<Integer, Integer> lineNrToLoopControlerIdMap;

	protected ArrayList<Integer> annotationValues;

	protected HashMap<Integer, Integer> invBBIdToRetBBIdMap;

	protected HashMap<Integer, Integer> retBBIdToInvBBIdMap;

	// private JOPSystemMethodCache jopMethodsMap;

	private TreeMap<Label, Label> catchHandleToStartMap;

	private HashMap<Label, Label> catchHandleToEndMap;

	private int mySize;

	protected GraphWriterVisitor() {
	    this.childrensReturnBlocks = new ArrayList<ReturnBlock>();
	    this.labelTracker = new LabelTracker();
	    /*
                 * try { this.jopMethodsMap = new JOPSystemMethodCache( (String)
                 * dataStore
                 * .getObject(IGraphBuilderConstants.JOP_SYSTEM_CLASSPATH_KEY) +
                 * File.pathSeparator + dataStore
                 * .getObject(IGraphBuilderConstants.JOP_JDK_CLASSPATH_KEY)); }
                 * catch (InitException e) { // ignore; }
                 */
	    this.lccIdToLabelMap = new HashMap<Integer, Label>();
	    this.lineNrToLoopControlerIdMap = new TreeMap<Integer, Integer>();
	    this.annotationValues = new ArrayList<Integer>();
	    this.invBBIdToRetBBIdMap = new HashMap<Integer, Integer>();
	    this.retBBIdToInvBBIdMap = new HashMap<Integer, Integer>();
	    this.catchHandleToEndMap = new HashMap<Label, Label>();
	    this.catchHandleToStartMap = new TreeMap<Label, Label>(
		    new Comparator<Label>() {
			public int compare(Label l1, Label l2) {
			    return l1.toString().compareTo(l2.toString());
			}
		    });
	}

	protected void addInstruction(IAnalysisInstruction insn) {
	    if (this.currBB == null) {
		this.currBB = new BasicBlock();
	    }
	    this.currBB.addInstruction(insn);
	    this.lastInstruction = insn;
	    // this.mySize += insn.get8BitLength();
	}

	protected void endBasicBlock() {
	    if (this.currBB != null) {
		mySize += this.currBB.getSize();
		int newVertexId = cfg.addVertex(this.currBB);
		if (this.currLabel != null) {
		    this.labelTracker.addOffer(this.currLabel, newVertexId);
		    HashSet<Integer> labelDemand = this.labelTracker
			    .getDemand(this.currLabel);
		    if (labelDemand != null) {
			for (Iterator<Integer> i = labelDemand.iterator(); i
				.hasNext();) {
			    int fromId = i.next().intValue();
			    cfg.addEdge(fromId, newVertexId);
			}
		    }
		}
		if (this.currBlockInRow) {
		    cfg.addEdge(this.lastVertexId, newVertexId);
		}
		this.lastVertexId = newVertexId;
		this.currBB = null;
		this.currLabel = null;
		this.currBlockInRow = true;
	    }// currBB may be null, for example if there is a label
	    // immediately
	    // after a method call. In this case there is nothing to do.
	}

	@SuppressWarnings("unchecked")
	protected void identifyLoopControllers() {
	    int startVertexId = currMethHook.getInvokeBlock();
	    int endVertexId = currMethHook.getReturnBlock();
	    // potential loop controler ids
	    Iterator<Integer> lccIds = this.lccIdToLabelMap.keySet().iterator();
	    while (lccIds.hasNext()) {
		int currLCCId = lccIds.next();
		IVertex currLCC = cfg.findVertexByIndex(currLCCId);

		HashSet<Integer> notInLoopEdges = new HashSet<Integer>();
		boolean inLoopEdgeFound = false;
		// vertexes already visited
		ArrayList<Integer> currPathVisitedVertexes = new ArrayList<Integer>();
		// vertexes yet to visit
		LinkedList<Integer> currPathVertexesToVisit = new LinkedList<Integer>();
		// trying to find an incoming edge of the current lcc
		// that cannot be traced to the invoke block of the current
		// method without passing through the lcc - e.g a loop
		// closing incoming edge
		// saving all other incoming edges of lcc - e.g. a
		// loop entering incoming edges
		for (Iterator<Integer> iterator = currLCC.getIncomingEdges()
			.iterator(); iterator.hasNext();) {
		    int currLCCInEdgeId = iterator.next();
		    IEdge currLCCInEdge = cfg.findEdgeByIndex(currLCCInEdgeId);
		    currPathVertexesToVisit.clear();
		    currPathVisitedVertexes.clear();
		    int currLCCInEdgeFromVertex = currLCCInEdge.getFromVertex();
		    if (currLCCInEdgeFromVertex == startVertexId)
			notInLoopEdges.add(currLCCInEdgeId);
		    else if (currLCCInEdgeFromVertex == currLCCId)
			inLoopEdgeFound = true;
		    else
			currPathVertexesToVisit.add(currLCCInEdge
				.getFromVertex());
		    while (!currPathVertexesToVisit.isEmpty()) {
			int currVertexId = currPathVertexesToVisit.poll();
			IVertex currVertex = cfg
				.findVertexByIndex(currVertexId);
			// if a return block, not mine, change it for its
			// corresponding
			// invoke
			if (this.retBBIdToInvBBIdMap.containsKey(currVertexId)) {
			    currPathVertexesToVisit
				    .add(this.retBBIdToInvBBIdMap
					    .get(currVertexId));
			}
			for (Iterator<Integer> iterator2 = currVertex
				.getIncomingEdges().iterator(); iterator2
				.hasNext();) {
			    IEdge currEdge = cfg.findEdgeByIndex(iterator2
				    .next());
			    int currEdgeFromVertexId = currEdge.getFromVertex();
			    if (currEdgeFromVertexId == startVertexId) {
				// invoke block found
				notInLoopEdges.add(currLCCInEdgeId);
				currPathVertexesToVisit.clear();
				break;
			    } else if (currEdgeFromVertexId != currLCCId) {
				if (!currPathVisitedVertexes
					.contains(currEdgeFromVertexId))
				    currPathVertexesToVisit
					    .add(currEdgeFromVertexId);
			    }
			}
			currPathVisitedVertexes.add(currVertexId);
		    }
		    if (!notInLoopEdges.contains(currLCCInEdgeId))
			// all reachable vertexes visited and invoke not found
			// =>
			// a loop closing edge found
			inLoopEdgeFound = true;
		}
		if (!inLoopEdgeFound) {
		    // the candidate is not a loop controler
		    lccIds.remove();
		} else {
		    // now I have to find all outgoing edges that go to
		    // the loop body
		    HashSet<Integer> lccBodyOutEdges = new HashSet<Integer>();
		    for (Iterator<Integer> iterator = currLCC
			    .getOutgoingEdges().iterator(); iterator.hasNext();) {
			int currLCCOutEdgeId = iterator.next();
			IEdge currLCCOutEdge = cfg
				.findEdgeByIndex(currLCCOutEdgeId);
			currPathVertexesToVisit.clear();
			currPathVisitedVertexes.clear();
			currPathVertexesToVisit.add(currLCCOutEdge
				.getToVertex());
			int currLCCOutEdgeToVertexId = currLCCOutEdge
				.getToVertex();
			if (currLCCOutEdgeToVertexId == currLCCId)
			    lccBodyOutEdges.add(currLCCOutEdgeId);
			else if (currLCCOutEdgeToVertexId != endVertexId)
			    currPathVertexesToVisit.add(currLCCOutEdge
				    .getToVertex());
			// if an outgoing edge of a loop controler
			// can be followed to it again, without using
			// any of the loop controler notInLoopEdges(found
			// in previous step - nested loops), it goes into
			// the loop body.
			while (!currPathVertexesToVisit.isEmpty()) {
			    int currVertexId = currPathVertexesToVisit.poll();
			    IVertex currVertex = cfg
				    .findVertexByIndex(currVertexId);
			    // if an invoke block, not mine, change it for
			    // its corresponding return
			    if (this.invBBIdToRetBBIdMap
				    .containsKey(currVertexId)) {
				currPathVertexesToVisit
					.add(this.invBBIdToRetBBIdMap
						.get(currVertexId));
			    }
			    for (Iterator<Integer> iterator2 = currVertex
				    .getOutgoingEdges().iterator(); iterator2
				    .hasNext();) {
				int currEdgeId = iterator2.next();
				// a loop entering edge is not part of the loop
				if (!notInLoopEdges.contains(currEdgeId)) {
				    IEdge currEdge = cfg
					    .findEdgeByIndex(currEdgeId);
				    int currEdgeToVertexId = currEdge
					    .getToVertex();
				    if (currEdgeToVertexId == currLCCId) {
					lccBodyOutEdges.add(currLCCOutEdgeId);
					currPathVertexesToVisit.clear();
					break;
				    } else if (currEdgeToVertexId != endVertexId) {
					if (!currPathVisitedVertexes
						.contains(currEdgeToVertexId))
					    currPathVertexesToVisit
						    .add(currEdgeToVertexId);
				    }
				}
			    }
			    currPathVisitedVertexes.add(currVertexId);
			}
		    }
		    if (lccBodyOutEdges.isEmpty()) {
			// that should be never invoked
			lccIds.remove();
		    } else {
			// the currlccId vertex is a loop controler
			// save notInLoopEdges - incoming edges that enter the
			// loop
			// save lccBodyOutEdges - outgoing edges to loop body
			// (max 2)
			for (Iterator<Integer> iterator = notInLoopEdges
				.iterator(); iterator.hasNext();)
			    currLCC.addInNotLoopEdge(iterator.next());
			for (Iterator<Integer> iterator = lccBodyOutEdges
				.iterator(); iterator.hasNext();)
			    currLCC.addEdgeToLoopBody(iterator.next());
			Label loopControlerLabel = this.lccIdToLabelMap
				.get(currLCCId);
			this.lineNrToLoopControlerIdMap.put(this.labelTracker
				.getLineNrToLabel(loopControlerLabel),
				currLCCId);
		    }
		}
	    }

	}

	// METHOD VISITOR METHODS
	public void visitCode() {
	    // clear all data
	    this.lastVertexId = currMethHook.getInvokeBlock();
	    this.currBB = null;
	    this.currLabel = null;
	    this.currBlockInRow = true;
	    this.mySize = 0;
	}

	public void visitEnd() {
	    // set the size in my own invoke block and in all children's
	    // return blocks
	    InvokeBlock myInvBB = (InvokeBlock) cfg.findVertexByIndex(
		    currMethHook.getInvokeBlock()).getData();
	    myInvBB.setSize(this.mySize);
	    for (Iterator<ReturnBlock> i = this.childrensReturnBlocks
		    .iterator(); i.hasNext();) {
		i.next().setSize(this.mySize);
	    }
	    // set a loop count annotation value in loop controlers
	    // the loopcontrollers are sorted by the line number
	    this.identifyLoopControllers();
	    Iterator<Integer> loopControlersIterator = this.lineNrToLoopControlerIdMap
		    .values().iterator();
	    Iterator<Integer> annotationValuesIterator = this.annotationValues
		    .iterator();
	    while ((loopControlersIterator.hasNext())
		    && (annotationValuesIterator.hasNext())) {
		IVertex currLoopControler = cfg
			.findVertexByIndex(loopControlersIterator.next());
		currLoopControler.setLoopCount(annotationValuesIterator.next());
	    }
	    if (loopControlersIterator.hasNext()) {
		errorMessages += "Not correctly annotated method "
			+ currMethBlock.getOwner() + "->"
			+ currMethHook.getMethodBlock().name
			+ "- too few annotations.\n";
	    }
	    if (annotationValuesIterator.hasNext()) {
		errorMessages += "Not correctly annotated method "
			+ currMethBlock.getOwner() + "->"
			+ currMethHook.getMethodBlock().name
			+ "- too many annotations.\n";
	    }
	    this.annotationValues.clear();

	    this.cleanUpCatches();
	}

	@SuppressWarnings("unchecked")
	protected void cleanUpCatches() {
	    Collection<Label> catchList = this.catchHandleToStartMap.keySet();
	    for (Iterator<Label> iterator = catchList.iterator(); iterator
		    .hasNext();) {
		Label currCatchLabel = iterator.next();
		int catchVertexId = this.labelTracker.getOffer(currCatchLabel);
		IVertex catchVertex = cfg.findVertexByIndex(catchVertexId);
		catchVertex.setCatchHandler();
		if (catchVertexId != -1) {
		    HashSet<Integer> catchEdges = new HashSet<Integer>();

		    HashSet<Integer> visitedVertexes = new HashSet<Integer>();
		    Queue<Integer> vertexesToHandle = new LinkedList<Integer>();
		    vertexesToHandle.add(catchVertexId);
		    while (!vertexesToHandle.isEmpty()) {
			int currVertexId = vertexesToHandle.poll();
			visitedVertexes.add(currVertexId);
			IVertex currVertex = cfg
				.findVertexByIndex(currVertexId);
			if ((currVertex.isLoopControler())
				&& (catchEdges.containsAll(currVertex
					.getInNotLoopEdges()))
				|| ((!currVertex.isLoopControler()) && (catchEdges
					.containsAll(currVertex
						.getIncomingEdges())))) {
			    for (Iterator<Integer> iterator2 = currVertex
				    .getOutgoingEdges().iterator(); iterator2
				    .hasNext();) {
				int edgeId = iterator2.next();
				IEdge edge = cfg.findEdgeByIndex(edgeId);
				edge.setExceptionEdge();
				catchEdges.add(edgeId);
				if (!visitedVertexes.contains(edge
					.getToVertex()))
				    vertexesToHandle.add(edge.getToVertex());
			    }

			}
		    }
		    /*
                         * Label endLabel =
                         * this.catchHandleToEndMap.get(currCatchLabel); Label
                         * previousLabel =
                         * currMethBlock.getPreviousLabel(endLabel); int
                         * endVertexId = this.labelTracker
                         * .getOffer(previousLabel);
                         */
		    int endVertexId = this.labelTracker
			    .getOffer(this.catchHandleToEndMap
				    .get(currCatchLabel));
		    if (endVertexId != -1) {
			int catchEdgeId = cfg.addEdge(endVertexId,
				catchVertexId);
			IEdge catchEdge = cfg.findEdgeByIndex(catchEdgeId);
			catchEdge.setExceptionEdge();
		    }
		    int startVertexId = this.labelTracker
			    .getOffer(this.catchHandleToStartMap
				    .get(currCatchLabel));
		    if (startVertexId != -1) {
			int catchEdgeId = cfg.addEdge(startVertexId,
				catchVertexId);
			IEdge catchEdge = cfg.findEdgeByIndex(catchEdgeId);
			catchEdge.setExceptionEdge();
		    }
		}
	    }

	}

	public void visitJumpInsn(int oc, Label label) {
	    this.addInstruction(instructionGenerator.getJumpInsn(oc, label));
	    Label lastLabel = this.currLabel;
	    this.endBasicBlock();
	    if (oc == OpCodes.GOTO) {
		this.currBlockInRow = false;
	    }

	    int labelVertex = this.labelTracker.getOffer(label);
	    // back jump
	    if (labelVertex != -1) {
		cfg.addEdge(this.lastVertexId, labelVertex);
		// every loop controler is either the 'to' node or the 'from'
		// node of a back jump edge
		this.lccIdToLabelMap.put(labelVertex, label);
		this.lccIdToLabelMap.put(this.lastVertexId, lastLabel);
	    } else {// forward jump
		this.labelTracker.addDemand(this.lastVertexId, label);
	    }
	}

	public void visitLabel(Label label) {
	    if (currMethBlock.isJumpLabel(label)) {
		this.endBasicBlock();
		this.currLabel = label;
		this.labelTracker.addLabelToLineNrMapping(label,
			this.currLineNr);
	    }
	}

	public void visitMethodInsn(int oc, String owner, String name,
		String desc) {
	    this.addInstruction(instructionGenerator.getMethodInsn(oc, owner,
		    name, desc));
	    this.endBasicBlock();
	    MethodKey newKey = new MethodKey(owner, name, desc);
	    ReturnBlock retBB = new ReturnBlock(new MethodKey(currMethBlock
		    .getOwner(), currMethBlock.name, currMethBlock.desc));
	    this.childrensReturnBlocks.add(retBB);
	    int retBBId = cfg.addVertex(retBB);
	    InvokeBlock invBB;
	    int invBBId = -1;
	    MethodBlock newMethBlock;
	    if ((hierarchy == null)||(oc == OpCodes.INVOKESPECIAL)) {
		invBB = new InvokeBlock(newKey);
		invBBId = cfg.addVertex(invBB);
		newMethBlock = currMethBlock.getChild(newKey);
		invBB.setAnalyserInstruction(lastInstruction);
		cfg.addEdge(this.lastVertexId, invBBId);

		MethodHook newMethHook = new MethodHook(invBBId, retBBId,
			newMethBlock);
		methodHookQueue.add(newMethHook);
	    } else {
		for (Iterator<MethodKey> iterator = hierarchy
			.getAllMethodImpls(newKey).iterator(); iterator
			.hasNext();) {
		    MethodKey currKey = iterator.next();
		    invBB = new InvokeBlock(currKey);
		    invBBId = cfg.addVertex(invBB);
		    newMethBlock = currMethBlock.getChild(currKey);
		    invBB.setAnalyserInstruction(lastInstruction);
		    cfg.addEdge(this.lastVertexId, invBBId);

		    MethodHook newMethHook = new MethodHook(invBBId, retBBId,
			    newMethBlock);
		    methodHookQueue.add(newMethHook);
		}
	    }

	    // I need both mappings, since the loop detection algorithm
	    // traces edges in both directions
	    this.invBBIdToRetBBIdMap.put(invBBId, retBBId);
	    this.retBBIdToInvBBIdMap.put(retBBId, invBBId);

	    this.endBasicBlock();
	    this.lastVertexId = retBBId;
	    this.currBlockInRow = true;
	}

	public void visitTryCatchBlock(Label start, Label end, Label handle,
		String desc) {
	    // save the data to be used later
	    this.catchHandleToStartMap.put(handle, start);
	    this.catchHandleToEndMap.put(handle, end);
	}

	public void visitInsn(int oc) {
	    // RET not used
	    this.addInstruction(instructionGenerator.getInsn(oc));
	    if ((oc >= OpCodes.IRETURN) && (oc <= OpCodes.RETURN)) {
		// rnd the current basic block and connect it to
		// this methods return block
		this.endBasicBlock();
		this.currBlockInRow = false;
		cfg.addEdge(this.lastVertexId, currMethHook.getReturnBlock());
		ReturnBlock myReturnBlock = (ReturnBlock) cfg
			.findVertexByIndex(currMethHook.getReturnBlock())
			.getData();
		myReturnBlock.setAnalyserInstruction(lastInstruction);
	    }
	    // current athrow implementation just stops the engine
	    // an athrow will be handled as an end of a program
	    if (oc == OpCodes.ATHROW) {
		this.endBasicBlock();
		this.currBlockInRow = false;
	    }
	}

	public void visitLineNumber(int lineNr, Label label) {
	    this.currLineNr = lineNr;
	    this.labelTracker.addLabelToLineNrMapping(label, lineNr);
	}

	public void visitJOPInsn(int opCode) {
	    this.addInstruction(instructionGenerator.getJOPInsn(opCode));

	}

	// all instructions handled by these methods do not change the
	// control flow.
	// Any instruction is implemented either in
	// hardware or microcode (and knows its size and wcet value)
	// or a method call was inserted immediately before it (and knows only
	// its size,
	// the wcet value gets the analyser from the method
	public void visitIincInsn(int var, int increment) {
	    this.addInstruction(instructionGenerator
		    .getIincInsn(var, increment));
	}

	public void visitIntInsn(int oc, int operand) {
	    this.addInstruction(instructionGenerator.getIntInsn(oc, operand));
	}

	public void visitLdcInsn(Object cnst) {
	    this.addInstruction(instructionGenerator.getLdcInsn(cnst));
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	    this.addInstruction(instructionGenerator.getLookupSwitchInsn(dflt,
		    keys, labels));
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
	    this.addInstruction(instructionGenerator.getMultiANewArrayInsn(
		    desc, dims));
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt,
		Label[] labels) {
	    this.addInstruction(instructionGenerator.getTableSwitchInsn(min,
		    max, dflt, labels));
	}

	public void visitTypeInsn(int oc, String desc) {
	    this.addInstruction(instructionGenerator.getTypeInsn(oc, desc));
	}

	public void visitVarInsn(int oc, int var) {
	    this.addInstruction(instructionGenerator.getVarInsn(oc, var));
	}

	public void visitFieldInsn(int oc, String owner, String name,
		String desc) {
	    this.addInstruction(instructionGenerator.getFieldInsn(oc, owner,
		    name, desc));
	}

	// empty methods of the MethodVisitorInterface
	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
		Object[] arg4) {
	    // QUESTION according to the ASM documentation, this could be
	    // used instead of
	    // labels to track control flow changes, but in the class files
	    // I saw the
	    // version was never high enough.
	    // useful, stack state known
	    System.out.println("I am here");// it is not called
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean arg1) {
	    if (Type.getType(desc).getClassName()
		    .endsWith("AnalyserAnnotation")) {
		return new WCETAnnotationVisitor();
	    } else {
		return null;
	    }
	}

	public AnnotationVisitor visitAnnotationDefault() {
	    return null;
	}

	public void visitAttribute(Attribute arg0) {
	}

	public AnnotationVisitor visitParameterAnnotation(int arg0,
		String arg1, boolean arg2) {
	    return null;
	}

	public void visitMaxs(int arg0, int arg1) {
	}

	public void visitLocalVariable(String arg0, String arg1, String arg2,
		Label arg3, Label arg4, int arg5) {

	}

	class WCETAnnotationVisitor implements AnnotationVisitor {
	    boolean visitNext = false;

	    public void visit(String name, Object value) {
		if (name.equals("type")) {
		    this.visitNext = true;
		} else if ((this.visitNext) && (name.equals("value"))) {
		    int[] loopCounts = (int[]) value;
		    for (int i = 0; i < loopCounts.length; i++) {
			annotationValues.add(loopCounts[i]);
		    }
		}

	    }

	    public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
		// empty
		return null;
	    }

	    public AnnotationVisitor visitArray(String arg0) {
		// empty
		return this;
	    }

	    public void visitEnd() {
		// empty

	    }

	    public void visitEnum(String arg0, String arg1, String arg2) {
		// empty
	    }

	}
    }

    // empty method visitor methods
    public void visit(int arg0, int arg1, String arg2, String arg3,
	    String arg4, String[] arg5) {
    }

    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	return null;
    }

    public void visitAttribute(Attribute arg0) {
    }

    public void visitEnd() {
    }

    public FieldVisitor visitField(int arg0, String arg1, String arg2,
	    String arg3, Object arg4) {
	return null;
    }

    public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
    }

    public void visitOuterClass(String arg0, String arg1, String arg2) {
    }

    public void visitSource(String arg0, String arg1) {
    }
}
