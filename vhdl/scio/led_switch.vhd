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
--	led_switch.vhd
--
--
--	2010-06-08	created
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;

entity led_switch is
generic (cpu_id : integer := 0; cpu_cnt : integer := 1);
port (
	clk		: in std_logic;
	reset	: in std_logic;

--
--	SimpCon IO interface
--
	sc_rd		: in std_logic;
	sc_rd_data	: out std_logic_vector(31 downto 0);
	
	sc_wr		: in std_logic;
	sc_wr_data	: in std_logic_vector(31 downto 0);
	
	sc_rdy_cnt	: out unsigned(1 downto 0);
--
--	LEDs
--
	oLEDR		: out std_logic_vector(17 downto 0);
--	oLEDG		: out std_logic_vector(7 downto 0);
	
--
--	Switches
--
	iSW			: in std_logic_vector(17 downto 0)
 );
end led_switch;


architecture rtl of led_switch is

	signal local_iSW_Reg	: std_logic_vector(17 downto 0);

begin

	-- Anti metastability
	process(CLK,RESET)
	begin
		if RESET = '1' then
			local_iSW_Reg <= (others => '0');
		elsif rising_edge(CLK) then
			local_iSW_Reg <= iSW;
		end if;
	end process;
	
	process(CLK,RESET)
	begin
		if RESET = '1' then
			sc_rd_data <= (others => '0');
			oLEDR <= (others => '0');
--			oLEDG <= (others => '0');
		elsif rising_edge(CLK) then
			if sc_rd = '1' then
				sc_rd_data(17 downto 0) <= local_iSW_Reg;
				sc_rd_data(31 downto 18) <= (others => '0');
			end if;
			if sc_wr = '1' then
				oLEDR <= sc_wr_data(17 downto 0);
--				oLEDG <= sc_wr_data(25 downto 18);
			end if;
		end if;
	end process;

end rtl;

