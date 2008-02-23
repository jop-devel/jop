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
--	cyc_conf.vhd
--
--	generate PS configuration stream for Cyclone from ROM
--	
--	resources on MAX7064
--
--		xx LCs
--
--	timing for Cyclone:
--		nConfig low							min 40 us	!!!
--		nConfig high to nStatus high		max 40 us	!!!
--		nStatus high to first rising DCLK	min 1 us
--		nConfig high to first rising DCLK	min 40 us	!!!
--		DCLK clk							max 100 MHz
--
--
--	todo:
--		nothing
--
--	2000-12-09	first idea
--	2001-10-26	creation
--	2002-12-30	serial config for cyclone
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.std_logic_unsigned.all;				-- this is not standard

entity cyc_conf is

port (
	clk			: in std_logic;							-- 20 MHz ?

	a			: out std_logic_vector(17 downto 0);	-- ROM adr
	d			: in std_logic_vector(7 downto 0);		-- ROM data

	nconfig		: out std_logic;						-- Cyclone nConfig
	conf_done	: in std_logic;							-- Cyclone conf_done

	dclk		: buffer std_logic;						-- Cyclone dclk
	data		: out std_logic;						-- Cyclone serial data

	nreset		: in std_logic							-- reset from watchdog
);
end cyc_conf ;

architecture test of cyc_conf is

	signal state 		: std_logic_vector(2 downto 0);

	signal sr			: std_logic_vector(7 downto 0);		-- shift register
	signal ar			: std_logic_vector(17 downto 0);	-- adress register
	signal cnt			: std_logic_vector(2 downto 0);		-- bit counter
	signal wcnt			: std_logic_vector(10 downto 0);	-- wcnt counter

constant start 			:std_logic_vector(2 downto 0) := "000";
constant first40us		:std_logic_vector(2 downto 0) := "001";
constant status			:std_logic_vector(2 downto 0) := "010";
constant second40us		:std_logic_vector(2 downto 0) := "011";
constant firstbit		:std_logic_vector(2 downto 0) := "100";
constant config			:std_logic_vector(2 downto 0) := "101";
constant resacex		:std_logic_vector(2 downto 0) := "110";
constant running		:std_logic_vector(2 downto 0) := "111";

begin

--
--	state machine
--
process(clk, nreset)

begin

	if nreset='0' then

		state <= start;
		ar <= (others => '0');
		sr <= (others => '0');
		cnt <= (others => '0');
		wcnt <= (others => '0');
		nconfig <= '0';
		dclk <= '0';
		data <= '0';

	else
		if rising_edge(clk) then
	
			case state is
	
				when start =>
					ar <= (others => '0');
					cnt <= (others => '0');
					wcnt <= (others => '0');
					nconfig <= '0';
					dclk <= '0';
					data <= '0';
	
					state <= first40us;
	
				when first40us =>
					wcnt <= wcnt + 1;
					if wcnt(10)='1' then
						state <= status;
					end if;
					
				when status =>
					wcnt <= (others => '0');
					nconfig <= '1';
					sr <= d;					-- read first byte
					ar <= ar + 1;
					state <= second40us;
					
				when second40us =>
					wcnt <= wcnt + 1;
					if wcnt(10)='1' then
						state <= firstbit;
					end if;
	
				when firstbit =>
					wcnt <= (others => '0');
					data <= sr(0);			-- first bit
					sr(6 downto 0) <= sr(7 downto 1);
					cnt <= cnt + 1;
					state <= config;
	
				when config =>
					if dclk='0' then
						dclk <= '1';
					else
						dclk <= '0';
						data <= sr(0);
	
						cnt <= cnt + 1;
						if (cnt="111") then
							sr <= d;
							ar <= ar + 1;
							if conf_done='1' then
								state <= running;
							end if;
						else
							sr(6 downto 0) <= sr(7 downto 1);
						end if;
					end if;
					
				when running =>

				when others =>
					state <= running;
					
			end case;
		end if;
	end if;

end process;

process (state, ar)
begin

	if state=running then
		a <= (others => 'Z');
	else
		a <= ar;
	end if;

end process;

end test;
