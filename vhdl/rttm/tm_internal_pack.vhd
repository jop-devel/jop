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

package tm_internal_pack is

	--
	-- TM STATE MACHINE
	--

	type state_type is (
		BYPASS,

		TRANSACTION,
		WAIT_TOKEN,
		COMMIT,

		EARLY_WAIT_TOKEN,
		EARLY_FLUSH,
		EARLY_COMMIT, -- same for all kinds of early commits

		ABORT
		);

end package tm_internal_pack;

package body tm_internal_pack is
end package body tm_internal_pack;
