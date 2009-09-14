library ieee;

use std.textio.all;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
-- TODO
use ieee.math_real.log2;
use ieee.math_real.ceil;

use work.tm_pack.all;

entity tb_tm_coordinator is
end tb_tm_coordinator;

architecture behav of tb_tm_coordinator is


--
--	Settings
--

constant cpu_cnt			: integer := 8;

--
--	Generic
--

signal finished				: boolean := false;

signal clk					: std_logic := '1';
signal reset				: std_logic;

constant cycle				: time := 10 ns;
constant reset_time			: time := 8 ns;

--
--	Testbench
--

subtype cpu_flags is std_logic_vector(0 to cpu_cnt-1);

signal commit_try			: cpu_flags;
signal commit_allow			: cpu_flags;

begin

--
--	Testbench
--

	dut: entity work.tm_coordinator(rtl)
	generic map (
		cpu_cnt => cpu_cnt
		)
	port map (
		clk => clk,
		reset => reset,
		commit_try => commit_try,
		commit_allow => commit_allow
		);

	-- TODO use postponed process in parallel or ugly manual delay? 

	gen: process is
	begin
		commit_try <= (others => '0');
	
		wait until falling_edge(reset);
		wait until rising_edge(clk);
				
		assert commit_allow= (0 to cpu_cnt-1 => '0');
		
		commit_try <= (others => '1');
		wait until rising_edge(clk);
		
		commit_try(3) <= '0';
		wait for 0 ns;
		
		assert commit_allow = cpu_flags'(0 => '1', others => '0');
		
		commit_try(1) <= '0';				
		wait for 0 ns;
		
		assert commit_allow = cpu_flags'(0 => '1', others => '0');		
		
		wait until rising_edge(clk);
		wait until rising_edge(clk);
		
		commit_try(0) <= '0';
		wait for 0 ns;
		
		wait until rising_edge(clk);
		
		assert commit_allow = cpu_flags'(2 => '1', others => '0');
		
		commit_try <= (others => '0');
		
		wait until rising_edge(clk);
		
		assert commit_allow = cpu_flags'(others => '0');
		
		finished <= true;
		write(output, "Test finished.");
		wait;
	end process gen;

--
--	Generic
--

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
