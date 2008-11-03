 
# Virtual Lab
# $Id: vlab.py,v 1.6 2008/11/03 11:41:24 jwhitham Exp $
#
# Copyright (C) 2008, Jack Whitham
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
from twisted.conch import error
from twisted.conch.ssh import transport
from twisted.internet import defer
from twisted.python import log 
from twisted.internet import protocol, reactor
from twisted.conch.ssh import keys, userauth
from twisted.conch.ssh import connection
from twisted.conch.ssh import channel, common
import sys, collections, zlib, os, base64, pickle, time

from vlabmsg import *

CVERSION = '0.1'

class VlabException(Exception):
    """All virtual lab exceptions inherit this."""
    pass

class ConnectException(VlabException):
    pass

class VlabErrorException(VlabException):
    """An exception that occurred on the board or relay shell.

    The exception has been reported over the SSH connection using 
    an error message. The VlabErrorException object has an
    errorcode attribute that is one of the codes listed in
    this module, e.g. ERR_UNAVAIALBLE. It also has an English
    description of the error which is printed if the exception is
    not caught."""
    def __init__(self, errorcode, reason):
        VlabException.__init__(self, reason)
        self.errorcode = errorcode

class VlabBitfileException(Exception):
    """An exception that is raised if an invalid bit file is
    supplied to the sendBitfile method. 

    In this case, "invalid" means that the bit file did not
    pass validation on the board server, e.g. it was made for the
    wrong FPGA type, or it was truncated, or it didn't have a valid
    header."""
    pass

TIMEOUT_FOR_SOCKS = 10.0
TIMEOUT_FOR_KEEPALIVE = 40.0

[ BINFO_UNUSED , BINFO_INVALID , BINFO_INCOMPLETE , BINFO_READY,
        BINFO_WRONG_FPGA, UINFO_IN_USE, UINFO_OFFLINE,
        UINFO_AVAILABLE, UINFO_UNKNOWN,
        VLAB_NOT_CONNECTED, VLAB_BOARD_SERVER, 
        VLAB_RELAY_SERVER, VLAB_UART, VLAB_UNKNOWN ] = range(14)

class VlabBitInfo:
    """Holds information about a bit file, returned by the
    getBitInfo method."""
    def __init__(self):
        self.bid = -1
        self.size_bits = ""
        self.design_name = ""
        self.part_name = ""
        self.syn_date = ""
        self.syn_time = ""
        self.status = BINFO_UNUSED

    def __str__(self):
        if ( self.status == BINFO_UNUSED ):
            return "unused"
        elif ( self.status == BINFO_INVALID ):
            return "invalid"
        elif ( self.status == BINFO_INCOMPLETE ):
            return "incomplete"
        else:
            return "%s (%s %s) for %s" % (self.design_name,
                    self.syn_date, self.syn_time, self.part_name)

class VlabUserInfo:
    """Holds information about a virtual lab user, returned by the
    getBoardUserInfo method."""
    def __init__(self):
        self.lock_count = 0
        self.user_name = None
        self.status = UINFO_UNKNOWN

    def __str__(self):
        if ( self.status == UINFO_AVAILABLE ):
            return "available"
        elif ( self.status == UINFO_OFFLINE ):
            return "offline"
        elif ( self.user_name == None ):
            return "?"
        else:
            return self.user_name

class VlabVersion:
    """Holds information about the software versions of various parts
    of the virtual lab. Available as the "version" field of VlabChannel."""
    def __init__(self):
        self.client = CVERSION
        self.relay = None
        self.embedded = None
        self.mutexdaemon = None

    def lowest(self):
        """Returns the lowest software version currently in use
        within the virtual lab."""
        d = [ x for x in [ self.client, self.relay, 
                    self.embedded, self.mutexdaemon ] if x != None ]
        d.sort()
        return d[ 0 ]

    def __str__(self):
        return str(self.lowest())

class VlabAuthorisation:
    """Holds authorisation data for a virtual lab user."""
    def __init__(self):
        self.relay_server_host_key = None
        self.user_name = None
        self.public_key = None
        self.private_key = None
        self.relay_server_name = None
        self.version = CVERSION

AUTH_LEADIN = '--- BEGIN VIRTUAL LAB AUTHORISATION DATA ---'
AUTH_LEADOUT = '--- END VIRTUAL LAB AUTHORISATION DATA ---'

