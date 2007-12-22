--
--	bytecode.vhd
--
--	Show bytecode mnemonic in the simulation
--
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--
--	2007-12-22	creation
--


library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity bytecode is

port (jinstr : in std_logic_vector(7 downto 0));
end bytecode;

architecture sim of bytecode is

	type bcval is (iadd, isub
	);
	signal val : bcval;

begin

	val <= bcval'val(to_integer(unsigned(jinstr)));

end sim;
