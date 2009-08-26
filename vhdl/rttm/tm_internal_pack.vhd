library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package tm_internal_pack is

	--
	-- TM STATE MACHINE
	--

	type state_type is (
		no_transaction,

		start_normal_transaction,
		normal_transaction,
		commit_wait_token, -- TODO additional states to register commit_in_allow?
		commit,

		early_commit_wait_token,
		early_commit,
		early_committed_transaction, -- TODO same for expl./OF EC?

		end_transaction, -- TODO only for EC?
		
		rollback
		);

	--type data_array is array (integer range <>) of std_logic_vector(31 downto 0);

end package tm_internal_pack;

package body tm_internal_pack is
end package body tm_internal_pack;
