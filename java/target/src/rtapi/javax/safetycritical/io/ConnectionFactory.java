/*---------------------------------------------------------------------*\
 *
 * Copyright aicas GmbH, Karlsruhe 2009
 *
 * This code is provided to the JSR 302 group for evaluation purpose
 * under the LGPL 2 license from GNU.  This notice must appear in all
 * derived versions of the code and the source must be made available
 * with any binary version.  Viewing this code does not prejudice one
 * from writing an independent version of the classes within.
 *
 * $Source: /home/cvs/jsr302/scj/specsrc/javax/safetycritical/io/ConnectionFactory.java,v $
 * $Revision: 1.2 $
 * $Author: jjh $
 * Contents: Java source code of ConnectionFactory
 *
\*---------------------------------------------------------------------*/

package javax.safetycritical.io;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;
import javax.safetycritical.annotate.SCJAllowed;

/** A factory for creating connections for a given URL type. */
@SCJAllowed(members = true)
public abstract class ConnectionFactory
{
  private String prefix_;

  /**
   * Create a Connection factory for the given URL type
   *
   * @param prefix is the type of the URL
   */
  protected ConnectionFactory(String prefix)
  {
    prefix_ = prefix;
  }

  /**
   * @param name
   * @return
   */
  boolean matches(String name)
  {
    return name.startsWith(prefix_);
  }

  /**
   * Create of connection for the URL type of this factory.
   *
   * @param url for which to create the connection
   * @return a connection for the URL
   * @throws ConnectionNotFoundException when the I/O resource referenced
   *                                     by the URL is not found.
   * @throws IOException when some other I/O problem is encountered.                            
   */
  public abstract Connection create(String url)
    throws ConnectionNotFoundException,
           IOException;

  @Override
  public int hashCode()
  {
    return prefix_.hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    return (other instanceof ConnectionFactory) &&
            prefix_.equals(((ConnectionFactory)other).prefix_);
  }
}
