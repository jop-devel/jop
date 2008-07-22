"""
Virtual Lab Client
Twisted Python interface
 
In order to use this module, you will need to understand 
Deferreds, a concept which is explained in the Twisted 
Python documentation. 

Version $Id: vlab.py,v 1.2 2008/07/22 15:41:15 jwhitham Exp $
"""

from twisted.conch import error
from twisted.conch.ssh import transport
from twisted.internet import defer
from twisted.python import log 
from twisted.internet import protocol, reactor
from twisted.conch.ssh import keys, userauth
from twisted.conch.ssh import connection
from twisted.conch.ssh import channel, common
import sys, collections, zlib, os, base64, pickle

from vlabmsg import *

CVERSION = '0.1'

class VlabException(Exception):
    """All virtual lab exceptions inherit this."""
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
    supplied to the SendBitfile method. 

    In this case, "invalid" means that the bit file did not
    pass validation on the board server, e.g. it was made for the
    wrong FPGA type, or it was truncated, or it didn't have a valid
    header."""
    pass

[ BINFO_UNUSED , BINFO_INVALID , BINFO_INCOMPLETE , BINFO_READY,
        BINFO_WRONG_FPGA, UINFO_IN_USE, UINFO_OFFLINE,
        UINFO_AVAILABLE, UINFO_UNKNOWN,
        VLAB_NOT_CONNECTED, VLAB_BOARD_SERVER, 
        VLAB_RELAY_SERVER, VLAB_UART, VLAB_UNKNOWN ] = range(14)

class VlabBitInfo:
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
    def __init__(self):
        self.client = CVERSION
        self.relay = None
        self.embedded = None
        self.mutexdaemon = None

    def Lowest(self):
        d = [ x for x in [ self.client, self.relay, 
                    self.embedded, self.mutexdaemon ] if x != None ]
        d.sort()
        return d[ 0 ]

    def __str__(self):
        return str(self.Lowest())

class VlabAuthorisation:
    def __init__(self):
        self.relay_server_host_key = None
        self.user_name = None
        self.public_key = None
        self.private_key = None
        self.relay_server_name = None
        self.version = CVERSION

AUTH_LEADIN = '--- BEGIN VIRTUAL LAB AUTHORISATION DATA ---'
AUTH_LEADOUT = '--- END VIRTUAL LAB AUTHORISATION DATA ---'

def SaveAuthorisation(auth, fname):
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

def LoadAuthorisation(fname):
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

class VlabChannel(channel.SSHChannel):
    """A Protocol for interacting with virtual lab services.
    
    Instances of this class are created by VlabClientSSHConnection objects."""

    name = 'session'

    def __init__(self, register_fn=None, debug=False, **args2):
        """Create a new VlabChannel with the specified register_fn.
        
        This __init__ method is normally called from the SSHConnection
        serviceStarted method."""
        channel.SSHChannel.__init__(self, **args2)
        self.InitData(debug)
        self.register_fn = register_fn

    # Requests and their protocol level (Cl*) functions.
    def GetBoardUserInfo(self, board_name):
        """Request a list of users of the specified board_name.

        Returns a Deferred. The callback will be called with a list
        of VlabUserInfo objects, one for each instance of board_name."""
        self.userinfo_table = dict()
        self.SendMessage(MSG_USERREQUEST, board_name)
        assert self.stage == VLAB_RELAY_SERVER, (
            "This is only possible when connected to the relay shell." )

        def Transform((command, parameters)):
            assert command == MSG_ENDLIST
            out = []
            i = 0
            while ( self.userinfo_table.has_key(i) ):
                out.append(self.userinfo_table[ i ])
                i += 1
            return out

        return self.AwaitEndMessage().addCallback(Transform)

    def ClUserInfo(self, index, valid, user_name, lock_count):
        try:
            uinfo = self.userinfo_table[ index ]
        except KeyError:
            uinfo = self.userinfo_table[ index ] = VlabUserInfo()

        uinfo.lock_count = lock_count
        uinfo.user_name = None
        if ( valid ):
            uinfo.user_name = user_name
            uinfo.status = UINFO_IN_USE
        elif ( user_name == "offline" ):
            uinfo.status = UINFO_OFFLINE
        else:
            uinfo.status = UINFO_AVAILABLE

    def GetBitInfo(self):
        """Request a list of bit file buffers on the board server.

        Returns a Deferred. The callback will be called with a list
        of VlabBitInfo objects, one for each bit file buffer."""
        self.AssertProper("GetBitInfo")
        self.bitinfo_table = dict()
        self.SendMessage(MSG_SHOWBITS)

        def Transform((command, parameters)):
            assert command == MSG_ENDLIST
            out = []
            i = 0
            while ( self.bitinfo_table.has_key(i) ):
                out.append(self.bitinfo_table[ i ])
                i += 1
            return out

        return self.AwaitEndMessage().addCallback(Transform)

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

    def SetUART(self, uart_number, baud):
        """Set the baud rate for the specified UART.

        Returns a Deferred. The callback will be called with the OK
        message. The baud rate can be any of the standard RS232 baud rates,
        e.g. 9600, 38400, 115200."""
        self.AssertProper("SetUART")
        self.SendMessage(MSG_SETUART, uart_number, baud)

        def Ack((command, parameter)):
            assert command == MSG_OK
            return True
            
        return self.AwaitEndMessage().addCallback(Ack)

    def SendBitfile(self, bits, compress_level=9):
        """Send a bit file to the board server.

        Returns a Deferred. The callback will be called with the bid 
        (bit file id), which is an integer. To load the bit file onto an
        FPGA, you must call ProgramFPGA with the bid."""
        self.AssertProper("SendBitfile")

        self.stored_bid = None
        transfer_complete = self.GetDeferred()
        cbits = [ '' ]

        def Fail(ex_data):
            transfer_complete.errback(ex_data)

        # stage 3 - receive MSG_LOADED via ClLoaded;
        # this calls Complete:
        def Complete((command, bid, ok)):
            assert command == MSG_LOADED
            assert self.stored_bid == bid
            if ( ok ):
                transfer_complete.callback(bid)
            else:
                transfer_complete.errback(
                        VlabBitfileException("Bitfile did not validate."))

        # stage 2 - receive MSG_LOADREADY via ClLoadReady;
        # this calls Transfer to send the data:
        def Transfer((command, bid)):
            assert command == MSG_LOADREADY
            self.stored_bid = bid
            self.write(cbits[ 0 ])
            self.AwaitEndMessage().addCallback(Complete).addErrback(Fail)

        # stage 1 - send loadbits message
        # First, compress the bit file (you might be 
        # sending the file over a slow network connection)
        def Start():
            cbits[ 0 ] = zlib.compress(bits, compress_level)
            size_bits = len(cbits[ 0 ]) * 8
            self.SendMessage(MSG_LOADBITS, size_bits)
            self.AwaitEndMessage().addCallback(Transfer).addErrback(Fail)

        Start()
        return transfer_complete

    def ClLoadReady(self, bid, num_bits, english):
        self.Reply().callback((MSG_LOADREADY, bid))

    def ClLoaded(self, bid, ok):
        self.Reply().callback((MSG_LOADED, bid, ok))

    def ProgramFPGA(self, fpga_num, bid):
        """Schedule programming the specified bit file (bid) onto FPGA fpga_num.

        Returns a Deferred. The callback will be called with True
        as its parameter on success. The errback is called on failure."""
        self.AssertProper("ProgramFPGA")

        self.stored_bid = bid
        self.SendMessage(MSG_PROGRAM, fpga_num, bid)
        # This is for the MSG_OK that is sent to say the bit file
        # is in the programming queue.
        self.AwaitEndMessage() 
        # This is for the MSG_PROGRAMOK.
        return self.AwaitEndMessage()

    def ClProgramOk(self, bid):
        # Check bid - very important to do this because
        # other ProgramOk messages might get through
        if ( bid == self.stored_bid ):
            self.Reply().callback(True)

    def ClProgramFailed(self, bid, shortname):
        # Check bid - see ClProgramOk
        if ( bid == self.stored_bid ):
            self.Reply().errback(VlabErrorException(
                    shortname, self.FindError(shortname)))

    def Connect(self, board_name):
        """Connect to a board. This is only possible
        when you have a connection to a relay shell.
        
        Returns a Deferred. The callback will be called when the
        connection is made; its parameter will be True."""
        if ( self.stage != VLAB_RELAY_SERVER ):
            raise VlabException("Connect() to a relay shell first.")

        output = self.GetDeferred()

        def Success((command, parameters)):
            assert command == MSG_OK
            self.stage = VLAB_BOARD_SERVER
            self.SendMessage(MSG_REM, 'Client "%s" on %s %x' %
                    (CVERSION, os.name, sys.hexversion))
            output.callback(True)

        def TryAgain(ex_data):
            if (( isinstance(ex_data.value, VlabErrorException) )
            and ( ex_data.value.errorcode == ERR_UNAVAILABLE )):
                # do try again
                self.SendMessage(MSG_CONNECT, board_name)
                self.AwaitEndMessage().addCallback(Success).addErrback(TryAgain)
            else:
                # don't try again
                output.errback(ex_data)

        self.SendMessage(MSG_CONNECT, board_name)
        self.AwaitEndMessage().addCallback(Success).addErrback(TryAgain)
        return output

    def Disconnect(self):
        """Disconnect from a board or relay server.
        This method doesn't return anything."""
        self.loseConnection()

    def OpenUART(self, uart_num, protocol):
        """Connect the specified Protocol object to UART uart_num.

        Returns a Deferred. The callback will be called with parameter
        True once the action has completed.
        OpenUART is only possible when the channel is connected to a
        board server. It can only be done once. Once it is done,
        other commands are no longer possible. This means you will
        normally need a control connection (for commands) and a
        data connection (for UART data)."""
        self.AssertProper("OpenUART")
        self.SendMessage(MSG_USEUART, uart_num)
        self.stage = VLAB_UNKNOWN

        def Success((command, parameter)):
            assert command == MSG_USINGUART
            self.uart_protocol = protocol
            self.stage = VLAB_UART
            self.uart_protocol.makeConnection(self) # I'm the transport.
            return True

        return self.AwaitEndMessage().addCallback(Success)

    # Protocol level Cl* functions that aren't associated
    # with a particular type of request.
    def ClGetEmbeddedVersion(self, version, english):
        self.version.embedded = version
        
    def ClGetRelayVersion(self, version, english):
        self.version.relay = version

    def ClGetMutexDaemonVersion(self, version, english):
        self.version.mutexdaemon = version

    def ClOk(self, p0):
        self.Reply().callback((MSG_OK, p0))

    def ClEndList(self):
        self.Reply().callback((MSG_ENDLIST, ''))

    def ClUsingUart(self):
        self.Reply().callback((MSG_USINGUART, ''))
        
    def ClError(self, name, server_message): 
        if ( name == ERR_DISCONNECT ):
            return
        my_message = ERR_TABLE.get(name, ERR_TABLE[ ERR_UNKNOWN ])
        self.Reply().errback(VlabErrorException(name,
                my_message))

    # Twisted level and data level functions
    def channelOpen(self, data):
        """Internal function called when the SSH connection is available."""
        self.conn.sendRequest(self, 'exec', common.NS('-no-shell-'),
                                  wantReply = 1)
        self.message_buffer = []
        self.stage = VLAB_RELAY_SERVER
        if ( self.register_fn != None ):
            self.register_fn(self)

    def AssertProper(self, caller):
        assert self.stage == VLAB_BOARD_SERVER, (
            "%s is only possible when connected to a board server." % caller)

    def AwaitEndMessage(self):
        """Creates a Deferred that will be called when an "end" message
        is received. 
        
        "end" messages indicate that a command has been processed by the
        server. The "end" messages are: ok, endlist, usinguart, error."""
        d = self.GetDeferred()
        self.reply.append(d)
        return d

    def GetDeferred(self):
        return defer.Deferred()

    def Reply(self):
        return self.reply.popleft()

    def dataReceived(self, buffer):
        """Internal function; called when data is received via SSH."""
        if ( self.stage == VLAB_UART ):
            self.uart_protocol.dataReceived(buffer)
            return

        for ch in buffer:
            if ( ch in "\r\n" ):
                self.ProcessMessage(''.join(self.message_buffer))
                self.message_buffer = []
            elif ( ch in '\x08\x7f' ):
                if ( len(self.message_buffer) != 0 ):
                    self.message_buffer.pop()
            else:
                if ( len(self.message_buffer) < 1024 ):
                    self.message_buffer.append(ch)

    def closed(self):
        """Internal function; called when the SSH connection is closed."""
        self.stage = VLAB_NOT_CONNECTED

    def connectionLost(self, reason):
        if ( self.stage == VLAB_UART ):
            print 'uart connection is lost :('
            self.uart_protocol.connectionLost(reason)

    def InitData(self, debug):
        self.reply = collections.deque()
        self.message_buffer = []
        self.userinfo_table = dict()
        self.version = VlabVersion()
        self.stage = VLAB_NOT_CONNECTED
        self.stored_bid = None
        self.uart_protocol = None
        self.register_fn = None
        self.debug = debug

    def SendMessage(self, tag, *parameters):
        """Internal function; sends a message to the relay shell or board
        server."""
        assert self.stage in (VLAB_RELAY_SERVER, VLAB_BOARD_SERVER), (
            "SendMessage is only possible when connected." )
        msg = '%s %s\n' % (tag, ' '.join([ str(p) for p in parameters]))
        if ( self.debug ):
            print 'VIRTUAL LAB Send: "%s"' % msg.strip()
        self.write(msg)

    def ProcessMessage(self, msg):
        """Internal function; process a message that has been received via
        SSH."""
        if ( self.debug ):
            print 'VIRTUAL LAB Receive: "%s"' % msg

        fields = msg.split()
        if ( len(fields) == 0 ):
            return False

        command = fields[ 0 ].lower()
        tbl_entry = MSG_TABLE.get(command, None)
        if ( tbl_entry == None ):
            return False

        get_all_remaining = tbl_entry[ 0 ]
        handler_name = tbl_entry[ 1 ]
        if ( get_all_remaining ):
            # Resplit - put all of final parameter into one string.
            fields = msg.split(None, len(tbl_entry) - 2)

        handler_fn = getattr(self, handler_name, None)
        if ( handler_fn == None ):
            raise VlabException('Missing handler function: %s' % (handler_name))
        else:
            if ( self.debug ):
                print 'VIRTUAL LAB calling function: %s' % (handler_name)

        params = []

        for (value, conv_fn) in zip(fields[ 1: ], tbl_entry[ 2: ]):
            params.append(conv_fn(value))

        handler_fn(*params)
        return True

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
        self.Consume()

    def Consume(self):
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
        self.Consume()
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
    >>> factory.GetChannel().addCallback(GotChannel)
    >>> reactor.connectTCP('foobar.cs.york.ac.uk', 22, factory)
    In this example, GotChannel will be called when the
    channel is available. You could also add an errback
    to catch connection errors.
    """

    def __init__(self, auth_data, debug=False):
        assert isinstance(auth_data, VlabAuthorisation)
        self.auth_data = auth_data
        self.vlab_channel = None
        self.vlab_channel_get_queue = []
        self.debug = debug

    def startedConnecting(self, connector):
        pass

    def clientConnectionLost(self, connector, reason):
        pass

    def clientConnectionFailed(self, connector, reason):
        pass

    def buildProtocol(self, addr):
        return VlabClientTransport(self.RegisterVlabChannel, 
                    self.auth_data, self.debug)

    def RegisterVlabChannel(self, vlab_channel):
        assert isinstance(vlab_channel, VlabChannel)
        self.vlab_channel = vlab_channel
        while ( len(self.vlab_channel_get_queue) != 0 ):
            d = self.vlab_channel_get_queue.pop()
            d.callback(self.vlab_channel)

    def GetChannel(self):
        if ( self.vlab_channel != None ):
            return defer.success(self.vlab_channel)
        else:
            d = defer.Deferred()
            self.vlab_channel_get_queue.append(d)
            return d

class VlabClientTransport(transport.SSHClientTransport):
    """Client transport for virtual lab services.

    Instances of this class are created by VlabClientFactory objects."""

    def __init__(self, register_fn, auth_data, debug):
        self.auth_data = auth_data
        self.register_fn = register_fn 
        self.debug = debug

    def verifyHostKey(self, pubKey, fingerprint):
        host_key = self.auth_data.relay_server_host_key.split()[ 1 ]
        if ( fingerprint != host_key ):
            return defer.fail(error.ConchError('bad key'))
        else:
            return defer.succeed(1)

    def connectionSecure(self):
        self.requestService(VlabClientUserAuth(self.auth_data,
                VlabClientSSHConnection(self.register_fn, self.debug)))

    def connectionMade(self):
        transport.SSHClientTransport.connectionMade(self)

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
        return self.__getKey().public().blob()

    def getPrivateKey(self):
        return defer.succeed(self.__getKey().keyObject)

    def __getKey(self):
        return keys.Key.fromString(data=self.auth_data.private_key)

class VlabClientSSHConnection(connection.SSHConnection):
    """SSHConnection for virtual lab services.

    Instances of this class are created by VlabClientTransport objects."""

    def __init__(self, register_fn, debug, *args1, **args2):
        self.register_fn = register_fn
        self.debug = debug
        connection.SSHConnection.__init__(self, *args1, **args2)

    def serviceStarted(self):
        self.openChannel(VlabChannel(register_fn=self.register_fn, 
                debug=self.debug, conn=self))


