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
#	You probably want to change the folloing parts in the Makefile:
#
#		QPROJ ... your Quartus FPGA project
#		COM_* ... your communication settings
#		all:, japp: ... USB or serial download
#		TARGET_APP_PATH, MAIN_CLASS ... your target application
#
#	for a quick change you can also use command line arguments when invoking make:
#		make japp -e QPROJ=cycwrk TARGET_APP_PATH=java/target/src/bench MAIN_CLASS=jbe/DoAll
#
#


#
#	Set USB to true for an USB based board (dspio, usbmin, lego)
#
USB=false


#
#	com1 is the usual serial port
#	com5 is the FTDI VCOM for the USB download
#		use -usb to download the Java application
#		without the echo 'protocol' on USB
#
ifeq ($(USB),true)
	COM_PORT=COM5
	COM_FLAG=-e -usb
else
	COM_PORT=COM1
	COM_FLAG=-e
endif

BLASTER_TYPE=ByteBlasterMV
#BLASTER_TYPE=USB-Blaster

ifeq ($(WINDIR),)
	USBRUNNER=./USBRunner
	S=:
else
	USBRUNNER=USBRunner.exe
	S=\;
endif

# 'some' different Quartus projects
QPROJ=cycmin cycbaseio cycbg dspio lego cycfpu cyc256x16 sopcmin usbmin cyccmp
# if you want to build only one Quartus project use e.q.:
ifeq ($(USB),true)
	QPROJ=usbmin
else
	QPROJ=cycmin
endif

# Number of cores for JopSim and RTTM simulation
CORE_CNT=4

# Which project do you want to be downloaded?
DLPROJ=$(QPROJ)
# Which project do you want to be programmed into the flash?
FLPROJ=$(DLPROJ)
# IP address for Flash programming
IPDEST=192.168.1.2
IPDEST=192.168.0.123

P1=test
P2=test
P3=HelloWorld
#P2=jvm
#P3=DoAll
#P1=rtapi
#P2=examples/scopes
#P3=LocalScope
#P3=LocalMatrixCalc

#P1=test
#P2=rtlib
#P3=CMPBuffer

#P1=common
#P2=ejip123/examples
#P3=HelloWorldHereIPing

#P1=bench
#P2=jbe
#P3=DoApp
# The test program for Basio and the NAND Flash
#P3=FlashBaseio

#P1=app
#P2=oebb
#P3=Main

#P2=wcet
#P3=Loop
WCET_METHOD=measure

#P1=.
#P2=dsvmmcp
#P3=TestDSVMMCP

#
#	some variables
#
TOOLS=java/tools
# setting for my Eclipse CVS project
# TOOLS=../../workspace/cvs_jop_tools
EXT_CP=-classpath java/lib/bcel-5.1.jar$(S)java/lib/jakarta-regexp-1.3.jar$(S)java/lib/RXTXcomm.jar$(S)java/lib/lpsolve55j.jar$(S)java/lib/log4j-1.2.15.jar$(S)java/lib/jgrapht-jdk1.5.jar$(S)java/lib/velocity-1.5.jar$(S)java/lib/velocity-dep-1.5.jar

# The line below makes the compilation crash, because it causes JOPizer to include a *lot*
# of classes which are actually not necessary.
#EXT_CP=-classpath java/jopeclipse/com.jopdesign.jopeclipse/lib/bcel-5.2.jar$(S)java/lib/jakarta-regexp-1.3.jar$(S)java/lib/RXTXcomm.jar$(S)java/lib/lpsolve55j.jar
#EXT_CP=-classpath java/lib/recompiled_bcel-5.2.jar$(S)java/lib/jakarta-regexp-1.3.jar$(S)java/lib/RXTXcomm.jar$(S)java/lib/lpsolve55j.jar

#TOOLS_JFLAGS=-d $(TOOLS)/dist/classes $(EXT_CP) -sourcepath $(TOOLS)/src$(S)$(TARGET)/src/common
TOOLS_JFLAGS=-g -d $(TOOLS)/dist/classes $(EXT_CP) -sourcepath $(TOOLS)/src$(S)$(TARGET)/src/common -encoding Latin1

