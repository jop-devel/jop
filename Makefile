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

# do the while process for the BG
test: directories jopflash jopser prog_flash

all: directories jopser japp

japp: java_app download

install:
	@echo nothing to install

clean:
	@echo "that's specific for my configuration ;-)"
	cd modelsim && ./clean.bat
	@echo classes
	d:/bin/del_class.bat
	cd quartus && d:/bin/qu_del.bat

# 'some' different Quartus projects
QPROJ=cycmin cyc12min cycbaseio cycbg
# if you want to build only one Quartus project use e.q.:
#QPROJ=cycmin
#QPROJ=cycbaseio
QPROJ=cycbg

# Which project do you want to be downloaded?
DLPROJ=cycbg
# Which project do you want to be programmed into the flash?
FLPROJ=cycbg

P1=test

P2=test
P3=Baseio
#P2=jvm
#P3=DoAll
#P2=testrt
#P3=PeriodicFull
P1=app
P2=oebb
P3=Main

tools:
	cd java/tools && ./build.bat
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
#	project.ttf files are used to boot from flash.
#
jopflash:
	cd asm && ./jopflash.bat
	mkdir
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




java_app:
	cd java/target && ./build.bat $(P1) $(P2) $(P3)

download:
	cd quartus/$(DLPROJ) && quartus_pgm -c ByteBlasterMV -m JTAG jop.cdf
	down -e java/target/dist/bin/$(P2)_$(P3).jop COM1

#
#	flash programming for the BG hardware as an example
#
prog_flash:
	quartus_pgm -c ByteblasterMV -m JTAG -o p\;jbc/$(FLPROJ).jbc
	cd java/target && ./build.bat app oebb BgInit
	down java/target/dist/bin/oebb_BgInit.jop COM1
	cd java/target && ./build.bat app oebb Main
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash java/target/dist/bin/oebb_Main.jop 192.168.1.2
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash ttf/$(FLPROJ).ttf 192.168.1.2
	quartus_pgm -c ByteBlasterMV -m JTAG -o p\;jbc/cyc_conf.jbc
	

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
