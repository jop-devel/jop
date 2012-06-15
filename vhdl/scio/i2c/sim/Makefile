#VHDL_HOME=/home/jrri/Documents/DTU/phd/jop/vhdl/scio/i2c
VHDL_HOME=/home/jrri/Documents/jop/vhdl/scio/i2c
TOOLS_HOME=/home/jrri/Tools

SIM_HOME=$(VHDL_HOME)/sim

BENCH_HOME=$(VHDL_HOME)/bench

ISE_PATH=/opt/Xilinx/12.4/ISE_DS/ISE
XIL_VHDL_LIB=$(TOOLS_HOME)/xil_libs/vhdl


QUARTUS_PATH=/home/jrri/Tools/quartus91/quartus
ALT_VHDL_LIB=$(TOOLS_HOME)/alt_libs/vhdl_libs

#OPTIONS=-93 -quiet -check_synthesis -lint -pedanticerrors
OPTIONS=-93 -quiet	

sim_i2c: clean com
	vcom $(OPTIONS) $(VHDL_HOME)/i2c_pkg.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/main_i2c_sm_sync.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/scl_sm_sync.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/fifo_pkg.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/counter_synth.vhd
	#vcom $(OPTIONS) $(VHDL_HOME)/fifo/dual_port_ram_xil.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/dual_port_ram_alt.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/FIFO_read_control_syn.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/FIFO_write_control_syn.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/synchronizer_synth.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/async_fifo_syn.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/i2c.vhd
	vcom $(OPTIONS) $(BENCH_HOME)/clk_gen.vhd
	vcom $(OPTIONS) $(BENCH_HOME)/rst_gen.vhd
	vcom $(OPTIONS) $(BENCH_HOME)/i2c_eeprom.vhd
	vcom $(OPTIONS) $(BENCH_HOME)/i2c_tb.vhd
	vsim -do $(SIM_HOME)/sim_i2c_tb.do i2c_tb

sim_fifo: clean com
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/fifo_pkg.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/counter_synth.vhd
	#vcom $(OPTIONS) $(VHDL_HOME)/fifo/dual_port_ram_xil.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/dual_port_ram_alt.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/FIFO_read_control_syn.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/FIFO_write_control_syn.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/synchronizer_synth.vhd
	vcom $(OPTIONS) $(VHDL_HOME)/fifo/async_fifo_syn.vhd
	vcom $(OPTIONS) $(BENCH_HOME)/clk_gen.vhd
	vcom $(OPTIONS) $(BENCH_HOME)/rst_gen.vhd
	vcom $(OPTIONS) $(BENCH_HOME)/fifo_tb.vhd
	vsim -do $(SIM_HOME)/sim_fifo_tb.do fifo_tb

com:
	vlib work
	#vmap simprim $(XIL_VHDL_LIB)/simprim
	#vmap unimacro $(XIL_VHDL_LIB)/unimacro
	#vmap unisim $(XIL_VHDL_LIB)/unisim
	#vmap xilinxcorelib $(XIL_VHDL_LIB)/xilinxcorelib
	vmap altera_mf $(ALT_VHDL_LIB)/altera_mf

clean:

	-rm -rf work
	-rm -rf transcript
	-rm *.wlf
	-rm *.ini
