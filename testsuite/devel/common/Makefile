#
# This Makefile is a test script which can be used to compare the WCET results between two
# branch checkouts. This was developed during the switch to the new common framework.
#
# Author: Stefan Hepp <stefan@stefant.org> 
#
# Usage: Set the checkout root dirs relative to his dir, run 'make' in this directory 
# 
# Targets:
#  prepare	Build tools
#  all		Run tests, cleanup results, compare results
#  wcet_test	Run all wcet analyses (Use -e TEST=testnew to rerun tests with newtools only)
#  cleanup	Remove uncomparable results (i.e. analysis time,..) from result files  
#  compare      Create diff on .csv files
#

# Paths relative to this makefile

NEWTOOLS_ROOT=../..
OLDTOOLS_ROOT=../../../master

OUTDIR=out

# Sourcepath to use for newtools, relative to newtools root
NEWTOOLS_SRC=../master/java/target/src


all: wcet_test cleanup compare

### Targets to prepare checkouts for building

.PHONY: prepare prepare_old prepare_new

prepare: prepare_new prepare_old

prepare_old:
	(cd $(OLDTOOLS_ROOT); $(MAKE) tools; $(MAKE) gen_mem -e ASM_SRC=jvm JVM_TYPE=USB)
	
prepare_new:
	(cd $(NEWTOOLS_ROOT); $(MAKE) tools; $(MAKE) gen_mem -e ASM_SRC=jvm JVM_TYPE=USB)


### Targets to run tests 

.PHONY: wcet_test clean

clean:
	-rm -rf $(OUTDIR)
 
# Target to generate WCET results for a number of test programs
TEST?=testall

wcet_test:
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=Loop                 WCET_METHOD=foo
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=LoopBoundTest        WCET_METHOD=measure
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=FUCAMethod           WCET_METHOD=measure
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=Dispatch             WCET_METHOD=measure
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=DispatchI            WCET_METHOD=measure
	#$(MAKE) $(TEST) -e P1=test P2=wcet P3=DispatchViaException WCET_METHOD=measure
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=StartKfl             WCET_METHOD=measure  
	#$(MAKE) $(TEST) -e P1=test P2=wcet P3=StartBenchUdpIp      WCET_METHOD=measure  
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=StartLift            WCET_METHOD=measure  
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=StartLineFollower    WCET_METHOD=measure  
	$(MAKE) $(TEST) -e P1=test P2=wcet P3=StartDSVM            WCET_METHOD=measure
	# Cycles in callgraph
	#$(MAKE) $(TEST) -e P1=test P2=wcet P3=BytecodeVerification WCET_METHOD=measure
	#$(MAKE) $(TEST) -e P1=test P2=wcet P3=Delegator            WCET_METHOD=measure
	#$(MAKE) $(TEST) -e P1=test P2=wcet P3=StartEjipCmp         WCET_METHOD=measure  

# Target to compare old and new results
.PHONY: cleanup compare

cleanup:
	for i in `ls $(OUTDIR)/new/csv/*.csv`; do \
	  cp $$i $(OUTDIR)/tmp.csv; \
	  grep -v "metric;date" $(OUTDIR)/tmp.csv | \
	    sed "s/wcet;ipet;\([0-9]*\);\([0-9]*\);\([0-9]*\);\([.0-9]*\);\([.0-9]*\);\(.*\)/wcet;ipet;\1;\2;\3;\6/" > $$i; done	
	for i in `ls $(OUTDIR)/old/csv/*.csv`; do \
	  cp $$i $(OUTDIR)/tmp.csv; \
	  grep -v "metric;date" $(OUTDIR)/tmp.csv | grep -v "min-cache-cost" | \
	    sed "s/wcet;ipet;\([0-9]*\);\([0-9]*\);\([0-9]*\);\([.0-9]*\);\([.0-9]*\);\(.*\)/wcet;ipet;\1;\2;\3;\6/" > $$i; done	

compare:
	-diff -r $(OUTDIR)/old/csv $(OUTDIR)/new/csv > $(OUTDIR)/csv.diff

### Targets to run a tests for a single app

.PHONY: testall testnew

testall: testnew testold 

testnew:
	cd $(NEWTOOLS_ROOT) && $(MAKE) java_app -e P1=$(P1) P2=$(P2) P3=$(P3) TARGET_SRC_PATH=$(NEWTOOLS_SRC)
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=0 WCET_DFA=no
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=0 WCET_DFA=yes
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=1 WCET_DFA=yes
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=2 WCET_DFA=yes 

testold:
	cd $(OLDTOOLS_ROOT) && $(MAKE) java_app -e P1=$(P1) P2=$(P2) P3=$(P3) 
	$(MAKE) do_oldwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=0 WCET_DFA=no
	$(MAKE) do_oldwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=0 WCET_DFA=yes
	$(MAKE) do_oldwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=1 WCET_DFA=yes
	$(MAKE) do_oldwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=2 WCET_DFA=yes 


### Internal target to perform a test		

WCET_DFA?=no
CSLENGTH?=0

# TODO use .asm file from newtools for both tests 

WCETNEW_OPTIONS="--callstring-length $(CSLENGTH) --result-file java/target/wcet/test/report.csv \
		 --projectname test"
WCETOLD_OPTIONS="-callstring-length $(CSLENGTH) -result-file java/target/wcet/test/report.csv \
		 -projectname test"


.PHONY: do_newwcet do_oldwcet	

ifeq ($(WCET_DFA),yes)
  result:=$(P1).$(P2).$(P3)_cs$(CSLENGTH)_dfa
else
  result:=$(P1).$(P2).$(P3)_cs$(CSLENGTH)
endif

do_newwcet: resultdir:=$(OUTDIR)/new/$(result)
do_newwcet: csvdir   :=$(OUTDIR)/new/csv
do_oldwcet: resultdir:=$(OUTDIR)/old/$(result)
do_oldwcet: csvdir   :=$(OUTDIR)/old/csv


do_newwcet: 
	-rm -rf $(NEWTOOLS_ROOT)/java/target/wcet/test
	cd $(NEWTOOLS_ROOT) && \
	$(MAKE) wcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) TARGET_SRC_PATH=$(NEWTOOLS_SRC) \
		WCET_DFA=$(WCET_DFA) WCET_OPTIONS=$(WCETNEW_OPTIONS)
	-rm -rf $(resultdir)
	-mkdir -p $(resultdir)
	-mkdir -p $(csvdir)
	cp -a $(NEWTOOLS_ROOT)/java/target/wcet/test/report $(resultdir)/
	cp -a $(NEWTOOLS_ROOT)/java/target/wcet/test/report.csv $(csvdir)/$(result).csv

do_oldwcet:
	-rm -rf $(OLDTOOLS_ROOT)/java/target/wcet
	cd $(OLDTOOLS_ROOT) && \
	$(MAKE) wcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) \
		WCET_DFA=$(WCET_DFA) WCET_OPTIONS=$(WCETOLD_OPTIONS)
	-rm -rf $(resultdir)
	-mkdir -p $(resultdir)
	-mkdir -p $(csvdir)
	cp -a $(OLDTOOLS_ROOT)/java/target/wcet/test/report $(resultdir)/
	cp -a $(OLDTOOLS_ROOT)/java/target/wcet/test/report.csv $(csvdir)/$(result).csv
	

