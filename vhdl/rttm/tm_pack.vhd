library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package tm_pack is
	subtype data is std_logic_vector(31 downto 0);
	type data_array is array(integer range <>) of data;

	constant TM_CMD_WIDTH: integer := 2;
	subtype tm_cmd is std_logic_vector(TM_CMD_WIDTH-1 downto 0);
	
	constant TM_CMD_START_TRANSACTION: tm_cmd := "01";
	constant TM_CMD_END_TRANSACTION: tm_cmd := "00";
	constant TM_CMD_EARLY_COMMIT: tm_cmd := "10";
end package tm_pack;

package body tm_pack is
end package body tm_pack;
