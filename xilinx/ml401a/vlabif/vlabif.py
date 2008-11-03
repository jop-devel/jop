#
# vlabif.py
# Virtual Lab Interface Driver
#
# Author: Jack Whitham
# $Id: vlabif.py,v 1.3 2008/11/03 11:41:28 jwhitham Exp $
#
#
# Copyright (C) 2008, Jack Whitham
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

VLAB_MAGIC_STRING = '\xf6lab\x70'

TX_PACKET_INFO = 0x80
TX_PACKET_CHANNEL_DATA = 0x81
TX_PACKET_READY = 0x82
TX_PACKET_OVERFLOW = 0x83
TX_PACKET_END_TX = 0x84
TX_ESCAPE_CODE = 0x87
TX_ESCAPE_MASK = 0xf8

RX_COMMAND_INFO = 0x70
RX_COMMAND_SEND_CHANNEL = 0x71

MAX_BYTES_AT_ONCE = 15
REPLY_TIMEOUT = 1.0
RESEND_TIMEOUT = 0.25



import sys, collections, time
from twisted.internet import reactor, defer, protocol


class VlabInterfaceError(Exception):
    """This exception is raised when a communication error
    occurs between the Virtual Lab Interface Hardware and the
    software drivers."""
    pass

class VlabChannelTransport:
    """An instance of this Transport class is created for each channel."""
    def __init__(self, cnum, vlihp):
        self.cnum = cnum
        self.protocol = None
        self.vlihp = vlihp
        self.outgoing_overflow = None
        self.outgoing_size = 0
        self.outgoing_queue = collections.deque()
        self.send_in_progress = False
        self.total_out = 0
        self.total_preout = 0
        self.total_in = 0
        self.wait_until_sent = []
        self.debug = False

    def dataReceived(self, bytes):
        if ( self.protocol != None ):
            self.total_in += len(bytes)
            if ( self.debug ):
                print 'Channel %u total in %u: %s' % (self.cnum, self.total_in,
                    repr(bytes))
            self.protocol.dataReceived(bytes)

    def setProtocol(self, proto, debug):
        if ( self.protocol != None ):
            self.protocol.connectionLost()

        self.debug = debug
        self.protocol = proto
        self.protocol.makeConnection(self)

    def waitUntilSent(self):
        d = defer.Deferred()
        self.wait_until_sent.append(d)
        return d

    def write(self, bytes):
        for ch in bytes:
            self.outgoing_queue.append(ch)

        self.total_preout += len(bytes)
        if ( self.debug ): 
            print 'Channel %u total preout %u: %s' % (self.cnum, 
                        self.total_preout, repr(bytes))

        if ( not self.send_in_progress ):
            self.send_in_progress = True
            self.send()

    def notifyOverflow(self, count):
        assert self.send_in_progress
        self.outgoing_overflow = count
        if ( self.debug ):
            print 'Channel %u overflow %u bytes' % (self.cnum, count + 1)

    def notifyReady(self, rc):
        assert self.send_in_progress
        assert len(self.outgoing_queue) >= self.outgoing_size
        assert self.outgoing_size > 0

        if ( self.debug ):
            print 'Channel %u notifyReady' % (self.cnum),

        if ( self.outgoing_overflow == None ):
            # All bytes received correctly
            successfully_sent = self.outgoing_size
            if ( self.debug ):
                print 'all bytes received.'
        else:
            # Some (or all) bytes not received
            successfully_sent = ( self.outgoing_size - 
                    self.outgoing_overflow ) - 1
            if ( self.debug ):
                print '%u bytes received.' % successfully_sent

            if ( successfully_sent == 0 ):
                # Channel is stalled! (No bytes were received.)
                reactor.callLater(RESEND_TIMEOUT, self.send)
                return

            assert 0 < successfully_sent < self.outgoing_size
       
        # Remove bytes that were received
        out = []
        for i in xrange(successfully_sent):
            out.append(self.outgoing_queue.popleft())

        self.total_out += successfully_sent
        if ( self.debug ): 
            print 'Channel %u total out %u: %s' % (self.cnum, 
                        self.total_out, repr(''.join(out)))

        # Finished sending?
        if ( len(self.outgoing_queue) == 0 ):
            self.send_in_progress = False
            while ( len(self.wait_until_sent) != 0 ):
                self.wait_until_sent.pop().callback(True)

        else:
            self.send()

    def send(self):
        assert self.send_in_progress
        if ( self.debug ):
            print 'Channel %u, sending (total) %u bytes.' % (self.cnum,
                    len(self.outgoing_queue))
        bytes = []
        for (i, ch) in enumerate(self.outgoing_queue):
            if ( i < MAX_BYTES_AT_ONCE ):
                bytes.append(ch)
            else:
                break


        bytes = ''.join(bytes)
        assert 0 < len(bytes) <= MAX_BYTES_AT_ONCE

        self.outgoing_overflow = None
        self.outgoing_size = len(bytes)
        self.vlihp.sendToChannel(self.cnum, 
                bytes).addCallback(self.notifyReady)

    

