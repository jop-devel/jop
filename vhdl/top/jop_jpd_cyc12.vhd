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
--	jop_jpd_cyc12.vhd
--
--	top level for JEOPARD/multiprocessor, cycore board with EP1C12
--
--	2002-03-28	creation
--	2002-06-27	isa bus for CS8900
--	2002-07-27	io for baseio
--	2002-08-02	second uart (use first for download and debug)
--	2002-11-01	removed second uart
--	2002-12-01	split memio
--	2002-12-07	disable clkout
--	2003-02-21	adapt for new Cyclone board with EP1C6
--	2003-07-08	invertion of cts, rts to uart
--	2004-09-11	new extension module
--	2004-10-08	mul operands from a and b, single instruction
--	2005-05-12	added the bsy routing through extension
--	2005-08-15	sp_ov can be used to show a stoack overflow on the wd pin
--	2005-11-30	SimpCon for IO devices
--	2007-03-17	Use jopcpu and change component interface to records
--	2008-07-22	Add Jack's MAC units


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.jop_config_global.all;
use work.jop_config.all;

entity jop is

generic (
	ram_cnt		: integer := 2;		-- clock cycles for external ram
--	rom_cnt		: integer := 3;		-- clock cycles for external rom OK for 20 MHz
	rom_cnt		: integer := 15;	-- clock cycles for external rom for 100 MHz
	jpc_width	: integer := 11;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0;		-- size of scratchpad RAM (in number of address bits for 32-bit words)
	cpu_cnt		: integer := 1;		-- number of cpus
	arb_cnt		: integer := 3		-- number of slaves
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
--	io_b	: inout std_logic_vector(10 downto 1);
	io_l	: inout std_logic_vector(20 downto 1);
	io_r	: inout std_logic_vector(20 downto 1);
	io_t	: inout std_logic_vector(6 downto 1)
	
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
	signal sc_arb_out		: arb_out_type(0 to arb_cnt-1);
	signal sc_arb_in		: arb_in_type(0 to arb_cnt-1);
	
	signal sc_mem_out		: sc_out_type;
	signal sc_mem_in		: sc_in_type;
	
	signal sc_io_out		: sc_out_array_type(0 to cpu_cnt-1);
	signal sc_io_in			: sc_in_array_type(0 to cpu_cnt-1);
	signal irq_in			  : irq_in_array_type(0 to cpu_cnt-1);
	signal irq_out			: irq_out_array_type(0 to cpu_cnt-1);
	signal exc_req			: exception_array_type(0 to cpu_cnt-1);

--
--	IO interface
--
	signal ser_in			: ser_in_type;
	signal ser_out			: ser_out_type;
	type wd_out_array is array (0 to cpu_cnt-1) of std_logic;
	signal wd_out			: wd_out_array;

	-- for generation of internal reset

-- memory interface

	signal ram_addr			: std_logic_vector(17 downto 0);
	signal ram_dout			: std_logic_vector(31 downto 0);
	signal ram_din			: std_logic_vector(31 downto 0);
	signal ram_dout_en	: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;

-- cmpsync

	signal sync_in_array	: sync_in_array_type(0 to cpu_cnt-1);
	signal sync_out_array	: sync_out_array_type(0 to cpu_cnt-1);
	
-- remove the comment for RAM access counting
-- signal ram_count		: std_logic;
	
-- control channel for HW methods

	signal cc_out_data      : std_logic_vector(31 downto 0);
    signal cc_out_wr        : std_logic;
    signal cc_out_rdy       : std_logic;
    signal cc_in_data       : std_logic_vector(31 downto 0);
    signal cc_in_wr         : std_logic;
    signal cc_2_rdy         : std_logic;
    signal cc_2_data        : std_logic_vector(31 downto 0);
    signal cc_2_wr          : std_logic;
    signal cc_in_rdy        : std_logic;

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

	wd <= wd_out(0);
	
	gen_cpu: for i in 0 to cpu_cnt-1 generate
		cpu: entity work.jopcpu
			generic map(
				jpc_width => jpc_width,
				block_bits => block_bits,
				spm_width => spm_width
			)
			port map(clk_int, int_res,
				sc_arb_out(i), sc_arb_in(i),
				sc_io_out(i), sc_io_in(i), irq_in(i), 
				irq_out(i), exc_req(i));
	end generate;
			
	arbiter: entity work.arbiter
		generic map(
			addr_bits => SC_ADDR_SIZE,
			cpu_cnt => arb_cnt
		)
		port map(clk_int, int_res,
			sc_arb_out, sc_arb_in,
			sc_mem_out, sc_mem_in
			-- Enable for use with Round Robin Arbiter
			-- sync_out_array(1)
			);

	scm: entity work.sc_sram32_flash
		generic map (
			ram_ws => ram_cnt-1,
			rom_ws => rom_cnt-1
		)
		port map (clk_int, int_res,
			sc_mem_out, sc_mem_in,

			ram_addr => ram_addr,
			ram_dout => ram_dout,
			ram_din => ram_din,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => ram_ncs,
			ram_noe => ram_noe,
			ram_nwe => ram_nwe,

			fl_a => fl_a,
			fl_d => fl_d,
			fl_ncs => fl_ncs,
			fl_ncsb => fl_ncsb,
			fl_noe => fl_noe,
			fl_nwe => fl_nwe,
			fl_rdy => fl_rdy

		);
		
	-- syncronization of processors
	sync: entity work.cmpsync generic map (
		cpu_cnt => cpu_cnt)
		port map
		(
			clk => clk_int,
			reset => int_res,
			sync_in_array => sync_in_array,
			sync_out_array => sync_out_array
		);
	
	-- io for processor 0
	io: entity work.scio generic map (
			cpu_id => 0,
			cpu_cnt => cpu_cnt
		)
		port map (clk_int, int_res,
			sc_io_out(0), sc_io_in(0),
			irq_in(0), irq_out(0), exc_req(0),

			sync_out => sync_out_array(0),
			sync_in => sync_in_array(0),

			txd => ser_txd,
			rxd => ser_rxd,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out(0),
			l => io_l,
			r => io_r,
			t => io_t,
			--b => io_b
			-- remove the comment for RAM access counting
			-- ram_cnt => ram_count			
            inhibit => '0',
            cc_out_data => cc_in_data,
            cc_out_wr => cc_in_wr,
            cc_out_rdy => cc_in_rdy,
 
            cc_in_data => cc_out_data,
            cc_in_wr => cc_out_wr,
            cc_in_rdy => cc_out_rdy
		);
	
	-- io for processors with only sc_sys
--	gen_io: for i in 1 to cpu_cnt-1 generate
--		io2: entity work.sc_sys generic map (
--			addr_bits => 4,
--			clk_freq => clk_freq,
--			cpu_id => i,
--			cpu_cnt => cpu_cnt
--		)
--		port map(
--			clk => clk_int,
--			reset => int_res,
--			address => sc_io_out(i).address(3 downto 0),
--			wr_data => sc_io_out(i).wr_data,
--			rd => sc_io_out(i).rd,
--			wr => sc_io_out(i).wr,
--			rd_data => sc_io_in(i).rd_data,
--			rdy_cnt => sc_io_in(i).rdy_cnt,
--			
--			irq_in => irq_in(i),
--			irq_out => irq_out(i),
--			exc_req => exc_req(i),
--			
--			sync_out => sync_out_array(i),
--			sync_in => sync_in_array(i),
--			wd => wd_out(i)
--			-- remove the comment for RAM access counting
--			-- ram_count => ram_count
--		);
--
--	end generate;
	

	-- HW methods
    cp1 : entity work.mac_coprocessor 
        port map (
                clk => clk_int,
                reset => int_res,

                sc_mem_out => sc_arb_out(1), 
                sc_mem_in => sc_arb_in(1),

                cc_out_data => cc_out_data,
                cc_out_wr => cc_out_wr,
                cc_out_rdy => cc_out_rdy,

                cc_in_data => cc_2_data,
                cc_in_wr => cc_2_wr,
                cc_in_rdy => cc_2_rdy
            );
    cp2 : entity work.mac_coprocessor 
        port map (
                clk => clk_int,
                reset => int_res,

                sc_mem_out => sc_arb_out(2), 
                sc_mem_in => sc_arb_in(2),

                cc_out_data => cc_2_data,
                cc_out_wr => cc_2_wr,
                cc_out_rdy => cc_2_rdy,

                cc_in_data => cc_in_data,
                cc_in_wr => cc_in_wr,
                cc_in_rdy => cc_in_rdy
            );

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			rama_d <= ram_dout(15 downto 0);
			ramb_d <= ram_dout(31 downto 16);
		else
			rama_d <= (others => 'Z');
			ramb_d <= (others => 'Z');
		end if;
	end process;

	ram_din <= ramb_d & rama_d;
	
	-- remove the comment for RAM access counting
	-- ram_count <= ram_ncs;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	rama_a <= ram_addr;
	rama_ncs <= ram_ncs;
	rama_noe <= ram_noe;
	rama_nwe <= ram_nwe;
	rama_nlb <= '0';
	rama_nub <= '0';

	ramb_a <= ram_addr;
	ramb_ncs <= ram_ncs;
	ramb_noe <= ram_noe;
	ramb_nwe <= ram_nwe;
	ramb_nlb <= '0';
	ramb_nub <= '0';

	freeio <= 'Z';

end rtl;
