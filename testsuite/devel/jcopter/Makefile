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
USE_WCA=yes
WCEP_EF?=false
DEFAULT_OPTS=--greedy.targets wca \
             --greedy.dump-stats greedy.csv \
             --inline.exclude-wca-targets true \
             --opt.cleanup-cp false \
             --opt.remove-unused-members false \
             --greedy.order WCAFirst \
             --greedy.use-wcep true \
	     --greedy.use-wcep-ef $(WCEP_EF) \
	     --greedy.cache-analysis ALL_FIT_REGIONS \
	     --greedy.wca-cache-analysis ALL_FIT_REGIONS 
#OPTS=--greedy.cache-analysis ALWAYS_MISS --greedy.wca-cache-analysis ALWAYS_MISS
#OPTS=--greedy.cache-analysis ALWAYS_MISS_OR_HIT
#OPTS=--greedy.cache-analysis ALWAYS_HIT
#OPTS=--greedy.order TopDown
#OPTS=--greedy.order BottomUp 
#OPTS=--use-wca false
#OPTS=--use-wca false --greedy.cache-analysis ALWAYS_HIT
#OPTS=-O 1  
#OPTS=--opt.simple-inliner false

# Options for JCopter and WCETAnalysis
P1=test
P2=wcet
WCET_METHOD=measure
USE_DFA=yes
CALLSTRING_LENGTH=0
USE_SCOPES=true

papa_opt:
	$(MAKE) P1=bench P2=papabench/jop P3=PapaBenchJopApplication \
		WCET_METHOD=papabench.core.fbw.tasks.handlers.CheckMega128ValuesTaskHandler.run \
		CALLSTRING_LENGTH=0 bench_opt
				
papa_orig:
	$(MAKE) P1=bench P2=papabench/jop P3=PapaBenchJopApplication \
		WCET_METHOD=papabench.core.fbw.tasks.handlers.CheckMega128ValuesTaskHandler.run \
		CALLSTRING_LENGTH=0 bench_orig

papa_down:
	$(MAKE) P1=bench P2=papabench/jop P3=PapaBenchJopApplication down


debie_opt:
	$(MAKE) P1=bench P2=debie/harness P3=Harness CALLSTRING_LENGTH=0 USE_SCOPES=false \
		OPTS="${OPTS} --inline.exclude-wca-targets false" bench_opt  

debie_orig:
	$(MAKE) P1=bench P2=debie/harness P3=Harness CALLSTRING_LENGTH=0 USE_SCOPES=false bench_orig  

debie_down:
	$(MAKE) P1=bench P2=debie/harness P3=Harness CALLSTRING_LENGTH=0 USE_SCOPES=false down_orig	

debie_aopt:
	$(MAKE) P1=bench P2=debie/harness P3=Harness CALLSTRING_LENGTH=0 USE_SCOPES=false \
		OPTS="${OPTS} --inline.exclude-wca-targets false" down_opt	

debie_dopt_all:
	$(MAKE) WCET_METHOD="debie.telecommand.TelecommandExecutionTask.tcInterruptService \
	                     debie.telecommand.TelecommandExecutionTask.tmInterruptService \
			     debie.particles.AcquisitionTask.handleHitTrigger              \
			     debie.telecommand.TelecommandExecutionTask.handleTelecommand  \
			     debie.particles.AcquisitionTask.handleAcquisition" debie_aopt

debie_opt_all:
	$(MAKE) WCET_METHOD="debie.telecommand.TelecommandExecutionTask.tcInterruptService \
	                     debie.telecommand.TelecommandExecutionTask.tmInterruptService \
			     debie.particles.AcquisitionTask.handleHitTrigger              \
			     debie.telecommand.TelecommandExecutionTask.handleTelecommand  \
			     debie.particles.AcquisitionTask.handleAcquisition" debie_opt

#debie_down:
#	$(MAKE) P1=bench P2=debie/harness P3=Harness down

# I am sure there is a nicer way to do this but I am too lazy now to think of something .. 
debie_opt_1:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tcInterruptService debie_opt
debie_opt_2:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tmInterruptService debie_opt
debie_opt_3:
	$(MAKE) WCET_METHOD=debie.particles.AcquisitionTask.handleHitTrigger debie_opt
