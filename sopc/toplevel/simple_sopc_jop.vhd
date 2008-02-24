--
--  This file is part of JOP, the Java Optimized Processor
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
--	simple_sopc_jop.vhd
--
--	Author: Martin Schoeberl (martin@jopdesign.com)
--
--	top level for SOPC/JOP experiments
--
--	Just the minimum version with a 256x32 SRAM, boot UART,
--	and a watchdog LED
--
--	2006-08-10	created from jopcyc.vhd
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config.all;

entity simple_sopc_jop is

port (
	clk			: in std_logic;

	--
	--	serial interface to download the
	--	Java application and System.in/out
	--
	ser_rxd		: in std_logic;
	ser_txd		: out std_logic;

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

	-- watchdog LED
	wd			: out std_logic
);
end simple_sopc_jop;

architecture rtl of simple_sopc_jop is

	component pll is
	generic (multiply_by : natural; divide_by : natural);
	port (
		inclk0		: in std_logic;
		c0			: out std_logic
	);
	end component;

	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	-- for generation of internal reset
	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

	signal byte_nena		: std_logic_vector(3 downto 0);
	signal address			: std_logic_vector(19 downto 0);

	signal ncs, noe, nwe	: std_logic; 

	signal reset_n			: std_logic;

begin

	pll_inst : pll generic map(
		multiply_by => pll_mult,
		divide_by => pll_div
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);
-- if you don't like the PLL use this:
-- clk_int <= clk;

	--
	--	internal reset
	--	no external reset needed
	--
	process(clk_int)
	begin
		if rising_edge(clk_int) then
			if (res_cnt/="111") then
				res_cnt <= res_cnt+1;
			end if;

			int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
		end if;
	end process;

	reset_n <= not int_res;

	--	the SOPC generated top level
	jop: work.jop_system port map (
		clk => clk_int,
		reset_n => reset_n,
		ser_rxd_to_the_jop_avalon_0 => ser_rxd,
		ser_txd_from_the_jop_avalon_0 => ser_txd,
		wd_from_the_jop_avalon_0 => wd,

		-- the_tri_state_bridge_0_avalon_slave
		chipselect_n_to_the_ext_ram => ncs,
		read_n_to_the_ext_ram => noe,
		tri_state_bridge_0_address => address,
		tri_state_bridge_0_byteenablen => byte_nena,
		tri_state_bridge_0_data(31 downto 16) => ramb_d,
		tri_state_bridge_0_data(15 downto 0) => rama_d,
		write_n_to_the_ext_ram => nwe
	);
	
	rama_nlb <= byte_nena(0);
	rama_nub <= byte_nena(1);
	ramb_nlb <= byte_nena(2);
	ramb_nub <= byte_nena(3);

	rama_ncs <= ncs;
	rama_noe <= noe;
	rama_nwe <= nwe;
	ramb_ncs <= ncs;
	ramb_noe <= noe;
	ramb_nwe <= nwe;

	-- A0/1 from the avalon interface is NC on 32-bit SRAM
	rama_a <= address(19 downto 2);
	ramb_a <= address(19 downto 2);

end rtl;
