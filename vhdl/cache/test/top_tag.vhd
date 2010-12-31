--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)
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
--	Test (resource and fmax) top level for the fifo_tag memory.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.oc_types.all;

entity top_tag is
port (
	clk, reset		: in std_logic;
		top_in		: in oc_tag_in_type;
		top_out		: out oc_tag_out_type
);
end top_tag;

architecture rtl of top_tag is


	signal clk_int: std_logic;

	signal in_reg_1, in_reg_2: oc_tag_in_type;
	signal out_reg_1, out_reg_2: oc_tag_out_type;

begin

	pll_inst : entity work.pll generic map(
		multiply_by => 10,
		divide_by => 1
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);

	tag: entity work.oc_tag
		port map(
			clk => clk_int,
			reset => reset,
			tag_in => in_reg_2,
			tag_out => out_reg_1
		);
		
		
process(clk_int)
begin

	-- some registers for fmax tests
	if rising_edge(clk_int) then
		in_reg_1 <= top_in;
		in_reg_2 <= in_reg_1;
		out_reg_2 <= out_reg_1;
		top_out <= out_reg_2;
	end if;
end process;


end rtl;

