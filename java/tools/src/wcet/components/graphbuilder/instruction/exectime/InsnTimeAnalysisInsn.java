/**
 * 
 */
package wcet.components.graphbuilder.instruction.exectime;

import com.jopdesign.wcet.WCETInstruction;

import wcet.framework.instruction.cache.InsnAnalysisInstruction;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
/*
 * NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3,
 * ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2,
 * DCONST_0, DCONST_1, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD,
 * SALOAD, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE,
 * SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP, IADD,
 * LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
 * FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR,
 * LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, I2L, I2F, I2D, L2I,
 * L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C, I2S, LCMP, FCMPL, FCMPG,
 * DCMPL, DCMPG, IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
 * ARRAYLENGTH, ATHROW, MONITORENTER, or MONITOREXIT
 */
public class InsnTimeAnalysisInsn extends InsnAnalysisInstruction implements
	ITimeAnalysisInstruction {

    public InsnTimeAnalysisInsn(int opc) {
	super(opc);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.graphbuilder.instuctions.exectime.ITimeAnalysisInstruction#getCycles()
         */
    public int getCycles() {
	int retVal = WCETInstruction.getCycles(this.opcode, false, 0);
	int b = WCETInstruction.calculateB(false, 0);
	switch (this.opcode) {
	/*case OpCodes.NOP:
	    return 1;
	case OpCodes.ACONST_NULL:
	    return 1;
	case OpCodes.ICONST_M1:
	    return 1;
	case OpCodes.ICONST_0:
	    return 1;
	case OpCodes.ICONST_1:
	    return 1;
	case OpCodes.ICONST_2:
	    return 1;
	case OpCodes.ICONST_3:
	    return 1;
	case OpCodes.ICONST_4:
	    return 1;
	case OpCodes.ICONST_5:
	    return 1;
	case OpCodes.LCONST_0:
	    return 2;
	case OpCodes.LCONST_1:
	    return 2;
	case OpCodes.FCONST_0:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FCONST_1:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FCONST_2:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.DCONST_0:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.DCONST_1:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.IALOAD:
	    return 32+3*ITimeAnalysisInstruction.rws;
	case OpCodes.LALOAD:
	    return 43+4*ITimeAnalysisInstruction.rws;
	case OpCodes.FALOAD:
	    return 32+3*ITimeAnalysisInstruction.rws;
	case OpCodes.DALOAD:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.AALOAD:
	    return 32+3*ITimeAnalysisInstruction.rws;
	case OpCodes.BALOAD:
	    return 32+3*ITimeAnalysisInstruction.rws;
	case OpCodes.CALOAD:
	    return 32+3*ITimeAnalysisInstruction.rws;
	case OpCodes.SALOAD:
	    return 32+3*ITimeAnalysisInstruction.rws;
	case OpCodes.IASTORE:
	    return 35+2*ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
	case OpCodes.LASTORE:
	    retVal= 48+2*ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
	    if(ITimeAnalysisInstruction.wws>3)
		retVal +=ITimeAnalysisInstruction.wws-3;
	    return retVal;
	case OpCodes.FASTORE:
	    return 35+2*ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
	case OpCodes.DASTORE:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.AASTORE:
	    return 35+2*ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
	case OpCodes.BASTORE:
	    return 35+2*ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
	case OpCodes.CASTORE:
	    return 35+2*ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
	case OpCodes.SASTORE:
	    return 35+2*ITimeAnalysisInstruction.rws+ITimeAnalysisInstruction.wws;
	case OpCodes.POP:
	    return 1;
	case OpCodes.POP2:
	    return 2;
	case OpCodes.DUP:
	    return 1;
	case OpCodes.DUP_X1:
	    return 5;
	case OpCodes.DUP_X2:
	    return 7;
	case OpCodes.DUP2:
	    return 6;
	case OpCodes.DUP2_X1:
	    return 8;
	case OpCodes.DUP2_X2:
	    return 10;
	case OpCodes.SWAP:
	    return 4;
	case OpCodes.IADD:
	    return 1;
	case OpCodes.LADD:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FADD:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.DADD:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.ISUB:
	    return 1;
	case OpCodes.LSUB:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FSUB:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.DSUB:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.IMUL:
	    return 35;
	case OpCodes.LMUL:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FMUL:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.DMUL:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.IDIV:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.LDIV:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FDIV:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.DDIV:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.IREM:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.LREM:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FREM:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.DREM:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.INEG:
	    return 4;
	case OpCodes.LNEG:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FNEG:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.DNEG:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.ISHL:
	    return 1;
	case OpCodes.LSHL:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.ISHR:
	    return 1;
	case OpCodes.LSHR:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.IUSHR:
	    return 1;
	case OpCodes.LUSHR:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.IAND:
	    return 1;
	case OpCodes.LAND:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.IOR:
	    return 1;
	case OpCodes.LOR:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.IXOR:
	    return 1;
	case OpCodes.LXOR:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.I2L:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.I2F:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.I2D:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.L2I:
	    return 3;
	case OpCodes.L2F:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.L2D:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.F2I:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.F2L:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.F2D:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.D2I:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.D2L:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.D2F:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.I2B:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.I2C:
	    return 2;
	case OpCodes.I2S:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.LCMP:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.FCMPL:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.DCMPL:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	case OpCodes.DCMPG:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;*/
	case OpCodes.IRETURN:
	    /*retVal =23;
	    if(ITimeAnalysisInstruction.rws>3){
		retVal += ITimeAnalysisInstruction.rws-3;
	    }
	    return retVal;*/
	case OpCodes.LRETURN:
	    /*retVal =25;
	    if(ITimeAnalysisInstruction.rws>3){
		retVal += ITimeAnalysisInstruction.rws-3;
	    }
	    return retVal;*/
	case OpCodes.FRETURN:
	    /*retVal =23;
	    if(ITimeAnalysisInstruction.rws>3){
		retVal += ITimeAnalysisInstruction.rws-3;
	    }
	    return retVal;*/
	case OpCodes.DRETURN:
	    /*retVal =25;
	    if(ITimeAnalysisInstruction.rws>3){
		retVal += ITimeAnalysisInstruction.rws-3;
	    }
	    return retVal;*/
	case OpCodes.ARETURN:
	    /*retVal =23;
	    if(ITimeAnalysisInstruction.rws>3){
		retVal += ITimeAnalysisInstruction.rws-3;
	    }
	    return retVal;*/
	case OpCodes.RETURN:
	    /*retVal =21;
	    if(ITimeAnalysisInstruction.rws>3){
		retVal += ITimeAnalysisInstruction.rws-3;
	    }*/
	    if(b>this.getHiddenCycles())
		retVal -= b - this.getHiddenCycles();
	    break;
	/*case OpCodes.ARRAYLENGTH:
	    return 6+ITimeAnalysisInstruction.rws;
	case OpCodes.ATHROW:
	    return ITimeAnalysisInstruction.CYCLES_OF_LAST_METHOD;
	case OpCodes.MONITORENTER:
	    return 11;
	case OpCodes.MONITOREXIT:
	    return 16;//TODO ??? 10/16
	default:
	    return ITimeAnalysisInstruction.CYCLES_UNKNOWN;*/
	}
	return retVal;
    }
    
    public int getHiddenCycles(){
	switch(this.opcode){
	case OpCodes.IRETURN:
	    return 10;
	case OpCodes.LRETURN:
	    return 11;
	case OpCodes.FRETURN:
	    return 10;
	case OpCodes.DRETURN:
	    return 11;
	case OpCodes.ARETURN:
	    return 10;
	case OpCodes.RETURN:
	    return 9;
	 default:
	     return ITimeAnalysisInstruction.CYCLES_UNKNOWN;
	}
    }
}
