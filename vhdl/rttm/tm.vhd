--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)
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
--	Real-Time Transactional Memory
--

--
--	Transactional Memory
--

--
-- Provides SimpCon level 2 pipelining. Level 3 is not provided due to the
-- need to interleave regular memory accesses transparently with conflict
-- checks.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;
use work.tm_internal_pack.all;


entity tm is

generic (
	addr_width		: integer := 18;	-- address bits of cachable memory
	way_bits		: integer := 5		-- 2**way_bits is number of entries
);
port (
	clk, reset		: in std_logic;
	from_cpu		: in sc_out_type;
	to_cpu			: out sc_in_type;
 	to_mem			: out sc_out_type;
 	from_mem		: in sc_in_type;
 	
 	broadcast		: in tm_broadcast_type;
 	conflict		: out std_logic;
 	
 	-- signal must be delayed by at least one cycle before being processed
 	-- to allow memory transaction to finish
 	commit_finished		: out std_logic;

	tag_full: out std_logic;
	
	state: in state_type;
	
	transaction_start: in std_logic);

end tm;

architecture rtl of tm is

	type stage1_state_type is
	( idle, write, read1, commit, broadcast1);
	
	type stage23_state_type is
	( idle, read_hit2, write2, read_hit3, read_miss2, read_miss3,
		read_miss4, commit_2, commit_3, commit_4 );

	type stage1_type is record
		state: stage1_state_type;
		addr: std_logic_vector(SC_ADDR_SIZE-1 downto 0);
		cpu_data: std_logic_vector(31 downto 0);
	end record;
	
	type stage2_type is record
		hit: std_logic;
		address: std_logic_vector(addr_width-1 downto 0);
		update_tags: std_logic;
		update_read: std_logic;
		update_dirty: std_logic;
		read: std_logic;
		dirty: std_logic;
	end record;
	
	type stage3_type is record
		read_read: std_logic;
		read_dirty: std_logic;
	end record;	
	
	type stage23_type is record
		state: stage23_state_type;
		--data: std_logic_vector(31 downto 0); -- TODO
		wr_data: std_logic_vector(31 downto 0);
		update_data: std_logic;
		line_addr: unsigned(way_bits-1 downto 0);
	end record;
	
	signal stage1, next_stage1: stage1_type;
	signal stage2, next_stage2: stage2_type;
	signal stage3, next_stage3: stage3_type;
	signal stage23, next_stage23: stage23_type;
	
	type stage1_async_type is record
		hit: std_logic;
		line_addr: unsigned(way_bits-1 downto 0);
		newline: unsigned(way_bits downto 0);
	end record;
	
	signal stage1_async: stage1_async_type;

	signal read_data, next_read_data: std_logic_vector(31 downto 0);
	
	signal from_cpu_dly: sc_out_type;
	signal from_mem_dly: sc_in_type;

	--
	-- Write buffer
	--
	
	constant lines		: integer := 2**way_bits;

	signal data: data_array(0 to lines-1);
	
	signal dirty		: std_logic_vector(lines-1 downto 0);
	signal read			: std_logic_vector(lines-1 downto 0);

	signal save_data, next_save_data: std_logic_vector(31 downto 0);
	
	--
	-- Commit/broadcast logic
	--
	
	signal commit_line			: unsigned(way_bits downto 0);
	signal shift				: std_logic;
	
	signal commit_addr			: std_logic_vector(addr_width-1 downto 0);
	
	signal broadcast_valid_dly	: std_logic;

	signal write_to_mem_finishing: std_logic;
	
	
	signal bcstage2, next_bcstage2: std_logic;
	signal bcstage3: std_logic;
	
	signal next_commit_line: unsigned (way_bits downto 0);
	
	signal next_commit_started: std_logic;
	signal commit_started: std_logic;
