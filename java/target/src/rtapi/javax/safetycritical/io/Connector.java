package javax.safetycritical.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * The class holding all static methods for creating all connection
 * objects.
 */
@SCJAllowed(members = true)
public class Connector
{
  /**
   * A constant for indicating the read and write mode.
   */
  public static final int READ_WRITE = 3;

  /**
   * A constant for indicating the read mode.
   */
  public static final int READ = 1;

  /**
   * A constant for indicating the write mode.
   */
  public static final int WRITE = 2;

  /**
   * create a new instance of this class in the current memory area. 
   * 
   * @return a new instance of this class.
   */
  public static Connector newInstance() { return new Connector(); }

  /**
   * Register a new factory for creating a specific kind of connection.
   * 
   * @param factory for a specific URL to add to the capability of this connector.
   */
  public void register(ConnectionFactory factory) {}

  /**
   * Get a new connection for the given URL.
   * 
   * @param url describes the I/O resource to open.
   * 
   * @return the connector for the given URL.
   *
   * @throws IllegalArgumentException when an invalid argument is given.
   *
   * @throws ConnectionNotFoundException when the resource is not found or the URL type is not supported.
   *
   * @throws IOException when some other I/O problem is encountered.
   */
  public Connection open(String url)
    throws IllegalArgumentException, ConnectionNotFoundException, IOException
  {
    throw new ConnectionNotFoundException("Connection is not an OutputConnection.");
  }

  /**
   * Get a new connection for the given URL.
   * 
   * @param url describes the I/O resource to open.
   * 
   * @param mode used to open the resource.
   * 
   * @return the connector for the given URL.
   *
   * @throws IllegalArgumentException when an invalid argument is given.
   *
   * @throws ConnectionNotFoundException when the resource is not found or the URL type is not supported.
   *
   * @throws IOException when some other I/O problem is encountered.
   */
  public Connection open(String url, int mode)
    throws IllegalArgumentException, ConnectionNotFoundException, IOException
  {
    throw new ConnectionNotFoundException("Connection is not an OutputConnection.");
  }

  /**
   * Get a new connection for the given URL.
   * 
   * @param url describes the I/O resource to open.
   * 
   * @param mode used to open the resource.
   *
   * @param timeouts indicates that the caller wants timeout exceptions.
   * 
   * @return the connector for the given URL.
   *
   * @throws IllegalArgumentException when an invalid argument is given.
   *
   * @throws ConnectionNotFoundException when the resource is not found or the URL type is not supported.
   *
   * @throws IOException when some other I/O problem is encountered.
   */
  public Connection open(String url, int mode, boolean timeouts)
    throws IllegalArgumentException, ConnectionNotFoundException, IOException
  {
    throw new ConnectionNotFoundException("Connection is not an OutputConnection.");
  }

  /**
   * A helper method to open a URL and get an {@link OutputStream} for it.
   * 
   * @param url describes the I/O resource to open.
   * 
   * @return a {@link OutputStream} for the given URL.
   *
   * @throws IllegalArgumentException when an invalid argument is given.
   *
   * @throws ConnectionNotFoundException when the resource is not found or the URL type is not supported.
   *
   * @throws IOException when some other I/O problem is encountered.
   */
  public OutputStream openOutputStream(String url)
    throws IllegalArgumentException, ConnectionNotFoundException, IOException
  {
    throw new IllegalArgumentException("Connection is not an OutputConnection.");
  }

  /**
   * A helper method to open a URL and get an {@link OutputStream} for it.
   * 
   * @param url describes the I/O resource to open.
   * 
   * @return a {@link InputStream} for the given URL.
   *
   * @throws IllegalArgumentException when an invalid argument is given.
   *
   * @throws ConnectionNotFoundException when the resource is not found or the URL type is not supported.
   *
   * @throws IOException when some other I/O problem is encountered.
   */
  public InputStream openInputStream(String url)
    throws IllegalArgumentException, ConnectionNotFoundException, IOException
  {
    throw new IllegalArgumentException("Connection is not an OutputConnection.");
  }

  /**
   * A helper method to open a URL and get an {@link OutputStream} for it.
   * 
   * @param url describes the I/O resource to open.
   * 
   * @return a {@link DataOutputStream} for the given URL.
   *
   * @throws IllegalArgumentException when an invalid argument is given.
   *
   * @throws ConnectionNotFoundException when the resource is not found or the URL type is not supported.
   *
   * @throws IOException when some other I/O problem is encountered.
   */
  public DataOutputStream openDataOutputStream(String url)
    throws IllegalArgumentException, ConnectionNotFoundException, IOException
  {
    throw new IllegalArgumentException("Connection is not an OutputConnection.");
  }

  /**
   * A helper method to open a URL and get an {@link OutputStream} for it.
   * 
   * @param url describes the I/O resource to open.
   * 
   * @return a {@link DataInputStream} for the given URL.
   *
   * @throws IllegalArgumentException when an invalid argument is given.
   *
   * @throws ConnectionNotFoundException when the resource is not found or the URL type is not supported.
   *
   * @throws IOException when some other I/O problem is encountered.
   */
  public DataInputStream openDataInputStream(String url)
    throws IllegalArgumentException, ConnectionNotFoundException, IOException
  {
    throw new IllegalArgumentException("Connection is not an OutputConnection.");
  }
}
