--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
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
--	cache.vhd
--
--	Bytecode caching
--
--
--	2005-01-11	first version
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity cache is
generic (jpc_width : integer);

port (

	clk, reset	: in std_logic;

	bc_len		: in std_logic_vector(9 downto 0);		-- length of method in words
	bc_addr		: in std_logic_vector(17 downto 0);		-- memory address of bytecode

	find		: in std_logic;							-- start lookup

	-- start of method in bc cache
	-- in 32 bit words - we load only at word boundries
	bcstart		: out std_logic_vector(jpc_width-3 downto 0);

	rdy			: out std_logic;						-- lookup finished
	in_cache	: out std_logic							-- method is in cache

);
end cache;

architecture rtl of cache is

--
--	signals for mem interface
--
	type state_type		is (
							idle, s1, s2
						);
	signal state 		: state_type;

	signal block_addr	: std_logic_vector(jpc_width-3 downto 0);
	signal bla, blb		: std_logic_vector(17 downto 0);
	signal nxt			: std_logic;

begin

	bcstart <= block_addr;

process(clk, reset, find)

begin
	if (reset='1') then
		state <= idle;
		rdy <= '1';
		in_cache <= '0';
		block_addr <= (others => '0');
		nxt <= '0';
		bla <= (others => '0');
		blb <= (others => '0');

	elsif rising_edge(clk) then

		case state is

			when idle =>
				state <= idle;
				rdy <= '1';
				if find = '1' then
					rdy <= '0';
					state <= s1;
				end if;
				-- block_addr is updated in s1 and now
				-- contains old value
				-- use the other block on a load
				nxt <= not block_addr(7);

			-- check for a hit
			when s1 =>
				in_cache <= '0';
				block_addr <= nxt & "0000000";
				state <= s2;

				if bla = bc_addr then
					block_addr <= "00000000";
					in_cache <= '1';
					state <= idle;
				end if;
				if blb = bc_addr then
					block_addr <= "10000000";
					in_cache <= '1';
					state <= idle;
				end if;

			-- correct tag memory on a miss
			when s2 =>
				if nxt = '0' then
					bla <= bc_addr;
					if bc_len(9 downto 7) /= "000" then
						blb <= (others => '0');
					end if;
				else
					blb <= bc_addr;
					if bc_len(9 downto 7) /= "000" then
						bla <= (others => '0');
					end if;
				end if;
				state <= idle;


		end case;
					
	end if;
end process;

end rtl;