class VlabGenericProtocol(protocol.Protocol):
    """This generic Protocol can form the basis for a Protocol
    that communicates using a channel. It is used by the
    DebugDriver and VlabInterfaceProtocol classes."""
    def __init__(self, debug=False):
        self.packet_handler_dispatch = dict()
        self.packet_handler = self.handleUnknown
        self.packet_data = []
        self.wait_for_ready = collections.deque()
        self.debug = debug

    def registerHandler(self, byte, method):
        self.packet_handler_dispatch[ byte ] = method

    def dataReceived(self, bytes):
        assert False, "replace this method"

    def doData(self, ch):
        self.packet_data.append(chr(ch))

    def doCommand(self, ch):
        # Command
        # Previous packet is complete
        self.packet_handler(''.join(self.packet_data))

        # New packet begins
        self.packet_data = []
        self.packet_handler = self.packet_handler_dispatch.get(
                    ch, self.handleUnknown)

    def doReady(self):
        if ( len(self.wait_for_ready) == 0 ):
            raise VlabInterfaceError("Ready packet out of expected sequence.")
        else:
            wfr = self.wait_for_ready.popleft()
            wfr.callback(True)
                
    def connectionMade(self):
        pass

    def write(self, bytes):
        if ( self.debug ):
            print 'sending data %s' % repr(bytes)
        self.transport.write(bytes)

    def connectionLost(self):
        pass

    def waitForReady(self):
        if ( self.debug ):
            print 'wait for ready (%u already waiting)' % (
                    len(self.wait_for_ready))

        wfr = defer.Deferred()
        self.wait_for_ready.append(wfr)

        if ( self.debug ):
            def Trigger(rc):
                print 'wait for ready triggered' 
                return rc
            wfr.addCallback(Trigger)
        return wfr

    def handleUnknown(self, packet_data):
        if ( len(packet_data) != 0 ):
            print "Ignoring Unknown packet: '%s'" % repr(packet_data)


