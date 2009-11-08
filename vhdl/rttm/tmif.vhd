library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;
use work.tm_internal_pack.all;



entity tmif is

generic (
	addr_width		: integer;
	way_bits		: integer;
	rttm_instrum	: boolean := false;
	confl_rds_only	: boolean := false
	);

port (
	clk					: in std_logic;
	reset				: in std_logic;
	
	--
	--	Commit logic
	--
	
	-- set until transaction finished/aborted
	commit_out_try			: out std_logic;
	commit_in_allow			: in std_logic;

	--
	--	Commit addresses
	--
	
	-- broadcast.valid is set for one cycle
	-- broadcast.address value is held until next .valid
	broadcast				: in tm_broadcast_type;

	--
	--	Memory IF to cpu
	--
	sc_out_cpu		: in sc_out_type;
	sc_in_cpu		: out sc_in_type;		

	--
	--	Memory IF to arbiter
	--
	sc_out_arb		: out sc_out_type;
	sc_in_arb		: in sc_in_type;

	--
	--	Rollback exception
	--
	exc_tm_rollback	: out std_logic
		
);

end tmif;

architecture rtl of tmif is

		
	signal state, next_state		: state_type;
		
	signal conflict					: std_logic;
	
	-- set asynchronously
	signal tm_cmd					: tm_cmd_type;
	

	signal commit_finished				: std_logic;
	
	signal commit_out_try_internal	: std_logic;
	
	-- filter signals to/from CPU/arbiter
	signal sc_out_cpu_filtered		: sc_out_type;
	signal sc_in_cpu_filtered		: sc_in_type;
	signal sc_out_arb_filtered		: sc_out_type;

	signal is_tm_magic_addr_async: std_logic;
	signal is_tm_magic_addr_sync: std_logic;
	
	signal sc_out_cpu_dly: sc_out_type;
	 
	signal transaction_start: std_logic;
	
	
	signal tag_full: std_logic;
	
	-- commit_finished signal is delayed long enough so that other processors
	-- detect conflict before they can obtain commit token
	-- (after commit_finished_dly has caused reset of commit_out_try)		
	signal commit_finished_dly: std_logic;
	signal commit_finished_dly_internal_1: std_logic;
	
	signal tm_busy: std_logic;
	
	type rollback_state_type is (rbi0, rbb0, rbb1, rbb2, rba1, rba2, rbi);
	signal next_rollback_state, rollback_state: rollback_state_type;
	
	-- instrumentation
	
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
	
	signal instr_helpers: instr_helpers_type;
	signal next_instr_helpers: instr_helpers_type;
	
	constant RETRIES_ADDR: std_logic_vector := "000";
	constant COMMITS_ADDR: std_logic_vector := "001";
	constant EARLY_COMMITS_ADDR: std_logic_vector := "010";
		
	constant READ_SET_ADDR: std_logic_vector := "011";
	constant WRITE_SET_ADDR: std_logic_vector := "100";
	constant READ_OR_WRITE_SET_ADDR: std_logic_vector := "101";
	
	
