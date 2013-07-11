/**
 * @author Frank Zeyda
 */
package scjlibs.examples.hijac.cdx;

import scjlibs.examples.hijac.cdx.CDxMission;
import scjlibs.examples.hijac.cdx.CDxSafelet;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Safelet;

/**
 * Entry point of the SCJ application.
 */
public class Main {
  public static void main(String[] args) {
	  
		Safelet<CDxMission> safelet = new CDxSafelet();
		JopSystem.startMission(safelet);
	  
	  
//    SafeletExecuter.run(new CDxSafelet());
  }
}
