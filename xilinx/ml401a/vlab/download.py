#!/usr/bin/python
# 
# Example for JOP (Java Optimised Processor)
# This program loads a bit file and then downloads a JOP
# file using the JOP protocol. It then switches to a terminal
# mode and echoes everything received from JOP.
# 


MAX_MEM	= 1048576/4
BLOCK_SIZE = 1024
EXIT_STRING = "JVM exit!"
BOARD_NAME = "jeopard"
VL_KEY = "vluser.key"


import vlab, sys, collections
from twisted.internet import reactor, defer

def DecodeJOP(jop_fname):
    fp = file(jop_fname)

    # Decode JOP file
    ram = []
    for line in fp:
        slash = line.find('/') 
        if ( slash >= 0 ):
            line = line[ : slash ]
       
        for word in line.split(','):
            word = word.strip()
            if ( len(word) > 0 ):
                ram.append(int(word))
                if ( len(ram) >= MAX_MEM ):
                    raise Exception("Too many words (%d/%d)" % (len(ram),
                            MAX_MEM))
        
    fp.close()
    if ( len(ram) == 0 ):
        raise Exception("No memory?")

    # Make byte buffer
    byte_buffer = []
    for word in ram:
        for j in xrange(4):
            byte_buffer.append(chr(0xff & (
                    word >> (( 3 - j ) * 8 ))))
    byte_buffer = ''.join(byte_buffer)
    print "%d words of Java bytecode (%d KB)" % (ram[1]-1, (ram[1]-1)/256)
    return byte_buffer


@defer.inlineCallbacks
def Main(bit_fname, jop_fname):
    byte_buffer = DecodeJOP(jop_fname)
    file('bytes.bin', 'wt').write(byte_buffer)
    bits = file(bit_fname).read()

    # Program FPGA and send JOP file
    print 'Connecting to lab service...'
    auth = vlab.loadAuthorisation(VL_KEY)
    vlf = vlab.VlabClientFactory(auth)
    reactor.connectTCP(auth.relay_server_name, 22, vlf)
    vl = yield vlf.getChannel()

    uproto = vlab.VlabUARTProtocol()

    print 'Connecting to board'
    bid = yield vl.connect(BOARD_NAME)

    print 'Sending bit file (%u bytes)' % len(bits)
    bid = yield vl.sendBitfile(bits)
    print 'Bitfile sent, bid %u' % bid
    rc = yield vl.programFPGA(0, bid)
    print 'Programming complete, rc %u' % rc
    yield vl.setUART(0, 115200)
    print 'SetUART rc %u' % rc
    rc = yield vl.openUART(0, uproto)
    print 'OpenUART rc %u' % rc

    # Send the program
    uproto.write(byte_buffer)

    print 'JOP Programming complete'
    fifo = collections.deque()
    stop = False

    # Terminal mode
    while ( not stop ):
        byte = yield uproto.read(1)
        byte = ord(byte)
        if ( not (( byte in (10, 13))
        or ( 32 <= byte < 127 ))):
            byte = ord('.')
        byte = chr(byte)

        sys.stdout.write(byte)
        sys.stdout.flush()
        fifo.append(byte)
        if ( len(fifo) > len(EXIT_STRING) ):
            fifo.popleft()
            if ( ''.join(fifo) == EXIT_STRING ):
                stop = True

    vl.disconnect()
    print ''
    print ''

@defer.inlineCallbacks
def Run():
    try:
        if ( len(sys.argv) != 3 ):
            print 'Usage: %s <bit file> <jop file>' % sys.argv[ 0 ]
        else:
            yield Main(sys.argv[ 1 ], sys.argv[ 2 ])
    finally:
        reactor.stop()

if ( __name__ == "__main__" ):
    reactor.addSystemEventTrigger('after', 'startup', Run)
    reactor.run()


