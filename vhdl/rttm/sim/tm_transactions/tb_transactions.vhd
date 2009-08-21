library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use ieee.math_real.log2;
use ieee.math_real.ceil;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.tm_pack.all;

entity tb_transactions is
end tb_transactions;

architecture behav of tb_transactions is


--
--	Settings
--

constant cpu_cnt			: integer := 2;

--
--	Generic
--

signal finished				: boolean := false;

constant cpu_cnt_width		: integer := integer(ceil(log2(real(cpu_cnt-1))));
signal clk					: std_logic := '1';
signal reset				: std_logic;

constant cycle				: time := 10 ns;
constant delta				: time := cycle/2;
constant reset_time			: time := 5 ns;

--
--	Testbench
--





	signal commit_out_try: std_logic;
	signal commit_in_allow: std_logic;
	signal broadcast: tm_broadcast_type;
	signal sc_cpu_out: sc_out_type;
	signal sc_cpu_in: sc_in_type;
	signal sc_arb_out: sc_out_type;
	signal sc_arb_in: sc_in_type;
	signal exc_tm_rollback: std_logic;
begin

--
--	Testbench
--

	dut: entity work.tmif(rtl)
	port map (
		clk => clk,
		reset => reset,
		commit_out_try => commit_out_try,
		commit_in_allow => commit_in_allow,
		broadcast => broadcast,
		sc_cpu_out => sc_cpu_out,
		sc_cpu_in => sc_cpu_in,
		sc_arb_out => sc_arb_out,
		sc_arb_in => sc_arb_in,
		exc_tm_rollback => exc_tm_rollback
		);

	gen: process is
		constant idle: sc_out_type :=
			(
			(others => '0'),
			(others => '0'),
			'0',
			'0',
			'0',
			'0',
			'0'); 
	begin
		sc_cpu_out <= idle;
	
		wait until falling_edge(reset);
		wait until rising_edge(clk);
		
		sc_cpu_out.wr <= '1';
		sc_cpu_out.address <= (SC_ADDR_SIZE-1 downto 0 => '0') or TM_MAGIC;
		sc_cpu_out.wr_data <= (31 downto tm_cmd_raw'high+1 => '0') & 
			TM_CMD_START_TRANSACTION;
		
		wait until rising_edge(clk);
		
		sc_cpu_out <= idle;
		
		finished <= true;
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
