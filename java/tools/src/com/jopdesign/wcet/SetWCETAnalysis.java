package com.jopdesign.wcet;
import org.apache.bcel.classfile.*;

/*
 * It calls the methods. 
 * @author rup, ms
 */
public class SetWCETAnalysis extends MyVisitor {
  WCETAnalyser wca;
	public SetWCETAnalysis(WCETAnalyser wca) {
		super(wca);
		this.wca = wca; 
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);

		Method[] methods = clazz.getMethods();
		
		for(int i=0; i < methods.length; i++) {
			Method m = methods[i];
			String methodId = m.getName()+m.getSignature();
      
      //if(m.getName().equalsIgnoreCase("loop10")){
      if(!m.isAbstract()){
        WCETMethodBlock wcmb = new WCETMethodBlock(m,clazz,wca);
        wcmb.controlFlowGraph();
        wcmb.directedGraph();
        wca.out.println(wcmb.toString());
        wca.dotout.print("dot -Tps "+wcmb.dotf+" > "+wcmb.dotf.substring(0,wcmb.dotf.length()-4)+".eps\n");
      }
        //System.out.println(wcmb.toString());
      //}
    
      // Analyze the method
	    //WCETAnalyser.controlFlowGraph(mi);
		}
	}
}
