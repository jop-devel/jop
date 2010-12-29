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

entity top_tag is
port (
	clk, reset		: in std_logic;
	din				: in std_logic;
	dout			: out std_logic
);
end top_tag;

architecture rtl of top_tag is


	constant  MEM_BITS	: integer := 24;
	constant  WAY_BITS	: integer := 4;

	signal x: std_logic;

	signal clk_int: std_logic;

	signal inval_reg: std_logic;
	signal addr_reg: std_logic_vector(MEM_BITS-1 downto 0);
	--		wraddr      : in std_logic_vector(addr_width-1 downto 0);
	signal wr_reg, hit_reg: std_logic;
	signal line_reg: unsigned(WAY_BITS-1 downto 0);
	
	signal inreg: std_logic_vector(MEM_BITS-1+2 downto 0);
	signal outreg: std_logic_vector(WAY_BITS-1+1 downto 0);

	-- size of main memory simulation in 32-bit words.
	-- change it to less memory to speedup the simulation
	-- minimum is 64 KB, 14 bits

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
		generic map(
			addr_width => MEM_BITS,
			way_bits => WAY_BITS
		)
		port map(
			clk => clk_int,
			reset => reset,
			invalidate => inval_reg,
			addr => addr_reg,
	--		wraddr      : in std_logic_vector(addr_width-1 downto 0);
			wr => wr_reg,
			hit => hit_reg,
			line => line_reg
		);
		
		
process(clk_int)
begin

	-- some registers for fmax tests
	if rising_edge(clk_int) then
		x <= din;
		inreg(MEM_BITS-1+2 downto 1) <= inreg(MEM_BITS-1+2-1 downto 0);
		inreg(0) <= x;
		addr_reg <= inreg(MEM_BITS-1 downto 0);
		inval_reg <= inreg(MEM_BITS);
		wr_reg <= inreg(MEM_BITS+1);
		outreg(WAY_BITS-1 downto 0) <= std_logic_vector(line_reg);
		outreg(WAY_BITS) <= hit_reg;
		
		if outreg(WAY_BITS downto 0) = (outreg'range => '0') then
			dout <= '1';
		else
			dout <= '0';
		end if;
	end if;
end process;


end rtl;

