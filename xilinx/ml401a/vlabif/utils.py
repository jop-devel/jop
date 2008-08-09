#
# extras.py
# Virtual Lab Interface - Extras
#
# Author: Jack Whitham
# $Id: utils.py,v 1.1 2008/08/09 12:29:30 jwhitham Exp $
#

from twisted.internet import protocol, defer, reactor
import random, collections

class SimpleDriver(protocol.Protocol):
    """This is a simple protocol for accessing a Virtual Lab
    Interface Hardware channel. 
    
    It is typically created as follows:
    >>> tc = vlabif.SimpleDriver()
    >>> vlihp.openChannel(CHANNEL_NUMBER, tc)

    Then, it can be used to read or write for a channel, e.g.
    >>> tc.write('hello world')
    >>> received_data = yield tc.wait(5)
    >>> print received_data
    """

    def __init__(self):
        """Create a new SimpleDriver."""
        self.__wait_queue = collections.deque()
        self.__data_queue = collections.deque()

    def wait(self, expect_nbytes):
        """Wait for expect_nbytes from the channel.

        Returns a Deferred. The callback is called with the
        received bytes when the operation is completed."""

        d = defer.Deferred()
        self.__wait_queue.append((expect_nbytes, d))
        self.checkQueue()
        return d

    def waitUntilSent(self):
        """Wait until every byte given to write() has been sent
        and acknowledged by the receiver.

        Returns a Deferred. The callback is called with the
        received bytes when the operation is completed."""
        return self.transport.waitUntilSent()

    def dataReceived(self, bytes):
        for ch in bytes:
            self.__data_queue.append(ch)

        self.checkQueue()

    def checkQueue(self):
        while ( len(self.__wait_queue) != 0 ):
            (expect_nbytes, d) = self.__wait_queue[ 0 ]
            if ( len(self.__data_queue) < expect_nbytes ):
                break
            else:
                self.__wait_queue.popleft()
                out = []
                for i in xrange(expect_nbytes):
                    out.append(self.__data_queue.popleft())
                d.callback(''.join(out))
           

    def connectionMade(self):
        pass

    def write(self, bytes):
        """Send bytes to the channel.

        This method returns immediately and does not produce
        a deferred."""
        self.transport.write(bytes)

    def connectionLost(self):
        pass

    def flush(self, delay_time):
        """Wait for the specified period of time (in seconds) and
        delete all input that arrives during this time. 

        Returns a Deferred. The callback is called with the
        number of bytes that were deleted."""

        assert len(self.__wait_queue) == 0
        done = defer.Deferred()

        def Fire():
            count = len(self.__data_queue)
            self.__data_queue.clear()
            done.callback(count)

        reactor.callLater(delay_time, Fire)
        return done
        

def makeNoise():
    """Generate 1 to 1000 bytes of random noise. Used for testing."""
    nbytes = random.randint(1, 1000)
    out = []
    for i in xrange(nbytes):
        out.append(chr(random.randint(0, 255)))
    return ''.join(out)

