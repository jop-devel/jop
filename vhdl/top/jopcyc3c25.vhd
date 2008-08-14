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
--	jopcyc3c25.vhd
--
--  alexander.dejaco@leximausi.com
--	top level for starter kit board
--  2008-13-08  creation: setup for the starter kit base board (based on jopcyc.vhd)
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;


entity jop is

generic (
	ram_cnt		: integer := 2;		-- clock cycles for external ram
	ram_addr_cnt: integer := 12;	-- RAM addr bits
--	rom_cnt		: integer := 3;		-- clock cycles for external rom OK for 20 MHz
	rom_cnt		: integer := 15;	-- clock cycles for external rom for 100 MHz
	jpc_width	: integer := 12;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0		-- size of scratchpad RAM (in number of address bits for 32-bit words)
);

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
	rama_ba0		: out std_logic;
	rama_ba1		: out std_logic;
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_dml		: out std_logic;
	rama_dmh		: out std_logic;
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

	io_l	: inout std_logic_vector(5 downto 1)

);
end jop;

architecture rtl of jop is

--
--	components:
--

component pll is
generic (multiply_by : natural; divide_by : natural);
port (
	inclk0		: in std_logic;
	c0			: out std_logic
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

--
--	jopcpu connections
--
	signal sc_mem_out		: sc_out_type;
	signal sc_mem_in		: sc_in_type;
	signal sc_io_out		: sc_out_type;
	signal sc_io_in			: sc_in_type;
	signal irq_in			: irq_bcf_type;
	signal irq_out			: irq_ack_type;
	signal exc_req			: exception_type;

--
--	IO interface
--
	signal ser_in			: ser_in_type;
	signal ser_out			: ser_out_type;
	signal wd_out			: std_logic;
	signal led_out			: std_logic;

	-- for generation of internal reset

-- memory interface

	signal ram_addr			: std_logic_vector(11 downto 0);
	signal ram_dout			: std_logic_vector(15 downto 0);
	signal ram_din			: std_logic_vector(15 downto 0);
	signal ram_dout_en		: std_logic;
	signal ram_ncs			: std_logic;
--	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;
	
	signal ram_ba0			: std_logic;
	signal ram_ba1			: std_logic;
	signal ram_dml			: std_logic;
	signal ram_dmh			: std_logic;
	signal ram_ncas			: std_logic;
	signal ram_nras			: std_logic;

begin

--
--	intern reset
--	no extern reset, epm7064 has too less pins
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

--
--	components of jop
--
	pll_inst : pll generic map(
		multiply_by => pll_mult,
		divide_by => pll_div
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);
-- clk_int <= clk;

	wd <= wd_out;

	cpm_cpu: entity work.jopcpu
		generic map(
			jpc_width => jpc_width,
			block_bits => block_bits,
			spm_width => spm_width
		)
		port map(clk_int, int_res,
			sc_mem_out, sc_mem_in,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req);

	cmp_io: entity work.scio 
		port map (clk_int, int_res,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req,

			txd => ser_txd,
			rxd => ser_rxd,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out,
			led => led_out,
			l => io_l,
			r(12 downto 9) => usb_c(3 downto 0),
			r(8 downto 1) => usb_d(7 downto 0),
			r(13) => usb_wub
--			t => io_t,
--			b => io_b
		);

	cmp_scm: entity work.sc_mem_if
		generic map (
			ram_ws => ram_cnt-1,
			addr_bits => ram_addr_cnt
			--rom_ws => rom_cnt-1
		)
		port map (clk_int, int_res,
			sc_mem_out, sc_mem_in,

			ram_addr => ram_addr,
			ram_dout => ram_dout,
			ram_din => ram_din,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => ram_ncs,
--			ram_noe => ram_noe,
			ram_nwe => ram_nwe,
			
			ram_ba0 => ram_ba0,
			ram_ba1 => ram_ba1,
			ram_dml => ram_dml,
			ram_dmh => ram_dmh,
			ram_ncas => ram_ncas,
			ram_nras => ram_nras

--			fl_a => fl_a,
--			fl_d => fl_d,
--			fl_ncs => fl_ncs,
--			fl_ncsb => fl_ncsb,
--			fl_noe => fl_noe,
--			fl_nwe => fl_nwe,
--			fl_rdy => fl_rdy

		);

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			rama_d <= ram_dout(15 downto 0);
			--rama_d <= ram_dout(15 downto 0);
			--ramb_d <= ram_dout(31 downto 16);
		else
			rama_d <= (others => 'Z');
			--ramb_d <= (others => 'Z');
		end if;
	end process;

	ram_din <= rama_d;
	--ram_din <= ramb_d & rama_d;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--

	rama_a <= ram_addr;
	rama_ncs <= ram_ncs;
--	rama_noe <= ram_noe;
	rama_nwe <= ram_nwe;
--	rama_nlb <= '0';
--	rama_nub <= '0';
	
	rama_ba0 <= ram_ba0;
	rama_ba1 <= ram_ba1;
	rama_dml <= ram_dml;
	rama_dmh <= ram_dmh;
	rama_ncas <= ram_ncas;
	rama_nras <= ram_nras;

--	ramb_a <= ram_addr;
--	ramb_ncs <= ram_ncs;
--	ramb_noe <= ram_noe;
--	ramb_nwe <= ram_nwe;
--	ramb_nlb <= '0';
--	ramb_nub <= '0';


--
--	EP1C12 additional power pins as tristatet output on EP1C6
--
--	dummy_gnd <= (others => 'Z');
--	dummy_vccint <= (others => 'Z');
--	freeio <= 'Z';

end rtl;