def saveAuthorisation(auth, fname):
    """Save a VlabAuthorisation object to a disk file, for
    distribution to a virtual lab user."""
    assert isinstance(auth, VlabAuthorisation)
    code = base64.b64encode(zlib.compress(pickle.dumps(auth), 9))
    sz = 70
    out = file(fname, 'wt')
    out.write('User: %s\n' % auth.user_name)
    out.write(AUTH_LEADIN)
    out.write('\n')

    for i in xrange(0, len(code), sz):
        out.write(code[ i : i + sz ])
        out.write('\n')

    out.write(AUTH_LEADOUT)
    out.write('\n')
    out.close()

def loadAuthorisation(fname):
    """Load a VlabAuthorisation object from disk. The object
    is typically passed to VlabClientFactory."""
    code = []
    start = False
    for line in file(fname, 'rt'):
        line = line.strip()
        if ( line == AUTH_LEADIN ):
            start = True
        elif ( line == AUTH_LEADOUT ):
            break
        elif ( start ):
            code.append(line)

    try:
        out = pickle.loads(zlib.decompress(base64.b64decode(''.join(code))))
        if ( not isinstance(out, VlabAuthorisation) ):
            raise Exception()
        return out
    except:
        raise VlabException("Loading authorisation failed.")

def vlabProcessMessage(handler_class, message_table, 
                    message_buffer, new_data, debug):
    """Internal function; process a message that has been received."""

    def doMsg(msg):
        if ( debug ):
            print 'VIRTUAL LAB Receive: "%s"' % msg

        fields = msg.split()
        if ( len(fields) == 0 ):
            return False

        command = fields[ 0 ].lower()
        tbl_entry = message_table.get(command, None)
        if ( tbl_entry == None ):
            return False

        get_all_remaining = tbl_entry[ 0 ]
        handler_name = tbl_entry[ 1 ]
        if ( get_all_remaining ):
            # Resplit - put all of final parameter into one string.
            fields = msg.split(None, len(tbl_entry) - 2)

        handler_fn = getattr(handler_class, handler_name, None)
        if ( handler_fn == None ):
            raise VlabException(
                        'Missing handler function: %s' % (handler_name))
        else:
            if ( debug ):
                print 'VIRTUAL LAB calling function: %s' % (handler_name)

        params = []

        for (value, conv_fn) in zip(fields[ 1: ], tbl_entry[ 2: ]):
            params.append(conv_fn(value))

        handler_fn(*params)
        return True

    for ch in new_data:
        if ( ch in "\r\n" ):
            doMsg(''.join(message_buffer))
            message_buffer.clear()
        elif ( ch in '\x08\x7f' ):
            if ( len(message_buffer) != 0 ):
                message_buffer.pop()
        else:
            if ( len(message_buffer) < 1024 ):
                message_buffer.append(ch)

def processUserInfo(userinfo_table,
                index, valid, user_name, lock_count):
    try:
        uinfo = userinfo_table[ index ]
    except KeyError:
        uinfo = userinfo_table[ index ] = VlabUserInfo()

    uinfo.lock_count = lock_count
    uinfo.user_name = None
    if ( valid ):
        uinfo.user_name = user_name
        uinfo.status = UINFO_IN_USE
    elif ( user_name == "offline" ):
        uinfo.status = UINFO_OFFLINE
    else:
        uinfo.status = UINFO_AVAILABLE


