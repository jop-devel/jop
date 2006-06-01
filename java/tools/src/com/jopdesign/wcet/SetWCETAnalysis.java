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
      
      //if(m.getName().equalsIgnoreCase("main")||m.getName().equalsIgnoreCase("loop11")){
      if(!m.isAbstract()){
        WCETMethodBlock wcmb = new WCETMethodBlock(m,clazz,wca);
        wca.msigtowcmb.put(methodId,wcmb);
//System.out.println("put "+methodId+" in msigtiwcmb");        
        wcmb.controlFlowGraph();
        wcmb.directedGraph();
        wca.out.println(wcmb.toString());
        wca.dotout.print("\tdot -Tps "+wcmb.dotf+" > "+wcmb.dotf.substring(0,wcmb.dotf.length()-4)+".eps\n");
      }
      //}
		}
    for(int i=0; i < methods.length; i++) {
      Method m = methods[i];
      String methodId = m.getName()+m.getSignature();
      if(m.getName().equalsIgnoreCase("main")){
      if(!m.isAbstract()){

        WCETMethodBlock wcmb = (WCETMethodBlock)wca.mtowcmb.get(m);
        wca.out.println("*** WCET FOR APPLICATION***");
        wca.out.println(wcmb.toLS(true,true,"")+"\n");
        System.out.println("HI");                
        
      }
      }
    }

	}
}
