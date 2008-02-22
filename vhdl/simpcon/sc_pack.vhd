--
--	sc_pack.vhd
--
--	Package for SimpCon defines
--
--	Author: Martin Schoeberl (martin@jopdesign.com)
--	
--
--	2007-03-16  first version
--
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package sc_pack is

	-- two more bits than needed for the main memory
	--    one to distinguishe between memory and IO access
	--    one more to allow memory mirroring for size auto
	--        detection at boot time
	constant SC_ADDR_SIZE : integer := 23;
	constant RDY_CNT_SIZE : integer := 2;

	type sc_out_type is record
		address		: std_logic_vector(SC_ADDR_SIZE-1 downto 0);
		wr_data		: std_logic_vector(31 downto 0);
		rd			: std_logic;
		wr			: std_logic;
		atomic	: std_logic;
	end record;

	type sc_in_type is record
		rd_data		: std_logic_vector(31 downto 0);
		rdy_cnt		: unsigned(RDY_CNT_SIZE-1 downto 0);
	end record;
	
	type sc_out_array_type is array (integer range <>) of sc_out_type;
	type sc_in_array_type is array (integer range <>) of sc_in_type;
	
end sc_pack;
