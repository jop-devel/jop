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

/**
 * 
 * @author Ulrich Feichter
 * @author Tobias Kellner
 * @author Christof Rath
 * @version $Rev: 849 $ $Date: 2007/01/24 19:37:07 $
 */
public class TestInputStream
{
	/**
	 * 
	 */
	private TCPInputStream stream;

	/**
	 * 
	 */
	public TestInputStream()
	{
		stream = new TCPInputStream(3);
	}

	/**
	 * @param istream
	 */
	protected TestInputStream(TCPInputStream istream)
	{
		stream = istream;
	}

	/**
	 * 
	 */
	protected void read()
	{
		while (true)
		{
			int uli = stream.read();
			if (uli == -1)
			{
				System.out.println("READ: got a -1");
				break;
			}

			System.out.println("READ: " + uli);
		}
	}

	public void run()
	{
		read();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		TestInputStream testclass1 = new TestInputStream();

		TestInputStream testclass2 = new TestInputStream(testclass1.stream);

		//FIXME
		//testclass2.start();

		int i = 0;
		while (i < 10000000) //TODO: waiting for what?
			// lets wait a bit
			i++;

		System.out.println("Free Buffer: " + testclass1.stream.getFreeBufferSpace());
		i = 0;
		for (; i < 5; i++)
		{
			System.out.print("writing... ");
			System.out.println(testclass1.stream.write(i));
			System.out.println("Free Buffer: " + testclass1.stream.getFreeBufferSpace());
		}

		i = 0;
		while (i < 10000000)
			// lets wait a bit
			i++;

		System.out.println("Free Buffer: " + testclass1.stream.getFreeBufferSpace());
		i = 0;
		for (; i < 5; i++)
		{
			System.out.print("writing... ");
			System.out.println(testclass1.stream.write(i));
			System.out.println("Free Buffer: " + testclass1.stream.getFreeBufferSpace());
		}

		i = 0;
		while (i < 10000000)
			// lets wait a bit
			i++;

		testclass1.stream.close();
		testclass1.stream.write(1);

	}
}
