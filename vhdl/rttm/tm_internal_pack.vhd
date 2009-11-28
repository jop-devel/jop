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
		no_transaction,

		normal_transaction,
		commit_wait_token, -- TODO additional states to register commit_in_allow?
		commit,

		early_commit_wait_token,
		early_commit,
		early_committed_transaction, -- TODO same for expl./OF EC?

		containment
		);

	--type data_array is array (integer range <>) of std_logic_vector(31 downto 0);

end package tm_internal_pack;

package body tm_internal_pack is
end package body tm_internal_pack;
