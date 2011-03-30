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
--	jop_config_s3k.vhd
--
--	package for S3K definitions
--

library ieee;
use ieee.std_logic_1164.all;

use work.jop_config_global.all;

package jop_config is

	constant clk_freq : integer := 50000000;

	-- constant for on-chip memory
	constant ram_width : integer := STACK_SIZE_GLOBAL;	-- address bits of internal ram (sp,...)

end jop_config;

package body jop_config is

end jop_config;
