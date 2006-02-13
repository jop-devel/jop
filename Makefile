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
#	com6 is the FTDI VCOM for the USB download
#		use -usb to download the Java application
#		without the echo 'protocol' on USB
#
COM_PORT=COM1
COM_FLAG=-e
#COM_PORT=COM6
#COM_FLAG=-e -usb

# 'some' different Quartus projects
QPROJ=cycmin cycbaseio cycbg dspio lego cycfpu
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

#P2=wishbone
#P3=Simple

#P1=bench
#P2=jbe
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

#P1=app
#P2=lego
#P3=LineFollower

#
#	some variables
#
TOOLS=java/tools
TARGET=java/target
EXT_LIB=-classpath java/lib/bcel-5.1.jar\;java/lib/jakarta-regexp-1.3.jar\;java/lib/RXTXcomm.jar
TOOLS_JFLAGS=-d $(TOOLS)/dist/classes $(EXT_LIB) -sourcepath $(TOOLS)/src\;$(TARGET)/src/common

# use this for serial download
all: directories tools jopser japp

japp: java_app download


# use this for USB download of FPGA configuration
# and Java program download
#all: directories tools jopusb japp
#
#japp: java_app download_usb


install:
	@echo nothing to install

clean:
	@echo "that's specific for my configuration ;-)"
	cd modelsim && ./clean.bat
	@echo classes
	d:/bin/del_class.bat
	cd quartus && d:/bin/qu_del.bat


tools:
	-rm -r $(TOOLS)/dist
	mkdir $(TOOLS)/dist
	mkdir $(TOOLS)/dist/lib
	mkdir $(TOOLS)/dist/classes
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/*.java
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/build/*.java
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/tools/*.java
	cd $(TOOLS)/dist/classes && jar cf ../lib/jop-tools.jar *

#	old version with batch file
#	cd java/tools && ./build.bat

# we moved the pc stuff to it's own target to be
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
		make qsyn -e QBT=$$target; \
		cd quartus/$$target; \
		quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
		quartus_cpf -c jop.sof ../../rbf/$$target.rbf; \
		cd ../..; \
	done


#
#	project.jbc fiels are used to boot from the USB interface
#
jopusb:
	cd asm && ./jopusb.bat
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		make qsyn -e QBT=$$target; \
		cd quartus/$$target; \
		quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
		quartus_cpf -c jop.sof ../../rbf/$$target.rbf; \
		cd ../..; \
	done

#
#	project.ttf files are used to boot from flash.
#
jopflash:
	cd asm && ./jopflash.bat
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		make qsyn -e QBT=$$target; \
		quartus_cpf -c quartus/$$target/jop.sof ttf/$$target.ttf; \
		cd ../..; \
	done


#
#	Quartus build process
#		called by jopser, jopusb,...
#
qsyn:
	echo $(QBT)
	echo "building $(QBT)"
	-rm -r quartus/$(QBT)/db
	-rm quartus/$(QBT)/jop.sof
	-rm jbc/$(QBT).jbc
	-rm rbf/$(QBT).rbf
	quartus_map quartus/$(QBT)/jop
	quartus_fit quartus/$(QBT)/jop
	quartus_asm quartus/$(QBT)/jop
	quartus_tan quartus/$(QBT)/jop

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
	java -cp java/tools/dist/lib/jop-tools.jar\;java/lib/RXTXcomm.jar com.jopdesign.tools.JavaDown \
		$(COM_FLAG) java/target/dist/bin/$(P2)_$(P3).jop $(COM_PORT)

#	this is the download version with down.exe
#	down $(COM_FLAG) java/target/dist/bin/$(P2)_$(P3).jop $(COM_PORT)

download_usb:
	cd rbf && ../USBRunner $(DLPROJ).cdf
	java -cp java/tools/dist/lib/jop-tools.jar\;java/lib/RXTXcomm.jar com.jopdesign.tools.JavaDown \
		$(COM_FLAG) java/target/dist/bin/$(P2)_$(P3).jop $(COM_PORT)

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


#
#	JOP porting test programs
#
#	TODO: combine all quartus stuff to a single target
#
jop_blink_test:
	cd asm && ./build.bat blink
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		echo "building $$target"; \
		rm -r quartus/$$target/db; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
		cd quartus/$$target && quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
	done
	cd quartus/$(DLPROJ) && quartus_pgm -c ByteBlasterMV -m JTAG jop.cdf
	e $(COM_PORT)


jop_testmon:
	cd asm && ./build.bat testmon
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		echo "building $$target"; \
		rm -r quartus/$$target/db; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
		cd quartus/$$target && quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
	done
	cd quartus/$(DLPROJ) && quartus_pgm -c ByteBlasterMV -m JTAG jop.cdf


#
#	UDP debugging
#
udp_dbg:
	java -cp java/pc/dist/lib/jop-pc.jar udp.UDPDbg
