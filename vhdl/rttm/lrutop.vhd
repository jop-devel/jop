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
--	Test top level for the RTTM
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

entity tmtop is
port (
	clk, reset		: in std_logic;
	in_cpu			: in sc_out_type;
	out_cpu			: out sc_in_type;
	mem_out		: out sc_out_type;
	mem_in		: in sc_in_type
);
end tmtop;

architecture rtl of tmtop is


	signal from_cpu		: sc_out_type;
	signal to_cpu		: sc_in_type;
	signal to_mem		: sc_out_type;
	signal from_mem		: sc_in_type;

	signal clk_int: std_logic;

	-- size of main memory simulation in 32-bit words.
	-- change it to less memory to speedup the simulation
	-- minimum is 64 KB, 14 bits
	constant  MEM_BITS	: integer := 15;

begin

	pll_inst : entity work.pll generic map(
		multiply_by => 5,
		divide_by => 1
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);

	lru: entity work.lru
		generic map(
			index_bits => 4,
			line_cnt => 16
		)
		port map(
			clk => clk_int,
			reset => reset,

			inval => '0',
			
			cpu_out => from_cpu,
			cpu_in => to_cpu,

			mem_out => to_mem,
			mem_in => from_mem
		);
		
		
process(clk_int)
begin

	if rising_edge(clk_int) then

		from_cpu <= in_cpu;
		out_cpu <= to_cpu;
		mem_out <= to_mem;
		from_mem <= mem_in;

	end if;
end process;

end rtl;

