--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009-2010, Peter Hilber (peter@hilber.name)
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
--
--
-- TODO
-- - SimpCon tm_cache flag is ignored on write
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
	addr_width		: integer;		-- address bits of cachable memory
	way_bits		: integer;		-- 2**way_bits is number of entries
	instrumentation	: boolean;
	ignore_masked_conflicts	: boolean
);
port (
	clk, reset		: in std_logic;
	
	from_cpu		: in sc_out_type;
	to_cpu			: out sc_in_type;
 	to_mem			: out sc_out_type;
 	from_mem		: in sc_in_type;
 	
 	-- tm_state_machine state
 	state			: in state_type;
 	broadcast		: in tm_broadcast_type;
 	-- tm_state_machine busy
 	rdy_cnt_busy	: in std_logic;	
	
	
	-- Events
	
	transaction_start: in std_logic;
 	conflict		: out std_logic;
 	
 	-- signal must be delayed by at least one cycle before being processed
 	-- to allow memory transaction to finish
 	commit_finished		: out std_logic;
	tag_full: out std_logic;

	
	-- Instrumentation
	
	read_set: out unsigned(way_bits downto 0);
	write_set: out unsigned(way_bits downto 0);
	read_or_write_set: out unsigned(way_bits downto 0)
);

end tm;

architecture rtl of tm is


	-- State machines for pipeline stages

	type stage1_state_type is
	( idle, write, read1, read_direct, write_direct, commit, broadcast1);
	
	type stage2ff_state_type is
	( idle, read_hit2, write2, read_hit3, read_miss2, read_miss3,
		read_miss4, commit_2, commit_3, commit_4, read_direct2, read_direct3,
		write_direct2 );


	-- Registers for pipeline stages

	type stage1_type is record
		state: stage1_state_type;
		addr: std_logic_vector(SC_ADDR_SIZE-1 downto 0);
		cpu_data: std_logic_vector(31 downto 0);
	end record;
	
	type stage2_type is record
		hit: std_logic;
		addr: std_logic_vector(addr_width-1 downto 0);
		update_tags: std_logic;
		update_read: std_logic;
		update_dirty: std_logic;
		read: std_logic;
		dirty: std_logic_vector(0 downto 0);
		read_flag_line_addr: unsigned(way_bits-1 downto 0);
	end record;
	
	type stage3_type is record		
		was_read: std_logic; -- was_read is only set if it was a hit
-- 		was_dirty: std_logic;
		addr: std_logic_vector(addr_width-1 downto 0);
		
		hit: std_logic;
		set_dirty: std_logic;		
	end record;	
	
	type stage2ff_type is record
		state: stage2ff_state_type;
		wr_data: std_logic_vector(31 downto 0);
		update_data: std_logic;
		line_addr: unsigned(way_bits-1 downto 0);
	end record;
	
	signal stage1, next_stage1: stage1_type;
	signal stage2, next_stage2: stage2_type;
	signal stage3, next_stage3: stage3_type;
	signal stage2ff, next_stage2ff: stage2ff_type;
	
	signal stage3_was_dirty: std_logic_vector(0 downto 0);
	
	
	-- Unregistered data
	
	type stage1_async_type is record
		hit: std_logic;
		line_addr: unsigned(way_bits-1 downto 0);
		newline: unsigned(way_bits downto 0);
	end record;
	
	signal stage1_async: stage1_async_type;

	
	-- Misc.
	
	signal read_data, next_read_data: std_logic_vector(31 downto 0);

	signal from_mem_rdy_cnt_dly: unsigned(RDY_CNT_SIZE-1 downto 0);
	
	-- remember to forward rd_data from from_mem when tm module is ready
	signal read_miss_publish, next_read_miss_publish: std_logic;
	
	signal next_stage2ff_line_addr_helper: 
		std_logic_vector(way_bits-1 downto 0);
		
	signal write_fifo_buffer_wrreq: std_logic;


	-- Data and state flags
	
	constant lines		: integer := 2**way_bits;

	signal data: data_array(0 to lines-1);
	
