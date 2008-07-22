# 
#  vlab.py
# 
#  Source code for non-generated parts of virtual lab module.
# 
#  Author: Jack Whitham
#  RCS: $Id: vlab.py,v 1.1 2008/07/22 12:14:55 jwhitham Exp $
#  

import fcntl, os, popen2, time, atexit, signal, zlib
from vlaberr import *
from vlabmsg import *

CVERSION = '0.1'
SHORT_TIMEOUT = 0.1

class VlabException(Exception):
    pass

class VlabConnectionException(VlabException):
    pass

class VlabErrorException(VlabException):
    def __init__(self, errorcode, reason):
        VlabException.__init__(self, reason)
        self.errorcode = errorcode

class VlabBitfileException(Exception):
    pass

[ BINFO_UNUSED , BINFO_INVALID , BINFO_INCOMPLETE , BINFO_READY,
        BINFO_WRONG_FPGA, UINFO_IN_USE, UINFO_OFFLINE,
        UINFO_AVAILABLE, UINFO_UNKNOWN,
        VLAB_NOT_CONNECTED, VLAB_BOARD_SERVER, 
        VLAB_RELAY_SERVER ] = range(12)

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

    def Lowest(self):
        d = [ x for x in [ self.client, self.relay, 
                    self.embedded ] if x != None ]
        d.sort()
        return d[ 0 ]

    def __str__(self):
        return str(self.Lowest())
        


class VlabPlatform:
    def __init__(self):
        self.board_info = None
        self.fpga_count = 0
        self.driver_name = None
        self.fpga_name = None

class VlabConn(VlabBase):
    def __init__(self):
        self.message_buffer = []
        self.send_fd = None
        self.receive_fd = None
        self.ssh = None
        self.debug = False
        self.timeout = 10.0
        atexit.register(self.Disconnect)

    def Connect(self, relay_server, user_name, ssh_port=None, 
                    private_key_file=None):

        cmd_list = [ 'ssh' ]
        if ( ssh_port != None ):
            cmd_list.extend(['-p', '%u' % ssh_port])
        if ( private_key_file != None ):
            cmd_list.extend(['-i', private_key_file])
        cmd_list.extend(['-l', user_name ])
        cmd_list.append(relay_server)

        if ( self.debug ):
            print 'Running command', cmd_list

        self.Disconnect()
        self.ssh = popen2.Popen3(cmd_list)
        self.receive_fd = self.ssh.fromchild
        self.send_fd = self.ssh.tochild
        self.__SetBlocking(False)
        self.SendMessage(MSG_REM, "Client version %s connected." % 
                    CVERSION)
        self.ReceiveMessage()

    def Disconnect(self):
        if ( self.receive_fd != None):
            try:
                self.receive_fd.close()
            except:
                pass

        if ( self.send_fd != None ):
            try:
                self.send_fd.close()
            except:
                pass

        if ( self.ssh != None ):
            try:
                os.kill(self.ssh.pid, signal.SIGTERM)
                time.sleep(0.05)
                os.kill(self.ssh.pid, signal.SIGTERM)
                time.sleep(0.05)
                os.kill(self.ssh.pid, signal.SIGKILL)
            except:
                pass

            self.ssh.wait()

        self.receive_fd = self.send_fd = self.ssh = None

    def __SetBlocking(self, blocking):
        b = os.O_NONBLOCK
        if ( blocking ):
            b = 0
        fcntl.fcntl(self.receive_fd.fileno(), fcntl.F_SETFL, b)

    def UartMode(self, blocking):
        self.__SetBlocking(blocking)
        out = (self.receive_fd, self.send_fd)
        self.receive_fd = self.send_fd = None
        return out

    def __CheckConn(self):
        if (( None in (self.receive_fd, self.send_fd) )
        or self.receive_fd.closed
        or self.send_fd.closed 
        or ( self.ssh == None )):
            raise VlabConnectionException("Not connected.")

    def SendMessage(self, tag, *parameters):
        self.__CheckConn()
        msg = '%s %s\n' % (tag, ' '.join([ str(p) for p in parameters]))

        if ( self.debug ):
            print 'Send', msg

        try:
            self.send_fd.write(msg)
            self.send_fd.flush()
        except IOError:
            raise VlabConnectionException("Sending message failed.")

    def ReceiveMessage(self):

        # Receive everything available now
        rx = self.__ReceiveMessage()
        t = self.timeout
        while (( not rx ) and ( t >= 0.0 )):
            # Wait for timeout, try again
            time.sleep(SHORT_TIMEOUT)
            rx = self.__ReceiveMessage()
            t -= SHORT_TIMEOUT

        # Empty buffer if something was received
        while ( rx ):
            rx = self.__ReceiveMessage()
        
    def __ReceiveMessage(self):
        self.__CheckConn()

        try:
            buffer = self.receive_fd.read(512)
        except IOError:
            buffer = ''

        for ch in buffer:
            if ( ch in "\r\n" ):
                self.__ProcessMessage(''.join(self.message_buffer))
                self.message_buffer = []
            elif ( ch in '\x08\x7f' ):
                if ( len(self.message_buffer) != 0 ):
                    self.message_buffer.pop()
            else:
                if ( len(self.message_buffer) < 1024 ):
                    self.message_buffer.append(ch)

        return ( len(buffer) != 0 )

    def __ProcessMessage(self, msg):    
        if ( self.debug ):
            print 'Receive', msg

        fields = msg.split()
        if ( len(fields) == 0 ):
            return False

        command = fields[ 0 ].lower()
        tbl_entry = MSG_TABLE.get(command, None)
        if ( tbl_entry == None ):
            return False

        get_all_remaining = tbl_entry[ 0 ]
        if ( get_all_remaining ):
            # Resplit - put all of final parameter into one string.
            fields = msg.split(None, len(tbl_entry) - 2)

        if ( self.debug ):
            print 'Received fields: ', fields

        handler_fn = getattr(self, tbl_entry[ 1 ], None)
        assert ( handler_fn != None )
        params = []

        for (value, conv_fn) in zip(fields[ 1: ], tbl_entry[ 2: ]):
            params.append(conv_fn(value))

        handler_fn(*params)
        return True

