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


--
--	TODO:
--	- read out statistics from all cores on core 0
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;
use work.tm_internal_pack.all;



entity tm_state_machine is

generic (
	-- width of memory addresses cached during transaction
	addr_width		: integer;
	
	-- Pattern used to detect magic address
	-- Magic address is located in upper half of external SRAM mirror.
	-- The pattern is used to execute TM commands and to return diagnostic 
	-- data, if rttm_instrum is enabled.
	-- When executing a TM command, the lower bits are actually ignored.
	-- Keep in synch with com.jopdesign.sys.Const.MEM_TM_MAGIC.
	tm_magic_detect	: std_logic_vector;
	
	-- fully associative read and write buffer has 2**way_bits entries
	way_bits		: integer;
	
	-- enable instrumentation (without probe effects)
	-- turn off to lower hardware consumption
	instrumentation	: boolean := true;
	
	-- enable to only detect truly conflicting reads,
	-- i.e. reads of an address not yet written during the transaction
	ignore_masked_conflicts	: boolean := false
	);

port (
	clk					: in std_logic;
	reset				: in std_logic;
	
	--
	--	Commit logic
	--
	
	-- set until transaction finished/aborted
	commit_token_request			: out std_logic;
	commit_token_grant			: in std_logic;

	--
	--	Commit addresses
	--
	
	-- broadcast.valid is set for one cycle
	-- broadcast.address value is held until next .valid
	broadcast				: in tm_broadcast_type;

	--
	--	Memory IF to cpu
	--
	sc_cpu_out		: in sc_out_type;
	sc_cpu_in		: out sc_in_type;		

	--
	--	Memory IF to arbiter
	--
	sc_arb_out		: out sc_out_type;
	sc_arb_in		: in sc_in_type;

	--
	--	rollback exception
	--
	exc_tm_rollback	: out std_logic;

	--
	--	HW transaction in progress
	--
	tm_in_transaction	: out std_logic;
	
	--
	--	HACK to speed up memory arbiter
	--
	early_commit_starting	: out std_logic
);

end tm_state_machine;

architecture rtl of tm_state_machine is

	-- State machines
		
	signal state, next_state		: state_type;

	-- containment state has internal state machine for correct timing and rdy_cnt
	-- generation
	-- cbi... containment idle
	-- cbb... containment busy
	-- cba... containment aborted
	type containment_state_type is (cbi0, cbb0, cbb1, cbb2, cba1, cba2, cbi);
	signal next_containment_state, containment_state: containment_state_type;

		
	-- TM commands
	
	signal tm_cmd					: tm_cmd_type;

	signal is_tm_magic_addr_async: std_logic;
	signal is_tm_magic_addr_sync: std_logic;	

	
	-- filter signals to/from CPU/arbiter
	
	signal sc_cpu_out_filtered		: sc_out_type;
	signal sc_cpu_in_filtered		: sc_in_type;
	signal sc_arb_out_filtered		: sc_out_type;


	-- Events
	 
	signal transaction_start: std_logic;

	signal conflict					: std_logic;
	signal tag_full: std_logic;

	signal commit_finished				: std_logic;
	
	
	-- Misc.
	
	signal sc_cpu_out_dly: sc_out_type;
	
	
	
	-- commit_finished signal is delayed long enough so that other processors
	-- detect conflict before they can obtain commit token
	-- (after commit_finished_dly has caused reset of commit_token_request)		
	signal commit_finished_dly: std_logic;
	signal commit_finished_dly_internal_1: std_logic;
	
	-- tm_state_machine is busy
	signal rdy_cnt_busy: std_logic;
	
		
	-- Instrumentation

	type instrumentation_type is record
		retries: unsigned(31 downto 0);
		commits: unsigned(31 downto 0);
		early_commits: unsigned(31 downto 0);
		
		read_set: unsigned(way_bits downto 0);
		write_set: unsigned(way_bits downto 0);
		read_or_write_set: unsigned(way_bits downto 0);
	end record;
	
	signal instrumentation_data: instrumentation_type;
	signal next_instrumentation_data: instrumentation_type;
	
	type instr_helpers_type is record
		last_value: unsigned(31 downto 0);
		hold_instr_value: std_logic;
	end record;
	
	signal instrum_helpers: instr_helpers_type;
	signal next_instr_helpers: instr_helpers_type;
	
	-- Keep in synch with com.jopdesign.sys.Const constants.
	constant RETRIES_ADDR: std_logic_vector := "000";
	constant COMMITS_ADDR: std_logic_vector := "001";
	constant EARLY_COMMITS_ADDR: std_logic_vector := "010";
		
	constant READ_SET_ADDR: std_logic_vector := "011";
	constant WRITE_SET_ADDR: std_logic_vector := "100";
	constant READ_OR_WRITE_SET_ADDR: std_logic_vector := "101";
	
