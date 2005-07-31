#
#	Makefile
#
#	Should build JOP and all tools from scratch.
#
#	not included at the moment:
#		ACEX board
#		configuration CPLD compiling
#		build of .ttf files for boot from Flash
#		Spartan-3 targets
#
#

all: tools jopser qsyn japp

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
QPROJ=cycbaseio

# Which project do you want to be downloaded?
DLPROJ=cycbaseio

P1=test

P2=test
P3=Baseio
#P2=jvm
#P3=DoAll
#P2=testrt
#P3=PeriodicFull
#P1=app
#P2=oebb
#P3=Main

tools:
	cd java/tools && ./build.bat

jopser:
	cd asm && ./jopser.bat

qsyn:
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		echo "building $$target"; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
	done

java_app:
	cd java/target && ./build.bat $(P1) $(P2) $(P3)

download:
	cd quartus/$(DLPROJ) && quartus_pgm -c ByteBlasterMV -m JTAG jop.cdf
	down -e java/target/dist/bin/$(P2)_$(P3).jop COM1