-- 	signal dirty		: std_logic_vector(lines-1 downto 0);
	signal read			: std_logic_vector(lines-1 downto 0);

	signal save_data, next_save_data: std_logic_vector(31 downto 0);
	

	-- Commit logic
	
	signal commit_shift				: std_logic;
	
	signal commit_addr			: std_logic_vector(addr_width-1 downto 0);
	
	signal next_commit_started: std_logic;
	signal commit_started: std_logic;
	
	signal commit_word_finishing: std_logic;
	
	signal write_buffer_empty: std_logic;
	
	
	-- Conflict detection logic
	
	signal bcstage2, next_bcstage2: std_logic;
	signal bcstage3: std_logic;
	
	signal broadcast_valid_dly	: std_logic;
	
	
	-- Instrumentation

	type r_w_set_instrum_type is record
		read_set: unsigned(way_bits downto 0);
		write_set: unsigned(way_bits downto 0);
		read_or_write_set: unsigned(way_bits downto 0);
	end record;
	
	signal r_w_set_instrum_current: r_w_set_instrum_type;
	signal r_w_set_instrum_max: r_w_set_instrum_type;
	
	type instrum_stage3_type is record
		set_read: std_logic;
	end record;
	
	signal instrum_stage3: instrum_stage3_type;
	
	component flags_ram
	GENERIC (
		way_bits: integer
	);
	PORT
	(
		address		: IN STD_LOGIC_VECTOR (way_bits-1 DOWNTO 0);
		clock		: IN STD_LOGIC  := '1';
		data		: IN STD_LOGIC_VECTOR (0 DOWNTO 0);
		wren		: IN STD_LOGIC  := '0';
		q			: OUT STD_LOGIC_VECTOR (0 DOWNTO 0)
	);
	end component;
	

begin

	--
	--	Tag instantiation
	--
	
	tag: entity work.tm_tag
		generic map(
			addr_width => addr_width,
			way_bits => way_bits
		)
		port map(
			clk => clk,
			reset => reset,
			
			transaction_start => transaction_start,
			
			addr => stage1.addr(addr_width-1 downto 0),
			wr => stage2.update_tags,
			hit => stage1_async.hit,
			line => stage1_async.line_addr,
			newline => stage1_async.newline
		);
		
				
		
		dirty_flags_ram_inst : flags_ram 
		GENERIC MAP (
				way_bits => way_bits
			)
		PORT MAP (
				address	 => next_stage2ff_line_addr_helper,
				clock	 => clk,
				data	 => next_stage2.dirty,
				wren	 => next_stage2.update_dirty,
				q	 => stage3_was_dirty
			);
			
		next_stage2ff_line_addr_helper <= 
