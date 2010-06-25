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
--	Testbench for the jop with 16 bit SRAM
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity tb_jop is
end;

architecture tb of tb_jop is

component jop is

port (
	clk				: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;

	oUART_CTS		: in std_logic;
	iUART_RTS		: out std_logic;

--
--	watchdog
--
	wd				: out std_logic;

--
--	only one ram bank
--
	oSRAM_A		 : out std_logic_vector(18 downto 0);		-- edit
	SRAM_DQ		 : inout std_logic_vector(31 downto 0);		-- edit
	oSRAM_CE1_N	 : out std_logic;
	oSRAM_OE_N	 : out std_logic;
	oSRAM_BE_N	 : out std_logic_vector(3 downto 0);
	oSRAM_WE_N	 : out std_logic;
	oSRAM_GW_N   : out std_logic;
	oSRAM_CLK	 : out std_logic;
	oSRAM_ADSC_N : out std_logic;
	oSRAM_ADSP_N : out std_logic;
	oSRAM_ADV_N	 : out std_logic;
	oSRAM_CE2	 : out std_logic;
	oSRAM_CE3_N  : out std_logic
);
end component;

	signal clk		: std_logic := '1';
	signal ser_rxd	: std_logic := '1';

--
--	RAM connection. We use address and control lines only
--	from rama.
--
	signal ram_addr		: std_logic_vector(18 downto 0);
	signal ram_data		: std_logic_vector(31 downto 0);
	signal oSRAM_CE1_N	: std_logic;
	signal oSRAM_OE_N	: std_logic;
	signal oSRAM_BE_N	: std_logic_vector(3 downto 0);
	signal oSRAM_WE_N	: std_logic;
	signal oSRAM_GW_N   : std_logic;
	signal oSRAM_CLK	: std_logic;
	signal oSRAM_ADSC_N : std_logic;
	signal oSRAM_ADSP_N : std_logic;
	signal oSRAM_ADV_N	: std_logic;
	signal oSRAM_CE2	: std_logic;
	signal oSRAM_CE3_N  : std_logic;
	signal pull_down    : std_logic;

	signal txd			: std_logic;

	-- size of main memory simulation in 32-bit words.
	-- change it to less memory to speedup the simulation
	-- minimum is 64 KB, 15 bits
	constant  MEM_BITS	: integer := 16;

