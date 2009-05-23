JOPDIR=../..
#set options=-93 -quiet -check_synthesis -lint -pedanticerrors
OPTIONS=-93 -quiet

all: work compile
	vsim -do sim.do tb_rttm

compile:
	vcom $(OPTIONS) $(JOPDIR)/simpcon/sc_pack.vhd
	vcom $(OPTIONS) ../tm.vhd
	vcom $(OPTIONS) tb_rttm.vhd

work:
	vlib work

ED=c:/programs/lemmy/lemmy

edit:
	$(ED) ../tm.vhd &
	$(ED) ../tmtop.vhd &
	$(ED) tb_rttm.vhd &
	$(ED) ../../simpcon/sc_pack.vhd &

clean:
	-rm -rf work
	-rm transcript
	-rm *.wlf



