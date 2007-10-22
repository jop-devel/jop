package wcet.components.graphbuilder.instruction;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import wcet.components.graphbuilder.IGraphBuilderConstants;
import wcet.components.graphbuilder.instruction.exectime.FieldTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.IincTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.InsnTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.IntTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.JOPTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.JumpTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.LdcTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.LookupSwitchTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.MethodTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.MultiANewArrayTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.TableSwitchTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.TypeTimeAnalysisInsn;
import wcet.components.graphbuilder.instruction.exectime.VarTimeAnalysisInsn;
import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.instruction.IAnalysisInstruction;
import wcet.framework.interfaces.instruction.IInstructionGenerator;
import wcet.framework.interfaces.instruction.OpCodes;
//QUESTION should I make the get...Insn methods static
//+:no init and call necessary, it does not do anything anyway
//-:no way how to change the generator without changing GraphWriter
/**
 * @author Elena Axamitova
 * @version 0.1 04.05.2007
 * 
 * Provides AnalysisInstructions objects. Privides a decoupling level
 * between GraphWriter and current jop instruction set.
 */
public class TimeInstructionGenerator implements IInstructionGenerator, IAnalyserComponent{

    /**
     * Shared datA store
     */
    private IDataStore dataStore;
    
    /**
     * Construct new generator anf store it in the data store.
     * @param ds - data store
     */
    public TimeInstructionGenerator(IDataStore ds){
	this.dataStore = ds;
	this.dataStore.storeObject(IGraphBuilderConstants.INSTRUCTION_GENERATOR_KEY, this);
    }
    
    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public IAnalysisInstruction getFieldInsn(int opcode, String owner, String name, String desc) {
	return new FieldTimeAnalysisInsn(opcode);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getIincInsn(int, int)
     */
    public IAnalysisInstruction getIincInsn(int var, int inc) {
	return new IincTimeAnalysisInsn(OpCodes.IINC);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getInsn(int)
     */
    public IAnalysisInstruction getInsn(int opcode) {
	return new InsnTimeAnalysisInsn(opcode);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getIntInsn(int, int)
     */
    public IAnalysisInstruction getIntInsn(int opcode, int operand) {
	return new IntTimeAnalysisInsn(opcode, operand);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getJumpInsn(int, org.objectweb.asm.Label)
     */
    public IAnalysisInstruction getJumpInsn(int opcode, Label label) {
	return new JumpTimeAnalysisInsn(opcode);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getLdcInsn(java.lang.Object)
     */
    public IAnalysisInstruction getLdcInsn(Object cnst) {
	 // TODO change to different lds's, somehow ...
	    // the opcode field in the LdcInsnNode does not help much

	    // ldc and ldc_w are not so much different in length and wcet
	    // value, but ... well ldc_w is the worst case
	    if ((cnst instanceof Integer) || (cnst instanceof Float)
		    || (cnst instanceof String) || (cnst instanceof Type)) {
		return new LdcTimeAnalysisInsn(OpCodes.LDC_W);
	    } else {
		return new LdcTimeAnalysisInsn(OpCodes.LDC2_W);
	    }
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getLookupSwitchInsn(org.objectweb.asm.Label, int[], org.objectweb.asm.Label[])
     */
    public IAnalysisInstruction getLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	return new LookupSwitchTimeAnalysisInsn(OpCodes.LOOKUPSWITCH, labels.length * 8 + 12);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public IAnalysisInstruction getMethodInsn(int opcode, String owner, String name, String desc) {
	return new MethodTimeAnalysisInsn(opcode);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getMultiANewArrayInsn(java.lang.String, int)
     */
    public IAnalysisInstruction getMultiANewArrayInsn(String desc, int dims) {
	return new MultiANewArrayTimeAnalysisInsn(OpCodes.MULTIANEWARRAY);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getTableSwitchInsn(int, int, org.objectweb.asm.Label, org.objectweb.asm.Label[])
     */
    public IAnalysisInstruction getTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
	return new TableSwitchTimeAnalysisInsn(OpCodes.TABLESWITCH, labels.length * 4 + 16);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getTypeInsn(int, java.lang.String)
     */
    public IAnalysisInstruction getTypeInsn(int opcode, String desc) {
	return new TypeTimeAnalysisInsn(opcode);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getVarInsn(int, int)
     */
    public IAnalysisInstruction getVarInsn(int opcode, int var) {
	return new VarTimeAnalysisInsn(opcode);
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
     */
    public boolean getOnlyOne() {
	return false;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
     */
    public int getOrder() {
	return IGlobalComponentOrder.NOT_EXECUTED;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
     */
    public void init() throws InitException {
	//no init needed
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    public String call() throws Exception {
	//not executed
	return null;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.instruction.IInstructionGenerator#getJOPInsn(int)
     */
    public IAnalysisInstruction getJOPInsn(int opcode) {
	return new JOPTimeAnalysisInsn(opcode);
    }

    

}
