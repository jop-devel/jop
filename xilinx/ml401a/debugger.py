
import sys

import vlab, collections, time, vlabif
from twisted.internet import reactor, defer, protocol

from vlab.download import BOARD_NAME, DecodeJOP, EXIT_STRING

@defer.inlineCallbacks
def Run():
    print 'Initialising...'

    try:
        bt = int(sys.argv[ 1 ])
    except:
        bt = None

    byte_buffer = DecodeJOP("../../java/target/dist/bin/HWMethTest.jop")
    bits = file("ml401.bit").read()
    debug_chain = vlabif.DebugConfig(chain_config_file="chain_config.py")
    vlihp = vlabif.VlabInterfaceProtocol(debug=False)
    auth = vlab.loadAuthorisation("../../vluser.key")
    dbg = vlabif.DebugDriver(debug_chain, debug=False)
    uproto = vlab.VlabUARTProtocol()

    print 'Breakpoint type = %s' % bt
    print 'Connecting to lab service... (1)'
    vlf = vlab.VlabClientFactory(auth)
    reactor.connectTCP(auth.relay_server_name, 22, vlf)
    vl = yield vlf.getChannel()
    print 'Connecting to board... (1)'
    yield vl.connect(BOARD_NAME)
    yield vl.setUART(1, 115200)
    yield vl.setUART(0, 115200)

    print 'FPGA programming...'
    bid = yield vl.sendBitfile(bits)
    yield vl.programFPGA(0, bid)

    print 'Starting vlabifhw and debugger...'
    yield vl.openUART(1, vlihp)
    yield vlihp.start()
    vlihp.openChannel(vlihp.num_channels - 1, dbg)
    yield dbg.reset()
    yield dbg.capture()
    data = yield dbg.downloadChain()
    if ( bt != None ):
        data[ 'break_command' ].setOutput(bt & 0x7) 
    yield dbg.uploadChain()
    yield dbg.ready()
    yield dbg.setControlLines(free_run_break=True)

    print 'Connecting to lab service... (2)'
    vlf = vlab.VlabClientFactory(auth)
    reactor.connectTCP(auth.relay_server_name, 22, vlf)
    vl = yield vlf.getChannel()
    print 'Connecting to board... (2)'
    yield vl.connect(BOARD_NAME)
    yield vl.openUART(0, uproto)

    print 'Sending JOP data and running to breakpoint...'
    uproto.write(byte_buffer)
    discard = yield uproto.read(int(len(byte_buffer) * 0.95))

    if ( bt != None ):
        if ( bt < 8 ):
            print 'run to break'
            yield runToBreak(dbg) 
            print 'hit'
            yield activityCapture(dbg)
            return
        else:
            yield dbg.setControlLines(free_run=True)
            yield delay(5.0)
            yield activityCapture(dbg)
            return

    yield dbg.setControlLines(free_run=True)
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

    #vl.disconnect()
    reactor.stop()
    return


@defer.inlineCallbacks
def activityCapture(dbg):
    while True:
        yield dbg.capture()
        yield dbg.downloadChain()
        yield dbg.printChain()
        yield dbg.clock(1)


@defer.inlineCallbacks
def memoryCapture(dbg):
    data = yield dbg.downloadChain()
    yield runToBreak(dbg) # run to memory read state
    data[ 'break_command' ].setOutput(4)
    yield dbg.uploadChain()
    yield dbg.ready()

    print 'Stepping...'
    extra_data = False
    events = collections.deque()
    cc = 0
    grab_next = False
    dump = file("clocks_after_first_read", 'wt')
    while True:
        if ( extra_data ):
            print '%u. ready count = %d' % (cc,
                    data[ 'sc_arb_in_0_rdy_cnt' ].getInt())

        if (( len(events) != 0 )
        and ( grab_next or ( data[ 'sc_arb_in_0_rdy_cnt' ].get() == '00' ))):
            ev = events.popleft()
            wr = ev[ 'wr' ]
            address = ev[ 'address' ]
            wrdata = ev[ 'wrdata' ]
            rddata = data[ 'sc_arb_in_0_rd_data' ].getInt()
            if ( wr ):
                print '%u. Write. M[0x%x (%u)] := 0x%x (%u)' % (
                        cc, address, address, wrdata, wrdata)
            else:
                print '%u. Read. M[0x%x (%u)] = 0x%x (%u)' % (
                        cc, address, address, rddata, rddata)

        if (( data[ 'sc_arb_out_0_rd' ].get() == '1' )
        or ( data[ 'sc_arb_out_0_wr' ].get() == '1' )):
            if ( extra_data ):
                print '%u. request' % (cc)
            events.append({
                "wr" : ( data[ 'sc_arb_out_0_wr' ].get() == '1' ),
                "address" : data[ 'sc_arb_out_0_address' ].getInt(),
                "wrdata" : data[ 'sc_arb_out_0_wr_data' ].getInt(),
                    })
            assert len(events) <= 2

        grab_next = ( data[ 'sc_arb_in_0_rdy_cnt' ].get() == '01' )

        yield dbg.clock(1)
        cc += 1
        yield dbg.capture()
        yield dbg.downloadChain()
    

        dump.write('cycle %u\n' % cc)
        for dr in dbg.getAsList():
            dump.write('   ')
            dump.write(dr.getText())
            dump.write('\n')
        dump.flush()

    yield runAndCapture(dbg, 100)
    reactor.stop()
    return

@defer.inlineCallbacks
def runAndCapture(dbg, repeats):
    for i in xrange(repeats):
        print '**'
        yield dbg.capture()
        yield dbg.downloadChain()
        dbg.printChain()
        yield dbg.clock(1)


@defer.inlineCallbacks
def runToBreak(dbg):
    yield dbg.setControlLines(free_run_break=True)
    yield dbg.capture()
    data = yield dbg.downloadChain()
    while ( data[ 'breakpoint' ].get() == "0" ):
        yield dbg.capture()
        yield dbg.downloadChain()
    yield dbg.setControlLines(free_run_break=False)

@defer.inlineCallbacks
def multibreak(dbg, capture_timer, repeats):
    for i in xrange(repeats):
        print ''
        print ''
        print '%u: Waiting for breakpoint...' % i
        yield runToBreak(dbg)
        dbg.printChain()

        cd = capture_timer
        while ( cd > 0 ):
            print '**'
            yield dbg.clock(1)
            yield dbg.capture()
            data = yield dbg.downloadChain()
            dbg.printChain()
            if ( data[ 'breakpoint' ].get() == "1" ):
                cd = capture_timer
            else:
                cd -= 1

def delay(seconds):
    done = defer.Deferred()
    def fire(): 
        done.callback(True)
    reactor.callLater(seconds, fire)
    return done


if ( __name__ == "__main__" ):
    reactor.addSystemEventTrigger('after', 'startup', Run)
    reactor.run()