begin

	joptop: jop port map(
		clk => clk,
		ser_txd => txd,
		ser_rxd => ser_rxd,
		oUART_CTS => '1',
		oSRAM_A => ram_addr,
		SRAM_DQ => ram_data(31 downto 0),
		oSRAM_CE1_N => oSRAM_CE1_N,
		oSRAM_OE_N => oSRAM_OE_N,
		oSRAM_BE_N => oSRAM_BE_N,
		oSRAM_WE_N => oSRAM_WE_N,
		oSRAM_GW_N => oSRAM_GW_N,
		oSRAM_CLK => oSRAM_CLK,
		oSRAM_ADSC_N => oSRAM_ADSC_N,
		oSRAM_ADSP_N => oSRAM_ADSP_N,
		oSRAM_ADV_N => oSRAM_ADV_N,
		oSRAM_CE2 => oSRAM_CE2,
		oSRAM_CE3_N => oSRAM_CE3_N
	);

	pull_down <= ram_data(0);
	
	main_mem: entity work.memory
		generic map (
			TimingChecksOn => true,

			tperiod_CLK_posedge => 5 ns,
			tpw_CLK_posedge => 2 ns,
			tpw_CLK_negedge => 2 ns,
			
			tpd_CLK_DQA0 => (others => 3.0 ns),
			tpd_OENeg_DQA0 => (others => 3.1 ns),

			tsetup_A0_CLK => 1.4 ns,
			tsetup_DQA0_CLK => 1.4 ns,
			tsetup_ADVNeg_CLK => 1.4 ns,
			tsetup_ADSCNeg_CLK => 1.4 ns,
			tsetup_CE2_CLK => 1.4 ns,
			tsetup_BWANeg_CLK => 1.4 ns,
			
			thold_A0_CLK => 0.4 ns,
			thold_DQA0_CLK => 0.4 ns,
			thold_ADVNeg_CLK => 0.4 ns,
			thold_ADSCNeg_CLK => 0.4 ns,
			thold_CE2_CLK => 0.4 ns,
			thold_BWANeg_CLK => 0.4 ns,

			tipd_A0 => (2 ns, 2 ns),
			tipd_A1 => (2 ns, 2 ns),
			tipd_A2 => (2 ns, 2 ns),
			tipd_A3 => (2 ns, 2 ns),
			tipd_A4 => (2 ns, 2 ns),
			tipd_A5 => (2 ns, 2 ns),
			tipd_A6 => (2 ns, 2 ns),
			tipd_A7 => (2 ns, 2 ns),
			tipd_A8 => (2 ns, 2 ns),
			tipd_A9 => (2 ns, 2 ns),
			tipd_A10 => (2 ns, 2 ns),
			tipd_A11 => (2 ns, 2 ns),
			tipd_A12 => (2 ns, 2 ns),
			tipd_A13 => (2 ns, 2 ns),
			tipd_A14 => (2 ns, 2 ns),
			tipd_A15 => (2 ns, 2 ns),
			tipd_A16 => (2 ns, 2 ns),
			tipd_A17 => (2 ns, 2 ns),
			tipd_A18 => (2 ns, 2 ns),

			tipd_DQA0 => (2 ns, 2 ns),
			tipd_DQA1 => (2 ns, 2 ns),
			tipd_DQA2 => (2 ns, 2 ns),
			tipd_DQA3 => (2 ns, 2 ns),
			tipd_DQA4 => (2 ns, 2 ns),
			tipd_DQA5 => (2 ns, 2 ns),
			tipd_DQA6 => (2 ns, 2 ns),
			tipd_DQA7 => (2 ns, 2 ns),
			tipd_DPA  => (2 ns, 2 ns),
			tipd_DQB0 => (2 ns, 2 ns),
			tipd_DQB1 => (2 ns, 2 ns),
			tipd_DQB2 => (2 ns, 2 ns),
			tipd_DQB3 => (2 ns, 2 ns),
			tipd_DQB4 => (2 ns, 2 ns),
			tipd_DQB5 => (2 ns, 2 ns),
			tipd_DQB6 => (2 ns, 2 ns),
			tipd_DQB7 => (2 ns, 2 ns),
			tipd_DPB  => (2 ns, 2 ns),
			tipd_DQC0 => (2 ns, 2 ns),
			tipd_DQC1 => (2 ns, 2 ns),
			tipd_DQC2 => (2 ns, 2 ns),
			tipd_DQC3 => (2 ns, 2 ns),
			tipd_DQC4 => (2 ns, 2 ns),
			tipd_DQC5 => (2 ns, 2 ns),
			tipd_DQC6 => (2 ns, 2 ns),
			tipd_DQC7 => (2 ns, 2 ns),
			tipd_DPC  => (2 ns, 2 ns),
			tipd_DQD0 => (2 ns, 2 ns),
			tipd_DQD1 => (2 ns, 2 ns),
			tipd_DQD2 => (2 ns, 2 ns),
			tipd_DQD3 => (2 ns, 2 ns),
			tipd_DQD4 => (2 ns, 2 ns),
			tipd_DQD5 => (2 ns, 2 ns),
			tipd_DQD6 => (2 ns, 2 ns),
			tipd_DQD7 => (2 ns, 2 ns),
			tipd_DPD  => (2 ns, 2 ns),
			
			tipd_BWANeg  => (2 ns, 2 ns),
			tipd_BWBNeg  => (2 ns, 2 ns),
			tipd_BWCNeg  => (2 ns, 2 ns),
			tipd_BWDNeg  => (2 ns, 2 ns),
			tipd_GWNeg   => (2 ns, 2 ns),
			tipd_BWENeg  => (2 ns, 2 ns),
			tipd_CLK     => (2 ns, 2 ns),
			tipd_CE1Neg  => (2 ns, 2 ns),
			tipd_CE2     => (2 ns, 2 ns),
			tipd_CE3Neg  => (2 ns, 2 ns),
			tipd_OENeg   => (2 ns, 2 ns),
			tipd_ADVNeg  => (2 ns, 2 ns),
			tipd_ADSPNeg => (2 ns, 2 ns),
			tipd_ADSCNeg => (2 ns, 2 ns)
			
			)
		port map(
		A0 => ram_addr(0),
		A1 => ram_addr(1),
		A2 => ram_addr(2),
		A3 => ram_addr(3),
		A4 => ram_addr(4),
		A5 => ram_addr(5),
		A6 => ram_addr(6),
		A7 => ram_addr(7),
		A8 => ram_addr(8),
		A9 => ram_addr(9),
		A10 => ram_addr(10),
		A11 => ram_addr(11),
		A12 => ram_addr(12),
		A13 => ram_addr(13),
		A14 => ram_addr(14),
		A15 => '0', -- ram_addr(15),
		A16 => '0', -- ram_addr(16),
		A17 => '0', -- ram_addr(17),
		A18 => '0', -- ram_addr(18),
		
		DQA0 => ram_data(0),
		DQA1 => ram_data(1),
		DQA2 => ram_data(2),
		DQA3 => ram_data(3),
		DQA4 => ram_data(4),
		DQA5 => ram_data(5),
		DQA6 => ram_data(6),
		DQA7 => ram_data(7),
		DPA => pull_down,
		DQB0 => ram_data(8),
		DQB1 => ram_data(9),
		DQB2 => ram_data(10),
		DQB3 => ram_data(11),
		DQB4 => ram_data(12),
		DQB5 => ram_data(13),
		DQB6 => ram_data(14),
		DQB7 => ram_data(15),
		DPB => pull_down,
		DQC0 => ram_data(16),
		DQC1 => ram_data(17),
		DQC2 => ram_data(18),
		DQC3 => ram_data(19),
		DQC4 => ram_data(20),
		DQC5 => ram_data(21),
		DQC6 => ram_data(22),
		DQC7 => ram_data(23),
		DPC => pull_down,
		DQD0 => ram_data(24),
		DQD1 => ram_data(25),
		DQD2 => ram_data(26),
		DQD3 => ram_data(27),
		DQD4 => ram_data(28),
		DQD5 => ram_data(29),
		DQD6 => ram_data(30),
		DQD7 => ram_data(31),
		DPD => pull_down,

		BWANeg => oSRAM_BE_N(0),
		BWBNeg => oSRAM_BE_N(1),
		BWCNeg => oSRAM_BE_N(2),
		BWDNeg => oSRAM_BE_N(3),
		GWNeg => oSRAM_GW_N,
		BWENeg => oSRAM_WE_N,
		CLK => oSRAM_CLK,
		CE1Neg => oSRAM_CE1_N,
		CE2 => oSRAM_CE2,
		CE3Neg =>  oSRAM_CE3_N,
		OENeg => oSRAM_OE_N,
		ADVNeg => oSRAM_ADV_N,
		ADSPNeg => oSRAM_ADSP_N,
		ADSCNeg => oSRAM_ADSC_N,
		MODE => '1',
		ZZ => '0'
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

