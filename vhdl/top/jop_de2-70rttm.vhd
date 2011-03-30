--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Peter Hilber (peter@hilber.name)
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
--	jop_512x32.vhd
--
--	top level for a 512x32 SRMA board (e.g. Altera DE2 board)
--
--	2009-03-31	adapted from jop_256x16.vhd
--	2009-09-30	adapted for RTTM
--


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
	ram_cnt		: integer := 3;		-- clock cycles for external ram
    --rom_cnt	: integer := 3;		-- clock cycles for external rom OK for 20 MHz
    rom_cnt		: integer := 15;	-- clock cycles for external rom for 100 MHz
	jpc_width	: integer := 12;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0;		-- size of scratchpad RAM (in number of address bits for 32-bit words)
	cpu_cnt		: integer := 4;		-- number of cpus
	tm_way_bits	: integer := 5;		-- 2**way_bits is number of entries
	tm_instrum	: boolean := true;	-- rttm instrumentation
	tm_ignore_masked_conflicts	: boolean := false -- ignore conflicts masked by output dependences	
);

port (
	clk				: in std_logic;
	clk2				: in std_logic;
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
end jop;

architecture rtl of jop is

--
--	constants:
--

constant tm_addr_width		: integer := 19;	-- address bits of cachable memory
constant tm_magic_detect	: std_logic_vector(19 downto 18) := (others => '1');



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
	signal sc_tm_out		: arb_out_type(0 to cpu_cnt-1);
	signal sc_tm_in			: arb_in_type(0 to cpu_cnt-1);
	
	signal sc_arb_out		: arb_out_type(0 to cpu_cnt-1);
	signal sc_arb_in		: arb_in_type(0 to cpu_cnt-1);

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
-- 	signal ser_in			: ser_in_type;
-- 	signal ser_out			: ser_out_type;
	signal wd_out			: std_logic;

	-- for generation of internal reset
-- memory interface

	signal ram_addr			: std_logic_vector(18 downto 0);	-- edit
	signal ram_dout			: std_logic_vector(31 downto 0);	-- edit
	signal ram_din			: std_logic_vector(31 downto 0);	-- edit
	signal ram_dout_en		: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;

-- not available at this board:
-- 	signal ser_ncts			: std_logic;
-- 	signal ser_nrts			: std_logic;

-- cmpsync

	signal sync_in_array	: sync_in_array_type(0 to cpu_cnt-1);
	signal sync_out_array	: sync_out_array_type(0 to cpu_cnt-1);
	
-- remove the comment for RAM access counting
-- signal ram_count		: std_logic;

--
--	TM
--
	
	signal exc_tm_rollback	: std_logic_vector(0 to cpu_cnt-1);
	signal tm_broadcast		: tm_broadcast_type;
	signal tm_broadcast_del	: tm_broadcast_type;	
	
	signal commit_token_request		: std_logic_vector(0 to cpu_cnt-1);
	signal commit_token_grant		: std_logic_vector(0 to cpu_cnt-1);
	
	signal tm_in_transaction		: std_logic_vector(0 to cpu_cnt-1);
	signal early_commit_starting	: std_logic_vector(0 to cpu_cnt-1);
	signal next_is_a_read			: std_logic_vector(0 to cpu_cnt-1);
	signal next_is_a_read_save		: std_logic_vector(0 to cpu_cnt-1);

begin



--ser_ncts <= '0';
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
	

	wd <= wd_out;

	gen_cpu: for i in 0 to cpu_cnt-1 generate
		cpu: entity work.jopcpu
		generic map(
			jpc_width => jpc_width,
			block_bits => block_bits,
			spm_width => spm_width
		)
		port map(clk_int, int_res,
				sc_tm_out(i), sc_tm_in(i),
				sc_io_out(i), sc_io_in(i), irq_in(i), 
				irq_out(i), exc_req(i), exc_tm_rollback(i));
	end generate;
	
	gen_tm: for i in 0 to cpu_cnt-1 generate
		tm: entity work.tm_state_machine
			generic map (
				addr_width => tm_addr_width,
				tm_magic_detect => tm_magic_detect,
				way_bits => tm_way_bits,
				instrumentation => tm_instrum,
				ignore_masked_conflicts => tm_ignore_masked_conflicts				
			)	
			port map (
				clk	=> clk_int,
				reset => int_res,
			
				commit_token_request => commit_token_request(i),
				commit_token_grant => commit_token_grant(i),
			
				broadcast => tm_broadcast_del,
			
				sc_cpu_out => sc_tm_out(i),  
				sc_cpu_in => sc_tm_in(i), 
			
				sc_arb_out => sc_arb_out(i),
				sc_arb_in => sc_arb_in(i),
			
				exc_tm_rollback => exc_tm_rollback(i),
				tm_in_transaction => tm_in_transaction(i),
				early_commit_starting => early_commit_starting(i)
				);
	end generate;

	coordinator: entity work.tm_coordinator(rtl)
	generic map (
		cpu_cnt => cpu_cnt
		)
	port map (
		clk => clk_int,
		reset => int_res,
		commit_token_request => commit_token_request,
		commit_token_grant => commit_token_grant
		);

	arbiter : entity work.arbiter
		generic map(
			addr_bits => SC_ADDR_SIZE,
			cpu_cnt	=> cpu_cnt)
		port map(
			clk => clk_int,
			reset => int_res,			
			arb_out => sc_arb_out,
			arb_in => sc_arb_in,
			mem_out => sc_mem_out,
			mem_in => sc_mem_in,
			committing => commit_token_grant,
			tm_in_transaction => tm_in_transaction,
			tm_broadcast => tm_broadcast,
			next_is_a_read => next_is_a_read 
		);
		
	gen_next_is_a_read: process (sc_tm_out, early_commit_starting, 
		next_is_a_read_save) is
	begin
		for i in 0 to cpu_cnt-1 loop
			next_is_a_read(i) <= next_is_a_read_save(i);
			
			if sc_tm_out(i).rd = '1' then
				next_is_a_read(i) <= '1';
			elsif sc_tm_out(i).wr = '1' or early_commit_starting(i) = '1' then
				next_is_a_read(i) <= '0'; 
			end if;
		end loop;
	end process gen_next_is_a_read;
	
	process (clk_int, int_res) is
	begin
	    if int_res = '1' then
	    	next_is_a_read_save <= (others => '0');
	    elsif rising_edge(clk_int) then
			next_is_a_read_save <= next_is_a_read;
	    end if;
	end process;

	
	-- Hold valid TM broadcast addresses and delay broadcast for 1 cycle. 
	hold_tm_broadcast: process (clk_int, int_res) is
	begin
	    if int_res = '1' then
	    	tm_broadcast_del <= ('0', (others => '0')); 
	    elsif rising_edge(clk_int) then
	    	tm_broadcast_del.valid <= tm_broadcast.valid;
		 	if tm_broadcast.valid = '1' then
				tm_broadcast_del.address <= tm_broadcast.address;
			end if;
	    end if;
	end process hold_tm_broadcast;


	scm: entity work.sc_mem_if
		generic map (
			ram_ws => ram_cnt-1,
			addr_bits => tm_addr_width			-- edit
		)
		port map (clk_int, int_res,
			sc_mem_out, sc_mem_in,

			ram_addr => ram_addr,
			ram_dout => ram_dout,
			ram_din => ram_din,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => ram_ncs,
			ram_noe => ram_noe,
			ram_nwe => ram_nwe
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
			ncts => oUART_CTS,
			nrts => iUART_RTS,
			wd => wd_out,
			l => open,
			r => open,
			t => open,
			b => open
			-- remove the comment for RAM access counting
			-- ram_cnt => ram_count			
		);
	
	-- io for processors with only sc_sys
	gen_io: for i in 1 to cpu_cnt-1 generate
		io2: entity work.sc_sys generic map (
			addr_bits => 4,
			clk_freq => clk_freq,
			cpu_id => i,
			cpu_cnt => cpu_cnt
		)
		port map(
			clk => clk_int,
			reset => int_res,
			address => sc_io_out(i).address(3 downto 0),
			wr_data => sc_io_out(i).wr_data,
			rd => sc_io_out(i).rd,
			wr => sc_io_out(i).wr,
			rd_data => sc_io_in(i).rd_data,
			rdy_cnt => sc_io_in(i).rdy_cnt,
			
			irq_in => irq_in(i),
			irq_out => irq_out(i),
			exc_req => exc_req(i),
			
			sync_out => sync_out_array(i),
			sync_in => sync_in_array(i),
			wd => open
			-- remove the comment for RAM access counting
			-- ram_cnt => ram_count
		);	

	end generate;
	

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			SRAM_DQ <= ram_dout;
		else
			SRAM_DQ <= (others => 'Z');
		end if;
	end process;

	ram_din <= SRAM_DQ;
	
	-- remove the comment for RAM access counting
	-- ram_count <= ram_ncs;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	oSRAM_A <= ram_addr;
	oSRAM_CE1_N <= ram_ncs;
	oSRAM_OE_N <= ram_noe;
	oSRAM_WE_N <= ram_nwe;
	oSRAM_BE_N <= (others => '0');
	oSRAM_GW_N <= '1';
	oSRAM_CLK <= clk_int;
	
	oSRAM_ADSC_N <= ram_ncs;
	oSRAM_ADSP_N <= '1';
	oSRAM_ADV_N	<= '1';
	
	oSRAM_CE2 <= not(ram_ncs);	
    oSRAM_CE3_N <= ram_ncs;
   

end rtl;