begin

	--
	--	Transactional memory core functionality
	--
	tm: entity work.tm(rtl)
	generic map (
		addr_width => addr_width,
		way_bits => way_bits,
		instrumentation => instrumentation,
		ignore_masked_conflicts => ignore_masked_conflicts
	)	
	port map (
		clk => clk,
		reset => reset,
		from_cpu => sc_cpu_out_filtered,
		to_cpu => sc_cpu_in_filtered,
		to_mem => sc_arb_out_filtered,
		from_mem => sc_arb_in,
		
		broadcast => broadcast,
		conflict => conflict,
		
		commit_finished => commit_finished,
		
		tag_full => tag_full,
		
		state => state,
		transaction_start => transaction_start,
		rdy_cnt_busy => rdy_cnt_busy,
		
		read_set => next_instrumentation_data.read_set,
		write_set => next_instrumentation_data.write_set,
		read_or_write_set => next_instrumentation_data.read_or_write_set
	);
	
	--
	--	Concurrent assignments
	--
	
	is_tm_magic_addr_async <= '1' when
		sc_cpu_out.address(tm_magic_detect'range) = tm_magic_detect else '0';
	
	-- request or hold commit token during these states  
	commit_token_request <= '1' 
		when state = WAIT_TOKEN or state = EARLY_WAIT_TOKEN or
		state = COMMIT or state = EARLY_FLUSH or
		state = EARLY_COMMIT
		else '0';
	
-- 	-- moved out of state machine and in previous cycle
-- 	next_rdy_cnt_busy <= '1' when 
-- 	(next_state = WAIT_TOKEN) or
-- 	(next_state = COMMIT) or
-- 	(next_state = EARLY_WAIT_TOKEN) or
-- 	(next_state = EARLY_FLUSH) or
-- 	((next_state = ABORT) and
-- 		(next_containment_state /= cbi0) and 
-- 		(next_containment_state /= cbi))
-- 	else '0';
	
	
	tm_in_transaction <= '1' when state /= BYPASS else '0';
	
				
	
	--
	--	TM STATE MACHINE
	--
	state_machine: process(commit_finished_dly, commit_token_grant, conflict, 
		containment_state, state, tag_full, tm_cmd, instrumentation_data) is		
	begin
		next_state <= state;
		exc_tm_rollback <= '0';
		rdy_cnt_busy <= '0';
		
		transaction_start <= '0';
		
		next_containment_state <= containment_state;
		
		if instrumentation then
			next_instrumentation_data.retries <= instrumentation_data.retries;
			next_instrumentation_data.commits <= instrumentation_data.commits;
			next_instrumentation_data.early_commits <= 
				instrumentation_data.early_commits;
		end if;
		
		case state is
			when BYPASS =>
				if tm_cmd = start_transaction then
					next_state <= TRANSACTION;
					
					transaction_start <= '1';
				end if;
				
			when TRANSACTION =>
				if tag_full = '1' then
					next_state <= EARLY_WAIT_TOKEN;
				end if;
			
				case tm_cmd is
					when end_transaction =>
						next_state <= WAIT_TOKEN;
					when early_commit =>
						next_state <= EARLY_WAIT_TOKEN;
					when aborted =>
						-- command is only issued if an exception is being 
						-- handled
						next_state <= BYPASS;
						
						if instrumentation then
							next_instrumentation_data.retries <= 
								instrumentation_data.retries + 1;
						end if;
					when start_transaction | none => 
						null;
				end case;						
								
				if conflict = '1' then
					next_state <= ABORT;
					
					if instrumentation then
						next_instrumentation_data.retries <= 
							instrumentation_data.retries + 1;
					end if;
					
					case tm_cmd is
						when none =>						
							next_containment_state <= cbi0;
						when aborted =>
							-- don't miss aborted command					
	 						next_state <= BYPASS;
						when others =>
							next_containment_state <= cbb0;
					end case;			
				end if;
								
			when WAIT_TOKEN =>
				rdy_cnt_busy <= '1';
			
				if conflict = '1' then
					next_state <= ABORT;
					next_containment_state <= cbb0;
					
					if instrumentation then
						next_instrumentation_data.retries <= 
							instrumentation_data.retries + 1;
					end if;
				elsif commit_token_grant = '1' then
					next_state <= COMMIT;
					
					if instrumentation then
						next_instrumentation_data.commits <= 
							instrumentation_data.commits + 1;
					end if;
				end if;
			
			when COMMIT =>
				rdy_cnt_busy <= '1';
				
				if commit_finished_dly = '1' then
					next_state <= BYPASS;
				end if;
				
			when EARLY_WAIT_TOKEN =>
				rdy_cnt_busy <= '1';
			
				if conflict = '1' then
					next_state <= ABORT;
					next_containment_state <= cbb0;
					
					if instrumentation then
						next_instrumentation_data.retries <= 
							instrumentation_data.retries + 1;
					end if;
				elsif commit_token_grant = '1' then
					next_state <= EARLY_FLUSH;
					
					if instrumentation then
						next_instrumentation_data.early_commits <= 
							instrumentation_data.early_commits + 1;
					end if;
				end if;
				
			when EARLY_FLUSH =>
				rdy_cnt_busy <= '1';
			
				if commit_finished_dly = '1' then
					next_state <= EARLY_COMMIT;
				end if;
				
			when EARLY_COMMIT =>
				case tm_cmd is
					when end_transaction =>
						next_state <= BYPASS;

						if instrumentation then
							next_instrumentation_data.commits <= 
								instrumentation_data.commits + 1;
						end if;
					when aborted =>
						-- indicates a program bug
						-- violates transaction isolation, since SHM was 
						-- already touched
						assert false;
						
						if instrumentation then
							next_instrumentation_data.retries <= 
								instrumentation_data.retries + 1;
						end if;
						next_state <= BYPASS;						
					when others =>
						null;
				end case;
				
			when ABORT =>

				-- If a transaction is being aborted, we need to assure that 
				-- the next bytecode issued will handle the exception.
				--
				-- This is necessary to restrict zombie transactions to a 
				-- single zombie bytecode and to assure that the try block 
				-- containing the transaction is not exited (when already 
				-- executing the end_transaction hardware command).
				--
				-- 2 cycles delay ensure that the exception will be handled 
				-- when the next bytecode is issued.
			
				case containment_state is
					when cbi0 =>
						exc_tm_rollback <= '1';
					
						next_containment_state <= cbi;
					
					when cbb0 =>
						exc_tm_rollback <= '1';
					
						next_containment_state <= cbb1;
						rdy_cnt_busy <= '1';
					
					when cbb1 =>
						next_containment_state <= cbb2;
						rdy_cnt_busy <= '1';
					
					when cbb2 =>
						next_containment_state <= cbi;
						rdy_cnt_busy <= '1';
					
					when cbi =>
						null;
												
					when cba1 =>
						next_containment_state <= cba2;
						rdy_cnt_busy <= '1';
					
					when cba2 =>
						next_state <= BYPASS;
						rdy_cnt_busy <= '1';
						
				end case;
				
				case tm_cmd is
					when none =>
						null;
					when aborted =>
						next_containment_state <= cba1;
					when others =>
						next_containment_state <= cbb1;
				end case;
		end case;
	end process state_machine;
	
	
	--
	--	 Adjustments to signals to/from CPU/arbiter.
	--
	filter: process(instrum_helpers, instrumentation_data, 
		is_tm_magic_addr_async, sc_cpu_in_filtered, sc_arb_out_filtered, 
		sc_cpu_out, state, rdy_cnt_busy, tm_cmd) is
	begin
		sc_cpu_out_filtered <= sc_cpu_out;
		sc_cpu_in <= sc_cpu_in_filtered;
		sc_arb_out <= sc_arb_out_filtered;
		
		if tm_cmd /= none or rdy_cnt_busy = '1' then
			sc_cpu_in.rdy_cnt <= "11";
 		end if;
		
		-- set tm_broadcast flag for conflict detection,
		-- which gets mapped to broadcast.valid by arbiter
		case state is
			when COMMIT | EARLY_FLUSH | EARLY_COMMIT => 
				sc_arb_out.tm_broadcast <= '1';
			when TRANSACTION | WAIT_TOKEN | 
			EARLY_WAIT_TOKEN | ABORT =>
				-- no writes to mem. should happen 
				sc_arb_out.tm_broadcast <= '0';
			when BYPASS =>
				sc_arb_out.tm_broadcast <= '0';
		end case;
				
		-- overrides when TM command is issued
		if is_tm_magic_addr_async = '1' then		
			sc_cpu_out_filtered.wr <= '0';
			-- ignore diagnostic reads
			sc_cpu_out_filtered.rd <= '0';
		end if;
				 
		if instrumentation then
			-- override rd_data, where appropriate
			next_instr_helpers <= instrum_helpers;
		
			if sc_cpu_out.rd = '1' then
				next_instr_helpers.hold_instr_value <= '0';
				if is_tm_magic_addr_async = '1' then
					next_instr_helpers.hold_instr_value <= '1';
				
					case sc_cpu_out.address(2 downto 0) is
						when RETRIES_ADDR =>
							next_instr_helpers.last_value <=
								instrumentation_data.retries;
						when COMMITS_ADDR =>
							next_instr_helpers.last_value <=
								instrumentation_data.commits;
						when EARLY_COMMITS_ADDR =>
							next_instr_helpers.last_value <=
								instrumentation_data.early_commits;
						when READ_SET_ADDR =>
							next_instr_helpers.last_value <=
								(31 downto way_bits+1 => '0') & 
									instrumentation_data.read_set;
						when WRITE_SET_ADDR =>
							next_instr_helpers.last_value <=
								(31 downto way_bits+1 => '0') &
								instrumentation_data.write_set;
						when READ_OR_WRITE_SET_ADDR =>
							next_instr_helpers.last_value <=
								(31 downto way_bits+1 => '0') &
								instrumentation_data.read_or_write_set;
						when others =>
							next_instr_helpers.last_value <=
								(others => 'X');
					end case;
				end if;
			end if;
			
			if instrum_helpers.hold_instr_value = '1' then
				sc_cpu_in.rd_data <= 
					std_logic_vector(instrum_helpers.last_value);
			end if;
		end if;
		
		assert not ((
			state = TRANSACTION or
			state = WAIT_TOKEN or 
			state = EARLY_WAIT_TOKEN or
			state = ABORT) and 
			sc_arb_out_filtered.wr = '1');
	end process;
	
	--
	--	Register signals
	--
	sync: process(reset, clk) is
	begin
		if reset = '1' then
			state <= BYPASS;
			
			is_tm_magic_addr_sync <= '0';
			sc_cpu_out_dly.wr <= '0';
			sc_cpu_out_dly.wr_data <= (others => 'X');
			
			commit_finished_dly_internal_1 <= '0';
			commit_finished_dly <= '0';
								
			-- containment_state <= -- don't care
-- 			rdy_cnt_busy <= '0';
			
			if instrumentation then
				instrumentation_data <= ((others => '0'), (others => '0'), 
					(others => '0'), (others => '0'), (others => '0'),
					(others => '0'));
				instrum_helpers <= ((others => '0'), '0');
			end if;
			
			early_commit_starting <= '0';
		elsif rising_edge(clk) then
			state <= next_state;
			
			is_tm_magic_addr_sync <= is_tm_magic_addr_async;
			sc_cpu_out_dly <= sc_cpu_out;
			
			commit_finished_dly_internal_1 <= commit_finished;
			commit_finished_dly <= commit_finished_dly_internal_1;
			
			containment_state <= next_containment_state;
			
-- 			rdy_cnt_busy <= next_rdy_cnt_busy;
			
			if instrumentation then
				instrumentation_data <= next_instrumentation_data;
				instrum_helpers <= next_instr_helpers;
			end if;
			
			early_commit_starting <= '0';
			if state = EARLY_WAIT_TOKEN then
				early_commit_starting <= '1';
			end if;
		end if;
	end process sync;
	
	--
	--	Decode TM command
	--
	decode_tm_cmd: process (sc_cpu_out_dly, is_tm_magic_addr_sync) is
	begin
		tm_cmd <= none;			
		
		-- could be moved to previous cycle
		if sc_cpu_out_dly.wr = '1' and is_tm_magic_addr_sync = '1' then
			tm_cmd <= tm_cmd_type'val(to_integer(unsigned(
				sc_cpu_out_dly.wr_data(tm_cmd_raw'range))));
		end if;
	end process decode_tm_cmd;	

end rtl;