debie_opt_4:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.handleTelecommand debie_opt
debie_opt_5:
	$(MAKE) WCET_METHOD=debie.particles.AcquisitionTask.handleAcquisition  debie_opt
debie_orig_1:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tcInterruptService debie_orig
debie_orig_2:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tmInterruptService debie_orig
debie_orig_3:
	$(MAKE) WCET_METHOD=debie.particles.AcquisitionTask.handleHitTrigger debie_orig
debie_orig_4:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.handleTelecommand debie_orig
debie_orig_5:
	$(MAKE) WCET_METHOD=debie.particles.AcquisitionTask.handleAcquisition debie_orig
debie_dopt_1:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tcInterruptService debie_aopt
debie_dopt_2:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.tmInterruptService debie_aopt
debie_dopt_3:
	$(MAKE) WCET_METHOD=debie.particles.AcquisitionTask.handleHitTrigger debie_aopt
debie_dopt_4:
	$(MAKE) WCET_METHOD=debie.telecommand.TelecommandExecutionTask.handleTelecommand debie_aopt
debie_dopt_5:
	$(MAKE) WCET_METHOD=debie.particles.AcquisitionTask.handleAcquisition debie_aopt


bench_opt:
	$(MAKE) USE_JCOPTER=yes do_bench_wcet

bench_orig:
	$(MAKE) USE_JCOPTER=no do_bench_wcet 

down_opt:
	$(MAKE) USE_JCOPTER=yes do_bench_acet

down_orig:
	$(MAKE) USE_JCOPTER=no do_bench_acet 

down:
	cd $(GIT_ROOT) && $(MAKE) USB=true BLASTER_TYPE=USB-Blaster \
	        P1=$(P1) P2=$(P2) P3=$(P3) CALLSTRING_LENGTH=$(CALLSTRING_LENGTH) USE_DFA=$(USE_DFA) \
		config download 

# Internal targets
		
do_bench_wcet:
	cd $(GIT_ROOT) && $(MAKE) JCOPTER_USE_WCA=$(USE_WCA) USE_SCOPES=$(USE_SCOPES) USE_SCOPECHECKS=false MEASURE=false \
	        P1=$(P1) P2=$(P2) P3=$(P3) CALLSTRING_LENGTH=$(CALLSTRING_LENGTH) USE_DFA=$(USE_DFA) \
		JCOPTER_OPT="-O 3 --dfa-cache-dir $(DFA_CACHE_DIR) --use-wca $(USE_WCA) $(DEFAULT_OPTS) $(OPTS)" java_app
	cd $(GIT_ROOT) && $(MAKE) WCET_OPTIONS="--no-load-linkinfo --no-ipet-dump-ilp --dfa-cache-dir $(DFA_CACHE_DIR) --dfa-analyze-boot true" \
        	P1=$(P1) P2=$(P2) P3=$(P3) CALLSTRING_LENGTH=$(CALLSTRING_LENGTH) USE_DFA=$(USE_DFA) \
		wcet
	@echo "Options: " $(P3) " --use-wca " $(USE_WCA) $(DEFAULT_OPTS) $(OPTS) 

do_bench_acet:
	cd $(GIT_ROOT) && $(MAKE) JCOPTER_USE_WCA=$(USE_WCA) USE_SCOPES=$(USE_SCOPES) USE_SCOPECHECKS=false MEASURE=true \
	        P1=$(P1) P2=$(P2) P3=$(P3) CALLSTRING_LENGTH=$(CALLSTRING_LENGTH) USE_DFA=$(USE_DFA) \
		JCOPTER_OPT="-O 3 --dfa-cache-dir $(DFA_CACHE_DIR) --use-wca $(USE_WCA) $(DEFAULT_OPTS) $(OPTS)" java_app
	cd $(GIT_ROOT) && $(MAKE) USB=true BLASTER_TYPE=USB-Blaster \
		P1=$(P1) P2=$(P2) P3=$(P3) CALLSTRING_LENGTH=$(CALLSTRING_LENGTH) USE_DFA=$(USE_DFA) \
		config download 
	@echo "Options: " $(P3) " --use-wca " $(USE_WCA) $(DEFAULT_OPTS) $(OPTS) 
