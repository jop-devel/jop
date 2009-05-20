JOPDIR=../..
#set options=-93 -quiet -check_synthesis -lint -pedanticerrors
OPTIONS=-93 -quiet

all: work
	vcom $(OPTIONS) $(JOPDIR)/simpcon/sc_pack.vhd
	vcom $(OPTIONS) ../tm.vhd
	vcom $(OPTIONS) tb_rttm.vhd
	vsim -do sim.do tb_rttm

work:
	vlib work


clean:
	-rm -rf work



