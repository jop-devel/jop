
# 
#  Generated file - virtual lab module
#  DO NOT MODIFY THIS GENERATED FILE.
#  
MSG_ACTIVITYINFO = "activityinfo"
MSG_BITINFO = "bitinfo"
MSG_BOARDASSIGN = "boardassign"
MSG_BOARDINFO = "boardinfo"
MSG_BOARDREQUEST = "boardrequest"
MSG_BYE = "bye"
MSG_CHECK = "check"
MSG_CONNECT = "connect"
MSG_ENDLIST = "endlist"
MSG_ERROR = "error"
MSG_EVERSION = "eversion"
MSG_EXIT = "exit"
MSG_FPGAINFO = "fpgainfo"
MSG_HELP = "help"
MSG_INSTANCEOFFLINE = "instanceoffline"
MSG_JDW = "jdw"
MSG_JDWDATA = "jdwdata"
MSG_JDWFAIL = "jdwfail"
MSG_LOADBITS = "loadbits"
MSG_LOADED = "loaded"
MSG_LOADREADY = "loadready"
MSG_LOGOFF = "logoff"
MSG_MVERSION = "mversion"
MSG_OK = "ok"
MSG_PROGRAM = "program"
MSG_PROGRAMFAILED = "programfailed"
MSG_PROGRAMOK = "programok"
MSG_QUIT = "quit"
MSG_RELOADMUTEX = "reloadmutex"
MSG_REM = "rem"
MSG_RVERSION = "rversion"
MSG_SETUART = "setuart"
MSG_SETUID = "setuid"
MSG_SHOWBITS = "showbits"
MSG_USERINFO = "userinfo"
MSG_USERREQUEST = "userrequest"
MSG_USEUART = "useuart"
MSG_USINGUART = "usinguart"
MSG_WALL = "wall"


VL_MSG_TABLE = {
    MSG_ACTIVITYINFO : [ False, "ClReceiveActivityInfo",int,int ],
    MSG_BITINFO : [ True, "ClReceiveBitInfo",int,int,int,str,str,str,str,str ],
    MSG_BOARDINFO : [ True, "ClReceiveBoardInfo",str ],
    MSG_ENDLIST : [ False, "ClEndList" ],
    MSG_ERROR : [ True, "ClError",str,str ],
    MSG_EVERSION : [ True, "ClGetEmbeddedVersion",str,str ],
    MSG_FPGAINFO : [ False, "ClReceiveFPGAInfo",int,str,str ],
    MSG_JDWDATA : [ False, "ClJdwData",str ],
    MSG_JDWFAIL : [ False, "ClJdwFail",str ],
    MSG_LOADED : [ False, "ClLoaded",int,int ],
    MSG_LOADREADY : [ True, "ClLoadReady",int,int,str ],
    MSG_MVERSION : [ True, "ClGetMutexDaemonVersion",str,str ],
    MSG_OK : [ True, "ClOk",str ],
    MSG_PROGRAMFAILED : [ False, "ClProgramFailed",int,str ],
    MSG_PROGRAMOK : [ False, "ClProgramOk",int ],
    MSG_REM : [ False, "Ignore" ],
    MSG_RVERSION : [ True, "ClGetRelayVersion",str,str ],
    MSG_USERINFO : [ False, "ClUserInfo",int,int,str,int ],
    MSG_USINGUART : [ False, "ClUsingUart" ],
    MSG_WALL : [ True, "ClReceiveWall",str ],
}

