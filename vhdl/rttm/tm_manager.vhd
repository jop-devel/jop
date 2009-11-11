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


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;
use work.tm_internal_pack.all;



entity tm_manager is

generic (
	-- width of memory addresses cached during transaction
	addr_width		: integer;
	
	-- Pattern used to detect magic address
	-- Magic address to execute TM commands is located in upper half 
	-- of external SRAM mirror.
	-- The lower bits are actually ignored.
	-- TODO Keep in synch with com.jopdesign.sys.Const.MEM_TM_MAGIC and
	-- com.jopdesign.build.ReplaceAtomicAnnotation.
	tm_magic_detect	: std_logic_vector(18 downto 17) := (others => '1');
	
	-- fully associative read and write buffer has 2**way_bits entries
	way_bits		: integer;
	
	-- enable instrumentation (without probe effects)
	-- turn off to lower hardware consumption
	rttm_instrum	: boolean := false;
	
	-- enable to only detect truly conflicting reads,
	-- i.e. reads of an address not yet written during the transaction
	confl_rds_only	: boolean := false
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
	--	Rollback exception
	--
	exc_tm_rollback	: out std_logic
		
);

end tm_manager;

architecture rtl of tm_manager is

	-- State machines
		
	signal state, next_state		: state_type;

	-- rollback state has internal state machine for correct timing and rdy_cnt
	-- generation
	-- rbi... rollback idle
	-- rbb... rollback busy
	-- rba... rollback aborted
	type rollback_state_type is (rbi0, rbb0, rbb1, rbb2, rba1, rba2, rbi);
	signal next_rollback_state, rollback_state: rollback_state_type;

		
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
	
	signal commit_token_request_buf	: std_logic;
	signal sc_cpu_out_dly: sc_out_type;
	
	
	
	-- commit_finished signal is delayed long enough so that other processors
	-- detect conflict before they can obtain commit token
	-- (after commit_finished_dly has caused reset of commit_out_try)		
	signal commit_finished_dly: std_logic;
	signal commit_finished_dly_internal_1: std_logic;
	
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
	
	signal instrumentation: instrumentation_type;
	signal next_instrumentation: instrumentation_type;
	
	type instr_helpers_type is record
		last_value: unsigned(31 downto 0);
		hold_instr_value: std_logic;
	end record;
	
	signal instrum_helpers: instr_helpers_type;
	signal next_instr_helpers: instr_helpers_type;
	
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
	cmp_tm: entity work.tm(rtl)
	generic map (
		addr_width => addr_width,
		way_bits => way_bits,
		rttm_instrum => rttm_instrum,
		confl_rds_only => confl_rds_only
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
		
		read_set => next_instrumentation.read_set,
		write_set => next_instrumentation.write_set,
		read_or_write_set => next_instrumentation.read_or_write_set
	);
	
	--
	--	Concurrent assignments
	--
	
	is_tm_magic_addr_async <= '1' when
		sc_cpu_out.address(tm_magic_detect'range) = tm_magic_detect else '0';
	
	-- request or hold commit token during these states  
	commit_token_request_buf <= '1' 
		when state = commit_wait_token or state = early_commit_wait_token or
		state = commit or state = early_commit or
		state = early_committed_transaction
		else '0';
	
	commit_token_request <= commit_token_request_buf;			
	
	--
	--	TM STATE MACHINE
	--
	state_machine: process(commit_finished_dly, commit_token_grant, conflict, 
		rollback_state, state, tag_full, tm_cmd, instrumentation) is		
	begin
		next_state <= state;
		exc_tm_rollback <= '0';
		rdy_cnt_busy <= '0';
		
		transaction_start <= '0';
		
		next_rollback_state <= rollback_state;
		
		if rttm_instrum then
			next_instrumentation.retries <= instrumentation.retries;
			next_instrumentation.commits <= instrumentation.commits;
			next_instrumentation.early_commits <= 
				instrumentation.early_commits;
		end if;
		
		case state is
			when no_transaction =>
				if tm_cmd = start_transaction then
					next_state <= normal_transaction;
					
					transaction_start <= '1';
				end if;
				
			when normal_transaction =>
				if tag_full = '1' then
					next_state <= early_commit_wait_token;
				end if;
			
				case tm_cmd is
					when end_transaction =>
						next_state <= commit_wait_token;
					when early_commit =>
						next_state <= early_commit_wait_token;
					when abort =>
						next_state <= rollback;
						next_rollback_state <= rbb0;
						
						if rttm_instrum then
							next_instrumentation.retries <= 
								instrumentation.retries + 1;
						end if;
					when aborted =>
						-- command is only issued if an exception is being 
						-- handled
						next_state <= no_transaction;
					when start_transaction | none => 
						null;
				end case;						
								
				if conflict = '1' then
					next_state <= rollback;
					
					if rttm_instrum then
						next_instrumentation.retries <= 
							instrumentation.retries + 1;
					end if;
					
					case tm_cmd is
						when none =>						
							next_rollback_state <= rbi0;
						when aborted =>
							-- don't miss aborted command					
	 						next_state <= no_transaction;
						when others =>
							next_rollback_state <= rbb0;
					end case;			
				end if;
								
			when commit_wait_token =>
				rdy_cnt_busy <= '1';
			
				if conflict = '1' then
					next_state <= rollback;
					next_rollback_state <= rbb0;
					
					if rttm_instrum then
						next_instrumentation.retries <= 
							instrumentation.retries + 1;
					end if;
				elsif commit_token_grant = '1' then
					next_state <= commit;
					
					if rttm_instrum then
						next_instrumentation.commits <= 
							instrumentation.commits + 1;
					end if;
				end if;
			
			when commit =>
				rdy_cnt_busy <= '1';
				
				-- TODO check condition
				if commit_finished_dly = '1' then
					next_state <= no_transaction;
				end if;
				
			when early_commit_wait_token =>
				rdy_cnt_busy <= '1';
			
				if conflict = '1' then
					next_state <= rollback;
					
					if rttm_instrum then
						next_instrumentation.retries <= 
							instrumentation.retries + 1;
					end if;
				elsif commit_token_grant = '1' then
					next_state <= early_commit;
					
					if rttm_instrum then
						next_instrumentation.early_commits <= 
							instrumentation.early_commits + 1;
					end if;
				end if;
				
			when early_commit =>
				rdy_cnt_busy <= '1';
			
				-- TODO check condition
				if commit_finished_dly = '1' then
					next_state <= early_committed_transaction;
				end if;
				
			when early_committed_transaction =>
				case tm_cmd is
					when end_transaction =>
						next_state <= no_transaction;

						if rttm_instrum then
							next_instrumentation.commits <= 
								instrumentation.commits + 1;
						end if;
					when aborted =>
						 -- TODO not consistent with exception handling
						assert false;
						next_state <= no_transaction;						
					when abort =>
						null; 
						-- not supported since transaction may have changed
						-- main memory 
					when others =>
						null;
				end case;
				
			when rollback =>

				-- If we are about to end a transaction (by executing a end 
				-- transaction command), we need to assure that the try block 
				-- is not exited (the transaction command write not finished)
				-- before the exception is raised (a special bytecode issued)   
				-- 2 cycles delay ensure that the exception will be handled 
				-- when the next bytecode is issued.
				-- TODO refer to documentation
				-- TODO too many wait cycles?
			
				case rollback_state is
					when rbi0 =>
						exc_tm_rollback <= '1';
					
						next_rollback_state <= rbi;
					
					when rbb0 =>
						exc_tm_rollback <= '1';
					
						next_rollback_state <= rbb1;
						rdy_cnt_busy <= '1';
					
					when rbb1 =>
						next_rollback_state <= rbb2;
						rdy_cnt_busy <= '1';
					
					when rbb2 =>
						next_rollback_state <= rbi;
						rdy_cnt_busy <= '1';
					
					when rbi =>
						null;
												
					when rba1 =>
						next_rollback_state <= rba2;
						rdy_cnt_busy <= '1';
					
					when rba2 =>
						next_state <= no_transaction;
						rdy_cnt_busy <= '1';
						
				end case;
				
				case tm_cmd is
					when none =>
						null;
					when aborted =>
						next_rollback_state <= rba1;
					when others =>
						next_rollback_state <= rbb1;
				end case;
		end case;
	end process state_machine;	

	--
	--	 Adjustments to signals to/from CPU/arbiter.
	--
	filter: process(instrum_helpers, instrumentation, is_tm_magic_addr_async, 
		sc_cpu_in_filtered, sc_arb_out_filtered, sc_cpu_out, state, 
		rdy_cnt_busy, tm_cmd) is
	begin
		-- TODO writes/reads outside of RAM	
		sc_cpu_out_filtered <= sc_cpu_out;
		sc_cpu_in <= sc_cpu_in_filtered;
		sc_arb_out <= sc_arb_out_filtered;
		
		if tm_cmd /= none or rdy_cnt_busy = '1' then
			sc_cpu_in.rdy_cnt <= "11";
 		end if;
		
		-- set tm_broadcast flag for conflict detection,
		-- which gets mapped to broadcast.valid by arbiter
		case state is
			when commit | early_commit | early_committed_transaction => 
				sc_arb_out.tm_broadcast <= '1';
			when normal_transaction | commit_wait_token | 
			early_commit_wait_token | rollback =>
				-- TODO no writes to mem should happen 
				sc_arb_out.tm_broadcast <= '0';
			when no_transaction =>
				sc_arb_out.tm_broadcast <= '0';
		end case;
				
		-- overrides when TM command is issued
		if is_tm_magic_addr_async = '1' then		
			sc_cpu_out_filtered.wr <= '0';
			-- ignore diagnostic reads
			sc_cpu_out_filtered.rd <= '0';
		end if;
				 
		if rttm_instrum then
			-- override rd_data, where appropriate
			next_instr_helpers <= instrum_helpers;
		
			if sc_cpu_out.rd = '1' then
				next_instr_helpers.hold_instr_value <= '0';
				if is_tm_magic_addr_async = '1' then
					next_instr_helpers.hold_instr_value <= '1';
				
					case sc_cpu_out.address(2 downto 0) is
						when RETRIES_ADDR =>
							next_instr_helpers.last_value <=
								instrumentation.retries;
						when COMMITS_ADDR =>
							next_instr_helpers.last_value <=
								instrumentation.commits;
						when EARLY_COMMITS_ADDR =>
							next_instr_helpers.last_value <=
								instrumentation.early_commits;
						when READ_SET_ADDR =>
							next_instr_helpers.last_value <=
								(31 downto way_bits+1 => '0') & 
									instrumentation.read_set;
						when WRITE_SET_ADDR =>
							next_instr_helpers.last_value <=
								(31 downto way_bits+1 => '0') &
								instrumentation.write_set;
						when READ_OR_WRITE_SET_ADDR =>
							next_instr_helpers.last_value <=
								(31 downto way_bits+1 => '0') &
								instrumentation.read_or_write_set;
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
			state = normal_transaction or
			state = commit_wait_token or 
			state = early_commit_wait_token or
			state = rollback) and 
			sc_arb_out_filtered.wr = '1');
	end process;
	
	--
	--	Register signals
	--
	sync: process(reset, clk) is
	begin
		if reset = '1' then
			state <= no_transaction;
			
			is_tm_magic_addr_sync <= '0';
			sc_cpu_out_dly <= sc_out_idle;
			
			commit_finished_dly_internal_1 <= '0';
			commit_finished_dly <= '0';
			
			-- rollback_state <= -- don't care
			
			if rttm_instrum then
				instrumentation <= ((others => '0'), (others => '0'), 
					(others => '0'), (others => '0'), (others => '0'),
					(others => '0'));
				instrum_helpers <= ((others => '0'), '0');
			end if;
		elsif rising_edge(clk) then
			state <= next_state;
			
			is_tm_magic_addr_sync <= is_tm_magic_addr_async;
			sc_cpu_out_dly <= sc_cpu_out;
			
			commit_finished_dly_internal_1 <= commit_finished;
			commit_finished_dly <= commit_finished_dly_internal_1;
			
			rollback_state <= next_rollback_state;
			
			if rttm_instrum then
				instrumentation <= next_instrumentation;
				instrum_helpers <= next_instr_helpers;
			end if;
		end if;
	end process sync;
	
	--
	--	Decode TM command
	--
	decode_tm_cmd: process (sc_cpu_out_dly, is_tm_magic_addr_sync) is
	begin
		tm_cmd <= none;			
		
		-- TODO could be moved to previous cycle
		if sc_cpu_out_dly.wr = '1' and is_tm_magic_addr_sync = '1' then
			tm_cmd <= tm_cmd_type'val(to_integer(unsigned(
				sc_cpu_out_dly.wr_data(tm_cmd_raw'range))));
		end if;
	end process decode_tm_cmd;	

end rtl;
