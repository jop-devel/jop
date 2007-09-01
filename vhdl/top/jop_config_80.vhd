--
--	jop_config_80.vhd
--
--	package for 80MHz definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	-- constants for 20MHz input and 80MHz internal clock
	constant clk_freq : integer := 80000000;
	constant pll_mult : natural := 4;
	constant pll_div : natural := 1;

	-- constant for on-chip memory
	constant ram_width : integer := 8;	-- address bits of internal ram (sp,...)

end jop_config;

package body jop_config is

end jop_config;
