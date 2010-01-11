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
--	top.vhd
--
--  alexander.dejaco@leximausi.com
--	top level test for starter kit board
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;


entity top is

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

--
--	ram bank
--
	rama_a		: out std_logic_vector(11 downto 0);
	rama_ba		: out std_logic_vector(1 downto 0);
--	rama_ba0		: out std_logic;
--	rama_ba1		: out std_logic;
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_dm		: out std_logic_vector(1 downto 0);
--	rama_dml		: out std_logic;
--	rama_dmh		: out std_logic;
	rama_clk		: out std_logic;
	rama_ncs		: out std_logic;
	rama_nwe		: out std_logic;
	rama_ncas	: out std_logic;
	rama_nras	: out std_logic;

--
--	sd-card
--

	sd_d		: inout std_logic_vector(3 downto 0);
	sd_clk		: out std_logic;
	sd_cmd		: out std_logic;
	
--
--	usb
--

	usb_c		: inout std_logic_vector(3 downto 0);
	usb_d		: inout std_logic_vector(7 downto 0);
	usb_wub		: inout std_logic;	-- send immediate / wakeup
	
--
--	usr led
--

	led		: out std_logic;
	
--
--	I/O pins of board
--

	io_l	: inout std_logic_vector(3 downto 1)

);
end top;

architecture rtl of top is

component pll is
generic (multiply_by : natural; divide_by : natural);
port (
	inclk0		: in std_logic;
	c0			: out std_logic
);
end component;

component test is
port (
              -- 1) global signals:
                 signal clk : IN STD_LOGIC;
                 signal reset_n : IN STD_LOGIC;

              -- the_jop_avalon_inst
          --       signal clk_to_the_jop_avalon_inst : IN STD_LOGIC;
          --       signal reset_to_the_jop_avalon_inst : IN STD_LOGIC;
                 signal ser_rxd_to_the_jop_avalon_inst : IN STD_LOGIC;
                 signal ser_txd_from_the_jop_avalon_inst : OUT STD_LOGIC;
                 signal wd_from_the_jop_avalon_inst : OUT STD_LOGIC;

              -- the_sdram
                 signal zs_addr_from_the_sdram : OUT STD_LOGIC_VECTOR (11 DOWNTO 0);
                 signal zs_ba_from_the_sdram : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal zs_cas_n_from_the_sdram : OUT STD_LOGIC;
                 signal zs_cke_from_the_sdram : OUT STD_LOGIC;
                 signal zs_cs_n_from_the_sdram : OUT STD_LOGIC;
                 signal zs_dq_to_and_from_the_sdram : INOUT STD_LOGIC_VECTOR (15 DOWNTO 0);
                 signal zs_dqm_from_the_sdram : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal zs_ras_n_from_the_sdram : OUT STD_LOGIC;
                 signal zs_we_n_from_the_sdram : OUT STD_LOGIC
);
end component;

--
--	Signals
--

	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";
	
	signal rama_ncke			: std_logic; --unused


begin

--
--	intern reset
--	no extern reset
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

	pll_inst : pll generic map(
		multiply_by => pll_mult,
		divide_by => pll_div
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);

	test: entity work.test 
		port map (
			clk_int,
			int_res,
			ser_rxd,
			ser_txd,
			wd,
			rama_a(11 downto 0),
			rama_ba(1 downto 0),
			rama_ncas,
			rama_ncke,
			rama_ncs,	
			rama_d(15 downto 0),		
			rama_dm(1 downto 0),
			rama_nras,
			rama_nwe
			
--			clk_int	-- ?
			
--			sd_d(3 downto 0),
--			sd_clk,
--			sd_cmd,
--			usb_c(3 downto 0),
--			usb_d(7 downto 0),
--			usb_wub,
--			led,
--			io_l(3 downto 1)
		);
		
	rama_clk <= clk_int;

end rtl;
