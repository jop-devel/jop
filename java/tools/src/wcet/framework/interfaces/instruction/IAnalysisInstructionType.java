/**
 * 
 */
package wcet.framework.interfaces.instruction;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
public interface IAnalysisInstructionType {

	public final static int INSN = 0;

	public final static int INT_INSN = 1;

	public final static int VAR_INSN = 2;

	public final static int TYPE_INSN = 3;

	public final static int FIELD_INSN = 4;

	public final static int METHOD_INSN = 5;

	public final static int JUMP_INSN = 6;

	public final static int LABEL = 7;

	public final static int LDC_INSN = 8;

	public final static int IINC_INSN = 9;

	public final static int TABLESWITCH_INSN = 10;

	public final static int LOOKUPSWITCH_INSN = 11;

	public final static int MULTIANEWARRAY_INSN = 12;

	public final static int FRAME = 13;

	public final static int LINE = 14;
	
	public final static int JOP_INSN = 30;
}
