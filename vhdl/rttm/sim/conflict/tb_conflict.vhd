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


entity tb_conflict is
end tb_conflict;

architecture behav of tb_conflict is


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
				
		commit_in_allow <= '1';
		
		wait until commit_out_try = '0';
		
		wait for cycle;
		commit_in_allow <= '0'; 
	end process commit_coordinator;

--
--	Verification
--

	verify_states: process is
		-- states must be assumed in this order
		constant states: states_type :=
			(normal_transaction, early_commit_wait_token,
			early_commit, early_committed_transaction);
	begin
		wait until started_of;
		
		waitStates(states, << signal .dut.state: state_type>>);
		
		ended_of <= true;
		
		waitStates(states_type'(early_committed_transaction,  
			no_transaction), << signal .dut.state: state_type>>);
			
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
	
		constant conflict_addr: natural := 1;
		constant conflict_data: natural := 16#55555555#;
	
		variable result: natural;
	begin
		sc_out_cpu.nc <= '0';
		
		wait until falling_edge(reset);		
		wait until rising_edge(clk);
		
		sc_write(clk, conflict_addr, conflict_data, sc_out_cpu, sc_in_cpu);
		
		sc_write(clk, TM_MAGIC, 
			(31 downto tm_cmd_raw'length => '0') & TM_CMD_START_TRANSACTION, 
			sc_out_cpu, sc_in_cpu);
			
		-- TODO write	
		
		sc_read(clk, conflict_addr, result, sc_out_cpu, sc_in_cpu);
		
				
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
