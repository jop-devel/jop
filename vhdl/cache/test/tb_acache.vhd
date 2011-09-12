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
	variable ix: unsigned(7 downto 0);
	
begin
	acin.handle <= (others => '0');
	acin.index <= (others => '0');
	acin.ial_val <= (others => '0');
	acin.ias_val <= (others => '0');
	acin.wr_ial_idx <= (others => '0');
	acin.chk_ial <= '0';
	acin.chk_ias <= '0';
	acin.wr_ial <= '0';
	acin.wr_ias <= '0';
	acin.inval <= '0';
	
	wait for 21 ns;
	
	-- first field access
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"01";
	acin.chk_ial <= '1';
	
	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.chk_ial <= '0';
	
	-- should be a miss, we need to fill the cache line

	ix := to_unsigned(0, 8);
	for i in 0 to (2**ACACHE_FIELD_BITS)-1 loop

--		ix := std_logic_vector(unsigned(i, 8));
		wait for 10 ns;
		acin.handle(15 downto 0) <= X"1234";
		acin.index(7 downto 0) <= std_logic_vector(ix); -- should be ix
		acin.ial_val <= std_logic_vector(ix) & X"00abcd";
		-- index is registerd on chk_gf in acache
		--	either register it here again or just do
		--	an the counter internal in the cache
		acin.wr_ial <= '1';
		
		wait for 10 ns;
		acin.handle <= (others => '0');
		acin.ial_val <= (others => '0');
		acin.wr_ial <= '0';
		
		ix := ix+1;

	end loop;

	-- this should now be a hit
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"01";
	acin.chk_ial <= '1';

	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.ial_val <= (others => '0');
	acin.chk_ial <= '0';
	
	-- this should now be a spatial hit
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"00";
	acin.chk_ial <= '1';

	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.ial_val <= (others => '0');
	acin.chk_ial <= '0';

	-- different object field access
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"3344";
	acin.index(7 downto 0) <= X"00";
	acin.chk_ial <= '1';
	
	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.chk_ial <= '0';
	
	wait for 10 ns;
	acin.handle(15 downto 0) <= X"3344";
	acin.index(7 downto 0) <= X"00";
	acin.ial_val <= X"00004444";
	acin.wr_ial <= '1';
	
	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.ial_val <= (others => '0');
	acin.wr_ial <= '0';

	wait for 10 ns;
	acin.handle(15 downto 0) <= X"1234";
	acin.index(7 downto 0) <= X"00";
	acin.chk_ial <= '1';

	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.ial_val <= (others => '0');
	acin.chk_ial <= '0';

	wait for 10 ns;
	acin.handle(15 downto 0) <= X"3344";
	acin.index(7 downto 0) <= X"00";
	acin.chk_ial <= '1';

	wait for 10 ns;
	acin.handle <= (others => '0');
	acin.ial_val <= (others => '0');
	acin.chk_ial <= '0';

	wait;

end process;	

end tb;
