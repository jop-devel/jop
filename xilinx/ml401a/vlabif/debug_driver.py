#
# debug_driver.py
# Virtual Lab Interface - Debug Chain Driver
#
# Author: Jack Whitham
# $Id: debug_driver.py,v 1.3 2008/11/03 11:41:28 jwhitham Exp $
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


RX_FREE_RUN_BIT = 0
RX_CLOCK_BIT = 1
RX_RESET_BIT = 2
RX_FREE_RUN_BREAK_BIT = 3
RX_CAPTURE_BIT = 4
RX_READY_BIT = 5

RX_COMMAND_CLOCK_STEP = 0xa0
RX_COMMAND_GET_DEBUG_CHAIN = 0xa1
RX_COMMAND_SET_DEBUG_CHAIN = 0xa2
RX_COMMAND_SET_CTRL = 0xa3
RX_COMMAND_NOP = 0xa4

WAIT_FOR_DC = 0x100 # (NOTE: not a byte)

from vlabif import VlabGenericProtocol
from twisted.internet import protocol, defer
import sys, debug_entity, collections

class SyncError(Exception):
    """This exception is raised as a result of a communications
    error between the hardware debugger and the software."""
    pass

class DebugConfig(debug_entity.DebugEntity):
    """Create and manage a debugging chain configuration.

    The configuration is loaded from a suitably-formatted
    configuration file. The writeVHDL method can be called
    to generate VHDL for the debugging chain. This object can also
    be passed to DebugDriver to carry out debugging."""
    
    def __init__(self, dc_name="Autogen_Debug_Entity",
                chain_config_file="chain_config.py"):
        """Create a new configuration."""

        self.dc_name = dc_name
        debug_entity.DebugEntity.__init__(self, dc_name=self.dc_name)
        
        p = { 'Add' : self.makeDebugRegister,
            'add' : self.makeDebugRegister,
            'ReadWrite' : debug_entity.ReadWriteRegister,
            'Read' : debug_entity.ReadRegister,
            'Write' : debug_entity.WriteRegister,
        }
        execfile(chain_config_file, p, p)
        self.finishChain()

    def writeVHDL(self, out_file=None):
        """Emit a VHDL description of the debugging chain hardware."""
        if ( out_file == None ):
            out_file = self.dc_name + ".vhd"

        f = file(out_file, 'wt')
        f.write(self.getVHDL())
        f.close()


