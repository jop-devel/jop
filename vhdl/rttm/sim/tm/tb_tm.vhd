--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Peter Hilber (peter@hilber.name)
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


library std;
library ieee;

use std.textio.all;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use ieee.math_real.log2;
use ieee.math_real.ceil;

library modelsim_lib;
use modelsim_lib.util.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;
use work.tm_internal_pack.all;


entity tb_tm is
end tb_tm;

architecture behav of tb_tm is

--
--	Implementation variants
--

constant cache_combined: boolean := true;


--
--	Settings
--

constant MEM_BITS			: integer := 15;

constant addr_width		: integer := 18;	-- address bits of cachable memory
constant way_bits		: integer := 3;		-- 2**way_bits is number of entries


constant conflict_detection_cycles		: integer := 6;

--
--	Generic
--

signal finished				: boolean := false;

signal clk					: std_logic := '1';
signal reset				: std_logic;

constant cycle				: time := 10 ns;
constant reset_time			: time := 5 ns;

--
--	Testbench
--

	signal commit_out_try: std_logic;
	signal commit_in_allow: std_logic;
	
	signal sc_cpu_out: sc_out_type;
	signal sc_cpu_in: sc_in_type;
	signal sc_arb_out: sc_out_type;
	signal sc_arb_in: sc_in_type;		
	signal exc_tm_rollback: std_logic;

	signal broadcast: tm_broadcast_type := 
		(
			valid => '0',
			address => (others => 'U')
		);

--
--	Test activity flags
--

	signal testing_commit: boolean := false;
	signal testing_conflict: boolean := false;


--
--	ModelSim Signal Spy
--

	-- TODO compiler did not allow this signal as external name	
	signal write_buffer: data_array(0 to 2**way_bits-1);
	
begin

--
--	Testbench
--

	dut: entity work.tm_state_machine(rtl)
	generic map (
		addr_width => addr_width,
		tm_magic_detect => TM_MAGIC_DETECT_SIMULATION,		
		way_bits => way_bits
	)	
	port map (
		clk => clk,
		reset => reset,
		commit_token_request => commit_out_try,
		commit_token_grant => commit_in_allow,
		broadcast => broadcast,
		sc_cpu_out => sc_cpu_out,
		sc_cpu_in => sc_cpu_in,
		sc_arb_out => sc_arb_out,
		sc_arb_in => sc_arb_in,
		exc_tm_rollback => exc_tm_rollback
		);
		
	commit_coordinator: process is
	begin
		commit_in_allow <= '0';
	
		wait until commit_out_try = '1';
		wait for cycle * 3;
				
		commit_in_allow <= '1';
		
		wait until commit_out_try = '0';
		
		wait for cycle;
		commit_in_allow <= '0'; 
	end process commit_coordinator;

	signal_spy: process is
	begin
		init_signal_spy("/tb_tm/dut/tm/data","/tb_tm/write_buffer");
		wait;
	end process signal_spy;


--	
--	Input
--
		
	gen: process is
		variable result: std_logic_vector(31 downto 0);
	
		-- main memory
		
		type ram_type is array (0 to 2**MEM_BITS-1) 
			of std_logic_vector(31 downto 0); 
		alias ram is << signal .memory.main_mem.ram: ram_type >>;
		
		-- tm state

--  		alias nesting_cnt is << signal .dut.nesting_cnt: nesting_cnt_type >>;
		
		alias valid is << signal .dut.tm.tag.valid: 
			std_logic_vector(2**way_bits-1 downto 0) >>;
		
		
		alias write_tags_v is << signal .dut.tm.dirty_flags_ram_inst.flags: 
			std_logic_vector(2**way_bits-1 downto 0) >>;		
			
-- 		alias read_tags_v is << signal .dut.tm.read: 
-- 			std_logic_vector(2**way_bits-1 downto 0) >>;		
		
		
		--alias buf is << signal .dut.tm.data: 
		--	data_array(0 to << constant .dut.tm.lines: integer >> - 1) >>;
		
-- 		type tag_array is array (0 to 2**way_bits-1) of 
-- 			std_logic_vector(addr_width-1 downto 0);
-- 		alias read_tags is << signal .dut.tm.tag.tag: tag_array >>;
		
		-- test data
	
		type addr_array is array (natural range <>) of 
			std_logic_vector(SC_ADDR_SIZE-1 downto 0);
		constant addr: addr_array := 
			("000" & X"049f8", "000" & X"07607", 
			"000" & X"00001", "000" & X"01b22",
			"000" & X"016c4", "000" & X"07e39",
			"000" & X"1280c"); 
			
		constant data: data_array :=
			(X"2a25359b" , X"2cd23bd6", X"42303eea", X"0000007b",
			X"5bdae77b", X"79967474", X"187ca438");

		-- assertion helpers
				
		variable lines_used: integer;
		variable addr_used: integer;
	begin
		sc_cpu_out.tm_cache <= '1';
		
	
		wait until falling_edge(reset);		
		wait until rising_edge(clk);
		
		-- normal mode read and write
		
 		assert << signal .dut.state: state_type>> = BYPASS;
		
		sc_write(clk, addr(2), data(3), sc_cpu_out, sc_cpu_in);
 		assert (now = 60 ns) or (now = 70 ns);
		assert ram(to_integer(unsigned(addr(2)))) = data(3);
		
		sc_read(clk, addr(2), result, sc_cpu_out, sc_cpu_in);