class VlabChannel(channel.SSHChannel):
    """A Protocol for interacting with virtual lab services.
    
    Instances of this class are created by VlabClientSSHConnection objects."""

    name = 'session'

    def __init__(self, register_fn=None, debug=False, fail_fn=None, **args2):
        """Create a new VlabChannel with the specified register_fn.
        
        This __init__ method is normally called from the SSHConnection
        serviceStarted method."""
        channel.SSHChannel.__init__(self, **args2)
        self.initData(debug)
        self.register_fn = register_fn

        if ( fail_fn == None ):
            def noFn(*args1, **args2): pass
            self.fail_fn = noFn
        else:
            self.fail_fn = fail_fn

    # Requests and their protocol level (Cl*) functions.
    def getBoardUserInfo(self, board_name):
        """Request a list of users of the specified board_name.

        Returns a Deferred. The callback will be called with a list
        of VlabUserInfo objects, one for each instance of board_name."""
        self.userinfo_table = dict()
        self.sendMessage(MSG_USERREQUEST, board_name)
        assert self.stage == VLAB_RELAY_SERVER, (
            "This is only possible when connected to the relay shell." )

        def transform((command, parameters)):
            assert command == MSG_ENDLIST
            out = []
            i = 0
            while ( self.userinfo_table.has_key(i) ):
                out.append(self.userinfo_table[ i ])
                i += 1
            return out

        return self.awaitEndMessage().addCallback(transform)

    def ClUserInfo(self, index, valid, user_name, lock_count):
        processUserInfo(self.userinfo_table,
                index, valid, user_name, lock_count)

    def getBitInfo(self):
        """Request a list of bit file buffers on the board server.

        Returns a Deferred. The callback will be called with a list
        of VlabBitInfo objects, one for each bit file buffer."""
        self.assertProper("getBitInfo")
        self.bitinfo_table = dict()
        self.sendMessage(MSG_SHOWBITS)

        def transform((command, parameters)):
            assert command == MSG_ENDLIST
            out = []
            i = 0
            while ( self.bitinfo_table.has_key(i) ):
                out.append(self.bitinfo_table[ i ])
                i += 1
            return out

        return self.awaitEndMessage().addCallback(transform)

    def ClReceiveBitInfo(self, index, bid, size_bits,
                design_name, part_name, syn_date, syn_time, english):
       
        try:
            binfo = self.bitinfo_table[ index ]
        except KeyError:
            binfo = self.bitinfo_table[ index ] = VlabBitInfo()
        binfo.bid = bid
        binfo.size_bits = size_bits
        binfo.design_name = design_name
        binfo.part_name = part_name
        binfo.syn_date = syn_date
        binfo.syn_time = syn_time
        if ( binfo.bid <= 0 ):
            binfo.status = BINFO_UNUSED
        elif ( binfo.size_bits <= 0 ):
            if ( binfo.syn_time == 'invalid' ):
                binfo.status = BINFO_INVALID
            elif ( binfo.syn_time == 'incomplete' ):
                binfo.status = BINFO_INCOMPLETE
            elif ( binfo.syn_time == 'wrong' ):
                binfo.status = BINFO_WRONG_FPGA
            else:
                binfo.status = BINFO_INVALID
        else:
            binfo.status = BINFO_READY

    def setUART(self, uart_number, baud):
        """Set the baud rate for the specified UART.

        Returns a Deferred. The callback will be called with the OK
        message. The baud rate can be any of the standard RS232 baud rates,
        e.g. 9600, 38400, 115200."""
        self.assertProper("setUART")
        self.sendMessage(MSG_SETUART, uart_number, baud)

        def ack((command, parameter)):
            assert command == MSG_OK
            return True
            
        return self.awaitEndMessage().addCallback(ack)

    def sendBitfile(self, bits, compress_level=9):
        """Send a bit file to the board server.

        Returns a Deferred. The callback will be called with the bid 
        (bit file id), which is an integer. To load the bit file onto an
        FPGA, you must call programFPGA with the bid."""
        self.assertProper("sendBitfile")

        self.stored_bid = None
        transfer_complete = self.getDeferred()
        cbits = [ '' ]

        def fail(ex_data):
            transfer_complete.errback(ex_data)

        # stage 3 - receive MSG_LOADED via ClLoaded;
        # this calls complete:
        def complete((command, bid, ok)):
            assert command == MSG_LOADED
            assert self.stored_bid == bid
            if ( ok ):
                transfer_complete.callback(bid)
            else:
                transfer_complete.errback(
                        VlabBitfileException("Bitfile did not validate."))

        # stage 2 - receive MSG_LOADREADY via ClLoadReady;
        # this calls transfer to send the data:
        def transfer((command, bid)):
            assert command == MSG_LOADREADY
            self.stored_bid = bid
            self.write(cbits[ 0 ])
            self.awaitEndMessage().addCallback(complete).addErrback(fail)

        # stage 1 - send loadbits message
        # First, compress the bit file (you might be 
        # sending the file over a slow network connection)
        def start():
            cbits[ 0 ] = zlib.compress(bits, compress_level)
            size_bits = len(cbits[ 0 ]) * 8
            self.sendMessage(MSG_LOADBITS, size_bits)
            self.awaitEndMessage().addCallback(transfer).addErrback(fail)

        start()
        return transfer_complete

    def ClLoadReady(self, bid, num_bits, english):
        self.reply().callback((MSG_LOADREADY, bid))

    def ClLoaded(self, bid, ok):
        self.reply().callback((MSG_LOADED, bid, ok))

    def programFPGA(self, fpga_num, bid):
        """Schedule programming the specified bit file (bid) onto FPGA fpga_num.

        Returns a Deferred. The callback will be called with True
        as its parameter on success. The errback is called on failure."""
        self.assertProper("programFPGA")

        self.stored_bid = bid
        self.sendMessage(MSG_PROGRAM, fpga_num, bid)
        # This is for the MSG_OK that is sent to say the bit file
        # is in the programming queue.
        self.awaitEndMessage() 
        # This is for the MSG_PROGRAMOK.
        return self.awaitEndMessage()

    def ClProgramOk(self, bid):
        # Check bid - very important to do this because
        # other ProgramOk messages might get through
        if ( bid == self.stored_bid ):
            self.reply().callback(True)

    def ClProgramFailed(self, bid, shortname):
        # Check bid - see ClProgramOk
        if ( bid == self.stored_bid ):
            self.reply().errback(VlabErrorException(
                    shortname, ERR_TABLE.get(shortname, 
                        ERR_TABLE[ ERR_UNKNOWN ])))

    def connect(self, board_name):
        """Connect to a board. This is only possible
        when you have a connection to a relay shell.
        
        Returns a Deferred. The callback will be called when the
        connection is made; its parameter will be True."""
        if ( self.stage != VLAB_RELAY_SERVER ):
            raise VlabException("connect() to a relay shell first.")

        output = self.getDeferred()

        def success((command, parameters)):
            assert command == MSG_OK
            self.stage = VLAB_BOARD_SERVER
            self.sendMessage(MSG_REM, 'Client "%s" on %s %x' %
                    (CVERSION, os.name, sys.hexversion))
            output.callback(True)

        def tryAgain(ex_data):
            if (( isinstance(ex_data.value, VlabErrorException) )
            and ( ex_data.value.errorcode == ERR_UNAVAILABLE )):
                # do try again
                self.sendMessage(MSG_CONNECT, board_name)
                self.awaitEndMessage().addCallback(success).addErrback(tryAgain)
            else:
                # don't try again
                output.errback(ex_data)

        self.sendMessage(MSG_CONNECT, board_name)
        self.awaitEndMessage().addCallback(success).addErrback(tryAgain)
        return output

    def disconnect(self):
        """Disconnect from a board or relay server.
        This method doesn't return anything."""
        self.loseConnection()

    def openUART(self, uart_num, protocol):
        """Connect the specified Protocol object to UART uart_num.

        Returns a Deferred. The callback will be called with parameter
        True once the action has completed.
        openUART is only possible when the channel is connected to a
        board server. It can only be done once. Once it is done,
        other commands are no longer possible. This means you will
        normally need a control connection (for commands) and a
        data connection (for UART data)."""
        self.assertProper("openUART")
        self.sendMessage(MSG_USEUART, uart_num)
        self.stage = VLAB_UNKNOWN

        def success((command, parameter)):
            assert command == MSG_USINGUART
            self.uart_protocol = protocol
            self.stage = VLAB_UART
            self.uart_protocol.makeConnection(self) # I'm the transport.
            return True

        return self.awaitEndMessage().addCallback(success)

    def jtagDirectCommand(self, jdw_cmd, *params):
        """Use the JTAG Direct Write module to execute a JTAG commmand.

        Returns a Deferred. The callback will be called with a Boolean
        that is True if the command was successful, False otherwise. """
        self.assertProper("jdw")
        self.sendMessage(MSG_JDW, jdw_cmd, *params)

        def success((command, parameter)):
            assert command in (MSG_OK, MSG_JDWFAIL)
            return command == MSG_OK 

        return self.awaitEndMessage().addCallback(success)
        
    def jtagDirectShift(self, readback, last, data, num_bits):
        """Shift data using the JTAG Direct Write module.

        Returns a Deferred. If readback=True, then the callback
        will be called with the received data. If not, then the callback
        will be called with True."""
        self.assertProper("jdw")
        assert ( len(data) * 8 ) >= num_bits
        if ( readback ):
            self.sendMessage(MSG_JDW, JDW_SHIFT_RW, num_bits, int(last))
        else:
            self.sendMessage(MSG_JDW, JDW_SHIFT_W, num_bits, int(last))

        done = defer.Deferred()

        def fail(ex_data):
            done.errback(ex_data)

        def success((command, parameter)):
            assert command == MSG_OK
            if ( readback ):
                if ( len(self.direct_shift_data) < ( num_bits / 8 )):
                    raise VlabException(
                            "Insufficient data was returned")

                out = []
                for i in xrange(( num_bits + 7 ) / 8):
                    out.append(self.direct_shift_data.popleft())

                done.callback(''.join(out))
            else:
                done.callback(True)

        def stage2((command, parameter)):
            assert command == MSG_OK
            self.direct_shift_data.clear()
            self.write(data)
            self.awaitEndMessage().addCallback(success).addErrback(fail)

        self.awaitEndMessage().addCallback(stage2).addErrback(fail)
        return done

    def ClJdwData(self, hexdata):
        for i in xrange(0, len(hexdata), 2):
            slice = hexdata[ i : i + 2 ]
            try:
                byte = int(slice, 16)
            except:
                return
            self.direct_shift_data.append(chr(byte))
        
    # Protocol level Cl* functions that aren't associated
    # with a particular type of request.
    def ClGetEmbeddedVersion(self, version, english):
        self.version.embedded = version
        
    def ClGetRelayVersion(self, version, english):
        self.version.relay = version

    def ClGetMutexDaemonVersion(self, version, english):
        self.version.mutexdaemon = version

    def ClOk(self, p0):
        self.reply().callback((MSG_OK, p0))

    def ClEndList(self):
        self.reply().callback((MSG_ENDLIST, ''))

    def ClUsingUart(self):
        self.reply().callback((MSG_USINGUART, ''))
        
    def ClJdwFail(self, p0):
        self.reply().callback((MSG_JDWFAIL, p0))
        
    def ClReceiveWall(self, *args):
        pass
        
    def ClError(self, name, server_message=""): 
        if ( name == ERR_DISCONNECT ):
            return
        my_message = ERR_TABLE.get(name, ERR_TABLE[ ERR_UNKNOWN ])
        self.reply().errback(VlabErrorException(name,
                my_message))

    # Twisted level and data level functions
    def channelOpen(self, data):
        """Internal function called when the SSH connection is available."""
        self.conn.sendRequest(self, 'exec', common.NS('-no-shell-'),
                                  wantReply = 1)
        self.message_buffer = collections.deque()
        self.stage = VLAB_RELAY_SERVER
        if ( self.register_fn != None ):
            self.register_fn(self)
            reactor.callLater(1.0, self.keepAlive)

    def assertProper(self, caller):
        assert self.stage == VLAB_BOARD_SERVER, (
            "%s is only possible when connected to a board server." % caller)

    def awaitEndMessage(self):
        """Creates a Deferred that will be called when an "end" message
        is received. 
        
        "end" messages indicate that a command has been processed by the
        server. The "end" messages are: ok, endlist, usinguart, error."""
        d = self.getDeferred()
        self.reply_queue.append(d)
        return d

    def getDeferred(self):
        return defer.Deferred()

    def reply(self):
        return self.reply_queue.popleft()

    def dataReceived(self, buffer):
        """Internal function; called when data is received via SSH."""
        if ( self.stage == VLAB_UART ):
            self.uart_protocol.dataReceived(buffer)
            return

        vlabProcessMessage(handler_class=self, 
                    message_table=VL_MSG_TABLE, 
                    new_data=buffer,
                    message_buffer=self.message_buffer, 
                    debug=self.debug)

    def closed(self):
        """Internal function; called when the SSH connection is closed."""
        self.stage = VLAB_NOT_CONNECTED

    def closeReceived(self):
        """Internal function; called as the SSH connection is closed
        by the other end."""
        self.loseConnection()

    def keepAlive(self):
        if ( self.stage in (VLAB_RELAY_SERVER, VLAB_BOARD_SERVER) ):
            if ( self.keep_alive_start == None ):
                self.keep_alive_start = time.time()
            self.sendMessage(MSG_REM, "keep alive %us\n" %
                                int(time.time() - self.keep_alive_start))
            reactor.callLater(TIMEOUT_FOR_KEEPALIVE, self.keepAlive)
        
    def connectionLost(self, reason):
        if ( self.stage == VLAB_UART ):
            self.uart_protocol.connectionLost()

    def initData(self, debug):
        self.reply_queue = collections.deque()
        self.message_buffer = collections.deque()
        self.userinfo_table = dict()
        self.version = VlabVersion()
        self.stage = VLAB_NOT_CONNECTED
        self.stored_bid = None
        self.uart_protocol = None
        self.register_fn = None
        self.debug = debug
        self.direct_shift_data = collections.deque()
        self.keep_alive_start = None

    def sendMessage(self, tag, *parameters):
        """Internal function; sends a message to the relay shell or board
        server."""
        if ( not ( self.stage in (VLAB_RELAY_SERVER, VLAB_BOARD_SERVER) )):
            return

        msg = '%s %s\n' % (tag, ' '.join([ str(p) for p in parameters]))
        if ( self.debug ):
            print 'VIRTUAL LAB Send: "%s"' % msg.strip()
        self.write(msg)


