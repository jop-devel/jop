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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * RandomAccessByteArrayOutputStream.java
 * 
 * An output stream which provide more control over its
 * internal byte array. Useful to create data blocks when
 * the initial fields are not known in advance.
 * 
 * Can be reused, to avoid much garbage collection.
 * It provide also some methods to read its content.
 * Yes, that may seem very odd. But now it looks like 
 * it's also a quick and reasonably good choice.
 * 
 * @author Paulo Abadie Guedes
 *
 * 06/12/2007 - 10:24:09
 * 
 */
public class RandomAccessByteArrayOutputStream extends ByteArrayOutputStream
{
  /**
   * The default constructor of this class.
   */
  public RandomAccessByteArrayOutputStream()
  {
    super();
  }
  
  /**
   * Seek the cursor to the given location, by changing the internal
   * pointer. This can be used to overwrite initial locations.
   * 
   * @param location
   */
  private synchronized void seek(int location)
  {
    if(location >= 0 && location < buf.length)
    {
      count = location;
    }
  }
  
  /**
   * write a short value to the stream.
   * 
   * @param value
   */
  public synchronized void writeShort(int value)
  {
    write ((byte) (0xff & (value >> 8)));
    write ((byte) (0xff & value));
  }
  
  /**
   * write an int value to the stream.
   * 
   * @param value
   */
  public synchronized void writeInt(int value)
  {
    write ((byte) (0xff & (value >> 24)));
    write ((byte) (0xff & (value >> 16)));
    write ((byte) (0xff & (value >>  8)));
    write ((byte) (0xff & value));    
  }
  
  /**
   * Read one int value from a given location.
   * This method provide read access to the packet content.
   * 
   * Although it may seem really strange to provide read
   * access to an output stream, this seems now to
   * be the easiest way out to manipulate the packet
   * content without causing it to allocate memory.
   * 
   * @param location
   * @return
   */
  public synchronized int readInt(int location)
  {
    return readBytes(location, 4);
  }
  
  /**
   * Read one short value from a given location.
   * 
   * @param location
   * @return
   */
  public synchronized int readShort(int location)
  {
    return readBytes(location, 4);
  }
  
  /**
   * Read one byte value from a given location.
   * 
   * @param location
   * @return
   */
  public synchronized int readByte(int location)
  {
    return readBytes(location, 1);
  }
  
  /**
   * Read a set of bytes from the stream. Limited from
   * 1 until 4 bytes.
   * 
   * @param location
   * @param size
   * @return
   */
  private synchronized int readBytes(int location, int size)
  {

    int value;
    int i;
    
    if((size < 1) || (size > 4) || (location + size >= buf.length))
    {
      throw new ArrayIndexOutOfBoundsException("Wrong location or size! " + location);
    }
    
    value = 0;
    for(i = 0; i < size; i++)
    {
      // the first shift does nothing, but the others do.
      value <<= 8;
      value = value | buf[location];
      location++;
    }
    
    return value;
  }
  
  /**
   * Overwrite an int value on the given location.
   * 
   * If the location is outside the
   * internal buffer range, ignore the request and return.  
   * 
   * @param value
   * @param location
   */
  public synchronized void overwriteInt(int value, int location)
  {
    overwriteData(value, location, 4);
  }
  
  /**
   * Overwrite a short value on the given location.
   * 
   * If the location is outside the
   * internal buffer range, ignore the request and return.  
   * 
   * @param value
   * @param location
   */
  public synchronized void overwriteShort(int value, int location)
  {
    overwriteData(value, location, 2);
  }
  
  /**
   * Overwrite a byte value on the given location.
   * 
   * If the location is outside the
   * internal buffer range, ignore the request and return.  
   * 
   * @param value
   * @param location
   */
  public synchronized void overwriteByte(int value, int location)
  {
    overwriteData(value, location, 1);
  }
  
  /**
   * Overwrite a set of bytes on the internal array.
   * Used to overwrite int, byte, short and long. 
   * 
   * If the location is outside the internal 
   * buffer range, ignore the request and return.  
   * 
   * @param value
   * @param location
   * @param size
   */
  private void overwriteData(int value, int location, int size)
  {int dif;
    int currentLocation;
    
    // get the current location, to be restored later.
    currentLocation = size();
    
    // if there's not enough room, ignore it and return.
    dif = (buf.length - size) - location;
    if(dif < 0)
    {
      return;
    }
    
    // seek to the position to be overwritten.
    seek(location);
    
    // write the value
    switch(size)
    {
      case 4:
      {
        writeInt(value);
        break;
      }
      case 2:
      {
        writeShort(value);
        break;
      }
      case 1:
      {
        write(value);
        break;
      }
      default:
      {
        // do nothing. This should *not* happen, as the method is 
        // a private one. Hence, used only internally. 
        break;
      }
    }
    
    // restore the pointer to the previous location.
    seek(currentLocation);
  }
  
  /**
   * This method write the internal content to an output stream.
   * It's used to avoid allocating and releasing new objects for every
   * packet sent. 
   *  
   * @param outputStream
   * @throws IOException
   */
  public synchronized void writeContent(OutputStream outputStream) throws IOException
  {
    int size = size();
    
    // don't write the entire buffer. Instead, write just what's necessary.
    outputStream.write(buf, 0, size);
  }
}
