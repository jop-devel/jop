--
--
--  This file is a part of JOP, the Java Optimized Processor
--
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
--
--	Testbench for the RTTM
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

entity tb_rttm is
end;

architecture tb of tb_rttm is


	signal clk			: std_logic := '1';
	signal reset		: std_logic;

	signal from_cpu		: sc_out_type;
	signal to_cpu		: sc_in_type;

	-- size of main memory simulation in 32-bit words.
	-- change it to less memory to speedup the simulation
	-- minimum is 64 KB, 14 bits
	constant  MEM_BITS	: integer := 15;

begin

	tm: entity work.tm
		generic map(
			addr_width => SC_ADDR_SIZE,
			way_bits => 3
		)
		port map(
			clk => clk,
			reset => reset,
			
			from_cpu => from_cpu,
			to_cpu => to_cpu
		);
		
--	100 MHz clock
		
clock: process
begin
   wait for 5 ns; clk  <= not clk;
end process clock;

process
begin
	reset <= '1';
	wait for 8 ns;
	reset <= '0';
	wait;
end process;

process
	variable result: natural;
begin

	from_cpu.address <= (others => '0');
	from_cpu.wr_data <= (others => '0');
	from_cpu.wr <= '0';
	from_cpu.rd <= '0';

	wait until rising_edge(clk);
	wait until rising_edge(clk);

	sc_write(clk, 0, 16000, from_cpu, to_cpu);
	sc_write(clk, 123, 456, from_cpu, to_cpu);
	sc_write(clk, 4711, 15, from_cpu, to_cpu);
	sc_read(clk, 123, result, from_cpu, to_cpu);
	sc_write(clk, 0, 255, from_cpu, to_cpu);
	sc_read(clk, 0, result, from_cpu, to_cpu);



	wait;
end process;

end tb;