class DebugDriver(VlabGenericProtocol):
    """This driver allows software to control the debugging hardware,
    usually via the Virtual Lab Interface hardware. If vlihp is a
    reference to a Virtual Lab Interface object, then DebugDriver 
    should be initialised as follows:

    >>> debug_config = vlabif.DebugConfig()
    >>> dbg = vlabif.DebugDriver(debug_config, debug=False)
    >>> vlihp.openChannel(CHANNEL_NUMBER, dbg)
    """

    def __init__(self, debug_chain, less_features=False, debug=False):
        """Create a new DebugDriver for the specified debug_chain,
        which should be an intance of DebugChain, such as DebugConfig."""
        VlabGenericProtocol.__init__(self, debug=debug)
        self.less_features = less_features

        assert isinstance(debug_chain, debug_entity.DebugChain)
        self.debug_chain = debug_chain
        self.chain_length = self.debug_chain.getDebugChainLength()
        self.chain_length_bytes = ( ( self.chain_length + 7 ) / 8 )
        self.control_lines = 0
        self.dc_bits = []

        self.echo_byte_queue = collections.deque()
        
    def handleUnknown(self, packet_data):
        pass

    def waitForEcho(self, expect):
        d = defer.Deferred()
        self.echo_byte_queue.append((expect, d))
        return d

    def dataReceived(self, bytes):
        if ( self.debug ):
            print 'receiving data %s' % repr(bytes)
        for ch in bytes:
            ch = ord(ch)

            if ( len(self.echo_byte_queue) == 0 ):
                raise SyncError("Received byte 0x%x out of sequence." % ch)

            (expect, d) = self.echo_byte_queue[ 0 ]
            if ( expect == WAIT_FOR_DC ):
                # Special case: receiving debug chain data
                for i in xrange(8):
                    if ( ch & 128 ):
                        self.dc_bits.append('1')
                    else:
                        self.dc_bits.append('0')
                    ch = ch << 1

                if ( len(self.dc_bits) >= self.chain_length ):
                    # Finished getting the debug chain
                    self.echo_byte_queue.popleft()
                    d.callback(''.join(self.dc_bits))
                    self.dc_bits = []

                assert len(self.dc_bits) < self.chain_length

            else:
                # Waiting for a regular echoed byte
                self.echo_byte_queue.popleft()
                if ( expect != ch ):
                    raise SyncError("Received byte 0x%x, expected byte 0x%x." %
                            (ch, expect))
                d.callback(True)

    def ready(self):
        """Equivalent to setControlLines(dc_ready=True).

        This triggers the ready output, causing the output
        registers to be loaded from the debug chain.

        Returns a Deferred. The callback is called with True
        when the operation is completed."""
        return self.setControlLines(dc_ready=True)

    def capture(self):
        """Equivalent to setControlLines(dc_capture=True).

        This triggers the capture output, causing the debug chain
        to be loaded from the input registers.

        Returns a Deferred. The callback is called with True
        when the operation is completed."""
        return self.setControlLines(dc_capture=True)

    def setControlLines(self, dc_capture=None, free_run_break=None,
                reset=None, clock=None, free_run=None,
                dc_ready=None):
        """Set the value of the control lines.
        
        The control lines are: dc_capture, reset, clock, 
        free_run, free_run_break.
        You can call this method with zero or more of these names as
        keyword arguments. Unspecified names are not changed.

        The value of a control line can be True (set to 1)
        or False (set to 0) or None (don't change). 

        Returns a Deferred. The callback is called with True
        when the operation is completed."""

        for (value, bit) in [
                    (dc_capture, RX_CAPTURE_BIT),
                    (reset, RX_RESET_BIT),
                    (clock, RX_CLOCK_BIT),
                    (free_run, RX_FREE_RUN_BIT),
                    (free_run_break, RX_FREE_RUN_BREAK_BIT),
                    (dc_ready, RX_READY_BIT),
                    ]:

            if ( value == None ):
                pass # Take no action
            else:
                if ( value ):
                    # Turn on
                    self.control_lines |= 1 << bit
                else:
                    # Turn off
                    self.control_lines &= ~ ( 1 << bit )

        self.write(chr(RX_COMMAND_SET_CTRL) + chr(self.control_lines))

        # dc_capture/dc_ready are left as zero since they are "one shot"
        self.control_lines &= ~ (( 1 << RX_CAPTURE_BIT ) 
                                | ( 1 << RX_READY_BIT ))
        return self.waitForEcho(RX_COMMAND_SET_CTRL)

    def downloadChain(self):
        """Download the debugging chain from the
        device under test.

        You need to capture the debugging chain before calling this
        method, by calling capture.

        Returns a Deferred. The callback is called with 
        a chain_value object when the operation is completed."""

        assert ( self.debug_chain != None )

        pgm = chr((self.chain_length_bytes >> 8) & 0xff)
        if ( self.less_features ):
            pgm = ''

        self.write(chr(RX_COMMAND_GET_DEBUG_CHAIN) + pgm +
                chr((self.chain_length_bytes >> 0) & 0xff))

        done = defer.Deferred()

        def fail(ex_data):
            done.errback(ex_data)

        def Received_Chain(chain_text):
            assert len(chain_text) >= self.chain_length
            chain_text = chain_text[ : self.chain_length ]
            self.debug_chain.loadDebugChainData(chain_text)
            done.callback(self.getAsDict())

        self.waitForEcho(RX_COMMAND_GET_DEBUG_CHAIN)
        self.waitForEcho(WAIT_FOR_DC).addCallback(
                    Received_Chain).addErrback(fail)

        return done

    def uploadChain(self):
        """Send a (possibly changed) debug chain to the
        device under test.

        To use this function, first download the chain with
        downloadChain. Then call the setOutput method of 
        the ReadWriteRegister objects that you wish to change.
        Then, call uploadChain. Once uploadChain has completed,
        use the ready function to load the output register.

        Returns a Deferred. The callback is called with 
        the parameter True the operation is completed."""

        assert ( self.debug_chain != None )
        bits = self.debug_chain.saveDebugChainData()
        bytes = []
        for byte in xrange(self.chain_length_bytes):
            out = 0
            for bit in xrange(byte * 8, (byte + 1) * 8):
                out = out << 1

                if (( bit < len(bits) )
                and ( bits[ bit ] == '1' )):
                    out = out | 1

            bytes.append(chr(out))

        pgm = chr(((self.chain_length_bytes - 1 ) >> 8) & 0xff)
        if ( self.less_features ):
            pgm = ''

        self.write(chr(RX_COMMAND_SET_DEBUG_CHAIN) + pgm +
                chr(((self.chain_length_bytes - 1 ) >> 0) & 0xff) + 
                ''.join(bytes))

        return self.waitForEcho(RX_COMMAND_SET_DEBUG_CHAIN)

    def reset(self):
        """Reset the device under test, by raising the reset line,
        activating free run, stopping free run, and lowering the reset
        line.

        Returns a Deferred. The callback is called with True when
        the operation is completed."""

        done = defer.Deferred()

        def fail(ex_data):
            done.errback(ex_data)

        def Stage_4(rc):
            done.callback(True)

        def Stage_3(rc):
            self.setControlLines(
                reset=False).addCallback(Stage_4).addErrback(fail)

        def Stage_2(rc):
            self.setControlLines(free_run=False, 
                reset=True).addCallback(Stage_3).addErrback(fail)

        self.control_lines = 0
        self.setControlLines(free_run=True, 
                reset=True).addCallback(Stage_2).addErrback(fail)
        return done
        
    def clock(self, n=1):
        """Send one or more clock cycles to the device under test.

        The minimum number of clock cycles is 1. The maximum number is
        65535. The clock line is left in the zero state.

        Returns a Deferred. The callback is called with True when
        the operation is completed."""

        if ( self.less_features ):
            assert False, "clock() feature is not available"

        assert 0 < n < 0x10000
        self.write(chr(RX_COMMAND_CLOCK_STEP) +
                chr((n >> 8) & 0xff) + chr((n >> 0) & 0xff))
        return self.waitForEcho(RX_COMMAND_CLOCK_STEP)

    def getAsDict(self):
        """Get the debug chain as a dictionary object,
        where each key is a register name (a string) and each value is
        a ReadWriteRegister object."""
        return self.debug_chain.getAsDict()

    def getAsList(self):
        """Get the debug chain as a list, where each item is
        a ReadWriteRegister object."""
        return self.debug_chain.getAsList()

    def printChain(self):
        """Print out the current value of the debug chain using
        "print getText()" for each ReadWriteRegister."""
        for dr in self.getAsList():
            print dr.getText()

