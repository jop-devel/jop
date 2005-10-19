#
#	Makefile
#
#	Should build JOP and all tools from scratch.
#
#	not included at the moment:
#		ACEX board
#		configuration CPLD compiling
#		Spartan-3 targets
#
#



#
#	com1 is the usual serial port
#	com14 is the FTDI VCOM for the USB download
#		use -usb to download the Java application
#		without the echo 'protocol' on USB
#
COM_PORT=com1
COM_FLAG=-e
#COM_PORT=com18
#COM_FLAG=-e -usb

# 'some' different Quartus projects
QPROJ=cycmin cyc12min cycbaseio cycbg dspio
# if you want to build only one Quartus project use e.q.:
QPROJ=cycmin

# Which project do you want to be downloaded?
DLPROJ=$(QPROJ)
# Which project do you want to be programmed into the flash?
FLPROJ=$(DLProj)
# IP address for Flash programming
IPDEST=192.168.1.2
IPDEST=192.168.0.123


P1=test
P2=test
P3=Hello

#P2=jvm
#P3=DoAll

#P2=testrt
#P3=PeriodicFull
#P1=app
#P2=oebb
#P3=Main
#P3=Usb
# for baseio (Rasmus)
#P1=app
#P2=tal
#P3=Tal

#P1=app
#P2=dsp
#P3=AC97
#P3=SigDel


# use this for serial download
all: directories tools jopser japp

# we use USB download now as default
#all: directories tools jopusb japp

japp: java_app download

install:
	@echo nothing to install

clean:
	@echo "that's specific for my configuration ;-)"
	cd modelsim && ./clean.bat
	@echo classes
	d:/bin/del_class.bat
	cd quartus && d:/bin/qu_del.bat


tools:
	cd java/tools && ./build.bat

# we moved the pc stuff to it's own target to bo
# NOT built on make all.
# It depends on javax.comm which is NOT installed
# by default - Blame SUN on this!
#
pc:
	cd java/pc && ./build.bat

#
#	project.jbc fiels are used to boot from the serial line
#
jopser:
	cd asm && ./jopser.bat
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		echo "building $$target"; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
		cd quartus/$$target && quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
	done

#
#	project.jbc fiels are used to boot from the USB interface
#
jopusb:
	cd asm && ./jopusb.bat
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		echo "building $$target"; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
		cd quartus/$$target && quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
	done

#
#	project.ttf files are used to boot from flash.
#
jopflash:
	cd asm && ./jopflash.bat
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		echo "building $$target"; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
		quartus_cpf -c quartus/$$target/jop.sof ttf/$$target.ttf; \
	done

#qsyn:
#	@echo $(QPROJ)
#	for target in $(QPROJ); do \
#		echo "building $$target"; \
#		qp="quartus/$$target/jop"; \
#		echo $$qp; \
#		quartus_map $$qp; \
#		quartus_fit $$qp; \
#		quartus_asm $$qp; \
#		quartus_tan $$qp; \
#	done


#
#	Modelsim target
#		without the tools
#
sim: java_app
	cd asm && ./jopsim.bat
	cd modelsim && ./sim.bat

#
#	JopSim target
#		without the tools
#
jsim: java_app
	java -cp java/tools/dist/lib/jop-tools.jar -Dlog="false" -Dhandle="true" \
	com.jopdesign.tools.JopSim java/target/dist/bin/$(P2)_$(P3).jop


java_app:
	cd java/target && ./build.bat $(P1) $(P2) $(P3)

download:
	cd quartus/$(DLPROJ) && quartus_pgm -c ByteBlasterMV -m JTAG jop.cdf
	down $(COM_FLAG) java/target/dist/bin/$(P2)_$(P3).jop $(COM_PORT)

#
#	flash programming
#
prog_flash: java_app
	quartus_pgm -c ByteblasterMV -m JTAG -o p\;jbc/$(DLPROJ).jbc
	down java/target/dist/bin/$(P2)_$(P3).jop $(COM_PORT)
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash java/target/dist/bin/$(P2)_$(P3).jop $(IPDEST)
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash ttf/$(FLPROJ).ttf $(IPDEST)
	quartus_pgm -c ByteBlasterMV -m JTAG -o p\;quartus/cycconf/cyc_conf.pof
	
#
#	flash programming for the BG hardware as an example
#
#prog_flash:
#	quartus_pgm -c ByteblasterMV -m JTAG -o p\;jbc/$(DLPROG).jbc
#	cd java/target && ./build.bat app oebb BgInit
#	down java/target/dist/bin/oebb_BgInit.jop $(COM_PORT)
#	cd java/target && ./build.bat app oebb Main
#	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash java/target/dist/bin/oebb_Main.jop 192.168.1.2
#	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash ttf/$(FLPROJ).ttf 192.168.1.2
#	quartus_pgm -c ByteBlasterMV -m JTAG -o p\;quartus/cycconf/cyc_conf.pof
	

pld_init:
	quartus_pgm -c ByteBlasterMV -m JTAG -o p\;quartus/cycconf/cyc_conf_init.pof

pld_conf:
	quartus_pgm -c ByteBlasterMV -m JTAG -o p\;quartus/cycconf/cyc_conf.pof

oebb:
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash java/target/dist/bin/oebb_Main.jop 192.168.1.2

# do the whole build process including flash programming
# for BG and baseio (TAL)
bg: directories tools jopflash jopser prog_flash

#
#	some directories for configuration files
#
directories: jbc ttf

jbc:
	mkdir jbc

ttf:
	mkdir ttf

#
# this line configures the FPGA and programs the PLD
# but uses a .jbc file
#
# However, the order is not so perfect. We would prefere to first
# program the PLD.
#
xxx:
	quartus_pgm -c ByteBlasterMV -m JTAG -o p\;jbc/cycbg.jbc
	quartus_pgm -c ByteBlasterMV -m JTAG -o p\;jbc/cyc_conf.jbc

