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
--	Testbench for the array cache
--

library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config_global.all;
use work.jop_types.all;


entity tb_acache is
end tb_acache;

architecture tb of tb_acache is

	signal clk: std_logic := '1';
	signal reset: std_logic;
	
	signal acin			: acache_in_type;
	signal acout		: acache_out_type;

begin

	ac: entity work.acache
		port map (
			clk => clk,
			reset => reset,
			acin => acin,
			acout => acout
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
	acin.handle <= (others => '0');
	acin.index <= (others => '0');
	acin.gf_val <= (others => '0');
	acin.pf_val <= (others => '0');
	acin.chk_gf <= '0';
	acin.chk_pf <= '0';
	acin.wr_gf <= '0';
	acin.wr_pf <= '0';
	acin.inval <= '0';
	
	wait for 21 ns;
	
	-- first field access
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"00";
	acin.chk_gf <= '1';
	
	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.chk_gf <= '0';
	
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"00";
	acin.gf_val <= X"0000abcd";
	acin.wr_gf <= '1';
	
	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.gf_val <= (others => '0');
	acin.wr_gf <= '0';

	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"00";
	acin.chk_gf <= '1';

	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.gf_val <= (others => '0');
	acin.chk_gf <= '0';

	-- different object field access
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"3344";
	acin.index(7 downto 0) <= X"00";
	acin.chk_gf <= '1';
	
	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.chk_gf <= '0';
	
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"3344";
	acin.index(7 downto 0) <= X"00";
	acin.gf_val <= X"00004444";
	acin.wr_gf <= '1';
	
	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.gf_val <= (others => '0');
	acin.wr_gf <= '0';

	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"00";
	acin.chk_gf <= '1';

	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.gf_val <= (others => '0');
	acin.chk_gf <= '0';

	wait for 10 ns;
	acin.handle(15 downto 0) <= X"3344";
	acin.index(7 downto 0) <= X"00";
	acin.chk_gf <= '1';

	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.gf_val <= (others => '0');
	acin.chk_gf <= '0';

	wait;

end process;	

end tb;
