COMPATIBILITY

RTTM currently supports the DE2 and the dspio board (Quartus projects de2-70rttm and cycrttm).


HOW TO USE RTTM

- Understand conventional JOP design flow.

- Adjust top level vhdl/top/joprttm.vhd or vhdl/top/jop_de2-70rttm.vhd generics.
- Set com.jopdesign.sys.Const.USE_RTTM in java/target/src/common/com/jopdesign/sys/Const.java to true. 
- Set com.jopdesign.build.JOPizer.USE_RTTM in java/tools/src/com/jopdesign/build/JOPizer.java to true.
- Set com.jopdesign.sys.Const.USE_RTTM_BIGMEM in java/target/src/common/com/jopdesign/sys/Const.java according to available SRAM.

- Set Makefile parameters as needed. RTTM-specific Makefile parameters:
QPROJ=cycrttm or QPROJ=de2-70rttm
USE_RTTM=yes 

- To compile processor and program and download program to target, run 'make all'.
- If you already compiled the processor and only want to compile and download the program, run 'make tools japp'.
- To simulate using Modelsim, run 'make tools tmsim'.


FILES OF INTEREST

TODO


KNOWN ISSUES

- The maximum size of the read or write set in a transaction not performing an early commit is one less than the capacity of the transaction cache.
- Garbage collection will not work if USE_RTTM is set.
- The constants in vhdl/simpcon/sc_arbiter_rttm_tdma.vhd are set for the DE2 board and could be lowered for the cyc12 board.
- The FIFO in vhdl/rttm/write_fifo_buffer.vhd is a direct instantation of Altera-specific IP.
- The generic intended_device_family in vhdl/rttm/write_fifo_buffer.vhd might need to be adjusted for other targets.
- 'make tools' must be run whenever com.jopdesign.sys.Const.USE_RTTM_BIGMEM is changed.


CHANGE HISTORY

2010-03-16 (Peter Hilber): Known issues
2010-01-14 (Peter Hilber): Instructions to build and run RTTM

