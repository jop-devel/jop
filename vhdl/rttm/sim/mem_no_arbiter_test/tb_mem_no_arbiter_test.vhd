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

use std.textio.all;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use ieee.math_real.log2;
use ieee.math_real.ceil;

use work.sc_pack.all;


entity tb_mem_no_arbiter_test is
end tb_mem_no_arbiter_test;

architecture behav of tb_mem_no_arbiter_test is


--
--	Settings
--

constant MEM_BITS			: integer := 15;

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

	signal sc_mem_out: sc_out_type;
	signal sc_mem_in: sc_in_type;
begin

--
--	Testbench
--

	dut: entity work.mem_no_arbiter(behav)
	generic map (
		MEM_BITS => MEM_BITS
		)
	port map (
		clk => clk,
		reset => reset,
		sc_mem_out => sc_mem_out,
		sc_mem_in => sc_mem_in
		);

	gen: process is
		variable result: natural;
	begin
		wait until falling_edge(reset);
		wait until rising_edge(clk);
	
		sc_write(clk, 1, 123, sc_mem_out, sc_mem_in);
		assert now = 60 ns;		
		
		sc_read(clk, 1, result, sc_mem_out, sc_mem_in);
		assert now = 100 ns and result = 123;
		
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
