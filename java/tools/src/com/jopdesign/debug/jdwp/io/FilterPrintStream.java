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

package com.jopdesign.debug.jdwp.io;

import java.io.PrintStream;

import com.jopdesign.debug.jdwp.jop.CharQueue;
import com.jopdesign.debug.jdwp.util.Util;

/**
 * FilterPrintStream.java
 * 
 * A stream which can filter content to redirect it for one of
 * two streams.
 * 
 * This PrintStream monitor the stream of characters flowing and
 * decide dynamically if it should print the content to the first
 * of the second stream, based on a very simple protocol.
 * 
 * When a token is recognized, the object read a text length
 * immediately after it. If the length is properly read, 
 * the object switch and redirect the next 'length' bytes
 * to the second stream. After that, it automatically
 * switch back to the first stream and repeat the process.
 * If there is an error on the length field, it is ignored.
 * 
 * Either way, the token and the length are removed from the stream.
 * 
 * This class is useful to simulate two different text streams over
 * one single stream.
 * 
 * Potential applications may include logging, debugging
 * and even simulation of an independent binary stream 
 * over the same channel.   
 * 
 * Assumptions:
 * 
 * - The prefix should never appear by accident in the text.
 * If this happens, It will be wrongly taken as part of a header
 * and (if there is no valid length after it) the entire
 * header will be removed from the stream.
 * 
 * - The communication channel is reliable: no byte will be lost.
 * 
 * @author Paulo Guedes
 *
 * 31/05/2007 - 10:48:09
 * 
 */
public class FilterPrintStream extends PrintStream
{
  // IMPROVE: refactor to create a FilterOutputStream
  //the correct way to implement this class should be 
  // using another stream with the filter behaviour. 
  // That would be a FilterOutputStream and the 
  // "write(byte b)" method should be overwritter.
  // Unfortunately I only realized this after 
  // everything was already working with this class;)...
  
  private PrintStream firstStream;
  private PrintStream secondStream;
  
  public static final String DEFAULT_TOKEN = "JDWP_DATA_PREFIX:";
  
  // the header token. Fixed by now.
  private String token;
  private char[] TOKEN_CHARS;
  
  // the size of the "length" field.
  public static final int SIZE_OF_LENGTH_FIELD = 4;
  
  private int bytesToPrint;
  
  private boolean useFirstStream = true;
  private CharQueue queue;
  
  /**
   * The default constructor.
   * 
   * @param first
   * @param second
   */
  public FilterPrintStream(PrintStream first, PrintStream second)
  {
    this(first, second, DEFAULT_TOKEN);
  }
  
  public FilterPrintStream(PrintStream first, PrintStream second, String ID)
  {
    super(first);
    
    int bufferSize;
    
    this.token = ID;
    TOKEN_CHARS = token.toCharArray();
    bufferSize = token.length() + SIZE_OF_LENGTH_FIELD;
    
    bytesToPrint = 0;
    useFirstStream = true;
    
    firstStream = first;
    secondStream = second;
    
    queue = new CharQueue(bufferSize);
  }
  
  /**
   * This method works as the regular print, sending bytes to the
   * first stream as default.
   * 
   * However, if the predefined token appears somewhere in the
   * char stream it will try to read the length after it.
   * After that, the next 'length' bytes will be redirected 
   * to the second stream until the last one is printed.
   * Then it will switch back to the first and will start 
   * monitoring again.
   *  
   * If there is a failure reading the length, print an
   * error, remove the wrong command from the stream
   * and continue with the first stream.
   * 
   * The expected format is:
   * <TOKEN><LENGTH>.
   */
  public void print(char c)
  {
    if(useFirstStream)
    {
      char next;
//      boolean isFull = queue.isFull();
//      if(isFull)
      boolean impossibleMatch = queue.impossibleMatch(TOKEN_CHARS);
      if(impossibleMatch && (queue.numElements() > 0))
      {
        next = queue.getNext();
        firstStream.print(next);
      }
//    addChar(c);
      queue.add(c);
      
      if(queue.isFull() && matchedToken())
//      if(queue.matchPrefix(TOKEN_CHARS))
      {
//        System.err.println(" Matched! ");
        // redirect every new byte to the second stream
        useFirstStream = false;
      }
//      else
//      {
//        firstStream.print(c);
//      }
    }
    else
    {
      secondStream.print(c);
      
      // debugging only!
//      debug(c);
      
      bytesToPrint --;
      if(bytesToPrint <= 0)
      {
        useFirstStream = true;
      }
    }
  }
  
  public void print(String string)
  {
    // avoid issues with "null"
    string = "" + string;
    
    // get existing bytes and print them one by one
    char[] data = string.toCharArray();
    int size = data.length;
    for(int i = 0; i < size; i++)
    {
      print(data[i]);
    }
  }
  
  public void println()
  {
    print(Util.NEW_LINE);
  }
  
  public void println(String string)
  {
    print(string);
    println();
  }
  
  /**
   * Match one expected token. The queue should be full
   * because if the prefix is matched, it will be completely 
   * consumed at once. 
   * 
   * @return
   */
  private boolean matchedToken()
  {
    boolean result;
    
    result = queue.matchPrefix(TOKEN_CHARS);
    if(result)
    {
//      debug(new String(queue.getElements()));
      
      queue.discard(TOKEN_CHARS.length);
      char[] data = queue.getElements();
      queue.setEmpty();
      
      bytesToPrint = readLength(data);
      
      if(bytesToPrint > 0)
      {
        useFirstStream = false;
      }
      else
      {
        // if there is any failure, just discard the queue content
        // and continue reading.
        bytesToPrint = 0;
        useFirstStream = true;
      }
    }
    
    return result;
  }

  /**
   * Read the length field and switch state as necessary.
   */
  private int  readLength(char[] data)
  {
    int length;
    String aux = new String(data);
    
    try
    {
      length = Integer.parseInt(aux);
    }
    catch (NumberFormatException e)
    {
      // if there is any failure, continue reading.
      length = 0;
      
      System.err.print("  Failure reading length field: ");
      System.err.println(data);
    }
    
    return length;
  }
    
//  public void sendPacketPrintByte(byte data, OutputStream outputStream) throws IOException
//  {
//    outputStream.write(TOKEN_BYTES);
//    outputStream.write(0);
//    outputStream.write(1);
//    
//    outputStream.write(data);
//  }
//  
  public void flush()
  {
    firstStream.flush();
    secondStream.flush();
  }
  
  public void close()
  {
    // if there is anything in the buffer, send it now.
    char[] data = queue.getElements();
    queue.setEmpty();
    firstStream.print(data);
    
    flush();
    firstStream.close();
    secondStream.close();
  }
  
  // Just for debugging. Comment all calls to this later
//  private void debug(char c)
//  {
//    firstStream.print(c);
//  }
//  
//  private void debug(String s)
//  {
//    firstStream.print(s);
//  }
}
