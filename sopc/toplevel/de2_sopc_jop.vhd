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
--	de2_sopc_jop.vhd
--
--	Author: Martin Schoeberl (martin@jopdesign.com)
--
--	top level for SOPC/JOP experiments
--
--	Just the minimum version with a 256x16 SRAM, boot UART,
--	and a watchdog LED
--
--	2006-08-10	created from jopcyc.vhd
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config.all;

entity de2_sopc_jop is

port (
	clk			: in std_logic;

	--
	--	serial interface to download the
	--	Java application and System.in/out
	--
	ser_rxd		: in std_logic;
	ser_txd		: out std_logic;

	--
	--	only one ram bank
	--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;

	--
	--	SDRAM
	--
	DRAM_CLK, DRAM_CKE		: out std_logic;
	DRAM_ADDR				: out std_logic_vector(11 downto 0);
	DRAM_BA_1, DRAM_BA_0	: buffer std_logic;
	DRAM_CS_N, DRAM_CAS_N, DRAM_RAS_N, DRAM_WE_N : out std_logic;
	DRAM_DQ					: inout std_logic_vector(15 downto 0);
	DRAM_UDQM, DRAM_LDQM	: buffer std_logic;

	wd			: out std_logic
);
end de2_sopc_jop;

architecture rtl of de2_sopc_jop is

	component de2_sdram_pll is
	generic (multiply_by : natural; divide_by : natural);
	port (
		inclk0		: in std_logic;
		c0			: out std_logic;
		c1			: out std_logic
	);
	end component;

	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	-- for generation of internal reset
	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

	signal byte_nena		: std_logic_vector(1 downto 0);
	signal address			: std_logic_vector(18 downto 0);

	signal reset_n			: std_logic;

	signal ba, dqm			: std_logic_vector(1 downto 0);
	
begin

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

	pll_sdram : de2_sdram_pll
	generic map(
		multiply_by => pll_mult,
		divide_by => pll_div
	)
	port map (
		inclk0 => clk,
		c0 => clk_int,
		c1 => DRAM_CLK		-- -3ns
	);

	--	the SOPC generated top level
	jop: work.jop_system port map (
		clk => clk_int,
		reset_n => reset_n,
		ser_rxd_to_the_jop_avalon_0 => ser_rxd,
		ser_txd_from_the_jop_avalon_0 => ser_txd,
		wd_from_the_jop_avalon_0 => wd,

		-- the_sdram
		zs_addr_from_the_sdram  => DRAM_ADDR,
		zs_ba_from_the_sdram  => ba,
		zs_cas_n_from_the_sdram  => DRAM_CAS_N,
		zs_cke_from_the_sdram  => DRAM_CKE,
		zs_cs_n_from_the_sdram  => DRAM_CS_N,
		zs_dq_to_and_from_the_sdram  => DRAM_DQ,
		zs_dqm_from_the_sdram  => dqm,
		zs_ras_n_from_the_sdram  => DRAM_RAS_N,
		zs_we_n_from_the_sdram  => DRAM_WE_N,

		-- the_tri_state_bridge_0_avalon_slave
		chipselect_n_to_the_ext_ram => rama_ncs,
		read_n_to_the_ext_ram => rama_noe,
		tri_state_bridge_0_address => address,
		tri_state_bridge_0_byteenablen => byte_nena,
		tri_state_bridge_0_data => rama_d,
		write_n_to_the_ext_ram => rama_nwe
	);
	
	DRAM_BA_1 <= ba(1);
	DRAM_BA_0 <= ba(0);
	DRAM_UDQM <= dqm(1);
	DRAM_LDQM <= dqm(0);

	rama_nlb <= byte_nena(0);
	rama_nub <= byte_nena(1);

	-- A0 from the avalon interface is NC on 16-bit SRAM
	rama_a <= address(18 downto 1);

end rtl;
