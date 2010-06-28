--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009, Peter Hilber (peter@hilber.name)
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


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config.ram_width;
use work.sc_pack.all;

package tm_pack is
	subtype data is std_logic_vector(31 downto 0);
	type data_array is array(natural range <>) of data;
		
	constant TM_MAGIC_SIMULATION: std_logic_vector(SC_ADDR_SIZE-1 downto 0) := 
		(SC_ADDR_SIZE-1 downto 19 => '0', 18 downto 0 => '1');
		
	constant TM_MAGIC_DETECT_SIMULATION: std_logic_vector(18 downto 17) := 
		(others => '1');

	constant TM_CMD_WIDTH: integer := 2;
	subtype tm_cmd_raw is std_logic_vector(TM_CMD_WIDTH-1 downto 0);
	
	-- keep in synch w/ enumeration type order
	constant TM_CMD_END_TRANSACTION: tm_cmd_raw := "00";
	constant TM_CMD_START_TRANSACTION: tm_cmd_raw := "01";
	constant TM_CMD_ABORTED: tm_cmd_raw := "10";
	constant TM_CMD_EARLY_COMMIT: tm_cmd_raw := "11";
	
	-- keep order in synch w/ constants
	type tm_cmd_type is (
		end_transaction,
		start_transaction,
		aborted,
		early_commit,
		none
	);
		
end package tm_pack;
