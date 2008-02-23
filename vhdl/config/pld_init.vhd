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
--	pld_init.vhd
--
--	Pinout wie pld_init auf BB board.
--
--	nce und noe wird durchgeschleift um erstes Programmieren von
--	Flash zu erlauben.
--	
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.std_logic_unsigned.all;

entity pld_init is

port (
	clk		: in std_logic;
	nreset	: in std_logic;

	a		: out std_logic_vector(16 downto 0);	-- FLASH adr
	noe_in	: in std_logic;							-- input from ACEX
	nce_in	: in std_logic;							-- input from ACEX
	noe		: out std_logic;						-- output to FLASH
	nce		: out std_logic;						-- output to FLASH
	d0in	: in std_logic;							-- D0 from FLASH
	d0out	: out std_logic;						-- reseved DATA0 to ACEX

	nconf	: out std_logic;						-- ACEX nConfig
	nstatus	: in std_logic;							-- ACEX nStatus			-- not used
	conf_done	: in std_logic;						-- ACEX conf_done

	csacx	: out std_logic;						-- ACEX CS ???
	nws		: out std_logic;						-- ACEX nWS
	nbsy	: in std_logic;							-- ACEX RDYnBSY			-- not used

	resacx	: out std_logic							-- ACEX reset line

);
end pld_init ;

architecture rtl of pld_init is

begin

	nconf <= '1';
	nws <= '1';
	resacx <= '0';			-- will be changed to neg. reset (some day)
	csacx <= '0';
	
	
	a <= (others => 'Z');
	d0out <= '1';
	noe <= noe_in;
	nce <= nce_in;

end rtl;