PCTOOLS=java/pc
PCTOOLS_JFLAGS=-g -d $(PCTOOLS)/dist/classes -sourcepath $(PCTOOLS)/src -encoding Latin1


TARGET=java/target

# changed to add another class to the tool chain
#TOOLS_CP=$(EXT_CP)$(S)$(TOOLS)/dist/lib/jop-tools.jar
TOOLS_CP=$(EXT_CP)$(S)$(TOOLS)/dist/lib/jop-tools.jar$(S)$(TOOLS)/dist/lib/JopDebugger.jar

TARGET_SOURCE=$(TARGET)/src/common$(S)$(TARGET)/src/jdk_base$(S)$(TARGET)/src/jdk11$(S)$(TARGET)/src/rtapi$(S)$(TARGET_APP_SOURCE_PATH)
TARGET_JFLAGS=-d $(TARGET)/dist/classes -sourcepath $(TARGET_SOURCE) -bootclasspath "" -extdirs "" -classpath "" -source 1.5
GCC_PARAMS=""

# uncomment this if you want floating point operations in hardware
# ATTN: be sure to choose 'cycfpu' as QPROJ else no FPU will be available
#GCC_PARAMS="-DFPU_ATTACHED"

#
#	Add your application source pathes and class that contains the
#	main method here. We are using those simple P1/2/3 variables for
#		P1=directory, P2=package name, and P3=main class
#	for sources 'inside' the JOP source tree
#
#	TARGET_APP_PATH is the path to your application source
#
#	MAIN_CLASS is the class that contains the Main method with package names
#
TARGET_APP_PATH=$(TARGET)/src/$(P1)
MAIN_CLASS=$(P2)/$(P3)

# here an example how to define an application outside
# from the jop directory tree
#TARGET_APP_PATH=/usr2/muvium/jopaptalone/src
#MAIN_CLASS=com/muvium/eclipse/PeriodicTimer/JOPBootstrapLauncher


#	add more directoies here when needed
#		(and use \; to escape the ';' when using a list!)
TARGET_APP_SOURCE_PATH=$(TARGET_APP_PATH)$(S)$(TARGET)/src/bench$(S)$(TARGET)/src/app
TARGET_APP=$(TARGET_APP_PATH)/$(MAIN_CLASS).java


# setting for rup DSVM on JopCMP
#TARGET_APP_PATH=/usrx/DSVMFP/src

# just any name that the .jop file gets.
JOPBIN=$(P3).jop


#
#	Debugger stuff
#
# Added flags for development with JDWP
#DEBUG_PORT = 8000
DEBUG_PORT = 8001
DEBUG_PARAMETERS= -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=$(DEBUG_PORT)
#DEBUG_PARAMETERS= 

#DEBUG_JOPIZER=$(DEBUG_PARAMETERS)
DEBUG_JOPIZER=

#DEBUG_JOPSIM=$(DEBUG_PARAMETERS)
DEBUG_JOPSIM=



#
#	application optimization with ProGuard:
#	proguard.sourceforge.net/
#	uncomment following line to use it
#OPTIMIZE=mv java/target/dist/lib/classes.zip java/target/dist/lib/in.zip; java -jar java/lib/proguard.jar @optimize.pro

#
#	application optimization with JOPtimizer
#	uncomment the following lines to use it
#
#OPTIMIZE=java $(EXT_CP)$(S)$(TOOLS)/dist/lib/joptimizer.jar joptimizer.JOPtimizerRunner \
#	 -config jar:file:$(TOOLS)/dist/lib/joptimizer.jar!/jop.conf $(MAIN_CLASS) && \
#	cd $(TARGET)/dist/classes && jar cf ../lib/classes.zip *



# build everything from scratch
all:
	make directories
	make tools
ifeq ($(USB),true)
	make jopusb
else
	make jopser
endif
	make japp

# build the Java application and download it
japp:
	make java_app
ifeq ($(USB),true)
	make config_usb
else
	make config_byteblaster
endif
	make download


install:
	@echo nothing to install

clean:
	@echo "that's specific for my configuration ;-)"
	cd modelsim && ./clean.bat
	@echo classes
	d:/bin/del_class.bat
	cd quartus && d:/bin/qu_del.bat