-- 		assert now = 120 ns and result = data(3);
		assert result = data(3);
		
		sc_write(clk, addr(4), data(0), sc_cpu_out, sc_cpu_in);
		
		sc_write(clk, addr(6), data(6), sc_cpu_out, sc_cpu_in);

		-- start and end transactions
		
--  		assert to_integer(nesting_cnt) = 0; 

		sc_write(clk, TM_MAGIC_SIMULATION, 
			(31 downto tm_cmd_raw'length => '0') & TM_CMD_START_TRANSACTION, 
			sc_cpu_out, sc_cpu_in);
		
 		assert << signal .dut.state: state_type>> = TRANSACTION;
--  		assert to_integer(nesting_cnt) = 1;		
-- 
-- 		sc_write(clk, TM_MAGIC, 
-- 			(31 downto tm_cmd_raw'length => '0') & TM_CMD_START_TRANSACTION, 
-- 			sc_out_cpu, sc_in_cpu);
-- 
--  		assert to_integer(nesting_cnt) = 2;
-- 		
-- 		sc_write(clk, TM_MAGIC, 
-- 			(31 downto tm_cmd_raw'length => '0') & TM_CMD_END_TRANSACTION, 
-- 			sc_out_cpu, sc_in_cpu);
-- 		
--  		assert to_integer(nesting_cnt) = 1;
-- 		
-- 		sc_write(clk, TM_MAGIC, 
-- 			(31 downto tm_cmd_raw'length => '0') & TM_CMD_START_TRANSACTION, 
-- 			sc_out_cpu, sc_in_cpu);
-- 			
--  		assert to_integer(nesting_cnt) = 2;
		
		assert << signal .dut.conflict: std_logic >> /= '1';
		assert valid = (2**way_bits-1 downto 0 => '0');
-- 		assert << signal .dut.tm.tag.shift: 
-- 			std_logic >> = '0';
		
-- 		assert read_tags_v = (2**way_bits-1 downto 0 => '0');		
-- 		assert << signal .dut.tm.tag.shift: 
-- 			std_logic >> = '0';
		
		-- writes and reads in transactional mode
		
		sc_write(clk, addr(0), data(0), sc_cpu_out, sc_cpu_in);
		
		assert write_tags_v = (2**way_bits-1 downto 1 => 'U') & "1";
		assert write_buffer(0) = data(0);
		assert ram(to_integer(unsigned(addr(0)))) = (31 downto 0 => 'U');
		
		sc_read(clk, addr(0), result, sc_cpu_out, sc_cpu_in);
		
 		assert result = data(0);
		
		
		-- read uncached word
		
		sc_read(clk, addr(2), result, sc_cpu_out, sc_cpu_in);
		
 		assert result = data(3);		
-- 		assert read_tags(0) = addr(0)(addr_width-1 downto 0);
-- 		assert read_tags(1) = addr(2)(addr_width-1 downto 0);
		
-- 		assert read_tags_v = (2**way_bits-1 downto 2 => '0') & "11";				



-- 		-- exit inner transaction
-- 		
-- 		sc_write(clk, TM_MAGIC, 
-- 			(31 downto tm_cmd_raw'length => '0') & TM_CMD_END_TRANSACTION, 
-- 			sc_out_cpu, sc_in_cpu);
-- 		
--  		assert to_integer(nesting_cnt) = 1;


		-- read uncached word

		sc_cpu_out.tm_cache <= '0';
		sc_read(clk, addr(6), result, sc_cpu_out, sc_cpu_in);
		sc_cpu_out.tm_cache <= '1';
		
		assert result = data(6);
-- 		assert read_tags_v(2**way_bits-1 downto 2) = 
-- 			(2**way_bits-1 downto 2 => '0');
-- 		assert read_tags_v(1 downto 0) = "11";				
		
		
		

		sc_write(clk, addr(0), data(1), sc_cpu_out, sc_cpu_in);
		
		if cache_combined then
			lines_used := 2;
		else
			lines_used := 1;
		end if;
		
		assert valid(lines_used-1 downto 0) = (lines_used-1 downto 0 => '1');
		assert valid(2**way_bits-1 downto lines_used) = (2**way_bits-1 downto lines_used => '0');  
		assert write_buffer(0) = data(1);

		assert to_integer(<< signal .dut.tm.tag.nxt: -- newline
			unsigned(way_bits downto 0) >>) = lines_used;
		
		sc_read(clk, addr(0), result, sc_cpu_out, sc_cpu_in);
		
		assert result = data(1);
						
		sc_write(clk, addr(1), data(2), sc_cpu_out, sc_cpu_in);

		assert write_buffer(0) = data(1);
		
		if cache_combined then
			addr_used := 2;
		else
			addr_used := 1;
		end if;
		
		assert write_buffer(addr_used) = data(2);

		if cache_combined then
			lines_used := 3;
		else
			lines_used := 1;
		end if;

		assert to_integer(<< signal .dut.tm.tag.nxt: -- newline
				unsigned(way_bits downto 0) >>) = lines_used;
						
		
		-- commit transaction

		testing_commit <= true;
		
		sc_write(clk, TM_MAGIC_SIMULATION, 
			(31 downto tm_cmd_raw'length => '0') & TM_CMD_END_TRANSACTION, 
			sc_cpu_out, sc_cpu_in);
		
		testing_commit <= false;
		
 		assert << signal .dut.state: state_type>> = BYPASS;
--  		assert to_integer(nesting_cnt) = 0;
		
		assert ram(to_integer(unsigned(addr(0)))) = (data(1));
		assert ram(to_integer(unsigned(addr(1)))) = (data(2));		
		
		
		
		-- start another transaction
		
		sc_write(clk, TM_MAGIC_SIMULATION, 
			(31 downto tm_cmd_raw'length => '0') & TM_CMD_START_TRANSACTION, 
			sc_cpu_out, sc_cpu_in);
		
 		assert << signal .dut.state: state_type>> = TRANSACTION;
		
		-- no conflict
				
		broadcast <= (
			valid => '1',
			address => addr(0));
			
		wait until rising_edge(clk);
		
		broadcast.valid <= '0';
		
		-- no conflict, no hit
		
		-- TODO
		
		-- write something that is not committed
		
		sc_write(clk, addr(3), data(4), sc_cpu_out, sc_cpu_in);
		
		
		-- conflict 
		
		sc_read(clk, addr(0), result, sc_cpu_out, sc_cpu_in);
		
		testing_conflict <= true;
		
		broadcast <= (
			valid => '1',
			address => addr(0));
		
		wait until rising_edge(clk);
		
		broadcast.valid <= '0';
		
		for i in 2 to conflict_detection_cycles+1 loop
			assert i <= conflict_detection_cycles;
			exit when exc_tm_rollback = '1';
			wait until rising_edge(clk);					
		end loop;

		testing_conflict <= false;
		
 		assert << signal .dut.state: state_type>> = ABORT;
		
		-- zombie reads/writes
		
		sc_read(clk, addr(4), result, sc_cpu_out, sc_cpu_in);

		sc_write(clk, addr(5), data(5), sc_cpu_out, sc_cpu_in);

 		assert << signal .dut.state: state_type>> = ABORT;
		
		-- ack containment
		
		sc_write(clk, TM_MAGIC_SIMULATION, 
			(31 downto tm_cmd_raw'length => '0') & TM_CMD_ABORTED, 
			sc_cpu_out, sc_cpu_in);
		
 		assert << signal .dut.state: state_type>> = BYPASS;
--  		assert to_integer(nesting_cnt) = 0; -- ( ) 
			
		assert ram(to_integer(unsigned(addr(3)))) = (31 downto 0 => 'U');
		assert ram(to_integer(unsigned(addr(5)))) = (31 downto 0 => 'U');
		
		finished <= true;
		write(output, "Test finished.");
		wait;
	end process gen;
	
	
	check_flags: process is
	begin
		wait until falling_edge(reset);
		loop
			wait until rising_edge(clk);
						
			assert commit_out_try = '0' or testing_commit;
			assert exc_tm_rollback = '0' or testing_conflict;
			
			-- check if broadcast flags set
			assert testing_commit or 
				not (sc_arb_out.wr ='1' and sc_arb_out.tm_broadcast = '1');
			assert not testing_commit or 
				not (sc_arb_out.wr ='1' and sc_arb_out.tm_broadcast = '0');
		end loop; 
	end process check_flags;

	

--
--	Generic
--

	memory: entity work.mem_no_arbiter(behav)
	generic map (
		MEM_BITS => MEM_BITS
		)
	port map (
		clk => clk,
		reset => reset,
		sc_mem_out => sc_arb_out,
		sc_mem_in => sc_arb_in
		);

	clock: process
	begin
	   	wait for cycle/2; clk <= not clk;
	   	if finished then
	   		wait;
	   	end if;
	end process clock;

	process
	begin
		reset <= '1';
		wait for reset_time;
		reset <= '0';
		wait;
	end process;
	

end;
