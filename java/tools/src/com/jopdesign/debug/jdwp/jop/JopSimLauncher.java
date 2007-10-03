/*******************************************************************************

    An implementation of the Java Debug Wire Protocol (JDWP) for JOP
    Copyright (C) 2007 Paulo Abadie Guedes

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
    
*******************************************************************************/

package com.jopdesign.debug.jdwp.jop;

import com.jopdesign.tools.JopSim;

/**
 * JopSimLauncher.java
 * 
 * 
 * @author Paulo Abadie Guedes
 *
 * 01/06/2007 - 17:58:24
 * 
 */
public class JopSimLauncher implements JopLauncher
{
  public JopSimLauncher()
  {
    
  }

  /* (non-Javadoc)
   * @see com.jopdesign.debug.jdwp.jop.JopLauncher#launchJop(java.lang.String[])
   */
  public void launchJop(String[] args)
  {
    JopSim.main(args);
  }
}
