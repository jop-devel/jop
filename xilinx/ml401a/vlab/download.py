#!/usr/bin/python


MAX_MEM	= 1048576/4
BLOCK_SIZE = 1024
EXIT_STRING = "JVM exit!"
SERVICE_SETTINGS = { 
        "relay_server" : "cynthia", 
        "user_name" : "vltest", 
        "board_name" : "burchtest", 
        "private_key_file" : "vluser_key",
    }


from vlab import Vlab
import sys, collections, time


def Main(bit_fname, jop_fname):
    # Open files
    fp = file(jop_fname)
    bits = file(bit_fname).read()

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
    file('bytes.bin', 'wt').write(byte_buffer)

    print "%d words of Java bytecode (%d KB)" % (ram[1]-1, (ram[1]-1)/256)

    # Program FPGA and send JOP file
    vl = Vlab()
    print 'Connecting to lab service...'
    vl.Connect(**SERVICE_SETTINGS)
    bid = vl.SendBitfile(bits)
    print 'Bitfile sent, bid %u' % bid
    vl.ProgramFPGA(0, bid)
    print 'FPGA Programming complete'
    vl.SetUart(0, 115200)

    # Send the program
    (rx_fd, tx_fd) = vl.UseUart(0, True)
    tx_fd.write(byte_buffer)
    tx_fd.flush()

    print 'JOP Programming complete'
    fifo = collections.deque()
    stop = False

    # Terminal mode
    while ( not stop ):
        byte = rx_fd.read(1)
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

    vl.Disconnect()
    print ''
    print ''


if ( __name__ == "__main__" ):
    if ( len(sys.argv) != 3 ):
        print 'Usage: %s <bit file> <jop file>' % sys.argv[ 0 ]
    else:
        Main(sys.argv[ 1 ], sys.argv[ 2 ])


