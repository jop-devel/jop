/**
 * 
 */
package wcet.components.graphbuilder.basicblockgb;

// import java.io.File;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.2 25.05.2007
 * 
 * Not ready.
 */
//TODO class hierarchy needed
public class ExceptionGraphWriter extends GraphWriter {

    private String errorMessages = "";

    public ExceptionGraphWriter(IDataStore ds) {
	super(ds);
    }
    
    public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
	    String arg3, String[] arg4) {
	return new ExceptionGraphWriterVisitor();
    }

    protected class ExceptionGraphWriterVisitor extends GraphWriter.GraphWriterVisitor {	
	protected Analyzer asmAnalyser;
	
	protected Frame[] currFrames;

	protected int lastInsnIdx;

	protected ExceptionGraphWriterVisitor() {
	    this.asmAnalyser = new Analyzer(new BasicInterpreter());
	}
	@Override
	protected void addInstruction(IAnalysisInstruction insn){
	    super.addInstruction(insn);
	    this.lastInsnIdx++;
	}
	
	public void visitEnd() {
	   super.visitEnd();
	   //hmmm
	}

	public void visitLabel(Label label) {
	    super.visitLabel(label);
	    //label is not an instruction, but it is on the stack, so
	    this.lastInsnIdx++;
	}

	public void visitMethodInsn(int oc, String owner, String name,
		String desc) {
	    super.visitMethodInsn(oc, owner, name, desc);
	    //get exeptions that can be thrown, connect them to
	    //corresponding EsceptionReturnBlocks, plus one for 
	    //RuntimeEsceptions
	}

	public void visitTryCatchBlock(Label start, Label end, Label handle,
		String desc) {
	    // TODO well now, that will be interesting ...
	    // athrow is in visitInsn()
	}
	
	@Override
	protected void cleanUpCatches(){
	    
	}
	
	public void visitInsn(int oc) {
	    super.visitInsn(oc);
	    // TODO handle athrow
	    // TODO current athrow implementation just stops the engine
	    // a clean up of the resulting graph is enough (delete catch
	    // parts)
	    if (oc == OpCodes.ATHROW) {
		this.endBasicBlock();
		this.currBlockInRow = false;
		if(this.currFrames==null){
		    try {
			this.asmAnalyser.analyze(currMethBlock.getOwner(), currMethBlock);
		    } catch (AnalyzerException e) {
			errorMessages += "GraphWrtiter: Unable to run the analyzer on the method: name "+
			currMethBlock.name +currMethBlock.desc +" of " +currMethBlock.getOwner();
		    }
		}
		if(this.currFrames != null){
		    BasicValue athrowValue = (BasicValue)this.currFrames[this.lastInsnIdx].getStack(0);
		    String exceptionName = athrowValue.getType().getInternalName();
		    //TODO connect either to a try-catch (if one applies) or to an ExceptionReturnBlock
		    System.err.println(exceptionName);
		}
		
	    }
	}
    }
}