class Version: pass

class Vlab(VlabConn):
    def __init__(self):
        VlabConn.__init__(self)
        self.result = ERR_NONE
        self.version = VlabVersion()
        self.platform = VlabPlatform()
        self.userinfo_table = dict()
        self.bitinfo_table = dict()
        self.stage = VLAB_NOT_CONNECTED

    def SendCommand(self, tag, *parameters):
        self.SendMessage(tag, *parameters)
        self.result = ERR_NORESP
        self.ReceiveMessage()
        if ( self.result != ERR_NONE ):
            raise VlabErrorException(self.result, self.FindError(self.result))

    def FindError(self, name):
        return ERR_TABLE.get(name, ERR_TABLE[ ERR_UNKNOWN ])

    def UseUart(self, uart_number, blocking):
        self.SendCommand(MSG_USEUART, uart_number)
        return self.UartMode(blocking)

    def SetUart(self, uart_number, baud):
        self.SendCommand(MSG_SETUART, uart_number, baud)

    def SendBitfile(self, bits, compress_level=9):
        # Begin by compressing the bit file
        # (You might be sending the file over a slow network connection)
        bits = zlib.compress(bits, compress_level)
        size_bits = len(bits) * 8

        self.load_bid = None
        self.load_ok = None
        # See ClLoadReady
        self.SendCommand(MSG_LOADBITS, size_bits)
        if ( self.load_bid == None ):
            raise VlabErrorException(self.result, 
                            "Bitfile id (BID) was not received.")
        bid_copy = self.load_bid
        try:
            self.send_fd.write(bits)
            self.send_fd.flush()
        except IOError:
            raise VlabConnectionException("Sending bitstream data failed.")

        # Null comment - should get us a Loaded acknowledgement
        self.SendCommand(MSG_REM)

        # What was the result? See ClLoaded
        if ( self.load_ok == None ):
            raise VlabConnectionException("Load completion "
                    "message did not arrive.")
        elif ( bid_copy != self.load_bid ):
            raise VlabErrorException(ERR_UNKNOWN,
                            "Bitfile id (BID) has changed!")
        elif ( not self.load_ok ):
            raise VlabBitfileException("Bitfile did not validate.")
            
        return bid_copy

    def ProgramFPGA(self, fpga_num, bid):
        # Schedule programming
        self.load_bid = bid
        self.SendCommand(MSG_PROGRAM, fpga_num, bid)

        # Wait for a response - this might take longer than
        # self.timeout because (1) the FPGA might take ages to
        # program, and (2) there might be a whole lot of other
        # things in the queue
        self.result = ERR_NORESP
        while ( self.result == ERR_NORESP ):
            self.SendMessage(MSG_REM)
            self.ReceiveMessage()

        if ( self.result != ERR_NONE ):
            raise VlabErrorException(self.result, self.FindError(self.result))

    def GetBitInfo(self):
        self.bitinfo_table = dict()
        self.SendCommand(MSG_SHOWBITS)

        out = []
        i = 0
        while ( self.bitinfo_table.has_key(i) ):
            out.append(self.bitinfo_table[ i ])
            i += 1
        return out

    def GetUserInfo(self, board_name):
        if ( self.stage != VLAB_RELAY_SERVER ):
            raise VlabException("This command only works while you "
                "are connected to the relay server (but not a board).")
        self.userinfo_table = dict()
        self.SendCommand(MSG_SHOWBITS, board_name)

    def ClUsingUart(self):
        self.result = ERR_NONE

    def ClOk(self, explanation):
        self.result = ERR_NONE

    def ClEndList(self):
        self.result = ERR_NONE

    def ClError(self, shortname, explanation):
        self.result = shortname

    def ClWall(self, english):
        print 'Wall received:', english
        
    def ClGetEmbeddedVersion(self, version, english):
        print 'Board version:', version, english
        self.version.embedded = version
        
    def ClGetRelayVersion(self, version, english):
        print 'Relay server version:', version, english
        self.version.relay = version

    def ClReceiveBoardInfo(self, english):
        print 'Board:', english
        self.platform.board_info = english

    def ClReceiveFPGAInfo(self, fpga_count, driver_name, fpga_name):
        self.platform.fpga_count = fpga_count
        self.platform.driver_name = driver_name
        self.platform.fpga_name = fpga_name

    def ClLoadReady(self, bid, num_bits, english):
        self.result = ERR_NONE
        self.load_bid = bid

    def ClLoaded(self, bid, ok):
        self.result = ERR_NONE
        self.load_bid = bid
        self.load_ok = ok

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

        
    def Connect(self, relay_server, board_name=None, ssh_port=None, 
                    user_name=None, private_key_file=None):
        VlabConn.Connect(self, relay_server=relay_server,
                ssh_port=ssh_port, user_name=user_name, 
                private_key_file=private_key_file)

        self.stage = VLAB_RELAY_SERVER
        if ( board_name != None ):
            self.ConnectBoard(board_name)

    def Disconnect(self):
        VlabConn.Disconnect(self)
        self.stage = VLAB_NOT_CONNECTED

    def ConnectBoard(self, board_name):
        if ( self.stage != VLAB_RELAY_SERVER ):
            raise VlabException("Connect() to a relay server first.")

        retry = True
        while ( retry and self.stage == VLAB_RELAY_SERVER ):
            retry = False
            try:
                self.SendCommand(MSG_CONNECT, board_name)
                self.stage = VLAB_BOARD_SERVER # finished

            except VlabErrorException, e:
                if ( e.errorcode == ERR_UNAVAILABLE ):
                    # try again - there may be more than one instance
                    # of this board, and another might be available.
                    # we will see ERR_BUSY when we run out of boards.
                    retry = True
                else:
                    raise e


    def ClProgramOk(self, bid):
        # Check bid - very important to do this because
        # other ProgramOk messages might get through
        if ( bid == self.load_bid ):
            self.result = ERR_NONE

    def ClProgramFailed(self, bid, shortname):
        # Check bed - see ClProgramOk
        if ( bid == self.load_bid ):
            self.result = shortname
        
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



