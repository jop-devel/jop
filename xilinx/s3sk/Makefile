############################################################################
# Makefile for command-line synthesis by Xilinx' ISE xflow.
#
# Requires:
#   - Xilinx xflow
#
# Inputs:
#   - Flow Specification: fpga.flw xst.opt high_effort.opt bitgen.opt
#   - Design:             <design>.prj <design>.ucf
#   -> Add a '# Device: ' and a '# Top: ' comment to your PRJ to
#      specify the FPGA device and the top-level component.
#
# Author:
#   Thomas B. Preusser <thomas.preusser@tu-dresden.de>
#
############################################################################
XFLOW     ?= xflow      # Provide path manually if not in your $PATH
DEFAULT   := jop.bit    # Default to JOP
MAKEFLAGS += --silent

.PHONY: default clobber
.SECONDARY:

# Default Target
default:
	make $(DEFAULT)

# Include Dependencies
-include $(basename $(MAKECMDGOALS)).dep

# Generation of Dependencies
%.dep: %.prj
	@set -e; \
	sed -ne's/^\# Device: \(.*\)$$/DEVICE := \1/p' $<>$@; \
	sed -ne's/^\# Top: \(.*\)$$/TOPLEVEL := \1/p' $<>>$@; \
	echo -n '$*.ngc: ' >>$@; \
	sed -ne's/^vhdl work "\(.*\)"$$/\1/p' $<|tr '\n' ' '>>$@; \
	echo >>$@

# Bitfile from a placed and routed Netlist
%.bit: %.ncd bitgen.opt fpga.flw
	$(XFLOW) -config bitgen.opt $<

# Place and route Netlist
%.ncd: %.ngc %.ucf high_effort.opt fpga.flw
	$(XFLOW) -p $(DEVICE) -g ucf:$*.ucf -implement high_effort.opt $<

# Generate Netlist from Project
%.ngc: %.prj xst.opt fpga.flw
	$(XFLOW) -p $(DEVICE) -g toplevel:$(TOPLEVEL) -synth xst.opt $<

# Missing flow files
%.flw %.opt:
	if [ ! -f "$@" ]; then echo "Missing flow file: '$@'."; exit 1; fi

# Cleanup *thoroughly*
clobber:
	rm -rf $$(find . -mindepth 1 -maxdepth 1 -name Makefile -o -name '*.flw' -o -name '*.opt' -o -name '*.prj' -o -name '*.ucf' -o -print)
