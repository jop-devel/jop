/**
 * 
 */
package wcet.components.graphbuilder.basicblockgb;

import wcet.components.graphbuilder.blocks.MethodBlock;
import wcet.components.graphbuilder.methodgb.MethodKey;
import wcet.components.graphbuilder.util.FileList;
import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.interfaces.instruction.IImplementationConfig;
import wcet.framework.interfaces.instruction.OpCodes;

/**
 * @author Elena Axamitova
 * @version 0.1 05.04.2007
 * 
 * MethodMap of jop system classes.
 */
public class JOPSystemMethodMap{
    private FileList systemMethodsList;
    
    protected JOPSystemMethodMap(String systemPath) throws InitException{
	this.systemMethodsList = new FileList(systemPath, ".class");
	this.systemMethodsList.findAllFiles();
    }
    
    protected MethodBlock getJOPMethodBlock(MethodKey key) throws TaskInitException{
	MethodBlock result = MethodBlock.getRootBlock(key, this.systemMethodsList);
	//TODO bad - infinite loop - wr(String) in JVMHelp - WORKAROUND
	if(!((key.getOwner().endsWith("JVMHelp")&&(key.getName().equals("wr"))&&(key.getDecription().equals("(Ljava/lang/String;)V")))))
	    result.resolve();
	return result;
    }
    
 public static void main(String[] args){
	for(int i = 0; i<IImplementationConfig.JOP_INSN_IMPL_DEFAULT.length; i++){
	    System.out.println("OpCode: "+ i+ " name:" + OpCodes.OPCODE_NAMES[i]+" desc:"+IImplementationConfig.JOP_METHOD_IMPL_DESCR[i]+".");
	}
    }
    
}