begin

	proc_stage0: process (broadcast, broadcast_valid_dly, commit_addr, 
		commit_line, commit_started, from_cpu, from_mem, stage1, stage1_async, 
		state, write_to_mem_finishing) is
	begin
		-- TODO assertion that no signals are issued in wrong time
		next_stage1.addr <= (others => 'X');
		next_stage1.cpu_data <= from_cpu.wr_data;
		
		commit_finished <= '0';
		
		next_commit_started <= commit_started; -- TODO
	
		case state is 
			when no_transaction | rollback_signal | rollback_wait |
				early_committed_transaction =>
				next_stage1.state <= idle;
				next_stage1.addr <= from_cpu.address;
				
			when commit_wait_token | early_commit_wait_token =>
				next_stage1.state <= idle;
				next_stage1.addr <= from_cpu.address;
				
				if broadcast.valid = '1' or 
					(broadcast_valid_dly = '1' and stage1.state /= broadcast1) 
					then
					next_stage1.state <= broadcast1;
					-- TODO why still valid if delayed?
					next_stage1.addr <= broadcast.address;
				end if;
				
				next_commit_started <= '0';				
			
			when normal_transaction =>
				next_stage1.state <= idle;
				next_stage1.addr <= from_cpu.address;
			
				if broadcast.valid = '1'  or 
					(broadcast_valid_dly = '1' and stage1.state /= broadcast1)
					then
					next_stage1.state <= broadcast1;
					next_stage1.addr <= broadcast.address; 
				end if;

				if from_cpu.nc = '0' then
					if from_cpu.wr = '1' then
						next_stage1.state <= write;
					elsif from_cpu.rd = '1' then
						next_stage1.state <= read1;
					end if;
				end if;
				
			when commit | early_commit =>
				next_stage1.state <= idle; -- TODO ?
				-- TODO use FIFO
				-- write is nearly finished and not all lines comm.
				-- TODO check rdy_cnt
				if write_to_mem_finishing = '1' or 
					((commit_started = '0') and (from_mem.rdy_cnt = 0)) then
					next_commit_started <= '1';
					if commit_line = stage1_async.newline then
						commit_finished <= '1';
					else 
						next_stage1.state <= commit;
						
						next_stage1.addr <= (others => '0');
						next_stage1.addr(commit_addr'range) <= commit_addr;
						-- shift <= '1';
					end if;
				end if;
		end case;		
	end process proc_stage0;

	proc_stage1: process(commit_line, stage1, stage1_async, stage23) is
	begin
		next_stage2.update_tags <= '0';
		next_stage2.hit <= stage1_async.hit;
		
		next_stage2.update_read <= '0';
		next_stage2.update_dirty <= '0';
		next_stage2.read <= 'X';
		next_stage2.dirty <= 'X'; 
		
		next_stage2.address <= stage1.addr(next_stage2.address'range);
		
		next_bcstage2 <= '0';
		
		next_stage23.line_addr <= stage23.line_addr;
		
		tag_full <= '0';
		
		if stage1.state = commit then
			next_stage23.line_addr <= commit_line(way_bits-1 downto 0);
		elsif stage1.state = read1 or stage1.state = write or
			stage1.state = broadcast1 then -- TODO
			if stage1_async.hit = '1' then
				next_stage23.line_addr <= stage1_async.line_addr;
			else
				next_stage23.line_addr <= stage1_async.newline(
					way_bits-1 downto 0);
			end if;
			
			if stage1_async.hit = '0' then
				-- TODO this is in the critical path
				-- TODO make sure operation finishes first
				if stage1_async.newline(way_bits-1 downto 0) = 
					(way_bits-1 downto 0 => '1') then
					tag_full <= '1';
				end if;
			end if;
		end if;
		
		case stage1.state is
			when idle =>
				null;
				
			when broadcast1 =>
				next_bcstage2 <= '1';
						
			when read1 =>
				next_stage2.update_tags <= '1';
				
				-- set read flag only if first access in transaction is a read 
				if stage1_async.hit = '0' then					
					next_stage2.update_read <= '1';
					next_stage2.read <= '1';
				end if;
				
				if stage1_async.hit = '0' then					
					next_stage2.update_dirty <= '1';
					next_stage2.dirty <= '0';
				end if;				
			when write =>
				next_stage2.update_tags <= '1';
				
				next_stage2.update_dirty <= '1';
				next_stage2.dirty <= '1';
				
				if stage1_async.hit = '0' then					
					next_stage2.update_read <= '1';
					next_stage2.read <= '0';
				end if;								
			when commit =>
		end case;				
	end process proc_stage1;
	
	proc_stage2only: process(data, dirty, read, stage23, stage2) is
	begin		
		-- TODO only valid in next cycle
		next_read_data <= data(to_integer(stage23.line_addr));
		
		next_stage3.read_dirty <= dirty(to_integer(stage23.line_addr));
		-- TODO naming ^v
		next_stage3.read_read <= read(to_integer(stage23.line_addr)) and
			stage2.hit;
	end process proc_stage2only;
	
	mem_sync: process (clk) is
	begin
	    if rising_edge(clk) then
			-- TODO stage 2 or 3?
			if stage23.update_data = '1' then
				-- TODO which reg?
				data(to_integer(stage23.line_addr)) <= stage23.wr_data;
			end if;
			
			if stage2.update_dirty = '1' then
				dirty(to_integer(stage23.line_addr)) <= stage2.dirty;
			end if;
			
			if stage2.update_read = '1' then
				read(to_integer(stage23.line_addr)) <= stage2.read;
			end if;
		end if;
	end process mem_sync;

	
	proc_stage23: process(commit_addr, commit_line, from_cpu_dly, from_mem, 
		from_mem_dly, read_data, save_data, stage1, stage1_async, stage2, 
		stage23, stage3, state, transaction_start) is
	begin
		shift <= '0';
	
		-- stage 3 signals
		to_cpu.rd_data <= save_data;
		
		next_stage23.update_data <= '0';		
		next_stage23.state <= idle;
		
		next_stage23.wr_data <= (others => 'X');
		to_mem.address <= (others => 'X');
		to_mem.wr_data <= (others => 'X');
		
		to_mem.wr <= '0';
		to_mem.rd <= '0';
		
		write_to_mem_finishing <= '0';
		
		next_commit_line <= commit_line;
		
				
		
		next_save_data <= save_data;
		
		case stage23.state is
			when idle =>
				-- TODO hazard?
				if state = no_transaction or 
				state = early_committed_transaction or
				state = rollback_signal or
				state = rollback_wait or
				from_cpu_dly.nc = '1' then
					to_mem.wr <= from_cpu_dly.wr;
					to_mem.rd <= from_cpu_dly.rd;
					
					to_mem.address <= stage1.addr;
					to_mem.wr_data <= stage1.cpu_data;
				end if;
				
				if from_mem_dly.rdy_cnt /= 0 and from_mem.rdy_cnt = 0 then
					next_save_data <= from_mem.rd_data;
					to_cpu.rd_data <= from_mem.rd_data;
				end if;
			
			when read_hit2 =>
				next_stage23.state <= read_hit3;
			
			when write2 =>
				null;
-- 				next_stage23.state <= write3;
			
-- 			when write3 =>
-- 				null;
			
			when read_hit3 =>
				-- TODO read_data is already a reg.
				next_save_data <= read_data;
				to_cpu.rd_data <= read_data;
				
			when read_miss2 =>
				next_stage23.state <= read_miss3;
				to_mem.rd <= '1';
				to_mem.address <= 
					(SC_ADDR_SIZE-1 downto addr_width => 
					'0') & stage2.address; -- TODO
			
			when read_miss3 =>
				next_stage23.state <= read_miss3;
				
				if from_mem.rdy_cnt(1) = '0' then
					next_stage23.state <= read_miss4;
				end if;
				
			when read_miss4 =>
				next_save_data <= from_mem.rd_data;
				to_cpu.rd_data <= from_mem.rd_data;
				next_stage23.update_data <= '1';
				next_stage23.wr_data <= from_mem.rd_data; 
			
			when commit_2 =>
				next_commit_line <= commit_line + 1;
				next_stage23.state <= commit_3;
			
			when commit_3 =>
				-- TODO don't write if not dirty
				if stage3.read_dirty = '1' then
					to_mem.wr <= '1';
					to_mem.address <= (SC_ADDR_SIZE-1 downto addr_width => 
						'0') & commit_addr;
					to_mem.wr_data <= read_data;
					next_stage23.state <= commit_4;
				else
					-- TODO one cycle earlier => no since shift etc.
					write_to_mem_finishing <= '1';
					next_stage23.state <= idle;
				end if;					
				
				shift <= '1'; -- TODO
				
			when commit_4 =>
				next_stage23.state <= commit_4;
				
				-- save one cycle
				if from_mem.rdy_cnt < 3 then
					write_to_mem_finishing <= '1';
					next_stage23.state <= idle;
				end if;
		end case;
		
		case stage1.state is
			when idle | broadcast1 =>
				null;
			when read1 =>
				if stage1_async.hit = '1' then
					next_stage23.state <= read_hit2;
				else
					next_stage23.state <= read_miss2;
				end if;
			when write =>
				next_stage23.state <= write2;
				next_stage23.wr_data <= stage1.cpu_data;
				next_stage23.update_data <= '1';										
			when commit =>
				next_stage23.state <= commit_2;
		end case;
		
		if transaction_start = '1' then
			next_commit_line <= (others => '0');
		end if;		
	end process proc_stage23;
	
	gen_rdy_cnt: process (from_cpu_dly, from_mem, stage1, stage23) is
		variable var_rdy_cnt: unsigned(RDY_CNT_SIZE-1 downto 0);
	begin
		var_rdy_cnt := "00";
		
		-- TODO not for tm cmd
		if from_cpu_dly.rd = '1' or from_cpu_dly.wr = '1' then
			var_rdy_cnt := "11";
		end if;
		
		case stage1.state is
			when write =>
				var_rdy_cnt := "10";
			when read1 =>
				var_rdy_cnt := "11"; -- TODO not waiting for hit res. is faster
-- 				if stage1_async.hit = '1' then
-- 					var_rdy_cnt := "10";
-- 				else
-- 					var_rdy_cnt := "11";
-- 				end if;
			when others =>
				case stage23.state is
					when write2 | read_hit2 =>
						var_rdy_cnt := "01";
					when read_miss2 =>
						var_rdy_cnt := "11"; -- TODO issue to_mem.rd earlier?
						
					-- TODO hazard fix - or add reg. and disable cycle 2 write
					-- TODO actually min(3, from_mem.rdy_cnt + 1)	
					when read_miss3 =>
						var_rdy_cnt := "11";
					when read_miss4 =>
						var_rdy_cnt := "01";					
					 
					when others =>
						null; -- set in state machine
				end case;
		end case;
		
		if from_mem.rdy_cnt > var_rdy_cnt then
			var_rdy_cnt := from_mem.rdy_cnt;
		end if;
		
		to_cpu.rdy_cnt <= var_rdy_cnt;
	end process gen_rdy_cnt;
	
	--
	--	Conflict detection
	--
	
	proc_bcstage2: process(reset, clk) is
	begin
		if reset = '1' then
			bcstage3 <= '0';
		elsif rising_edge(clk) then
			bcstage3 <= bcstage2;
		end if; 
	end process proc_bcstage2;
	
	proc_bcstage3: process(bcstage3, stage3) is
	begin
		conflict <= '0';
		-- TODO only if it was a hit
		if bcstage3 = '1' and stage3.read_read = '1' then
			conflict <= '1';
		end if;
	end process proc_bcstage3;

	sync: process (clk, reset) is
	begin
	    if reset = '1' then
			stage1 <= (state => idle, addr => (others => '0'), 
				cpu_data => (others => '0'));
			stage2 <= (hit => '0', address => (others => '0'),
				update_tags => '0', update_read => '0',
				update_dirty => '0', read => '0', dirty => '0');
			stage3 <= (read_read => '0', read_dirty => '0');
			stage23 <= (state => idle,
				wr_data => (others => '0'), update_data => '0',
				line_addr => (others => '0'));
				 
			bcstage2 <= '0';
			broadcast_valid_dly <= '0';
			save_data <= (others => '0');
			from_cpu_dly <= sc_out_idle;
			-- TODO from_mem_dly
			commit_line <= (others => '0');
			
			read_data <= (others => '0');
			
			commit_started <= '0'; 
				 
	    elsif rising_edge(clk) then
			stage1 <= next_stage1;
			stage2 <= next_stage2;
			stage3 <= next_stage3;
			stage23 <= next_stage23;
			bcstage2 <= next_bcstage2;
			broadcast_valid_dly <= broadcast.valid;
			save_data <= next_save_data;
			from_cpu_dly <= from_cpu;
			from_mem_dly <= from_mem;
			commit_line <= next_commit_line;
			
			read_data <= next_read_data;
			
			commit_started <= next_commit_started;
	    end if;
	end process sync;


	--
	--	Tag instantiation
	--

	tag: entity work.tag
		generic map(
			addr_width => addr_width,
			way_bits => way_bits
		)
		port map(
			clk => clk,
			reset => reset,
			
			transaction_start => transaction_start,
			
			-- TODO feed from stage 0?
			addr => stage1.addr(addr_width-1 downto 0),
			wr => stage2.update_tags,
			hit => stage1_async.hit, -- TODO delay if fed in stage 0
			line => stage1_async.line_addr,
			newline => stage1_async.newline,
-- 			full => tag_full,
			
			shift => shift,
			lowest_addr => commit_addr
		);

end;