--			(X downto next_stage2ff.line_addr'high+1 => '0') & 
			std_logic_vector(next_stage2ff.line_addr); 
			
			
		write_fifo_buffer_inst : entity work.write_fifo_buffer
		generic map (
			addr_width => addr_width,
			way_bits => way_bits
		)
		port map (
			clock	 => clk,
			data	 => stage3.addr,
			rdreq	 => commit_shift,
			sclr	 => transaction_start,
			wrreq	 => write_fifo_buffer_wrreq,
			empty	 => write_buffer_empty,
			q	 => commit_addr
		);
		
		write_fifo_buffer_wrreq <= '1' when
			(stage3_was_dirty(0) = '0' or stage3.hit = '0') and
				stage3.set_dirty = '1' else '0';


	--
	--	(Somewhat) pipelined processing
	--

	proc_stage0: process (broadcast, broadcast_valid_dly, commit_addr, 
		commit_started, from_cpu, from_mem_rdy_cnt_dly, stage1, 
		state, commit_word_finishing, write_buffer_empty) is
	begin
		next_stage1.addr <= (others => 'X');
		next_stage1.cpu_data <= from_cpu.wr_data;
		
		commit_finished <= '0';
		
		next_commit_started <= commit_started;
	
		case state is 
			when BYPASS | ABORT |
				EARLY_COMMIT =>
				if from_cpu.rd = '1' then
					next_stage1.state <= read_direct;
				elsif from_cpu.wr = '1' and state /= ABORT then
					next_stage1.state <= write_direct;
				else
					next_stage1.state <= idle;
				end if;
				
				next_stage1.addr <= from_cpu.address;
				
			when WAIT_TOKEN | EARLY_WAIT_TOKEN =>
				next_stage1.state <= idle;
				
				if broadcast.valid = '1' or 
					(broadcast_valid_dly = '1' and stage1.state /= broadcast1) 
					then
					next_stage1.state <= broadcast1;
					next_stage1.addr <= broadcast.address;
				end if;
				
				next_commit_started <= '0';				
			
			when TRANSACTION =>
				next_stage1.state <= idle;

				if from_cpu.wr = '1' or from_cpu.rd = '1' then
					next_stage1.addr <= from_cpu.address;

					if from_cpu.wr = '1' then
						-- TODO tm_cache flag is ignored on write
						next_stage1.state <= write;
					elsif from_cpu.rd = '1' then
						if from_cpu.tm_cache = '1' then
							next_stage1.state <= read1;
						else
							next_stage1.state <= read_direct;
						end if;
					end if;
				elsif broadcast.valid = '1'  or 
					(broadcast_valid_dly = '1' and stage1.state /= broadcast1)
					then
					next_stage1.addr <= broadcast.address;
					next_stage1.state <= broadcast1; 
				end if;

			when COMMIT | EARLY_FLUSH =>
				next_stage1.state <= idle;

				-- a memory read transaction was started in the previous cycle 
				-- at the latest
				if commit_word_finishing = '1' or 
					((commit_started = '0') and (from_mem_rdy_cnt_dly(1) = '0')) then
					next_commit_started <= '1';
					if write_buffer_empty = '1' then
						commit_finished <= '1';
					else 
						next_stage1.state <= commit;
						
						next_stage1.addr <= (others => '0');
						next_stage1.addr(commit_addr'range) <= commit_addr;
					end if;
				end if;
		end case;		
	end process proc_stage0;

	proc_stage1: process(stage1, stage1_async, stage2ff) is
	begin
		next_stage2.update_tags <= '0';
		next_stage2.hit <= stage1_async.hit;
		
		next_stage2.update_read <= '0';
		next_stage2.update_dirty <= '0';
		next_stage2.read <= 'X';
		next_stage2.dirty(0) <= 'X'; 
		
		next_stage2.addr <= stage1.addr(next_stage2.addr'range);
		
		next_bcstage2 <= '0';
		
		next_stage2ff.line_addr <= stage2ff.line_addr;
		
		tag_full <= '0';
		
		-- set line_addr and tag_full
		--if stage1.state = commit then
		--	next_stage2ff.line_addr <= commit_line(way_bits-1 downto 0);
		if stage1.state = read1 or stage1.state = write or 
			stage1.state = commit then
			if stage1_async.hit = '1' then
				next_stage2ff.line_addr <= stage1_async.line_addr;
			else
				next_stage2ff.line_addr <= stage1_async.newline(
					way_bits-1 downto 0);
			end if;
			
			if stage1_async.hit = '0' then
				if stage1_async.newline(way_bits-1 downto 0) = 
					(way_bits-1 downto 0 => '1') then
					tag_full <= '1';
				end if;
			end if;
		end if;
		--elsif stage1.state = broadcast1 then
		-- always executed for instrumentation purposes 
		next_stage2.read_flag_line_addr <= stage1_async.line_addr;
		--end if;
		
		-- set tag update enables and next_bcstage2 
		case stage1.state is
			when idle | commit | read_direct | write_direct =>
				null;
				
			when broadcast1 =>
				next_bcstage2 <= '1';
						
			when read1 =>
				next_stage2.update_tags <= '1';
				
				-- if ignore_masked_conflicts enabled, 
				-- set read flag only if first access in transaction is a read 
				if (not ignore_masked_conflicts) or (stage1_async.hit = '0') then					
					next_stage2.update_read <= '1';
					next_stage2.read <= '1';
				end if;
				
				if stage1_async.hit = '0' then					
					next_stage2.update_dirty <= '1';
					next_stage2.dirty(0) <= '0';
				end if;				
			when write =>
				next_stage2.update_tags <= '1';
				
				next_stage2.update_dirty <= '1';
				next_stage2.dirty(0) <= '1';
				
				if stage1_async.hit = '0' then					
					next_stage2.update_read <= '1';
					next_stage2.read <= '0';
				end if;								
		end case;				
	end process proc_stage1;
	
	proc_stage2: process(data, read, stage2ff, stage2) is
	begin		
		next_read_data <= data(to_integer(stage2ff.line_addr));
		
		-- Since RAM access is synthesized in stage 3 instead of stage 2,
		-- we set was_dirty using a dedicated component
-- 		next_stage3.was_dirty <= dirty(to_integer(stage2ff.line_addr));
		-- naming ^v
		-- .was_read is only set if it was a hit
		next_stage3.was_read <= read(to_integer(stage2.read_flag_line_addr)) and
			stage2.hit;
			
		next_stage3.addr <= stage2.addr;
		
		next_stage3.set_dirty <= stage2.update_dirty and stage2.dirty(0);
		next_stage3.hit <= stage2.hit;
	end process proc_stage2;
	
	memory_block_access: process (clk) is
	begin
	    if rising_edge(clk) then
			if stage2ff.update_data = '1' then
				data(to_integer(stage2ff.line_addr)) <= stage2ff.wr_data;
			end if;
			
			-- moved to dedicated component
-- 			if stage2.update_dirty = '1' then
-- 				dirty(to_integer(stage2ff.line_addr)) <= stage2.dirty;
-- 			end if;
			
			if stage2.update_read = '1' then
				read(to_integer(stage2ff.line_addr)) <= stage2.read;
			end if;
		end if;
	end process memory_block_access;
	
	proc_stage2ff: process(commit_addr, from_mem, 
		read_data, save_data, stage1, stage1_async, stage2, stage2ff, 
		stage3_was_dirty, from_mem_rdy_cnt_dly,
		rdy_cnt_busy, read_miss_publish) is
	begin
		commit_shift <= '0';
	
		-- stage 3 signals
		to_cpu.rd_data <= save_data;
		
		next_stage2ff.update_data <= '0';		
		next_stage2ff.state <= idle;
		
		next_stage2ff.wr_data <= (others => 'X');
		to_mem.address <= (others => 'X');
		to_mem.wr_data <= (others => 'X');
		
		to_mem.wr <= '0';
		to_mem.rd <= '0';
		
		commit_word_finishing <= '0';
		
		next_save_data <= save_data;
		
		next_read_miss_publish <= read_miss_publish;

		
				
		case stage2ff.state is
			when idle | write_direct2 =>
				null;
			
			when read_direct2 | read_direct3 =>
				-- forward non-transactional reads w/ pipeline level 2
				if ((from_mem.rdy_cnt = 0 and not (from_mem_rdy_cnt_dly = 0)) or 
					from_mem_rdy_cnt_dly = 1)  
				then
					next_save_data <= from_mem.rd_data;
-- 					to_cpu.rd_data <= from_mem.rd_data;
				else
					next_stage2ff.state <= read_direct3;
				end if;
							
			when read_hit2 =>
				next_stage2ff.state <= read_hit3;
			
			when write2 =>
				null;
			
			when read_hit3 =>
				next_save_data <= read_data;
				to_cpu.rd_data <= read_data;
				
			when read_miss2 =>
				next_stage2ff.state <= read_miss3;
				to_mem.rd <= '1';
				to_mem.address <= 
					(SC_ADDR_SIZE-1 downto addr_width => 
					'0') & stage2.addr;
			
			when read_miss3 =>
				next_stage2ff.state <= read_miss3;
				
				if from_mem.rdy_cnt(1) = '0' then
					next_stage2ff.state <= read_miss4;
					next_read_miss_publish <= '1';
				end if;
				
			when read_miss4 =>
				-- read finishes only in next cycle 
				-- (due to control hazard when updating cache for next memory 
				-- access)
				next_stage2ff.update_data <= '1';
				next_stage2ff.wr_data <= from_mem.rd_data;
			
			when commit_2 =>				
				next_stage2ff.state <= commit_3;
				
				assert stage2.hit = '1';
			
			when commit_3 =>
				to_mem.wr <= '1';
				to_mem.address <= (SC_ADDR_SIZE-1 downto addr_width => 
					'0') & commit_addr;
				to_mem.wr_data <= read_data;
				next_stage2ff.state <= commit_4;

				assert stage3_was_dirty(0) = '1';
				
				commit_shift <= '1';
				
			when commit_4 =>
				next_stage2ff.state <= commit_4;
				
				-- save one cycle
				if from_mem.rdy_cnt < 3 then
					commit_word_finishing <= '1';
					next_stage2ff.state <= idle;
				end if;
		end case;
		
		case stage1.state is
			when idle | broadcast1 =>
				null;
			when read1 =>
				if stage1_async.hit = '1' then
					next_stage2ff.state <= read_hit2;
				else
					next_stage2ff.state <= read_miss2;
				end if;
			when write =>
				next_stage2ff.state <= write2;
				next_stage2ff.wr_data <= stage1.cpu_data;
				next_stage2ff.update_data <= '1';										
			when commit =>
				next_stage2ff.state <= commit_2;
			when read_direct =>
				next_stage2ff.state <= read_direct2;
				to_mem.rd <= '1';

				to_mem.address <= stage1.addr;
				to_mem.wr_data <= (others => 'X');
			when write_direct =>
				next_stage2ff.state <= write_direct2;
				to_mem.wr <= '1';

				to_mem.address <= stage1.addr;
				to_mem.wr_data <= stage1.cpu_data;
		end case;
		
		-- if this read miss triggered an early commit,
		-- wait until the EARLY_FLUSH finished before updating rd_data
		if read_miss_publish = '1' and rdy_cnt_busy = '0' then
			-- could do that on next_read_miss_publish rising edge
-- 			to_cpu.rd_data <= from_mem.rd_data;			
			next_save_data <= from_mem.rd_data;
			next_read_miss_publish <= '0';
		end if;		
	end process proc_stage2ff;
	
	
	
	--
	--	Concurrent assignments
	--
	
	read_set <= r_w_set_instrum_max.read_set;
	write_set <= r_w_set_instrum_max.write_set;
	read_or_write_set <= r_w_set_instrum_max.read_or_write_set;
	
	
	--
	--	Part of rdy_cnt generation
	--
	
	gen_rdy_cnt: process (from_mem_rdy_cnt_dly, rdy_cnt_busy, 
		read_miss_publish, stage1, stage2ff) is
	begin
		case stage1.state is
			when write =>
				to_cpu.rdy_cnt <= "11"; -- don't know about buffer overflow yet
			when read1 =>
				to_cpu.rdy_cnt <= "11"; -- not waiting for hit res. is faster
-- 				if stage1_async.hit = '1' then
-- 					to_cpu.rdy_cnt <= "10";
-- 				else
-- 					to_cpu.rdy_cnt <= "11";
-- 				end if;
			when read_direct | write_direct =>
				to_cpu.rdy_cnt <= "11";
			when idle | commit | broadcast1 =>
				case stage2ff.state is
					when write2 | read_hit2 =>
						to_cpu.rdy_cnt <= "01";
					when read_miss2 =>
						to_cpu.rdy_cnt <= "11";
						
					when read_miss3 =>
						-- avoid cache control hazard if next access is a write	
						to_cpu.rdy_cnt <= "11";
					when read_miss4 =>
						to_cpu.rdy_cnt <= "01";
						
					-- direct read from memory
					when read_direct2 =>
						to_cpu.rdy_cnt <= "11";
					when read_direct3 =>
						to_cpu.rdy_cnt <= from_mem_rdy_cnt_dly;
					
					when write_direct2 =>
						to_cpu.rdy_cnt <= "11";
					
					-- on direct write to memory, we're in state idle
					when idle | read_hit3 | commit_2 | commit_3 |
						commit_4 =>
						-- rdy_cnt 0 or set by arbiter or tm_state_machine
						
						-- rdy_cnt is delayed and a new SHM transaction will
						-- be delayed by an additional cycle
						case from_mem_rdy_cnt_dly is
							when "11" =>
								to_cpu.rdy_cnt <= "11";
							when others =>
								to_cpu.rdy_cnt <= "00";
						end case;
				end case;
		end case;
		
		-- need another cycle to publish read data 		
		if read_miss_publish = '1' and rdy_cnt_busy = '0' then
			to_cpu.rdy_cnt <= "01";
		end if;
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
		-- .was_read is only set if it was a hit
		if bcstage3 = '1' and stage3.was_read = '1' then
			conflict <= '1';
		end if;
	end process proc_bcstage3;


	--
	--	Register signals
	--
	
	sync: process (clk, reset) is
	begin
	    if reset = '1' then
			stage1 <= (state => idle, addr => (others => '0'), 
				cpu_data => (others => '0'));
			stage2 <= (hit => '0', addr => (others => '0'),
				update_tags => '0', update_read => '0',
				update_dirty => '0', read => '0', dirty => "0",
				read_flag_line_addr => (others => '0'));
			stage3 <= (was_read => '0', addr => (others => '0'),
				hit => '0', set_dirty => '0');
			stage2ff <= (state => idle,
				wr_data => (others => '0'), update_data => '0',
				line_addr => (others => '0'));
				 
			bcstage2 <= '0';
			broadcast_valid_dly <= '0';
			save_data <= (others => '0');
			
			read_data <= (others => '0');
			
			commit_started <= '0';
			
			from_mem_rdy_cnt_dly <= (others => '0');
			read_miss_publish <= '0';				 
	    elsif rising_edge(clk) then
			stage1 <= next_stage1;
			stage2 <= next_stage2;
			stage3 <= next_stage3;
			stage2ff <= next_stage2ff;
			bcstage2 <= next_bcstage2;
			broadcast_valid_dly <= broadcast.valid;
			save_data <= next_save_data;
			
			read_data <= next_read_data;
			
			commit_started <= next_commit_started;
			
			from_mem_rdy_cnt_dly <= from_mem.rdy_cnt;
			read_miss_publish <= next_read_miss_publish;
	    end if;
	end process sync;


	--
	--	Instrumentation
	--
	
	gen_instr: process(reset, clk) is
	begin
		if reset = '1' then
			if instrumentation then
				-- reset maxima
				r_w_set_instrum_max <=
					((others => '0'), (others => '0'), (others => '0'));
			end if;  
		elsif rising_edge(clk) then
			if instrumentation then
				-- save for stage 3
				instrum_stage3.set_read <= stage2.update_read and stage2.read;
			
				-- update counters in stage 3
				if (stage3.was_read = '0' or stage3.hit = '0') and
					instrum_stage3.set_read = '1' then
					r_w_set_instrum_current.read_set <=
						r_w_set_instrum_current.read_set + 1;
				end if;
								
				if (stage3_was_dirty(0) = '0' or stage3.hit = '0') and
					stage3.set_dirty = '1' then
					r_w_set_instrum_current.write_set <=
						r_w_set_instrum_current.write_set + 1;
				end if;
				
				if stage3.hit = '0' and
					(instrum_stage3.set_read = '1' or
					stage3.set_dirty = '1') 
					then
					r_w_set_instrum_current.read_or_write_set <=
						r_w_set_instrum_current.read_or_write_set + 1; 
				end if;
				
				-- reset counters
				if state = BYPASS then
					r_w_set_instrum_current <=
						((others => '0'), (others => '0'), (others => '0'));  
				end if;
				
				-- update maxima
				if state = COMMIT or state = EARLY_FLUSH then
					if r_w_set_instrum_current.write_set > 
						r_w_set_instrum_max.write_set then
						r_w_set_instrum_max.write_set <= 
							r_w_set_instrum_current.write_set;
					end if;
					
					if r_w_set_instrum_current.read_set > 
						r_w_set_instrum_max.read_set then
						r_w_set_instrum_max.read_set <= 
							r_w_set_instrum_current.read_set;
					end if;
	
					if r_w_set_instrum_current.read_or_write_set > 
						r_w_set_instrum_max.read_or_write_set then
						r_w_set_instrum_max.read_or_write_set <= 
							r_w_set_instrum_current.read_or_write_set;
					end if;
				end if;
			end if;			
		end if;
	end process;

end;
