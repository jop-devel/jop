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

package com.jopdesign.debug.jdwp.bcel;

import java.io.File;

import org.apache.bcel.util.Class2HTML;

import com.jopdesign.debug.jdwp.util.StringList;
/**
 * Class2HTMLHelper.java
 * 
 * An utility class to call Class2HTML (a tool from BCEL)
 * for all class files in the current folder.
 * 
 * Probably it would be better to use Ant to do this, but this class is a
 * quick (a.k.a. "good enough") solution. Ant is not helping me today;)
 * 
 * @author Paulo Abadie Guedes
 *
 * 11/07/2007 - 15:27:45
 * 
 */
public class Class2HTMLHelper
{
  
  public static void main(String args[])
  {
    int index = 0;
    int i, size;
    StringList toolParameters = new StringList();
    String[] parameters = new String[3];
    String rootFolder;
    String fileName;
    FileList list;
    
    if(args.length != 1 && args.length != 3)
    {
      System.out.println();
      System.out.println("  This class just call Class2HTML from BCEL to show class information");
      System.out.println();
      System.out.println("  Possible usage:");
      System.out.println();
      System.out.println("  <root folder>");
      System.out.println("  -d <output folder> <root folder>");
      System.out.println();
      System.exit(-1);
    }
    else
    {
      
      if(args.length == 3)
      {
        toolParameters.add(args[index]);
        index++;
        
        toolParameters.add(args[index]);
        index++;
      }
      
      rootFolder = args[index];
      list = FileList.createClassFileList(rootFolder);
      size = list.size();
      
      for(i = 0; i < size; i++)
      {
        File file = list.get(i);
//        fileName = file.getName();
        fileName = file.toString();
        toolParameters.add(fileName);
      }
      
//      System.out.println(toolParameters);
      parameters = toolParameters.toStringArray();
      
      Class2HTML.main(parameters);
    }
  }
}