#
#	build all the (Java) tools
#
tools:
	-rm -rf $(TOOLS)/dist
	mkdir $(TOOLS)/dist
	mkdir $(TOOLS)/dist/lib
	mkdir $(TOOLS)/dist/classes
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/*.java
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/build/*.java
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/tools/*.java
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/wca_rup/*.java
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/dfa/*.java
	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/wcet/*.java
	cp $(TOOLS)/src/com/jopdesign/wcet/report/*.vm $(TOOLS)/dist/classes/com/jopdesign/wcet/report
# Build libgraph and joptimizer
	#make joptimizer -e TOOLS_JFLAGS="$(TOOLS_JFLAGS)" TOOLS="$(TOOLS)"
# quick hack to get the tools with the debugger ok
# the build.xml from the debugger contains the correct info
# but also some more (old?) stuff
# does not work as some Sun classes for JDWP are missing
#	javac $(TOOLS_JFLAGS) $(TOOLS)/src/com/jopdesign/debug/jdwp/*.java
	cd $(TOOLS)/dist/classes && jar cf ../lib/jop-tools.jar *

#
#	Build joptimizer and libgraph
#	JAR must contain the filename of the jar tool of the JDK.
#
ifneq ($(JAVA_HOME),)
    JAR="$(JAVA_HOME)/bin/jar"
else
    JAR=jar
endif

joptimizer:
	make compile_java -e JAVAC_FLAGS="$(TOOLS_JFLAGS)" JAVA_DIR=$(TOOLS)/src/com/jopdesign/libgraph
	make compile_java -e JAVAC_FLAGS="$(TOOLS_JFLAGS)" JAVA_DIR=$(TOOLS)/src/joptimizer
	#cd $(TOOLS)/dist/classes && jar cfm ../lib/joptimizer.jar ../../src/joptimizer/MANIFEST.MF \
	cd $(TOOLS)/dist/classes && $(JAR) cf ../lib/joptimizer.jar \
		joptimizer com/jopdesign/libgraph \
		-C ../../src/joptimizer log4j.properties \
		-C ../../src/joptimizer jop.conf
	cd $(TOOLS)/dist/classes && $(JAR) uf ../lib/joptimizer.jar \
		-C ../../src joptimizer/config/jop-arch.properties \
		-C ../../src joptimizer/config/jvm-arch.properties

#
#	A helper target to compile all java files in a directory and all subdirs
#	Dont know how to 'find' on windows, so going the long way..
#
ifneq ($(JAVA_DIR),)
  jdirs := $(subst :,,$(shell ls -R $(JAVA_DIR) | grep ":"))
  jfiles := $(foreach dir,$(jdirs),$(wildcard $(dir)/*.java))
endif
compile_java:
	@echo "Compiling files in $(JAVA_DIR) .."
	@javac $(JAVAC_FLAGS) $(jfiles)


# we moved the pc stuff to it's own target to be
# NOT built on make all.
# It depends on javax.comm which is NOT installed
# by default - Blame SUN on this!
#
#	TODO: change it to RXTXcomm if it's working ok
#
pc:
	-rm -rf $(PCTOOLS)/dist
	mkdir $(PCTOOLS)/dist
	mkdir $(PCTOOLS)/dist/lib
	mkdir $(PCTOOLS)/dist/classes
#	make compile_java -e JAVA_DIR=$(PCTOOLS)/src
	javac $(PCTOOLS_JFLAGS) $(PCTOOLS)/src/udp/*.java
	cd $(PCTOOLS)/dist/classes && jar cf ../lib/jop-pc.jar *

#
# make target for the tools that are still in C
#
cprog:
	gcc c_src/amd.c -o amd.exe
	gcc c_src/e.c -o e.exe

#
#	compile and JOPize the application
#
java_app:
	-rm -rf $(TARGET)/dist
	-mkdir $(TARGET)/dist
	-mkdir $(TARGET)/dist/classes
	-mkdir $(TARGET)/dist/lib
	-mkdir $(TARGET)/dist/bin
	javac $(TARGET_JFLAGS) $(TARGET)/src/common/com/jopdesign/sys/*.java
	javac $(TARGET_JFLAGS) $(TARGET)/src/jdk_base/java/lang/annotation/*.java	# oh new Java 1.5 world!
	javac $(TARGET_JFLAGS) $(TARGET_APP)
	cd $(TARGET)/dist/classes && jar cf ../lib/classes.zip *
	$(OPTIMIZE)
# use SymbolManager for Paulo's version of JOPizer instead
	java $(DEBUG_JOPIZER) $(TOOLS_CP) -Dmgci=false com.jopdesign.build.JOPizer \
		-cp $(TARGET)/dist/lib/classes.zip -o $(TARGET)/dist/bin/$(JOPBIN) $(MAIN_CLASS)
#	java $(DEBUG_JOPIZER) $(TOOLS_CP) -Dmgci=false com.jopdesign.debug.jdwp.jop.JopSymbolManager \
#		-cp $(TARGET)/dist/lib/classes.zip -o $(TARGET)/dist/bin/$(JOPBIN) $(MAIN_CLASS)
	java $(TOOLS_CP) com.jopdesign.tools.jop2dat $(TARGET)/dist/bin/$(JOPBIN)
	cp *.dat modelsim
	rm -f *.dat


#
#	project.sof fiels are used to boot from the serial line
#
jopser:
	cd asm && export GCC_PARAMS=$(GCC_PARAMS) && ./jopser.bat
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		make qsyn -e QBT=$$target; \
		cd quartus/$$target; \
		cd ../..; \
	done


#
#	project.rbf fiels are used to boot from the USB interface
#
jopusb:
	cd asm && export GCC_PARAMS=$(GCC_PARAMS) && ./jopusb.bat
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		make qsyn -e QBT=$$target; \
		cd quartus/$$target; \
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
	done


#
#	Quartus build process
#		called by jopser, jopusb,...
#
qsyn:
	echo $(QBT)
	echo "building $(QBT)"
	-rm -rf quartus/$(QBT)/db
	-rm -f quartus/$(QBT)/jop.sof
	-rm -f jbc/$(QBT).jbc
	-rm -f rbf/$(QBT).rbf
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
	# for simulation of CMP 
	# cd modelsim && ./sim_cmp.bat

#
#	JopSim target
#		without the tools
#		use -Dcpucnt=# for a CMP simulation
#
jsim: java_app
	java $(DEBUG_JOPSIM) -cp java/tools/dist/lib/jop-tools.jar -Dlog="false" \
	com.jopdesign.tools.JopSim java/target/dist/bin/$(JOPBIN)

#
#	Simulate RTTM
#
tmsim: java_app
	java $(DEBUG_JOPSIM) -cp java/tools/dist/lib/jop-tools.jar -Dcpucnt=$(CORE_CNT) \
	com.jopdesign.tools.TMSim java/target/dist/bin/$(JOPBIN)

#
#	JopServer target
#		without the tools
#
jsim_server: java_app
	java $(DEBUG_JOPSIM) \
	-cp java/tools/dist/lib/jop-tools.jar$(S)$(TOOLS)/dist/lib/JopDebugger.jar -Dlog="false" \
	com.jopdesign.debug.jdwp.jop.JopServer java/target/dist/bin/$(JOPBIN)


config_byteblaster:
	cd quartus/$(DLPROJ) && quartus_pgm -c $(BLASTER_TYPE) -m JTAG jop.cdf

config_usb:
	cd rbf && ../$(USBRUNNER) $(DLPROJ).rbf

download:
#	java -cp java/tools/dist/lib/jop-tools.jar$(S)java/lib/RXTXcomm.jar com.jopdesign.tools.JavaDown \
#		$(COM_FLAG) java/target/dist/bin/$(JOPBIN) $(COM_PORT)
#
#	this is the download version with down.exe
	down $(COM_FLAG) java/target/dist/bin/$(JOPBIN) $(COM_PORT)


#
#	flash programming
#
prog_flash: java_app
	quartus_pgm -c $(BLASTER_TYPE) -m JTAG -o p\;jbc/$(DLPROJ).jbc
	down java/target/dist/bin/$(JOPBBIN) $(COM_PORT)
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash java/target/dist/bin/$(JOPBIN) $(IPDEST)
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash ttf/$(FLPROJ).ttf $(IPDEST)
	quartus_pgm -c $(BLASTER_TYPE) -m JTAG -o p\;quartus/cycconf/cyc_conf.pof

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
#	quartus_pgm -c $(BLASTER_TYPE) -m JTAG -o p\;quartus/cycconf/cyc_conf.pof

erase_flash:
	java -cp java/pc/dist/lib/jop-pc.jar udp.Erase $(IPDEST)

pld_init:
	quartus_pgm -c $(BLASTER_TYPE) -m JTAG -o p\;quartus/cycconf/cyc_conf_init.pof

pld_conf:
	quartus_pgm -c $(BLASTER_TYPE) -m JTAG -o p\;quartus/cycconf/cyc_conf.pof

oebb:
	java -cp java/pc/dist/lib/jop-pc.jar udp.Flash java/target/dist/bin/oebb_Main.jop 192.168.1.2

# do the whole build process including flash programming
# for BG and baseio (TAL)
bg: directories tools jopflash jopser prog_flash

#
#	some directories for configuration files
#
directories: jbc ttf rbf

jbc:
	mkdir jbc

ttf:
	mkdir ttf

rbf:
	mkdir rbf

#
# this line configures the FPGA and programs the PLD
# but uses a .jbc file
#
# However, the order is not so perfect. We would prefere to first
# program the PLD.
#
xxx:
	quartus_pgm -c $(BLASTER_TYPE) -m JTAG -o p\;jbc/cycbg.jbc
	quartus_pgm -c $(BLASTER_TYPE) -m JTAG -o p\;jbc/cyc_conf.jbc


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
		rm -rf quartus/$$target/db; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
		cd quartus/$$target && quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
	done
	cd quartus/$(DLPROJ) && quartus_pgm -c $(BLASTER_TYPE) -m JTAG jop.cdf
	e $(COM_PORT)


jop_testmon:
	cd asm && ./build.bat testmon
	@echo $(QPROJ)
	for target in $(QPROJ); do \
		echo "building $$target"; \
		rm -rf quartus/$$target/db; \
		qp="quartus/$$target/jop"; \
		echo $$qp; \
		quartus_map $$qp; \
		quartus_fit $$qp; \
		quartus_asm $$qp; \
		quartus_tan $$qp; \
		cd quartus/$$target && quartus_cpf -c jop.cdf ../../jbc/$$target.jbc; \
	done
	cd quartus/$(DLPROJ) && quartus_pgm -c $(BLASTER_TYPE) -m JTAG jop.cdf


#
#	UDP debugging
#
udp_dbg:
	java -cp java/pc/dist/lib/jop-pc.jar udp.UDPDbg

#
#	Rasmus's WCET analyser (start)
#	use
#		-Dlatex=true
#	to getv LaTeX friendly table output.
#
# WCETAnalyser options
# latex: it will output latex formatting in the tables (afterwards 
# replace ">" with "$>$ and "_" with "\_")
# dot:   it will generate directed graphs of basic blocks in dot format 
# (see: http://www.graphviz.org/)
# jline: it will insert Java source code into the bytecode tables
wca_rup:
	-rm -rf $(TARGET)/wcet
	-mkdir $(TARGET)/wcet
	-mkdir $(TARGET)/tmp
	java $(DEBUG_JOPIZER) $(TOOLS_CP) com.jopdesign.build.WcetPreprocess \
		-cp $(TARGET)/dist/lib/classes.zip -o $(TARGET)/tmp $(MAIN_CLASS)
	java -Xss16M $(TOOLS_CP) -Dlatex=true -Ddot=true -Djline=false -Dlatex=true -Ddfa=false -Dls=true com.jopdesign.wca_rup.WCETAnalyser \
		-mm $(WCET_METHOD) \
		-cp $(TARGET)/tmp -o $(TARGET)/wcet/$(P3)wcet.txt -sp $(TARGET_SOURCE) $(MAIN_CLASS)
	-rm -rf $(TARGET)/tmp

# WCET help
wcet_help:
	java $(TOOLS_CP) com.jopdesign.wcet.WCETAnalysis -help
# WCET analyzer
# make before     : java_app
# make after (dot): (cd java/target/wcet/<project-name>; make)
#
# Makefile options:
# WCET_DFA: perform dataflow analysis
# WCET_UPPAAL: whether to use modelchecking for WCET analysis
# WCET_VERIFYTA: UPPAAL verifier executable
# WCET_OPTIONS: Additional WCET options (run 'make wcet_help')
#
# Profiling: add -Xss16M -agentlib:hprof=cpu=samples,interval=2,depth=8 to java arguments
WCET_DFA?=no
WCET_UPPAAL?=no
WCET_VERIFYTA?=verifyta	 # only needed if WCET_UPPAAL=yes
wcet:
	-mkdir $(TARGET)/tmp
	java $(DEBUG_JOPIZER) $(TOOLS_CP) com.jopdesign.build.WcetPreprocess \
		-cp $(TARGET)/dist/lib/classes.zip -o $(TARGET)/tmp $(MAIN_CLASS)
	-mkdir -p $(TARGET)/wcet
	java \
	  $(TOOLS_CP) com.jopdesign.wcet.WCETAnalysis \
		-cp $(TARGET)/dist/classes -sp $(TARGET_SOURCE) \
		-app-class $(MAIN_CLASS) -target-method $(WCET_METHOD) \
		-outdir $(TARGET)/wcet \
		-dataflow-analysis $(WCET_DFA) \
		-uppaal $(WCET_UPPAAL) -uppaal-verifier $(WCET_VERIFYTA) \
		$(WCET_OPTIONS)	
	-rm -rf $(TARGET)/tmp


# dot2eps works for both rasmus WCETAnalyser and wcet.WCETAnalyser
dot2eps:
	cd $(TARGET)/wcet && make

dfa:
	java -Xss16M $(TOOLS_CP) com.jopdesign.dfa.Main \
		-cp $(TARGET)/dist/lib/classes.zip $(MAIN_CLASS)

test:
	java $(TOOLS_CP) com.jopdesign.wcet.CallGraph \
		-cp $(TARGET)/dist/lib/classes.zip -o $(TARGET)/wcet/$(P3)call.txt -sp $(TARGET_SOURCE) $(MAIN_CLASS)


###### end of Makefile #######







#
# some MS specific setting - just ignore it
#

# shortcut for my work in Eclipse on TCP/IP
eapp: ecl_app config_usb download

esim: ecl_app
	java $(DEBUG_JOPSIM) -cp java/tools/dist/lib/jop-tools.jar -Dlog="false" \
	com.jopdesign.tools.JopSim java/target/dist/bin/$(JOPBIN)

#
# do it from my eclipse workspace
#
ecl_app:
	cd ../../workspace/cvs_jop_target/classes && jar cf ../../../cpu/jop/java/target/dist/lib/classes.zip *
	java $(TOOLS_CP) -Dmgci=false com.jopdesign.build.JOPizer \
		-cp $(TARGET)/dist/lib/classes.zip -o $(TARGET)/dist/bin/$(JOPBIN) $(MAIN_CLASS)
	java $(TOOLS_CP) com.jopdesign.tools.jop2dat $(TARGET)/dist/bin/$(JOPBIN)
	cp *.dat modelsim
	rm -f *.dat

#
# test AppInfo
# MS: some temporary targets for AppInfo and libgraph tests
#
appinfo: tools
	java $(DEBUG_JOPIZER) $(TOOLS_CP) com.jopdesign.build.AppInfo \
		-cp $(TARGET)/dist/lib/classes.zip $(MAIN_CLASS)

testapp: tools
	make java_app
	-mkdir $(TARGET)/xxx
	java $(DEBUG_JOPIZER) $(TOOLS_CP) com.jopdesign.build.WcetPreprocess \
		-cp $(TARGET)/dist/lib/classes.zip -o $(TARGET)/xxx $(MAIN_CLASS)
	java $(DEBUG_JOPIZER) $(TOOLS_CP) -Dmgci=false com.jopdesign.build.JOPizer \
		-cp $(TARGET)/xxx -o $(TARGET)/dist/bin/$(JOPBIN) $(MAIN_CLASS)
