JOPDIR=../..
#set options=-93 -quiet -check_synthesis -lint -pedanticerrors
OPTIONS=-93 -quiet

all: work compile
	vsim -do sim.do TestNoC

compile:
#	vcom $(OPTIONS) $(JOPDIR)/simpcon/sc_pack.vhd
	vcom $(OPTIONS) ../NoCTypes.vhd
	vcom $(OPTIONS) ../SimpConSlaveIF.vhd
	vcom $(OPTIONS) ../SizedFIFOF.vhd
	vcom $(OPTIONS) ../Receiver.vhd
	vcom $(OPTIONS) ../Sender.vhd
	vcom $(OPTIONS) ../TDMANode.vhd
	vcom $(OPTIONS) ../TDMANoC.vhd
	vcom $(OPTIONS) TestNoC.vhd
	vcom $(OPTIONS) TestNode.vhd
	vcom $(OPTIONS) TestReceiver.vhd
	vcom $(OPTIONS) TestSender.vhd

work:
	vlib work

clean:
	-rm -rf work
	-rm transcript
	-rm *.wlf