class VlabUARTProtocol(protocol.Protocol):
    """A Protocol for communication with a virtual lab UART.

    This protocol automatically buffers incoming data, so you can
    wait for data using:
    >>> data = yield vlabuart.read(10)
    Outgoing data is not buffered, so you can send data using:
    >>> vlabuart.write(data)
    """

    def __init__(self):
        self.total_bytes = 0
        self.read_buffer = collections.deque()
        self.consumers_waiting = collections.deque()

    def dataReceived(self, data):
        """This method is called automatically when data is
        received."""

        assert type(data) == str
        received_nbytes = len(data)
        if ( received_nbytes == 0 ):
            return

        # Record newly received data
        self.read_buffer.append(data)
        self.total_bytes += received_nbytes
        self.consume()

    def consume(self):
        """Send received data to consumers, if possible."""
        if ( len(self.consumers_waiting) == 0 ):
            return

        # Send data to the first consumer if there is
        # enough data to satisfy the request
        (d, request_nbytes) = self.consumers_waiting[ 0 ]
        if ( self.total_bytes < request_nbytes ):
            return

        self.consumers_waiting.popleft()
        # Fill output buffer from read buffer
        out = []
        reply_nbytes = 0
        while ( reply_nbytes < request_nbytes ):
            part = self.read_buffer.popleft()
            self.total_bytes -= len(part)
            reply_nbytes += len(part)
            out.append(part)
            assert self.total_bytes >= 0

        # The buffer provided more bytes than the request
        # asked for - fix this.
        if ( reply_nbytes > request_nbytes ):
            keep = reply_nbytes - request_nbytes
            part = out.pop()
            part1 = part[ : - keep ]
            part2 = part[ - keep : ]
            out.append(part1)
            self.read_buffer.appendleft(part2)
            self.total_bytes += len(part2)

        # Callback
        d.callback(''.join(out)) 

    def makeConnection(self, transport):
        """This method is called automatically when a connection
        is made."""
        self.transport = transport
        self.discard()
        
    def write(self, data):
        """Write data to the virtual lab UART."""
        self.transport.write(data)

    def flush(self):
        """Ignored: exists for compatibility."""
        pass

    def read(self, request_nbytes):
        """Read a number of bytes from the virtual lab UART.

        Returns a Deferred. The callback will be called with a
        string containing the specified number of bytes,
        once they are received."""

        d = defer.Deferred()
        self.consumers_waiting.append((d, request_nbytes))
        self.consume()
        return d

    def discard(self):
        """Discards incoming data buffer contents."""
        self.total_bytes = 0
        self.read_buffer.clear()

