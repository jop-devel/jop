
#
# Targets:
#  prepare	Build tools
#  wcet_test	Run all wcet analyses
#

# Paths relative to this makefile

NEWTOOLS_ROOT=../..
OLDTOOLS_ROOT=../../../wcmem

OUTDIR=out


all: wcet_test

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
wcet_test:
	$(MAKE) testall -e P1=test P2=wcet P3=Loop     WCET_METHOD=foo
	$(MAKE) testall -e P1=test P2=wcet P3=StartKfl WCET_METHOD=measure  
	$(MAKE) testall -e P1=test P2=wcet P3=Dispatch WCET_METHOD=measure


### Targets to run a tests for a single app

.PHONY: testall testall_new testall_old 

testall: testall_new testall_old

testall_new:
	cd $(NEWTOOLS_ROOT) && $(MAKE) java_app -e P1=$(P1) P2=$(P2) P3=$(P3) 
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=0 WCET_DFA=no
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=0 WCET_DFA=yes
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=1 WCET_DFA=yes
	$(MAKE) do_newwcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) CSLENGTH=2 WCET_DFA=yes 

testall_old:
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
		 --projectname test -q"
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
	$(MAKE) wcet -e P1=$(P1) P2=$(P2) P3=$(P3) WCET_METHOD=$(WCET_METHOD) \
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
	
