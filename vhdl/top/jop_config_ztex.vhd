library ieee;
use ieee.std_logic_1164.all;

use work.jop_config_global.all;

package jop_config is

	constant clk_freq : integer := 93000000;

	-- constant for on-chip memory
	constant ram_width : integer := STACK_SIZE_GLOBAL;	-- address bits of internal ram (sp,...)

end jop_config;

package body jop_config is

end jop_config;
