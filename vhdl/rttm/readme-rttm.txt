REAL-TIME TRANSACTIONAL MEMORY IMPLEMENTATION README 



*** INDEX

Compatibility
How to use RTTM
Files/folders of interest
Known issues
Change history


*** COMPATIBILITY

RTTM currently supports the DE2 and the dspio board (Quartus projects de2-70rttm and cycrttm). Support for the dspio board is very limited, since only 2 cores+RTTM modules fit on the EP1C12. 


*** HOW TO USE RTTM

- See http://www.jopwiki.com/Getting_started to learn about the conventional JOP design flow.
- The RTTM design flow differs as follows from the conventional CMP design flow:

- Adjust top level vhdl/top/jop_de2-70rttm.vhd (DE2 board) or vhdl/top/joprttm.vhd (dspio board) generics.
- Set com.jopdesign.sys.Const.USE_RTTM in java/target/src/common/com/jopdesign/sys/Const.java to true. 
- Set com.jopdesign.sys.Const.USE_RTTM_BIGMEM in java/target/src/common/com/jopdesign/sys/Const.java according to available SRAM (true if using DE2 board, false if using dspio board).
- Set com.jopdesign.build.JOPizer.USE_RTTM in java/tools/src/com/jopdesign/build/JOPizer.java to true.

- Set Makefile parameters as needed. RTTM-specific Makefile parameters:
QPROJ=de2-70rttm or QPROJ=cycrttm
USE_RTTM=yes

- To compile processor and program and download program to target, run 'make all'.
- If you already compiled the processor and only want to compile and download the program, run 'make tools japp'.
- To simulate using Modelsim, run 'make tools tmsim'.
- The programming interface is described in the thesis "Hardware Transactional Memory for a Real-Time Chip Multiprocessor", section "Programming interface". 
- Example programs are in java/target/src/test/rttm.

- Hope you will have some fun, and no inexplicable crashes ;)


*** FILES/FOLDERS OF INTEREST

java/target/src/common/rttm/* implements the programming interface
java/target/src/test/rttm/jsim/* contains benchmark programs used for a RTTM behavioral level simulation (see target jtmsim in the Makefile), which are not directly compatible to the RTTM implementation
java/target/src/test/rttm/tests/* contains test and example programs; run 'make tools rttm_tests' to run the tests contained in the top folder
java/target/src/test/rttm/tests/manual/Transaction.java is the conceptual reference of an atomic method and the base for the generation of an atomic method
java/tools/src/com/jopdesign/build/ReplaceAtomicAnnotation.java creates the transaction wrapper at link time
quartus/cycrttm/jop.qpf is the Quartus project for the dspio board
quartus/de2-70rttm/jop.qpf is the Quartus project for the DE2 board
vhdl/rttm/* contains the RTTM module
vhdl/rttm/sim/* tests some RTTM module functionality (invoked using make -C vhdl/rttm/sim/)
vhdl/rttm/performance-measurements.patch for maximum performance, but without instrumentation; RTTM-only; apply using git apply ...
vhdl/rttm/tm_state_machine.vhd implements the state machine
vhdl/rttm/tm.vhd implements most of the transaction cache
vhdl/rttm/tag.vhd implements the tag memory
vhdl/rttm/tm_coordinator.vhd implements the transaction coordinator
vhdl/simpcon/sc_arbiter_rttm_tdma.vhd implements the modified TDMA arbiter
vhdl/top/jop_de2-70rttm.vhd is the top level entity for the DE2 board
vhdl/top/joprttm.vhd is the top level entity for the dspio board


*** KNOWN ISSUES

- The maximum size of the read or write set in a transaction not performing an early commit is _one less_ than the capacity of the transaction cache.
- Garbage collection will not work if USE_RTTM is set.
- 'make tools' must be run whenever com.jopdesign.sys.Const.USE_RTTM_BIGMEM is changed.
- The constants in vhdl/simpcon/sc_arbiter_rttm_tdma.vhd are set for the DE2 board and could be lowered for the cyc12 board.
- The FIFO in vhdl/rttm/write_fifo_buffer.vhd is a direct instantiation of Altera-specific IP.
- The generic intended_device_family in vhdl/rttm/write_fifo_buffer.vhd might need to be adjusted for other targets.
- Instrumentation data for each CPU may only be accessed using the respective CPU.  
- TDMA slots are only reallocated while a processor is executing a hardware transaction, not during the entire software transaction.
- On a write during a transaction, the tm_cache flag is assumed to be set (which is always the case in the current implementation). 


*** CHANGE HISTORY

2010-05-16 (Peter Hilber): Amendments
2010-04-25 (Peter Hilber): Amendments
2010-04-24 (Peter Hilber): Amendments
2010-04-18 (Peter Hilber): Amendments
2010-03-16 (Peter Hilber): Known issues
2010-01-14 (Peter Hilber): Instructions to build and run RTTM
