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
--	jop_config_global.vhd
--
--	package for global JOP configuration
--
--	Constants ending with _GLOBAL can be overwritten by
--	local configurations (and shall not be used directly).
--	Constants without the _GLOBAL suffix are used directly.
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config_global is

	-- on-chip memory size (stack plus JVM vaiables and constants)
	constant STACK_SIZE_GLOBAL : integer := 8; -- # of address bits of internal ram (sp,...)
	
	-- enable or diable the object cache
	constant USE_OCACHE : std_logic := '1';
	
	-- depends on main memry size (sc_pack)
	constant OCACHE_ADDR_BITS : integer := 23; -- TODO: align with other memory parameters
	constant OCACHE_WAY_BITS : integer := 3;
	-- current field index is 8 bit, but JOPizer allows only 32 fields
	-- assume that the number of maximum fields per object will not
	-- grow beyond 256 in the next years
	constant OCACHE_MAX_INDEX_BITS : integer := 8;
	-- number of fields per cache line
	constant OCACHE_INDEX_BITS : integer := 0;
	
end package jop_config_global;

package body jop_config_global is
end package body jop_config_global;