class VlabBase:
    def ClReceiveActivityInfo(self, p0, p1): pass
    def ClReceiveBitInfo(self, p0, p1, p2, p3, p4, p5, p6, p7): pass
    def ClReceiveBoardInfo(self, p0): pass
    def ClEndList(self): pass
    def ClError(self, p0, p1): pass
    def ClGetEmbeddedVersion(self, p0, p1): pass
    def ClReceiveFPGAInfo(self, p0, p1, p2): pass
    def ClJdwData(self, p0): pass
    def ClJdwFail(self, p0): pass
    def ClLoaded(self, p0, p1): pass
    def ClLoadReady(self, p0, p1, p2): pass
    def ClGetMutexDaemonVersion(self, p0, p1): pass
    def ClOk(self, p0): pass
    def ClProgramFailed(self, p0, p1): pass
    def ClProgramOk(self, p0): pass
    def Ignore(self): pass
    def ClGetRelayVersion(self, p0, p1): pass
    def ClUserInfo(self, p0, p1, p2, p3): pass
    def ClUsingUart(self): pass
    def ClReceiveWall(self, p0): pass


MD_MSG_TABLE = {
    MSG_BOARDREQUEST : [ False, "MdBoardRequest",str ],
    MSG_BYE : [ False, "MdExit" ],
    MSG_ENDLIST : [ False, "MdEndList" ],
    MSG_ERROR : [ True, "MdError",str,str ],
    MSG_EXIT : [ False, "MdExit" ],
    MSG_INSTANCEOFFLINE : [ False, "MdInstanceOffline" ],
    MSG_LOGOFF : [ False, "MdExit" ],
    MSG_OK : [ True, "MdOk",str ],
    MSG_QUIT : [ False, "MdExit" ],
    MSG_RELOADMUTEX : [ False, "MdReloadMutex" ],
    MSG_REM : [ False, "Ignore" ],
    MSG_SETUID : [ False, "MdSetuid",int ],
    MSG_USERINFO : [ False, "MdUserInfo",int,int,str,int ],
    MSG_USERREQUEST : [ False, "MdUserRequest",str ],
}

class MutexDaemonBase:
    def MdBoardRequest(self, p0): pass
    def MdExit(self): pass
    def MdEndList(self): pass
    def MdError(self, p0, p1): pass
    def MdInstanceOffline(self): pass
    def MdOk(self, p0): pass
    def MdReloadMutex(self): pass
    def Ignore(self): pass
    def MdSetuid(self, p0): pass
    def MdUserInfo(self, p0, p1, p2, p3): pass
    def MdUserRequest(self, p0): pass
