/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen
  Copyright (C) 2006, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.wcet;
import org.apache.bcel.classfile.*;

import com.jopdesign.build.AppVisitor;

/*
 * It calls the methods.
 * @author rup, ms
 */
public class SetWCETAnalysis extends AppVisitor {
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


        if(true){//!m.isAbstract()){
          WCETMethodBlock wcmb = new WCETMethodBlock(cli.getMethodInfo(methodId),wca);
          wca.msigtowcmb.put(methodId,wcmb);
          wca.wcmbs.add(wcmb);
//  System.out.println("put "+clazz.getClassName()+"."+methodId+" in msigtiwcmb");
//          wcmb.controlFlowGraph();
//          wcmb.directedGraph();
          //wcmb.toString();
//System.out.println("comparing:"+(clazz.getClassName()+"."+m.getName())+" to:"+wca.appmethod);
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
/*    if(wca.analyze){

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
    }*/


	}
}
