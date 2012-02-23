--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


--
--
--	Testbench for the jop
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

library work;
use work.i2c_pkg.all;

entity tb_jop is
end;

architecture tb of tb_jop is

component jop_iic is

port (
	clk		: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;
	ser_ncts		: in std_logic;
	ser_nrts		: out std_logic;

--
--	watchdog
--
	wd		: out std_logic;
	freeio	: out std_logic;

--
--	two ram banks
--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;
	ramb_a		: out std_logic_vector(17 downto 0);
	ramb_d		: inout std_logic_vector(15 downto 0);
	ramb_ncs	: out std_logic;
	ramb_noe	: out std_logic;
	ramb_nlb	: out std_logic;
	ramb_nub	: out std_logic;
	ramb_nwe	: out std_logic;

--
--	config/program flash and big nand flash
--
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic;

--
--	I/O pins of board
--
	io_b	: inout std_logic_vector(10 downto 1);
	io_l	: inout std_logic_vector(20 downto 1);
	io_r	: inout std_logic_vector(20 downto 1);
	io_t	: inout std_logic_vector(6 downto 1);
	
		oLEDR		: out std_logic_vector(17 downto 0);
--	oLEDG		: out std_logic_vector(7 downto 0);
	
--
--	Switches
--
	iSW			: in std_logic_vector(17 downto 0);
	
	sda : inout std_logic;
	scl : inout std_logic
--
--	dummy input pins for EP1C6 on board with EP1C12 pinout
--	EP1C12 has additional GND and VCCINT pins.
--
--	dummy_gnd		: out std_logic_vector(5 downto 0);
--	dummy_vccint	: out std_logic_vector(5 downto 0)
);
end component;


component memory is
	generic(add_bits : integer; data_bits : integer); 
	port(
		addr	: in std_logic_vector(add_bits-1 downto 0);
		data	: inout std_logic_vector(data_bits-1 downto 0);
		ncs		: in std_logic;
		noe		: in std_logic;
		nwr		: in std_logic);
 
end component;

component I2C_EEPROM
	generic (device : string (1 to 5) := "24C16");
	port (STRETCH : in    time := 1 ns;
	      E0      : in    std_logic := 'L';
	      E1      : in    std_logic := 'L';
	      E2      : in    std_logic := 'L';
	      WC      : in    std_logic := 'L';
	      SCL     : inout std_logic;
	      SDA     : inout std_logic);
	end component I2C_EEPROM;
	

-- To simulate a future I2C slave connected to the bus.
-- component i2c
-- 	port (clk             : in    std_logic;
-- 	      reset           : in    std_logic;
-- 	      sda             : inout std_logic;
-- 	      scl             : inout std_logic;
-- 	      device_addr     : in    std_logic_vector (6 downto 0);
-- 	      masl            : in    std_logic;
-- 	      strt            : in    std_logic;
-- 	      txrx            : in    std_logic;
-- 	      busy            : out   std_logic;
-- 	      t_const         : in    timming;
-- 	      tx_fifo_wr_ena  : in    std_logic;
-- 	      tx_fifo_full    : out   std_logic;
-- 	      data_in         : in    std_logic_vector (7 downto 0);
-- 	      tx_fifo_occ_in  : out   std_logic_vector (3 downto 0);
-- 	      tx_fifo_occ_out : out   std_logic_vector (3 downto 0);
-- 	      rx_fifo_rd_ena  : in    std_logic;
-- 	      rx_fifo_empty   : out   std_logic;
-- 	      data_out        : out   std_logic_vector (7 downto 0);
-- 	      rx_fifo_occ_in  : out   std_logic_vector (3 downto 0);
-- 	      rx_fifo_occ_out : out   std_logic_vector (3 downto 0));
-- 	end component i2c;


	signal clk		: std_logic := '1';
	signal ser_rxd	: std_logic := '1';

--
--	RAM connection. We use address and control lines only
--	from rama.
--
	signal ram_addr		: std_logic_vector(17 downto 0);
	signal ram_data		: std_logic_vector(31 downto 0);
	signal ram_noe		: std_logic;
	signal ram_ncs		: std_logic;
	signal ram_nwr		: std_logic;

	signal txd			: std_logic;
	
	signal sda_int, scl_int : std_logic;
	signal a, b : std_logic_vector(17 downto 0);

	-- size of main memory simulation in 32-bit words.
	-- change it to less memory to speedup the simulation
	-- minimum is 64 KB, 14 bits
	constant  MEM_BITS	: integer := 15;

begin

	sda_int <= 'H';
	scl_int <= 'H';

	joptop: jop_iic port map(
		clk => clk,
		ser_rxd => ser_rxd,
		ser_ncts => '0',
		ser_txd => txd,
		fl_rdy => '1',
		rama_a => ram_addr,
		rama_d => ram_data(15 downto 0),
		ramb_d => ram_data(31 downto 16),
		rama_noe => ram_noe,
		rama_ncs => ram_ncs,
		rama_nwe => ram_nwr,
		oLEDR => a, 
		iSW => b,
		sda => sda_int,
		scl => scl_int
		
	);

	main_mem: memory generic map(MEM_BITS, 32) port map(
			addr => ram_addr(MEM_BITS-1 downto 0),
			data => ram_data,
			ncs => ram_ncs,
			noe => ram_noe,
			nwr => ram_nwr
		);
		
	eeprom: component I2C_EEPROM
	generic map (
		device => "24C01"
		)
	port map (
		STRETCH => 5 ns,
		E0      => 'L',
		E1      => 'L',
		E2      => 'L',
		WC      => 'L',
		SCL     => scl_int,
		SDA     => sda_int
		);
		
--	100 MHz clock
		
clock : process
   begin
   wait for 5 ns; clk  <= not clk;
end process clock;

--
--	print out data from uart
--
process

	variable data : std_logic_vector(8 downto 0);
	variable l : line;

begin
	wait until txd='0';
	wait for 4.34 us;
	for i in 0 to 8 loop
		wait for 8.68 us;
		data(i) := txd;
	end loop;
	write(l, character'val(to_integer(unsigned(data(7 downto 0)))));
	writeline(output, l);

end process;

--
--	simulate download for jvm.asm test
--
process

	variable data : std_logic_vector(10 downto 0);
	variable l : line;

begin

	data := "11010100110";
	wait for 10 us;
	for i in 0 to 9 loop
		wait for 8.68 us;
		ser_rxd <= data(i);
	end loop;

end process;

end tb;

