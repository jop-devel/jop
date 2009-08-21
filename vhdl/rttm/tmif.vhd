library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use ieee.std_logic_unsigned.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;

entity tmif is

-- generic (
-- 
-- 
-- );

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
	broadcast				: in tm_broadcast_type;

	--
	--	Memory IF to cpu
	--
	sc_cpu_out		: in sc_out_type;
	sc_cpu_in		: out sc_in_type;		
	-- memory access types
	-- TODO more hints about memory access type?

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

end tmif;

architecture rtl of tmif is

	--
	-- TM STATE MACHINE
	--

	type state_type is (
		no_transaction,

		start_normal_transaction,
		normal_transaction,
		commit_wait_token, -- TODO additional states to register commit_in_allow?
		commit,

		early_commit_wait_token,
		early_commit,
		early_committed_transaction, -- TODO same for expl./OF EC?

		end_transaction, -- TODO only for EC?
		
		rollback
		);
		
	signal state, next_state		: state_type;
	signal nesting_cnt				: nesting_cnt_type;
	signal next_nesting_cnt			: nesting_cnt_type;
		
	signal conflict					: std_logic;
	
	-- set synchronously
	signal tm_cmd					: tm_cmd_type;
	
	-- TODO
	--signal tm_cmd_valid				: std_logic;
	
	
	signal commit_finished			: std_logic;
	
	signal read_tag_memory_of		: std_logic;
	signal write_buffer_of			: std_logic;


	signal sc_cpu_in_filtered		: sc_in_type;

	signal processing_tm_cmd		: std_logic;	
	signal next_processing_tm_cmd	: std_logic;
	
	signal tm_cmd_rdy_cnt			: unsigned(RDY_CNT_SIZE-1 downto 0);
begin

	--
	-- TM STATE MACHINE
	--

	cmp_tm: entity work.tm(rtl)
	port map (
		clk => clk,
		reset => reset,
		from_cpu => sc_cpu_out,
		to_cpu => sc_cpu_in_filtered,
		to_mem => sc_arb_out,
		from_mem => sc_arb_in
		);			

	sync: process(reset, clk) is
	begin
		if reset = '1' then
			state <= no_transaction;
			nesting_cnt <= (others => '0');
			
			processing_tm_cmd <= '0';
			
			--tm_cmd <= none;
		elsif rising_edge(clk) then
			state <= next_state;
			nesting_cnt <= next_nesting_cnt;
			
			processing_tm_cmd <= next_processing_tm_cmd;			
		end if;
	end process sync;
	
	gen_tm_cmd: process (sc_cpu_out) is
	begin
		tm_cmd <= none;
		
		-- TODO
		if sc_cpu_out.wr = '1' then
			if sc_cpu_out.address = TM_MAGIC then
				tm_cmd <= tm_cmd_type'val(to_integer(unsigned(
					sc_cpu_out.wr_data(tm_cmd_raw'range))));
			end if;
		end if;
	end process gen_tm_cmd;	

	
	gen_rdy_cnt_sel: process(processing_tm_cmd, tm_cmd, tm_cmd_rdy_cnt) is
	begin
		next_processing_tm_cmd <= processing_tm_cmd;	
	
		if processing_tm_cmd = '0' then
			if tm_cmd /= none then
				next_processing_tm_cmd <= '1';
			end if;
		else
			if tm_cmd_rdy_cnt = "00" then
				next_processing_tm_cmd <= '0';
			end if;
		end if;
	end process gen_rdy_cnt_sel;
	
	sc_cpu_in.rdy_cnt <= sc_cpu_in_filtered.rdy_cnt 
		when next_processing_tm_cmd = '0'
		else tm_cmd_rdy_cnt;

	
	nesting_cnt_process: process(nesting_cnt, tm_cmd) is
	begin	
		case tm_cmd is
			when start_transaction =>
				next_nesting_cnt <= nesting_cnt + 1;
			when end_transaction =>
				next_nesting_cnt <= nesting_cnt - 1;
			when others =>
				next_nesting_cnt <= nesting_cnt;
		end case;				
	end process nesting_cnt_process; 

	-- sets next_state, exc_tm_rollback, tm_cmd_rdy_cnt
	state_machine: process(state, tm_cmd, nesting_cnt, commit_in_allow,
		conflict, write_buffer_of, read_tag_memory_of, commit_finished) is
	begin
		next_state <= state;
		exc_tm_rollback <= '0';
		tm_cmd_rdy_cnt <= "00";
		
		case state is
			when no_transaction =>
				if tm_cmd = start_transaction then
						next_state <= start_normal_transaction;
						-- TODO not needed if set asynchronously
						tm_cmd_rdy_cnt <= "01";
				end if;
				
			when start_normal_transaction =>
				next_state <= normal_transaction;
			when normal_transaction =>
				case tm_cmd is
					when end_transaction =>
						if nesting_cnt = nesting_cnt_type'(0 => '1', others => '0') then
							next_state <= commit_wait_token;
							tm_cmd_rdy_cnt <= "11";
						end if;
					when early_commit =>
						next_state <= early_commit_wait_token;
						tm_cmd_rdy_cnt <= "11";
					when others => 
						null;
				end case;						
				
				if read_tag_memory_of = '1' or write_buffer_of = '1' then
					next_state <= early_commit_wait_token;
				end if;
				
				if conflict = '1' then
					next_state <= rollback;
				end if;
			when commit_wait_token =>
				if commit_in_allow = '1' then
					next_state <= commit;
				end if;
			
				if conflict = '1' then
					next_state <= rollback;
				end if;
			when commit =>	
				
			when early_commit_wait_token =>
				if commit_in_allow = '1' then
					next_state <= early_commit;
					-- TODO start commit
				end if;
				
				if conflict = '1' then
					next_state <= rollback;
				end if;
			when early_commit =>
				if commit_finished = '1' then
					next_state <= early_committed_transaction;
				end if;
			when early_committed_transaction =>
				case tm_cmd is
					when end_transaction =>
						if nesting_cnt = 
							nesting_cnt_type'(0 => '1', others => '0') then
							next_state <= end_transaction;
							tm_cmd_rdy_cnt <= "10"; -- TODO
						end if;
					when others =>
						null;
				end case;
				
			when end_transaction =>
				-- TODO
				next_state <= no_transaction;
				-- TODO not needed if set asynchronously
				tm_cmd_rdy_cnt <= "01";
				
			when rollback =>
				exc_tm_rollback <= '1';
				-- TODO
				next_state <= no_transaction;
				
			
		end case;
	end process state_machine;
	
	-- TODO register?
	commit_out_try <= '1' 
		when state = commit_wait_token or state = early_commit_wait_token or
		state = early_committed_transaction or state = commit
		-- or state = end_transaction
		else '0';

end rtl;