class VlabClientFactory(protocol.ClientFactory):
    """Create new virtual lab clients.

    The purpose of this class is to connect to a virtual lab
    service (a relay shell, to be exact) and then provide access
    to the VlabChannel object that has been created during the
    connection. The VlabChannel object is used to interact with
    the relay shell and board server.

    A typical usage of VlabClientFactory is as follows:
    >>> factory = VlabClientFactory(VlabAuthorisation())
    >>> factory.getChannel().addCallback(GotChannel).addErrback(Fail)
    >>> reactor.connectTCP('foobar.cs.york.ac.uk', 22, factory)
    In this example, your method GotChannel will be called when the
    channel is available. The errback function Fail is called if there
    is a connection error.

    An alternative usage allows connections via SOCKS.
    >>> factory = vlab.VlabClientFactory(auth_data=VlabAuthorisation(),
            socks_connect_address='foobar.cs.york.ac.uk',
            socks_connect_port=22)
    >>> factory.getChannel().addCallback(GotChannel).addErrback(Fail)
    >>> reactor.connectTCP('socksserver.example.com', 1080, factory)
    In this example, the connection is made via the SOCKS service
    at socksserver.example.com. 
    """

    def __init__(self, auth_data, socks_connect_address=None,
                socks_connect_port=None, debug=False):
        assert isinstance(auth_data, VlabAuthorisation)
        self.auth_data = auth_data
        self.vlab_channel = None
        self.vlab_channel_get_queue = []
        self.socks_connect_address = socks_connect_address
        self.socks_connect_port = socks_connect_port
        self.debug = debug
        self.first_failure = None
        self.connector_list = []
        self.transport_list = []

    def startedConnecting(self, connector):
        self.connector_list.append(connector)

    def abort(self):
        """Abort all scheduled connection attempts."""
        while ( len(self.connector_list) != 0 ):
            cl = self.connector_list.pop()
            try:
                cl.stopConnecting()
            except:
                pass
        while ( len(self.transport_list) != 0 ):
            t = self.transport_list.pop()
            try:
                t.abort()
            except:
                pass

    def clientConnectionLost(self, connector, reason):
        self.failHandler(reason)

    def clientConnectionFailed(self, connector, reason):
        self.failHandler(reason)

    def buildProtocol(self, addr):
        t = VlabClientTransport(
                    register_fn=self.registerVlabChannel, 
                    auth_data=self.auth_data,
                    fail_fn=self.failHandler,
                    debug=self.debug,
                    socks_connect_address=self.socks_connect_address, 
                    socks_connect_port=self.socks_connect_port)
        self.transport_list.append(t)
        return t

    def failHandler(self, ex_data):
        if ( self.first_failure != None ):
            self.first_failure = ex_data
        while ( len(self.vlab_channel_get_queue) != 0 ):
            d = self.vlab_channel_get_queue.pop()
            d.errback(ex_data)

    def registerVlabChannel(self, vlab_channel):
        assert isinstance(vlab_channel, VlabChannel)
        self.vlab_channel = vlab_channel
        while ( len(self.vlab_channel_get_queue) != 0 ):
            d = self.vlab_channel_get_queue.pop()
            d.callback(self.vlab_channel)

    def getFirstFailure(self):
        return self.first_failure

    def getChannel(self):
        """Get the VlabChannel object representing the connection.

        Returns a Deferred. The callback will be called with the VlabChannel
        object."""

        if ( self.first_failure != None ):
            return defer.failure(self.first_failure)
        elif ( self.vlab_channel != None ):
            return defer.success(self.vlab_channel)
        else:
            d = defer.Deferred()
            self.vlab_channel_get_queue.append(d)
            return d

