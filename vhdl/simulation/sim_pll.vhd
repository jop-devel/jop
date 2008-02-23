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
--	dummy PLL for the simulation
--

LIBRARY ieee;
USE ieee.std_logic_1164.all;

ENTITY pll IS
	generic (multiply_by : natural; divide_by : natural);
	PORT
	(
		inclk0		: IN STD_LOGIC  := '0';
		c0		: OUT STD_LOGIC 
	);
END pll;


ARCHITECTURE SYN OF pll IS

BEGIN

    assert multiply_by = 1 and divide_by = 1 
		report "PLL factors have to be 1 for dummy PLL" severity ERROR;
	c0 <= inclk0;

END SYN;
