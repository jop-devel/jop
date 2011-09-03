#
# Benchmark script to compare ACET and WCET of optimized and unoptimized versions of various benchmarks
# with various options. Implemented as Makefile so that this is (hopefully) platform-independent.
# 
# Author: Stefan Hepp <stefan@stefant.org>
#
# Usage: 
#  Run 'make tools' in your git-root
#  Run 'make P3=<StartLift|..> USE_WCA=yes ...' in this directory
# 
# Targets:
#  bench_opt   Compile benchmark, optimize and run WCET analysis; you must set at least P3
#  bench_orig  Compile benchmark, and run WCET analysis; you must set at least P3
#  down        Download previously compiled benchmark; you must set at least P3
#  down_opt    Compile benchmark with MEASURE=true, optimize and download; you must set at least P3
#  down_orig   Compile benchmark with MEASURE=true and download; you must set at least P3
#  debie_opt   Compile debie benchmark, optimize and run WCET analysis  
#  debie_orig  Compile debie benchmark, run WCET analysis
#  debie_down  Download previously compiled debie benchmark
#
# The debie java sources are expected to be checked out into java/target/src/bench/debie   
#
# TODO: Automate this stuff
# 

# Paths relative to your working dir
GIT_ROOT=../../..

# Paths relative to git root
DFA_CACHE_DIR=../dfa-cache

# Options for JCopter
USE_WCA?=yes
DEFAULT_OPTS=--greedy.targets wca \
             --greedy.use-wcep true \
	     --greedy.use-wcep-ef true \
	     --greedy.cache-analysis ALWAYS_MISS_OR_HIT \
	     --greedy.wca-cache-analysis ALL_FIT_REGIONS 
OPTS?=

# Options for JCopter and WCETAnalysis
P1?=test
P2?=wcet
WCET_METHOD?=measure
USE_DFA?=yes
CS_LENGTH?=1

debie_opt:
	$(MAKE) -e P1=bench P2=debie/harness P3=Harness WCET_METHOD=$(WCET_METHOD) USE_DFA=$(USE_DFA)\
		OPTS="$(OPTS)" USE_WCA=$(USE_WCA) CS_LENGTH=0 bench_opt  

debie_orig:
	$(MAKE) -e P1=bench P2=debie/harness P3=Harness WCET_METHOD=$(WCET_METHOD) USE_DFA=$(USE_DFA)\
		OPTS="$(OPTS)" USE_WCA=$(USE_WCA) CS_LENGTH=0 bench_orig  

debie_down:
	$(MAKE) -e P1=bench P2=debie/harness P3=Harness down

debie_opt_1:
	$(MAKE) -e WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tcInterruptService \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_opt
debie_opt_2:
	$(MAKE) -e WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tmInterruptService \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_opt
debie_opt_3:
	$(MAKE) -e WCET_METHOD=debie.particles.AcquisitionTask.handleHitTrigger  \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_opt
debie_opt_4:
	$(MAKE) -e WCET_METHOD=debie.telecommand.TelecommandExecutionTask.handleTelecommand \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_opt
debie_opt_5:
	$(MAKE) -e WCET_METHOD=debie.particles.AcquisitionTask.handleAcquisition  \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_opt
debie_orig_1:
	$(MAKE) -e WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tcInterruptService \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_orig
debie_orig_2:
	$(MAKE) -e WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tmInterruptService \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_orig
debie_orig_3:
	$(MAKE) -e WCET_METHOD=debie.particles.AcquisitionTask.handleHitTrigger  \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_orig
debie_orig_4:
	$(MAKE) -e WCET_METHOD=debie.telecommand.TelecommandExecutionTask.handleTelecommand \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_orig
debie_orig_5:
	$(MAKE) -e WCET_METHOD=debie.particles.AcquisitionTask.handleAcquisition  \
		USE_DFA=$(USE_DFA) OPTS="$(OPTS)" USE_WCA=$(USE_WCA) debie_orig


bench_opt:
	$(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) USE_DFA=$(USE_DFA) USE_JCOPTER=yes \
		JCOPTER_USE_WCA=$(USE_WCA) OPTS="$(OPTS)" CS_LENGTH=$(CS_LENGTH) do_bench_wcet

bench_orig:
	$(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) USE_DFA=$(USE_DFA) USE_JCOPTER=no \
		CS_LENGTH=$(CS_LENGTH) do_bench_wcet 

down_opt:
	$(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) USE_DFA=$(USE_DFA) USE_JCOPTER=yes \
		JCOPTER_USE_WCA=$(USE_WCA) OPTS="$(OPTS)" CS_LENGTH=$(CS_LENGTH) do_bench_acet

down_orig:
	$(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) USE_DFA=$(USE_DFA) USE_JCOPTER=no \
		CS_LENGTH=$(CS_LENGTH) do_bench_acet 

down:
	cd $(GIT_ROOT) && $(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) USB=true BLASTER_TYPE=USB-Blaster \
		config download 

# Internal targets
		
do_bench_wcet:
	cd $(GIT_ROOT) && $(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) \
		USE_DFA=$(USE_DFA) JCOPTER_USE_WCA=$(USE_WCA) USE_JCOPTER=$(USE_JCOPTER) \
		CALLSTRING_LENGTH=$(CS_LENGTH) MEASURE=false \
		JCOPTER_OPT="-O 3 --dfa-cache-dir $(DFA_CACHE_DIR) $(DEFAULT_OPTS) $(OPTS)" java_app
	cd $(GIT_ROOT) && $(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) \
		USE_DFA=$(USE_DFA) WCET_OPTIONS="--no-load-linkinfo --dfa-cache-dir $(DFA_CACHE_DIR)" \
		CALLSTRING_LENGTH=$(CS_LENGTH) wcet
	@echo "Options: " $(P3) " --use-wca " $(USE_WCA) $(DEFAULT_OPTS) $(OPTS) 

do_bench_acet:
	cd $(GIT_ROOT) && $(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) \
		USE_DFA=$(USE_DFA) JCOPTER_USE_WCA=$(USE_WCA) USE_JCOPTER=$(USE_JCOPTER) \
		CALLSTRING_LENGTH=$(CS_LENGTH) MEASURE=true \
		JCOPTER_OPT="-O 3 --dfa-cache-dir $(DFA_CACHE_DIR) $(DEFAULT_OPTS) $(OPTS)" java_app
	cd $(GIT_ROOT) && $(MAKE) -e P1=$(P1) P2=$(P2) P3=$(P3) USB=true BLASTER_TYPE=USB-Blaster \
		config download 
	@echo "Options: " $(P3) " --use-wca " $(USE_WCA) $(DEFAULT_OPTS) $(OPTS) 
