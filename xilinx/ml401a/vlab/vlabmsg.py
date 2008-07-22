
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


MSG_TABLE = {
    MSG_ACTIVITYINFO : [ False, "ClReceiveActivityInfo",int,int ],
    MSG_BITINFO : [ True, "ClReceiveBitInfo",int,int,int,str,str,str,str,str ],
    MSG_BOARDINFO : [ True, "ClReceiveBoardInfo",str ],
    MSG_ENDLIST : [ False, "ClEndList" ],
    MSG_ERROR : [ True, "ClError",str,str ],
    MSG_EVERSION : [ True, "ClGetEmbeddedVersion",str,str ],
    MSG_FPGAINFO : [ False, "ClReceiveFPGAInfo",int,str,str ],
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
    def ClReceiveActivityInfo(self, p0, p1):
        pass
    def ClReceiveBitInfo(self, p0, p1, p2, p3, p4, p5, p6, p7):
        pass
    def ClReceiveBoardInfo(self, p0):
        pass
    def ClEndList(self):
        pass
    def ClError(self, p0, p1):
        pass
    def ClGetEmbeddedVersion(self, p0, p1):
        pass
    def ClReceiveFPGAInfo(self, p0, p1, p2):
        pass
    def ClLoaded(self, p0, p1):
        pass
    def ClLoadReady(self, p0, p1, p2):
        pass
    def ClGetMutexDaemonVersion(self, p0, p1):
        pass
    def ClOk(self, p0):
        pass
    def ClProgramFailed(self, p0, p1):
        pass
    def ClProgramOk(self, p0):
        pass
    def Ignore(self):
        pass
    def ClGetRelayVersion(self, p0, p1):
        pass
    def ClUserInfo(self, p0, p1, p2, p3):
        pass
    def ClUsingUart(self):
        pass
    def ClReceiveWall(self, p0):
        pass
