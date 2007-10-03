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

package com.jopdesign.debug.jdwp.util;

import java.util.Hashtable;

/**
 * ArgumentParser.java
 *
 * A simple class to get a list of arguments, parse them and return
 * the corresponding values. Each string should have a format 
 * <prefix><value>, so the program can identify the values. 
 * 
 * The last recognized value for each parameter always override the 
 * previous ones. This is useful to handle repeated values, as well as
 * to set default values before parsing the input. 
 * 
 * Usage:
 * 1) Create a list of the possible strings which may be used as prefix.
 * 2) (optional) Call parseArguments with default string values 
 * 3) Call parseArguments with the given list of String objects (override defaults)
 * 4) Call getValue() or getValueAsInt() to query the values
 * 
 * @author Paulo Guedes
 * 29/05/2007 - 18:02:17
 * 
 */
public class ArgumentParser
{
  private Hashtable table;
  private StringList parameterList;
  
  public ArgumentParser(StringList expectedArguments)
  {
    this.parameterList = expectedArguments;
    clear();
  }
  
  public void clear()
  {
    table = new Hashtable();
  }
  
  /**
   * Parse a String array to capture the values of each known argument.
   *  
   * Can be used both to set the default values and to get values from 
   * standard input. If a value is set more than once, only the last 
   * value will be considered.
   * 
   * To set default values, call this method with the default strings
   * before parsing the program input.
   * 
   * @param args
   * @throws IllegalArgumentException
   */
  public void parseArguments(String[] args) throws IllegalArgumentException
  {
    int i;
    
    for(i = 0; i < args.length; i++)
    {
      String parameter = args[i];
      parse(parameter);
    }
  }
  
  /**
   * Check if a given parameter was defined in the args array.
   * 
   * @param args
   * @param parameter
   * @return
   */
  public boolean isDefined(String[] args, String parameter)
  {
    int index, size;
    String possibleParameter;
    boolean result = false;
    
    if(parameter != null)
    {
      parameter = parameter.trim();
      size = args.length;
      for (index = 0; index < size; index++)
      {
        possibleParameter = args[index];
        if(possibleParameter.startsWith(parameter))
        {
          // matched: was defined.
          result = true;
          break;
        }
      }
    }
    
    return result;
  }
  
  /**
   * Search the list of possible prefixes to see if there is a match.
   * If there is, link the prefix to its value and return.
   * If the value is empty or null, just ignore.
   * 
   * @param parameter
   */
  public void parse(String parameter) throws IllegalArgumentException
  {
    int index, size, length;
    String possiblePrefix, value;
//    boolean matched = false;
    
    if(parameter != null)
    {
      parameter = parameter.trim();
      size = parameterList.size();
      for (index = 0; index < size; index++)
      {
        possiblePrefix = parameterList.get(index);
        if(parameter.startsWith(possiblePrefix))
        {
//          matched = true;
          
          length = possiblePrefix.length();
          value = parameter.substring(length);
          value = value.trim();
          
          if("".equals(value) == false)
          {
            table.put(possiblePrefix, value);
          }
          break;
        }
      }
    }
//    if(matched == false)
//    {
//      throw new IllegalArgumentException(" Illegal argument! " + parameter);
//    }
  }
  
  /**
   * Return the corresponding value associated with the given parameter.
   * 
   * If the value was not defined, return the empty String("").
   * 
   * @param parameter
   * @return
   */
  public String getValue(String parameter)
  {
    Object data = table.get(parameter);
    String value = null;
    
    if(data != null)
    {
      value = data.toString();
    }
    else
    {
      value = "";
    }
    
    return value;
  }
  
  /**
   * Return the corresponding value associated with the given parameter,
   * after converting it to an integer.
   * 
   * If the parameter cannot be parsed
   * into an integer value, throw a NumberFormatException object.
   * 
   * @param parameter
   * @return
   * @throws NumberFormatException
   */
  public int getValueAsInt(String parameter) throws NumberFormatException
  {
    String data = getValue(parameter);
    int value = Integer.parseInt(data);
    
    return value;
  }
}
