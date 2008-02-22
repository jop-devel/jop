library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

package sc_arbiter_pack is
	
	type arb_out_type is array (integer range <>) of sc_out_type;
	type arb_in_type is array (integer range <>) of sc_in_type;

end sc_arbiter_pack;