SOCKS_ERROR_TABLE = {
'\x01': "General SOCKS server failure",
'\x02': "Connection denied by rule",
'\x03': "Network unreachable",
'\x04': "Host unreachable",
'\x05': "Connection refused",
'\x06': "TTL Expired",
'\x07': "Command not supported",
'\x08': "Address type not supported",
}
[ SOCKS_PASS, SOCKS_GREETING, 
        SOCKS_CONNECTING, SOCKS_FAILED ] = range(4)

class VlabClientTransport(transport.SSHClientTransport):
    """Client transport for virtual lab services.

    Instances of this class are created by VlabClientFactory objects."""

    def __init__(self, register_fn, fail_fn, auth_data, debug,
                socks_connect_address, socks_connect_port):
        self.auth_data = auth_data
        self.register_fn = register_fn 
        self.fail_fn = fail_fn 
        self.debug = debug
        self.socks_connect_address = socks_connect_address
        self.socks_connect_port = socks_connect_port
        self.socks_received = collections.deque()
        if ( self.socks_connect_address == None ):
            self.socks_state = SOCKS_PASS
            self.socks_request = ""
        else:
            self.socks_state = SOCKS_GREETING
            self.socks_request = self.getSocksRequest()


    def getSocksRequest(self):
        socks_request = [
            0x5, # SOCKSv5
            0x1, # establish a TCP/IP stream connection
            0x0, # reserved (0)
            0x3, # address type = domain name
            len(self.socks_connect_address) ]

        # address 
        for ch in self.socks_connect_address:
            socks_request.append(ord(ch))

        # big-endian port number
        socks_request.append(( self.socks_connect_port >> 8 ) & 0xff)
        socks_request.append(self.socks_connect_port & 0xff)
        return ''.join([ chr(ch) for ch in socks_request ])

    def verifyHostKey(self, pubKey, fingerprint):
        host_key = self.auth_data.relay_server_host_key.split()[ 1 ]
        if ( fingerprint != host_key ):
            return defer.fail(error.ConchError('bad key'))
        else:
            return defer.succeed(1)

    def connectionSecure(self):
        self.requestService(VlabClientUserAuth(self.auth_data,
                VlabClientSSHConnection(self.register_fn, 
                    self.fail_fn, self.debug)))

    def dataReceived(self, bytes):
        if ( self.socks_state == SOCKS_PASS ):
            transport.SSHClientTransport.dataReceived(self, bytes)
        else:
            if ( self.debug ):
                print 'receive bytes %s' % repr(bytes)
            for ch in bytes:
                self.socks_received.append(ch)

            if (( self.socks_state == SOCKS_GREETING )
            and ( len(self.socks_received) >= 2 )):
                if ( self.debug ):
                    print "that's a greeting"
                ver = self.socks_received.popleft()
                method = self.socks_received.popleft()
                if (( ver != '\x05' )
                or ( method != '\x00' )):
                    self.socks_state = SOCKS_FAILED
                    self.fail_fn(ConnectException(
                        "SOCKS server isn't version 5."))
                self.transport.write(self.socks_request)
                self.socks_state = SOCKS_CONNECTING

            elif (( self.socks_state == SOCKS_CONNECTING )
            and ( len(self.socks_received) >= 5 )):
                if ( self.debug ):
                    print "that's a connect message"
                result = self.socks_received[ 1 ]
                if ( result != '\x00' ):
                    self.socks_state = SOCKS_FAILED
                    self.fail_fn(ConnectException(
                        "SOCKS connection failed: %s" % 
                        SOCKS_ERROR_TABLE.get(result, 
                            'Unknown error %u' % ord(result))))
                address_type = ord(self.socks_received[ 3 ])
                if ( address_type == 1 ):
                    # IPv4 address
                    # Packet size includes 4 bytes of IPv4
                    packet_size = 4 + 4 + 2
                elif ( address_type == 3 ):
                    # Domain name
                    packet_size = ( 4 + 
                            1 + ord(self.socks_received[ 4 ]) + 2 ) 
                else:
                    self.fail_fn(ConnectException(
                        "SOCKS: Unknown address type %u" % address_type))
               
                if ( len(self.socks_received) >= packet_size ):
                    if ( self.debug ):
                        print "that's completion"
                    for i in xrange(packet_size):
                        self.socks_received.popleft()

                    # Now the SSH connection is possible
                    transport.SSHClientTransport.connectionMade(self)
                    transport.SSHClientTransport.dataReceived(self, 
                            ''.join(self.socks_received))

                    self.socks_received.clear()
                    self.socks_state = SOCKS_PASS
            else:
                print 'Received something else from socks!'

    def socksFail(self):
        if (( self.socks_state != SOCKS_PASS )
        and ( self.socks_state != SOCKS_FAILED )):
            if ( self.debug ):
                print 'SOCKS timeout'

            self.fail_fn(ConnectException(
                "SOCKS: Connection to remote host failed"))

            try:
                self.transport.loseConnection()
            except:
                pass

    def abort(self):
        self.socksFail()

    def connectionMade(self):
        if ( self.socks_state == SOCKS_PASS ):
            if ( self.debug ):
                print 'Connection made'
            transport.SSHClientTransport.connectionMade(self)
        elif ( self.socks_state == SOCKS_GREETING ):
            # Greeting: no authentication methods supported
            if ( self.debug ):
                print 'send greeting'
            self.transport.write('\x05\x01\x00')
            reactor.callLater(TIMEOUT_FOR_SOCKS, self.socksFail)

    def connectionLost(self, reason):
        if ( self.socks_state == SOCKS_CONNECTING ):
            # SOCKS server must have closed the connection,
            # apparently without sending an error code.
            self.fail_fn(ConnectException(
                    "SOCKS: Server closed connection - DNS error?"))
        else:
            self.fail_fn(reason)
        self.socks_state = SOCKS_FAILED
        transport.SSHClientTransport.connectionLost(self, reason)

    def receiveError(self, reasonCode, desc):
        # This happens if authentication fails.
        self.socks_state = SOCKS_FAILED
        self.fail_fn(ConnectException("SSH: (%s) %s" % (reasonCode, desc)))
        transport.SSHClientTransport.receiveError(self, reasonCode, desc)

    def sendDisconnect(self, reasonCode, desc):
        # This happens if the host key doesn't match.
        # It happens before connectionLost.
        self.fail_fn(ConnectException("SSH: (%s) %s" % (reasonCode, desc)))
        transport.SSHClientTransport.sendDisconnect(self, reasonCode, desc)



