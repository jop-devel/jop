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
--	cyc_conf_init.vhd
--
--	dummy version of cyc_conf_init with all address pins tristatet.
--	
--	resources on MAX7064
--
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.std_logic_unsigned.all;				-- this is not standard

entity cyc_conf_init is

port (
	clk			: in std_logic;							-- 20 MHz ?

	a			: out std_logic_vector(17 downto 0);	-- ROM adr
	d			: in std_logic_vector(7 downto 0);		-- ROM data

	nconfig		: out std_logic;						-- Cyclone nConfig
	conf_done	: in std_logic;							-- Cyclone conf_done

	dclk		: out std_logic;						-- Cyclone dclk
	data		: out std_logic;						-- Cyclone serial data

	nreset		: in std_logic							-- reset from watchdog
);
end cyc_conf_init ;

architecture rtl of cyc_conf_init is

begin

	a <= (others => 'Z');
	nconfig <= '1';
	data <= '0';
	dclk <= '0';

end rtl;