ERR_TABLE = {}
ERR_NOUART = "nouart"
ERR_TABLE[ ERR_NOUART ] = "Requested UART does not exist."
ERR_BADBAUD = "badbaud"
ERR_TABLE[ ERR_BADBAUD ] = "Requested baud rate is not supported by the hardware."
ERR_BUSY = "busy"
ERR_TABLE[ ERR_BUSY ] = "Requested FPGA board is busy."
ERR_COMMAND = "command"
ERR_TABLE[ ERR_COMMAND ] = "Unsupported command."
ERR_UNKNOWN = "unknown"
ERR_TABLE[ ERR_UNKNOWN ] = "Unknown error."
ERR_DISCONNECT = "disconnect"
ERR_TABLE[ ERR_DISCONNECT ] = "Disconnected."
ERR_UNKNOWNBOARD = "unknownboard"
ERR_TABLE[ ERR_UNKNOWNBOARD ] = "Unknown FPGA board requested."
ERR_UNAVAILABLE = "unavailable"
ERR_TABLE[ ERR_UNAVAILABLE ] = "Requested FPGA board is not online."
ERR_NOSUCHFPGA = "nosuchfpga"
ERR_TABLE[ ERR_NOSUCHFPGA ] = "The FPGA number is invalid."
ERR_CONFIG = "config"
ERR_TABLE[ ERR_CONFIG ] = "Server configuration error."
ERR_NONE = "none"
ERR_TABLE[ ERR_NONE ] = "No error."
ERR_NORESP = "noresp"
ERR_TABLE[ ERR_NORESP ] = "No response."
ERR_PARSEBITS = "parsebits"
ERR_TABLE[ ERR_PARSEBITS ] = "Unable to parse bit file header."
ERR_DONENOTHIGH = "donenothigh"
ERR_TABLE[ ERR_DONENOTHIGH ] = "DONE pin did not go high."
ERR_IDFAILED = "idfailed"
ERR_TABLE[ ERR_IDFAILED ] = "Reading IDCODE from FPGA failed."
ERR_WRONGDRIVER = "wrongdriver"
ERR_TABLE[ ERR_WRONGDRIVER ] = "IDCODE not recognised by the driver: FPGA not supported."
ERR_BADSIZE = "badsize"
ERR_TABLE[ ERR_BADSIZE ] = "Number of bytes is not valid for this FPGA."
ERR_ALLOC = "alloc"
ERR_TABLE[ ERR_ALLOC ] = "Unable to allocate a buffer for this bitfile."
ERR_DENIED = "denied"
ERR_TABLE[ ERR_DENIED ] = "Bitfile id (BID) is not valid: programming is denied."
ERR_PQFULL = "pqfull"
ERR_TABLE[ ERR_PQFULL ] = "Programming queue is full."
ERR_NOSPACE = "nospace"
ERR_TABLE[ ERR_NOSPACE ] = "There is no space for new bitfiles."
ERR_TDOMISMATCH = "tdomismatch"
ERR_TABLE[ ERR_TDOMISMATCH ] = "TDO readback mismatch during XSVF playback: FPGA disconnected?"
ERR_XSVFERROR = "xsvferror"
ERR_TABLE[ ERR_XSVFERROR ] = "Error in XSVF file."
ERR_PERMISSION = "permission"
ERR_TABLE[ ERR_PERMISSION ] = "Board access denied."
ERR_NOSETUID = "nosetuid"
ERR_TABLE[ ERR_NOSETUID ] = "Use the setuid command first."
ERR_ALREADYLOCKED = "alreadylocked"
ERR_TABLE[ ERR_ALREADYLOCKED ] = "You already hold a lock."
ERR_NOMUTEXDAEMON = "nomutexdaemon"
ERR_TABLE[ ERR_NOMUTEXDAEMON ] = "The mutual exclusion daemon is not running."
ERR_TIMEOUT = "timeout"
ERR_TABLE[ ERR_TIMEOUT ] = "Connection timed out due to inactivity."
ERR_NOJTAG = "nojtag"
ERR_TABLE[ ERR_NOJTAG ] = "No JTAG support on this board."
ERR_NOTJDW = "notjdw"
ERR_TABLE[ ERR_NOTJDW ] = "Not a valid JTAG Direct Write command."
ERR_NOTLOCKED = "notlocked"
ERR_TABLE[ ERR_NOTLOCKED ] = "JTAG Direct Write not locked."
ERR_CANTLOCKJTAG = "cantlockjtag"
ERR_TABLE[ ERR_CANTLOCKJTAG ] = "JTAG Direct Write access is locked by something else."
ERR_JDWFAIL = "jdwfail"
ERR_TABLE[ ERR_JDWFAIL ] = "Failure in JTAG Direct Write."
JDW_RESET = "reset"
JDW_IDLE = "idle"
JDW_SELECTDR = "selectdr"
JDW_CAPTUREDR = "capturedr"
JDW_SHIFTDR = "shiftdr"
JDW_EXIT1DR = "exit1dr"
JDW_PAUSEDR = "pausedr"
JDW_EXIT2DR = "exit2dr"
JDW_UPDATEDR = "updatedr"
JDW_SELECTIR = "selectir"
JDW_CAPTUREIR = "captureir"
JDW_SHIFTIR = "shiftir"
JDW_EXIT1IR = "exit1ir"
JDW_PAUSEIR = "pauseir"
JDW_EXIT2IR = "exit2ir"
JDW_UPDATEIR = "updateir"
JDW_LOCK = "lock"
JDW_UNLOCK = "unlock"
JDW_NAVIGATE_TAP = "navigate_tap"
JDW_PULSE_TCK = "pulse_tck"
JDW_SET_TCK = "set_tck"
JDW_SET_TMS = "set_tms"
JDW_SHIFT_W = "shift_w"
JDW_SHIFT_RW = "shift_rw"
