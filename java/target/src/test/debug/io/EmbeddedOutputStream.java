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

package debug.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * EmbeddedOutputStream.java
 * 
 * A class to embed data into a text stream in a transparent way.
 *
 * This class is responsible for writting data to an output
 * stream, in a format that can later be filtered and removed from
 * the output. The isolated data can then be recovered and redirected
 * to another stream for usage.
 * 
 * The effect is that it actually "embed" data packets inside a text stream.
 * The receiving stream has to be prepared to accept and filter 
 * those data packets, to avoid them to be printed to the end user 
 * and also to recover its data and handle it.
 * 
 * This class could easily be used to simulate several distinct streams
 * over the same text stream, by using distinct tokens for each kind of
 * stream. Currently there is no such need yet.  
 * To implement this the change would be just to set some methods 
 * as not static and the token as a private field.
 * Then each object would start with a new token.
 * 
 * On the other side, the output stream receiving this data would need
 * to be ready to handle multiple streams as well. This could be achieved
 * by composing distinct output stream objects, one to filter each 
 * specific type of token.  
 * 
 * @author Paulo Abadie Guedes
 * 
 * 04/06/2007 - 23:03:38
 * 
 */
public class EmbeddedOutputStream extends OutputStream
{
  public static final String DEFAULT_TOKEN = "JDWP_DATA_PREFIX:";
  public static final byte[] hexTable = {'0', '1', '2', '3', '4', '5', '6', 
  '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
  
  private PrintStream printStream;
  private String token;
  
  public EmbeddedOutputStream (OutputStream outputStream)
  {
    this(new PrintStream(outputStream));
  }

  public EmbeddedOutputStream (PrintStream outputStream)
  {
    this(outputStream, DEFAULT_TOKEN);
  }
  
  public EmbeddedOutputStream (PrintStream outputStream, String token)
  {
    printStream = outputStream;
    this.token = token;
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  public void write(int data) throws IOException
  {
    //------------------------------------------------------------
    // WARNING: don't print ANYTHING to the standard output between 
    // the three calls below or the protocol will be broken.
    // If this is done, the inserted text will be interpreted as
    // data fields on the other side, which will break the receiver.
    
    printStream.print(token);
    printStream.print("0002");
    printHex((byte)data, printStream);
    
    //------------------------------------------------------------
  }
  
  public void testEmbeddedOutputStreamSendBytes()
    {
      int index;
      int num;
      
      num = 256;
  //    num = 5;
      for(index = 0; index < num; index++)
      {
        writeByte((byte)index);
      }
      
  //    num = 256;
  //    for(index = 0; index < num; index++)
  //    {
  //      printHex((byte) index);
  //      printStream.print(" ");
  //    }
  //    printHex((byte)128);
  //    testShiftRight();
    }

  public void testEmbeddedOutputStream()
  {
    int index;
    int num;
    
    num = 0x0fffffff;
    for(index = 0; index < num; index++)
    {
      writeInt(index);
    }
  }

  /**
   * Send one byte embedded into the regular text flow of a print stream.
   * 
   * @param data
   */
  public void writeByte(byte data)
  {
    //------------------------------------------------------------
    // WARNING: don't print ANYTHING to the standard output between 
    // the three calls below or the protocol will be broken.
    // If this is done, the inserted text will be interpreted as
    // data fields on the other side, which will break the receiver.
    
    printStream.print(token);
    printStream.print("0002");
    printHex(data);
    
    //------------------------------------------------------------
  }

  public void writeInt(int data)
  {
    //------------------------------------------------------------
    // WARNING: don't print ANYTHING to the standard output between 
    // the three calls below or the protocol will be broken.
  
    printStream.print(token);
    // System.err.println("Token delivered.");
      
    printStream.print("0008");
    //  System.err.println("Size delivered.");
      
    printIntHex(data);
    // System.err.println("Data delivered.");
    //------------------------------------------------------------
  }

  public void printIntHex(int data)
  {
    // WARNING this method is used to send embedded data. 
    // Don't print debug information inside it.
    
    printHex((byte)((data >> 24)& 0xff));
    printHex((byte)((data >> 16)& 0xff));
    printHex((byte)((data >> 8)& 0xff));
    printHex((byte)(data & 0xff));
  }

  public static void printIntHex(int data, PrintStream stream)
  {
    printHex((byte)((data >> 24)& 0xff), stream);
    printHex((byte)((data >> 16)& 0xff), stream);
    printHex((byte)((data >> 8)& 0xff), stream);
    printHex((byte)(data & 0xff), stream);    
  }
  
  public void printHex(byte data)
  {
    printHex(data, printStream);
  }

  public static void printHex(byte data, PrintStream printStream)
  {
    // WARNING this method is used to send embedded data. 
    // Don't print debug information inside it.
    byte nibble;
    
    // unsigned shift right operator does not exist for bytes.
    // The 'and' operation below solved the issue. 
    nibble = hexTable[(data >>> 4) & 0x0F];
    printStream.print((char)nibble);
    
    nibble = hexTable[data & 0x0F];
    printStream.print((char)nibble);
  }
  
//  public void write (byte[] b, int off, int len)
//    throws IOException, NullPointerException, IndexOutOfBoundsException
//  {
//    if (off < 0 || len < 0 || off + len > b.length)
//      throw new ArrayIndexOutOfBoundsException ();
//    
//    //TODO: check if the length is greater than 9999 and handle it
//    
//    printStream.print(token);
//    if(len < 1000)
//    {
//      printStream.print("0");
//    }
//    if(len < 100)
//    {
//      printStream.print("0");
//    }
//    if(len < 10)
//    {
//      printStream.print("0");
//    }
//    printStream.print(len);
//
//    for (int i = 0; i < len; ++i)
//    {
//      printHex (b[off + i], printStream);
//    }
//  }
}