begin

	is_tm_magic_addr_async <= '1' when
		sc_out_cpu.address(TM_MAGIC_DETECT'range) = TM_MAGIC_DETECT else '0';

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
		from_cpu => sc_out_cpu_filtered,
		to_cpu => sc_in_cpu_filtered,
		to_mem => sc_out_arb_filtered,
		from_mem => sc_in_arb,
		
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
		
	
	sync: process(reset, clk) is
	begin
		if reset = '1' then
			state <= no_transaction;
			
			is_tm_magic_addr_sync <= '0';
			sc_out_cpu_dly <= sc_out_idle;
			
			commit_finished_dly_internal_1 <= '0';
			commit_finished_dly <= '0';
			
			-- rollback_state <= -- don't care
			
			if rttm_instrum then
				instrumentation <= ((others => '0'), (others => '0'), 
					(others => '0'), (others => '0'), (others => '0'),
					(others => '0'));
				instr_helpers <= ((others => '0'), '0');
			end if;
		elsif rising_edge(clk) then
			state <= next_state;
			
			is_tm_magic_addr_sync <= is_tm_magic_addr_async;
			sc_out_cpu_dly <= sc_out_cpu;
			
			commit_finished_dly_internal_1 <= commit_finished;
			commit_finished_dly <= commit_finished_dly_internal_1;
			
			rollback_state <= next_rollback_state;
			
			if rttm_instrum then
				instrumentation <= next_instrumentation;
				instr_helpers <= next_instr_helpers;
			end if;
		end if;
	end process sync;
	
	gen_tm_cmd: process (sc_out_cpu_dly, is_tm_magic_addr_sync) is
	begin
		tm_cmd <= none;			
		
		-- could be moved to previous cycle
		if sc_out_cpu_dly.wr = '1' and is_tm_magic_addr_sync = '1' then
			tm_cmd <= tm_cmd_type'val(to_integer(unsigned(
				sc_out_cpu_dly.wr_data(tm_cmd_raw'range))));
		end if;
	end process gen_tm_cmd;	

	
	--
	-- TM STATE MACHINE
	--

	state_machine: process(commit_finished_dly, commit_in_allow, conflict, 
		instrumentation, rollback_state, state, tag_full, tm_cmd) is		
	begin
		next_state <= state;
		exc_tm_rollback <= '0';
		tm_busy <= '0';
		
		transaction_start <= '0';
		
		next_rollback_state <= rollback_state;
		
		if rttm_instrum then
			next_instrumentation.retries <= instrumentation.retries;
			next_instrumentation.commits <= instrumentation.commits;
			next_instrumentation.early_commits <= instrumentation.early_commits;
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
				tm_busy <= '1';
			
				if conflict = '1' then
					next_state <= rollback;
					next_rollback_state <= rbb0;
					
					if rttm_instrum then
						next_instrumentation.retries <= 
							instrumentation.retries + 1;
					end if;
				elsif commit_in_allow = '1' then
					next_state <= commit;
					
					if rttm_instrum then
						next_instrumentation.commits <= 
							instrumentation.commits + 1;
					end if;
				end if;
			
			when commit =>
				tm_busy <= '1';
				
				-- TODO check condition
				if commit_finished_dly = '1' then
					next_state <= no_transaction;
				end if;
				
			when early_commit_wait_token =>
				tm_busy <= '1';
			
				if conflict = '1' then
					next_state <= rollback;
					
					if rttm_instrum then
						next_instrumentation.retries <= 
							instrumentation.retries + 1;
					end if;
				elsif commit_in_allow = '1' then
					next_state <= early_commit;
					
					if rttm_instrum then
						next_instrumentation.early_commits <= 
							instrumentation.early_commits + 1;
					end if;
				end if;
				
			when early_commit =>
				tm_busy <= '1';
			
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

				-- 2 cycles delay to ensure that exception will be handled when
				-- next bytecode is issued

				-- don't set busy when asynchronous, but else delay for 2 cycles
				-- TODO refer to documentation
			
				case rollback_state is
					when rbi0 =>
						exc_tm_rollback <= '1';
					
						next_rollback_state <= rbi;
					
					when rbb0 =>
						exc_tm_rollback <= '1';
					
						next_rollback_state <= rbb1;
						tm_busy <= '1';
					
					when rbb1 =>
						next_rollback_state <= rbb2;
						tm_busy <= '1';
					
					when rbb2 =>
						next_rollback_state <= rbi;
						tm_busy <= '1';
					
					when rbi =>
						null;
												
					when rba1 =>
						next_rollback_state <= rba2;
						tm_busy <= '1';
					
					when rba2 =>
						next_state <= no_transaction;
						tm_busy <= '1';
						
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
		
	
	commit_out_try_internal <= '1' 
		when state = commit_wait_token or state = early_commit_wait_token or
		state = commit or state = early_commit or
		state = early_committed_transaction
		else '0';
	
	commit_out_try <= commit_out_try_internal;		
	

	-- sets sc_out_cpu_filtered, sc_out_arb, sc_in_cpu
	process(instr_helpers, instrumentation, is_tm_magic_addr_async, 
		sc_in_cpu_filtered, sc_out_arb_filtered, sc_out_cpu, state, tm_busy, 
		tm_cmd) is
	begin
		-- TODO writes/reads outside of RAM	
		sc_out_cpu_filtered <= sc_out_cpu;
		sc_in_cpu <= sc_in_cpu_filtered;
		sc_out_arb <= sc_out_arb_filtered;
		
		if tm_cmd /= none or tm_busy = '1' then
			sc_in_cpu.rdy_cnt <= "11";
 		end if;
		
		-- set broadcast
		case state is
			when commit | early_commit | early_committed_transaction => 
				sc_out_arb.tm_broadcast <= '1';
			when normal_transaction | commit_wait_token | 
			early_commit_wait_token | rollback =>
				-- TODO no writes to mem should happen 
				sc_out_arb.tm_broadcast <= '0';
			when no_transaction =>
				sc_out_arb.tm_broadcast <= '0';
		end case;
				
		-- overrides when TM command is issued
		if is_tm_magic_addr_async = '1' then		
			sc_out_cpu_filtered.wr <= '0';
			-- ignore diagnostic reads
			sc_out_cpu_filtered.rd <= '0';
		end if;
		
		if rttm_instrum then
			next_instr_helpers <= instr_helpers;
		
			if sc_out_cpu.rd = '1' then
				next_instr_helpers.hold_instr_value <= '0';
				if is_tm_magic_addr_async = '1' then
					next_instr_helpers.hold_instr_value <= '1';
				
					case sc_out_cpu.address(2 downto 0) is
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
			
			if instr_helpers.hold_instr_value = '1' then
				sc_in_cpu.rd_data <= 
					std_logic_vector(instr_helpers.last_value);
			end if;
		end if;
		
		assert not ((
			state = normal_transaction or
			state = commit_wait_token or 
			state = early_commit_wait_token or
			state = rollback) and 
			sc_out_arb_filtered.wr = '1');
	end process; 

end rtl;
