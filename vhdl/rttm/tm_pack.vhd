library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_config.ram_width;
use work.sc_pack.all;

package tm_pack is
	subtype data is std_logic_vector(31 downto 0);
	type data_array is array(natural range <>) of data;
		
	-- Magic address to execute TM commands is located in upper half 
	-- of external SRAM mirror (TM_MAGIC_DETECT) 
	-- The lower bits are actually ignored.	
	constant TM_MAGIC_DETECT: std_logic_vector(18 downto 17) := 
		(others => '1');
	constant TM_MAGIC: std_logic_vector(SC_ADDR_SIZE-1 downto 0) := 
		(SC_ADDR_SIZE-1 downto 19 => '0', 18 downto 0 => '1');
		
		 

	constant TM_CMD_WIDTH: integer := 2;
	subtype tm_cmd_raw is std_logic_vector(TM_CMD_WIDTH-1 downto 0);
	
	constant TM_CMD_END_TRANSACTION: tm_cmd_raw := "00";
	constant TM_CMD_START_TRANSACTION: tm_cmd_raw := "01";
	constant TM_CMD_ABORTED: tm_cmd_raw := "10";
	constant TM_CMD_EARLY_COMMIT: tm_cmd_raw := "11";
	
	type tm_cmd_type is (
		end_transaction,
		start_transaction,
		aborted,
		early_commit,
		none
	);
	
	-- TODO width too pessimistic
	subtype nesting_cnt_type is unsigned((ram_width-2)-1 downto 0);
	
	constant sc_out_idle		: sc_out_type := (
								(others => '0'),
								(others => '0'),
								'0', '0', '0', '0', '0'); 
	
end package tm_pack;