class VlabInterfaceProtocol(VlabGenericProtocol):
    """This driver allows software to control the Virtual Lab
    Interface Hardware. It can be connected to any Protocol
    object, such as a serial port provided by the pyserial module:

    >>> vlihp = vlabif.VlabInterfaceProtocol()
    >>> serialport.SerialPort(vlihp, SERIAL_PORT,
                    reactor=reactor, baudrate=115200)
    >>> yield vlihp.start()

    The above is useful if the hardware is connected directly to
    a serial port on your machine. If it is connected to a virtual
    lab serial port (provided by virtual lab software) then the
    following initialisation sequence should be used (with vlf being
    the virtual lab factory object):

    >>> vlihp = vlabif.VlabInterfaceProtocol(debug=False)
    >>> vl = yield vlf.getChannel()
    >>> yield vl.connect(BOARD_NAME)
    >>> yield vl.setUART(UART_NUMBER, 115200)
    >>> yield vl.openUART(UART_NUMBER, vlihp)
    >>> yield vlihp.start()
    """
    def __init__(self, debug=False):
        """Create a new Protocol object."""
        VlabGenericProtocol.__init__(self, debug=debug)
        self.num_channels = None
        self.hw_version = None

        self.ch_transport = [ None ]
        for i in xrange(1, 16):
            self.ch_transport.append(VlabChannelTransport(i, self))

        self.registerHandler(TX_PACKET_INFO, self._HandleInfo)
        self.registerHandler(TX_PACKET_CHANNEL_DATA, self._HandleChannelData)
        self.registerHandler(TX_PACKET_OVERFLOW, self._HandleOverflow)
        self.registerHandler(TX_PACKET_READY, self._HandleReady)
        self.registerHandler(TX_PACKET_END_TX, self._HandleEndTx)

        self.escape_active = False
        self.flushing = None
        self.tx_count = 0
        self.rx_count = 0
        self.started = False

    def getNumChannels(self):
        """Return the number of channels provided by the hardware.
        
        This is the ext_channels value given as a generic parameter
        to vlabifhw or vlabifhw_cn, plus two: channel zero represents
        the serial line used to communicate with the software,
        and the highest channel number is used for the debug chain
        hardware. Since ext_channels must be greater than zero, the
        smallest possible number of channels is 3."""
        return self.num_channels

    def getVersion(self):
        """Returns the hardware version number."""
        return self.hw_version

    def write(self, data):
        """Send bytes directly to the Virtual Lab Interface Hardware.

        Do not call this method. You need to open a channel and
        communicate through that."""
        VlabGenericProtocol.write(self, data)
        self.tx_count += len(data)
        if ( self.debug ):
            print 'total tx %d rx %d' % (self.tx_count, self.rx_count)

    def dataReceived(self, bytes):
        if ( not self.started ):
            return

        if ( self.debug ):
            print 'data received %s' % repr(bytes)

        if ( self.flushing != None ):
            self.flushing.callback(bytes)
            return

        self.rx_count += len(bytes)
        if ( self.debug ):
            print 'total tx %d rx %d' % (self.tx_count, self.rx_count)

        for ch in bytes:
            ch = ord(ch)

            if (( not self.escape_active )
            and (( ch & TX_ESCAPE_MASK ) == 
                    ( TX_ESCAPE_MASK & TX_ESCAPE_CODE ))):

                # Command or escape code
                if ( ch == TX_ESCAPE_CODE ):
                    self.escape_active = True
                else:
                    if ( self.debug ):
                        print 'executing command: %x' % ch

                    self.doCommand(ch)
                    if ( ch == TX_PACKET_READY ):
                        self.doReady()
                    
            else:
                # Data
                if ( self.escape_active ):
                    # Assert that escaping was required
                    # (If it wasn't, that's a clue about a protocol error)
                    assert (( ch & TX_ESCAPE_MASK ) == 
                            ( TX_ESCAPE_MASK & TX_ESCAPE_CODE ))
                self.doData(ch)
                self.escape_active = False

    def _HandleInfo(self, packet_data):
        if ( len(packet_data) != 1 ):
            raise VlabInterfaceError("Malformed Info packet: '%s'" %
                    repr(packet_data))

        self.num_channels = ord(packet_data[ 0 ]) >> 4
        if ( self.num_channels == 0 ):
            # 16
            self.num_channels = 16

        self.hw_version = ord(packet_data[ 0 ]) & 0xf
        assert 1 < self.num_channels <= 16

    def _HandleEndTx(self, packet_data):
        if ( len(packet_data) != 0 ):
            raise VlabInterfaceError("Malformed End Tx packet: '%s'" %
                    repr(packet_data))

    def _HandleReady(self, packet_data):
        if ( len(packet_data) != 0 ):
            raise VlabInterfaceError("Malformed Ready packet: '%s'" %
                    repr(packet_data))

    def _HandleChannelData(self, packet_data):
        if ( len(packet_data) == 0 ):
            raise VlabInterfaceError(
                    "Malformed zero length Channel Data packet")

        cnum = ord(packet_data[ 0 ]) >> 4
        if (( cnum == 0 ) or ( cnum >= self.num_channels )):
            raise VlabInterfaceError(
                    "Malformed channel number in Channel Data packet")

        if ( len(packet_data) > 0 ):
            if ( self.debug ):
                print 'Channel %u data received: %s' % (cnum,
                            repr(packet_data))
            self.ch_transport[ cnum ].dataReceived(packet_data[ 1: ])

    def _HandleOverflow(self, packet_data):
        if ( len(packet_data) != 1 ):
            raise VlabInterfaceError("Malformed Overflow packet: '%s'" %
                    repr(packet_data))

        v = ord(packet_data)
        cnum = v >> 4
        count = v & 0xf
        if ( self.debug ):
            print '** Overflow %u on channel %u' % (count, cnum)

        self.ch_transport[ cnum ].notifyOverflow(count)

    def start(self):
        """Start up the Virtual Lab Interface Hardware.

        This must be called once a connection is established.
        Before it is called, the hardware is in passthrough mode,
        with all information from channel 0 being relayed to channel 1
        and vice versa. This allows the hardware to act as a UART
        for connection to software that doesn't support the
        Virtual Lab Interface Hardware Protocol.

        Returns a Deferred. The callback is called with True
        when the operation is completed."""
        # What we do here is:
        # 1. Wait for the input buffer to empty (with a short timeout)
        # 2. Send the VLAB_MAGIC_STRING
        # 3. Receive Ready: means that we're started. Ok.
        # 4. Send RX_COMMAND_INFO
        # 5. Receive Ready: done

        done = defer.Deferred()
        class State: pass
        state = State()
        state.buffer_empty = True
        state.ready_received = False

        def Fail(ex_data):
            done.errback(ex_data)

        def ExpectReady_2(rc):
            done.callback(True)
    
        def ExpectReady_1(rc):
            # Request info packet
            state.ready_received = True
            self.write(chr(RX_COMMAND_INFO))
            self.waitForReady().addCallback(ExpectReady_2).addErrback(Fail)

        def Timeout_1():
            if ( state.ready_received ):
                # ExpectReady was reached before the timeout expired
                return

            done.errback(VlabInterfaceError("Activation string "
                    "was not acknowledged."))

        def TestEmpty():
            if ( state.buffer_empty ):
                # proceed
                self.write(VLAB_MAGIC_STRING)
                reactor.callLater(REPLY_TIMEOUT, Timeout_1)
                self.waitForReady().addCallback(
                            ExpectReady_1).addErrback(Fail)
                self.flushing = None
            else:
                # Keep waiting.
                state.buffer_empty = True
                reactor.callLater(REPLY_TIMEOUT, TestEmpty)

        def NotEmpty(bytes):
            state.buffer_empty = False
            self.flushing = defer.Deferred()
            self.flushing.addCallback(NotEmpty)

        self.started = True
        self.flushing = defer.Deferred()
        self.flushing.addCallback(NotEmpty)
        reactor.callLater(REPLY_TIMEOUT, TestEmpty)
        return done

    def openChannel(self, cnum, proto, debug=False):
        """Open a Virtual Lab Interface Hardware channel.
        
        The specified Protocol proto is associated with channel
        number cnum (must be greater than 0 and less than getChannels()).
        After association, information written to the protocol is
        emitted by the specified hardware channel as output. Information
        received by the hardware channel is sent to the protocol.
        Any Twisted Protocol object can be used, but vlabif provides
        two Protocols that might be particularly useful.
        One is SimpleDriver:

        >>> test = vlabif.SimpleDriver()
        >>> vlihp.openChannel(1, test)
        >>> test.write(chr(10))         # send 1 byte
        >>> rc = yield test.wait(1)     # wait for 1 byte

        The debug chain driver is also a Protocol:

        >>> dbg = vlabif.DebugDriver(debug_config)
        >>> vlihp.openChannel(CHANNEL_NUMBER, dbg)

        You could even give a VlabInterfaceProtocol object to
        openChannel in order to chain two Virtual Lab
        Interface Hardware devices together.
        """
        assert self.num_channels != None
        assert self.num_channels > 1
        assert 0 < cnum < self.num_channels
        self.ch_transport[ cnum ].setProtocol(proto, debug or self.debug)

    def sendToChannel(self, cnum, bytes):
        assert self.num_channels != None
        assert 0 < cnum < self.num_channels
        assert 0 < len(bytes) <= MAX_BYTES_AT_ONCE

        self.write(chr(RX_COMMAND_SEND_CHANNEL)
                + chr(cnum | ( len(bytes) << 4 ))
                + bytes)
        return self.waitForReady()
        


