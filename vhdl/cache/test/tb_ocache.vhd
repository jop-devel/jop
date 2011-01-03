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
--	Testbench for the object cache
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.jop_types.all;


entity tb_ocache is
end tb_ocache;

architecture tb of tb_ocache is

	signal clk: std_logic := '1';
	signal reset: std_logic;
	
	signal ocin			: ocache_in_type;
	signal ocout		: ocache_out_type;

begin

	oc: entity work.ocache
		port map (
			clk => clk,
			reset => reset,
			ocin => ocin,
			ocout => ocout
		);
--	100 MHz clock
		
clock: process
   begin
   wait for 5 ns; clk  <= not clk;
end process clock;

process
begin
	reset <= '1';
	wait for 11 ns;
	reset <= '0';
	wait;
end process;

process
begin
	ocin.handle <= (others => '0');
	ocin.index <= (others => '0');
	ocin.gf_val <= (others => '0');
	ocin.pf_val <= (others => '0');
	ocin.chk_gf <= '0';
	ocin.chk_pf <= '0';
	ocin.wr_gf <= '0';
	ocin.wr_pf <= '0';
	ocin.inval <= '0';
	
	wait for 21 ns;
	
	-- first field access
	wait for 10 ns;
	ocin.handle(15 downto 0) <= X"1234";
	ocin.index(7 downto 0) <= X"00";
	ocin.chk_gf <= '1';
	
	wait for 10 ns;
	ocin.handle <= (others => '0');
	ocin.chk_gf <= '0';
	
	wait for 10 ns;
	ocin.handle(15 downto 0) <= X"1234";
	ocin.index(7 downto 0) <= X"00";
	ocin.gf_val <= X"0000abcd";
	ocin.wr_gf <= '1';
	
	wait for 10 ns;
	ocin.handle <= (others => '0');
	ocin.gf_val <= (others => '0');
	ocin.wr_gf <= '0';

	wait for 10 ns;
	ocin.handle(15 downto 0) <= X"1234";
	ocin.index(7 downto 0) <= X"00";
	ocin.chk_gf <= '1';

	wait for 10 ns;
	ocin.handle <= (others => '0');
	ocin.gf_val <= (others => '0');
	ocin.chk_gf <= '0';

	-- different object field access
	wait for 10 ns;
	ocin.handle(15 downto 0) <= X"3344";
	ocin.index(7 downto 0) <= X"00";
	ocin.chk_gf <= '1';
	
	wait for 10 ns;
	ocin.handle <= (others => '0');
	ocin.chk_gf <= '0';
	
	wait for 10 ns;
	ocin.handle(15 downto 0) <= X"3344";
	ocin.index(7 downto 0) <= X"00";
	ocin.gf_val <= X"00004444";
	ocin.wr_gf <= '1';
	
	wait for 10 ns;
	ocin.handle <= (others => '0');
	ocin.gf_val <= (others => '0');
	ocin.wr_gf <= '0';

	wait for 10 ns;
	ocin.handle(15 downto 0) <= X"1234";
	ocin.index(7 downto 0) <= X"00";
	ocin.chk_gf <= '1';

	wait for 10 ns;
	ocin.handle <= (others => '0');
	ocin.gf_val <= (others => '0');
	ocin.chk_gf <= '0';

	wait for 10 ns;
	ocin.handle(15 downto 0) <= X"3344";
	ocin.index(7 downto 0) <= X"00";
	ocin.chk_gf <= '1';

	wait for 10 ns;
	ocin.handle <= (others => '0');
	ocin.gf_val <= (others => '0');
	ocin.chk_gf <= '0';

	wait;

end process;	

end tb;
