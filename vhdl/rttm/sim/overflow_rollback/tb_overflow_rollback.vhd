library std;
library ieee;

use std.textio.all;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use ieee.math_real.log2;
use ieee.math_real.ceil;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;
use work.tm_internal_pack.all;


entity tb_overflow_rollback is
	generic (
		overflow_by_write: boolean := false
	);
end tb_overflow_rollback;

architecture behav of tb_overflow_rollback is


--
--	Settings
--

constant MEM_BITS			: integer := 15;

constant addr_width		: integer := 18;	-- address bits of cachable memory
constant way_bits		: integer := 3;		-- 2**way_bits is number of entries



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
	
	signal sc_out_cpu: sc_out_type;
	signal sc_in_cpu: sc_in_type;
	signal sc_out_arb: sc_out_type;
	signal sc_in_arb: sc_in_type;		
	signal exc_tm_rollback: std_logic;

	signal broadcast: tm_broadcast_type := 
		(
			valid => '0',
			address => (others => 'U')
		);
		
	signal started_of: boolean := false;
	signal ended_of: boolean := false;
	signal ended_transaction: boolean := false;

--
--	Test activity flags
--

	signal testing_commit: boolean := false;
	signal testing_conflict: boolean := false;

--
--	State machine tracking
--
	
	type states_type is array (natural range <>) of state_type;
	procedure waitStates (constant states: in states_type; 
		signal state: state_type) is
	begin
		for i in states'low to states'high-1 loop
			assert state = states(i); 
			loop
				wait until rising_edge(clk);
				if state = states(i) then
					null;
				elsif state = states(i+1) then
					exit;
				else
					assert false;
				end if;
			end loop;
		end loop;
	end procedure waitStates;
	
	
begin

--
--	Testbench
--

	dut: entity work.tmif(rtl)
	generic map (
		addr_width => addr_width,
		way_bits => way_bits
	)	
	port map (
		clk => clk,
		reset => reset,
		commit_out_try => commit_out_try,
		commit_in_allow => commit_in_allow,
		broadcast => broadcast,
		sc_out_cpu => sc_out_cpu,
		sc_in_cpu => sc_in_cpu,
		sc_out_arb => sc_out_arb,
		sc_in_arb => sc_in_arb,
		exc_tm_rollback => exc_tm_rollback
		);
		
	commit_coordinator: process is
	begin
		commit_in_allow <= '0';
	
		wait until commit_out_try = '1';
		
		wait for cycle * 3;
		
		broadcast <= ( valid => '1', address => std_logic_vector(
			to_unsigned(1, SC_ADDR_SIZE)));
		
		wait for cycle;
		
		broadcast <= ( valid => '0', address => (others => 'U'));
			
		-- TODO	
-- 		commit_in_allow <= '1';
-- 		
-- 		wait until commit_out_try = '0';
-- 		
-- 		wait for cycle;
-- 		commit_in_allow <= '0'; 
	end process commit_coordinator;

--
--	Verification
--

	verify_states: process is
	begin
		wait until started_of;
		
		waitStates(states_type'(normal_transaction, early_commit_wait_token,
			rollback_signal), 
			<< signal .dut.state: state_type>>);
		
		ended_of <= true;
		
		waitStates(states_type'(rollback_signal, no_transaction), 
			<< signal .dut.state: state_type>>);
			
		ended_transaction <= true;
	end process verify_states;


--	
--	Input
--
				
	gen: process is
		-- main memory
		
		type ram_type is array (0 to 2**MEM_BITS-1) 
			of std_logic_vector(31 downto 0); 
		alias ram is << signal .memory.main_mem.ram: ram_type >>;
		
		variable ignored: natural;	
	
	begin
		sc_out_cpu.nc <= '0';
		
		wait until falling_edge(reset);		
		wait until rising_edge(clk);

		for i in 1 to 2**way_bits+1 loop
			sc_write(clk, i, i, sc_out_cpu, sc_in_cpu);
		end loop;
		
		sc_write(clk, TM_MAGIC, 
			(31 downto tm_cmd_raw'length => '0') & TM_CMD_START_TRANSACTION, 
			sc_out_cpu, sc_in_cpu);
		
		for i in 1 to 2**way_bits-1 loop
			sc_read(clk, i, ignored, sc_out_cpu, sc_in_cpu);
		end loop;
		
		started_of <= true;
		testing_commit <= true;
		testing_conflict <= true;

		if overflow_by_write then
			sc_write(clk, 2**way_bits, 2**way_bits, sc_out_cpu, sc_in_cpu);
		else
			sc_read(clk, 2**way_bits, ignored, sc_out_cpu, sc_in_cpu);
		end if;
		
		assert ended_of;
		assert not ended_transaction;
							
		-- TODO
-- 		sc_write(clk, TM_MAGIC, 
-- 			(31 downto tm_cmd_raw'length => '0') & TM_CMD_END_TRANSACTION,
-- 			sc_out_cpu, sc_in_cpu);
		
		for i in 1 to 3 loop
			wait until rising_edge(clk);
		end loop;
		
		sc_write(clk, TM_MAGIC, 
			(31 downto tm_cmd_raw'length => '0') & TM_CMD_ABORTED,
			sc_out_cpu, sc_in_cpu);
		
		-- TODO remove
		for i in 1 to 2 loop
			wait until rising_edge(clk);
		end loop;
		
		testing_commit <= false;
		testing_conflict <= false;
		
		assert ended_transaction;
		
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
		sc_mem_out => sc_out_arb,
		sc_mem_in => sc_in_arb
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
