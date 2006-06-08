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
		if(wca.init){
  		for(int i=0; i < methods.length; i++) {
  			Method m = methods[i];
  			String methodId = m.getName()+m.getSignature();
        

        if(!m.isAbstract()){
          WCETMethodBlock wcmb = new WCETMethodBlock(m,clazz,wca);
          wca.msigtowcmb.put(methodId,wcmb);
          wca.wcmbs.add(wcmb);
  //System.out.println("put "+methodId+" in msigtiwcmb");        
          wcmb.controlFlowGraph();
          wcmb.directedGraph();
          //wcmb.toString();
System.out.println("comparing:"+(clazz.getClassName()+"."+m.getName())+" to:"+wca.appmethod);          
          if((clazz.getClassName()+"."+m.getName()).equalsIgnoreCase(wca.appmethod)){
            wca.wcmbapp = wcmb;
          }
        }
        else
          System.out.println("not putting"+m.getName());
        //}
  		}
    }
//    for(int i=0; i < methods.length; i++) {
//      Method m = methods[i];
//      String methodId = m.getName()+m.getSignature();
//      if(m.getName().equalsIgnoreCase("main")){
//        if(!m.isAbstract()){
//          WCETMethodBlock wcmb = (WCETMethodBlock)wca.mtowcmb.get(m);
//          String lss = wcmb.toLS(true,true,"");
//          wca.out.println("*** WCET FOR APPLICATION***");
//          wca.out.println("WCET = " + wcmb.wcetlp);
//          wca.out.println(lss+"\n");
//        }
//      }
//    }
    if(wca.analyze){

      for(int i=0; i < methods.length; i++) {
        Method m = methods[i];
        String methodId = m.getName()+m.getSignature();
        
        //if(m.getName().equalsIgnoreCase("main")||m.getName().equalsIgnoreCase("loop11")){
        if(!m.isAbstract()){
          WCETMethodBlock wcmb = (WCETMethodBlock)wca.mtowcmb.get(m);
          //wca.out.println(wcmb.toString());
          wca.wcasb.append(wcmb.toString());
          wcmb.link();
          wca.dotout.print("\tdot -Tps "+wcmb.dotf+" > "+wcmb.dotf.substring(0,wcmb.dotf.length()-4)+".eps\n");
        }
        //}
      }
    }
    

	}
}
