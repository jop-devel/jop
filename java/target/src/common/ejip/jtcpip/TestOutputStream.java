/* 
 * Copyright  (c) 2006-2007 Graz University of Technology. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The names "Graz University of Technology" and "IAIK of Graz University of
 *    Technology" must not be used to endorse or promote products derived from
 *    this software without prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE LICENSOR BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY  OF SUCH DAMAGE.
 */

package ejip.jtcpip;

import java.io.IOException;

/**
 * NOTE: works only if copied to package ejip.jtcpip!
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 849 $ $Date: 2007/01/24 19:37:07 $
 */
public class TestOutputStream
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		/*
		 * TCPOutputStream stream = new TCPOutputStream(3);
		 * 
		 * for(int j=0; j<2; j++) { System.out.println("Free Buffer: " +
		 * stream.getFreeBufferSpace()); int i=0; try{ for(; i < 5; i++) {
		 * System.out.println("writing"); stream.write(i);
		 * System.out.println("Free Buffer: " + stream.getFreeBufferSpace()); }
		 * 
		 * }catch(IOException e){ System.out.println("count: " + i + " message: " +
		 * e.getMessage()); } System.out.println("Free Buffer: " +
		 * stream.getFreeBufferSpace()); i=0; for(; i < 5; i++) {
		 * System.out.print("reading... ");
		 * System.out.println((int)stream.read()); System.out.println("Free
		 * Buffer: " + stream.getFreeBufferSpace()); } } try { stream.close();
		 * stream.write(1); }catch(IOException e){
		 * System.out.println(e.getMessage()); }
		 */
		// FIXME TEST TEST TEST
		TCPOutputStream stream = new TCPOutputStream(5);
		try
		{
			stream.write(1);
			stream.write(2);
			stream.write(3);
			stream.write(4);
			stream.write(5);
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());

			stream.setPtrForRetransmit();

			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());

			stream.ackData(5);

			stream.write(6);
			stream.write(7);
			stream.write(8);

			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());

			stream.ackData(3);
			System.out.println((int) stream.read());

			stream.ackData(1);
			stream.ackData(1);
			System.out.println((int) stream.read());
			stream.write(6);
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			stream.ackData(1);
			// empty and all ptrs to 1
			stream.write(1);
			stream.write(2);
			stream.write(3);
			stream.write(4);
			stream.write(5);
			stream.ackData(5);
			// empty and all ptrs newly to 1
			stream.write(1);
			stream.write(2);
			stream.write(3);
			stream.write(4);
			stream.write(5);
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			stream.ackData(3);
			stream.write(6);
			stream.write(7);
			System.out.println((int) stream.read());
			System.out.println((int) stream.read());
			stream.ackData(4);

			/*
			 * stream.write(7); stream.write(8);
			 * System.out.println((int)stream.read());
			 * System.out.println((int)stream.read());
			 * System.out.println((int)stream.read()); stream.ackData(3);
			 * System.out.println((int)stream.read());
			 */
		} catch (IOException e)
		{
			System.out.println(e.getMessage());
		}

	}

}