class VlabClientUserAuth(userauth.SSHUserAuthClient):
    """Authentication for virtual lab services.

    Instances of this class are created by VlabClientTransport objects."""

    def __init__(self, auth_data, *args1, **args2):
        self.auth_data = auth_data
        userauth.SSHUserAuthClient.__init__(self, 
                    self.auth_data.user_name, *args1, **args2)

    def getPassword(self, prompt=None):
        return # this says we won't do password authentication

    def getPublicKey(self):
        try:
            # For use with recent versions of Conch
            return self.__getKey().public().blob()
        except AttributeError:
            # For old versions
            return keys.getPublicKeyString(data=self.auth_data.public_key)

    def getPrivateKey(self):
        try:
            # For use with recent versions of Conch
            return defer.succeed(self.__getKey().keyObject)
        except AttributeError:
            # For old versions
            return defer.succeed(keys.getPrivateKeyObject(
                                    data=self.auth_data.private_key))

    def __getKey(self):
        return keys.Key.fromString(data=self.auth_data.private_key)

class VlabClientSSHConnection(connection.SSHConnection):
    """SSHConnection for virtual lab services.

    Instances of this class are created by VlabClientTransport objects."""

    def __init__(self, register_fn, fail_fn, debug, *args1, **args2):
        self.register_fn = register_fn
        self.debug = debug
        self.fail_fn = fail_fn
        connection.SSHConnection.__init__(self, *args1, **args2)

    def serviceStarted(self):
        connection.SSHConnection.serviceStarted(self)
        self.openChannel(VlabChannel(register_fn=self.register_fn, 
                fail_fn=self.fail_fn,
                debug=self.debug, conn=self))

    def serviceStopped(self):
        connection.SSHConnection.serviceStopped(self)
        self.fail_fn(ConnectException("SSH service stopped"))


